package com.dev.thecodecup.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.dev.thecodecup.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class PaymentActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private ImageView imgQRCode;
    private ProgressBar progressBar;
    private TextView txtPaymentStatus;
    private MaterialButton btnCancel;
    private MaterialButton btnCheckStatus;

    private String checkoutUrl;
    private String qrCodeUrl;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Get data from intent
        checkoutUrl = getIntent().getStringExtra("CHECKOUT_URL");
        qrCodeUrl = getIntent().getStringExtra("QR_CODE_URL");
        orderId = getIntent().getStringExtra("ORDER_ID");

        if (qrCodeUrl == null || qrCodeUrl.isEmpty()) {
            Toast.makeText(this, "No payment information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadQRCode();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        imgQRCode = findViewById(R.id.imgQRCode);
        progressBar = findViewById(R.id.progressBar);
        txtPaymentStatus = findViewById(R.id.txtPaymentStatus);
        btnCancel = findViewById(R.id.btnCancel);
        btnCheckStatus = findViewById(R.id.btnCheckStatus);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> {
            // Show confirmation dialog
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Cancel Payment")
                    .setMessage("Are you sure you want to cancel payment? Your order will not be confirmed.")
                    .setPositiveButton("Cancel Payment", (dialog, which) -> finish())
                    .setNegativeButton("Continue", null)
                    .show();
        });

        btnCancel.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Cancel Payment")
                    .setMessage("Are you sure you want to cancel payment?")
                    .setPositiveButton("Yes", (dialog, which) -> finish())
                    .setNegativeButton("No", null)
                    .show();
        });

        btnCheckStatus.setOnClickListener(v -> checkPaymentStatus());
    }

    private void loadQRCode() {
        progressBar.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(qrCodeUrl)
                .into(imgQRCode);

        // Hide progress bar after a delay (assuming image loads)
        imgQRCode.postDelayed(() -> progressBar.setVisibility(View.GONE), 2000);
    }

    private void checkPaymentStatus() {
        // TODO: Implement WebSocket connection to check payment status
        // For now, just show a message
        Toast.makeText(this, "Checking payment status...", Toast.LENGTH_SHORT).show();

        // Simulate checking
        txtPaymentStatus.setText("Checking...");
        txtPaymentStatus.postDelayed(() -> {
            txtPaymentStatus.setText("Waiting for payment...");
            Toast.makeText(this, "Payment not received yet. Please try again later.",
                    Toast.LENGTH_SHORT).show();
        }, 2000);
    }

    @Override
    public void onBackPressed() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Cancel Payment")
                .setMessage("Are you sure you want to cancel payment? Your order will not be confirmed.")
                .setPositiveButton("Cancel Payment", (dialog, which) -> super.onBackPressed())
                .setNegativeButton("Continue", null)
                .show();
    }
}
