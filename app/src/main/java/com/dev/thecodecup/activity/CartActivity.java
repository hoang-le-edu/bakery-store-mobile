package com.dev.thecodecup.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.adapter.CartAdapter;
import com.dev.thecodecup.model.network.api.BakeryJavaBridge;
import com.dev.thecodecup.model.network.api.Cart;
import com.dev.thecodecup.model.network.api.CartOrderDetail;
import com.dev.thecodecup.model.network.api.CreateCartCallback;
import com.dev.thecodecup.model.network.api.RemoveProductFromCartRequest;
import com.dev.thecodecup.model.network.api.SingleCartResponse;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import retrofit2.Response;

public class CartActivity extends BaseBottomNavActivity {

    // Header
    private ImageButton btnHome;
    private ImageButton btnDeleteCart;

    // Cart Selector
    private Spinner spinnerCarts;
    private ImageButton btnCreateNewCart;

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
    private int selectedCartIndex = -1;
    private CartAdapter cartAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        setupViews();
        setupListeners();

        loadCarts();
    }

    @Override
    protected int getBottomNavMenuItemId() {
        return R.id.navigation_cart;
    }

    private void initViews() {
        btnHome = findViewById(R.id.btnHome);
        btnDeleteCart = findViewById(R.id.btnDeleteCart);
        spinnerCarts = findViewById(R.id.spinnerCarts);
        btnCreateNewCart = findViewById(R.id.btnCreateNewCart);

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
                cartOrderDetail -> {
                    // Handle click item nếu cần
                    return Unit.INSTANCE;
                }
        );
        recyclerViewCartItems.setAdapter(cartAdapter);

        // Setup swipe-to-delete
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback());
        itemTouchHelper.attachToRecyclerView(recyclerViewCartItems);
    }

    private void setupListeners() {
        if (btnHome != null) {
            btnHome.setOnClickListener(v -> finish());
        }

        if (btnDeleteCart != null) {
            btnDeleteCart.setOnClickListener(v -> deleteCurrentCart());
        }

        spinnerCarts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (selectedCartIndex != position) {
                    selectedCartIndex = position;
                    displayCartItems(); // Cập nhật lại RecyclerView và tổng tiền
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnCreateNewCart.setOnClickListener(v -> createNewCart());

        btnProceedToCheckout.setOnClickListener(v -> {
            if (carts != null &&
                    !carts.isEmpty() &&
                    selectedCartIndex >= 0 &&
                    selectedCartIndex < carts.size()) {

                Cart selectedCart = carts.get(selectedCartIndex);
                Toast.makeText(
                        CartActivity.this,
                        "Checkout is not implemented yet",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void loadCarts() {
        final ProgressDialog dialog = ProgressDialog.show(
                this,
                null,
                "Loading cart...",
                true,
                false
        );

        BakeryJavaBridge.INSTANCE.fetchCart(
                this,
                (response, error) -> {
                    dialog.dismiss();

                        if (error != null) {
                            Toast.makeText(
                                    CartActivity.this,
                                    "Connection error: " + error.getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show();
                            showEmptyCart();
                            return;
                        }
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
                            setupCartSpinner();
                            selectedCartIndex = 0;
                            displayCartItems();
                        }
                    } else {
                        int code = (response != null) ? response.code() : -1;
                        Toast.makeText(
                                CartActivity.this,
                                "Error: " + code,
                                Toast.LENGTH_SHORT
                        ).show();
                        showEmptyCart();
                    }
                }
        );
    }

    private void setupCartSpinner() {
        if (carts == null || carts.isEmpty()) {
            spinnerCarts.setVisibility(View.GONE);
            return;
        }

        spinnerCarts.setVisibility(View.VISIBLE);
        List<String> cartNames = new ArrayList<>();
        for (Cart cart : carts) {
            cartNames.add(cart.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cartNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCarts.setAdapter(adapter);
    }

    private void createNewCart() {
        final ProgressDialog dialog = ProgressDialog.show(this, null, "Đang tạo giỏ hàng mới...", true, false);
        BakeryJavaBridge.INSTANCE.createCart(this, new CreateCartCallback() {
            @Override
            public void onResult(Response<SingleCartResponse> response, Throwable error) {
                dialog.dismiss();
                if (response != null && response.isSuccessful()) {
                    Toast.makeText(CartActivity.this, "Đã tạo giỏ hàng mới thành công!", Toast.LENGTH_SHORT).show();
                    loadCarts();
                } else {
                    Toast.makeText(CartActivity.this, "Tạo giỏ hàng thất bại", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

        cartAdapter.setItems(selectedCart.getOrder_detail());

        int totalPrice = selectedCart.getTotal_price();
        String formatted = formatPrice(totalPrice) + "₫";
        txtTotalPrice.setText(formatted);
        txtBottomTotalPrice.setText(formatted);

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
        spinnerCarts.setVisibility(View.GONE);

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
                (response, error) -> {
                    dialog.dismiss();

                    if (error != null) {
                        Toast.makeText(
                                CartActivity.this,
                                "Error: " + error.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    if (response != null && response.isSuccessful()) {
                        Toast.makeText(
                                CartActivity.this,
                                "Cart deleted",
                                Toast.LENGTH_SHORT
                        ).show();
                        loadCarts();
                    } else {
                        int code = (response != null) ? response.code() : -1;
                        Toast.makeText(
                                CartActivity.this,
                                "Error: " + code,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void removeProductFromCart(String cartId, String orderDetailId) {
        final ProgressDialog dialog = ProgressDialog.show(this, null, "Đang xóa sản phẩm...", true, false);
        RemoveProductFromCartRequest request = new RemoveProductFromCartRequest(cartId, orderDetailId);

        BakeryJavaBridge.INSTANCE.removeProductFromCart(this, request, (response, error) -> {
            dialog.dismiss();
            if (response != null && response.isSuccessful()) {
                Toast.makeText(this, "Đã xóa sản phẩm khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
                loadCarts(); // Reload to reflect changes
            } else {
                Toast.makeText(this, "Xóa sản phẩm thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatPrice(int price) {
        return String.format("%,d", price).replace(",", ".");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCarts(); // Refresh cart mỗi lần quay lại màn
    }

    private class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

        SwipeToDeleteCallback() {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            CartOrderDetail item = cartAdapter.getItem(position);

            if (item != null) {
                new AlertDialog.Builder(CartActivity.this)
                        .setTitle("Confirm Deletion")
                        .setMessage("Are you sure you want to remove the product '" + item.getProduct_name() + "' from the cart?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            if (selectedCartIndex >= 0 && selectedCartIndex < carts.size()) {
                                String cartId = carts.get(selectedCartIndex).getOrder_id();
                                removeProductFromCart(cartId, item.getId());
                            }
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            cartAdapter.notifyItemChanged(position); // Revert swipe
                        })
                        .setCancelable(false)
                        .show();
            }
        }
    }
}
