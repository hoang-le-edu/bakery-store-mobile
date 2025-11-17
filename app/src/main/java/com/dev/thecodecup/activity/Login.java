package com.dev.thecodecup.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwnerKt;

import com.dev.thecodecup.R;
import com.dev.thecodecup.auth.GoogleAuthManager;
import com.dev.thecodecup.model.auth.AuthManager;
import com.dev.thecodecup.model.network.NetworkModule;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.widget.TextView;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import kotlin.jvm.functions.Function0;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;


/**
 * Đăng nhập Firebase (Identity Toolkit - VerifyPassword)
 * -> Lưu idToken/refreshToken/expiresIn vào AuthManager
 * -> Chuyển sang ProductListActivity.
 */
public class Login extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton, googleSignInButton;
    private TextView forgotPasswordText, registerButton;

    private final OkHttpClient http = new OkHttpClient();
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final int RC_GOOGLE_SIGN_IN = 1234; // request code

    private GoogleAuthManager googleAuthManager;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (AuthManager.INSTANCE.isLoggedIn()) {
            NetworkModule.INSTANCE.setTokenProvider(() -> AuthManager.INSTANCE.getValidIdTokenBlocking());
            startActivity(new Intent(Login.this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.login); // layout bạn đã gửi
        // view binding (khớp id trong login.xml)
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        googleSignInButton = findViewById(R.id.googleSignInButton);
        registerButton = findViewById(R.id.registerButton);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);


        googleAuthManager = GoogleAuthManager.getInstance(this);



        loginButton.setOnClickListener(v -> doLogin());
        googleSignInButton.setOnClickListener(v -> doLoginGoogle());
        registerButton.setOnClickListener(v -> {
            // Chuyển sang RegisterActivity
            startActivity(new Intent(Login.this, RegisterActivity.class));
        });
        forgotPasswordText.setOnClickListener(v -> doForgotPassword());

    }

//    private void doLogin() {
//        String email = String.valueOf(emailEditText.getText()).trim();
//        String pass  = String.valueOf(passwordEditText.getText()).trim();
//
//        if (email.isEmpty() || pass.isEmpty()) {
//            Toast.makeText(this, "Nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        ProgressDialog dlg = ProgressDialog.show(this, null, "Đang đăng nhập…", true, false);
//
//        new Thread(() -> {
//            try {
//                // Endpoint VerifyPassword (REST)
//                // https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=API_KEY
//                String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key="
//                        + BuildConfig.FIREBASE_WEB_API_KEY;
//
//                JSONObject body = new JSONObject();
//                body.put("email", email);
//                body.put("password", pass);
//                body.put("returnSecureToken", true);
//
//                Request req = new Request.Builder()
//                        .url(url)
//                        .post(RequestBody.create(body.toString().getBytes(StandardCharsets.UTF_8), JSON))
//                        .build();
//
//                Response res = http.newCall(req).execute();
//                String text = res.body() != null ? res.body().string() : "";
//
//                if (res.isSuccessful()) {
//                    JSONObject j = new JSONObject(text);
//                    String idToken = j.optString("idToken", "");
//                    String refreshToken = j.optString("refreshToken", "");
//                    long expiresIn = parseLongSafe(j.optString("expiresIn", "3600"), 3600L);
//
//                    if (idToken.isEmpty()) {
//                        uiFail(dlg, "Không nhận được idToken.");
//                        return;
//                    }
//
//                    // Lưu token để NetworkModule interceptor tự gắn Authorization
//                    runOnUiThread(() -> {
//                        AuthManager.INSTANCE.setTokens(idToken, refreshToken, expiresIn);
//                        NetworkModule.INSTANCE.setTokenProvider(new Function0<String>() {
//                            @Override
//                            public String invoke() {
//                                return AuthManager.INSTANCE.getValidIdTokenBlocking();
//                            }
//                        });
//                        dlg.dismiss();
//                        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(this, ProductListActivity.class));
//                        finish();
//                    });
//                } else {
//                    // lỗi từ Firebase
//                    String msg = parseFirebaseError(text);
//                    uiFail(dlg, msg);
//                }
//            } catch (IOException | JSONException e) {
//                uiFail(dlg, "Lỗi kết nối: " + e.getMessage());
//            }
//        }).start();
//    }

    private void doLoginGoogle() {
        // Mở UI Google Sign-In
        Intent signInIntent = googleAuthManager.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            ProgressDialog dlg = ProgressDialog.show(
                    this, null, "Đang đăng nhập với Google...", true, false
            );

            googleAuthManager.handleSignInResultFromJava(data, LifecycleOwnerKt.getLifecycleScope(this), result -> {
                dlg.dismiss();
                if (result.isSuccess()) {
                    FirebaseUser user = ((GoogleAuthManager.AuthResult.Success<FirebaseUser>) result).getData();
                    if (user != null) {
                        // Lấy idToken từ Firebase user
                        user.getIdToken(true).addOnCompleteListener(tokenTask -> {
                            if (tokenTask.isSuccessful()) {
                                String idToken = tokenTask.getResult().getToken();
                                AuthManager.INSTANCE.setTokens(idToken, "", 3600L);
                                NetworkModule.INSTANCE.setTokenProvider(() ->
                                        AuthManager.INSTANCE.getValidIdTokenBlocking()
                                );

                                Toast.makeText(Login.this,
                                        "Đăng nhập Google thành công",
                                        Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(Login.this, HomeActivity.class));
                                finish();
                            } else {
                                Toast.makeText(Login.this,
                                        "Không lấy được idToken: " + tokenTask.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        Toast.makeText(Login.this, "User Google rỗng", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Exception e = ((GoogleAuthManager.AuthResult.Failure) result).getException();
                    Toast.makeText(Login.this,
                            "Google Sign-In thất bại: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void doLogin() {
        String email = emailEditText.getText().toString().trim();
        String pass = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Enter password and email", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog dlg = ProgressDialog.show(
                this, null, "Signing in...", true, false);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    dlg.dismiss();

                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user == null) {
                            uiFail(dlg, "Không nhận được user từ Firebase");
                            return;
                        }

                        user.getIdToken(true).addOnCompleteListener(tokenTask -> {
                            if (tokenTask.isSuccessful()) {
                                String idToken = tokenTask.getResult().getToken();

                                // Lưu token vào AuthManager + interceptor như cũ
                                AuthManager.INSTANCE.setTokens(idToken, "", 3600L);
                                NetworkModule.INSTANCE.setTokenProvider(() ->
                                        AuthManager.INSTANCE.getValidIdTokenBlocking()
                                );

                                Toast.makeText(this, "Sign in successfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, HomeActivity.class));
                                finish();
                            } else {
                                uiFail(dlg, "Không lấy được idToken: " + tokenTask.getException().getMessage());
                            }
                        });
                    } else {
                        uiFail(dlg, "Đăng nhập thất bại: " + task.getException().getMessage());
                    }
                });
    }

    /**
     * Gửi email khôi phục mật khẩu.
     * Firebase sẽ tự gửi một link/email để người dùng tự reset pass qua web/email.
     */
    private void doForgotPassword() {
        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Email để nhận link khôi phục mật khẩu", Toast.LENGTH_LONG).show();
            return;
        }

        ProgressDialog dlg = ProgressDialog.show(
                this, null, "Đang gửi email khôi phục...", true, false);

        // Sử dụng FirebaseAuth instance
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    dlg.dismiss();

                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Đã gửi email khôi phục mật khẩu đến " + email + ". Vui lòng kiểm tra hộp thư!",
                                Toast.LENGTH_LONG).show();
                    } else {
                        // Xử lý lỗi (ví dụ: email không tồn tại)
                        Toast.makeText(this,
                                "Lỗi gửi email: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
//    private void doForgotPassword() {
//        String email = emailEditText.getText().toString().trim();
//
//        if (email.isEmpty()) {
//            Toast.makeText(this, "Vui lòng nhập Email để nhận link khôi phục mật khẩu", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        ProgressDialog dlg = ProgressDialog.show(
//                this, null, "Đang gửi email khôi phục...", true, false);
//
//        // Sử dụng FirebaseAuth instance
//        FirebaseAuth auth = FirebaseAuth.getInstance();
//        auth.sendPasswordResetEmail(email)
//                .addOnCompleteListener(task -> {
//                    dlg.dismiss();
//
//                    if (task.isSuccessful()) {
//                        // Thông báo cho người dùng kiểm tra email.
//                        // Link trong email sẽ tự mở ResetPasswordActivity
//                        Toast.makeText(this,
//                                "Đã gửi email khôi phục mật khẩu. Vui lòng kiểm tra email và nhấn vào link để đặt lại mật khẩu trong ứng dụng!",
//                                Toast.LENGTH_LONG).show();
//                    } else {
//                        Toast.makeText(this,
//                                "Lỗi gửi email: " + task.getException().getMessage(),
//                                Toast.LENGTH_LONG).show();
//                    }
//                });
//    }

    private void uiFail(ProgressDialog dlg, String msg) {
        runOnUiThread(() -> {
            dlg.dismiss();
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        });
    }

    private long parseLongSafe(String s, long def) {
        try { return Long.parseLong(s); } catch (Exception ignored) { return def; }
    }

    private String parseFirebaseError(String body) {
        try {
            JSONObject j = new JSONObject(body);
            JSONObject e = j.optJSONObject("error");
            if (e != null) {
                String message = e.optString("message", "");
                if (!message.isEmpty()) return message;
            }
        } catch (Exception ignored) {}        return "Đăng nhập thất bại.";
    }
}
