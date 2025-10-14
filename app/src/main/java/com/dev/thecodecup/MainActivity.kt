package com.dev.thecodecup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.dev.thecodecup.model.network.NetworkTest
import com.dev.thecodecup.ui.NavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 🧪 Test API Connection - Check Logcat with filter: "NetworkTest"
        NetworkTest.testApiConnection()
        
        setContent {
            // 🎨 TEMPORARY: Using ApiTestScreen to see products in UI
            // Comment this and uncomment NavGraph below to restore normal app
//            ApiTestScreen()
            
            // Normal app navigation (temporarily commented)
             val navController = rememberNavController()
             NavGraph(navController)
        }
    }
}