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
import com.dev.thecodecup.adapter.ProductAdapter;
import com.dev.thecodecup.model.network.ApiService;
import com.dev.thecodecup.model.network.NetworkModule;
import com.dev.thecodecup.model.network.dto.AdminProductCategoryDto;
import com.dev.thecodecup.model.network.dto.AdminProductDto;
import com.dev.thecodecup.model.network.dto.AdminProductsResponseDto;
import com.dev.thecodecup.model.network.dto.ProductDto;

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

    private EditText etSearch;
    private TextView tabAll;
    private LinearLayout tabContainer;
    private RecyclerView rvProducts;

    private ProductAdapter adapter;
    private ApiService apiService;

    private String currentCategoryId = "all";
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

        // Initial load: all categories + all products
        loadProductsFromApi(null, currentCategoryId);
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

        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> onBackPressed());
        }
    }

    private void setupRecycler() {
        adapter = new ProductAdapter(this);
        adapter.setOnItemClickListener(product -> {
            Intent intent = new Intent(AdminProductListActivity.this, ProductDetailActivity.class);
            intent.putExtra("productId", product.getProductId());
            startActivity(intent);
        });
        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        rvProducts.setAdapter(adapter);
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
        tabAll.setOnClickListener(v -> selectTabAndReload("all"));
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

        // Keep the first child (tabAll), remove the rest before rebuilding
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
            tabView.setPadding(16, 8, 16, 8);
            tabView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_md));
            tabView.setTextColor(ContextCompat.getColor(this, android.R.color.black));

            tabView.setOnClickListener(v -> selectTabAndReload(c.getCategoryId()));
            tabContainer.addView(tabView);
        }

        updateTabUI();
    }

    private void selectTabAndReload(String categoryId) {
        currentCategoryId = categoryId != null ? categoryId : "all";
        updateTabUI();
        performSearch();
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

        if ("all".equals(currentCategoryId)) {
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
        tab.setBackground(null);
        tab.setTextColor(ContextCompat.getColor(this, android.R.color.black));
    }

    private void setTabSelected(TextView tab) {
        tab.setBackgroundResource(R.drawable.bg_order_tab_selected);
        tab.setTextColor(ContextCompat.getColor(this, android.R.color.white));
    }

    private void bindProducts(AdminProductsResponseDto response) {
        List<ProductDto> flatList = new ArrayList<>();

        if (response != null && response.getData() != null) {
            for (AdminProductCategoryDto cat : response.getData()) {
                if (cat == null || cat.getProductList() == null) continue;

                // If a category filter is active, skip others (defensive in case backend ignores filter)
                if (!"all".equals(currentCategoryId)) {
                    if (cat.getCategoryId() == null || !cat.getCategoryId().equals(currentCategoryId)) {
                        continue;
                    }
                }

                for (AdminProductDto p : cat.getProductList()) {
                    if (p == null) continue;
                    flatList.add(mapToProductDto(p));
                }
            }
        }

        adapter.setItems(flatList);
    }

    private ProductDto mapToProductDto(AdminProductDto p) {
        String id = p.getProductId() != null ? p.getProductId() : "";
        String name = p.getProductName() != null ? p.getProductName() : "";
        String price = p.getProductPrice() != null ? p.getProductPrice() : "0";
        String desc = p.getProductDescription();
        String image = p.getProductImage();
        return new ProductDto(id, name, price, desc, image);
    }
}
