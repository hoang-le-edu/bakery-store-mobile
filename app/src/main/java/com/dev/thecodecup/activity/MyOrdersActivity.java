package com.dev.thecodecup.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.adapter.OrderHistoryAdapter;
import com.dev.thecodecup.model.network.api.BakeryJavaBridge;
import com.dev.thecodecup.model.network.api.CustomerOrdersResponse;
import com.dev.thecodecup.model.network.api.Order;
import com.dev.thecodecup.model.network.api.PaymentLinkCallback;
import com.dev.thecodecup.model.network.api.PaymentLinkResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Response;

public class MyOrdersActivity extends AppCompatActivity implements OrderHistoryAdapter.OnOrderClickListener {

    private ImageButton btnBack;
    private TextView tabAll, tabWaitForApproval, tabInProgress, tabDelivered, tabCancelled;
    private RecyclerView rvOrders;

    private OrderHistoryAdapter adapter;

    private final List<Order> allOrders = new ArrayList<>();
    private final List<Order> filteredOrders = new ArrayList<>();

    private static final String FILTER_ALL = "ALL";
    private static final String FILTER_WAIT_FOR_APPROVAL = "Wait For Approval";
    private static final String FILTER_IN_PROGRESS = "In Progress";
    private static final String FILTER_DELIVERED = "Completed";
    private static final String FILTER_CANCELLED = "Cancelled";

    private String currentFilter = FILTER_ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        initViews();
        setupTabs();
        setupRecycler();
        loadOrders();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tabAll = findViewById(R.id.tabAll);
        tabWaitForApproval = findViewById(R.id.tabWaitForApproval);
        tabInProgress = findViewById(R.id.tabInProgress);
        tabDelivered = findViewById(R.id.tabDelivered);
        tabCancelled = findViewById(R.id.tabCancelled);
        rvOrders = findViewById(R.id.rvOrders);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecycler() {
        adapter = new OrderHistoryAdapter(this, this);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adapter);
    }

    private void setupTabs() {
        View.OnClickListener listener = v -> {
            int id = v.getId();
            if (id == R.id.tabWaitForApproval) {
                currentFilter = FILTER_WAIT_FOR_APPROVAL;
            } else if (id == R.id.tabInProgress) {
                currentFilter = FILTER_IN_PROGRESS;
            } else if (id == R.id.tabDelivered) {
                currentFilter = FILTER_DELIVERED;
            } else if (id == R.id.tabCancelled) {
                currentFilter = FILTER_CANCELLED;
            } else {
                currentFilter = FILTER_ALL;
            }

            updateTabUI();
            applyFilter();
        };

        tabAll.setOnClickListener(listener);
        tabWaitForApproval.setOnClickListener(listener);
        tabInProgress.setOnClickListener(listener);
        tabDelivered.setOnClickListener(listener);
        tabCancelled.setOnClickListener(listener);

        updateTabUI(); // default = ALL
    }

    private void updateTabUI() {
        resetTab(tabAll);
        resetTab(tabWaitForApproval);
        resetTab(tabInProgress);
        resetTab(tabDelivered);
        resetTab(tabCancelled);

        switch (currentFilter) {
            case FILTER_WAIT_FOR_APPROVAL:
                setTabSelected(tabWaitForApproval);
                break;
            case FILTER_IN_PROGRESS:
                setTabSelected(tabInProgress);
                break;
            case FILTER_DELIVERED:
                setTabSelected(tabDelivered);
                break;
            case FILTER_CANCELLED:
                setTabSelected(tabCancelled);
                break;
            case FILTER_ALL:
            default:
                setTabSelected(tabAll);
                break;
        }
    }

    private void resetTab(TextView tab) {
        tab.setBackground(null);
        tab.setTextColor(ContextCompat.getColor(this, android.R.color.black));
    }

    private void setTabSelected(TextView tab) {
        tab.setBackgroundResource(R.drawable.bg_order_tab_selected);
        tab.setTextColor(ContextCompat.getColor(this, android.R.color.white));
    }

    private void applyFilter() {
        filteredOrders.clear();

        if (FILTER_ALL.equals(currentFilter)) {
            filteredOrders.addAll(allOrders);
        } else {
            for (Order o : allOrders) {
                String status = o.getStatus();
                if (status == null) {
                    status = o.getOrder_status();
                }
                if (status != null && status.equals(currentFilter)) {
                    filteredOrders.add(o);
                }
            }
        }

        adapter.setOrders(filteredOrders);
    }

    private void loadOrders() {
        ProgressDialog dialog = ProgressDialog.show(this, null, "Loading orders...", true, false);

        BakeryJavaBridge.INSTANCE.loadCustomerOrders(this, (response, error) -> {
            dialog.dismiss();

            if (error != null) {
                Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (response != null && response.isSuccessful() && response.body() != null) {
                CustomerOrdersResponse ordersResponse = response.body();
                processOrders(ordersResponse.getData());
            } else {
                Toast.makeText(this, "Failed to load orders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processOrders(Map<String, List<Order>> data) {
        if (data == null) return;

        allOrders.clear();

        // Collect all orders from all status groups
        if (data.containsKey("Wait For Approval")) {
            allOrders.addAll(data.get("Wait For Approval"));
        }
        
        if (data.containsKey("In Progress")) {
            allOrders.addAll(data.get("In Progress"));
        }
        
        if (data.containsKey("Completed")) {
            allOrders.addAll(data.get("Completed"));
        }
        
        if (data.containsKey("Cancelled")) {
            allOrders.addAll(data.get("Cancelled"));
        }

        applyFilter();
    }

    @Override
    public void onOrderClick(Order order) {
        // Handle order click if needed
        // Can navigate to order detail screen
    }

    @Override
    public void onPayNowClick(Order order) {
        // Handle pay now button click - create payment link and open payment screen
        if (order == null || order.getOrder_id() == null) {
            Toast.makeText(this, "Invalid order", Toast.LENGTH_SHORT).show();
            return;
        }

        createPaymentLink(order.getOrder_id());
    }

    private void createPaymentLink(String orderId) {
        final ProgressDialog dialog = ProgressDialog.show(this, null,
                "Creating payment link...", true, false);

        BakeryJavaBridge.INSTANCE.createPaymentLink(this, orderId, new PaymentLinkCallback() {
            @Override
            public void onResult(Response<PaymentLinkResponse> response, Throwable error) {
                dialog.dismiss();

                if (error != null) {
                    Log.e("MyOrdersActivity", "Payment link error", error);
                    Toast.makeText(MyOrdersActivity.this,
                            "Payment link error: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (response != null && response.isSuccessful() && response.body() != null) {
                    PaymentLinkResponse paymentResponse = response.body();

                    if (paymentResponse.getError() == 0) {
                        Log.d("MyOrdersActivity", "Payment link created - URL: " + paymentResponse.getCheckoutUrl());
                        Log.d("MyOrdersActivity", "QR Code present: " + (paymentResponse.getQrCode() != null));
                        if (paymentResponse.getQrCode() != null) {
                            Log.d("MyOrdersActivity", "QR Code length: " + paymentResponse.getQrCode().length());
                        }
                        
                        // Open payment screen
                        Intent intent = new Intent(MyOrdersActivity.this, PaymentActivity.class);
                        intent.putExtra("CHECKOUT_URL", paymentResponse.getCheckoutUrl());
                        intent.putExtra("QR_CODE", paymentResponse.getQrCode());
                        intent.putExtra("ORDER_ID", orderId);
                        startActivity(intent);
                    } else {
                        Toast.makeText(MyOrdersActivity.this,
                                "Payment error: " + paymentResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MyOrdersActivity.this,
                            "Failed to create payment link", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
