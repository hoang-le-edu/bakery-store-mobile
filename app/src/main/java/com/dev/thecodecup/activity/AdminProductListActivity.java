package com.dev.thecodecup.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.TypedValue;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.adapter.AdminProductAdapter;
import com.dev.thecodecup.adapter.AdminToppingAdapter;
import com.dev.thecodecup.model.network.ApiService;
import com.dev.thecodecup.model.network.NetworkModule;
import com.dev.thecodecup.model.network.dto.AdminProductCategoryDto;
import com.dev.thecodecup.model.network.dto.AdminProductDto;
import com.dev.thecodecup.model.network.dto.AdminProductsResponseDto;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.util.TypedValue;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.adapter.AdminProductAdapter;
import com.dev.thecodecup.model.network.ApiService;
import com.dev.thecodecup.model.network.NetworkModule;
import com.dev.thecodecup.model.network.dto.AdminProductCategoryDto;
import com.dev.thecodecup.model.network.dto.AdminProductDto;
import com.dev.thecodecup.model.network.dto.AdminProductsResponseDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Admin product list screen
 * - Layout mirrors customer list but uses admin APIs (/api/admin/products/all)
 * - Supports category tabs + searchText filter (server side)
 */

public class AdminProductListActivity extends AdminBottomNavActivity {
    // Xóa rvToppings, toppingAdapter

    private EditText etSearch;
    private TextView tabAll;
    private LinearLayout tabContainer;
    private RecyclerView rvProducts;
    private FloatingActionButton fabAddProduct;

    private AdminProductAdapter adapter;
    private ApiService apiService;

    private static final String TAB_ALL = "all";
    private static final String TAB_TOPPING = "topping";
    private static final int REQUEST_ADD_PRODUCT = 100;
    private static final int REQUEST_EDIT_PRODUCT = 101;
    private String currentCategoryId = TAB_ALL;
    private List<AdminProductCategoryDto> categories = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product_list);

        setupAdminBottomNav();
        apiService = NetworkModule.INSTANCE.getApiService();

        initViews();
        setupRecycler();
        setupSearch();
        setupTabAll();
        setupTabTopping();

        // Initial load: all categories + all products
        loadProductsFromApi(null, currentCategoryId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the product list when returning to this activity
        // This ensures we see any changes made in the edit activity
        performSearch();
    }

    @Override
    protected int getAdminMenuItemId() {
        return R.id.navigation_admin_product;
    }

    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        tabAll = findViewById(R.id.tabAll);
        tabContainer = findViewById(R.id.tabContainer);
        rvProducts = findViewById(R.id.rvProducts);
        fabAddProduct = findViewById(R.id.fabAddProduct);

        // FAB click listener
        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(AdminProductListActivity.this, AdminAddProductActivity.class);
            startActivityForResult(intent, REQUEST_ADD_PRODUCT);
        });

        // Đặt padding, minHeight, gravity, textSize, bold, ripple effect cho tabAll giống các tab động
        int px16 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        tabAll.setPadding(px16, 0, px16, 0);
        tabAll.setMinHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics()));
        tabAll.setGravity(android.view.Gravity.CENTER);
        tabAll.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_lg));
        tabAll.setTypeface(tabAll.getTypeface(), android.graphics.Typeface.BOLD);
        tabAll.setClickable(true);
        TypedValue outValue = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)) {
            tabAll.setBackgroundResource(outValue.resourceId);
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMarginEnd(8);
        tabAll.setLayoutParams(params);
    }

    private void setupRecycler() {
        adapter = new AdminProductAdapter(this);
        adapter.setOnItemClickListener(product -> {
            // This is called when the update dialog completes or when item is clicked
            // Reload the product list to reflect changes
            performSearch();
        });
        adapter.setOnDeleteClickListener((product, position) -> {
            // Show confirmation dialog before deleting
            showDeleteConfirmDialog(product, position);
        });
        rvProducts.setLayoutManager(new GridLayoutManager(this, 1));
        rvProducts.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_ADD_PRODUCT || requestCode == REQUEST_EDIT_PRODUCT) && resultCode == RESULT_OK) {
            // Reload product list
            performSearch();
        }
    }

    private void showUpdateProductConfirm(AdminProductDto updatedProduct) {
        // Gọi API update sản phẩm
        String id = updatedProduct.getProductId();
        // Tìm sản phẩm gốc trong danh sách hiện tại
        AdminProductDto oldProduct = null;
        for (AdminProductCategoryDto cat : categories) {
            if (cat == null || cat.getProductList() == null) continue;
            for (AdminProductDto p : cat.getProductList()) {
                if (p != null && id != null && id.equals(p.getProductId())) {
                    oldProduct = p;
                    break;
                }
            }
            if (oldProduct != null) break;
        }

        java.util.Map<String, Object> body = new java.util.HashMap<>();
        if (oldProduct != null) {
            if (!safeEquals(updatedProduct.getProductName(), oldProduct.getProductName())) {
                body.put("name", updatedProduct.getProductName());
            }
            if (!safeEquals(updatedProduct.getProductDescription(), oldProduct.getProductDescription())) {
                body.put("description", updatedProduct.getProductDescription());
            }
            if (!safeEquals(updatedProduct.getProductPrice(), oldProduct.getProductPrice())) {
                body.put("price", updatedProduct.getProductPrice());
            }
            if (!safeEquals(updatedProduct.getProductImageUrl(), oldProduct.getProductImageUrl())) {
                body.put("product_image_url", updatedProduct.getProductImageUrl());
            }
            if (!safeEquals(updatedProduct.getAvgRating(), oldProduct.getAvgRating())) {
                body.put("avg_rating", updatedProduct.getAvgRating());
            }
            if (!safeEquals(updatedProduct.getReviewCount(), oldProduct.getReviewCount())) {
                body.put("review_count", updatedProduct.getReviewCount());
            }
            // Thêm các trường khác nếu có
        } else {
            // Nếu không tìm thấy sản phẩm gốc, gửi tất cả các trường
            body.put("name", updatedProduct.getProductName());
            body.put("description", updatedProduct.getProductDescription());
            body.put("price", updatedProduct.getProductPrice());
            body.put("product_image_url", updatedProduct.getProductImageUrl());
            body.put("avg_rating", updatedProduct.getAvgRating());
            body.put("review_count", updatedProduct.getReviewCount());
        }

        if (body.isEmpty()) {
            Toast.makeText(AdminProductListActivity.this, "No changes to update", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.updateAdminProduct(id, body).enqueue(new Callback<AdminProductDto>() {
            @Override
            public void onResponse(Call<AdminProductDto> call, Response<AdminProductDto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminProductListActivity.this, "Update successful", Toast.LENGTH_SHORT).show();
                    performSearch(); // reload list
                } else {
                    Toast.makeText(AdminProductListActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AdminProductDto> call, Throwable t) {
                Toast.makeText(AdminProductListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // So sánh an toàn cho cả null
    private boolean safeEquals(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private void setupSearch() {
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    private void setupTabAll() {
        tabAll.setOnClickListener(v -> selectTabAndReload(TAB_ALL));
    }

    private void setupTabTopping() {
        // Thêm tab topping vào tabContainer nếu chưa có
        if (tabContainer == null) return;
        boolean hasToppingTab = false;
        for (int i = 0; i < tabContainer.getChildCount(); i++) {
            View child = tabContainer.getChildAt(i);
            if (child.getTag() != null && TAB_TOPPING.equals(child.getTag())) {
                hasToppingTab = true;
                break;
            }
        }
        if (!hasToppingTab) {
            TextView tabTopping = new TextView(this);
            tabTopping.setText("Topping");
            tabTopping.setTag(TAB_TOPPING);
            // Padding giống tabAll trong ProductListActivity: 16dp left/right, 0dp top/bottom
            int px16 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
            tabTopping.setPadding(px16, 0, px16, 0);
            tabTopping.setMinHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics()));
            tabTopping.setGravity(android.view.Gravity.CENTER);
            tabTopping.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_lg));
            tabTopping.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            tabTopping.setTypeface(tabTopping.getTypeface(), android.graphics.Typeface.BOLD);
            tabTopping.setClickable(true);
            // Ripple effect foreground
            TypedValue outValue = new TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)) {
                tabTopping.setForeground(ContextCompat.getDrawable(this, outValue.resourceId));
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd(8);
            tabTopping.setLayoutParams(params);
            tabTopping.setOnClickListener(v -> selectTabAndReload(TAB_TOPPING));
            tabContainer.addView(tabTopping);
        }
    }

    private void performSearch() {
        String search = etSearch.getText().toString().trim();
        loadProductsFromApi(search.isEmpty() ? null : search, currentCategoryId);
    }

    private void loadProductsFromApi(@Nullable String searchText, @Nullable String categoryId) {
        String categoryFilter = categoryId != null && !"all".equals(categoryId) ? categoryId : null;

        apiService.getAdminProducts(null, searchText, categoryFilter)
                .enqueue(new Callback<AdminProductsResponseDto>() {
                    @Override
                    public void onResponse(Call<AdminProductsResponseDto> call, Response<AdminProductsResponseDto> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            categories = response.body().getData() != null
                                    ? response.body().getData()
                                    : Collections.emptyList();

                            buildTabs(categories);
                            bindProducts(response.body());
                        } else {
                                Toast.makeText(AdminProductListActivity.this,
                                    "Failed to load products",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AdminProductsResponseDto> call, Throwable t) {
                        Toast.makeText(AdminProductListActivity.this,
                            "Error: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void buildTabs(List<AdminProductCategoryDto> data) {
        if (tabContainer == null) return;

        // Xóa tất cả các tab trừ tabAll (index 0)
        while (tabContainer.getChildCount() > 1) {
            tabContainer.removeViewAt(1);
        }

        if (data == null || data.isEmpty()) {
            updateTabUI();
            return;
        }

        for (AdminProductCategoryDto c : data) {
            if (c == null) continue;
            TextView tabView = new TextView(this);
            tabView.setText(c.getCategoryName() != null ? c.getCategoryName() : "Category");
            tabView.setTag(c.getCategoryId());
            int px16 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
            tabView.setPadding(px16, 0, px16, 0);
            tabView.setMinHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics()));
            tabView.setGravity(android.view.Gravity.CENTER);
            tabView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_lg));
            tabView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            tabView.setTypeface(tabView.getTypeface(), android.graphics.Typeface.BOLD);
            tabView.setClickable(true);
            TypedValue outValue = new TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)) {
                tabView.setForeground(ContextCompat.getDrawable(this, outValue.resourceId));
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMarginEnd(8);
            tabView.setLayoutParams(params);
            tabView.setOnClickListener(v -> {
                currentCategoryId = c.getCategoryId();
                updateTabUI();
                loadProductsFromApi(null, c.getCategoryId());
            });
            tabContainer.addView(tabView);
        }
        setupTabTopping();
        updateTabUI();
    }

    private void selectTabAndReload(String categoryId) {
        currentCategoryId = categoryId != null ? categoryId : TAB_ALL;
        updateTabUI();
        if (TAB_TOPPING.equals(currentCategoryId)) {
            adapter.setToppingMode(true);
            loadToppingList();
        } else {
            adapter.setToppingMode(false);
            loadProductsFromApi(null, currentCategoryId);
        }
    }

    private void loadToppingList() {
        // Gọi API để lấy topping list (không filter theo category)
        apiService.getAdminProducts(null, null, null)
                .enqueue(new Callback<AdminProductsResponseDto>() {
                    @Override
                    public void onResponse(Call<AdminProductsResponseDto> call, Response<AdminProductsResponseDto> response) {
                        List<AdminProductDto> toppings = new ArrayList<>();
                        if (response.isSuccessful() && response.body() != null && response.body().getToppingData() != null) {
                            for (var toppingCat : response.body().getToppingData()) {
                                if (toppingCat.getToppingList() != null) {
                                    toppings.addAll(toppingCat.getToppingList());
                                }
                            }
                        }
                        adapter.setItems(toppings);
                    }

                    @Override
                    public void onFailure(Call<AdminProductsResponseDto> call, Throwable t) {
                        adapter.setItems(new ArrayList<>());
                        Toast.makeText(AdminProductListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateTabUI() {
        resetTab(tabAll);
        if (tabContainer != null) {
            for (int i = 0; i < tabContainer.getChildCount(); i++) {
                View child = tabContainer.getChildAt(i);
                if (child instanceof TextView && child.getId() != R.id.tabAll) {
                    resetTab((TextView) child);
                }
            }
        }

        if (TAB_ALL.equals(currentCategoryId)) {
            setTabSelected(tabAll);
        } else if (tabContainer != null) {
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

    private void resetTab(TextView tab) {
        tab.setBackgroundResource(0);
        // Set màu đen trực tiếp để không bị hệ thống đổi thành xám
        tab.setTextColor(0xFF000000); // #000000
        tab.setTypeface(tab.getTypeface(), android.graphics.Typeface.BOLD);
    }

    private void setTabSelected(TextView tab) {
        tab.setBackgroundResource(R.drawable.bg_order_tab_selected);
        tab.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        tab.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void bindProducts(AdminProductsResponseDto response) {
        adapter.setToppingMode(false);
        List<AdminProductDto> flatList = new ArrayList<>();
        if (response != null && response.getData() != null) {
            for (AdminProductCategoryDto cat : response.getData()) {
                if (cat == null || cat.getProductList() == null) continue;

                // If a category filter is active, skip others (defensive in case backend ignores filter)
                if (!TAB_ALL.equals(currentCategoryId)) {
                    if (cat.getCategoryId() == null || !cat.getCategoryId().equals(currentCategoryId)) {
                        continue;
                    }
                }

                for (AdminProductDto p : cat.getProductList()) {
                    if (p == null) continue;
                    flatList.add(p);
                }
            }
        }
        adapter.setItems(flatList);
    }

    private void showDeleteConfirmDialog(AdminProductDto product, int position) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete \"" + product.getProductName() + "\"?")
            .setPositiveButton("Delete", (dialog, which) -> {
                deleteProduct(product, position);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteProduct(AdminProductDto product, int position) {
        String productId = product.getProductId();
        if (productId == null) {
            Toast.makeText(this, "Invalid product ID", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.deleteAdminProduct(productId).enqueue(new Callback<com.dev.thecodecup.model.network.api.SuccessResponse>() {
            @Override
            public void onResponse(Call<com.dev.thecodecup.model.network.api.SuccessResponse> call, 
                                   Response<com.dev.thecodecup.model.network.api.SuccessResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(AdminProductListActivity.this, 
                        response.body().getMessage() != null ? response.body().getMessage() : "Product deleted successfully", 
                        Toast.LENGTH_SHORT).show();
                    adapter.removeItem(position);
                } else {
                    Toast.makeText(AdminProductListActivity.this, "Failed to delete product", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.dev.thecodecup.model.network.api.SuccessResponse> call, Throwable t) {
                Toast.makeText(AdminProductListActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Không cần mapToProductDto nữa, dùng trực tiếp AdminProductDto
}