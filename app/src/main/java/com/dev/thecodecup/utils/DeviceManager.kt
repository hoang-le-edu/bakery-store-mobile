package com.dev.thecodecup.utils

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

object DeviceManager {
    private const val PREFS_NAME = "device_prefs"
    private const val KEY_DEVICE_ID = "device_id"
    
    private var deviceId: String? = null
    
    fun getDeviceId(context: Context): String {
        if (deviceId != null) {
            return deviceId!!
        }
        
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        deviceId = prefs.getString(KEY_DEVICE_ID, null)
        
        if (deviceId == null) {
            deviceId = generateDeviceId()
            prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        }
        
        return deviceId!!
    }
    
    private fun generateDeviceId(): String {
        return UUID.randomUUID().toString()
    }
}