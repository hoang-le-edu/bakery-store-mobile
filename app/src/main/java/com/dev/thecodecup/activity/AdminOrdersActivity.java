package com.dev.thecodecup.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.adapter.AdminOrderAdapter;
import com.dev.thecodecup.model.network.ApiService;
import com.dev.thecodecup.model.network.NetworkModule;
import com.dev.thecodecup.model.network.dto.AdminOrderDto;
import com.dev.thecodecup.model.network.dto.AdminOrdersResponseDto;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminOrdersActivity extends AdminBottomNavActivity {

    private TextView tabAll, tabWaitForApproval, tabInProgress, tabOutForDelivery, tabDelivered, tabCancelled;
    private RecyclerView rvOrders;

    private AdminOrderAdapter adapter;
    private ApiService apiService;

    private final List<AdminOrderDto> allOrders = new ArrayList<>();
    private final List<AdminOrderDto> filteredOrders = new ArrayList<>();

    private static final String FILTER_ALL = "ALL";
    private static final String FILTER_WAIT_FOR_APPROVAL = "Wait For Approval";
    private static final String FILTER_IN_PROGRESS = "In Progress";
    private static final String FILTER_OUT_FOR_DELIVERY = "Delivering";
    private static final String FILTER_DELIVERED = "Completed";
    private static final String FILTER_CANCELLED = "Cancelled";

    private String currentFilter = FILTER_ALL;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);
        setupAdminBottomNav();

        apiService = NetworkModule.INSTANCE.getApiService();

        initViews();
        setupTabs();
        setupRecycler();

        loadOrdersFromApi();
    }

    @Override
    protected int getAdminMenuItemId() {
        return R.id.navigation_admin_orders;
    }

    private void initViews() {
        tabAll = findViewById(R.id.tabAll);
        tabWaitForApproval = findViewById(R.id.tabWaitForApproval);
        tabInProgress = findViewById(R.id.tabInProgress);
        tabOutForDelivery = findViewById(R.id.tabOutForDelivery);
        tabDelivered = findViewById(R.id.tabDelivered);
        tabCancelled = findViewById(R.id.tabCancelled);
        rvOrders = findViewById(R.id.rvOrders);
    }

    private void setupRecycler() {
        adapter = new AdminOrderAdapter();
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
            } else if (id == R.id.tabOutForDelivery) {
                currentFilter = FILTER_OUT_FOR_DELIVERY;
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
        tabOutForDelivery.setOnClickListener(listener);
        tabDelivered.setOnClickListener(listener);
        tabCancelled.setOnClickListener(listener);

        updateTabUI(); // default = ALL
    }

    private void updateTabUI() {
        resetTab(tabAll);
        resetTab(tabWaitForApproval);
        resetTab(tabInProgress);
        resetTab(tabOutForDelivery);
        resetTab(tabDelivered);
        resetTab(tabCancelled);

        switch (currentFilter) {
            case FILTER_WAIT_FOR_APPROVAL:
                setTabSelected(tabWaitForApproval);
                break;
            case FILTER_IN_PROGRESS:
                setTabSelected(tabInProgress);
                break;
            case FILTER_OUT_FOR_DELIVERY:
                setTabSelected(tabOutForDelivery);
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
        tab.setBackgroundResource(R.drawable.bg_order_tab_selected); // shape hồng bo tròn
        tab.setTextColor(ContextCompat.getColor(this, android.R.color.white));
    }

    private void applyFilter() {
        filteredOrders.clear();

        if (FILTER_ALL.equals(currentFilter)) {
            filteredOrders.addAll(allOrders);
        } else {
            for (AdminOrderDto o : allOrders) {
                if (o.getOrderStatus() != null && o.getOrderStatus().equals(currentFilter)) {
                    filteredOrders.add(o);
                }
            }
        }

        adapter.setItems(filteredOrders);
    }

    private void loadOrdersFromApi() {
        apiService.getAdminOrders().enqueue(new Callback<AdminOrdersResponseDto>() {
            @Override
            public void onResponse(Call<AdminOrdersResponseDto> call, Response<AdminOrdersResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AdminOrdersResponseDto body = response.body();
                    if (body.getData() != null) {
                        allOrders.clear();
                        allOrders.addAll(body.getData());
                        applyFilter();
                    }
                } else {
                    // Handle error
                    android.widget.Toast.makeText(AdminOrdersActivity.this, 
                            "Failed to load orders", 
                            android.widget.Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AdminOrdersResponseDto> call, Throwable t) {
                // Handle failure
                android.widget.Toast.makeText(AdminOrdersActivity.this, 
                        "Error: " + t.getMessage(), 
                        android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }
}
