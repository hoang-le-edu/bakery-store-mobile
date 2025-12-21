package com.dev.thecodecup.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.dev.thecodecup.R;
import com.dev.thecodecup.auth.GoogleAuthManager;
import com.dev.thecodecup.model.auth.AuthManager;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Admin profile screen with logout, similar to customer Profile.
 */
public class AdminProfileActivity extends AdminBottomNavActivity {

    private TextView tvAdminName, tvAdminEmail, tvAdminPhone;
    private ImageView ivAvatar;
    private Button btnLogout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);

        ivAvatar = findViewById(R.id.ivAvatar);
        tvAdminName = findViewById(R.id.tvAdminName);
        tvAdminEmail = findViewById(R.id.tvAdminEmail);
        tvAdminPhone = findViewById(R.id.tvAdminPhone);
        btnLogout = findViewById(R.id.btnLogout);

        bindAdminData();
        setupAdminBottomNav();

        btnLogout.setOnClickListener(v -> handleLogout());
    }

    @Override
    protected int getAdminMenuItemId() {
        return R.id.navigation_admin_user;
    }

    private void bindAdminData() {
        // Pull from saved prefs if available
        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        String email = prefs.getString("USER_EMAIL", "admin@example.com");
        String name = prefs.getString("USER_NAME", "Admin");
        String phone = prefs.getString("USER_PHONE", "");
        tvAdminName.setText(name);
        tvAdminEmail.setText(email);
        tvAdminPhone.setText("Phone number: " + (phone.isEmpty() ? "N/A" : phone));
    }

    private void handleLogout() {
        // Clear stored app tokens and user type
        AuthManager.INSTANCE.clearTokens();
        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        prefs.edit()
            .remove("USER_TYPE")
            .remove("ACCESS_TOKEN")
            .remove("USER_NAME")
            .remove("USER_EMAIL")
            .remove("USER_PHONE")
            .apply();

        // Firebase + Google sign out
        try { FirebaseAuth.getInstance().signOut(); } catch (Exception ignored) {}
        GoogleAuthManager.getInstance(this).signOutGoogle();

        // Navigate to Login and clear back stack
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
