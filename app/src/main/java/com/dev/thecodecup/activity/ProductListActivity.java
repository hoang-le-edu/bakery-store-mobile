package com.dev.thecodecup.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.adapter.ProductAdapter;
import com.dev.thecodecup.model.network.dto.CategoryDto;
import com.dev.thecodecup.model.network.dto.ProductDto;
import com.dev.thecodecup.model.network.viewmodel.ProductViewModel;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;



public class ProductListActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private RecyclerView rvProducts;
    private ProductAdapter adapter;
    private List<ProductDto> productList = new ArrayList<>();
    private ProductViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        tabLayout = findViewById(R.id.tabLayout);
        rvProducts = findViewById(R.id.rvProducts);

        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(this, productList);
        rvProducts.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // Quan sát LiveData (phù hợp với Java)
        viewModel.getProductsLiveData().observe(this, products -> {
            if (products != null) {
                productList.clear();
                productList.addAll(products);
                adapter.notifyDataSetChanged();
            }
        });

        // --- Thu thập dữ liệu Flow categories (từ Kotlin) ---
        viewModel.getCategoriesLiveData().observe(this, categories -> {
            if (categories != null) {
                setupTabs(categories);
            }
        });

        // --- Gọi API load categories ---
        viewModel.loadCategories();

        // --- Gọi API load products mặc định ---
        viewModel.loadProducts(null, null, "all");
    }

    private void setupTabs(List<CategoryDto> categories) {
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText("Tất cả").setTag("all"));

        if (categories != null) {
            for (CategoryDto category : categories) {
                TabLayout.Tab tab = tabLayout.newTab();
                tab.setText(category.getCategoryName());
                tab.setTag(category.getCategoryId());
                tabLayout.addTab(tab);
            }
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                String categoryId = (String) tab.getTag();
                viewModel.loadProducts(null, null, categoryId);
            }

            @Override public void onTabUnselected(@NonNull TabLayout.Tab tab) {}
            @Override public void onTabReselected(@NonNull TabLayout.Tab tab) {}
        });
    }
}
