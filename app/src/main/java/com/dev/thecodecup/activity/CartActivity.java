package com.dev.thecodecup.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
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
import com.dev.thecodecup.model.network.api.CartCallback;
import com.dev.thecodecup.model.network.api.CartOrderDetail;
import com.dev.thecodecup.model.network.api.RemoveProductFromCartRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;

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
    private MaterialCheckBox checkboxSelectAll;

    // Summary
    private TextView txtTotalQuantity;
    private TextView txtTotalPrice;

    // Bottom bar
    private TextView txtBottomTotalPrice;
    private MaterialButton btnProceedToCheckout;

    // Data
    private Cart currentCart = null;
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

        // Hide cart selector UI since we only have one cart now
        if (spinnerCarts != null)
            spinnerCarts.setVisibility(View.GONE);
        if (btnCreateNewCart != null)
            btnCreateNewCart.setVisibility(View.GONE);

        recyclerViewCartItems = findViewById(R.id.recyclerViewCartItems);
        emptyCartLayout = findViewById(R.id.emptyCartLayout);
        checkboxSelectAll = findViewById(R.id.checkboxSelectAll);

        txtTotalQuantity = findViewById(R.id.txtTotalQuantity);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        txtBottomTotalPrice = findViewById(R.id.txtBottomTotalPrice);

        btnProceedToCheckout = findViewById(R.id.btnProceedToCheckout);
    }

    private void setupViews() {
        recyclerViewCartItems.setLayoutManager(new LinearLayoutManager(this));

        cartAdapter = new CartAdapter(
                this,
                this::editCartItem,
                this::onSelectionChanged);
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

        if (checkboxSelectAll != null) {
            checkboxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    cartAdapter.selectAll();
                } else {
                    cartAdapter.deselectAll();
                }
            });
        }

        if (btnProceedToCheckout != null) {
            btnProceedToCheckout.setOnClickListener(v -> {
                android.util.Log.d("CartActivity", "Checkout button clicked");
                proceedToCheckout();
            });
        } else {
            android.util.Log.e("CartActivity", "btnProceedToCheckout is NULL!");
        }
    }

    private Unit onSelectionChanged() {
        updateSummary();
        updateSelectAllCheckbox();
        return Unit.INSTANCE;
    }

    private void updateSelectAllCheckbox() {
        if (checkboxSelectAll != null) {
            checkboxSelectAll.setOnCheckedChangeListener(null);
            checkboxSelectAll.setChecked(cartAdapter.isAllSelected());
            checkboxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    cartAdapter.selectAll();
                } else {
                    cartAdapter.deselectAll();
                }
            });
        }
    }

    private Unit editCartItem(CartOrderDetail item) {
        if (currentCart == null)
            return Unit.INSTANCE;

        // Open ProductDetailActivity in edit mode
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("PRODUCT_ID", item.getProduct_id());
        intent.putExtra("EDIT_MODE", true);
        intent.putExtra("ORDER_ID", currentCart.getOrder_id());
        intent.putExtra("ORDER_DETAIL_ID", item.getId());
        intent.putExtra("CURRENT_SIZE", item.getSize());
        intent.putExtra("CURRENT_QUANTITY", item.getQuantity());
        intent.putExtra("CURRENT_NOTE", item.getNote());

        // Pass topping IDs
        ArrayList<String> toppingIds = new ArrayList<>();
        if (item.getToppings() != null) {
            for (com.dev.thecodecup.model.network.api.CartTopping topping : item.getToppings()) {
                toppingIds.add(topping.getTopping_id());
            }
        }
        intent.putStringArrayListExtra("CURRENT_TOPPING_IDS", toppingIds);

        startActivity(intent);
        return Unit.INSTANCE;
    }

    private void proceedToCheckout() {
        if (currentCart == null ||
                currentCart.getOrder_detail() == null ||
                currentCart.getOrder_detail().isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get only selected items
        List<CartOrderDetail> selectedItems = cartAdapter.getSelectedItems();
        
        if (selectedItems.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn sản phẩm để thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayList<String> selectedIds = new ArrayList<>();
        int totalPrice = 0;
        
        for (CartOrderDetail item : selectedItems) {
            selectedIds.add(item.getId());
            try {
                totalPrice += (int) Double.parseDouble(item.getTotal_price());
            } catch (NumberFormatException e) {
                android.util.Log.e("CartActivity", "Invalid price: " + item.getTotal_price());
            }
        }

        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putStringArrayListExtra("ORDER_DETAIL_IDS", selectedIds);
        intent.putExtra("ORDER_TOTAL", totalPrice);
        startActivity(intent);
    }

    private void loadCarts() {
        BakeryJavaBridge.INSTANCE.fetchCart(
                this,
                new CartCallback() {
                    @Override
                    public void onResult(Response<com.dev.thecodecup.model.network.api.CartResponse> response,
                            Throwable error) {

                        if (error != null) {
                            android.util.Log.e("CartActivity", "Error fetching cart", error);
                            Toast.makeText(
                                    CartActivity.this,
                                    "Connection error: " + error.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            showEmptyCart();
                            return;
                        }

                        if (response == null) {
                            android.util.Log.e("CartActivity", "Response is null");
                            Toast.makeText(CartActivity.this, "Response is null", Toast.LENGTH_SHORT).show();
                            showEmptyCart();
                            return;
                        }

                        android.util.Log.d("CartActivity", "Response code: " + response.code());
                        android.util.Log.d("CartActivity", "Response successful: " + response.isSuccessful());

                        if (!response.isSuccessful()) {
                            android.util.Log.e("CartActivity", "Response not successful: " + response.code());
                            Toast.makeText(CartActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                            showEmptyCart();
                            return;
                        }

                        if (response.body() == null) {
                            android.util.Log.e("CartActivity", "Response body is null");
                            Toast.makeText(CartActivity.this, "Response body is null", Toast.LENGTH_SHORT).show();
                            showEmptyCart();
                            return;
                        }

                        android.util.Log.d("CartActivity", "Response message: " + response.body().getMessage());
                        currentCart = response.body().getData();

                        if (currentCart == null) {
                            android.util.Log.d("CartActivity", "Cart data is null - cart is empty");
                            showEmptyCart();
                            return;
                        }

                        android.util.Log.d("CartActivity", "Cart loaded: " + currentCart.getOrder_id());
                        android.util.Log.d("CartActivity", "Cart items count: " +
                                (currentCart.getOrder_detail() != null ? currentCart.getOrder_detail().size() : 0));

                        if (currentCart.getOrder_detail() == null || currentCart.getOrder_detail().isEmpty()) {
                            android.util.Log.d("CartActivity", "Cart has no items");
                            showEmptyCart();
                        } else {
                            android.util.Log.d("CartActivity", "Displaying cart items");
                            displayCartItems();
                        }
                    }
                });
    }

    private void displayCartItems() {
        if (currentCart == null) {
            showEmptyCart();
            return;
        }

        if (currentCart.getOrder_detail() == null ||
                currentCart.getOrder_detail().isEmpty()) {
            showEmptyCart();
            return;
        }

        emptyCartLayout.setVisibility(View.GONE);
        recyclerViewCartItems.setVisibility(View.VISIBLE);
        if (btnDeleteCart != null) {
            btnDeleteCart.setVisibility(View.VISIBLE);
        }
        if (btnProceedToCheckout != null) {
            btnProceedToCheckout.setVisibility(View.VISIBLE);
            btnProceedToCheckout.setEnabled(true);
            btnProceedToCheckout.setClickable(true);
            android.util.Log.d("CartActivity", "Checkout button made visible and enabled");
        }

        cartAdapter.setItems(currentCart.getOrder_detail());
        updateSummary();
        updateSelectAllCheckbox();
    }

    private void updateSummary() {
        android.util.Log.d("CartActivity", "=== updateSummary called ===");
        
        if (cartAdapter == null) {
            android.util.Log.d("CartActivity", "CartAdapter is null");
            return;
        }
        
        if (currentCart == null || currentCart.getOrder_detail() == null) {
            android.util.Log.d("CartActivity", "Cart is null, resetting summary");
            txtTotalQuantity.setText("0");
            txtTotalPrice.setText("0₫");
            txtBottomTotalPrice.setText("0₫");
            if (btnProceedToCheckout != null) {
                btnProceedToCheckout.setEnabled(false);
            }
            return;
        }

        List<CartOrderDetail> selectedItems = cartAdapter.getSelectedItems();
        android.util.Log.d("CartActivity", "Selected items count: " + selectedItems.size());
        
        if (selectedItems.isEmpty()) {
            android.util.Log.d("CartActivity", "No items selected, resetting summary");
            txtTotalQuantity.setText("0");
            txtTotalPrice.setText("0₫");
            txtBottomTotalPrice.setText("0₫");
            if (btnProceedToCheckout != null) {
                btnProceedToCheckout.setEnabled(false);
            }
            return;
        }

        int totalPrice = 0;
        int totalQty = 0;
        for (CartOrderDetail item : selectedItems) {
            totalQty += item.getQuantity();
            android.util.Log.d("CartActivity", "Item: " + item.getProduct_name() + 
                ", Qty: " + item.getQuantity() + 
                ", Price: " + item.getTotal_price());
            try {
                totalPrice += (int) Double.parseDouble(item.getTotal_price());
            } catch (NumberFormatException e) {
                android.util.Log.e("CartActivity", "Invalid price: " + item.getTotal_price(), e);
            }
        }

        android.util.Log.d("CartActivity", "Total Qty: " + totalQty + ", Total Price: " + totalPrice);
        
        String formatted = formatPrice(totalPrice) + "₫";
        txtTotalQuantity.setText(String.valueOf(totalQty));
        txtTotalPrice.setText(formatted);
        txtBottomTotalPrice.setText(formatted);

        if (btnProceedToCheckout != null) {
            btnProceedToCheckout.setEnabled(true);
        }
    }

    private void showEmptyCart() {
        currentCart = null;
        emptyCartLayout.setVisibility(View.VISIBLE);
        recyclerViewCartItems.setVisibility(View.GONE);
        btnDeleteCart.setVisibility(View.GONE);
        btnProceedToCheckout.setVisibility(View.GONE);
        if (spinnerCarts != null)
            spinnerCarts.setVisibility(View.GONE);

        txtTotalQuantity.setText("0");
        txtTotalPrice.setText("0₫");
        txtBottomTotalPrice.setText("0₫");
    }

    private void deleteCurrentCart() {
        if (currentCart == null) {
            return;
        }

        BakeryJavaBridge.INSTANCE.deleteCart(
                this,
                currentCart.getOrder_id(),
                (response, error) -> {

                    if (error != null) {
                        Toast.makeText(
                                CartActivity.this,
                                "Error: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (response != null && response.isSuccessful()) {
                        Toast.makeText(
                                CartActivity.this,
                                "Cart deleted",
                                Toast.LENGTH_SHORT).show();
                        loadCarts();
                    } else {
                        int code = (response != null) ? response.code() : -1;
                        Toast.makeText(
                                CartActivity.this,
                                "Error: " + code,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeProductFromCart(String cartId, String orderDetailId) {
        RemoveProductFromCartRequest request = new RemoveProductFromCartRequest(cartId, orderDetailId);

        BakeryJavaBridge.INSTANCE.removeProductFromCart(this, request, (response, error) -> {
            if (response != null && response.isSuccessful()) {
                Toast.makeText(this, "Product removed from cart", Toast.LENGTH_SHORT).show();
                loadCarts(); // Reload to reflect changes
            } else {
                Toast.makeText(this, "Failed to remove product", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatPrice(int price) {
        return String.format("%,d", price).replace(",", ".");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCarts(); // Refresh cart when returning to screen
    }

    private class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

        SwipeToDeleteCallback() {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            CartOrderDetail item = cartAdapter.getItem(position);

            if (item != null) {
                new AlertDialog.Builder(CartActivity.this)
                        .setTitle("Delete Product")
                        .setMessage("Are you sure you want to remove '" + item.getProduct_name()
                                + "' from your cart?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            if (currentCart != null) {
                                String cartId = currentCart.getOrder_id();
                                removeProductFromCart(cartId, item.getId());
                            }
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            cartAdapter.notifyItemChanged(position); // Revert swipe
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setCancelable(false)
                        .show();
            }
        }
    }
}
