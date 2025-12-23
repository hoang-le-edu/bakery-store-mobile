package com.dev.thecodecup

import android.app.Application
import com.dev.thecodecup.model.auth.AuthManager
import com.dev.thecodecup.model.network.NetworkModule

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Khởi tạo nơi lưu token (SharedPreferences) 1 lần cho toàn app
        AuthManager.init(this)
        // Khởi tạo NetworkModule với application context
        NetworkModule.appContext = applicationContext
    }
}
