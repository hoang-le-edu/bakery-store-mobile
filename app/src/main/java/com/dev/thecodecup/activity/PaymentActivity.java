package com.dev.thecodecup.activity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dev.thecodecup.R;
import com.dev.thecodecup.services.PaymentWebSocketService;
import com.dev.thecodecup.services.PaymentUpdateListener;
import com.google.android.material.button.MaterialButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class PaymentActivity extends BaseAuthActivity implements PaymentUpdateListener {

    private static final String TAG = "PaymentActivity";
    private static final String SOCKET_URL = "https://socket.dotb.cloud/";
    private static final int REQUEST_STORAGE_PERMISSION = 1001;

    private ImageButton toolbar;
    private ImageView imgQRCode;
    private ProgressBar progressBar;
    private TextView txtPaymentStatus;
    private TextView txtCheckoutUrl;
    private MaterialButton btnCancel;
    private MaterialButton btnCheckStatus;
    private MaterialButton btnDownloadQR;

    private String checkoutUrl;
    private String qrCodeData;
    private String orderId;
    private Bitmap qrCodeBitmap;

    private Socket socket;
    private PaymentWebSocketService paymentWebSocketService;
    private boolean isPaymentCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Get data from intent
        checkoutUrl = getIntent().getStringExtra("CHECKOUT_URL");
        qrCodeData = getIntent().getStringExtra("QR_CODE");
        orderId = getIntent().getStringExtra("ORDER_ID");

        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "No order ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        generateQRCode();
        
        // Use the new WebSocket service
        initPaymentWebSocketService();
        
        // Keep existing socket implementation as fallback
        // initSocketListener();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        imgQRCode = findViewById(R.id.imgQRCode);
        progressBar = findViewById(R.id.progressBar);
        txtPaymentStatus = findViewById(R.id.txtPaymentStatus);
        txtCheckoutUrl = findViewById(R.id.txtCheckoutUrl);
        btnCancel = findViewById(R.id.btnCancel);
        btnCheckStatus = findViewById(R.id.btnCheckStatus);
        btnDownloadQR = findViewById(R.id.btnDownloadQR);

        // Hide checkout URL by default
        if (txtCheckoutUrl != null) {
            txtCheckoutUrl.setVisibility(View.GONE);
        }
    }

    private void setupListeners() {
        // Back button listener
        toolbar.setOnClickListener(v -> {
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

        btnDownloadQR.setOnClickListener(v -> downloadQRCode());
    }

    private void generateQRCode() {
        progressBar.setVisibility(View.VISIBLE);

        try {
            // Try to use QR code from PayOS first
            if (qrCodeData != null && !qrCodeData.isEmpty()) {
                Log.d(TAG, "QR code data received (length: " + qrCodeData.length() + ")");
                Log.d(TAG, "QR code data (first 100 chars): " +
                    qrCodeData.substring(0, Math.min(100, qrCodeData.length())));

                // Check if this is a base64 image or EMVCo QR string
                boolean isBase64Image = qrCodeData.startsWith("data:image") ||
                                       qrCodeData.startsWith("iVBOR") ||
                                       qrCodeData.startsWith("/9j/");

                if (isBase64Image) {
                    // Handle base64 image
                    Log.d(TAG, "Detected base64 image format");
                    String base64String = qrCodeData;
                    if (qrCodeData.contains(",")) {
                        String[] parts = qrCodeData.split(",");
                        if (parts.length > 1) {
                            base64String = parts[1];
                            Log.d(TAG, "Removed data URI prefix: " + parts[0]);
                        }
                    }

                    try {
                        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
                        qrCodeBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                        if (qrCodeBitmap != null) {
                            imgQRCode.setImageBitmap(qrCodeBitmap);
                            progressBar.setVisibility(View.GONE);
                            Log.d(TAG, "QR code loaded from base64 successfully - Size: " +
                                qrCodeBitmap.getWidth() + "x" + qrCodeBitmap.getHeight());
                            return;
                        }
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "Invalid base64 string", e);
                    }
                } else {
                    // Handle EMVCo QR string (VietQR format)
                    Log.d(TAG, "Detected EMVCo/VietQR string format");
                    try {
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        qrCodeBitmap = barcodeEncoder.encodeBitmap(qrCodeData, BarcodeFormat.QR_CODE, 512, 512);
                        imgQRCode.setImageBitmap(qrCodeBitmap);
                        progressBar.setVisibility(View.GONE);
                        Log.d(TAG, "QR code generated from EMVCo string successfully - Size: " +
                            qrCodeBitmap.getWidth() + "x" + qrCodeBitmap.getHeight());
                        return;
                    } catch (WriterException e) {
                        Log.e(TAG, "Error generating QR code from EMVCo string", e);
                    }
                }

                Log.w(TAG, "Failed to process QR code from PayOS, falling back to URL generation");
            } else {
                Log.d(TAG, "No QR code data provided, will generate from URL");
            }

            // Fallback: Generate QR code from checkout URL if available
            if (checkoutUrl != null && !checkoutUrl.isEmpty()) {
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                qrCodeBitmap = barcodeEncoder.encodeBitmap(checkoutUrl, BarcodeFormat.QR_CODE, 512, 512);
                imgQRCode.setImageBitmap(qrCodeBitmap);
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "QR code generated from URL");
            } else {
                throw new Exception("No QR code data or checkout URL available");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error generating QR code", e);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void initPaymentWebSocketService() {
        paymentWebSocketService = PaymentWebSocketService.getInstance(this);
        
        // Add this activity as a listener
        paymentWebSocketService.addPaymentUpdateListener(this);
        
        // Connect and subscribe to payment updates for this order
        paymentWebSocketService.connect();
        paymentWebSocketService.subscribeToOrderPayment(orderId);
        
        Toast.makeText(this, "Listening for payment status via WebSocket service...", Toast.LENGTH_SHORT).show();
        txtPaymentStatus.setText("Waiting for payment confirmation...");
    }
    
    private void checkPaymentStatus() {
        // Check payment status via API as backup when returning to app
        Log.d(TAG, "Checking payment status for order: " + orderId);
        
        // Show checking status
        runOnUiThread(() -> {
            txtPaymentStatus.setText("Checking payment status...");
            progressBar.setVisibility(View.VISIBLE);
        });
        
        // Call API to check current payment status
        new Thread(() -> {
            try {
                // TODO: Replace with your actual API endpoint
                // Example: GET /api/orders/{orderId}/payment-status
                // For now, simulate API call
                Thread.sleep(2000); 
                
                // Simulate API response checking
                // In real implementation, you would:
                // 1. Call your backend API: GET /api/orders/{orderId}
                // 2. Check if order.paymentStatus == "PAID"
                // 3. If paid, trigger onPaymentSuccess manually
                
                // Example API call structure:
                // String apiUrl = "https://your-api.com/api/orders/" + orderId;
                // OkHttpClient client = new OkHttpClient();
                // Request request = new Request.Builder().url(apiUrl).build();
                // Response response = client.newCall(request).execute();
                // if (response.isSuccessful()) {
                //     JSONObject orderData = new JSONObject(response.body().string());
                //     String paymentStatus = orderData.getString("paymentStatus");
                //     if ("PAID".equals(paymentStatus)) {
                //         onPaymentSuccess(orderId, orderData.getString("amount"), orderData.getString("orderNumber"));
                //     }
                // }
                
                runOnUiThread(() -> {
                    if (!isPaymentCompleted) {
                        txtPaymentStatus.setText("Waiting for payment confirmation...");
                        Toast.makeText(this, "Listening for payment updates...", Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (InterruptedException e) {
                Log.e(TAG, "Payment status check interrupted", e);
                runOnUiThread(() -> {
                    txtPaymentStatus.setText("Waiting for payment confirmation...");
                });
            }
        }).start();
    }

    // PaymentUpdateListener implementation
    @Override
    public void onPaymentSuccess(String orderId, String amount, String orderNumber) {
        runOnUiThread(() -> {
            // Verify this is for our order
            if (orderId.equals(this.orderId)) {
                txtPaymentStatus.setText("Payment successful! ✓");
                Toast.makeText(this,
                        "Payment successful! Order #" + orderNumber + " confirmed.",
                        Toast.LENGTH_LONG).show();
                
                // Update order status in local storage if needed
                updateOrderStatusLocally(orderId, "paid", "In Progress");
                
                // Navigate back to orders or main screen
                finishPaymentSuccess();
            } else {
                Log.w(TAG, "Received payment for different order: " + orderId);
            }
        });
    }

    @Override
    public void onPaymentFailed(String reason) {
        runOnUiThread(() -> {
            txtPaymentStatus.setText("Payment failed");
            Toast.makeText(this, "Payment failed: " + reason, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onPaymentError(String message) {
        runOnUiThread(() -> {
            txtPaymentStatus.setText("Error processing payment status");
            Log.e(TAG, "Payment error: " + message);
        });
    }

    @Override
    public void onConnectionStatusChanged(boolean connected, String message) {
        runOnUiThread(() -> {
            if (connected) {
                txtPaymentStatus.setText("Connected. Waiting for payment...");
            } else {
                txtPaymentStatus.setText("Connection: " + message);
            }
        });
    }

    private void initSocketListener() {
        try {
            IO.Options options = new IO.Options();
            options.transports = new String[] { "websocket" };
            options.reconnection = true;
            options.reconnectionDelay = 1000;
            options.reconnectionAttempts = 5;
            options.timeout = 10000;

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

            socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.w(TAG, "Socket disconnected: " + args[0]);
                    runOnUiThread(() -> {
                        txtPaymentStatus.setText("Connection lost. Reconnecting...");
                    });
                }
            });

            socket.on("reconnect", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(TAG, "Socket reconnected after " + args[0] + " attempts");
                    runOnUiThread(() -> {
                        txtPaymentStatus.setText("Reconnected. Waiting for payment...");
                    });
                    
                    // Rejoin room after reconnection
                    String room = "triggerPaymentStatus/" + orderId;
                    socket.emit("join", room);
                }
            });

            socket.on("event-phenikaa", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject msg = (JSONObject) args[0];
                        boolean success = msg.optBoolean("success", false);

                        Log.d(TAG, "PayOS payment event received: " + msg.toString());

                        runOnUiThread(() -> {
                            if (success) {
                                // Extract payment details from PayOS webhook data
                                JSONObject data = msg.optJSONObject("data");
                                if (data != null) {
                                    String orderIdFromPayment = data.optString("order_id", "");
                                    String amount = data.optString("amount", "");
                                    String orderNumber = data.optString("order_number", "");
                                    
                                    // Verify this is for our order
                                    if (orderIdFromPayment.equals(orderId)) {
                                        txtPaymentStatus.setText("Payment successful! ✓");
                                        Toast.makeText(PaymentActivity.this,
                                                "Payment successful! Order #" + orderNumber + " confirmed.",
                                                Toast.LENGTH_LONG).show();
                                        
                                        // Update order status in local storage if needed
                                        updateOrderStatusLocally(orderId, "paid", "In Progress");
                                        
                                        // Navigate back to orders or main screen
                                        finishPaymentSuccess();
                                    } else {
                                        Log.w(TAG, "Received payment for different order: " + orderIdFromPayment);
                                    }
                                } else {
                                    txtPaymentStatus.setText("Payment successful! ✓");
                                    Toast.makeText(PaymentActivity.this,
                                            "Payment successful! Order confirmed.",
                                            Toast.LENGTH_LONG).show();
                                    finishPaymentSuccess();
                                }
                            } else {
                                txtPaymentStatus.setText("Payment failed");
                                Toast.makeText(PaymentActivity.this,
                                        "Payment failed. Please try again.",
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing payment event", e);
                        // Handle parsing error
                    }
                }
            });

            socket.connect();
        } catch (URISyntaxException e) {
            Log.e(TAG, "Socket URI syntax error", e);
            txtPaymentStatus.setText("Error connecting to payment service");
        }
    }

    private void updateOrderStatusLocally(String orderId, String paymentStatus, String orderStatus) {
        // TODO: Implement logic to update order status in local database
    }

    private void finishPaymentSuccess() {
        // Disconnect from socket
        // socket.disconnect();
        
        // Disconnect from WebSocket service
        if (paymentWebSocketService != null) {
            paymentWebSocketService.disconnect();
        }
        
        // Return to MyOrdersActivity and refresh the list
        Intent intent = new Intent(this, MyOrdersActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void downloadQRCode() {
        if (qrCodeBitmap == null) {
            Toast.makeText(this, "QR code is not available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        } else {
            saveImage(qrCodeBitmap);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImage(qrCodeBitmap);
            } else {
                Toast.makeText(this, "Permission denied. Cannot save QR code.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveImage(Bitmap bitmap) {
        String fileName = "QRCode_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".png";
        OutputStream fos;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                fos = getContentResolver().openOutputStream(uri);
            } else {
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                File image = new File(imagesDir, fileName);
                fos = new FileOutputStream(image);
            }

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            Toast.makeText(this, "QR code saved to Pictures folder", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error saving QR code", e);
            Toast.makeText(this, "Failed to save QR code", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Disconnect from socket
        if (socket != null && socket.connected()) {
            socket.disconnect();
        }
        
        // Disconnect from WebSocket service
        if (paymentWebSocketService != null) {
            paymentWebSocketService.removePaymentUpdateListener(this);
            paymentWebSocketService.disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // When user returns to app, check payment status and reconnect WebSocket
        if (!isPaymentCompleted) {
            Log.d(TAG, "Resuming payment activity - checking status and reconnecting");
            
            // Force reconnect WebSocket service to ensure fresh connection
            if (paymentWebSocketService != null) {
                paymentWebSocketService.forceReconnect();
                paymentWebSocketService.subscribeToOrderPayment(orderId);
            }
            
            // Also check payment status via API as backup
            checkPaymentStatus();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Payment activity paused");
        // Don't disconnect WebSocket here - keep it running in background
        // The service will handle background connections
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "Payment activity stopped");
        // Keep WebSocket connection alive for background updates
        // Only disconnect in onDestroy when user completely leaves
    }
}
