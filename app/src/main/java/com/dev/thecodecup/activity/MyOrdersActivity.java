package com.dev.thecodecup.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.adapter.OrderHistoryAdapter;
import com.dev.thecodecup.model.network.api.BakeryJavaBridge;
import com.dev.thecodecup.model.network.api.CreateReviewRequest;
import com.dev.thecodecup.model.network.api.CustomerOrdersResponse;
import com.dev.thecodecup.model.network.api.MyReviewData;
import com.dev.thecodecup.model.network.api.Order;
import com.dev.thecodecup.model.network.api.PaymentLinkCallback;
import com.dev.thecodecup.model.network.api.PaymentLinkResponse;
import com.dev.thecodecup.model.network.api.UpdateReviewRequest;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Response;

public class MyOrdersActivity extends AppCompatActivity implements OrderHistoryAdapter.OnOrderClickListener, OrderHistoryAdapter.OnReviewClickListener {

    private ImageButton btnBack;
    private TextView tabAll, tabWaitForApproval, tabInProgress, tabDelivered, tabCancelled;
    private RecyclerView rvOrders;

    private OrderHistoryAdapter adapter;

    private final List<Order> allOrders = new ArrayList<>();
    private final List<Order> filteredOrders = new ArrayList<>();

    private static final String FILTER_ALL = "ALL";
    private static final String FILTER_WAIT_FOR_APPROVAL = "Wait For Approval";
    private static final String FILTER_IN_PROGRESS = "In Progress";
    private static final String FILTER_DELIVERED = "Completed";
    private static final String FILTER_CANCELLED = "Cancelled";

    private String currentFilter = FILTER_ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        initViews();
        setupTabs();
        setupRecycler();
        loadOrders();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tabAll = findViewById(R.id.tabAll);
        tabWaitForApproval = findViewById(R.id.tabWaitForApproval);
        tabInProgress = findViewById(R.id.tabInProgress);
        tabDelivered = findViewById(R.id.tabDelivered);
        tabCancelled = findViewById(R.id.tabCancelled);
        rvOrders = findViewById(R.id.rvOrders);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecycler() {
        adapter = new OrderHistoryAdapter(this, this, this);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adapter);
    }

    private void setupTabs() {
        View.OnClickListener listener = v -> {
            int id = v.getId();
            if (id == R.id.tabWaitForApproval) {
                currentFilter = FILTER_WAIT_FOR_APPROVAL;
            } else if (id == R.id.tabInProgress) {
                currentFilter = FILTER_IN_PROGRESS;
            } else if (id == R.id.tabDelivered) {
                currentFilter = FILTER_DELIVERED;
            } else if (id == R.id.tabCancelled) {
                currentFilter = FILTER_CANCELLED;
            } else {
                currentFilter = FILTER_ALL;
            }

            updateTabUI();
            applyFilter();
        };

        tabAll.setOnClickListener(listener);
        tabWaitForApproval.setOnClickListener(listener);
        tabInProgress.setOnClickListener(listener);
        tabDelivered.setOnClickListener(listener);
        tabCancelled.setOnClickListener(listener);

        updateTabUI(); // default = ALL
    }

    private void updateTabUI() {
        resetTab(tabAll);
        resetTab(tabWaitForApproval);
        resetTab(tabInProgress);
        resetTab(tabDelivered);
        resetTab(tabCancelled);

        switch (currentFilter) {
            case FILTER_WAIT_FOR_APPROVAL:
                setTabSelected(tabWaitForApproval);
                break;
            case FILTER_IN_PROGRESS:
                setTabSelected(tabInProgress);
                break;
            case FILTER_DELIVERED:
                setTabSelected(tabDelivered);
                break;
            case FILTER_CANCELLED:
                setTabSelected(tabCancelled);
                break;
            case FILTER_ALL:
            default:
                setTabSelected(tabAll);
                break;
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

    private void applyFilter() {
        filteredOrders.clear();

        if (FILTER_ALL.equals(currentFilter)) {
            filteredOrders.addAll(allOrders);
        } else {
            for (Order o : allOrders) {
                String status = o.getStatus();
                if (status == null) {
                    status = o.getOrder_status();
                }
                if (status != null && status.equals(currentFilter)) {
                    filteredOrders.add(o);
                }
            }
        }

        adapter.setOrders(filteredOrders);
    }

    private void loadOrders() {
        ProgressDialog dialog = ProgressDialog.show(this, null, "Loading orders...", true, false);

        BakeryJavaBridge.INSTANCE.loadCustomerOrders(this, (response, error) -> {
            dialog.dismiss();

            if (error != null) {
                Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (response != null && response.isSuccessful() && response.body() != null) {
                CustomerOrdersResponse ordersResponse = response.body();
                processOrders(ordersResponse.getData());
            } else {
                Toast.makeText(this, "Failed to load orders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processOrders(Map<String, List<Order>> data) {
        if (data == null) return;

        allOrders.clear();

        // Collect all orders from all status groups
        if (data.containsKey("Wait For Approval")) {
            allOrders.addAll(data.get("Wait For Approval"));
        }
        
        if (data.containsKey("In Progress")) {
            allOrders.addAll(data.get("In Progress"));
        }
        
        if (data.containsKey("Completed")) {
            allOrders.addAll(data.get("Completed"));
        }
        
        if (data.containsKey("Cancelled")) {
            allOrders.addAll(data.get("Cancelled"));
        }

        applyFilter();
    }

    @Override
    public void onOrderClick(Order order) {}

    @Override
    public void onReviewClick(Order order, int position) {
        ProgressDialog dialog = ProgressDialog.show(this, "", "Checking for existing review...", true, false);

        BakeryJavaBridge.INSTANCE.getMyReview(this, order.getOrder_id(), (response, error) -> {
            dialog.dismiss();
            if (response != null && response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                // Review exists, show view/edit dialog
                showReviewDialog(order, position, response.body().getData());
            } else {
                // No review exists or error occurred (e.g. 404), show create dialog
                showReviewDialog(order, position, null);
            }
        });
    }

    private void showReviewDialog(final Order order, final int position, @Nullable final MyReviewData existingReview) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_review_input, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();

        TextView tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        RatingBar ratingBar = view.findViewById(R.id.ratingBar_dialog);
        TextInputLayout reviewTextInputLayout = view.findViewById(R.id.reviewTextInputLayout);
        EditText etReviewText = view.findViewById(R.id.etReviewText);
        TextView tvReadOnlyReview = view.findViewById(R.id.tvReadOnlyReview);
        Button btnSubmit = view.findViewById(R.id.btnSubmitReview);
        Button btnEdit = view.findViewById(R.id.btnEditReview);
        Button btnDelete = view.findViewById(R.id.btnDeleteReview);

        // Mode: VIEW existing review
        if (existingReview != null) {
            tvDialogTitle.setText("Your Review");
            btnSubmit.setVisibility(View.GONE);
            btnEdit.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
            reviewTextInputLayout.setVisibility(View.GONE);
            tvReadOnlyReview.setVisibility(View.VISIBLE);
            ratingBar.setIsIndicator(true);

            ratingBar.setRating(existingReview.getRating());
            tvReadOnlyReview.setText(existingReview.getReview_text());

            // EDIT button logic
            btnEdit.setOnClickListener(v -> {
                tvDialogTitle.setText("Edit Review");
                btnEdit.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
                btnSubmit.setText("Update");
                btnSubmit.setVisibility(View.VISIBLE);
                reviewTextInputLayout.setVisibility(View.VISIBLE);
                tvReadOnlyReview.setVisibility(View.GONE);
                etReviewText.setText(existingReview.getReview_text());
                ratingBar.setIsIndicator(false);
            });

            // DELETE button logic
            btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                    .setTitle("Delete Review")
                    .setMessage("Are you sure you want to delete this review?")
                    .setPositiveButton("Delete", (d, i) -> deleteReview(order.getOrder_id(), position, dialog))
                    .setNegativeButton("Cancel", null)
                    .show();
            });
        }
        // Mode: CREATE new review
        else {
            tvDialogTitle.setText("Write a Review");
            btnSubmit.setText("Submit");
        }

        btnSubmit.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            if (rating == 0) {
                Toast.makeText(this, "Please provide a rating (at least 1 star)", Toast.LENGTH_SHORT).show();
                return;
            }
            String reviewText = etReviewText.getText().toString().trim();

            // If we are UPDATING an existing review
            if (existingReview != null) {
                UpdateReviewRequest request = new UpdateReviewRequest((int) rating, reviewText);
                BakeryJavaBridge.INSTANCE.updateReview(this, order.getOrder_id(), request, (response, error) -> {
                    if (response != null && response.isSuccessful()) {
                        Toast.makeText(this, "Review updated!", Toast.LENGTH_SHORT).show();
                        updateOrderInList(position, (int) rating);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Failed to update review", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            // If we are CREATING a new review
            else {
                if (order.getOrder_detail() == null || order.getOrder_detail().isEmpty()) {
                    Toast.makeText(this, "Cannot submit review: Product info missing.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String productId = order.getOrder_detail().get(0).getProduct_id();
                CreateReviewRequest request = new CreateReviewRequest(order.getOrder_id(), (int) rating, reviewText);
                BakeryJavaBridge.INSTANCE.createReview(this, productId, request, (response, error) -> {
                    if (response != null && response.isSuccessful()) {
                        Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show();
                        updateOrderInList(position, (int) rating);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Failed to submit review", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        dialog.show();
    }

    private void deleteReview(String orderId, int position, DialogInterface dialog) {
        ProgressDialog progress = ProgressDialog.show(this, "", "Deleting...", true);
        BakeryJavaBridge.INSTANCE.deleteReview(this, orderId, (response, error) -> {
            progress.dismiss();
            if (response != null && response.isSuccessful()) {
                Toast.makeText(this, "Review deleted", Toast.LENGTH_SHORT).show();
                updateOrderInList(position, 0); // Reset rate to 0 to show "Review" button again
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Failed to delete review", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateOrderInList(int position, int newRating) {
        if (position >= 0 && position < filteredOrders.size()) {
            Order order = filteredOrders.get(position);
            // Re-create the object to ensure the list item is re-bound with new data
            Order updatedOrder = new Order(order.getOrder_id(), order.getOrder_number(), order.getDate_created(), order.getHost_id(), order.getPayment_method(), order.getPayment_status(), order.getReceiver_name(), order.getReceiver_address(), order.getReceiver_phone(), order.getOrder_status(), order.getStatus(), order.getCount_product(), order.getOrder_total(), order.getTotal_price(), order.getOrder_date(), newRating, order.getFeedback(), order.getNote(), order.getCreated_at(), order.getSource(), order.getOrder_detail());
            filteredOrders.set(position, updatedOrder);
            adapter.notifyItemChanged(position);
        }
    }

    @Override
    public void onPayNowClick(Order order) {
        // Handle pay now button click - create payment link and open payment screen
        if (order == null || order.getOrder_id() == null) {
            Toast.makeText(this, "Invalid order", Toast.LENGTH_SHORT).show();
            return;
        }

        createPaymentLink(order.getOrder_id());
    }

    private void createPaymentLink(String orderId) {
        final ProgressDialog dialog = ProgressDialog.show(this, null,
                "Creating payment link...", true, false);

        BakeryJavaBridge.INSTANCE.createPaymentLink(this, orderId, new PaymentLinkCallback() {
            @Override
            public void onResult(Response<PaymentLinkResponse> response, Throwable error) {
                dialog.dismiss();

                if (error != null) {
                    Log.e("MyOrdersActivity", "Payment link error", error);
                    Toast.makeText(MyOrdersActivity.this,
                            "Payment link error: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (response != null && response.isSuccessful() && response.body() != null) {
                    PaymentLinkResponse paymentResponse = response.body();

                    if (paymentResponse.getError() == 0) {
                        Log.d("MyOrdersActivity", "Payment link created - URL: " + paymentResponse.getCheckoutUrl());
                        Log.d("MyOrdersActivity", "QR Code present: " + (paymentResponse.getQrCode() != null));
                        if (paymentResponse.getQrCode() != null) {
                            Log.d("MyOrdersActivity", "QR Code length: " + paymentResponse.getQrCode().length());
                        }
                        
                        // Open payment screen
                        Intent intent = new Intent(MyOrdersActivity.this, PaymentActivity.class);
                        intent.putExtra("CHECKOUT_URL", paymentResponse.getCheckoutUrl());
                        intent.putExtra("QR_CODE", paymentResponse.getQrCode());
                        intent.putExtra("ORDER_ID", orderId);
                        startActivity(intent);
                    } else {
                        Toast.makeText(MyOrdersActivity.this,
                                "Payment error: " + paymentResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MyOrdersActivity.this,
                            "Failed to create payment link", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
