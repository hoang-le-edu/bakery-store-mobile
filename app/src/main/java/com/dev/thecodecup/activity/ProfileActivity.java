package com.dev.thecodecup.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.dev.thecodecup.R;
import com.dev.thecodecup.auth.GoogleAuthManager;
import com.dev.thecodecup.model.auth.AuthManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Trang Profile cơ bản theo theme.
 */
public class ProfileActivity extends BaseBottomNavActivity {

    private TextView tvUserName, tvUserEmail, tvUserPhone;
    private ImageView ivAvatar, btnEditProfile;
    private LinearLayout rowMyInfo, rowAddress, rowOrders;
    private Button btnLogout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        bindUserData();
        setupClicks();
        setupBottomNav();
    }

    private void initViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserPhone = findViewById(R.id.tvUserPhone);
        ivAvatar = findViewById(R.id.ivAvatar);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        rowMyInfo = findViewById(R.id.rowMyInfo);
        rowAddress = findViewById(R.id.rowAddress);
        rowOrders = findViewById(R.id.rowOrders);
        btnLogout = findViewById(R.id.btnLogout);
    }

    private void bindUserData() {
        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        String name = prefs.getString("USER_NAME", "Sweet Guest");
        String email = prefs.getString("USER_EMAIL", "guest@bepmetay.com");
        String phone = prefs.getString("USER_PHONE", "");

        tvUserName.setText(name);
        tvUserEmail.setText(email);
        tvUserPhone.setText("Phone number: " + (phone.isEmpty() ? "N/A" : phone));
    }

    private void setupClicks() {
        btnEditProfile.setOnClickListener(v -> {
            // TODO: mở màn chỉnh sửa thông tin nếu có
        });

        rowMyInfo.setOnClickListener(v -> {
            // TODO: mở màn MyInfoActivity
        });

        rowAddress.setOnClickListener(v -> {
            // TODO: mở màn AddressActivity
        });

        rowOrders.setOnClickListener(v -> {
            startActivity(new Intent(this, MyOrdersActivity.class));
        });

        btnLogout.setOnClickListener(v -> {
            handleLogout();   // bấm 1 cái là logout luôn
        });

    }

    @Override
    protected int getBottomNavMenuItemId() {
        return R.id.navigation_profile;
    }

    private void handleLogout() {
        AuthManager.INSTANCE.clearTokens();

        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        prefs.edit()
            .remove("USER_TYPE")
            .remove("ACCESS_TOKEN")
            .remove("USER_NAME")
            .remove("USER_EMAIL")
            .remove("USER_PHONE")
            .apply();

        GoogleAuthManager.getInstance(this).signOutGoogle();

        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
