package com.dev.thecodecup.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dev.thecodecup.R;
 

/** Displays the signed-in user's profile details. */
public class CustomerDetailActivity extends AppCompatActivity {

    private TextView tvCustomerName;
    private TextView tvEmail;
    private TextView tvPhoneNumber;
    private TextView tvDateRegistered;
    private TextView tvDateOfBirth;
    private TextView tvGender;
    private TextView tvAddress;
    private ProgressBar progressBar;
    

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_detail);

        initViews();
        setupBack();
        bindUserData();
        
    }

    private void initViews() {
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        tvDateRegistered = findViewById(R.id.tvDateRegistered);
        tvDateOfBirth = findViewById(R.id.tvDateOfBirth);
        tvGender = findViewById(R.id.tvGender);
        tvAddress = findViewById(R.id.tvAddress);
        progressBar = findViewById(R.id.progressBar);
        
    }

    private void setupBack() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void bindUserData() {
        // Hide loader if present
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        String name = prefs.getString("USER_NAME", getString(R.string.no_data));
        String email = prefs.getString("USER_EMAIL", getString(R.string.no_data));
        String phone = prefs.getString("USER_PHONE", "");
        String address = prefs.getString("USER_ADDRESS", "");
        String dob = prefs.getString("USER_DOB", "");
        String gender = prefs.getString("USER_GENDER", "");
        String registered = prefs.getString("USER_REGISTERED_AT", "");

        tvCustomerName.setText(name.isEmpty() ? getString(R.string.no_data) : name);
        tvEmail.setText(email.isEmpty() ? getString(R.string.no_data) : email);
        tvPhoneNumber.setText(phone.isEmpty() ? getString(R.string.no_data) : phone);
        tvAddress.setText(address.isEmpty() ? getString(R.string.no_data) : address);
        tvDateOfBirth.setText(dob.isEmpty() ? getString(R.string.no_data) : dob);
        tvGender.setText(gender.isEmpty() ? getString(R.string.no_data) : gender);
        tvDateRegistered.setText(registered.isEmpty() ? getString(R.string.no_data) : registered);
    }

    
}
