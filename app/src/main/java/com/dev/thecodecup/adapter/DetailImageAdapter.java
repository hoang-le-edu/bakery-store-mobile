package com.dev.thecodecup.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dev.thecodecup.R;

import java.util.List;

public class DetailImageAdapter extends RecyclerView.Adapter<DetailImageAdapter.ViewHolder> {

    private final List<Uri> images;
    private final OnRemoveListener onRemoveListener;

    public interface OnRemoveListener {
        void onRemove(Uri uri);
    }

    public DetailImageAdapter(List<Uri> images, OnRemoveListener listener) {
        this.images = images;
        this.onRemoveListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_detail_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri uri = images.get(position);
        Glide.with(holder.itemView.getContext())
                .load(uri)
                .centerCrop()
                .into(holder.ivImage);
        
        holder.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onRemoveListener != null) {
                    onRemoveListener.onRemove(uri);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        ImageButton btnRemove;

        ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
