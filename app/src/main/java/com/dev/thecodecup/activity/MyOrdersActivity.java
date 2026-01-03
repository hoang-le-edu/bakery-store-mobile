package com.dev.thecodecup.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.thecodecup.R;
import com.dev.thecodecup.adapter.MediaUploadAdapter;
import com.dev.thecodecup.adapter.OrderHistoryAdapter;
import com.dev.thecodecup.model.auth.AuthManager;
import com.dev.thecodecup.model.network.api.BakeryJavaBridge;
import com.dev.thecodecup.model.network.api.CreateReviewRequest;
import com.dev.thecodecup.model.network.api.CustomerOrdersResponse;
import com.dev.thecodecup.model.network.api.MyReviewData;
import com.dev.thecodecup.model.network.api.Order;
import com.dev.thecodecup.model.network.api.PaymentLinkCallback;
import com.dev.thecodecup.model.network.api.PaymentLinkResponse;
import com.dev.thecodecup.model.network.api.UpdateReviewRequest;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;

public class MyOrdersActivity extends BaseAuthActivity implements OrderHistoryAdapter.OnOrderClickListener, OrderHistoryAdapter.OnReviewClickListener {

    private ImageButton btnBack;
    private TextView tabAll, tabWaitForApproval, tabInProgress, tabDelivered, tabCancelled;
    private RecyclerView rvOrders;

    private OrderHistoryAdapter adapter;

    private final List<Order> allOrders = new ArrayList<>();
    private final List<Order> filteredOrders = new ArrayList<>();

    // Media upload related fields
    private MediaUploadAdapter mediaUploadAdapter;
    private List<MediaUploadAdapter.MediaItem> uploadedMediaItems = new ArrayList<>();
    private List<String> uploadedFilePaths = new ArrayList<>();
    private ExecutorService uploadExecutor = Executors.newSingleThreadExecutor();
    private ActivityResultLauncher<Intent> mediaPickerLauncher;

    private static final String FILTER_ALL = "ALL";
    private static final String FILTER_WAIT_FOR_APPROVAL = "Wait For Approval";
    private static final String FILTER_IN_PROGRESS = "In Progress";
    private static final String FILTER_DELIVERED = "Completed";
    private static final String FILTER_CANCELLED = "Cancelled";

    private String currentFilter = FILTER_ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        initMediaPicker();
        initViews();
        setupTabs();
        setupRecycler();
        loadOrders();
    }

    private void initMediaPicker() {
        mediaPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    handleMediaSelection(result.getData());
                }
            }
        );
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tabAll = findViewById(R.id.tabAll);
        tabWaitForApproval = findViewById(R.id.tabWaitForApproval);
        tabInProgress = findViewById(R.id.tabInProgress);
        tabDelivered = findViewById(R.id.tabDelivered);
        tabCancelled = findViewById(R.id.tabCancelled);
        rvOrders = findViewById(R.id.rvOrders);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecycler() {
        adapter = new OrderHistoryAdapter(this, this, this);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adapter);
    }

    private void setupTabs() {
        View.OnClickListener listener = v -> {
            int id = v.getId();
            if (id == R.id.tabWaitForApproval) {
                currentFilter = FILTER_WAIT_FOR_APPROVAL;
            } else if (id == R.id.tabInProgress) {
                currentFilter = FILTER_IN_PROGRESS;
            } else if (id == R.id.tabDelivered) {
                currentFilter = FILTER_DELIVERED;
            } else if (id == R.id.tabCancelled) {
                currentFilter = FILTER_CANCELLED;
            } else {
                currentFilter = FILTER_ALL;
            }

            updateTabUI();
            applyFilter();
        };

        tabAll.setOnClickListener(listener);
        tabWaitForApproval.setOnClickListener(listener);
        tabInProgress.setOnClickListener(listener);
        tabDelivered.setOnClickListener(listener);
        tabCancelled.setOnClickListener(listener);

        updateTabUI(); // default = ALL
    }

    private void updateTabUI() {
        resetTab(tabAll);
        resetTab(tabWaitForApproval);
        resetTab(tabInProgress);
        resetTab(tabDelivered);
        resetTab(tabCancelled);

        switch (currentFilter) {
            case FILTER_WAIT_FOR_APPROVAL:
                setTabSelected(tabWaitForApproval);
                break;
            case FILTER_IN_PROGRESS:
                setTabSelected(tabInProgress);
                break;
            case FILTER_DELIVERED:
                setTabSelected(tabDelivered);
                break;
            case FILTER_CANCELLED:
                setTabSelected(tabCancelled);
                break;
            case FILTER_ALL:
            default:
                setTabSelected(tabAll);
                break;
        }
    }

    private void resetTab(TextView tab) {
        tab.setBackground(null);
        tab.setTextColor(ContextCompat.getColor(this, android.R.color.black));
    }

    private void setTabSelected(TextView tab) {
        tab.setBackgroundResource(R.drawable.bg_order_tab_selected);
        tab.setTextColor(ContextCompat.getColor(this, android.R.color.white));
    }

    private void applyFilter() {
        filteredOrders.clear();

        if (FILTER_ALL.equals(currentFilter)) {
            filteredOrders.addAll(allOrders);
        } else {
            for (Order o : allOrders) {
                String status = o.getStatus();
                if (status == null) {
                    status = o.getOrder_status();
                }
                if (status != null && status.equals(currentFilter)) {
                    filteredOrders.add(o);
                }
            }
        }

        adapter.setOrders(filteredOrders);
    }

    private void loadOrders() {
        ProgressDialog dialog = ProgressDialog.show(this, null, "Loading orders...", true, false);

        BakeryJavaBridge.INSTANCE.loadCustomerOrders(this, (response, error) -> {
            dialog.dismiss();

            if (error != null) {
                Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (response != null && response.isSuccessful() && response.body() != null) {
                CustomerOrdersResponse ordersResponse = response.body();
                processOrders(ordersResponse.getData());
            } else {
                Toast.makeText(this, "Failed to load orders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processOrders(Map<String, List<Order>> data) {
        if (data == null) return;

        allOrders.clear();

        // Collect all orders from all status groups
        if (data.containsKey("Wait For Approval")) {
            allOrders.addAll(data.get("Wait For Approval"));
        }
        
        if (data.containsKey("In Progress")) {
            allOrders.addAll(data.get("In Progress"));
        }
        
        if (data.containsKey("Completed")) {
            allOrders.addAll(data.get("Completed"));
        }
        
        if (data.containsKey("Cancelled")) {
            allOrders.addAll(data.get("Cancelled"));
        }

        applyFilter();
    }

    @Override
    public void onOrderClick(Order order) {
        // Open OrderDetailActivity when user clicks on an order
        if (order == null || order.getOrder_id() == null || order.getOrder_id().isEmpty()) {
            Toast.makeText(this, "Order ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra(OrderDetailActivity.EXTRA_ORDER_ID, order.getOrder_id());
        startActivity(intent);
    }

    @Override
    public void onReviewClick(Order order, int position) {
        ProgressDialog dialog = ProgressDialog.show(this, "", "Checking for existing review...", true, false);

        BakeryJavaBridge.INSTANCE.getMyReview(this, order.getOrder_id(), (response, error) -> {
            dialog.dismiss();
            if (response != null && response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                // Review exists, show view/edit dialog
                showReviewDialog(order, position, response.body().getData());
            } else {
                // No review exists or error occurred (e.g. 404), show create dialog
                showReviewDialog(order, position, null);
            }
        });
    }

    private void showReviewDialog(final Order order, final int position, @Nullable final MyReviewData existingReview) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_review_input, null);
        builder.setView(view);
        final AlertDialog dialog = builder.create();

        // Initialize views
        TextView tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        RatingBar ratingBar = view.findViewById(R.id.ratingBar_dialog);
        TextInputLayout reviewTextInputLayout = view.findViewById(R.id.reviewTextInputLayout);
        EditText etReviewText = view.findViewById(R.id.etReviewText);
        TextView tvReadOnlyReview = view.findViewById(R.id.tvReadOnlyReview);
        Button btnSubmit = view.findViewById(R.id.btnSubmitReview);
        Button btnEdit = view.findViewById(R.id.btnEditReview);
        Button btnDelete = view.findViewById(R.id.btnDeleteReview);
        
        // Media upload components
        MaterialCardView cardAddMedia = view.findViewById(R.id.cardAddMedia);
        RecyclerView rvUploadedMedia = view.findViewById(R.id.rvUploadedMedia);

        // Initialize media upload adapter
        uploadedMediaItems = new ArrayList<>();
        uploadedFilePaths = new ArrayList<>();
        
        mediaUploadAdapter = new MediaUploadAdapter(new MediaUploadAdapter.OnMediaActionListener() {
            @Override
            public void onRemoveMedia(MediaUploadAdapter.MediaItem item, int position) {
                uploadedMediaItems.remove(position);
                if (item.getFilePath() != null) {
                    uploadedFilePaths.remove(item.getFilePath());
                }
                mediaUploadAdapter.submitList(new ArrayList<>(uploadedMediaItems));
            }
        });
        
        rvUploadedMedia.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvUploadedMedia.setAdapter(mediaUploadAdapter);

        // Add media button click listener
        cardAddMedia.setOnClickListener(v -> showMediaPicker());

        // Mode: VIEW existing review
        if (existingReview != null) {
            tvDialogTitle.setText("Your Review");
            btnSubmit.setVisibility(View.GONE);
            btnEdit.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
            reviewTextInputLayout.setVisibility(View.GONE);
            tvReadOnlyReview.setVisibility(View.VISIBLE);
            cardAddMedia.setVisibility(View.GONE);
            ratingBar.setIsIndicator(true);

            ratingBar.setRating(existingReview.getRating());
            tvReadOnlyReview.setText(existingReview.getReview_text());
            
            // Load existing media files
            if (existingReview.getMedia_files() != null && !existingReview.getMedia_files().isEmpty()) {
                loadExistingMediaFiles(existingReview.getMedia_files());
            }

            // EDIT button logic
            btnEdit.setOnClickListener(v -> {
                tvDialogTitle.setText("Edit Review");
                btnEdit.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
                btnSubmit.setText("Update");
                btnSubmit.setVisibility(View.VISIBLE);
                reviewTextInputLayout.setVisibility(View.VISIBLE);
                tvReadOnlyReview.setVisibility(View.GONE);
                cardAddMedia.setVisibility(View.VISIBLE);
                etReviewText.setText(existingReview.getReview_text());
                ratingBar.setIsIndicator(false);
            });

            // DELETE button logic
            btnDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                    .setTitle("Delete Review")
                    .setMessage("Are you sure you want to delete this review?")
                    .setPositiveButton("Delete", (d, i) -> deleteReview(order.getOrder_id(), position, dialog))
                    .setNegativeButton("Cancel", null)
                    .show();
            });
        }
        // Mode: CREATE new review
        else {
            tvDialogTitle.setText("Write a Review");
            btnSubmit.setText("Submit");
        }

        btnSubmit.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            if (rating == 0) {
                Toast.makeText(this, "Please provide a rating (at least 1 star)", Toast.LENGTH_SHORT).show();
                return;
            }
            String reviewText = etReviewText.getText().toString().trim();

            // Check if any media is still uploading
            boolean hasUploadingMedia = false;
            for (MediaUploadAdapter.MediaItem item : uploadedMediaItems) {
                if (item.isUploading()) {
                    hasUploadingMedia = true;
                    break;
                }
            }
            
            if (hasUploadingMedia) {
                Toast.makeText(this, "Please wait for media uploads to complete", Toast.LENGTH_SHORT).show();
                return;
            }

            // If we are UPDATING an existing review
            if (existingReview != null) {
                UpdateReviewRequest request = new UpdateReviewRequest((int) rating, reviewText, uploadedFilePaths);
                BakeryJavaBridge.INSTANCE.updateReview(this, order.getOrder_id(), request, (response, error) -> {
                    if (response != null && response.isSuccessful()) {
                        Toast.makeText(this, "Review updated!", Toast.LENGTH_SHORT).show();
                        updateOrderInList(position, (int) rating);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Failed to update review", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            // If we are CREATING a new review
            else {
                if (order.getOrder_detail() == null || order.getOrder_detail().isEmpty()) {
                    Toast.makeText(this, "Cannot submit review: Product info missing.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String productId = order.getOrder_detail().get(0).getProduct_id();
                CreateReviewRequest request = new CreateReviewRequest(order.getOrder_id(), (int) rating, reviewText, uploadedFilePaths);
                BakeryJavaBridge.INSTANCE.createReview(this, productId, request, (response, error) -> {
                    if (response != null && response.isSuccessful()) {
                        Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show();
                        updateOrderInList(position, (int) rating);
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Failed to submit review", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        dialog.show();
    }

    private void showMediaPicker() {
        if (uploadedMediaItems.size() >= 5) {
            Toast.makeText(this, "Maximum 5 files allowed", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {"image/jpeg", "image/png", "image/gif", "video/mp4", "video/mov", "video/avi"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        
        try {
            mediaPickerLauncher.launch(Intent.createChooser(intent, "Select Media"));
        } catch (Exception e) {
            Toast.makeText(this, "Error opening media picker", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleMediaSelection(Intent data) {
        List<Uri> selectedUris = new ArrayList<>();
        
        if (data.getClipData() != null) {
            // Multiple files selected
            int count = data.getClipData().getItemCount();
            for (int i = 0; i < count; i++) {
                Uri uri = data.getClipData().getItemAt(i).getUri();
                selectedUris.add(uri);
            }
        } else if (data.getData() != null) {
            // Single file selected
            selectedUris.add(data.getData());
        }

        for (Uri uri : selectedUris) {
            if (uploadedMediaItems.size() >= 5) {
                Toast.makeText(this, "Maximum 5 files allowed", Toast.LENGTH_SHORT).show();
                break;
            }
            
            if (validateAndAddMediaItem(uri)) {
                // Item was added successfully
            }
        }
    }

    private boolean validateAndAddMediaItem(Uri uri) {
        try {
            String mimeType = getContentResolver().getType(uri);
            if (mimeType == null) {
                Toast.makeText(this, "Cannot determine file type", Toast.LENGTH_SHORT).show();
                return false;
            }

            // Validate file type
            if (!isValidMediaType(mimeType)) {
                Toast.makeText(this, "Invalid file type. Please select JPG, PNG, GIF, MP4, MOV, or AVI files", Toast.LENGTH_SHORT).show();
                return false;
            }

            // Get file size
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            long fileSize = 0;
            String fileName = "media_file";
            
            if (cursor != null) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                
                if (cursor.moveToFirst()) {
                    if (sizeIndex != -1) {
                        fileSize = cursor.getLong(sizeIndex);
                    }
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex);
                    }
                }
                cursor.close();
            }

            // Validate file size (10MB limit)
            long maxSize = 10 * 1024 * 1024; // 10MB
            if (fileSize > maxSize) {
                Toast.makeText(this, "File too large. Maximum size is 10MB", Toast.LENGTH_SHORT).show();
                return false;
            }

            // Create media item
            String mediaType = mimeType.startsWith("video/") ? "video" : "image";
            MediaUploadAdapter.MediaItem mediaItem = new MediaUploadAdapter.MediaItem(uri, mediaType, fileName);
            
            // Add to list and start upload
            uploadedMediaItems.add(mediaItem);
            mediaUploadAdapter.submitList(new ArrayList<>(uploadedMediaItems));
            
            // Upload the file
            uploadMediaFile(mediaItem);
            
            return true;
        } catch (Exception e) {
            Toast.makeText(this, "Error processing file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean isValidMediaType(String mimeType) {
        return mimeType.equals("image/jpeg") ||
               mimeType.equals("image/jpg") ||
               mimeType.equals("image/png") ||
               mimeType.equals("image/gif") ||
               mimeType.equals("video/mp4") ||
               mimeType.equals("video/mov") ||
               mimeType.equals("video/avi");
    }

    private void uploadMediaFile(MediaUploadAdapter.MediaItem mediaItem) {
        Log.d("MediaUpload", "Starting upload for: " + mediaItem.getName());
        mediaItem.setUploading(true);
        mediaUploadAdapter.submitList(new ArrayList<>(uploadedMediaItems));

        uploadExecutor.execute(() -> {
            File tempFile = null;
            try {
                Log.d("MediaUpload", "Creating temp file from URI: " + mediaItem.getUri());
                // Create a temporary file
                tempFile = createTempFileFromUri(mediaItem.getUri(), mediaItem.getName());
                Log.d("MediaUpload", "Temp file created: " + tempFile.getAbsolutePath() + ", size: " + tempFile.length() + " bytes");
                
                // Create RequestBody and MultipartBody.Part
                String mimeType = getContentResolver().getType(mediaItem.getUri());
                Log.d("MediaUpload", "MIME type: " + mimeType);
                
                RequestBody requestBody = RequestBody.create(tempFile, MediaType.parse(mimeType));
                MultipartBody.Part mediaPart = MultipartBody.Part.createFormData("media", tempFile.getName(), requestBody);
                
                // Get auth token
                String token = AuthManager.INSTANCE.getIdTokenOrNull();
                if (token == null) {
                    Log.e("MediaUpload", "Authentication token is null");
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Authentication token not found", Toast.LENGTH_SHORT).show();
                        removeMediaItem(mediaItem);
                    });
                    return;
                }
                
                Log.d("MediaUpload", "Starting API call with token length: " + token.length());

                // Keep reference to temp file for cleanup
                final File finalTempFile = tempFile;

                // Upload via BakeryJavaBridge
                BakeryJavaBridge.INSTANCE.uploadReviewMedia(this, "Bearer " + token, mediaPart, (response, error) -> {
                    Log.d("MediaUpload", "Upload response received");
                    runOnUiThread(() -> {
                        mediaItem.setUploading(false);
                        
                        if (response != null && response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                            // Upload successful - use file_url for display, file_path for API
                            String filePath = response.body().getData().getFile_path();
                            String fileUrl = response.body().getData().getFile_url();
                            String convertedUrl = convertToAssetsPath(fileUrl); // Convert to assets path
                            
                            Log.d("MediaUpload", "Upload successful, file path: " + filePath + ", file URL: " + fileUrl + ", converted: " + convertedUrl);
                            mediaItem.setFilePath(filePath);     // Keep file path for API submission
                            mediaItem.setFileUrl(convertedUrl);  // Use converted URL for display
                            uploadedFilePaths.add(filePath);
                            
                            Toast.makeText(this, "Media uploaded successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            // Upload failed - log the error for debugging
                            String errorMessage = "Failed to upload media";
                            if (error != null) {
                                Log.e("MediaUpload", "Upload error: " + error.getMessage(), error);
                                errorMessage += ": " + error.getMessage();
                            } else if (response != null) {
                                Log.e("MediaUpload", "Upload failed with HTTP code: " + response.code() + ", message: " + response.message());
                                try {
                                    String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                                    Log.e("MediaUpload", "Error body: " + errorBody);
                                } catch (Exception e) {
                                    Log.e("MediaUpload", "Error reading error body", e);
                                }
                                errorMessage += " (HTTP " + response.code() + ")";
                            }
                            
                            removeMediaItem(mediaItem);
                            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                        
                        mediaUploadAdapter.submitList(new ArrayList<>(uploadedMediaItems));
                        
                        // Clean up temp file after upload attempt
                        if (finalTempFile != null && finalTempFile.exists()) {
                            Log.d("MediaUpload", "Cleaning up temp file: " + finalTempFile.getAbsolutePath());
                            finalTempFile.delete();
                        }
                    });
                });
                
            } catch (Exception e) {
                // Clean up temp file on exception
                if (tempFile != null && tempFile.exists()) {
                    Log.d("MediaUpload", "Cleaning up temp file due to exception: " + tempFile.getAbsolutePath());
                    tempFile.delete();
                }
                
                Log.e("MediaUpload", "Exception during upload", e);
                runOnUiThread(() -> {
                    mediaItem.setUploading(false);
                    removeMediaItem(mediaItem);
                    Toast.makeText(this, "Error uploading media: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    mediaUploadAdapter.submitList(new ArrayList<>(uploadedMediaItems));
                });
            }
        });
    }

    private File createTempFileFromUri(Uri uri, String fileName) throws Exception {
        Log.d("MediaUpload", "Creating temp file from URI: " + uri);
        InputStream inputStream = getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new Exception("Cannot open input stream for URI: " + uri);
        }

        // Create temp file
        String mimeType = getContentResolver().getType(uri);
        String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        if (extension == null) {
            // Fallback to extract extension from filename if available
            String uriPath = uri.getPath();
            if (uriPath != null && uriPath.contains(".")) {
                extension = uriPath.substring(uriPath.lastIndexOf(".") + 1);
            } else {
                extension = "tmp";
            }
        }
        
        Log.d("MediaUpload", "File extension determined: " + extension + " (from MIME type: " + mimeType + ")");
        
        File tempFile = File.createTempFile("upload_", "." + extension, getCacheDir());
        Log.d("MediaUpload", "Temp file created at: " + tempFile.getAbsolutePath());
        
        FileOutputStream outputStream = new FileOutputStream(tempFile);
        byte[] buffer = new byte[8192]; // Increased buffer size for better performance
        int length;
        long totalBytes = 0;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
            totalBytes += length;
        }
        
        inputStream.close();
        outputStream.close();
        
        Log.d("MediaUpload", "File copied successfully. Size: " + totalBytes + " bytes, Final file size: " + tempFile.length() + " bytes");
        
        if (tempFile.length() == 0) {
            throw new Exception("Temp file is empty after copying from URI");
        }
        
        return tempFile;
    }

    private void removeMediaItem(MediaUploadAdapter.MediaItem mediaItem) {
        uploadedMediaItems.remove(mediaItem);
        if (mediaItem.getFilePath() != null) {
            uploadedFilePaths.remove(mediaItem.getFilePath());
        }
        mediaUploadAdapter.submitList(new ArrayList<>(uploadedMediaItems));
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

    private void loadExistingMediaFiles(List<com.dev.thecodecup.model.network.api.ReviewMediaFile> mediaFiles) {
        for (com.dev.thecodecup.model.network.api.ReviewMediaFile mediaFile : mediaFiles) {
            try {
                // Convert URL to assets path for display
                String convertedUrl = convertToAssetsPath(mediaFile.getUrl());
                
                Uri uri = Uri.parse(convertedUrl);
                MediaUploadAdapter.MediaItem mediaItem = new MediaUploadAdapter.MediaItem(uri, mediaFile.getType(), mediaFile.getName());
                mediaItem.setFilePath(mediaFile.getUrl());    // Keep original for API
                mediaItem.setFileUrl(convertedUrl);           // Use converted for display
                uploadedMediaItems.add(mediaItem);
                uploadedFilePaths.add(mediaFile.getUrl());    // Keep original for API
            } catch (Exception e) {
                Log.e("ReviewDialog", "Error loading existing media: " + e.getMessage());
            }
        }
        
        if (mediaUploadAdapter != null) {
            mediaUploadAdapter.submitList(new ArrayList<>(uploadedMediaItems));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (uploadExecutor != null) {
            uploadExecutor.shutdown();
        }
    }

    private void deleteReview(String orderId, int position, DialogInterface dialog) {
        ProgressDialog progress = ProgressDialog.show(this, "", "Deleting...", true);
        BakeryJavaBridge.INSTANCE.deleteReview(this, orderId, (response, error) -> {
            progress.dismiss();
            if (response != null && response.isSuccessful()) {
                Toast.makeText(this, "Review deleted", Toast.LENGTH_SHORT).show();
                updateOrderInList(position, 0); // Reset rate to 0 to show "Review" button again
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Failed to delete review", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateOrderInList(int position, int newRating) {
        if (position >= 0 && position < filteredOrders.size()) {
            Order order = filteredOrders.get(position);
            // Re-create the object to ensure the list item is re-bound with new data
            Order updatedOrder = new Order(order.getOrder_id(), order.getOrder_number(), order.getDate_created(), order.getHost_id(), order.getPayment_method(), order.getPayment_status(), order.getReceiver_name(), order.getReceiver_address(), order.getReceiver_phone(), order.getOrder_status(), order.getStatus(), order.getCount_product(), order.getOrder_total(), order.getTotal_price(), order.getOrder_date(), newRating, order.getFeedback(), order.getNote(), order.getCreated_at(), order.getSource(), order.getOrder_detail());
            filteredOrders.set(position, updatedOrder);
            adapter.notifyItemChanged(position);
        }
    }

    @Override
    public void onPayNowClick(Order order) {
        // Handle pay now button click - create payment link and open payment screen
        if (order == null || order.getOrder_id() == null) {
            Toast.makeText(this, "Invalid order", Toast.LENGTH_SHORT).show();
            return;
        }

        createPaymentLink(order.getOrder_id());
    }

    private void createPaymentLink(String orderId) {
        final ProgressDialog dialog = ProgressDialog.show(this, null,
                "Creating payment link...", true, false);

        BakeryJavaBridge.INSTANCE.createPaymentLink(this, orderId, new PaymentLinkCallback() {
            @Override
            public void onResult(Response<PaymentLinkResponse> response, Throwable error) {
                dialog.dismiss();

                if (error != null) {
                    Log.e("MyOrdersActivity", "Payment link error", error);
                    Toast.makeText(MyOrdersActivity.this,
                            "Payment link error: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (response != null && response.isSuccessful() && response.body() != null) {
                    PaymentLinkResponse paymentResponse = response.body();

                    if (paymentResponse.getError() == 0) {
                        Log.d("MyOrdersActivity", "Payment link created - URL: " + paymentResponse.getCheckoutUrl());
                        Log.d("MyOrdersActivity", "QR Code present: " + (paymentResponse.getQrCode() != null));
                        if (paymentResponse.getQrCode() != null) {
                            Log.d("MyOrdersActivity", "QR Code length: " + paymentResponse.getQrCode().length());
                        }
                        
                        // Open payment screen
                        Intent intent = new Intent(MyOrdersActivity.this, PaymentActivity.class);
                        intent.putExtra("CHECKOUT_URL", paymentResponse.getCheckoutUrl());
                        intent.putExtra("QR_CODE", paymentResponse.getQrCode());
                        intent.putExtra("ORDER_ID", orderId);
                        startActivity(intent);
                    } else {
                        Toast.makeText(MyOrdersActivity.this,
                                "Payment error: " + paymentResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MyOrdersActivity.this,
                            "Failed to create payment link", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
