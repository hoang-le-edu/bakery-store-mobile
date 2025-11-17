package com.dev.thecodecup.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import android.graphics.Color;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dev.thecodecup.R;
import com.dev.thecodecup.model.network.dto.ProductByIdDto;
import com.dev.thecodecup.model.network.viewmodel.ProductViewModel;

import java.text.NumberFormat;
import java.util.Locale;


public class ProductDetailActivity extends AppCompatActivity {

    private ProductViewModel productViewModel;

    private TextView tvName, tvPrice, tvDescription, tvTotalPrice;
    private ImageView ivImage;
    private LinearLayout layoutSizes, layoutToppings;
    private ImageButton btnBack;
    private Button btnAddToCart;

    private double basePrice = 0;
    private double selectedSizePrice = 0;
    private double selectedToppingPrice = 0;

    private int selectedSizeIndex = -1;
    private int selectedToppingIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initViews();

        String productId = getIntent().getStringExtra("productId");
        Log.d("API", "ProductDetailActivity → productId = " + productId);

        if (productId == null) {
            Log.e("API", "productId null");
            finish();
            return;
        }

        productViewModel = new ViewModelProvider(
                this,
                new ViewModelProvider.AndroidViewModelFactory(getApplication())
        ).get(ProductViewModel.class);

        loadProduct(productId);

        btnAddToCart.setOnClickListener(v -> {

            // CHECK RÀNG BUỘC CHỌN SIZE / TOPPING
            if (selectedSizeIndex == -1 || selectedToppingIndex == -1) {
                String msg;

                if (selectedSizeIndex == -1 && selectedToppingIndex == -1) {
                    msg = "Please select a size and a topping before adding to cart.";
                } else if (selectedSizeIndex == -1) {
                    msg = "Please select a size before adding to cart.";
                } else { // selectedToppingIndex == -1
                    msg = "Please select a topping before adding to cart.";
                }

                Toast.makeText(ProductDetailActivity.this, msg, Toast.LENGTH_SHORT).show();
                return; // dừng, không cho thêm vào giỏ hàng
            }

            // ✅ Nếu qua được đây nghĩa là đã chọn cả size và topping
            // ... phần code thêm vào giỏ hàng hiện tại của bạn
        });

    }

    private void initViews() {
        tvName = findViewById(R.id.tvName);
        tvPrice = findViewById(R.id.tvPrice);
        tvDescription = findViewById(R.id.tvDescription);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        ivImage = findViewById(R.id.ivImage);
        layoutSizes = findViewById(R.id.layoutSizes);
        layoutToppings = findViewById(R.id.layoutToppings);
        btnAddToCart = findViewById(R.id.btnAddToCart);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void loadProduct(String productId) {

        productViewModel.loadProductById(productId, product -> {
            if (product == null) {
                Log.e("API", "Không tải được dữ liệu product");
                return kotlin.Unit.INSTANCE;
            }

            bindProduct(product);
            return kotlin.Unit.INSTANCE;
        });
    }

    private void bindProduct(ProductByIdDto product) {

        tvName.setText(product.getName());
        tvDescription.setText(product.getDescription() == null ? "" : product.getDescription());

        // Giá gốc
        basePrice = safeDouble(product.getPrice());
        tvPrice.setText(formatPrice(basePrice));

        // Total price
        updateTotalPrice();

        // Ảnh chính
        Glide.with(this)
                .load(product.getImageUrl())
                .into(ivImage);

        // ===== SIZE LIST =====
        layoutSizes.removeAllViews();

        if (product.getSizeList() != null) {
            for (int i = 0; i < product.getSizeList().size(); i++) {
                var size = product.getSizeList().get(i);

                Button btn = createOptionButton(size.getName());

                int index = i;
                btn.setOnClickListener(v -> {
                    if (selectedSizeIndex == index) {
                        // đang selected, bấm lại -> unselect
                        selectedSizeIndex = -1;
                        selectedSizePrice = 0;
                        highlightSelected(layoutSizes, -1);
                    } else {
                        // chọn mới
                        selectedSizeIndex = index;
                        selectedSizePrice = safeDouble(size.getPrice());
                        highlightSelected(layoutSizes, index);
                    }
                    updateTotalPrice();
                });


                layoutSizes.addView(btn);
            }
        }

        // ===== TOPPING LIST =====
        layoutToppings.removeAllViews();

        if (product.getToppingList() != null) {
            for (int i = 0; i < product.getToppingList().size(); i++) {
                var top = product.getToppingList().get(i);

                Button btn = createOptionButton(top.getName());

                int index = i;
                btn.setOnClickListener(v -> {
                    if (selectedToppingIndex == index) {
                        // đang selected, bấm lại -> unselect
                        selectedToppingIndex = -1;
                        selectedToppingPrice = 0;
                        highlightSelected(layoutToppings, -1);
                    } else {
                        // chọn mới
                        selectedToppingIndex = index;
                        selectedToppingPrice = safeDouble(top.getPrice());
                        highlightSelected(layoutToppings, index);
                    }
                    updateTotalPrice();
                });


                layoutToppings.addView(btn);
            }
        }

    }

    // Tạo button dạng tùy chọn
    private Button createOptionButton(String text) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setBackgroundResource(R.drawable.rounded_button_pink);
        btn.setPadding(20, 20, 20, 20);

        // mỗi button chiếm 1 phần bằng nhau trên 1 hàng
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(16, 6, 16, 6);
        btn.setLayoutParams(params);

        btn.setTextColor(Color.WHITE);
        btn.setAllCaps(false);
        btn.setTextSize(16);

        return btn;
    }

    // Highlight button được chọn
    private void highlightSelected(LinearLayout layout, int selectedIndex) {
        for (int j = 0; j < layout.getChildCount(); j++) {
            View child = layout.getChildAt(j);
            if (child instanceof Button) {
                Button btn = (Button) child;

                if (j == selectedIndex && selectedIndex != -1) {
                    // đang được chọn → đỏ
                    btn.setBackgroundResource(R.drawable.rounded_button_red);
                } else {
                    // không được chọn (hoặc không có gì chọn) → hồng
                    btn.setBackgroundResource(R.drawable.rounded_button_pink);
                }

                // luôn hiển thị rõ (không mờ)
                btn.setAlpha(1f);
            }
        }
    }


    // Update tổng tiền
    private void updateTotalPrice() {
        double total = basePrice + selectedSizePrice + selectedToppingPrice;
        tvTotalPrice.setText("Total " + formatPrice(total));
    }

    private double safeDouble(Object value) {
        if (value == null) return 0;
        try { return Double.parseDouble(value.toString()); }
        catch (Exception e) { return 0; }
    }

    private String formatPrice(double value) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format((long) value) + "đ";
    }


}
