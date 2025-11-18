// ProductAdapter.java
package com.dev.thecodecup.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.dev.thecodecup.R;
import com.dev.thecodecup.activity.ProductDetailActivity;
import com.dev.thecodecup.model.network.dto.ProductDto;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {

    private final Context ctx;
    private final List<ProductDto> items = new ArrayList<>();
    private OnItemClickListener clickListener;

    public interface OnItemClickListener {
        void onItemClick(ProductDto product);
    }

    public ProductAdapter(Context ctx) {
        this.ctx = ctx;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setItems(List<ProductDto> data) {
        items.clear();
        if (data != null)
            items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false); // uses ivImage, tvName, tvPrice, btnAdd
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ProductDto p = items.get(position);

        android.util.Log.d("Adapter", "Binding Product: " + p.getProductName());

        h.tvName.setText(p.getProductName() != null ? p.getProductName() : "");

        // price "6000.00" -> "6.000đ"
        String price = p.getProductPrice();
        if (price != null) {
            try {
                double d = Double.parseDouble(price);
                h.tvPrice.setText(new DecimalFormat("#,###").format(d) + "đ");
            } catch (NumberFormatException e) {
                h.tvPrice.setText(price + "đ");
            }
        } else {
            h.tvPrice.setText("");
        }

        String url = p.getProductImage(); // JSON field product_image (URL đầy đủ)
        RequestOptions opts = new RequestOptions()
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop();

        Glide.with(h.ivImage.getContext())
                .setDefaultRequestOptions(opts)
                .load(url)
                .into(h.ivImage);


        h.btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(ctx, ProductDetailActivity.class);
            intent.putExtra("productId", p.getProductId());
            ctx.startActivity(intent);
        });

        // Click on item to view detail
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
        ImageView ivImage;
        TextView tvName, tvPrice;
        ImageButton btnAdd;

        VH(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }
    }
}
