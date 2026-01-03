package com.dev.thecodecup.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dev.thecodecup.R;
import com.dev.thecodecup.model.network.api.ReviewItem;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private final Context context;
    private final List<ReviewItem> reviewList = new ArrayList<>();

    public ReviewAdapter(Context context) {
        this.context = context;
    }

    public void setReviews(List<ReviewItem> reviews) {
        reviewList.clear();
        if (reviews != null) {
            reviewList.addAll(reviews);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewItem review = reviewList.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserAvatar;
        TextView tvUserName, tvReviewDate, tvVerifiedPurchase, tvReviewText;
        RatingBar ratingBar;
        RecyclerView rvReviewMedia;
        ReviewMediaAdapter mediaAdapter;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvReviewDate = itemView.findViewById(R.id.tvReviewDate);
            tvVerifiedPurchase = itemView.findViewById(R.id.tvVerifiedPurchase);
            tvReviewText = itemView.findViewById(R.id.tvReviewText);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            rvReviewMedia = itemView.findViewById(R.id.rvReviewMedia);

            // Initialize media adapter
            mediaAdapter = new ReviewMediaAdapter(context);
            rvReviewMedia.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            rvReviewMedia.setAdapter(mediaAdapter);
        }

        public void bind(ReviewItem review) {
            // User Name
            if (review.getUser() != null) {
                tvUserName.setText(review.getUser().getName());
                
                // Avatar
                Glide.with(context)
                        .load(review.getUser().getAvatar())
                        .placeholder(R.drawable.ic_profile_placeholder) // Đảm bảo bạn có drawable này hoặc dùng ảnh khác
                        .error(R.drawable.ic_profile_placeholder)
                        .into(ivUserAvatar);
            } else {
                tvUserName.setText("Unknown User");
                ivUserAvatar.setImageResource(R.drawable.ic_profile_placeholder);
            }

            // Rating
            ratingBar.setRating(review.getRating());

            // Date
            tvReviewDate.setText(review.getReviewed_at());

            // Verified Purchase Badge
            if (review.is_verified_purchase()) {
                tvVerifiedPurchase.setVisibility(View.VISIBLE);
            } else {
                tvVerifiedPurchase.setVisibility(View.GONE);
            }

            // Review Text
            if (review.getReview_text() != null && !review.getReview_text().isEmpty()) {
                tvReviewText.setText(review.getReview_text());
                tvReviewText.setVisibility(View.VISIBLE);
            } else {
                tvReviewText.setVisibility(View.GONE);
            }

            // Media Files
            if (review.getMedia_files() != null && !review.getMedia_files().isEmpty()) {
                mediaAdapter.submitList(review.getMedia_files());
                rvReviewMedia.setVisibility(View.VISIBLE);
            } else {
                rvReviewMedia.setVisibility(View.GONE);
            }
        }
        }
    }
