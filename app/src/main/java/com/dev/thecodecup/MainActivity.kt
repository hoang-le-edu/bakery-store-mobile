package com.dev.thecodecup

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dev.thecodecup.activity.AdminOrdersActivity
import com.dev.thecodecup.activity.Login
import com.dev.thecodecup.activity.ProductListActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if user is logged in
        val currentUser = FirebaseAuth.getInstance().currentUser
        
        if (currentUser != null) {
            // User is logged in, go to ProductList
            val intent = Intent(this, ProductListActivity::class.java)
            startActivity(intent)
        } else {
            // User not logged in, go to Login
            val intent = Intent(this, com.dev.thecodecup.activity.Login::class.java)
            startActivity(intent)
        }
        
        finish()
    }
}

//class MainActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Chuy?n sang màn h?nh Login
//        val intent = Intent(this, AdminOrdersActivity::class.java)
//        startActivity(intent)
//
//        // K?t thúc MainActivity n?u không c?n gi? nó
//        finish()
//    }
//}
