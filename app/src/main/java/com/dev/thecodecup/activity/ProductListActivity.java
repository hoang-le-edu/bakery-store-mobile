package com.dev.thecodecup.activity;

import com.dev.thecodecup.model.auth.AuthManager;
import com.dev.thecodecup.auth.GoogleAuthManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Toast;

import com.dev.thecodecup.R;
import com.dev.thecodecup.adapter.ProductAdapter;
//import com.dev.thecodecup.model.network.dto.CategoryDto;
import com.dev.thecodecup.model.network.dto.CategoryWithProductsDto;
//import com.dev.thecodecup.model.network.dto.CategoryWithProductsDto;
import com.dev.thecodecup.model.network.viewmodel.ProductViewModel;
import com.google.android.material.tabs.TabLayout;
import android.widget.PopupMenu;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ProductListActivity extends BaseBottomNavActivity {

    private TabLayout tabLayout;
    private RecyclerView rvProducts;
    private ProductAdapter adapter;
    private ProductViewModel viewModel;
    private ImageButton btnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        setupBottomNav();

        // 1) View binding
        tabLayout = findViewById(R.id.tabLayout);
        rvProducts = findViewById(R.id.rvProducts);
        bottomNav = findViewById(R.id.bottomNav);

        // 2) RecyclerView + Adapter (2 cột)
        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(this);
        adapter.setOnItemClickListener(product -> {
            // Navigate to ProductDetailActivity
            Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
            intent.putExtra("PRODUCT_ID", product.getProductId());
            startActivity(intent);
        });
        rvProducts.setAdapter(adapter);

        // 3) ViewModel
        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // 4) Quan sát Products -> cập nhật adapter để lên hình
        viewModel.getProductsLiveData().observe(this, products -> {
            android.util.Log.d("ProductActivity",
                    "Nhận được " + (products != null ? products.size() : 0) + " sản phẩm.");
            if (products != null && !products.isEmpty()) {
                adapter.setItems(products);
                rvProducts.post(() -> adapter.notifyDataSetChanged());
            } else if (products != null && products.isEmpty()) {
                adapter.setItems(products); // Cần đảm bảo adapter có thể xử lý list rỗng (nên đã làm)
            }
        });

        // 5) Quan sát Categories -> đổ TabLayout và load category đầu tiên
        viewModel.getCategoriesLiveData().observe(this, categories -> {
            buildTabs(categories);
            // Chọn tab đầu tiên (tab All) để load sản phẩm ban đầu
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

            @Override
            public void onTabUnselected(@NonNull TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(@NonNull TabLayout.Tab tab) {
                // Có thể refresh lại nếu muốn
                String categoryId = (String) tab.getTag();
                viewModel.loadProducts(null, null, categoryId);
            }
        });



        // 7) Gọi load categories ban đầu
        viewModel.loadCategories();
    }


    @Override
    protected int getBottomNavMenuItemId() {
        return R.id.navigation_product;
    }


    /** Đổ danh sách Tab từ categories */
    private void buildTabs(List<CategoryWithProductsDto> categories) {
        tabLayout.removeAllTabs();

        // 1) Tab "All" đứng đầu
        TabLayout.Tab allTab = tabLayout.newTab().setText("All");
        allTab.setTag("all");  // <-- QUAN TRỌNG: dùng đúng với default trong ViewModel
        tabLayout.addTab(allTab);

        // 2) Nếu không có category nào thì thôi, chỉ có tab All
        if (categories == null || categories.isEmpty()) {
            return;
        }

        // 3) Các tab category phía sau
        for (CategoryWithProductsDto c : categories) {
            String title = c.getCategoryName() != null ? c.getCategoryName() : "Category";
            TabLayout.Tab tab = tabLayout.newTab().setText(title);
            tab.setTag(c.getCategoryId());  // tag = ID category thực tế
            tabLayout.addTab(tab);
        }
    }

}
