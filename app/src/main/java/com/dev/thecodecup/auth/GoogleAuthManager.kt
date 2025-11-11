package com.dev.thecodecup.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.dev.thecodecup.BuildConfig
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.function.Consumer

class GoogleAuthManager(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Web Client ID loaded from BuildConfig (defined in local.properties)
    private val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID

    // Configure Legacy Google Sign-In
    private val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(webClientId)
        .requestEmail()
        .build()

    private val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(context, googleSignInOptions)

    /**
     * Get current user
     */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    /**
     * Check if user is signed in
     */
    fun isUserSignedIn(): Boolean = getCurrentUser() != null

    /**
     * Get Google Sign-In Intent
     * This will open a full-screen sign-in UI where user can enter any Google account
     */
    fun getSignInIntent(): Intent {
        Log.d(TAG, "Creating sign-in intent with webClientId: $webClientId")
        return googleSignInClient.signInIntent
    }

    /**
     * Handle sign-in result from Intent
     * This processes the Google account returned from the sign-in UI
     */
    suspend fun handleSignInResult(data: Intent?): AuthResult<FirebaseUser> {
        return try {
            Log.d(TAG, "Handling sign-in result...")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            Log.d(TAG, "Google account: ${account?.email}")

            if (account?.idToken != null) {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                val user = authResult.user

                if (user != null) {
                    Log.d(TAG, "Firebase sign-in successful: ${user.email}")
                    AuthResult.Success(user)
                } else {
                    AuthResult.Failure(Exception("Sign in failed: User is null"))
                }
            } else {
                AuthResult.Failure(Exception("No ID token"))
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google Sign-In failed: ${e.statusCode} - ${e.message}", e)
            AuthResult.Failure(Exception("Google Sign-In failed: ${e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Sign-in error", e)
            AuthResult.Failure(e)
        }
    }

     fun handleSignInResultFromJava(
        data: Intent?,
        lifecycleScope: androidx.lifecycle.LifecycleCoroutineScope,
        callback: Consumer<AuthResult<FirebaseUser>>
    ) {
        lifecycleScope.launch {
            val result = handleSignInResult(data)
            callback.accept(result)
        }
    }


    /**
     * Get Firebase ID Token for API calls
     */
    suspend fun getIdToken(): Result<String> {
        return try {
            val user = getCurrentUser()
            if (user != null) {
                val token = user.getIdToken(false).await().token
                if (token != null) {
                    Result.success(token)
                } else {
                    Result.failure(Exception("Token is null"))
                }
            } else {
                Result.failure(Exception("User not signed in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign out from both Google and Firebase
     */
    suspend fun signOut() {
        try {
            Log.d(TAG, "Signing out...")
            googleSignInClient.signOut().await()
            auth.signOut()
            Log.d(TAG, "Sign out successful")
        } catch (e: Exception) {
            Log.e(TAG, "Sign out error (ignored): ${e.message}")
            // Ignore errors during sign out
        }
    }

    sealed class AuthResult<out T> {
        data class Success<out T>(val data: T) : AuthResult<T>()
        data class Failure(val exception: Exception) : AuthResult<Nothing>()

        fun isSuccess(): Boolean = this is Success

        fun getOrNull(): T? = if (this is Success) data else null

        fun exceptionOrNull(): Exception? = if (this is Failure) exception else null
    }

    companion object {
        private const val TAG = "GoogleAuthManager"

        @Volatile
        private var instance: GoogleAuthManager? = null

        @JvmStatic
        fun getInstance(context: Context): GoogleAuthManager =
            instance ?: synchronized(this) {
                instance ?: GoogleAuthManager(context.applicationContext).also { instance = it }
            }
    }
}
