package com.dev.thecodecup.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.adapter.ProductAdapter;
//import com.dev.thecodecup.model.network.dto.CategoryDto;
import com.dev.thecodecup.model.network.dto.CategoryWithProductsDto;
//import com.dev.thecodecup.model.network.dto.CategoryWithProductsDto;
import com.dev.thecodecup.model.network.viewmodel.ProductViewModel;
import com.google.android.material.tabs.TabLayout;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ProductListActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private RecyclerView rvProducts;
    private ProductAdapter adapter;
    private ProductViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        // 1) View binding
        tabLayout  = findViewById(R.id.tabLayout);
        rvProducts = findViewById(R.id.rvProducts);

        // 2) RecyclerView + Adapter (2 cột)
        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(this);
        rvProducts.setAdapter(adapter);

        // 3) ViewModel
        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // 4) Quan sát Products -> cập nhật adapter để lên hình
        viewModel.getProductsLiveData().observe(this, products -> {
            // products là List<ProductDto> lấy từ API
            adapter.setItems(products);
        });

        // 5) Quan sát Categories -> đổ TabLayout và load category đầu tiên
        viewModel.getCategoriesLiveData().observe(this, categories -> {
            buildTabs(categories);
            // Chọn tab đầu tiên (nếu có) để load sản phẩm ban đầu
            if (tabLayout.getTabCount() > 0) {
                TabLayout.Tab first = tabLayout.getTabAt(0);
                if (first != null) {
                    first.select();
                    String categoryId = (String) first.getTag();
                    // searchText = null, limit = 20 (tuỳ chỉnh), categoryId = id tab
                    viewModel.loadProducts(null, null, categoryId);
                }
            }
        });

        // 6) Sự kiện đổi tab -> load sản phẩm theo category đã chọn
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                String categoryId = (String) tab.getTag();
                viewModel.loadProducts(null, null, categoryId);
            }
            @Override public void onTabUnselected(@NonNull TabLayout.Tab tab) {}
            @Override public void onTabReselected(@NonNull TabLayout.Tab tab) {
                // Có thể refresh lại nếu muốn
                String categoryId = (String) tab.getTag();
                viewModel.loadProducts(null, null, categoryId);
            }
        });

        // 7) Gọi load categories ban đầu
        viewModel.loadCategories();
    }

    /** Đổ danh sách Tab từ categories */
    private void buildTabs(List<CategoryWithProductsDto> categories) {
        tabLayout.removeAllTabs();
        if (categories == null || categories.isEmpty()) return;

        for (CategoryWithProductsDto c : categories) {
            String title = c.getCategoryName() != null ? c.getCategoryName() : "Danh mục";
            TabLayout.Tab tab = tabLayout.newTab().setText(title);
            // tag = category_id để khi click tab sẽ dùng id call API
            tab.setTag(c.getCategoryId());
            tabLayout.addTab(tab);
        }
    }
}
