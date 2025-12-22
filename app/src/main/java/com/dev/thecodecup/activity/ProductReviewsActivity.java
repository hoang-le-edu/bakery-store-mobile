package com.dev.thecodecup.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.adapter.ReviewAdapter;
import com.dev.thecodecup.model.network.api.BakeryJavaBridge;
import com.dev.thecodecup.model.network.api.ProductReviewsCallback;
import com.dev.thecodecup.model.network.api.RatingDistribution;
import com.dev.thecodecup.model.network.api.ReviewResponse;
import com.dev.thecodecup.model.network.api.ReviewSummary;

import retrofit2.Response;

public class ProductReviewsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvAverageRating, tvTotalReviews;
    private RatingBar ratingBarAverage;
    
    // Progress Bars for distribution
    private ProgressBar progress5, progress4, progress3, progress2, progress1;
    private TextView tvCount5, tvCount4, tvCount3, tvCount2, tvCount1;

    private RecyclerView rvReviews;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private ReviewAdapter reviewAdapter;
    private String productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_reviews);

        productId = getIntent().getStringExtra("productId");
        if (productId == null) {
            Toast.makeText(this, "Product ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        loadReviews();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvAverageRating = findViewById(R.id.tvAverageRating);
        tvTotalReviews = findViewById(R.id.tvTotalReviews);
        ratingBarAverage = findViewById(R.id.ratingBarAverage);

        progress5 = findViewById(R.id.progress5);
        progress4 = findViewById(R.id.progress4);
        progress3 = findViewById(R.id.progress3);
        progress2 = findViewById(R.id.progress2);
        progress1 = findViewById(R.id.progress1);

        tvCount5 = findViewById(R.id.tvCount5);
        tvCount4 = findViewById(R.id.tvCount4);
        tvCount3 = findViewById(R.id.tvCount3);
        tvCount2 = findViewById(R.id.tvCount2);
        tvCount1 = findViewById(R.id.tvCount1);

        rvReviews = findViewById(R.id.rvReviews);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
    }

    private void setupRecyclerView() {
        reviewAdapter = new ReviewAdapter(this);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        rvReviews.setAdapter(reviewAdapter);
    }

    private void loadReviews() {
        progressBar.setVisibility(View.VISIBLE);
        rvReviews.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        BakeryJavaBridge.INSTANCE.getProductReviews(this, productId, 1, new ProductReviewsCallback() {
            @Override
            public void onResult(Response<ReviewResponse> response, Throwable error) {
                progressBar.setVisibility(View.GONE);

                if (error != null) {
                    Log.e("ProductReviews", "Error loading reviews", error);
                    Toast.makeText(ProductReviewsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response != null && response.isSuccessful() && response.body() != null) {
                    ReviewResponse reviewResponse = response.body();
                    if (reviewResponse.getSuccess()) {
                        displayData(reviewResponse);
                    } else {
                        Toast.makeText(ProductReviewsActivity.this, reviewResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProductReviewsActivity.this, "Failed to load reviews", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void displayData(ReviewResponse response) {
        // 1. Update Summary
        ReviewSummary summary = response.getData().getSummary();
        
        tvAverageRating.setText(String.format("%.1f", summary.getAverage_rating()));
        ratingBarAverage.setRating((float) summary.getAverage_rating());
        tvTotalReviews.setText(summary.getTotal_reviews() + " reviews");

        updateDistribution(summary.getRating_distribution(), summary.getTotal_reviews());

        // 2. Update List
        if (response.getData().getReviews().getData() != null && !response.getData().getReviews().getData().isEmpty()) {
            reviewAdapter.setReviews(response.getData().getReviews().getData());
            rvReviews.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        } else {
            rvReviews.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void updateDistribution(RatingDistribution dist, int total) {
        if (total == 0) return;

        updateProgressRow(progress5, tvCount5, dist.getFive(), total);
        updateProgressRow(progress4, tvCount4, dist.getFour(), total);
        updateProgressRow(progress3, tvCount3, dist.getThree(), total);
        updateProgressRow(progress2, tvCount2, dist.getTwo(), total);
        updateProgressRow(progress1, tvCount1, dist.getOne(), total);
    }

    private void updateProgressRow(ProgressBar progress, TextView tvCount, int count, int total) {
        int percentage = (int) ((count / (float) total) * 100);
        progress.setProgress(percentage);
        tvCount.setText(String.valueOf(count));
    }
}
