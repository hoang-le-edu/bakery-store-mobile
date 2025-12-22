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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dev.thecodecup.R;
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

public class PaymentActivity extends AppCompatActivity {

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

    private void downloadQRCode() {
        if (qrCodeBitmap == null) {
            Toast.makeText(this, "QR code not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check permission for Android 9 and below
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
                return;
            }
        }

        saveQRCodeToGallery();
    }

    private void saveQRCodeToGallery() {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "QR_Payment_" + orderId + "_" + timestamp + ".png";

            OutputStream fos;
            Uri imageUri;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10 and above - use MediaStore
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BakeryPayments");

                imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (imageUri == null) {
                    throw new Exception("Failed to create MediaStore entry");
                }
                fos = getContentResolver().openOutputStream(imageUri);
            } else {
                // Android 9 and below - use legacy storage
                File imagesDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "BakeryPayments");
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs();
                }

                File image = new File(imagesDir, fileName);
                fos = new FileOutputStream(image);
                imageUri = Uri.fromFile(image);
            }

            // Save bitmap to output stream
            qrCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            if (fos != null) {
                fos.close();
            }

            // Notify gallery for Android 9 and below
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, imageUri));
            }

            Toast.makeText(this, "QR code saved to Gallery", Toast.LENGTH_LONG).show();
            Log.d(TAG, "QR code saved successfully: " + fileName);

        } catch (Exception e) {
            Log.e(TAG, "Error saving QR code", e);
            Toast.makeText(this, "Failed to save QR code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveQRCodeToGallery();
            } else {
                Toast.makeText(this, "Permission denied. Cannot save QR code.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
