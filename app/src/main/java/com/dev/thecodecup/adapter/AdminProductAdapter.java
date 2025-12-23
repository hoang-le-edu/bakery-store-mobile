package com.dev.thecodecup.adapter;

import android.content.Context;
import android.text.TextUtils;
import com.dev.thecodecup.activity.AdminUpdateProductDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.model.network.dto.AdminProductDto;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.VH> {
    private final Context ctx;
    private final List<AdminProductDto> items = new ArrayList<>();
    private OnItemClickListener clickListener;
    private boolean isToppingMode = false;

    public void setToppingMode(boolean topping) {
        isToppingMode = topping;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(AdminProductDto product);
    }

    public AdminProductAdapter(Context ctx) {
        this.ctx = ctx;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setItems(List<AdminProductDto> data) {
        items.clear();
        if (data != null)
            items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_admin_product, parent, false); // layout riêng cho admin
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AdminProductDto p = items.get(position);
        h.txtProductName.setText(p.getProductName() != null ? p.getProductName() : "");
        String price = p.getProductPrice();
        if (price != null) {
            try {
                double d = Double.parseDouble(price);
                h.txtProductPrice.setText(new DecimalFormat("#,###").format(d) + "đ");
            } catch (NumberFormatException e) {
                h.txtProductPrice.setText(price + "đ");
            }
        } else {
            h.txtProductPrice.setText("");
        }

        // Trạng thái: nếu có logic kiểm soát hàng, ví dụ còn hàng/hết hàng
        h.txtProductStatus.setText("In stock"); // or get from p if available

        // Đánh giá
        int rating = p.getAvgRating() != null ? p.getAvgRating() : 0;
        int reviewCount = p.getReviewCount() != null ? p.getReviewCount() : 0;
        h.txtProductRating.setText(rating + "★ (" + reviewCount + " reviews)");

        // Luôn hiển thị hình ảnh nếu có (kể cả topping)
        if (h.imgProduct != null) {
            h.imgProduct.setVisibility(View.VISIBLE);
            String imgUrl = p.getProductImageUrl();
            if (imgUrl != null && !imgUrl.isEmpty()) {
                // Sử dụng Glide để load ảnh
                try {
                    com.bumptech.glide.Glide.with(h.imgProduct.getContext())
                        .load(imgUrl)
                        .placeholder(com.dev.thecodecup.R.drawable.placeholder_image)
                        .error(com.dev.thecodecup.R.drawable.error_image)
                        .into(h.imgProduct);
                } catch (Exception e) {
                    h.imgProduct.setImageResource(com.dev.thecodecup.R.drawable.placeholder_image);
                }
            } else {
                h.imgProduct.setImageResource(com.dev.thecodecup.R.drawable.placeholder_image);
            }
        }

        // Nút sửa/xóa sản phẩm
        h.btnEditProduct.setOnClickListener(v -> {
            // Hiển thị dialog cập nhật sản phẩm
            new AdminUpdateProductDialog(v.getContext(), p, updatedProduct -> {
                if (clickListener != null) {
                    clickListener.onItemClick(updatedProduct); // callback cho activity xử lý update API
                }
            }).show();
        });
        h.btnDeleteProduct.setOnClickListener(v -> {
            // TODO: xác nhận và xóa sản phẩm
        });
        h.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onItemClick(p);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtProductName, txtProductPrice, txtProductStatus, txtProductRating;
        ImageButton btnEditProduct, btnDeleteProduct;
        android.widget.ImageView imgProduct;
        VH(@NonNull View itemView) {
            super(itemView);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtProductPrice = itemView.findViewById(R.id.txtProductPrice);
            txtProductStatus = itemView.findViewById(R.id.txtProductStatus);
            txtProductRating = itemView.findViewById(R.id.txtProductRating);
            btnEditProduct = itemView.findViewById(R.id.btnEditProduct);
            btnDeleteProduct = itemView.findViewById(R.id.btnDeleteProduct);
            imgProduct = itemView.findViewById(R.id.imgProduct);
        }
    }
}
