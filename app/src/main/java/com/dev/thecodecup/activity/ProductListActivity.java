package com.dev.thecodecup.activity;

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

        tabAll = findViewById(R.id.tabAll);
        rvProducts = findViewById(R.id.rvProducts);
        bottomNav = findViewById(R.id.bottomNav);

        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(this);
        adapter.setOnItemClickListener(product -> {
            Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
            intent.putExtra("productId", product.getProductId());
            startActivity(intent);
        });
        rvProducts.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        viewModel.getProductsLiveData().observe(this, products -> {
            if (products != null && !products.isEmpty()) {
                adapter.setItems(products);
                rvProducts.post(() -> adapter.notifyDataSetChanged());
            } else if (products != null && products.isEmpty()) {
                adapter.setItems(products);
            }
        });

        viewModel.getCategoriesLiveData().observe(this, categories -> {
            buildTabs(categories);
            selectTab(tabAll, "all");
            viewModel.loadProducts(null, null, "all");
        });

        setupTabListeners();
        viewModel.loadCategories();
    }

    @Override
    protected int getBottomNavMenuItemId() {
        return R.id.navigation_product;
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
            // Padding và minHeight giống tabAll trong XML (left=16, top=0, right=16, bottom=0, minHeight=48dp)
            int px16 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
            tabView.setPadding(px16, 0, px16, 0);
            tabView.setMinHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics()));
            tabView.setGravity(android.view.Gravity.CENTER);
            tabView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_lg));
            tabView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            tabView.setTypeface(tabView.getTypeface(), android.graphics.Typeface.BOLD);
            tabView.setClickable(true);
            // Sửa lỗi setForeground cho ripple effect
            android.util.TypedValue outValue = new android.util.TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)) {
                tabView.setForeground(ContextCompat.getDrawable(this, outValue.resourceId));
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMarginEnd(8);
            tabView.setLayoutParams(params);

            tabView.setOnClickListener(v -> {
                selectTab(tabView, c.getCategoryId());
                viewModel.loadProducts(null, null, c.getCategoryId());
            });

            tabContainer.addView(tabView);
        }
    }
}