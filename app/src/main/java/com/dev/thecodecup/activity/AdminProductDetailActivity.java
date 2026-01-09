package com.dev.thecodecup.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.dev.thecodecup.R;
import com.dev.thecodecup.model.network.ApiService;
import com.dev.thecodecup.model.network.NetworkModule;
import com.dev.thecodecup.model.network.dto.AdminProductDetailDto;
import com.dev.thecodecup.model.network.dto.AdminCategoryDto;
import com.dev.thecodecup.model.network.dto.AdminToppingPriceDto;
import com.dev.thecodecup.model.network.dto.ApiResponse;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "productId";

    private ImageButton btnBack;
    private ProgressBar progressBar;
    private ImageView imgThumbnail;
    private TextView tvName;
    private TextView tvStatus;
    private TextView tvIsTopping;
    private TextView tvPrice;
    private TextView tvCost;
    private TextView tvUpMPrice;
    private TextView tvUpLPrice;
    private TextView tvPriority;
    private TextView tvCategories;
    private TextView tvToppings;
    private TextView tvDescription;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product_detail);

        apiService = NetworkModule.INSTANCE.getApiService();

        initViews();

        String productId = getIntent() != null ? getIntent().getStringExtra(EXTRA_PRODUCT_ID) : null;
        if (TextUtils.isEmpty(productId)) {
            Toast.makeText(this, "Product ID is missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnBack.setOnClickListener(v -> finish());
        loadProduct(productId);
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
        imgThumbnail = findViewById(R.id.imgThumbnail);
        tvName = findViewById(R.id.tvName);
        tvStatus = findViewById(R.id.tvStatus);
        tvIsTopping = findViewById(R.id.tvIsTopping);
        tvPrice = findViewById(R.id.tvPrice);
        tvCost = findViewById(R.id.tvCost);
        tvUpMPrice = findViewById(R.id.tvUpMPrice);
        tvUpLPrice = findViewById(R.id.tvUpLPrice);
        tvPriority = findViewById(R.id.tvPriority);
        tvCategories = findViewById(R.id.tvCategories);
        tvToppings = findViewById(R.id.tvToppings);
        tvDescription = findViewById(R.id.tvDescription);
    }

    private void loadProduct(String productId) {
        progressBar.setVisibility(View.VISIBLE);

        apiService.getAdminProductById(productId).enqueue(new Callback<ApiResponse<AdminProductDetailDto>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<AdminProductDetailDto>> call,
                                   @NonNull Response<ApiResponse<AdminProductDetailDto>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    bindProduct(response.body().getData());
                } else {
                    Toast.makeText(AdminProductDetailActivity.this, "Failed to load product", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<AdminProductDetailDto>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminProductDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void bindProduct(AdminProductDetailDto p) {
        if (p == null) return;

        tvName.setText(nonNull(p.getName()));

        String status = p.getStatus();
        tvStatus.setText(!TextUtils.isEmpty(status) ? status : "Unknown");

        boolean topping = p.getToppingFlag() != null && p.getToppingFlag() != 0;
        tvIsTopping.setText(topping ? "Topping item" : "Product item");

        tvPrice.setText(formatCurrency(p.getPrice()));
        tvCost.setText(formatCurrency(p.getCost()));
        tvUpMPrice.setText(formatCurrency(p.getUpMPrice()));
        tvUpLPrice.setText(formatCurrency(p.getUpLPrice()));

        tvPriority.setText(String.valueOf(p.getPriority() != null ? p.getPriority() : "0"));

        // Display categories from categories array
        List<String> categoryNames = new ArrayList<>();
        if (p.getCategories() != null && !p.getCategories().isEmpty()) {
            for (AdminCategoryDto category : p.getCategories()) {
                if (category != null && !TextUtils.isEmpty(category.getName())) {
                    categoryNames.add(category.getName());
                }
            }
        }
        if (categoryNames.isEmpty()) {
            tvCategories.setText("None");
        } else {
            tvCategories.setText(TextUtils.join(", ", categoryNames));
        }

        // Display toppings with names and prices
        List<AdminToppingPriceDto> toppingPrices = p.getToppingsId() != null ? p.getToppingsId() : new ArrayList<>();
        if (toppingPrices.isEmpty()) {
            tvToppings.setText("None");
        } else {
            List<String> toppingRows = new ArrayList<>();
            for (AdminToppingPriceDto tp : toppingPrices) {
                String name = tp != null && !TextUtils.isEmpty(tp.getName()) ? tp.getName() : "Unknown";
                String extra = tp != null ? tp.getExtraPrice() : "0";
                toppingRows.add(String.format(Locale.getDefault(), "• %s (+%s)",
                        name, formatCurrency(extra)));
            }
            tvToppings.setText(TextUtils.join("\n", toppingRows));
        }

        String desc = p.getDescription();
        tvDescription.setText(TextUtils.isEmpty(desc) ? "(none)" : desc);

        String imageUrl = !TextUtils.isEmpty(p.getThumbnailImage()) ? p.getThumbnailImage() : p.getImageUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(imgThumbnail);
        } else {
            imgThumbnail.setImageResource(R.drawable.placeholder_image);
        }
    }

    private String formatCurrency(String value) {
        if (TextUtils.isEmpty(value)) return "0đ";
        try {
            double number = Double.parseDouble(value);
            DecimalFormat formatter = new DecimalFormat("###,###,###");
            return formatter.format(number) + "đ";
        } catch (Exception e) {
            return value + "đ";
        }
    }

    private String nonNull(String text) {
        return text != null ? text : "";
    }
}
