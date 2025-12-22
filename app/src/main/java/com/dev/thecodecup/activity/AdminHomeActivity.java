package com.dev.thecodecup.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.dev.thecodecup.R;
import com.dev.thecodecup.model.network.ApiService;
import com.dev.thecodecup.model.network.NetworkModule;
import com.dev.thecodecup.model.network.dto.AdminOrdersResponseDto;
import com.dev.thecodecup.model.network.dto.AdminOrderDto;
import com.dev.thecodecup.model.network.dto.AdminProductsResponseDto;
import com.dev.thecodecup.model.network.dto.AdminProductCategoryDto;

// MPAndroidChart imports
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Dashboard admin
 * - đếm tổng số sản phẩm
 * - đếm tổng số đơn hàng
 * - hiển thị biểu đồ cột Products vs Orders
 * - hiển thị pie chart trạng thái orders
 * - hiển thị chart doanh thu
 * - filter orders theo criteria
 */
public class AdminHomeActivity extends AdminBottomNavActivity {

    private TextView tvProductCount;
    private TextView tvOrderCount;
    private TextView tvOrderPendingCount;
    private TextView tvOrderCompletedCount;
    private BarChart barChartSummary;
    private PieChart pieChartOrderStatus;
    private BarChart barChartRevenue;

    // Filter UI
    private EditText etFilterOrderId;
    private EditText etFilterCustomerName;
    private EditText etFilterDateFrom;
    private EditText etFilterDateTo;
    private Button btnFilterSearch;
    private Button btnFilterClear;

    private ApiService apiService;

    private int totalProducts = 0;
    private int totalOrders = 0;
    private List<AdminOrderDto> allOrders = new ArrayList<>();
    private Map<String, Integer> orderStatusCount = new HashMap<>();

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private Calendar calendar = Calendar.getInstance();

    @Override
    protected int getAdminMenuItemId() {
        return R.id.navigation_admin_home;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        setupAdminBottomNav();

        // Stats
        tvProductCount = findViewById(R.id.tvProductCount);
        tvOrderCount = findViewById(R.id.tvOrderCount);
        tvOrderPendingCount = findViewById(R.id.tvOrderPendingCount);
        tvOrderCompletedCount = findViewById(R.id.tvOrderCompletedCount);

        // Charts
        barChartSummary = findViewById(R.id.barChartSummary);
        pieChartOrderStatus = findViewById(R.id.pieChartOrderStatus);
        barChartRevenue = findViewById(R.id.barChartRevenue);

        // Filters
        etFilterOrderId = findViewById(R.id.etFilterOrderId);
        etFilterCustomerName = findViewById(R.id.etFilterCustomerName);
        etFilterDateFrom = findViewById(R.id.etFilterDateFrom);
        etFilterDateTo = findViewById(R.id.etFilterDateTo);
        btnFilterSearch = findViewById(R.id.btnFilterSearch);
        btnFilterClear = findViewById(R.id.btnFilterClear);

        apiService = NetworkModule.INSTANCE.getApiService();

        String accessToken = getIntent().getStringExtra("accessToken");
        String tokenSource = "intent";
        if (accessToken == null || accessToken.isEmpty()) {
            accessToken = getSharedPreferences("APP_PREFS", MODE_PRIVATE)
                    .getString("ACCESS_TOKEN", null);
            tokenSource = "prefs";
        }
        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(this, "Missing access token. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Using admin token from " + tokenSource, Toast.LENGTH_SHORT).show();

        // Setup date pickers
        setupDatePickers();

        // Setup filter buttons
        setupFilterButtons();

        // Load initial data
        loadDashboardData();
    }

    private void setupDatePickers() {
        etFilterDateFrom.setOnClickListener(v -> showDatePicker(etFilterDateFrom));
        etFilterDateTo.setOnClickListener(v -> showDatePicker(etFilterDateTo));
    }

    private void showDatePicker(EditText editText) {
        DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(year, month, dayOfMonth);
                editText.setText(dateFormat.format(selectedCalendar.getTime()));
            }
        };

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                listener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void setupFilterButtons() {
        btnFilterSearch.setOnClickListener(v -> performSearch());
        btnFilterClear.setOnClickListener(v -> clearFilters());
    }

    private void performSearch() {
        String orderId = etFilterOrderId.getText().toString().trim();
        String customerName = etFilterCustomerName.getText().toString().trim();
        String dateFrom = etFilterDateFrom.getText().toString().trim();
        String dateTo = etFilterDateTo.getText().toString().trim();

        apiService.searchAdminOrders(
                orderId.isEmpty() ? null : orderId,
                customerName.isEmpty() ? null : customerName,
                dateFrom.isEmpty() ? null : dateFrom,
                dateTo.isEmpty() ? null : dateTo
        ).enqueue(new Callback<AdminOrdersResponseDto>() {
            @Override
            public void onResponse(Call<AdminOrdersResponseDto> call, Response<AdminOrdersResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allOrders = response.body().getData() != null ? response.body().getData() : new ArrayList<>();
                    tvOrderCount.setText("Filtered orders: " + allOrders.size());
                    updateOrderStats();
                    updateCharts();
                    Toast.makeText(AdminHomeActivity.this, "Found " + allOrders.size() + " orders", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AdminHomeActivity.this, "Error searching orders", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AdminOrdersResponseDto> call, Throwable t) {
                Toast.makeText(AdminHomeActivity.this, "Search failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearFilters() {
        etFilterOrderId.setText("");
        etFilterCustomerName.setText("");
        etFilterDateFrom.setText("");
        etFilterDateTo.setText("");
        loadDashboardData();
    }

    private void loadDashboardData() {
        // Load products count
        apiService.getAdminProducts(null, null, null)
                .enqueue(new Callback<AdminProductsResponseDto>() {
                    @Override
                    public void onResponse(Call<AdminProductsResponseDto> call, Response<AdminProductsResponseDto> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            totalProducts = countProducts(response.body());
                            tvProductCount.setText("Total products: " + totalProducts);
                            updateCharts();
                        } else {
                            Toast.makeText(AdminHomeActivity.this,
                                    "Error loading products",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AdminProductsResponseDto> call, Throwable t) {
                        Toast.makeText(AdminHomeActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Load all orders
        apiService.getAdminOrders().enqueue(new Callback<AdminOrdersResponseDto>() {
            @Override
            public void onResponse(Call<AdminOrdersResponseDto> call, Response<AdminOrdersResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allOrders = response.body().getData() != null ? response.body().getData() : new ArrayList<>();
                    totalOrders = allOrders.size();
                    tvOrderCount.setText("Total orders: " + totalOrders);
                    updateOrderStats();
                    updateCharts();
                } else {
                    Toast.makeText(AdminHomeActivity.this,
                            "Error loading orders",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AdminOrdersResponseDto> call, Throwable t) {
                Toast.makeText(AdminHomeActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateOrderStats() {
        orderStatusCount.clear();

        for (AdminOrderDto order : allOrders) {
            String status = order.getOrderStatus() != null ? order.getOrderStatus() : "Unknown";
            orderStatusCount.put(status, orderStatusCount.getOrDefault(status, 0) + 1);
        }

        int pendingCount = orderStatusCount.getOrDefault("Pending", 0);
        int completedCount = orderStatusCount.getOrDefault("Completed", 0);

        tvOrderPendingCount.setText("⏳ Pending: " + pendingCount);
        tvOrderCompletedCount.setText("✓ Completed: " + completedCount);
    }

    private int countProducts(AdminProductsResponseDto res) {
        int count = 0;
        if (res != null && res.getData() != null) {
            for (AdminProductCategoryDto cat : res.getData()) {
                if (cat != null && cat.getProductList() != null) {
                    count += cat.getProductList().size();
                }
            }
        }
        return count;
    }

    private void updateCharts() {
        if (barChartSummary != null) updateBarChartSummary();
        if (pieChartOrderStatus != null) updatePieChartOrderStatus();
        if (barChartRevenue != null) updateBarChartRevenue();
    }

    private void updateBarChartSummary() {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, (float) totalProducts));
        entries.add(new BarEntry(1f, (float) totalOrders));

        BarDataSet dataSet = new BarDataSet(entries, "Summary");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.4f);

        barChartSummary.setData(data);
        barChartSummary.getXAxis().setGranularity(1f);
        barChartSummary.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return value == 0f ? "Products" : (value == 1f ? "Orders" : "");
            }
        });

        Description desc = new Description();
        desc.setText("Products vs Orders");
        barChartSummary.setDescription(desc);
        barChartSummary.invalidate();
    }

    private void updatePieChartOrderStatus() {
        List<PieEntry> entries = new ArrayList<>();
        int[] colors = new int[orderStatusCount.size()];
        int colorIndex = 0;

        for (Map.Entry<String, Integer> entry : orderStatusCount.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
            colors[colorIndex++] = ColorTemplate.MATERIAL_COLORS[colorIndex % ColorTemplate.MATERIAL_COLORS.length];
        }

        if (entries.isEmpty()) {
            entries.add(new PieEntry(0, "No Data"));
            colors = new int[]{0xFF999999};
        }

        PieDataSet dataSet = new PieDataSet(entries, "Order Status Distribution");
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(12f);

        pieChartOrderStatus.setData(data);
        
        Legend legend = pieChartOrderStatus.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setWordWrapEnabled(true);

        Description desc = new Description();
        desc.setText("Orders by Status");
        pieChartOrderStatus.setDescription(desc);
        pieChartOrderStatus.invalidate();
    }

    private void updateBarChartRevenue() {
        // Calculate daily revenue
        Map<String, Double> dailyRevenue = new HashMap<>();
        List<String> sortedDates = new ArrayList<>();

        for (AdminOrderDto order : allOrders) {
            String createdAt = order.getCreatedAt();
            if (createdAt != null && createdAt.length() >= 10) {
                String dateKey = createdAt.substring(0, 10);

                try {
                    String totalStr = order.getOrderTotal();
                    if (totalStr != null && !totalStr.isEmpty()) {
                        totalStr = totalStr.replaceAll("[^0-9.]", "");
                        double amount = Double.parseDouble(totalStr);
                        dailyRevenue.put(dateKey, dailyRevenue.getOrDefault(dateKey, 0.0) + amount);
                        if (!sortedDates.contains(dateKey)) {
                            sortedDates.add(dateKey);
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        List<BarEntry> entries = new ArrayList<>();
        int startIndex = Math.max(0, sortedDates.size() - 10);
        
        for (int i = startIndex; i < sortedDates.size(); i++) {
            String date = sortedDates.get(i);
            entries.add(new BarEntry(i - startIndex, (float) (double) dailyRevenue.get(date)));
        }

        if (entries.isEmpty()) {
            entries.add(new BarEntry(0, 0));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Daily Revenue");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        barChartRevenue.setData(data);
        barChartRevenue.getXAxis().setGranularity(1f);

        Description desc = new Description();
        desc.setText("Daily Revenue (Last 10 Days)");
        barChartRevenue.setDescription(desc);
        barChartRevenue.invalidate();
    }
}
