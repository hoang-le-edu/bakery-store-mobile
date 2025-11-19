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
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
//        btnProfile = findViewById(R.id.btnProfile);
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

            @Override
            public void onTabUnselected(@NonNull TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(@NonNull TabLayout.Tab tab) {
                // Có thể refresh lại nếu muốn
                String categoryId = (String) tab.getTag();
                viewModel.loadProducts(null, null, categoryId);
            }
        });

        // 7) Gọi load categories ban đầu
        viewModel.loadCategories();
//        btnProfile.setOnClickListener(v -> showProfileMenu(v));

        // 8) Setup bottom navigation
//        setupBottomNavigation();
    }

//    private void setupBottomNavigation() {
//        bottomNav.setSelectedItemId(R.id.navigation_home);
//
//        bottomNav.setOnItemSelectedListener(item -> {
//            int itemId = item.getItemId();
//
//            if (itemId == R.id.navigation_home) {
//                Intent intent = new Intent(ProductListActivity.this, HomeActivity.class);
//                startActivity(intent);
//                return true;
//            } else if (itemId == R.id.navigation_cart) {
//                // Navigate to Cart
//                Intent intent = new Intent(ProductListActivity.this, CartActivity.class);
//                startActivity(intent);
//                return true;
//            } else if (itemId == R.id.navigation_product) {
//                // This is product list activity, do nothing
//                return true;
//            } else if (itemId == R.id.navigation_profile) {
//                Intent intent = new Intent(ProductListActivity.this, ProfileActivity.class);
//                startActivity(intent);
//                return true;
//            }
//            return false;
//        });
//    }

    @Override
    protected int getBottomNavMenuItemId() {
        return R.id.navigation_product;
    }

//    private void showProfileMenu(View anchorView) {
//        PopupMenu popup = new PopupMenu(this, anchorView);
//        popup.getMenu().add(0, 1, 0, "Logout");
//
//        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                if (item.getItemId() == 1) { // ID của "Đăng xuất"
//                    handleLogout();
//                    return true;
//                }
//                return false;
//            }
//        });
//        popup.show();
//    }
//
//    private void handleLogout() {
//        AuthManager.INSTANCE.clearTokens();
//
//        GoogleAuthManager.getInstance(this).signOutGoogle();
//
//        Intent intent = new Intent(this, Login.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//        finish();
//    }

    /** Đổ danh sách Tab từ categories */
    private void buildTabs(List<CategoryWithProductsDto> categories) {
        tabLayout.removeAllTabs();
        if (categories == null || categories.isEmpty())
            return;

        for (CategoryWithProductsDto c : categories) {
            String title = c.getCategoryName() != null ? c.getCategoryName() : "Category";
            TabLayout.Tab tab = tabLayout.newTab().setText(title);
            // tag = category_id để khi click tab sẽ dùng id call API
            tab.setTag(c.getCategoryId());
            tabLayout.addTab(tab);
        }
    }
}
