package com.dev.thecodecup.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.dev.thecodecup.R;
import com.dev.thecodecup.utils.TokenExpirationTester;

/**
 * Debug Activity để test token expiration handling
 * 
 * Thêm activity này vào AndroidManifest.xml để test:
 * <activity
 *     android:name=".activity.TokenTestActivity"
 *     android:exported="true" />
 * 
 * Sau đó có thể mở bằng ADB:
 * adb shell am start -n com.dev.thecodecup/.activity.TokenTestActivity
 */
public class TokenTestActivity extends BaseAuthActivity {
    
    private TextView tvTokenStatus;
    private Button btnSimulateExpiration;
    private Button btnCheckTokenStatus;
    private Button btnRefresh;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Tạo layout đơn giản programmatically
        // (trong production, nên tạo layout XML riêng)
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(40, 40, 40, 40);
        
        // Title
        TextView title = new TextView(this);
        title.setText("Token Expiration Tester");
        title.setTextSize(24);
        title.setPadding(0, 0, 0, 40);
        layout.addView(title);
        
        // Status text
        tvTokenStatus = new TextView(this);
        tvTokenStatus.setText("Token status will appear here");
        tvTokenStatus.setPadding(0, 0, 0, 40);
        layout.addView(tvTokenStatus);
        
        // Check status button
        btnCheckTokenStatus = new Button(this);
        btnCheckTokenStatus.setText("Check Token Status");
        btnCheckTokenStatus.setOnClickListener(v -> checkTokenStatus());
        layout.addView(btnCheckTokenStatus);
        
        // Simulate expiration button
        btnSimulateExpiration = new Button(this);
        btnSimulateExpiration.setText("Simulate Token Expiration");
        btnSimulateExpiration.setOnClickListener(v -> simulateExpiration());
        layout.addView(btnSimulateExpiration);
        
        // Refresh button
        btnRefresh = new Button(this);
        btnRefresh.setText("Refresh Status");
        btnRefresh.setOnClickListener(v -> checkTokenStatus());
        layout.addView(btnRefresh);
        
        setContentView(layout);
        
        // Initial status check
        checkTokenStatus();
    }
    
    private void checkTokenStatus() {
        boolean hasValidToken = TokenExpirationTester.hasValidToken();
        
        String status = "Token Status:\n\n";
        if (hasValidToken) {
            status += "✓ Valid token found\n";
            status += "User is authenticated";
        } else {
            status += "✗ No valid token\n";
            status += "User needs to login";
        }
        
        tvTokenStatus.setText(status);
        
        // Log detailed info
        TokenExpirationTester.logTokenInfo();
    }
    
    private void simulateExpiration() {
        // Simulate token expiration
        TokenExpirationTester.simulateTokenExpiration(this);
        
        // Update status (though it might not show since we'll be redirected)
        tvTokenStatus.setText("Token expired! Redirecting to login...");
    }
}
