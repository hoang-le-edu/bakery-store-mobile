package com.dev.thecodecup.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.adapter.CartAdapter;
import com.dev.thecodecup.model.network.api.BakeryJavaBridge;
import com.dev.thecodecup.model.network.api.Cart;
import com.dev.thecodecup.model.network.api.CartOrderDetail;
import com.dev.thecodecup.model.network.api.CartListCallback;
import com.dev.thecodecup.model.network.api.CartResponse;
import com.dev.thecodecup.model.network.api.DeleteCartCallback;
import com.dev.thecodecup.model.network.api.SuccessResponse;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import retrofit2.Response;

public class CartActivity extends BaseBottomNavActivity {

    // Header
    private ImageButton btnHome;
    private ImageButton btnDeleteCart;

    // Content
    private RecyclerView recyclerViewCartItems;
    private View emptyCartLayout;

    // Summary
    private TextView txtTotalQuantity;
    private TextView txtTotalPrice;

    // Bottom bar
    private TextView txtBottomTotalPrice;
    private MaterialButton btnProceedToCheckout;

    // Data
    private List<Cart> carts = new ArrayList<>();
    private int selectedCartIndex = 0;
    private CartAdapter cartAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        setupViews();
        setupListeners();
        // Nếu muốn dùng bottom nav, BaseBottomNavActivity sẽ tự bỏ qua vì layout này không có bottomNav
        // setupBottomNav();

        loadCarts();
    }

    @Override
    protected int getBottomNavMenuItemId() {
        // Nếu bạn dùng bottom nav và có item navigation_cart trong menu
        return R.id.navigation_cart;
    }

    private void initViews() {
        btnHome = findViewById(R.id.btnHome);
        btnDeleteCart = findViewById(R.id.btnDeleteCart);

        recyclerViewCartItems = findViewById(R.id.recyclerViewCartItems);
        emptyCartLayout = findViewById(R.id.emptyCartLayout);

        txtTotalQuantity = findViewById(R.id.txtTotalQuantity);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        txtBottomTotalPrice = findViewById(R.id.txtBottomTotalPrice);

        btnProceedToCheckout = findViewById(R.id.btnProceedToCheckout);
    }

    private void setupViews() {
        recyclerViewCartItems.setLayoutManager(new LinearLayoutManager(this));

        cartAdapter = new CartAdapter(
                this,
                new Function1<CartOrderDetail, Unit>() {
                    @Override
                    public Unit invoke(CartOrderDetail cartOrderDetail) {
                        // Handle click item nếu cần
                        return Unit.INSTANCE;
                    }
                }
        );
        recyclerViewCartItems.setAdapter(cartAdapter);
    }

    private void setupListeners() {
        // Nút home: quay lại màn trước
        if (btnHome != null) {
            btnHome.setOnClickListener(v -> finish());
        }

        // Nút xoá giỏ
        if (btnDeleteCart != null) {
            btnDeleteCart.setOnClickListener(v -> deleteCurrentCart());
        }

        // Nút Checkout
        btnProceedToCheckout.setOnClickListener(v -> {
            if (carts != null &&
                    !carts.isEmpty() &&
                    selectedCartIndex >= 0 &&
                    selectedCartIndex < carts.size()) {

                Cart selectedCart = carts.get(selectedCartIndex);
                // TODO: điều hướng sang CheckoutActivity khi sẵn sàng
                Toast.makeText(
                        CartActivity.this,
                        "Checkout chưa được implement",
                        Toast.LENGTH_SHORT
                ).show();

//                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
//                intent.putExtra("CART_ORDER_ID", selectedCart.getOrder_id());
//                intent.putExtra("CART_TOTAL", selectedCart.getTotal_price());
//                startActivity(intent);
            }
        });
    }

    private void loadCarts() {
        final ProgressDialog dialog = ProgressDialog.show(
                this,
                null,
                "Đang tải giỏ hàng...",
                true,
                false
        );

        BakeryJavaBridge.INSTANCE.fetchCart(
                this,
                new CartListCallback() {
                    @Override
                    public void onResult(Response<CartResponse> response, Throwable error) {
                        dialog.dismiss();

                        if (error != null) {
                            Toast.makeText(
                                    CartActivity.this,
                                    "Lỗi kết nối: " + error.getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();
                            showEmptyCart();
                            return;
                        }

                        if (response != null &&
                                response.isSuccessful() &&
                                response.body() != null) {

                            carts = response.body().getData();
                            if (carts == null || carts.isEmpty()) {
                                carts = new ArrayList<>();
                                showEmptyCart();
                            } else {
                                selectedCartIndex = 0;
                                displayCartItems();
                            }
                        } else {
                            int code = (response != null) ? response.code() : -1;
                            Toast.makeText(
                                    CartActivity.this,
                                    "Lỗi: " + code,
                                    Toast.LENGTH_SHORT
                            ).show();
                            showEmptyCart();
                        }
                    }
                }
        );
    }

    private void displayCartItems() {
        if (carts == null ||
                carts.isEmpty() ||
                selectedCartIndex < 0 ||
                selectedCartIndex >= carts.size()) {
            showEmptyCart();
            return;
        }

        Cart selectedCart = carts.get(selectedCartIndex);

        if (selectedCart.getOrder_detail() == null ||
                selectedCart.getOrder_detail().isEmpty()) {
            showEmptyCart();
            return;
        }

        emptyCartLayout.setVisibility(View.GONE);
        recyclerViewCartItems.setVisibility(View.VISIBLE);
        btnDeleteCart.setVisibility(View.VISIBLE);
        btnProceedToCheckout.setVisibility(View.VISIBLE);

        // Set list
        cartAdapter.setItems(selectedCart.getOrder_detail());

        // Tổng tiền
        int totalPrice = selectedCart.getTotal_price();
        String formatted = formatPrice(totalPrice) + "₫";
        txtTotalPrice.setText(formatted);
        txtBottomTotalPrice.setText(formatted);

        // Tổng số lượng (sum quantity từ CartOrderDetail)
        int totalQty = 0;
        for (CartOrderDetail d : selectedCart.getOrder_detail()) {
            totalQty += d.getQuantity();
        }
        txtTotalQuantity.setText(String.valueOf(totalQty));
    }

    private void showEmptyCart() {
        emptyCartLayout.setVisibility(View.VISIBLE);
        recyclerViewCartItems.setVisibility(View.GONE);
        btnDeleteCart.setVisibility(View.GONE);
        btnProceedToCheckout.setVisibility(View.GONE);

        txtTotalQuantity.setText("0");
        txtTotalPrice.setText("0₫");
        txtBottomTotalPrice.setText("0₫");
    }

    private void deleteCurrentCart() {
        if (carts == null ||
                carts.isEmpty() ||
                selectedCartIndex < 0 ||
                selectedCartIndex >= carts.size()) {
            return;
        }

        Cart selectedCart = carts.get(selectedCartIndex);

        final ProgressDialog dialog = ProgressDialog.show(
                this,
                null,
                "Đang xóa giỏ hàng...",
                true,
                false
        );

        BakeryJavaBridge.INSTANCE.deleteCart(
                this,
                selectedCart.getOrder_id(),
                new DeleteCartCallback() {
                    @Override
                    public void onResult(Response<SuccessResponse> response, Throwable error) {
                        dialog.dismiss();

                        if (error != null) {
                            Toast.makeText(
                                    CartActivity.this,
                                    "Lỗi: " + error.getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        if (response != null && response.isSuccessful()) {
                            Toast.makeText(
                                    CartActivity.this,
                                    "Đã xóa giỏ hàng",
                                    Toast.LENGTH_SHORT
                            ).show();
                            loadCarts();
                        } else {
                            int code = (response != null) ? response.code() : -1;
                            Toast.makeText(
                                    CartActivity.this,
                                    "Lỗi: " + code,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                }
        );
    }

    private String formatPrice(int price) {
        return String.format("%,d", price).replace(",", ".");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCarts(); // Refresh cart mỗi lần quay lại màn
    }
}
