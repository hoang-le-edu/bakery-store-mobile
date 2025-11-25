package com.dev.thecodecup

import android.app.Application
import com.dev.thecodecup.model.auth.AuthManager

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Khởi tạo nơi lưu token (SharedPreferences) 1 lần cho toàn app
        AuthManager.init(this)
    }
}
