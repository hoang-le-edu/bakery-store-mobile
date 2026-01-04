package com.dev.thecodecup.activity;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.dev.thecodecup.R;

public class MediaViewerActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvTitle;
    private ImageView ivImage;
    private VideoView videoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_viewer);

        initViews();
        setupData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);
        ivImage = findViewById(R.id.ivImage);
        videoView = findViewById(R.id.videoView);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupData() {
        String mediaUrl = getIntent().getStringExtra("media_url");
        String mediaType = getIntent().getStringExtra("media_type");
        String mediaName = getIntent().getStringExtra("media_name");

        if (mediaName != null) {
            tvTitle.setText(mediaName);
        } else {
            tvTitle.setText("Media");
        }

        if (mediaUrl != null) {
            if ("video".equals(mediaType)) {
                // Show video
                ivImage.setVisibility(View.GONE);
                videoView.setVisibility(View.VISIBLE);
                videoView.setVideoURI(Uri.parse(mediaUrl));
                videoView.setOnPreparedListener(mp -> {
                    videoView.start();
                    mp.setLooping(true);
                });
            } else {
                // Show image
                videoView.setVisibility(View.GONE);
                ivImage.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(mediaUrl)
                        .into(ivImage);
            }
        }
    }
}