package com.dev.thecodecup.activity;
<<<<<<< HEAD
import com.dev.thecodecup.model.auth.AuthManager;
=======
>>>>>>> 961c39d98dbd30ad5e67631c678459b5f8ffc05a

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.util.TypedValue;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.adapter.ProductAdapter;
import com.dev.thecodecup.model.network.dto.CategoryWithProductsDto;
import com.dev.thecodecup.model.network.viewmodel.ProductViewModel;
import androidx.core.content.ContextCompat;

import java.util.List;

public class ProductListActivity extends BaseBottomNavActivity {

    private TextView tabAll;
    private RecyclerView rvProducts;
    private ProductAdapter adapter;
    private ProductViewModel viewModel;
    private String currentCategoryId = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        setupBottomNav();

<<<<<<< HEAD
        // 1) View binding
        tabLayout  = findViewById(R.id.tabLayout);
=======
        tabAll = findViewById(R.id.tabAll);
>>>>>>> 961c39d98dbd30ad5e67631c678459b5f8ffc05a
        rvProducts = findViewById(R.id.rvProducts);
        btnProfile = findViewById(R.id.btnProfile);

        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(this);
<<<<<<< HEAD
=======
        adapter.setOnItemClickListener(product -> {
            Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
            intent.putExtra("productId", product.getProductId());
            startActivity(intent);
        });
>>>>>>> 961c39d98dbd30ad5e67631c678459b5f8ffc05a
        rvProducts.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        viewModel.getProductsLiveData().observe(this, products -> {
<<<<<<< HEAD
            android.util.Log.d("ProductActivity", "Nhận được " + (products != null ? products.size() : 0) + " sản phẩm.");
=======
>>>>>>> 961c39d98dbd30ad5e67631c678459b5f8ffc05a
            if (products != null && !products.isEmpty()) {
                adapter.setItems(products);
                rvProducts.post(() -> adapter.notifyDataSetChanged());
            } else if (products != null && products.isEmpty()) {
                adapter.setItems(products);
            }
        });

        viewModel.getCategoriesLiveData().observe(this, categories -> {
            buildTabs(categories);
<<<<<<< HEAD
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
=======
            selectTab(tabAll, "all");
            viewModel.loadProducts(null, null, "all");
        });

        setupTabListeners();
>>>>>>> 961c39d98dbd30ad5e67631c678459b5f8ffc05a
        viewModel.loadCategories();
        btnProfile.setOnClickListener(v -> showProfileMenu(v));
    }

    @Override
    protected int getBottomNavMenuItemId() {
        return R.id.navigation_home;
    }

    private void showProfileMenu(View anchorView) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        popup.getMenu().add(0, 1, 0, "Logout");

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == 1) { // ID của "Đăng xuất"
                    handleLogout();
                    return true;
                }
                return false;
            }
        });
        popup.show();
    }

    private void handleLogout() {
        AuthManager.INSTANCE.clearTokens();

        Intent intent = new Intent(this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupTabListeners() {
        View.OnClickListener listener = v -> {
            if (v.getId() == R.id.tabAll) {
                selectTab(tabAll, "all");
                viewModel.loadProducts(null, null, "all");
            } else {
                Object tag = v.getTag();
                if (tag != null) {
                    String categoryId = (String) tag;
                    selectTab((TextView) v, categoryId);
                    viewModel.loadProducts(null, null, categoryId);
                }
            }
        };

        tabAll.setOnClickListener(listener);
        updateTabUI();
    }

    private void selectTab(TextView tab, String categoryId) {
        currentCategoryId = categoryId;
        updateTabUI();
    }

    private void updateTabUI() {
        resetTab(tabAll);

        LinearLayout tabContainer = findViewById(R.id.tabContainer);
        if (tabContainer != null) {
            for (int i = 0; i < tabContainer.getChildCount(); i++) {
                View child = tabContainer.getChildAt(i);
                if (child instanceof TextView && child.getId() != R.id.tabAll) {
                    resetTab((TextView) child);
                }
            }
        }

        if (currentCategoryId.equals("all")) {
            setTabSelected(tabAll);
        } else {
            if (tabContainer != null) {
                for (int i = 0; i < tabContainer.getChildCount(); i++) {
                    View child = tabContainer.getChildAt(i);
                    if (child instanceof TextView) {
                        Object tag = child.getTag();
                        if (tag != null && tag.equals(currentCategoryId)) {
                            setTabSelected((TextView) child);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void resetTab(TextView tab) {
        tab.setBackground(null);
        tab.setTextColor(ContextCompat.getColor(this, android.R.color.black));
    }

    private void setTabSelected(TextView tab) {
        tab.setBackgroundResource(R.drawable.bg_order_tab_selected);
        tab.setTextColor(ContextCompat.getColor(this, android.R.color.white));
    }

    private void buildTabs(List<CategoryWithProductsDto> categories) {
<<<<<<< HEAD
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
=======
        if (categories == null || categories.isEmpty()) {
            return;
        }

        LinearLayout tabContainer = findViewById(R.id.tabContainer);
        if (tabContainer == null) {
            return;
        }

        while (tabContainer.getChildCount() > 1) {
            tabContainer.removeViewAt(1);
        }

        for (CategoryWithProductsDto c : categories) {
            String title = c.getCategoryName() != null ? c.getCategoryName() : "Category";
            TextView tabView = new TextView(this);
            tabView.setText(title);
            tabView.setTag(c.getCategoryId());
            tabView.setPadding(16, 8, 16, 8);
            tabView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_md));
            tabView.setTextColor(ContextCompat.getColor(this, android.R.color.black));

            tabView.setOnClickListener(v -> {
                selectTab(tabView, c.getCategoryId());
                viewModel.loadProducts(null, null, c.getCategoryId());
            });

            tabContainer.addView(tabView);
        }
    }
}
>>>>>>> 961c39d98dbd30ad5e67631c678459b5f8ffc05a
