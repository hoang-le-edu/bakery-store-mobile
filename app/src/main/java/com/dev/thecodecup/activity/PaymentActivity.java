package com.dev.thecodecup.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.dev.thecodecup.R;
import com.google.android.material.button.MaterialButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONObject;

import java.net.URISyntaxException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import android.text.TextUtils;
import android.Manifest;
import android.content.pm.PackageManager;
import android.provider.MediaStore;

public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = "PaymentActivity";
    private static final String SOCKET_URL = "https://socket.dotb.cloud/";

    private ImageButton toolbar;
    private ImageView imgQRCode;
    private ProgressBar progressBar;
    private TextView txtPaymentStatus;
    private TextView txtCheckoutUrl;
    private MaterialButton btnCancel;
    private MaterialButton btnCheckStatus;
    private MaterialButton btnSaveQR;

    private String checkoutUrl;
    private String orderId;
    private String paymentQrContent; // Direct payment QR data/content (e.g., VietQR string)

    private Socket socket;
    private static final int REQ_WRITE_STORAGE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Get data from intent
        checkoutUrl = getIntent().getStringExtra("CHECKOUT_URL");
        // Accept direct QR content if available (prefer this over URL QR)
        paymentQrContent = getIntent().getStringExtra("PAYMENT_QR_CONTENT");
        if (TextUtils.isEmpty(paymentQrContent)) {
            // Also allow an alternate key if caller uses a different name
            paymentQrContent = getIntent().getStringExtra("QR_CONTENT");
        }
        orderId = getIntent().getStringExtra("ORDER_ID");

        // Require at least one of: direct QR content OR checkout URL
        if ((checkoutUrl == null || checkoutUrl.isEmpty()) && (paymentQrContent == null || paymentQrContent.isEmpty())) {
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
        btnSaveQR = findViewById(R.id.btnSaveQR);

        // Display checkout URL
        if (txtCheckoutUrl != null) {
            if (!TextUtils.isEmpty(checkoutUrl)) {
                txtCheckoutUrl.setText(checkoutUrl);
                txtCheckoutUrl.setVisibility(View.VISIBLE);
            } else {
                txtCheckoutUrl.setVisibility(View.GONE);
            }
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

        if (btnSaveQR != null) {
            btnSaveQR.setOnClickListener(v -> saveQrToGallery());
        }
    }

    private void generateQRCode() {
        progressBar.setVisibility(View.VISIBLE);

        try {
            // Prefer direct payment QR content if provided; otherwise, fall back to checkout URL
            String qrSource = !TextUtils.isEmpty(paymentQrContent) ? paymentQrContent : checkoutUrl;

            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(qrSource, BarcodeFormat.QR_CODE, 512, 512);
            imgQRCode.setImageBitmap(bitmap);
            progressBar.setVisibility(View.GONE);

            if (!TextUtils.isEmpty(paymentQrContent)) {
                txtPaymentStatus.setText("Scan to pay");
                Log.d(TAG, "QR code generated from direct payment content");
            } else {
                txtPaymentStatus.setText("Scan to open payment page");
                Log.d(TAG, "QR code generated from checkout URL: " + checkoutUrl);
            }
        } catch (WriterException e) {
            Log.e(TAG, "Error generating QR code", e);
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap getQrBitmapFromView() {
        if (imgQRCode == null) return null;
        Drawable drawable = imgQRCode.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        if (drawable != null) {
            int w = drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : imgQRCode.getWidth();
            int h = drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : imgQRCode.getHeight();
            if (w <= 0 || h <= 0) return null;
            Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bmp;
        }
        return null;
    }

    private void saveQrToGallery() {
        Bitmap bitmap = getQrBitmapFromView();
        if (bitmap == null) {
            Toast.makeText(this, "QR not ready yet", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveImageQPlus(bitmap);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_WRITE_STORAGE);
                return;
            }
            saveImageLegacy(bitmap);
        }
    }

    private String generateFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return "PaymentQR_" + timeStamp + ".png";
    }

    private void saveImageQPlus(Bitmap bitmap) {
        try {
            ContentValues values = new ContentValues();
            String fileName = generateFileName();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/BakeryStore");
            values.put(MediaStore.Images.Media.IS_PENDING, 1);

            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) throw new Exception("Failed to create MediaStore record");

            try (OutputStream os = getContentResolver().openOutputStream(uri)) {
                if (os == null) throw new Exception("Failed to open output stream");
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            }

            values.clear();
            values.put(MediaStore.Images.Media.IS_PENDING, 0);
            getContentResolver().update(uri, values, null, null);

            Toast.makeText(this, "Saved to Pictures/BakeryStore", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Save image failed (Q+)", e);
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageLegacy(Bitmap bitmap) {
        try {
            File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File appDir = new File(picturesDir, "BakeryStore");
            if (!appDir.exists() && !appDir.mkdirs()) {
                throw new Exception("Failed to create directory");
            }
            File file = new File(appDir, generateFileName());
            try (FileOutputStream fos = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            }
            // Make it visible in gallery
            Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            scanIntent.setData(Uri.fromFile(file));
            sendBroadcast(scanIntent);

            Toast.makeText(this, "Saved to Pictures/BakeryStore", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Save image failed (legacy)", e);
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveQrToGallery();
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
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
