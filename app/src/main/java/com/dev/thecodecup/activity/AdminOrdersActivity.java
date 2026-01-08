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
    private String filterStatus = null;
    private String filterPaymentMethod = null;
    private String filterPaymentStatus = null;
    private Integer filterOrderTotal = null;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    // Sorting
    private String sortOrder = "desc"; // default: newest first

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
        apiService.getAdminOrders(filterStatus, filterPaymentMethod, filterPaymentStatus, currentQuery,
            filterOrderTotal, dateFrom, dateTo, sortOrder).enqueue(new Callback<AdminOrdersResponseDto>() {
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

        searchRunnable = () -> {
            currentQuery = etSearch.getText().toString().trim();
            loadOrdersFromApi();
        };

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
            btnFilter.setOnClickListener(v -> openFilterDialog());
        }
    }

    private void openFilterDialog() {
        android.view.LayoutInflater inflater = android.view.LayoutInflater.from(this);
        android.view.View view = inflater.inflate(R.layout.dialog_filter_orders, null, false);

        android.widget.CheckBox cbPaymentBanking = view.findViewById(R.id.cbPaymentBanking);
        android.widget.CheckBox cbPaymentCash = view.findViewById(R.id.cbPaymentCash);

        android.widget.CheckBox cbPaymentStatusPending = view.findViewById(R.id.cbPaymentStatusPending);
        android.widget.CheckBox cbPaymentStatusPaid = view.findViewById(R.id.cbPaymentStatusPaid);

        android.widget.RadioGroup rgTotal = view.findViewById(R.id.rgTotal);
        android.widget.RadioButton rbTotalAll = view.findViewById(R.id.rbTotalAll);
        android.widget.RadioButton rbTotal1 = view.findViewById(R.id.rbTotal1);
        android.widget.RadioButton rbTotal2 = view.findViewById(R.id.rbTotal2);
        android.widget.RadioButton rbTotal3 = view.findViewById(R.id.rbTotal3);
        android.widget.RadioButton rbTotal4 = view.findViewById(R.id.rbTotal4);

        android.widget.TextView tvFromDate = view.findViewById(R.id.tvFromDate);
        android.widget.TextView tvToDate = view.findViewById(R.id.tvToDate);

        android.widget.RadioGroup rgSortOrder = view.findViewById(R.id.rgSortOrder);
        android.widget.RadioButton rbSortNone = view.findViewById(R.id.rbSortNone);
        android.widget.RadioButton rbSortNewest = view.findViewById(R.id.rbSortNewest);
        android.widget.RadioButton rbSortOldest = view.findViewById(R.id.rbSortOldest);

        android.widget.Button btnReset = view.findViewById(R.id.btnReset);
        android.widget.Button btnApply = view.findViewById(R.id.btnApply);

        // Prefill current selections (status handled by tabs; no status control here)
        cbPaymentBanking.setChecked("Banking".equalsIgnoreCase(filterPaymentMethod) || filterPaymentMethod == null);
        cbPaymentCash.setChecked("Cash".equalsIgnoreCase(filterPaymentMethod) || filterPaymentMethod == null);

        cbPaymentStatusPending.setChecked("pending".equalsIgnoreCase(filterPaymentStatus) || filterPaymentStatus == null);
        cbPaymentStatusPaid.setChecked("paid".equalsIgnoreCase(filterPaymentStatus) || filterPaymentStatus == null);

        if (filterOrderTotal == null) {
            rbTotalAll.setChecked(true);
        } else {
            switch (filterOrderTotal) {
                case 1: rbTotal1.setChecked(true); break;
                case 2: rbTotal2.setChecked(true); break;
                case 3: rbTotal3.setChecked(true); break;
                case 4: rbTotal4.setChecked(true); break;
                default: rbTotalAll.setChecked(true); break;
            }
        }

        tvFromDate.setText(dateFrom == null ? "From" : dateFrom);
        tvToDate.setText(dateTo == null ? "To" : dateTo);

        if (sortOrder == null) {
            rbSortNone.setChecked(true);
        } else if ("asc".equalsIgnoreCase(sortOrder)) {
            rbSortOldest.setChecked(true);
        } else {
            rbSortNewest.setChecked(true);
        }

        tvFromDate.setOnClickListener(v -> pickDate((picked) -> {
            dateFrom = picked;
            tvFromDate.setText(picked);
        }));

        tvToDate.setOnClickListener(v -> pickDate((picked) -> {
            dateTo = picked;
            tvToDate.setText(picked);
        }));

        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setView(view)
                .create();

        btnReset.setOnClickListener(v -> {
            cbPaymentBanking.setChecked(true);
            cbPaymentCash.setChecked(true);
            cbPaymentStatusPending.setChecked(true);
            cbPaymentStatusPaid.setChecked(true);
            rgTotal.check(R.id.rbTotalAll);
            rbSortNewest.setChecked(true);
            dateFrom = null;
            dateTo = null;
            filterStatus = null;
            tvFromDate.setText("From");
            tvToDate.setText("To");
        });

        btnApply.setOnClickListener(v -> {
            // status is controlled by tabs; clear extra status filter
            filterStatus = null;

            // payment method: if both checked -> null (all)
            boolean banking = cbPaymentBanking.isChecked();
            boolean cash = cbPaymentCash.isChecked();
            if (banking && cash) {
                filterPaymentMethod = null;
            } else if (banking) {
                filterPaymentMethod = "Banking";
            } else if (cash) {
                filterPaymentMethod = "Cash";
            } else {
                filterPaymentMethod = null;
            }

            // payment status
            boolean pending = cbPaymentStatusPending.isChecked();
            boolean paid = cbPaymentStatusPaid.isChecked();
            if (pending && paid) {
                filterPaymentStatus = null;
            } else if (pending) {
                filterPaymentStatus = "pending";
            } else if (paid) {
                filterPaymentStatus = "paid";
            } else {
                filterPaymentStatus = null;
            }

            // order total
            int totalId = rgTotal.getCheckedRadioButtonId();
            if (totalId == R.id.rbTotal1) filterOrderTotal = 1;
            else if (totalId == R.id.rbTotal2) filterOrderTotal = 2;
            else if (totalId == R.id.rbTotal3) filterOrderTotal = 3;
            else if (totalId == R.id.rbTotal4) filterOrderTotal = 4;
            else filterOrderTotal = null;

            // sort order
            if (rbSortNone.isChecked()) {
                sortOrder = null;
            } else {
                sortOrder = rbSortOldest.isChecked() ? "asc" : "desc";
            }

            loadOrdersFromApi();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void performSearch(String query) {
        currentQuery = query != null ? query.trim() : "";
        loadOrdersFromApi();
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

    private interface DatePicked {
        void onPicked(String date);
    }

    private void pickDate(DatePicked callback) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        int year = cal.get(java.util.Calendar.YEAR);
        int month = cal.get(java.util.Calendar.MONTH);
        int day = cal.get(java.util.Calendar.DAY_OF_MONTH);

        android.app.DatePickerDialog dialog = new android.app.DatePickerDialog(this,
                (view, y, m, d) -> {
                    java.text.DecimalFormat df = new java.text.DecimalFormat("00");
                    String picked = y + "-" + df.format(m + 1) + "-" + df.format(d);
                    callback.onPicked(picked);
                }, year, month, day);
        dialog.show();
    }
}
