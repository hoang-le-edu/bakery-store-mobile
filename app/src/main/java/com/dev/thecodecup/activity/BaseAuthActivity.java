package com.dev.thecodecup.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.dev.thecodecup.model.auth.AuthManager;

/**
 * Base Activity để xử lý token hết hạn
 * Tất cả các Activity cần bảo vệ nên extend từ BaseAuthActivity này
 */
public abstract class BaseAuthActivity extends AppCompatActivity {

    public static final String ACTION_TOKEN_EXPIRED = "com.dev.thecodecup.TOKEN_EXPIRED";
    
    private final BroadcastReceiver tokenExpiredReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleTokenExpired();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Đăng ký receiver để lắng nghe sự kiện token hết hạn
        LocalBroadcastManager.getInstance(this).registerReceiver(
            tokenExpiredReceiver,
            new IntentFilter(ACTION_TOKEN_EXPIRED)
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy đăng ký receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(tokenExpiredReceiver);
    }

    /**
     * Xử lý khi token hết hạn
     * - Xóa token đã lưu
     * - Chuyển về màn hình login
     */
    private void handleTokenExpired() {
        // Xóa token
        AuthManager.INSTANCE.clearTokens();
        
        // Thông báo cho user
        Toast.makeText(this, "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
        
        // Chuyển về màn hình login
        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Broadcast event token hết hạn từ bất kỳ đâu
     */
    public static void broadcastTokenExpired(Context context) {
        Intent intent = new Intent(ACTION_TOKEN_EXPIRED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
