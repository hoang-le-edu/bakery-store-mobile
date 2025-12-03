package com.dev.thecodecup.activity;

import android.os.Bundle;
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
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Dashboard admin
 * - đếm tổng số sản phẩm
 * - đếm tổng số đơn hàng
 * - hiển thị biểu đồ cột Products vs Orders
 */
public class AdminHomeActivity extends AdminBottomNavActivity {

    private TextView tvProductCount;
    private TextView tvOrderCount;
    private BarChart barChart;

    private ApiService apiService;

    private int totalProducts = 0;
    private int totalOrders = 0;

    @Override
    protected int getAdminMenuItemId() {
        return R.id.navigation_admin_home;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        setupAdminBottomNav();

        tvProductCount = findViewById(R.id.tvProductCount);
        tvOrderCount = findViewById(R.id.tvOrderCount);
        barChart = findViewById(R.id.barChartSummary);

        apiService = NetworkModule.INSTANCE.getApiService();

        String accessToken = getIntent().getStringExtra("accessToken");
        if (accessToken == null || accessToken.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy access token", Toast.LENGTH_SHORT).show();
            return;
        }

        String bearer = "Bearer " + accessToken;
        loadDashboardData(bearer);
    }

    private void loadDashboardData(String bearerToken) {
        // 1. Products
        apiService.getAdminProducts(bearerToken, null, null, null)
                .enqueue(new Callback<AdminProductsResponseDto>() {
                    @Override
                    public void onResponse(Call<AdminProductsResponseDto> call, Response<AdminProductsResponseDto> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            AdminProductsResponseDto body = response.body();
                            totalProducts = countProducts(body);
                            tvProductCount.setText("Tổng số sản phẩm: " + totalProducts);
                            updateChart();
                        } else {
                            Toast.makeText(AdminHomeActivity.this,
                                    "Lỗi lấy danh sách sản phẩm",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AdminProductsResponseDto> call, Throwable t) {
                        Toast.makeText(AdminHomeActivity.this,
                                t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

        // 2. Orders
        apiService.getAdminOrders(bearerToken)
                .enqueue(new Callback<AdminOrdersResponseDto>() {
                    @Override
                    public void onResponse(Call<AdminOrdersResponseDto> call, Response<AdminOrdersResponseDto> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            AdminOrdersResponseDto body = response.body();
                            totalOrders = countOrders(body);
                            tvOrderCount.setText("Tổng số đơn hàng: " + totalOrders);
                            updateChart();
                        } else {
                            Toast.makeText(AdminHomeActivity.this,
                                    "Lỗi lấy danh sách đơn hàng",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AdminOrdersResponseDto> call, Throwable t) {
                        Toast.makeText(AdminHomeActivity.this,
                                t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
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

    private int countOrders(AdminOrdersResponseDto res) {
        if (res == null || res.getData() == null) return 0;
        List<AdminOrderDto> list = res.getData();
        return list.size();
    }

    private void updateChart() {
        if (barChart == null) return;

        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, (float) totalProducts)); // x=0 -> Products
        entries.add(new BarEntry(1f, (float) totalOrders));   // x=1 -> Orders

        BarDataSet dataSet = new BarDataSet(entries, "Thống kê");
        BarData data = new BarData(dataSet);
        data.setBarWidth(0.4f);

        barChart.setData(data);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                if (value == 0f) return "Products";
                if (value == 1f) return "Orders";
                return "";
            }
        });

        Description description = new Description();
        description.setText("Tổng số sản phẩm & đơn hàng");
        barChart.setDescription(description);

        barChart.invalidate(); // refresh
    }
}
