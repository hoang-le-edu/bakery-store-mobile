package com.dev.thecodecup.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.adapter.OrderDetailProductsAdapter;
import com.dev.thecodecup.model.network.api.BakeryJavaBridge;
import com.dev.thecodecup.model.network.api.CancelOrderCallback;
import com.dev.thecodecup.model.network.api.OrderDetailCallback;
import com.dev.thecodecup.model.network.api.PaymentLinkCallback;
import com.dev.thecodecup.model.network.api.PaymentLinkResponse;
import com.dev.thecodecup.model.network.api.SuccessResponse;
import com.dev.thecodecup.model.network.dto.OrderDetailData;
import com.dev.thecodecup.model.network.dto.OrderDetailItem;
import com.dev.thecodecup.model.network.dto.OrderDetailResponse;
import com.dev.thecodecup.model.network.dto.OrderVoucher;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Response;

public class OrderDetailActivity extends BaseAuthActivity {

    private static final String TAG = "OrderDetailActivity";
    public static final String EXTRA_ORDER_ID = "ORDER_ID";

    private MaterialToolbar toolbar;
    private ImageButton btnBack;
    private TextView txtOrderNumber;
    private TextView txtOrderDate;
    private TextView txtOrderStatus;
    private TextView txtCustomerName;
    private TextView txtCustomerPhone;
    private TextView txtCustomerLevel;
    private TextView txtReceiverName;
    private TextView txtShippingAddress;
    private TextView txtPaymentMethod;
    private TextView txtSubtotal;
    private TextView txtShippingFee;
    private TextView txtDiscount;
    private TextView txtVouchers;
    private TextView txtTotal;
    private RecyclerView recyclerViewProducts;
    private MaterialButton btnPayNow;
    private MaterialButton btnCancelOrder;

    private OrderDetailProductsAdapter productsAdapter;
    private String orderId;
    private OrderDetailData orderData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        orderId = getIntent().getStringExtra(EXTRA_ORDER_ID);
        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "Order ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        loadOrderDetail();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        btnBack = findViewById(R.id.btnBack);
        txtOrderNumber = findViewById(R.id.txtOrderNumber);
        txtOrderDate = findViewById(R.id.txtOrderDate);
        txtOrderStatus = findViewById(R.id.txtOrderStatus);
        txtCustomerName = findViewById(R.id.txtCustomerName);
        txtCustomerPhone = findViewById(R.id.txtCustomerPhone);
        txtCustomerLevel = findViewById(R.id.txtCustomerLevel);
        txtReceiverName = findViewById(R.id.txtReceiverName);
        txtShippingAddress = findViewById(R.id.txtShippingAddress);
        txtPaymentMethod = findViewById(R.id.txtPaymentMethod);
        txtSubtotal = findViewById(R.id.txtSubtotal);
        txtShippingFee = findViewById(R.id.txtShippingFee);
        txtDiscount = findViewById(R.id.txtDiscount);
        txtVouchers = findViewById(R.id.txtVouchers);
        txtTotal = findViewById(R.id.txtTotal);
        recyclerViewProducts = findViewById(R.id.recyclerViewProducts);
        btnPayNow = findViewById(R.id.btnPayNow);
        btnCancelOrder = findViewById(R.id.btnCancelOrder);
    }

    private void setupToolbar() {
        if (btnBack != null) btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        productsAdapter = new OrderDetailProductsAdapter();
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProducts.setAdapter(productsAdapter);
    }

    private void loadOrderDetail() {
        final ProgressDialog dialog = ProgressDialog.show(this, null,
                "Loading order details...", true, false);

        BakeryJavaBridge.INSTANCE.loadOrderDetail(this, orderId, new OrderDetailCallback() {
            @Override
            public void onResult(Response<OrderDetailResponse> response, Throwable error) {
                dialog.dismiss();

                if (error != null) {
                    Log.e(TAG, "Error loading order detail", error);
                    Toast.makeText(OrderDetailActivity.this,
                            "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                if (response != null && response.isSuccessful() && response.body() != null) {
                    OrderDetailResponse detailResponse = response.body();
                    if (detailResponse.getData() != null && !detailResponse.getData().isEmpty()) {
                        orderData = detailResponse.getData().get(0);
                        displayOrderDetail();
                    } else {
                        Toast.makeText(OrderDetailActivity.this,
                                "Order not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(OrderDetailActivity.this,
                            "Failed to load order detail", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void displayOrderDetail() {
        DecimalFormat formatter = new DecimalFormat("#,###");

        // Order info
        txtOrderNumber.setText(orderData.getOrderNumber() != null ? orderData.getOrderNumber() : "N/A");
        
        // Format date
        if (orderData.getDateCreated() != null) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                Date date = inputFormat.parse(orderData.getDateCreated());
                txtOrderDate.setText(outputFormat.format(date));
            } catch (Exception e) {
                txtOrderDate.setText(orderData.getDateCreated());
            }
        }

        // Status - highlighted
        String status = orderData.getStatus() != null ? orderData.getStatus() : "Unknown";
        txtOrderStatus.setText(status);
        setStatusStyle(status);

        // Customer info
        txtCustomerName.setText(orderData.getCustomerName() != null ? orderData.getCustomerName() : "N/A");
        txtCustomerPhone.setText(orderData.getCustomerPhone() != null ? orderData.getCustomerPhone() : "N/A");
        txtCustomerLevel.setText(orderData.getCustomerLevel() != null ? orderData.getCustomerLevel() : "N/A");

        // Receiver info
        txtReceiverName.setText(orderData.getToName() != null ? orderData.getToName() : "N/A");
        txtShippingAddress.setText(orderData.getToAddress() != null ? orderData.getToAddress() : "N/A");
        txtPaymentMethod.setText(orderData.getPaymentMethod() != null ? orderData.getPaymentMethod() : "N/A");

        // Products
        List<OrderDetailItem> products = orderData.getOrderDetail();
        if (products != null && !products.isEmpty()) {
            productsAdapter.setProducts(products);
        }

        // Price breakdown
        int subtotal = orderData.getTotalPrice() != null ? orderData.getTotalPrice() : 0;
        int shippingFee = 0;
        if (orderData.getShippingFee() != null) {
            try {
                shippingFee = (int) Double.parseDouble(orderData.getShippingFee());
            } catch (NumberFormatException e) {
                shippingFee = 0;
            }
        }
        int discount = orderData.getDiscount() != null ? orderData.getDiscount() : 0;
        int total = orderData.getOrderTotal() != null ? orderData.getOrderTotal() : 0;

        txtSubtotal.setText(formatter.format(subtotal) + "₫");
        txtShippingFee.setText(formatter.format(shippingFee) + "₫");
        txtDiscount.setText("-" + formatter.format(discount) + "₫");
        txtTotal.setText(formatter.format(total) + "₫");

        // Vouchers
        List<OrderVoucher> vouchers = orderData.getVouchers();
        if (vouchers != null && !vouchers.isEmpty()) {
            StringBuilder voucherText = new StringBuilder();
            for (OrderVoucher voucher : vouchers) {
                if (voucherText.length() > 0) voucherText.append(", ");
                voucherText.append(voucher.getVoucherCode());
            }
            txtVouchers.setText(voucherText.toString());
            txtVouchers.setVisibility(View.VISIBLE);
        } else {
            txtVouchers.setVisibility(View.GONE);
        }

        // Pay Now button - show only for Banking + pending payment
        boolean isBanking = "Banking".equalsIgnoreCase(orderData.getPaymentMethod());
        boolean isPending = "Wait For Approval".equalsIgnoreCase(status);
        
        if (isBanking && isPending) {
            btnPayNow.setVisibility(View.VISIBLE);
            btnPayNow.setOnClickListener(v -> createPaymentLink());
        } else {
            btnPayNow.setVisibility(View.GONE);
        }

        // Cancel Order button - show only for Wait For Approval status
        if (isPending) {
            btnCancelOrder.setVisibility(View.VISIBLE);
            btnCancelOrder.setOnClickListener(v -> showCancelOrderDialog());
        } else {
            btnCancelOrder.setVisibility(View.GONE);
        }
    }

    private void setStatusStyle(String status) {
        // Always use background drawable with radius
        txtOrderStatus.setBackgroundResource(R.drawable.bg_order_status);

        // Map statuses to color resources (align with Admin screens)
        int color;
        String s = status != null ? status : "";
        if ("Wait For Approval".equalsIgnoreCase(s)) {
            color = getResources().getColor(R.color.status_waitForApproval);
        } else if ("In Progress".equalsIgnoreCase(s) || "Delivering".equalsIgnoreCase(s)) {
            color = getResources().getColor(R.color.status_inProgress);
        } else if ("Completed".equalsIgnoreCase(s)) {
            color = getResources().getColor(R.color.status_completed);
        } else if ("Cancelled".equalsIgnoreCase(s)) {
            color = getResources().getColor(R.color.status_cancelled);
        } else {
            color = getResources().getColor(android.R.color.darker_gray);
        }

        // Apply solid color to shape (avoid relying on tint default #DB5560)
        android.graphics.drawable.Drawable bg = txtOrderStatus.getBackground();
        if (bg instanceof android.graphics.drawable.GradientDrawable) {
            ((android.graphics.drawable.GradientDrawable) bg.mutate()).setColor(color);
        } else if (bg != null) {
            // Fallback to tint for non-shape drawables
            bg.setTint(color);
        }

        txtOrderStatus.setTextColor(0xFFFFFFFF); // White text
        txtOrderStatus.setPadding(24, 12, 24, 12);
    }

    private void createPaymentLink() {
        final ProgressDialog dialog = ProgressDialog.show(this, null,
                "Creating payment link...", true, false);

        BakeryJavaBridge.INSTANCE.createPaymentLink(this, orderId, new PaymentLinkCallback() {
            @Override
            public void onResult(Response<PaymentLinkResponse> response, Throwable error) {
                dialog.dismiss();

                if (error != null) {
                    Log.e(TAG, "Payment link error", error);
                    Toast.makeText(OrderDetailActivity.this,
                            "Payment link error: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (response != null && response.isSuccessful() && response.body() != null) {
                    PaymentLinkResponse paymentResponse = response.body();

                    if (paymentResponse.getError() == 0) {
                        // Open payment screen with QR code
                        Intent intent = new Intent(OrderDetailActivity.this, PaymentActivity.class);
                        intent.putExtra("CHECKOUT_URL", paymentResponse.getCheckoutUrl());
                        intent.putExtra("QR_CODE", paymentResponse.getQrCode());
                        intent.putExtra("ORDER_ID", orderId);
                        startActivity(intent);
                    } else {
                        Toast.makeText(OrderDetailActivity.this,
                                "Payment error: " + paymentResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(OrderDetailActivity.this,
                            "Failed to create payment link", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showCancelOrderDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Cancel Order")
            .setMessage("Are you sure you want to cancel this order?")
            .setPositiveButton("Cancel Order", (dialog, which) -> cancelOrder())
            .setNegativeButton("No", null)
            .show();
    }

    private void cancelOrder() {
        final ProgressDialog dialog = ProgressDialog.show(this, null,
            "Cancelling order...", true, false);

        BakeryJavaBridge.INSTANCE.cancelOrder(this, orderId, new CancelOrderCallback() {
            @Override
            public void onResult(Response<SuccessResponse> response, Throwable error) {
                dialog.dismiss();

                if (error != null) {
                    Log.e(TAG, "Cancel order error", error);
                    Toast.makeText(OrderDetailActivity.this,
                            "Error cancelling order: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (response != null && response.isSuccessful() && response.body() != null) {
                    Toast.makeText(OrderDetailActivity.this,
                            response.body().getMessage(),
                            Toast.LENGTH_SHORT).show();
                    // Reload order detail to update status
                    loadOrderDetail();
                } else {
                    String errorMsg = "Unable to cancel order";
                    if (response != null && response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }
                    Toast.makeText(OrderDetailActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String formatPrice(int price) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(price) + "₫";
    }

    /**
     * Convert review media URL to use build/assets/reviews path
     * From: https://domain.com/storage/reviews/media/filename.jpg
     * To:   https://domain.com/storage/build/assets/reviews/filename.jpg
     */
    private String convertToAssetsPath(String originalUrl) {
        if (originalUrl == null || !originalUrl.contains("/storage/reviews/media/")) {
            return originalUrl;
        }
        
        // Extract filename from the original URL
        String filename = originalUrl.substring(originalUrl.lastIndexOf("/") + 1);
        
        // Build new URL with build/assets/reviews path
        String baseUrl = originalUrl.substring(0, originalUrl.indexOf("/storage/"));
        return baseUrl + "/storage/build/assets/reviews/" + filename;
    }
}
