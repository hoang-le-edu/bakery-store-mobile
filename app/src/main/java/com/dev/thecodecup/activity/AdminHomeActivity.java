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
                            AdminProductsResponseDto res = response.body();
                            // Ưu tiên lấy trường products_count nếu có
                            int productsCount = 0;
                            try {
                                java.lang.reflect.Method m = res.getClass().getMethod("getProductsCount");
                                Object val = m.invoke(res);
                                if (val instanceof Integer) {
                                    productsCount = (Integer) val;
                                } else if (val != null) {
                                    productsCount = Integer.parseInt(val.toString());
                                }
                            } catch (Exception e) {
                                // fallback nếu không có method getProductsCount
                                productsCount = countProducts(res);
                            }
                            totalProducts = productsCount;
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

        tvOrderPendingCount.setText("Pending: " + pendingCount);
        tvOrderCompletedCount.setText("Completed: " + completedCount);
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
        // Sử dụng màu tươi hơn: Products - xanh lá tươi, Orders - đỏ tươi
        int[] summaryColors = new int[] { 0xFF00E676, 0xFFFF1744 };
        dataSet.setColors(summaryColors);

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
        List<Integer> colorList = new ArrayList<>();

        // Map trạng thái sang màu giống AdminOrdersActivity
        Map<String, Integer> statusColorMap = new HashMap<>();
        statusColorMap.put("Wait For Approval", 0xFFFF1744); // Đỏ tươi
        statusColorMap.put("In Progress", 0xFFFFD600); // Vàng tươi
        statusColorMap.put("Delivering", 0xFFFF9100); // Cam tươi
        statusColorMap.put("Completed", 0xFF00E676); // Xanh lá tươi
        statusColorMap.put("Cancelled", 0xFF90A4AE); // Xám xanh tươi
        statusColorMap.put("Unknown", 0xFFB0BEC5); // Xám nhạt tươi

        for (Map.Entry<String, Integer> entry : orderStatusCount.entrySet()) {
            String status = entry.getKey();
            if (status != null && status.equalsIgnoreCase("Draft")) continue;
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
            Integer color = statusColorMap.get(status);
            if (color == null) color = 0xFF90CAF9; // Mặc định xanh dương nhạt nếu không khớp
            colorList.add(color);
        }

        int[] colors;
        if (entries.isEmpty()) {
            entries.add(new PieEntry(0, "No Data"));
            colors = new int[]{0xFF999999};
        } else {
            colors = new int[colorList.size()];
            for (int i = 0; i < colorList.size(); i++) {
                colors[i] = colorList.get(i);
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "Order Status Distribution");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(android.graphics.Color.BLACK); // Đặt màu chữ thành đen cho value

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

    // Kết hợp nhiều bảng màu để tăng số lượng màu khác nhau
    private int[] combineColorPalettes() {
        List<Integer> palette = new ArrayList<>();
        for (int c : ColorTemplate.MATERIAL_COLORS) palette.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS) palette.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS) palette.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS) palette.add(c);
        for (int c : ColorTemplate.LIBERTY_COLORS) palette.add(c);
        int[] arr = new int[palette.size()];
        for (int i = 0; i < palette.size(); i++) arr[i] = palette.get(i);
        return arr;
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
        final List<String> xLabels = new ArrayList<>();

        for (int i = startIndex; i < sortedDates.size(); i++) {
            String date = sortedDates.get(i);
            entries.add(new BarEntry(i - startIndex, (float) (double) dailyRevenue.get(date)));
            // Chỉ hiển thị MM-dd
            String label = date;
            if (date != null && date.length() >= 10) {
                label = date.substring(5, 10); // MM-dd
            }
            xLabels.add(label);
        }

        if (entries.isEmpty()) {
            entries.add(new BarEntry(0, 0));
            xLabels.add("");
        }

        BarDataSet dataSet = new BarDataSet(entries, "Daily Revenue");
        // Sử dụng màu cam tươi cho cột doanh thu
        int[] revenueColors = new int[] { 0xFFFF9100 };
        dataSet.setColors(revenueColors);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        barChartRevenue.setData(data);
        barChartRevenue.getXAxis().setGranularity(1f);

        // Set X axis label formatter to show date string
        XAxis xAxis = barChartRevenue.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int index = (int) value;
                if (index >= 0 && index < xLabels.size()) {
                    return xLabels.get(index);
                } else {
                    return "";
                }
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);

        Description desc = new Description();
        desc.setText("Daily Revenue (Last 10 Days)");
        barChartRevenue.setDescription(desc);
        barChartRevenue.invalidate();
    }
}
