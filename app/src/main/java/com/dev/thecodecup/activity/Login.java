package com.dev.thecodecup.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.dev.thecodecup.R;
import com.dev.thecodecup.model.network.NetworkTest;

public class Login extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Test API Connection - Check Logcat with filter: "NetworkTest"
        NetworkTest.testApiConnection();
    }
}
