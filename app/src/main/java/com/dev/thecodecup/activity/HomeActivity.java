package com.dev.thecodecup.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.adapter.ProductAdapter;
import com.dev.thecodecup.model.network.dto.ProductDto;
import com.dev.thecodecup.model.network.viewmodel.ProductViewModel;

import java.util.List;

public class HomeActivity extends BaseBottomNavActivity  {

    private RecyclerView rvBestSeller;
    private ProductAdapter bestSellerAdapter;
    private ProductViewModel productViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);   // layout home của bạn

        initViews();
        setupBestSellerList();
        setupViewModel();
        setupBottomNav();
    }

    @Override
    protected int getBottomNavMenuItemId() {
        return R.id.navigation_home;
    }

    private void initViews() {
        rvBestSeller = findViewById(R.id.rvBestSeller);
        bottomNav    = findViewById(R.id.bottomNav);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }


    /** RecyclerView ngang cho mục Bestseller */
    private void setupBestSellerList() {
        // list chạy ngang: vuốt sang trái/phải (trên emulator có thể kéo bằng chuột)
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvBestSeller.setLayoutManager(layoutManager);

        bestSellerAdapter = new ProductAdapter(this);

        bestSellerAdapter.setOnItemClickListener(product -> {
            Intent intent = new Intent(HomeActivity.this, ProductDetailActivity.class);
            intent.putExtra("PRODUCT_ID", product.getProductId());
            startActivity(intent);
        });

        rvBestSeller.setAdapter(bestSellerAdapter);
        rvBestSeller.setHasFixedSize(true);

        rvBestSeller.addOnChildAttachStateChangeListener(
                new RecyclerView.OnChildAttachStateChangeListener() {
                    @Override
                    public void onChildViewAttachedToWindow(@NonNull View view) {
                        // 1. THU NHỎ CARD: rộng khoảng 160dp để thấy nhiều item
                        ViewGroup.LayoutParams lpCard = view.getLayoutParams();
                        if (lpCard != null) {
                            lpCard.width = dpToPx(150); // bạn chỉnh 150/170 tuỳ ý
                            view.setLayoutParams(lpCard);
                        }
//
//                        // 2. ẢNH VUÔNG (cao = rộng của card trừ padding)
//                        ImageView ivImage = view.findViewById(R.id.ivImage);
//                        if (ivImage != null) {
//                            ivImage.post(() -> {
//                                ViewGroup.LayoutParams lpImg = ivImage.getLayoutParams();
//                                int w = ivImage.getWidth();
//                                if (w > 0) {
//                                    lpImg.height = (int) (w * 0.7f);
//                                    ivImage.setLayoutParams(lpImg);
//                                }
//                            });
//                        }

                        // 3. BẬT MARQUEE cho tên product (nếu bạn muốn)
                        TextView tvName = view.findViewById(R.id.tvName);
                        if (tvName != null) {
                            tvName.setSingleLine(true);
                            tvName.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                            tvName.setMarqueeRepeatLimit(-1);
                            tvName.setHorizontallyScrolling(true);
                            tvName.setSelected(true); // bắt buộc để chạy
                        }
                    }

                    @Override
                    public void onChildViewDetachedFromWindow(@NonNull View view) {
                        // không cần xử lý gì
                    }
                }
        );
    }

    /** Dùng lại ProductViewModel giống ProductListActivity */
    private void setupViewModel() {
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        // Lắng nghe dữ liệu sản phẩm
        productViewModel.getProductsLiveData().observe(this, products -> {
            if (products != null) {
                updateBestSeller(products);
            }
        });

        // Gọi API lấy tất cả sản phẩm (không quan tâm category)
        // searchText = null, limit = null, categoryId = null
        productViewModel.loadProducts(null, null, null);
    }

    /** Đổ dữ liệu vào list Bestseller (tạm thời lấy tất cả sản phẩm) */
    private void updateBestSeller(List<ProductDto> products) {
        bestSellerAdapter.setItems(products);
        rvBestSeller.post(bestSellerAdapter::notifyDataSetChanged);
    }

}
