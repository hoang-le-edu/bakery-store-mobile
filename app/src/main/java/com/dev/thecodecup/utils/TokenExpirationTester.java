package com.dev.thecodecup.utils;

import android.content.Context;
import android.util.Log;

import com.dev.thecodecup.activity.BaseAuthActivity;
import com.dev.thecodecup.model.auth.AuthManager;

/**
 * Utility class để test token expiration handling
 * 
 * Sử dụng trong development/testing để kiểm tra xem app có tự động
 * chuyển về login khi token hết hạn hay không
 */
public class TokenExpirationTester {
    
    private static final String TAG = "TokenExpTest";
    
    /**
     * Simulate token expiration bằng cách clear token và broadcast event
     * 
     * Cách dùng: Gọi từ bất kỳ Activity nào (có thể thêm vào menu debug)
     * 
     * @param context Context của activity hiện tại
     */
    public static void simulateTokenExpiration(Context context) {
        Log.d(TAG, "=== Simulating Token Expiration ===");
        
        // Clear tokens
        AuthManager.INSTANCE.clearTokens();
        Log.d(TAG, "Tokens cleared");
        
        // Broadcast token expired event
        BaseAuthActivity.broadcastTokenExpired(context);
        Log.d(TAG, "Token expired event broadcasted");
        
        Log.d(TAG, "Expected: App should redirect to Login screen");
    }
    
    /**
     * Kiểm tra xem user có token hợp lệ không
     * 
     * @return true nếu user có token hợp lệ
     */
    public static boolean hasValidToken() {
        boolean isLoggedIn = AuthManager.INSTANCE.isLoggedIn();
        boolean isExpired = AuthManager.INSTANCE.isExpired();
        String token = AuthManager.INSTANCE.getIdTokenOrNull();
        
        Log.d(TAG, "=== Token Status ===");
        Log.d(TAG, "Is Logged In: " + isLoggedIn);
        Log.d(TAG, "Is Expired: " + isExpired);
        Log.d(TAG, "Has Token: " + (token != null && !token.isEmpty()));
        
        return isLoggedIn && !isExpired;
    }
    
    /**
     * Log token information cho debugging
     */
    public static void logTokenInfo() {
        String token = AuthManager.INSTANCE.getIdTokenOrNull();
        boolean isLoggedIn = AuthManager.INSTANCE.isLoggedIn();
        boolean isExpired = AuthManager.INSTANCE.isExpired();
        
        Log.d(TAG, "=== Token Information ===");
        Log.d(TAG, "Token exists: " + (token != null));
        if (token != null) {
            // Chỉ log 20 ký tự đầu của token cho security
            String tokenPreview = token.length() > 20 
                ? token.substring(0, 20) + "..." 
                : token;
            Log.d(TAG, "Token preview: " + tokenPreview);
        }
        Log.d(TAG, "Is logged in: " + isLoggedIn);
        Log.d(TAG, "Is expired: " + isExpired);
        Log.d(TAG, "========================");
    }
}
