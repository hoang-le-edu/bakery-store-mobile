package com.dev.thecodecup

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dev.thecodecup.activity.AdminOrdersActivity
import com.dev.thecodecup.activity.Login
import com.dev.thecodecup.activity.ProductListActivity
import com.dev.thecodecup.activity.SplashActivity


//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//
//        // 🧪 Test API Connection - Check Logcat with filter: "NetworkTest"
//        NetworkTest.testApiConnection()
//
//        setContent {
//            // 🎨 TEMPORARY: Using ApiTestScreen to see products in UI
//            // Comment this and uncomment NavGraph below to restore normal app
////            ApiTestScreen()
//
//            // Normal app navigation (temporarily commented)
//             val navController = rememberNavController()
//             NavGraph(navController)
//        }
//    }
//}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Chuyển sang màn hình Login
        val intent = Intent(this, SplashActivity::class.java)
        startActivity(intent)

        // Kết thúc MainActivity nếu không cần giữ nó
        finish()
    }
}

//class MainActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        val intent = Intent(this, AdminOrdersActivity::class.java)
//        startActivity(intent)
//
//        // Kết thúc MainActivity nếu không cần giữ nó
//        finish()
//    }
//}
