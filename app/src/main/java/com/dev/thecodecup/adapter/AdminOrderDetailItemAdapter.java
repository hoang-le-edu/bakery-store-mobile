package com.dev.thecodecup.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dev.thecodecup.R;
import com.dev.thecodecup.model.network.ApiService;
import com.dev.thecodecup.model.network.dto.ApiResponse;
import com.dev.thecodecup.model.network.dto.AdminOrderItemDto;
import com.dev.thecodecup.model.network.dto.AdminProductDetailDto;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminOrderDetailItemAdapter extends RecyclerView.Adapter<AdminOrderDetailItemAdapter.OrderItemViewHolder> {

    private static final String TAG = "AdminOrderDetailAdapter";
    private final ApiService apiService;
    private final List<AdminOrderItemDto> items = new ArrayList<>();

    public AdminOrderDetailItemAdapter(ApiService apiService) {
        this.apiService = apiService;
    }

    public void setItems(List<AdminOrderItemDto> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_admin_order_detail_product, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class OrderItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName;
        TextView tvVariant;
        TextView tvPrice;
        TextView tvNote;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvVariant = itemView.findViewById(R.id.tvVariant);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvNote = itemView.findViewById(R.id.tvNote);
        }

        void bind(AdminOrderItemDto item) {
            tvProductName.setText(item.getProductName() != null ? item.getProductName() : "Unknown");

            // Build variant string: Size S • x1 • X topping
            StringBuilder variantBuilder = new StringBuilder();
            if (item.getSize() != null && !item.getSize().isEmpty()) {
                variantBuilder.append("Size ").append(item.getSize());
            }
            variantBuilder.append(" • x").append(item.getQuantity() != null ? item.getQuantity() : 0);
            
            Integer toppingCount = item.getCountTopping();
            if (toppingCount != null && toppingCount > 0) {
                variantBuilder.append(" • ").append(toppingCount).append(" topping");
            } else if (item.getToppings() != null && !item.getToppings().isEmpty()) {
                variantBuilder.append(" • ").append(item.getToppings().size()).append(" topping");
            }
            
            tvVariant.setText(variantBuilder.toString());

            // Price
            String priceStr = item.getTotalPrice() != null ? item.getTotalPrice() : item.getProductPrice();
            tvPrice.setText(formatCurrency(priceStr));

            // Note
            if (item.getNote() != null && !item.getNote().isEmpty()) {
                tvNote.setVisibility(View.VISIBLE);
                tvNote.setText("Note: " + item.getNote());
            } else {
                tvNote.setVisibility(View.GONE);
            }

            // Load image with logging - try multiple possible field names
            String imageUrl = item.getImage();
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = item.getProductImage();
            }
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = item.getImageUrl();
            }
            
            Log.d(TAG, "Product: " + item.getProductName() + ", Image URL: " + imageUrl + ", ProductId: " + item.getProductId());
            
            if (imageUrl != null && !imageUrl.isEmpty()) {
                loadImage(imageUrl);
            } else if (item.getProductId() != null && !item.getProductId().isEmpty()) {
                fetchProductImage(item.getProductId());
            } else {
                Log.w(TAG, "No image URL or productId available for: " + item.getProductName());
                imgProduct.setImageResource(R.drawable.placeholder_image);
            }
        }

        private void fetchProductImage(String productId) {
            if (apiService == null) {
                Log.e(TAG, "ApiService is null");
                imgProduct.setImageResource(R.drawable.placeholder_image);
                return;
            }
            
            Log.d(TAG, "Fetching product image for productId: " + productId);
            apiService.getAdminProductById(productId).enqueue(new Callback<ApiResponse<AdminProductDetailDto>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<AdminProductDetailDto>> call,
                                       @NonNull Response<ApiResponse<AdminProductDetailDto>> response) {
                    String fetchedUrl = null;
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        fetchedUrl = response.body().getData().getImageUrl();
                        Log.d(TAG, "Fetched image URL: " + fetchedUrl);
                    } else {
                        Log.w(TAG, "Failed to fetch product image. Response code: " + response.code());
                    }
                    
                    if (fetchedUrl != null && !fetchedUrl.isEmpty()) {
                        loadImage(fetchedUrl);
                    } else {
                        imgProduct.setImageResource(R.drawable.placeholder_image);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ApiResponse<AdminProductDetailDto>> call, @NonNull Throwable t) {
                    Log.e(TAG, "Error fetching product image: " + t.getMessage(), t);
                    imgProduct.setImageResource(R.drawable.placeholder_image);
                }
            });
        }

        private void loadImage(String url) {
            // Normalize duplicated '/build/assets' segments in product image URLs
            String normalizedUrl = normalizeAssetsUrl(url);
            Log.d(TAG, "Loading image with Glide: " + normalizedUrl);
            Glide.with(itemView.getContext())
                    .load(normalizedUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(imgProduct);
        }

        /**
         * Collapse duplicated 'build/assets' segments in URLs.
         * Example:
         *   /storage/build/assets/build/assets/Product/... -> /storage/build/assets/Product/...
         */
        private String normalizeAssetsUrl(String url) {
            if (url == null) return null;
            String normalized = url;
            while (normalized.contains("/build/assets/build/assets/")) {
                normalized = normalized.replace("/build/assets/build/assets/", "/build/assets/");
            }
            return normalized;
        }

        private String formatCurrency(String value) {
            if (value == null || value.isEmpty()) return "0đ";
            try {
                double number = Double.parseDouble(value);
                DecimalFormat formatter = new DecimalFormat("###,###,###");
                return formatter.format(number) + "đ";
            } catch (Exception e) {
                return value + "đ";
            }
        }
    }
}
