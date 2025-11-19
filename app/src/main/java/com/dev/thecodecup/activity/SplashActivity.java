package com.dev.thecodecup.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dev.thecodecup.R;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 1500; // 1.5s

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // TODO: đổi LoginActivity thành màn login thực tế của bạn
            Intent intent = new Intent(SplashActivity.this, Login.class);
            startActivity(intent);
            finish();
        }, SPLASH_DELAY);
    }
}
