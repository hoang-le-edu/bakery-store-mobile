package com.dev.thecodecup.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dev.thecodecup.R;

import java.util.Objects;

public class MediaUploadAdapter extends ListAdapter<MediaUploadAdapter.MediaItem, MediaUploadAdapter.MediaViewHolder> {

    public interface OnMediaActionListener {
        void onRemoveMedia(MediaItem item, int position);
    }

    private final OnMediaActionListener listener;

    public MediaUploadAdapter(OnMediaActionListener listener) {
        super(new MediaDiffCallback());
        this.listener = listener;
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
                .inflate(R.layout.item_media_upload, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        MediaItem item = getItem(position);
        holder.bind(item, position, listener, this); // Pass adapter instance
    }

    public static class MediaItem {
        private final Uri uri;
        private final String type; // "image" or "video"
        private final String name;
        private String filePath; // set after successful upload (for API submission)
        private String fileUrl;  // set after successful upload (for display)
        private boolean isUploading;

        public MediaItem(Uri uri, String type, String name) {
            this.uri = uri;
            this.type = type;
            this.name = name;
            this.isUploading = false;
        }

        // Getters
        public Uri getUri() { return uri; }
        public String getType() { return type; }
        public String getName() { return name; }
        public String getFilePath() { return filePath; }
        public String getFileUrl() { return fileUrl; }
        public boolean isUploading() { return isUploading; }

        // Setters
        public void setFilePath(String filePath) { this.filePath = filePath; }
        public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
        public void setUploading(boolean uploading) { this.isUploading = uploading; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MediaItem mediaItem = (MediaItem) o;
            return Objects.equals(uri, mediaItem.uri) &&
                   Objects.equals(type, mediaItem.type) &&
                   Objects.equals(name, mediaItem.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uri, type, name);
        }
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivMedia;
        private final ImageView ivPlayIcon;
        private final ProgressBar progressBar;
        private final ImageButton btnRemove;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMedia = itemView.findViewById(R.id.ivMedia);
            ivPlayIcon = itemView.findViewById(R.id.ivPlayIcon);
            progressBar = itemView.findViewById(R.id.progressBar);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }

        public void bind(MediaItem item, int position, OnMediaActionListener listener, MediaUploadAdapter adapter) {
            // Load image/video thumbnail - use fileUrl if available (after upload), otherwise use original URI
            String imageSource = item.getFileUrl() != null ? 
                adapter.convertToAssetsPath(item.getFileUrl()) : 
                item.getUri().toString();
            
            Glide.with(itemView.getContext())
                    .load(imageSource)
                    .centerCrop()
                    .placeholder(R.drawable.img_placeholder)
                    .error(R.drawable.img_placeholder)
                    .into(ivMedia);

            // Show play icon for videos
            if ("video".equals(item.getType())) {
                ivPlayIcon.setVisibility(View.VISIBLE);
            } else {
                ivPlayIcon.setVisibility(View.GONE);
            }

            // Show progress or remove button
            if (item.isUploading()) {
                progressBar.setVisibility(View.VISIBLE);
                btnRemove.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.GONE);
                btnRemove.setVisibility(View.VISIBLE);
            }

            // Handle remove click
            btnRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveMedia(item, position);
                }
            });
        }
    }

    private static class MediaDiffCallback extends DiffUtil.ItemCallback<MediaItem> {
        @Override
        public boolean areItemsTheSame(@NonNull MediaItem oldItem, @NonNull MediaItem newItem) {
            return oldItem.getUri().equals(newItem.getUri());
        }

        @Override
        public boolean areContentsTheSame(@NonNull MediaItem oldItem, @NonNull MediaItem newItem) {
            return oldItem.equals(newItem) && 
                   oldItem.isUploading() == newItem.isUploading() &&
                   Objects.equals(oldItem.getFilePath(), newItem.getFilePath()) &&
                   Objects.equals(oldItem.getFileUrl(), newItem.getFileUrl());
        }
    }
}