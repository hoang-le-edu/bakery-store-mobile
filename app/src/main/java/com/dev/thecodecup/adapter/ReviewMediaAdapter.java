package com.dev.thecodecup.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dev.thecodecup.R;
import com.dev.thecodecup.activity.MediaViewerActivity;
import com.dev.thecodecup.model.network.api.ReviewMediaFile;

import java.util.Objects;

public class ReviewMediaAdapter extends ListAdapter<ReviewMediaFile, ReviewMediaAdapter.MediaViewHolder> {

    private final Context context;

    public ReviewMediaAdapter(Context context) {
        super(new MediaDiffCallback());
        this.context = context;
    }

    /**
     * Convert review media URL to use build/assets/reviews path
     * From: https://domain.com/storage/reviews/media/filename.jpg
     * To:   https://domain.com/storage/build/assets/reviews/filename.jpg
     */
    private String convertToAssetsPath(String originalUrl) {
        if (originalUrl == null || !originalUrl.contains("/storage/reviews/media/")) {
            return originalUrl;
        }
        
        // Extract filename from the original URL
        String filename = originalUrl.substring(originalUrl.lastIndexOf("/") + 1);
        
        // Build new URL with build/assets/reviews path
        String baseUrl = originalUrl.substring(0, originalUrl.indexOf("/storage/"));
        return baseUrl + "/storage/build/assets/reviews/" + filename;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review_media, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        ReviewMediaFile mediaFile = getItem(position);
        holder.bind(mediaFile, context, this); // Pass adapter instance
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivMedia;
        private final ImageView ivPlayIcon;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMedia = itemView.findViewById(R.id.ivMedia);
            ivPlayIcon = itemView.findViewById(R.id.ivPlayIcon);
        }

        public void bind(ReviewMediaFile mediaFile, Context context, ReviewMediaAdapter adapter) {
            // Convert URL to use build/assets/reviews path
            String imageUrl = adapter.convertToAssetsPath(mediaFile.getUrl());
            
            // Load image/video thumbnail
            Glide.with(context)
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.img_placeholder)
                    .error(R.drawable.img_placeholder)
                    .into(ivMedia);

            // Show play icon for videos
            if ("video".equals(mediaFile.getType())) {
                ivPlayIcon.setVisibility(View.VISIBLE);
            } else {
                ivPlayIcon.setVisibility(View.GONE);
            }

            // Handle click to open full screen viewer
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, MediaViewerActivity.class);
                intent.putExtra("media_url", imageUrl); // Use converted URL
                intent.putExtra("media_type", mediaFile.getType());
                intent.putExtra("media_name", mediaFile.getName());
                context.startActivity(intent);
            });
        }
    }

    private static class MediaDiffCallback extends DiffUtil.ItemCallback<ReviewMediaFile> {
        @Override
        public boolean areItemsTheSame(@NonNull ReviewMediaFile oldItem, @NonNull ReviewMediaFile newItem) {
            return Objects.equals(oldItem.getUrl(), newItem.getUrl());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ReviewMediaFile oldItem, @NonNull ReviewMediaFile newItem) {
            return Objects.equals(oldItem.getUrl(), newItem.getUrl()) &&
                   Objects.equals(oldItem.getType(), newItem.getType()) &&
                   Objects.equals(oldItem.getName(), newItem.getName());
        }
    }
}