package com.dev.thecodecup.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.adapter.AdminOrderDetailItemAdapter;
import com.dev.thecodecup.adapter.AdminStatusHistoryAdapter;
import com.dev.thecodecup.model.network.ApiService;
import com.dev.thecodecup.model.network.NetworkModule;
import com.dev.thecodecup.model.network.api.SuccessResponse;
import com.dev.thecodecup.model.network.dto.AdminOrderDetailDto;
import com.dev.thecodecup.model.network.dto.AdminOrderDetailResponseDto;
import com.dev.thecodecup.model.network.dto.AdminStatusHistoryDto;
import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminOrderDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "ORDER_ID";

    private ApiService apiService;
    private String orderId;

    private ProgressBar progressBar;
    private ScrollView scrollContent;
    private TextView tvOrderNumber;
    private TextView tvOrderDate;
    private TextView tvStatus;
    private TextView tvTotal;
    private TextView tvCustomerName;
    private TextView tvCustomerPhone;
    private TextView tvShippingAddress;
    private TextView tvPaymentMethod;
    private TextView tvPaymentStatus;
    private TextView tvShippingFee;
    private TextView tvDiscount;
    private TextView tvLastStatusChange;
    private RecyclerView rvItems;
    private MaterialButton btnPrimary;
    private MaterialButton btnSecondary;
    private ImageButton btnStatusHistory;
    private ImageButton btnCustomerDetail;

    private final AdminOrderDetailItemAdapter itemAdapter = new AdminOrderDetailItemAdapter();
    private final AdminStatusHistoryAdapter historyAdapter = new AdminStatusHistoryAdapter();

    private AdminOrderDetailDto currentOrder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_detail);

        apiService = NetworkModule.INSTANCE.getApiService();
        orderId = getIntent().getStringExtra(EXTRA_ORDER_ID);
        if (orderId == null) {
            Toast.makeText(this, "Order ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bindViews();
        setupToolbar();
        setupRecycler();
        setupActions();
        fetchDetail();
    }

    private void bindViews() {
        progressBar = findViewById(R.id.progressBar);
        scrollContent = findViewById(R.id.scrollContent);
        tvOrderNumber = findViewById(R.id.tvOrderNumber);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvStatus = findViewById(R.id.tvStatus);
        tvTotal = findViewById(R.id.tvTotal);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvCustomerPhone = findViewById(R.id.tvCustomerPhone);
        tvShippingAddress = findViewById(R.id.tvShippingAddress);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        tvShippingFee = findViewById(R.id.tvShippingFee);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvLastStatusChange = findViewById(R.id.tvLastStatusChange);
        rvItems = findViewById(R.id.rvItems);
        btnPrimary = findViewById(R.id.btnPrimary);
        btnSecondary = findViewById(R.id.btnSecondary);
        btnStatusHistory = findViewById(R.id.btnStatusHistory);
        btnCustomerDetail = findViewById(R.id.btnCustomerDetail);
    }

    private void setupToolbar() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecycler() {
        rvItems.setLayoutManager(new LinearLayoutManager(this));
        rvItems.setAdapter(itemAdapter);
    }

    private void setupActions() {
        btnStatusHistory.setOnClickListener(v -> showStatusHistoryDialog());
        btnCustomerDetail.setOnClickListener(v ->
                Toast.makeText(this, "Customer detail page coming soon", Toast.LENGTH_SHORT).show());
    }

    private void fetchDetail() {
        showLoading(true);
        apiService.getAdminOrderDetail(orderId).enqueue(new Callback<AdminOrderDetailResponseDto>() {
            @Override
            public void onResponse(Call<AdminOrderDetailResponseDto> call, Response<AdminOrderDetailResponseDto> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    currentOrder = response.body().getData();
                    bindData(currentOrder);
                } else {
                    Toast.makeText(AdminOrderDetailActivity.this, "Cannot load order detail", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<AdminOrderDetailResponseDto> call, Throwable t) {
                showLoading(false);
                Toast.makeText(AdminOrderDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void bindData(AdminOrderDetailDto data) {
        tvOrderNumber.setText(data.getOrderNumber() != null ? data.getOrderNumber() : "#");
        tvOrderDate.setText(data.getDateCreated() != null ? data.getDateCreated() : "--");
        tvStatus.setText(data.getStatus() != null ? data.getStatus() : "--");
        tvStatus.setBackgroundResource(resolveStatusBackground(data.getStatus()));

        String total = data.getTotalPrice() != null ? data.getTotalPrice() : data.getOrderTotal();
        tvTotal.setText(formatCurrency(total));

        if (data.getCustomerInfo() != null) {
            tvCustomerName.setText(data.getCustomerInfo().getCustomerName());
            tvCustomerPhone.setText(data.getCustomerInfo().getCustomerPhone());
        }

        if (data.getShippingInfo() != null) {
            String address = data.getShippingInfo().getToAddress();
            if (address == null || address.isEmpty()) {
                address = data.getShippingInfo().getStreet();
            }
            tvShippingAddress.setText(address != null ? address : "--");
            tvShippingFee.setText(formatCurrency(data.getShippingInfo().getShippingFee()));
        }

        if (data.getPaymentInfo() != null) {
            tvPaymentMethod.setText(data.getPaymentInfo().getPaymentMethod());
            tvPaymentStatus.setText(data.getPaymentInfo().getPaymentStatus());
        }

        tvDiscount.setText(formatCurrency(data.getDiscount() != null ? String.valueOf(data.getDiscount()) : "0"));

        itemAdapter.setItems(data.getOrderDetail());

        List<AdminStatusHistoryDto> histories = data.getStatusHistory();
        historyAdapter.setItems(histories);
        if (histories != null && !histories.isEmpty()) {
            AdminStatusHistoryDto last = histories.get(histories.size() - 1);
            tvLastStatusChange.setText(last.getChangedAt());
        }

        updateActionButtons(data.getStatus());
    }

    private void updateActionButtons(String status) {
        btnPrimary.setVisibility(View.GONE);
        btnSecondary.setVisibility(View.GONE);

        if ("Wait For Approval".equalsIgnoreCase(status)) {
            configurePrimary("Mark In Progress", "In Progress");
            configureSecondary("Hủy đơn", "Cancelled");
        } else if ("In Progress".equalsIgnoreCase(status)) {
            configurePrimary("Mark Delivering", "Delivering");
            configureSecondary("Hủy đơn", "Cancelled");
        } else if ("Delivering".equalsIgnoreCase(status)) {
            configurePrimary("Mark Completed", "Completed");
            configureSecondary("Hủy đơn", "Cancelled");
        }
    }

    private void configurePrimary(String label, String nextStatus) {
        btnPrimary.setVisibility(View.VISIBLE);
        btnPrimary.setText(label);
        btnPrimary.setOnClickListener(v -> confirmAndUpdate(nextStatus));
    }

    private void configureSecondary(String label, String nextStatus) {
        btnSecondary.setVisibility(View.VISIBLE);
        btnSecondary.setText(label);
        btnSecondary.setOnClickListener(v -> confirmAndUpdate(nextStatus));
    }

    private void confirmAndUpdate(String nextStatus) {
        new AlertDialog.Builder(this)
                .setTitle("Update Status")
                .setMessage("Update order to: " + nextStatus + "?")
                .setPositiveButton("Confirm", (dialog, which) -> updateStatus(nextStatus))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateStatus(String status) {
        showLoading(true);
        HashMap<String, String> body = new HashMap<>();
        body.put("status", status);
        apiService.updateOrderStatus(orderId, body).enqueue(new Callback<SuccessResponse>() {
            @Override
            public void onResponse(Call<SuccessResponse> call, Response<SuccessResponse> response) {
                showLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(AdminOrderDetailActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                    fetchDetail();
                } else {
                    Toast.makeText(AdminOrderDetailActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SuccessResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(AdminOrderDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showStatusHistoryDialog() {
        if (currentOrder == null || currentOrder.getStatusHistory() == null || currentOrder.getStatusHistory().isEmpty()) {
            Toast.makeText(this, "No history", Toast.LENGTH_SHORT).show();
            return;
        }
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_status_history, null);
        RecyclerView rv = dialogView.findViewById(R.id.rvStatusHistory);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(historyAdapter);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Close", (DialogInterface dialog, int which) -> dialog.dismiss())
                .show();
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        scrollContent.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        btnPrimary.setEnabled(!loading);
        btnSecondary.setEnabled(!loading);
    }

    private String formatCurrency(String value) {
        if (value == null || value.isEmpty()) return "0đ";
        try {
            double num = Double.parseDouble(value);
            DecimalFormat formatter = new DecimalFormat("###,###,###");
            return formatter.format(num) + "đ";
        } catch (Exception e) {
            return value + "đ";
        }
    }

    private int resolveStatusBackground(String status) {
        if (status == null) return R.drawable.bg_order_status;
        switch (status) {
            case "Wait For Approval":
                return R.drawable.bg_status_pending;
            case "In Progress":
            case "Delivering":
                return R.drawable.bg_status_progress;
            case "Completed":
                return R.drawable.bg_status_completed;
            case "Cancelled":
                return R.drawable.bg_status_cancelled;
            default:
                return R.drawable.bg_order_status;
        }
    }
}
