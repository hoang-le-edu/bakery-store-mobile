package com.dev.thecodecup.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.adapter.AdminCustomerOrderAdapter;
import com.dev.thecodecup.model.network.ApiService;
import com.dev.thecodecup.model.network.NetworkModule;
import com.dev.thecodecup.model.network.dto.AdminCustomerDetailDto;
import com.dev.thecodecup.model.network.dto.AdminCustomerDetailResponseDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCustomerDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_ID = "ORDER_ID";

    private ApiService apiService;
    private String orderId;

    private ProgressBar progressBar;
    private ScrollView scrollContent;
    private TextView tvCustomerName;
    private TextView tvEmail;
    private TextView tvPhoneNumber;
    private TextView tvDateRegistered;
    private TextView tvDateOfBirth;
    private TextView tvGender;
    private TextView tvAddress;
    private RecyclerView rvOrders;
    private AdminCustomerOrderAdapter orderAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_customer_detail);

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
        fetchCustomerDetail();
    }

    private void bindViews() {
        progressBar = findViewById(R.id.progressBar);
        scrollContent = findViewById(R.id.scrollContent);
        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        tvDateRegistered = findViewById(R.id.tvDateRegistered);
        tvDateOfBirth = findViewById(R.id.tvDateOfBirth);
        tvGender = findViewById(R.id.tvGender);
        tvAddress = findViewById(R.id.tvAddress);
        rvOrders = findViewById(R.id.rvOrders);
    }

    private void setupToolbar() {
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecycler() {
        orderAdapter = new AdminCustomerOrderAdapter();
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(orderAdapter);
        orderAdapter.setListener(order -> {
            if (order.getId() == null) return;
            Intent intent = new Intent(AdminCustomerDetailActivity.this, AdminOrderDetailActivity.class);
            intent.putExtra(AdminOrderDetailActivity.EXTRA_ORDER_ID, order.getId());
            startActivity(intent);
        });
    }

    private void fetchCustomerDetail() {
        showLoading(true);
        apiService.getAdminCustomerDetail(orderId).enqueue(new Callback<AdminCustomerDetailResponseDto>() {
            @Override
            public void onResponse(Call<AdminCustomerDetailResponseDto> call, Response<AdminCustomerDetailResponseDto> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    bindData(response.body().getData());
                } else {
                    Toast.makeText(AdminCustomerDetailActivity.this, "Cannot load customer detail", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<AdminCustomerDetailResponseDto> call, Throwable t) {
                showLoading(false);
                Toast.makeText(AdminCustomerDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void bindData(AdminCustomerDetailDto data) {
        tvCustomerName.setText(data.getFullName() != null ? data.getFullName() : "--");
        tvEmail.setText(data.getEmail() != null ? data.getEmail() : "--");
        tvPhoneNumber.setText(data.getPhoneNumber() != null ? data.getPhoneNumber() : "--");
        tvDateRegistered.setText(data.getDateRegistered() != null ? formatDate(data.getDateRegistered()) : "--");
        tvDateOfBirth.setText(data.getDateOfBirth() != null ? data.getDateOfBirth() : "--");
        tvGender.setText(data.getGender() != null ? data.getGender() : "--");

        // Build address
        StringBuilder address = new StringBuilder();
        if (data.getStreet() != null && !data.getStreet().isEmpty()) {
            address.append(data.getStreet());
        }
        if (data.getWard() != null && !data.getWard().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(data.getWard());
        }
        if (data.getDistrict() != null && !data.getDistrict().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(data.getDistrict());
        }
        if (data.getProvince() != null && !data.getProvince().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(data.getProvince());
        }

        tvAddress.setText(address.length() > 0 ? address.toString() : "--");

        // Set orders
        if (data.getOrders() != null) {
            orderAdapter.setItems(data.getOrders());
        }
    }

    private String formatDate(String dateString) {
        if (dateString == null) return "--";
        try {
            String[] parts = dateString.split(" ");
            if (parts.length > 0) {
                String[] dateParts = parts[0].split("-");
                if (dateParts.length == 3) {
                    return dateParts[2] + "/" + dateParts[1] + "/" + dateParts[0];
                }
            }
        } catch (Exception e) {
            // Return original if parsing fails
        }
        return dateString;
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        scrollContent.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
    }
}
