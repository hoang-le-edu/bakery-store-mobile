package com.dev.thecodecup.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.adapter.OrderHistoryAdapter;
import com.dev.thecodecup.model.network.api.BakeryJavaBridge;
import com.dev.thecodecup.model.network.api.Order;
import com.dev.thecodecup.model.network.api.PaymentLinkCallback;
import com.dev.thecodecup.model.network.api.PaymentLinkResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class OrderListFragment extends Fragment implements OrderHistoryAdapter.OnOrderClickListener {

    private static final String TAG = "OrderListFragment";
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private ProgressBar progressBar;
    private OrderHistoryAdapter adapter;
    private List<Order> orderList = new ArrayList<>();

    // Key để truyền data qua Bundle
    private static final String ARG_ORDERS = "orders";

    public OrderListFragment() {
        // Required empty public constructor
    }

    // Sử dụng Serializable/Parcelable ArrayList vì Order đã implement Serializable
    public static OrderListFragment newInstance(ArrayList<Order> orders) {
        OrderListFragment fragment = new OrderListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ORDERS, orders);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // Lấy danh sách order từ arguments
            // Cần cast về ArrayList<Order>
            // noinspection unchecked
            ArrayList<Order> receivedOrders = (ArrayList<Order>) getArguments().getSerializable(ARG_ORDERS);
            if (receivedOrders != null) {
                this.orderList = receivedOrders;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        tvEmpty = view.findViewById(R.id.tv_empty);
        progressBar = view.findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrderHistoryAdapter(getContext(), this);
        recyclerView.setAdapter(adapter);

        updateUI();
    }

    // Hàm này có thể dùng để update data runtime nếu cần (ví dụ pull-to-refresh từ
    // Activity)
    public void setOrders(List<Order> orders) {
        this.orderList = orders;
        if (adapter != null) {
            updateUI();
        }
    }

    private void updateUI() {
        if (orderList == null || orderList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            adapter.setOrders(orderList);
        }
    }

    @Override
    public void onOrderClick(Order order) {
        // Xử lý khi click vào order (ví dụ: mở màn hình chi tiết OrderDetailActivity)
        // Toast.makeText(getContext(), "Clicked order: " + order.getOrder_number(),
        // Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPayNowClick(Order order) {
        // Handle Pay Now button click
        if (order.getOrder_id() == null || order.getOrder_id().isEmpty()) {
            Toast.makeText(getContext(), "Order ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        createPaymentLink(order.getOrder_id());
    }

    private void createPaymentLink(String orderId) {
        final ProgressDialog dialog = ProgressDialog.show(getContext(), null,
                "Creating payment link...", true, false);

        BakeryJavaBridge.INSTANCE.createPaymentLink(requireActivity(), orderId, new PaymentLinkCallback() {
            @Override
            public void onResult(Response<PaymentLinkResponse> response, Throwable error) {
                dialog.dismiss();

                if (error != null) {
                    Log.e(TAG, "Payment link error", error);
                    Toast.makeText(getContext(),
                            "Payment link error: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (response != null && response.isSuccessful() && response.body() != null) {
                    PaymentLinkResponse paymentResponse = response.body();

                    if (paymentResponse.getError() == 0) {
                        // Open payment screen
                        Intent intent = new Intent(getContext(), PaymentActivity.class);
                        intent.putExtra("CHECKOUT_URL", paymentResponse.getCheckoutUrl());
                        intent.putExtra("ORDER_ID", orderId);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getContext(),
                                "Payment error: " + paymentResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(),
                            "Failed to create payment link", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
