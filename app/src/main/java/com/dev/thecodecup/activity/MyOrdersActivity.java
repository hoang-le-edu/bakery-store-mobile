package com.dev.thecodecup.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.dev.thecodecup.R;
import com.dev.thecodecup.model.network.api.BakeryJavaBridge;
import com.dev.thecodecup.model.network.api.CustomerOrdersResponse;
import com.dev.thecodecup.model.network.api.Order;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyOrdersActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ViewPagerAdapter pagerAdapter;

    // Define the tabs we want to show
    private final String[] TAB_TITLES = new String[]{"Wait For Approval", "In Progress", "Completed", "Cancelled"};

    // Data holder for each tab
    private final List<List<Order>> tabData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        btnBack = findViewById(R.id.btn_back);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        btnBack.setOnClickListener(v -> finish());

        // Initialize empty data for tabs
        for (int i = 0; i < TAB_TITLES.length; i++) {
            tabData.add(new ArrayList<>());
        }

        setupViewPager();
        loadOrders();
    }

    private void setupViewPager() {
        pagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> 
            tab.setText(TAB_TITLES[position])
        ).attach();
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

        // Clear existing data
        for (List<Order> list : tabData) {
            list.clear();
        }

        // Map API response keys to our tabs
        // API Keys: "Wait For Approval", "In Progress", "Completed", "Cancelled"
        
        // Tab 0: Wait For Approval
        if (data.containsKey("Wait For Approval")) {
            tabData.get(0).addAll(data.get("Wait For Approval"));
        }
        
        // Tab 1: In Progress
        if (data.containsKey("In Progress")) {
            tabData.get(1).addAll(data.get("In Progress"));
        }
        
        // Tab 2: Completed
        if (data.containsKey("Completed")) {
            tabData.get(2).addAll(data.get("Completed"));
        }
        
        // Tab 3: Cancelled
        if (data.containsKey("Cancelled")) {
            tabData.get(3).addAll(data.get("Cancelled"));
        }

        // Notify adapter to update fragments
        // Since FragmentStateAdapter doesn't have a direct notifyDataSetChanged for fragments already created in a simple way,
        // we can either recreate the adapter or use a mechanism to update fragments.
        // For simplicity, re-setting adapter or just notifying change if fragments handle their own data.
        // A better approach with FragmentStateAdapter is to have fragments retrieve data or push data to them.
        
        // Here I'll re-set the adapter to force refresh, though not most efficient, it works for this scale.
        // Alternatively, we can keep references to fragments, but they are managed by VP2.
        
        // Let's try to just notify. But fragments need to know data changed.
        // I'll make the adapter create fragments with current data. 
        // But since fragments are cached, we need to update them.
        // The simplest correct way is to let the fragments fetch data or update the adapter.
        
        // Actually, let's create a new adapter instance to refresh everything cleanly.
        setupViewPager();
    }

    private class ViewPagerAdapter extends FragmentStateAdapter {

        public ViewPagerAdapter(@NonNull AppCompatActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            List<Order> orders = tabData.get(position);
            // Convert List<Order> to ArrayList<Order>
            ArrayList<Order> orderArrayList = new ArrayList<>(orders);
            return OrderListFragment.newInstance(orderArrayList);
        }

        @Override
        public int getItemCount() {
            return TAB_TITLES.length;
        }
    }
}
