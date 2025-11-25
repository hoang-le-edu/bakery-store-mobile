package com.dev.thecodecup.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.adapter.AdminOrderAdapter;
import com.dev.thecodecup.model.network.dto.OrderDto;

import java.util.ArrayList;
import java.util.List;

public class AdminOrdersActivity extends AppCompatActivity {

    private TextView tabAll, tabPending, tabOnGoing, tabSuccess, tabCancelled;
    private RecyclerView rvOrders;

    private AdminOrderAdapter adapter;

    private final List<OrderDto> allOrders = new ArrayList<>();
    private final List<OrderDto> filteredOrders = new ArrayList<>();

    private static final String FILTER_ALL = "ALL";
    private static final String FILTER_PENDING = "PENDING";
    private static final String FILTER_ON_GOING = "ON_GOING";
    private static final String FILTER_SUCCESS = "SUCCESS";
    private static final String FILTER_CANCELLED = "CANCELLED";

    private String currentFilter = FILTER_ALL;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        initViews();
        setupTabs();
        setupRecycler();

        loadMockData();
        applyFilter();
    }

    private void initViews() {
        tabAll = findViewById(R.id.tabAll);
        tabPending = findViewById(R.id.tabPending);
        tabOnGoing = findViewById(R.id.tabOnGoing);
        tabSuccess = findViewById(R.id.tabSuccess);
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
            if (id == R.id.tabPending) {
                currentFilter = FILTER_PENDING;
            } else if (id == R.id.tabOnGoing) {
                currentFilter = FILTER_ON_GOING;
            } else if (id == R.id.tabSuccess) {
                currentFilter = FILTER_SUCCESS;
            } else if (id == R.id.tabCancelled) {
                currentFilter = FILTER_CANCELLED;
            } else {
                currentFilter = FILTER_ALL;
            }

            updateTabUI();
            applyFilter();
        };

        tabAll.setOnClickListener(listener);
        tabPending.setOnClickListener(listener);
        tabOnGoing.setOnClickListener(listener);
        tabSuccess.setOnClickListener(listener);
        tabCancelled.setOnClickListener(listener);

        updateTabUI(); // default = ALL
    }

    private void updateTabUI() {
        resetTab(tabAll);
        resetTab(tabPending);
        resetTab(tabOnGoing);
        resetTab(tabSuccess);
        resetTab(tabCancelled);

        switch (currentFilter) {
            case FILTER_PENDING:
                setTabSelected(tabPending);
                break;
            case FILTER_ON_GOING:
                setTabSelected(tabOnGoing);
                break;
            case FILTER_SUCCESS:
                setTabSelected(tabSuccess);
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
            for (OrderDto o : allOrders) {
                if (o.getOrderStatus().equals(currentFilter)) {
                    filteredOrders.add(o);
                }
            }
        }

        adapter.setItems(filteredOrders);
    }

    private void loadMockData() {
        allOrders.clear();

        allOrders.add(new OrderDto(
                "1 - A1",
                "Pham An",
                "PENDING",
                "April 17, 2024 21:31",
                "60000"
        ));

        allOrders.add(new OrderDto(
                "1 - A2",
                "Pham Binh",
                "ON_GOING",
                "April 16, 2024 20:10",
                "68000"
        ));

        allOrders.add(new OrderDto(
                "1 - A3",
                "Pham An",
                "CANCELLED",
                "April 15, 2024 19:45",
                "50000"
        ));

        allOrders.add(new OrderDto(
                "1 - A4",
                "Nguyen Nam",
                "PENDING",
                "April 12, 2024 21:00",
                "72000"
        ));

        allOrders.add(new OrderDto(
                "1 - A5",
                "Le Hoa",
                "ON_GOING",
                "April 11, 2024 18:30",
                "45000"
        ));

        allOrders.add(new OrderDto(
                "1 - A6",
                "Ha Tram",
                "SUCCESS",
                "April 11, 2024 18:30",
                "45000"
        ));
    }
}
