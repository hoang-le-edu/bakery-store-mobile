package com.dev.thecodecup.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageButton;

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
import com.dev.thecodecup.activity.AdminOrderDetailActivity;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminOrdersActivity extends AdminBottomNavActivity {

    private TextView tabAll, tabWaitForApproval, tabInProgress, tabOutForDelivery, tabDelivered, tabCancelled;
    private RecyclerView rvOrders;
    private TextView tvEmpty;
    private EditText etSearch;
    private ImageButton btnFilter;

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
    private String dateFrom = null;
    private String dateTo = null;
    private String currentQuery = "";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);
        setupAdminBottomNav();

        apiService = NetworkModule.INSTANCE.getApiService();

        initViews();
        setupTabs();
        setupRecycler();
        setupSearchAndFilter();

        loadOrdersFromApi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list when returning from detail to reflect updated statuses
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
        tvEmpty = findViewById(R.id.tvEmpty);
        etSearch = findViewById(R.id.etSearch);
        btnFilter = findViewById(R.id.btnFilter);
    }

    private void setupRecycler() {
        adapter = new AdminOrderAdapter();
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adapter);
        adapter.setListener(order -> {
            if (order.getId() == null) return;
            android.content.Intent intent = new android.content.Intent(this, AdminOrderDetailActivity.class);
            intent.putExtra(AdminOrderDetailActivity.EXTRA_ORDER_ID, order.getId());
            startActivity(intent);
        });
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
        if (tvEmpty != null) {
            tvEmpty.setVisibility(filteredOrders.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void loadOrdersFromApi() {
        apiService.getAdminOrders().enqueue(new Callback<AdminOrdersResponseDto>() {
            @Override
            public void onResponse(Call<AdminOrdersResponseDto> call, Response<AdminOrdersResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AdminOrdersResponseDto body = response.body();
                    if (body.getData() != null) {
                        allOrders.clear();
                        for (AdminOrderDto o : body.getData()) {
                            String status = o.getOrderStatus();
                            if (status != null && status.equalsIgnoreCase("draft")) {
                                continue; // skip draft orders
                            }
                            allOrders.add(o);
                        }
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

    private void setupSearchAndFilter() {
        if (etSearch == null) return;

        // Clear button handling on drawableEnd
        etSearch.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (etSearch.getCompoundDrawables()[2] != null) { // drawableEnd
                    int leftEdgeOfRightDrawable = etSearch.getRight() - etSearch.getCompoundDrawables()[2].getBounds().width() - etSearch.getPaddingEnd();
                    if (event.getRawX() >= leftEdgeOfRightDrawable) {
                        etSearch.setText("");
                        return true;
                    }
                }
            }
            return false;
        });

        searchRunnable = () -> performSearch(currentQuery);

        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                currentQuery = s.toString().trim();
                handler.removeCallbacks(searchRunnable);
                handler.postDelayed(searchRunnable, 450); // debounce ~450ms
            }
        });

        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> openDateRangePicker());
        }
    }

    private void performSearch(String query) {
        // If no query and no date filter -> reload default
        if ((query == null || query.isEmpty()) && (dateFrom == null && dateTo == null)) {
            loadOrdersFromApi();
            return;
        }

        // Build query: if not empty, search by order_id and customer_name (OR logic)
        String q = (query != null && !query.isEmpty()) ? query : null;

        if (q == null) {
            // Only date filter, no text query
            apiService.searchAdminOrders(null, null, dateFrom, dateTo).enqueue(new Callback<AdminOrdersResponseDto>() {
                @Override
                public void onResponse(Call<AdminOrdersResponseDto> call, Response<AdminOrdersResponseDto> response) {
                    handleSearchResponse(response);
                }

                @Override
                public void onFailure(Call<AdminOrdersResponseDto> call, Throwable t) {
                    allOrders.clear();
                    applyFilter();
                }
            });
        } else {
            // Search by order_id and customer_name (OR) + date filter
            final String finalQuery = q;
            java.util.Set<String> mergedOrderIds = new java.util.LinkedHashSet<>();
            final List<AdminOrderDto> mergedOrders = new ArrayList<>();
            final int[] completedRequests = {0};

            // Search by order_id
            apiService.searchAdminOrders(finalQuery, null, dateFrom, dateTo).enqueue(new Callback<AdminOrdersResponseDto>() {
                @Override
                public void onResponse(Call<AdminOrdersResponseDto> call, Response<AdminOrdersResponseDto> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        AdminOrdersResponseDto body = response.body();
                        if (body.getData() != null) {
                            for (AdminOrderDto o : body.getData()) {
                                String status = o.getOrderStatus();
                                if (status != null && !status.equalsIgnoreCase("draft")) {
                                    if (!mergedOrderIds.contains(o.getId())) {
                                        mergedOrderIds.add(o.getId());
                                        mergedOrders.add(o);
                                    }
                                }
                            }
                        }
                    }
                    completedRequests[0]++;
                    if (completedRequests[0] == 2) {
                        finalizeMergedSearch(mergedOrders);
                    }
                }

                @Override
                public void onFailure(Call<AdminOrdersResponseDto> call, Throwable t) {
                    completedRequests[0]++;
                    if (completedRequests[0] == 2) {
                        finalizeMergedSearch(mergedOrders);
                    }
                }
            });

            // Search by customer_name
            apiService.searchAdminOrders(null, finalQuery, dateFrom, dateTo).enqueue(new Callback<AdminOrdersResponseDto>() {
                @Override
                public void onResponse(Call<AdminOrdersResponseDto> call, Response<AdminOrdersResponseDto> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        AdminOrdersResponseDto body = response.body();
                        if (body.getData() != null) {
                            for (AdminOrderDto o : body.getData()) {
                                String status = o.getOrderStatus();
                                if (status != null && !status.equalsIgnoreCase("draft")) {
                                    if (!mergedOrderIds.contains(o.getId())) {
                                        mergedOrderIds.add(o.getId());
                                        mergedOrders.add(o);
                                    }
                                }
                            }
                        }
                    }
                    completedRequests[0]++;
                    if (completedRequests[0] == 2) {
                        finalizeMergedSearch(mergedOrders);
                    }
                }

                @Override
                public void onFailure(Call<AdminOrdersResponseDto> call, Throwable t) {
                    completedRequests[0]++;
                    if (completedRequests[0] == 2) {
                        finalizeMergedSearch(mergedOrders);
                    }
                }
            });
        }
    }

    private void finalizeMergedSearch(List<AdminOrderDto> mergedOrders) {
        allOrders.clear();
        // Filter results by substring match (case-insensitive)
        String queryLower = currentQuery.toLowerCase();
        for (AdminOrderDto order : mergedOrders) {
            boolean matchesOrderNumber = order.getOrderNumber() != null && 
                    order.getOrderNumber().toLowerCase().contains(queryLower);
            boolean matchesOrderId = order.getOrderId() != null && 
                    order.getOrderId().toLowerCase().contains(queryLower);
            boolean matchesCustomerName = order.getCustomerName() != null && 
                    order.getCustomerName().toLowerCase().contains(queryLower);
            if (matchesOrderNumber || matchesOrderId || matchesCustomerName) {
                allOrders.add(order);
            }
        }
        applyFilter();
    }

    private void handleSearchResponse(Response<AdminOrdersResponseDto> response) {
        if (response.isSuccessful() && response.body() != null) {
            AdminOrdersResponseDto body = response.body();
            allOrders.clear();
            if (body.getData() != null) {
                for (AdminOrderDto o : body.getData()) {
                    String status = o.getOrderStatus();
                    if (status != null && !status.equalsIgnoreCase("draft")) {
                        allOrders.add(o);
                    }
                }
            }
            applyFilter();
        } else {
            allOrders.clear();
            applyFilter();
        }
    }

    private void openDateRangePicker() {
        try {
            com.google.android.material.datepicker.MaterialDatePicker.Builder<androidx.core.util.Pair<Long, Long>> builder =
                    com.google.android.material.datepicker.MaterialDatePicker.Builder.dateRangePicker();
            builder.setTitleText("Chọn khoảng ngày");

            com.google.android.material.datepicker.MaterialDatePicker<androidx.core.util.Pair<Long, Long>> picker = builder.build();

            picker.addOnPositiveButtonClickListener(selection -> {
                if (selection != null) {
                    Long start = selection.first;
                    Long end = selection.second;
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                    if (start != null) {
                        dateFrom = sdf.format(new java.util.Date(start));
                    }
                    if (end != null) {
                        dateTo = sdf.format(new java.util.Date(end));
                    }
                    performSearch(currentQuery);
                }
            });

            picker.addOnNegativeButtonClickListener(v -> {
                // do nothing on cancel
            });

            picker.addOnDismissListener(dialog -> {
                // optional
            });

            picker.show(getSupportFragmentManager(), "admin_orders_date_range");
        } catch (Exception e) {
            // Fallback: if MaterialDatePicker not available
            android.widget.Toast.makeText(this, "Date picker not available", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}
