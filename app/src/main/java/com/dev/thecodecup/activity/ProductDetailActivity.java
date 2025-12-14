package com.dev.thecodecup.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.dev.thecodecup.R;
import com.dev.thecodecup.adapter.ImageCarouselAdapter;
import com.dev.thecodecup.model.network.api.AddToCartRequest;
import com.dev.thecodecup.model.network.api.AddToCartCallback;
import com.dev.thecodecup.model.network.api.BakeryJavaBridge;
import com.dev.thecodecup.model.network.api.Cart;
import com.dev.thecodecup.model.network.api.CartProductRequest;
import com.dev.thecodecup.model.network.api.ProductDetail;
import com.dev.thecodecup.model.network.api.ProductDetailCallback;
import com.dev.thecodecup.model.network.api.ProductDetailResponse;
import com.dev.thecodecup.model.network.api.SuccessResponse;
import com.dev.thecodecup.model.network.api.Topping;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    private ViewPager2 imageCarousel;
    private ImageButton btnBack;
    private TextView tvProductName;
    private TextView tvProductPrice;
    private TextView tvTotalPrice;
    private ChipGroup chipGroupSizes;
    private ChipGroup chipGroupToppings;
    private ImageButton btnDecrease;
    private ImageButton btnIncrease;
    private TextView tvQuantity;
    private EditText etNote;
    private Button btnAddToCart;
    private ImageButton btnCart;

    private ProductDetail productDetail = null;
    private String productId = "";
    private String selectedSize = null;
    private int selectedSizePrice = 0;
    private final List<Topping> selectedToppings = new ArrayList<>();
    private int quantity = 1;
    private boolean hasSize = false;

    private final Handler carouselHandler = new Handler(Looper.getMainLooper());
    private int currentImageIndex = 0;

    // Edit mode fields
    private boolean isEditMode = false;
    private String orderId = "";
    private String orderDetailId = "";
    private List<String> currentToppingIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        productId = getIntent().getStringExtra("PRODUCT_ID");
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Product ID không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check if in edit mode
        isEditMode = getIntent().getBooleanExtra("EDIT_MODE", false);
        if (isEditMode) {
            orderId = getIntent().getStringExtra("ORDER_ID");
            orderDetailId = getIntent().getStringExtra("ORDER_DETAIL_ID");
            quantity = getIntent().getIntExtra("CURRENT_QUANTITY", 1);
            selectedSize = getIntent().getStringExtra("CURRENT_SIZE");
            currentToppingIds = getIntent().getStringArrayListExtra("CURRENT_TOPPING_IDS");
            if (currentToppingIds == null)
                currentToppingIds = new ArrayList<>();
        }

        initViews();
        setupListeners();
        loadProductDetail();
    }

    private void initViews() {
        imageCarousel = findViewById(R.id.imageCarousel);
        btnBack = findViewById(R.id.btnBack);
        btnCart = findViewById(R.id.btnCart);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        chipGroupSizes = findViewById(R.id.chipGroupSizes);
        chipGroupToppings = findViewById(R.id.chipGroupToppings);
        btnDecrease = findViewById(R.id.btnDecrease);
        btnIncrease = findViewById(R.id.btnIncrease);
        tvQuantity = findViewById(R.id.tvQuantity);
        etNote = findViewById(R.id.etNote);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);

        // Update button text if in edit mode
        if (isEditMode) {
            btnAddToCart.setText("Cập nhật");
            btnCart.setVisibility(android.view.View.GONE);
        }

        // Set initial quantity and note if in edit mode
        if (isEditMode) {
            tvQuantity.setText(String.valueOf(quantity));
            String currentNote = getIntent().getStringExtra("CURRENT_NOTE");
            if (currentNote != null && !currentNote.isEmpty()) {
                etNote.setText(currentNote);
            }
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnCart.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
            startActivity(intent);
        });

        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQuantityAndPrice();
            }
        });

        btnIncrease.setOnClickListener(v -> {
            quantity++;
            updateQuantityAndPrice();
        });

        btnAddToCart.setOnClickListener(v -> {
            if (isEditMode) {
                updateCartItem();
            } else {
                addToCart();
            }
        });
    }

    private void loadProductDetail() {
        ProgressDialog dialog = ProgressDialog.show(this, null, "Đang tải...", true, false);

        BakeryJavaBridge.INSTANCE.loadProductDetail(
                this,
                productId,
                (response, error) -> {
                    dialog.dismiss();

                    if (error != null) {
                        String errorMsg = "Exception: " +
                                error.getClass().getSimpleName() + " - " +
                                error.getMessage() + "\nProduct ID: " + productId;
                        Toast.makeText(ProductDetailActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        Log.e("ProductDetail", "Exception loading product", error);
                        return;
                    }

                    if (response == null) {
                        Toast.makeText(ProductDetailActivity.this,
                                "Response null", Toast.LENGTH_LONG).show();
                        return;
                    }

                    try {
                        if (response.isSuccessful()
                                && response.body() != null
                                && Boolean.TRUE.equals(response.body().getSuccess())) {

                            productDetail = response.body().getData();
                            if (productDetail != null) {
                                displayProductDetail();
                            } else {
                                Toast.makeText(ProductDetailActivity.this,
                                        "Dữ liệu sản phẩm null", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            String apiMsg = "API Error: " +
                                    response.code() + " - " + response.message() +
                                    "\nProduct ID: " + productId;

                            Toast.makeText(ProductDetailActivity.this,
                                    apiMsg, Toast.LENGTH_LONG).show();

                            Log.e("ProductDetail", "API failed: " + apiMsg);

                            if (response.errorBody() != null) {
                                String body = response.errorBody().string();
                                Log.e("ProductDetail", "Response body: " + body);
                            }
                        }
                    } catch (IOException e) {
                        Log.e("ProductDetail", "Error reading errorBody", e);
                    }
                });
    }

    private void displayProductDetail() {
        if (productDetail == null)
            return;
        ProductDetail product = productDetail;

        tvProductName.setText(product.getName());
        tvProductPrice.setText(formatPrice(product.getPrice()) + "₫");

        // Image carousel
        List<String> imageUrls = new ArrayList<>();
        if (product.getImage_url() != null) {
            imageUrls.add(product.getImage_url());
        }
        if (product.getProductDetailImages() != null) {
            for (com.dev.thecodecup.model.network.api.ProductImage img : product.getProductDetailImages()) {
                if (img.getImage_url() != null) {
                    imageUrls.add(img.getImage_url());
                }
            }
        }

        if (!imageUrls.isEmpty()) {
            ImageCarouselAdapter adapter = new ImageCarouselAdapter(imageUrls);
            imageCarousel.setAdapter(adapter);
            startAutoCarousel(imageUrls.size());
        }

        // Sizes
        chipGroupSizes.removeAllViews();
        List<com.dev.thecodecup.model.network.api.Size> sizeList = product.getSize_list();
        hasSize = sizeList != null && !sizeList.isEmpty();

        if (sizeList != null) {
            for (int index = 0; index < sizeList.size(); index++) {
                com.dev.thecodecup.model.network.api.Size size = sizeList.get(index);

                Chip chip = new Chip(this);
                String chipText = size.getName()
                        + " (+" + formatPrice(String.valueOf(size.getPrice())) + "₫)";
                chip.setText(chipText);
                chip.setCheckable(true);

                // Mặc định: nền trắng, viền đỏ, CHỮ ĐEN
                chip.setChipBackgroundColorResource(R.color.white);
                chip.setChipStrokeColorResource(R.color.red_add_button);
                chip.setChipStrokeWidth(2f);
                chip.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                chip.setTypeface(null, Typeface.NORMAL);

                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedSize = size.getName();
                        selectedSizePrice = size.getPrice();
                        updateTotalPrice();

                        // Khi được chọn -> làm nổi bật: nền đỏ, chữ trắng, bold
                        chip.setChipBackgroundColorResource(R.color.red_add_button);
                        chip.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                        chip.setTypeface(null, Typeface.BOLD);

                        // Uncheck others
                        int count = chipGroupSizes.getChildCount();
                        for (int i = 0; i < count; i++) {
                            Chip otherChip = (Chip) chipGroupSizes.getChildAt(i);
                            if (otherChip != chip) {
                                otherChip.setChecked(false);
                            }
                        }
                    } else {
                        // Khi bỏ chọn -> trở lại chữ đen, nền trắng, normal
                        if (size.getName().equals(selectedSize)) {
                            selectedSize = null;
                            selectedSizePrice = 0;
                            updateTotalPrice();
                        }
                        chip.setChipBackgroundColorResource(R.color.white);
                        chip.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                        chip.setTypeface(null, Typeface.NORMAL);
                    }
                });

                chipGroupSizes.addView(chip);

                // Auto-select based on mode
                if (isEditMode) {
                    // In edit mode, select the current size
                    if (size.getName().equals(selectedSize)) {
                        chip.setChecked(true);
                    }
                } else {
                    // In add mode, auto-select first
                    if (index == 0) {
                        chip.setChecked(true);
                    }
                }
            }
        }

        // Toppings
        chipGroupToppings.removeAllViews();
        List<Topping> toppingList = product.getTopping_list();
        if (toppingList != null) {
            for (Topping topping : toppingList) {
                Chip chip = new Chip(this);
                String chipText = topping.getName()
                        + " (+" + formatPrice(topping.getPrice()) + "₫)";
                chip.setText(chipText);
                chip.setCheckable(true);

                // Mặc định: chữ đen, nền trắng
                chip.setChipBackgroundColorResource(R.color.white);
                chip.setChipStrokeColorResource(R.color.red_add_button);
                chip.setChipStrokeWidth(2f);
                chip.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                chip.setTypeface(null, Typeface.NORMAL);

                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedToppings.add(topping);

                        // Nổi bật khi chọn
                        chip.setChipBackgroundColorResource(R.color.red_add_button);
                        chip.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                        chip.setTypeface(null, Typeface.BOLD);
                    } else {
                        selectedToppings.remove(topping);

                        // Trở lại bình thường
                        chip.setChipBackgroundColorResource(R.color.white);
                        chip.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                        chip.setTypeface(null, Typeface.NORMAL);
                    }
                    updateTotalPrice();
                });

                chipGroupToppings.addView(chip);

                // Pre-select toppings in edit mode
                if (isEditMode && currentToppingIds.contains(topping.getId())) {
                    chip.setChecked(true);
                }
            }
        }

        updateTotalPrice();
    }

    private void startAutoCarousel(final int imageCount) {
        if (imageCount <= 1)
            return;

        carouselHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                currentImageIndex = (currentImageIndex + 1) % imageCount;
                imageCarousel.setCurrentItem(currentImageIndex, true);
                carouselHandler.postDelayed(this, 3000);
            }
        }, 3000);
    }

    private void updateQuantityAndPrice() {
        tvQuantity.setText(String.valueOf(quantity));
        updateTotalPrice();
    }

    private void updateTotalPrice() {
        if (productDetail == null)
            return;
        ProductDetail product = productDetail;

        int basePrice = parsePrice(product.getPrice());
        int toppingsPrice = 0;
        for (Topping t : selectedToppings) {
            toppingsPrice += parsePrice(t.getPrice());
        }

        int totalPrice = (basePrice + selectedSizePrice + toppingsPrice) * quantity;
        tvTotalPrice.setText(formatPrice(String.valueOf(totalPrice)) + "₫");
    }

    private void addToCart() {
        if (productDetail == null)
            return;

        // Single cart system - just add product directly
        addProductToCart();
    }

    private void addProductToCart() {

        int basePrice = parsePrice(productDetail.getPrice());
        int toppingsPrice = 0;
        for (Topping t : selectedToppings) {
            toppingsPrice += parsePrice(t.getPrice());
        }

        int totalPrice = (basePrice + selectedSizePrice + toppingsPrice) * quantity;
        String note = etNote.getText().toString().trim();

        // sizeToSend logic
        String sizeToSend;
        List<com.dev.thecodecup.model.network.api.Size> sizeList = productDetail.getSize_list();
        if (sizeList == null || sizeList.isEmpty()) {
            sizeToSend = "";
        } else {
            if (selectedSize != null) {
                sizeToSend = selectedSize;
            } else {
                com.dev.thecodecup.model.network.api.Size firstSize = sizeList.get(0);
                sizeToSend = firstSize != null ? firstSize.getName() : "";
            }
        }

        List<String> toppingIds = new ArrayList<>();
        for (Topping t : selectedToppings) {
            toppingIds.add(t.getId());
        }

        CartProductRequest productRequest = new CartProductRequest(
                productId,
                totalPrice,
                sizeToSend,
                toppingIds,
                note,
                quantity);

        AddToCartRequest request = new AddToCartRequest(productRequest);

        final ProgressDialog addDialog = ProgressDialog.show(this, null, "Đang thêm vào giỏ...", true, false);
        BakeryJavaBridge.INSTANCE.addProductToCart(this, request, (response, error) -> {
            addDialog.dismiss();
            if (response != null && response.isSuccessful()) {
                Toast.makeText(this, "Đã thêm vào giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                String errorMsg = "Thêm vào giỏ hàng thất bại";
                if (error != null) {
                    errorMsg += ": " + error.getMessage();
                } else if (response != null) {
                    errorMsg += ". Code: " + response.code();
                }
                Toast.makeText(ProductDetailActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateCartItem() {
        if (productDetail == null)
            return;

        int basePrice = parsePrice(productDetail.getPrice());
        int toppingsPrice = 0;
        for (Topping t : selectedToppings) {
            toppingsPrice += parsePrice(t.getPrice());
        }

        int totalPrice = (basePrice + selectedSizePrice + toppingsPrice) * quantity;
        String note = etNote.getText().toString().trim();

        // sizeToSend logic
        String sizeToSend;
        List<com.dev.thecodecup.model.network.api.Size> sizeList = productDetail.getSize_list();
        if (sizeList == null || sizeList.isEmpty()) {
            sizeToSend = "";
        } else {
            if (selectedSize != null) {
                sizeToSend = selectedSize;
            } else {
                com.dev.thecodecup.model.network.api.Size firstSize = sizeList.get(0);
                sizeToSend = firstSize != null ? firstSize.getName() : "";
            }
        }

        List<String> toppingIds = new ArrayList<>();
        for (Topping t : selectedToppings) {
            toppingIds.add(t.getId());
        }

        com.dev.thecodecup.model.network.api.UpdateCartProductRequest updateRequest = new com.dev.thecodecup.model.network.api.UpdateCartProductRequest(
                orderId,
                orderDetailId,
                sizeToSend,
                toppingIds,
                quantity,
                note,
                totalPrice);

        final ProgressDialog updateDialog = ProgressDialog.show(this, null, "Đang cập nhật...", true, false);
        BakeryJavaBridge.INSTANCE.updateProductInCart(this, updateRequest,
                new com.dev.thecodecup.model.network.api.UpdateCartProductCallback() {
                    @Override
                    public void onResult(Response<com.dev.thecodecup.model.network.api.SuccessResponse> response,
                            Throwable error) {
                        updateDialog.dismiss();

                        if (error != null) {
                            Log.e("ProductDetail", "Update error", error);
                            Toast.makeText(ProductDetailActivity.this,
                                    "Lỗi: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (response != null && response.isSuccessful() && response.body() != null) {
                            Toast.makeText(ProductDetailActivity.this,
                                    "Đã cập nhật sản phẩm!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String errorMsg = "Cập nhật thất bại";
                            if (response != null) {
                                errorMsg += ". Code: " + response.code();
                            }
                            Toast.makeText(ProductDetailActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private int parsePrice(String price) {
        if (price == null)
            return 0;
        try {
            double d = Double.parseDouble(price);
            return (int) d;
        } catch (Exception e) {
            return 0;
        }
    }

    private String formatPrice(String price) {
        int p = parsePrice(price);
        return String.format("%,d", p).replace(",", ".");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        carouselHandler.removeCallbacksAndMessages(null);
    }
}
