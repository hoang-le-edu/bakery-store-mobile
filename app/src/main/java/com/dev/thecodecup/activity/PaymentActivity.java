package com.dev.thecodecup.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dev.thecodecup.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = "PaymentActivity";
    private static final String SOCKET_URL = "https://socket.dotb.cloud/";

    private MaterialToolbar toolbar;
    private ImageView imgQRCode;
    private ProgressBar progressBar;
    private TextView txtPaymentStatus;
    private TextView txtCheckoutUrl;
    private MaterialButton btnCancel;
    private MaterialButton btnCheckStatus;

    private String checkoutUrl;
    private String orderId;

    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Get data from intent
        checkoutUrl = getIntent().getStringExtra("CHECKOUT_URL");
        orderId = getIntent().getStringExtra("ORDER_ID");

        if (checkoutUrl == null || checkoutUrl.isEmpty()) {
            Toast.makeText(this, "No payment information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "No order ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        generateQRCode();
        initSocketListener();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        imgQRCode = findViewById(R.id.imgQRCode);
        progressBar = findViewById(R.id.progressBar);
        txtPaymentStatus = findViewById(R.id.txtPaymentStatus);
        txtCheckoutUrl = findViewById(R.id.txtCheckoutUrl);
        btnCancel = findViewById(R.id.btnCancel);
        btnCheckStatus = findViewById(R.id.btnCheckStatus);

        // Display checkout URL
        if (txtCheckoutUrl != null) {
            txtCheckoutUrl.setText(checkoutUrl);
        }
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

    private void generateQRCode() {
        progressBar.setVisibility(View.VISIBLE);

        try {
            // Generate QR code from checkout URL
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(checkoutUrl, BarcodeFormat.QR_CODE, 512, 512);
            imgQRCode.setImageBitmap(bitmap);
            progressBar.setVisibility(View.GONE);

            Log.d(TAG, "QR code generated successfully for URL: " + checkoutUrl);
        } catch (WriterException e) {
            Log.e(TAG, "Error generating QR code", e);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPaymentStatus() {
        Toast.makeText(this, "Listening for payment status via WebSocket...", Toast.LENGTH_SHORT).show();
        txtPaymentStatus.setText("Waiting for payment confirmation...");
    }

    private void initSocketListener() {
        try {
            IO.Options options = new IO.Options();
            options.transports = new String[] { "websocket" };
            options.reconnection = true;

            socket = IO.socket(SOCKET_URL, options);

            socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "Socket server is live!");
                    runOnUiThread(() -> {
                        txtPaymentStatus.setText("Connected. Waiting for payment...");
                    });

                    // Join room for this order
                    String room = "triggerPaymentStatus/" + orderId;
                    socket.emit("join", room);
                    Log.d(TAG, "Joined room: " + room);
                }
            });

            socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e(TAG, "Cannot connect to socket server!", (Exception) args[0]);
                    runOnUiThread(() -> {
                        txtPaymentStatus.setText("Connection error. Please check manually.");
                    });
                }
            });

            socket.on("event-phenikaa", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject msg = (JSONObject) args[0];
                        boolean success = msg.optBoolean("success", false);

                        Log.d(TAG, "Payment event received: " + msg.toString());

                        runOnUiThread(() -> {
                            if (success) {
                                txtPaymentStatus.setText("Payment successful! ✓");
                                Toast.makeText(PaymentActivity.this,
                                        "Payment successful! Order confirmed.",
                                        Toast.LENGTH_LONG).show();

                                // Navigate back to main screen or orders
                                finishPaymentSuccess();
                            } else {
                                txtPaymentStatus.setText("Payment failed");
                                Toast.makeText(PaymentActivity.this,
                                        "Payment failed. Please try again.",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing payment event", e);
                    }
                }
            });

            socket.connect();
            Log.d(TAG, "Connecting to socket...");

        } catch (URISyntaxException e) {
            Log.e(TAG, "Socket URI error", e);
            Toast.makeText(this, "Failed to connect to payment service", Toast.LENGTH_SHORT).show();
        }
    }

    private void finishPaymentSuccess() {
        // Wait 2 seconds then finish
        new android.os.Handler().postDelayed(() -> {
            // You can navigate to OrdersActivity or MainActivity here
            finish();
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socket != null) {
            socket.disconnect();
            socket.off();
            Log.d(TAG, "Socket disconnected");
        }
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
