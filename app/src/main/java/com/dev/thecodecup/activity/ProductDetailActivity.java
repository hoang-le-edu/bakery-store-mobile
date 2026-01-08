package com.dev.thecodecup.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.dev.thecodecup.R;
import com.dev.thecodecup.adapter.ImageCarouselAdapter;
import com.dev.thecodecup.model.network.api.AddToCartRequest;
import com.dev.thecodecup.model.network.api.BakeryJavaBridge;
import com.dev.thecodecup.model.network.api.CartProductRequest;
import com.dev.thecodecup.model.network.api.ProductDetail;
import com.dev.thecodecup.model.network.api.Topping;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Response;

public class ProductDetailActivity extends BaseAuthActivity {

    private ViewPager2 imageCarousel;
    private TextView tvProductName, tvProductPrice, tvQuantity, tvTotalPrice, tvReviewCount;
    private ChipGroup chipGroupSizes, chipGroupToppings;
    private ImageButton btnDecrease, btnIncrease;
    private Button btnAddToCart;
    private EditText etNote;
    private LinearLayout btnViewReviews; // Added review button
    private RatingBar productRatingBar;

    private final Handler carouselHandler = new Handler();
    private int currentImageIndex = 0;
    private int quantity = 1;

    private ProductDetail productDetail;
    private String productId;
    
    // Edit mode variables
    private boolean isEditMode = false;
    private String orderId;
    private String orderDetailId;
    private Set<String> currentToppingIds = new HashSet<>();

    private String selectedSize;
    private int selectedSizePrice = 0;
    private final Set<Topping> selectedToppings = new HashSet<>();
    private boolean hasSize = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Check for edit mode extras (support both uppercase and lowercase keys)
        boolean hasEditModeData = getIntent().hasExtra("ORDER_ID") || getIntent().hasExtra("order_id");
        boolean hasOrderDetailId = getIntent().hasExtra("ORDER_DETAIL_ID") || getIntent().hasExtra("order_detail_id");
        
        if (hasEditModeData && hasOrderDetailId) {
            isEditMode = true;
            
            // Support both uppercase (from CartActivity) and lowercase keys
            orderId = getIntent().getStringExtra("ORDER_ID");
            if (orderId == null) orderId = getIntent().getStringExtra("order_id");
            
            orderDetailId = getIntent().getStringExtra("ORDER_DETAIL_ID");
            if (orderDetailId == null) orderDetailId = getIntent().getStringExtra("order_detail_id");
            
            productId = getIntent().getStringExtra("PRODUCT_ID");
            if (productId == null) productId = getIntent().getStringExtra("product_id");
            
            quantity = getIntent().getIntExtra("CURRENT_QUANTITY", 1);
            if (quantity == 1) quantity = getIntent().getIntExtra("quantity", 1);
            
            String currentSize = getIntent().getStringExtra("CURRENT_SIZE");
            if (currentSize == null) currentSize = getIntent().getStringExtra("current_size");
            if (currentSize != null) selectedSize = currentSize;
            
            ArrayList<String> toppingIds = getIntent().getStringArrayListExtra("CURRENT_TOPPING_IDS");
            if (toppingIds == null) toppingIds = getIntent().getStringArrayListExtra("current_toppings");
            if (toppingIds != null) currentToppingIds.addAll(toppingIds);
            
            String currentNote = getIntent().getStringExtra("CURRENT_NOTE");
            if (currentNote == null) currentNote = getIntent().getStringExtra("note");
            
            initViews(); // Call initViews first to find etNote
            if (currentNote != null && etNote != null) etNote.setText(currentNote);
            
            if (btnAddToCart != null) btnAddToCart.setText("Cập nhật giỏ hàng");
            
            Log.d("ProductDetailActivity", "=== Edit Mode ===");
            Log.d("ProductDetailActivity", "Order ID: " + orderId);
            Log.d("ProductDetailActivity", "Order Detail ID: " + orderDetailId);
            Log.d("ProductDetailActivity", "Product ID: " + productId);
            Log.d("ProductDetailActivity", "Quantity: " + quantity);
            Log.d("ProductDetailActivity", "Size: " + selectedSize);
            Log.d("ProductDetailActivity", "Note: " + currentNote);
            Log.d("ProductDetailActivity", "Topping IDs: " + currentToppingIds);
        } else {
            // Try getting ID with standard camelCase key
            productId = getIntent().getStringExtra("productId");
            
            // Fallback: Try getting ID with legacy constant key
            if (productId == null) {
                productId = getIntent().getStringExtra("PRODUCT_ID");
            }
            
            initViews();
        }

        setupListeners();

        if (productId != null) {
            loadProductDetail();
        } else {
            Toast.makeText(this, "Product ID is missing", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        // Updated IDs to match camelCase in XML layout
        imageCarousel = findViewById(R.id.imageCarousel);
        tvProductName = findViewById(R.id.tvProductName);
        tvProductPrice = findViewById(R.id.tvProductPrice);
        chipGroupSizes = findViewById(R.id.chipGroupSizes);
        chipGroupToppings = findViewById(R.id.chipGroupToppings);
        tvQuantity = findViewById(R.id.tvQuantity);
        btnDecrease = findViewById(R.id.btnDecrease);
        btnIncrease = findViewById(R.id.btnIncrease);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        etNote = findViewById(R.id.etNote);
        btnViewReviews = findViewById(R.id.btnViewReviews);
        productRatingBar = findViewById(R.id.productRatingBar);
        tvReviewCount = findViewById(R.id.tvReviewCount);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        // Initial quantity set
        tvQuantity.setText(String.valueOf(quantity));
    }

    private void setupListeners() {
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

        // Set click listener for reviews button
        if (btnViewReviews != null) {
            btnViewReviews.setOnClickListener(v -> {
                if (productId != null) {
                    Intent intent = new Intent(ProductDetailActivity.this, ProductReviewsActivity.class);
                    intent.putExtra("productId", productId);
                    startActivity(intent);
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Cannot load reviews: Product ID missing", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadProductDetail() {
        final ProgressDialog dialog = ProgressDialog.show(this, null, "Đang tải...", true, false);

        BakeryJavaBridge.INSTANCE.loadProductDetail(this, productId, (response, error) -> {
            dialog.dismiss();

            if (response != null && response.isSuccessful() && response.body() != null) {
                productDetail = response.body().getData();
                displayProductDetail();
                
                // Ensure quantity and total are updated for edit mode
                if (isEditMode) {
                    tvQuantity.setText(String.valueOf(quantity));
                    updateTotalPrice();
                }
            } else {
                Toast.makeText(this, "Failed to load product details", Toast.LENGTH_SHORT).show();
                Log.e("ProductDetail", "Error: ", error);
            }
        });
    }

    private void displayProductDetail() {
        if (productDetail == null) return;
        ProductDetail product = productDetail;

        tvProductName.setText(product.getName());
        tvProductPrice.setText(formatPrice(product.getPrice()) + "₫");

        // Update review info
        Double avgRating = product.getAvg_rating();
        Integer reviewCount = product.getReview_count();

        if (avgRating != null) {
            productRatingBar.setRating(avgRating.floatValue());
        } else {
            productRatingBar.setRating(0f);
        }

        if (reviewCount != null && reviewCount > 0) {
            tvReviewCount.setText(reviewCount + " Reviews");
        } else {
            tvReviewCount.setText("No reviews yet");
        }

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
                String chipText = size.getName() + " (+" + formatPrice(String.valueOf(size.getPrice())) + "₫)";
                chip.setText(chipText);
                chip.setCheckable(true);

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

                        chip.setChipBackgroundColorResource(R.color.red_add_button);
                        chip.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                        chip.setTypeface(null, Typeface.BOLD);

                        int count = chipGroupSizes.getChildCount();
                        for (int i = 0; i < count; i++) {
                            Chip otherChip = (Chip) chipGroupSizes.getChildAt(i);
                            if (otherChip != chip) {
                                otherChip.setChecked(false);
                            }
                        }
                    } else {
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

                if (isEditMode) {
                    if (size.getName().equals(selectedSize)) {
                        chip.setChecked(true);
                    }
                } else {
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
                String chipText = topping.getName() + " (+" + formatPrice(topping.getPrice()) + "₫)";
                chip.setText(chipText);
                chip.setCheckable(true);

                chip.setChipBackgroundColorResource(R.color.white);
                chip.setChipStrokeColorResource(R.color.red_add_button);
                chip.setChipStrokeWidth(2f);
                chip.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                chip.setTypeface(null, Typeface.NORMAL);

                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedToppings.add(topping);
                        chip.setChipBackgroundColorResource(R.color.red_add_button);
                        chip.setTextColor(ContextCompat.getColor(this, android.R.color.white));
                        chip.setTypeface(null, Typeface.BOLD);
                    } else {
                        selectedToppings.remove(topping);
                        chip.setChipBackgroundColorResource(R.color.white);
                        chip.setTextColor(ContextCompat.getColor(this, android.R.color.black));
                        chip.setTypeface(null, Typeface.NORMAL);
                    }
                    updateTotalPrice();
                });

                chipGroupToppings.addView(chip);
                
                if (isEditMode && currentToppingIds.contains(topping.getId())) {
                    chip.setChecked(true);
                }
            }
        }

        updateTotalPrice();
    }

    private void startAutoCarousel(final int imageCount) {
        if (imageCount <= 1) return;
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
        if (productDetail == null) return;
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
        if (productDetail == null) return;

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
                quantity
        );

        // Workaround for backend API: send an empty order_ids list.
        AddToCartRequest request = new AddToCartRequest(productRequest, Collections.emptyList());

        final ProgressDialog addDialog = ProgressDialog.show(this, null, "Adding to cart...", true, false);
        BakeryJavaBridge.INSTANCE.addProductToCart(this, request, (response, error) -> {
            addDialog.dismiss();
            if (response != null && response.isSuccessful()) {
                Toast.makeText(this, "Product added to cart successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                String errorMsg = "Failed to add to cart";
                if (error != null) {
                    errorMsg += ": " + error.getMessage();
                } else if (response != null) {
                    try {
                        errorMsg = new JSONObject(response.errorBody().string()).getString("message");
                    } catch (Exception e) {
                        errorMsg += ". Code: " + response.code();
                    }
                }
                Toast.makeText(ProductDetailActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateCartItem() {
        if (productDetail == null) return;
        
        int basePrice = parsePrice(productDetail.getPrice());
        int toppingsPrice = 0;
        for (Topping t : selectedToppings) {
            toppingsPrice += parsePrice(t.getPrice());
        }

        int totalPrice = (basePrice + selectedSizePrice + toppingsPrice) * quantity;
        String note = etNote.getText().toString().trim();

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
                totalPrice
        );

        final ProgressDialog updateDialog = ProgressDialog.show(this, null, "Đang cập nhật...", true, false);
        BakeryJavaBridge.INSTANCE.updateProductInCart(this, updateRequest,
                new com.dev.thecodecup.model.network.api.UpdateCartProductCallback() {
                    @Override
                    public void onResult(Response<com.dev.thecodecup.model.network.api.SuccessResponse> response, Throwable error) {
                        updateDialog.dismiss();

                        if (error != null) {
                            Log.e("ProductDetail", "Update error", error);
                            Toast.makeText(ProductDetailActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (response != null && response.isSuccessful() && response.body() != null) {
                            Toast.makeText(ProductDetailActivity.this, "Đã cập nhật sản phẩm!", Toast.LENGTH_SHORT).show();
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
        if (price == null) return 0;
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
