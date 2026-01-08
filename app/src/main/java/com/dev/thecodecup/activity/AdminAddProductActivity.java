package com.dev.thecodecup.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dev.thecodecup.R;
import com.dev.thecodecup.adapter.SelectedCategoryAdapter;
import com.dev.thecodecup.adapter.SelectedToppingAdapter;
import com.dev.thecodecup.adapter.DetailImageAdapter;
import com.dev.thecodecup.adapter.ToppingSelectionAdapter;
import com.dev.thecodecup.model.ToppingSelection;
import com.dev.thecodecup.model.network.ApiService;
import com.dev.thecodecup.utils.FileUtils;
import com.dev.thecodecup.model.network.NetworkModule;
import com.dev.thecodecup.model.network.dto.AdminProductDto;
import com.dev.thecodecup.model.network.dto.AdminProductsResponseDto;
import com.dev.thecodecup.model.network.dto.AdminToppingCategoryDto;
import com.dev.thecodecup.model.network.dto.CategoriesResponse;
import com.dev.thecodecup.model.network.dto.CategoryDto;
import com.dev.thecodecup.model.network.dto.ProductDetailResponseDto;
import com.dev.thecodecup.model.network.dto.ProductOperationResponseDto;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for adding or editing products
 * Can be launched in two modes:
 * 1. Add mode (no productId extra)
 * 2. Edit mode (with productId extra)
 */
public class AdminAddProductActivity extends AppCompatActivity {

    private TextView tvTitle;
    private EditText etProductName, etProductDescription, etCost, etPrice;
    private EditText etUpMPrice, etUpLPrice, etPriority;
    private RadioGroup rgStatus, rgIsTopping;
    private RadioButton rbActive, rbInactive, rbNotTopping, rbIsTopping;
    private Button btnSelectCategories, btnSelectToppings;
    private Button btnSelectThumbnail, btnSelectDetailImages;
    private Button btnCancel, btnSave;
    private ImageView ivThumbnail;
    private RecyclerView rvSelectedCategories, rvSelectedToppings, rvDetailImages;

    private SelectedCategoryAdapter categoryAdapter;
    private SelectedToppingAdapter toppingAdapter;
    private DetailImageAdapter detailImageAdapter;

    private ApiService apiService;
    private List<CategoryDto> allCategories = new ArrayList<>();
    private List<AdminProductDto> allToppings = new ArrayList<>();
    private List<CategoryDto> selectedCategories = new ArrayList<>();
    private List<ToppingSelection> selectedToppings = new ArrayList<>();
    private List<Uri> detailImageUris = new ArrayList<>();
    private Uri thumbnailUri = null;

    private String productId = null; // null for add mode, non-null for edit mode
    private boolean isEditMode = false;
    
    // Topping category ID - this is the default category for toppings
    private static final String TOPPING_CATEGORY_ID = "2a1dbe25-b549-11ef-8332-70a6cc37ddf9";

    private ActivityResultLauncher<Intent> thumbnailPicker;
    private ActivityResultLauncher<Intent> detailImagesPicker;
    
    // Views that should be hidden when "Is Topping" is selected
    private View categoriesSection;
    private View toppingsSection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_product);

        apiService = NetworkModule.INSTANCE.getApiService();

        // Check if edit mode
        productId = getIntent().getStringExtra("productId");
        isEditMode = productId != null;

        initViews();
        setupRecyclerViews();
        setupImagePickers();
        setupClickListeners();
        
        // Load categories first, then product details if in edit mode
        loadCategories();

        if (isEditMode) {
            tvTitle.setText("Edit Product");
            btnSave.setText("Update Product");
            // Product details will be loaded after categories are loaded
        }
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        ImageButton btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
        etProductName = findViewById(R.id.etProductName);
        etProductDescription = findViewById(R.id.etProductDescription);
        etCost = findViewById(R.id.etCost);
        etPrice = findViewById(R.id.etPrice);
        etUpMPrice = findViewById(R.id.etUpMPrice);
        etUpLPrice = findViewById(R.id.etUpLPrice);
        etPriority = findViewById(R.id.etPriority);
        rgStatus = findViewById(R.id.rgStatus);
        rgIsTopping = findViewById(R.id.rgIsTopping);
        rbActive = findViewById(R.id.rbActive);
        rbInactive = findViewById(R.id.rbInactive);
        rbNotTopping = findViewById(R.id.rbNotTopping);
        rbIsTopping = findViewById(R.id.rbIsTopping);
        btnSelectCategories = findViewById(R.id.btnSelectCategories);
        btnSelectToppings = findViewById(R.id.btnSelectToppings);
        btnSelectThumbnail = findViewById(R.id.btnSelectThumbnail);
        btnSelectDetailImages = findViewById(R.id.btnSelectDetailImages);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);
        ivThumbnail = findViewById(R.id.ivThumbnail);
        rvSelectedCategories = findViewById(R.id.rvSelectedCategories);
        rvSelectedToppings = findViewById(R.id.rvSelectedToppings);
        rvDetailImages = findViewById(R.id.rvDetailImages);
        
        // Section containers
        categoriesSection = findViewById(R.id.categoriesSection);
        toppingsSection = findViewById(R.id.toppingsSection);
        
        // Setup radio group listener to toggle UI
        rgIsTopping.setOnCheckedChangeListener((group, checkedId) -> {
            updateUIBasedOnProductType();
        });
    }
    
    private void updateUIBasedOnProductType() {
        boolean isTopping = rbIsTopping.isChecked();
        
        if (isTopping) {
            // Hide categories and toppings sections when this is a topping
            categoriesSection.setVisibility(View.GONE);
            toppingsSection.setVisibility(View.GONE);
            
            // Auto-select topping category
            selectedCategories.clear();
            // Find and add the topping category
            for (CategoryDto cat : allCategories) {
                if (TOPPING_CATEGORY_ID.equals(cat.getCategoryId())) {
                    selectedCategories.add(cat);
                    break;
                }
            }
            categoryAdapter.notifyDataSetChanged();
            
            // Clear any selected toppings
            selectedToppings.clear();
            toppingAdapter.notifyDataSetChanged();
        } else {
            // Show categories and toppings sections for regular products
            categoriesSection.setVisibility(View.VISIBLE);
            toppingsSection.setVisibility(View.VISIBLE);
            
            // Remove topping category if it was auto-selected
            CategoryDto toppingCat = null;
            for (CategoryDto cat : selectedCategories) {
                if (TOPPING_CATEGORY_ID.equals(cat.getCategoryId())) {
                    toppingCat = cat;
                    break;
                }
            }
            if (toppingCat != null) {
                selectedCategories.remove(toppingCat);
                categoryAdapter.notifyDataSetChanged();
            }
        }
    }

    private void setupRecyclerViews() {
        // Categories
        categoryAdapter = new SelectedCategoryAdapter(selectedCategories, new SelectedCategoryAdapter.OnRemoveListener() {
            @Override
            public void onRemove(CategoryDto category) {
                selectedCategories.remove(category);
                categoryAdapter.notifyDataSetChanged();
            }
        });
        rvSelectedCategories.setLayoutManager(new LinearLayoutManager(this));
        rvSelectedCategories.setAdapter(categoryAdapter);

        // Toppings
        toppingAdapter = new SelectedToppingAdapter(selectedToppings, new SelectedToppingAdapter.OnRemoveListener() {
            @Override
            public void onRemove(ToppingSelection topping) {
                selectedToppings.remove(topping);
                toppingAdapter.notifyDataSetChanged();
            }
        });
        rvSelectedToppings.setLayoutManager(new LinearLayoutManager(this));
        rvSelectedToppings.setAdapter(toppingAdapter);

        // Detail Images
        detailImageAdapter = new DetailImageAdapter(detailImageUris, new DetailImageAdapter.OnRemoveListener() {
            @Override
            public void onRemove(Uri uri) {
                detailImageUris.remove(uri);
                detailImageAdapter.notifyDataSetChanged();
            }
        });
        rvDetailImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvDetailImages.setAdapter(detailImageAdapter);
    }

    private void setupImagePickers() {
        thumbnailPicker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new androidx.activity.result.ActivityResultCallback<androidx.activity.result.ActivityResult>() {
                @Override
                public void onActivityResult(androidx.activity.result.ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        thumbnailUri = result.getData().getData();
                        Glide.with(AdminAddProductActivity.this).load(thumbnailUri).into(ivThumbnail);
                    }
                }
            }
        );

        detailImagesPicker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new androidx.activity.result.ActivityResultCallback<androidx.activity.result.ActivityResult>() {
                @Override
                public void onActivityResult(androidx.activity.result.ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        if (result.getData().getClipData() != null) {
                            // Multiple images
                            int count = result.getData().getClipData().getItemCount();
                            for (int i = 0; i < count; i++) {
                                Uri imageUri = result.getData().getClipData().getItemAt(i).getUri();
                                detailImageUris.add(imageUri);
                            }
                        } else if (result.getData().getData() != null) {
                            // Single image
                            detailImageUris.add(result.getData().getData());
                        }
                        detailImageAdapter.notifyDataSetChanged();
                    }
                }
            }
        );
    }

    private void setupClickListeners() {
        btnSelectCategories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCategorySelectionDialog();
            }
        });
        btnSelectToppings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToppingSelectionDialog();
            }
        });
        btnSelectThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectThumbnailImage();
            }
        });
        btnSelectDetailImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDetailImages();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProduct();
            }
        });
    }

    private void loadCategories() {
        apiService.getAdminCategories().enqueue(new Callback<CategoriesResponse>() {
            @Override
            public void onResponse(Call<CategoriesResponse> call, Response<CategoriesResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    allCategories = response.body().getData();
                    // Load toppings after categories
                    loadToppings();
                }
            }

            @Override
            public void onFailure(Call<CategoriesResponse> call, Throwable t) {
                Toast.makeText(AdminAddProductActivity.this, "Failed to load categories: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadToppings() {
        // Load toppings from the products API
        apiService.getAdminProducts(null, null, null).enqueue(new Callback<AdminProductsResponseDto>() {
            @Override
            public void onResponse(Call<AdminProductsResponseDto> call, Response<AdminProductsResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Extract toppings from topping_data
                    if (response.body().getToppingData() != null && !response.body().getToppingData().isEmpty()) {
                        allToppings.clear();
                        for (AdminToppingCategoryDto toppingCategory : response.body().getToppingData()) {
                            if (toppingCategory.getToppingList() != null) {
                                allToppings.addAll(toppingCategory.getToppingList());
                            }
                        }
                    }
                    
                    // If in edit mode, load product details after toppings are loaded
                    if (isEditMode) {
                        loadProductDetail();
                    }
                }
            }

            @Override
            public void onFailure(Call<AdminProductsResponseDto> call, Throwable t) {
                Toast.makeText(AdminAddProductActivity.this, "Failed to load toppings: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                // Still try to load product details if in edit mode
                if (isEditMode) {
                    loadProductDetail();
                }
            }
        });
    }

    private void loadProductDetail() {
        apiService.getAdminProductDetail(productId).enqueue(new Callback<ProductDetailResponseDto>() {
            @Override
            public void onResponse(Call<ProductDetailResponseDto> call, Response<ProductDetailResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    populateProductData(response.body());
                } else {
                    Toast.makeText(AdminAddProductActivity.this, "Failed to load product", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductDetailResponseDto> call, Throwable t) {
                Toast.makeText(AdminAddProductActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateProductData(ProductDetailResponseDto response) {
        var data = response.getData();
        
        etProductName.setText(data.getName());
        etProductDescription.setText(data.getDescription());
        etCost.setText(data.getCost());
        etPrice.setText(data.getPrice());
        etUpMPrice.setText(data.getUpMPrice());
        etUpLPrice.setText(data.getUpLPrice());
        etPriority.setText(String.valueOf(data.getPriority()));

        if ("active".equals(data.getStatus())) {
            rbActive.setChecked(true);
        } else {
            rbInactive.setChecked(true);
        }

        if (data.isTopping() == 1) {
            rbIsTopping.setChecked(true);
        } else {
            rbNotTopping.setChecked(true);
        }

        // Load thumbnail
        if (data.getThumbnailImage() != null && !data.getThumbnailImage().isEmpty()) {
            Glide.with(this)
                .load(data.getThumbnailImage())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(ivThumbnail);
        }

        // Load selected categories
        if (data.getCategoriesId() != null && !data.getCategoriesId().isEmpty()) {
            selectedCategories.clear();
            for (String catId : data.getCategoriesId()) {
                for (CategoryDto cat : allCategories) {
                    if (cat.getCategoryId().equals(catId)) {
                        selectedCategories.add(cat);
                        break;
                    }
                }
            }
            categoryAdapter.notifyDataSetChanged();
        }

        // Load toppings with names from allToppings list
        if (data.getToppingsId() != null && !data.getToppingsId().isEmpty()) {
            selectedToppings.clear();
            for (var topping : data.getToppingsId()) {
                // Find topping name from allToppings
                String toppingName = null;
                for (AdminProductDto availableTopping : allToppings) {
                    if (availableTopping.getProductId() != null && 
                        availableTopping.getProductId().equals(topping.getToppingId())) {
                        toppingName = availableTopping.getProductName();
                        break;
                    }
                }
                // Fallback if topping not found
                if (toppingName == null) {
                    toppingName = "Topping " + topping.getToppingId().substring(0, Math.min(8, topping.getToppingId().length()));
                }
                selectedToppings.add(new ToppingSelection(topping.getToppingId(), toppingName, topping.getExtraPrice()));
            }
            toppingAdapter.notifyDataSetChanged();
        }

        // Load detail images - Note: These are URLs, not URIs
        // We'll need to handle this differently since DetailImageAdapter expects URIs
        // For now, we'll just show a message that existing images are loaded
        if (data.getProductDetailImages() != null && !data.getProductDetailImages().isEmpty()) {
            Toast.makeText(this, 
                "Product has " + data.getProductDetailImages().size() + " existing images. Add new images to replace them.", 
                Toast.LENGTH_LONG).show();
        }
        
        // Update UI based on product type (topping or regular product)
        updateUIBasedOnProductType();
    }

    private void showCategorySelectionDialog() {
        if (allCategories.isEmpty()) {
            Toast.makeText(this, "Loading categories...", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] categoryNames = new String[allCategories.size()];
        boolean[] checkedItems = new boolean[allCategories.size()];

        for (int i = 0; i < allCategories.size(); i++) {
            categoryNames[i] = allCategories.get(i).getCategoryName();
            boolean isSelected = false;
            for (CategoryDto selected : selectedCategories) {
                if (selected.getCategoryId().equals(allCategories.get(i).getCategoryId())) {
                    isSelected = true;
                    break;
                }
            }
            checkedItems[i] = isSelected;
        }

        new AlertDialog.Builder(this)
            .setTitle("Select Categories")
            .setMultiChoiceItems(categoryNames, checkedItems, new android.content.DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which, boolean isChecked) {
                    CategoryDto category = allCategories.get(which);
                    if (isChecked) {
                        boolean alreadySelected = false;
                        for (CategoryDto selected : selectedCategories) {
                            if (selected.getCategoryId().equals(category.getCategoryId())) {
                                alreadySelected = true;
                                break;
                            }
                        }
                        if (!alreadySelected) {
                            selectedCategories.add(category);
                        }
                    } else {
                        // Remove category
                        CategoryDto toRemove = null;
                        for (CategoryDto selected : selectedCategories) {
                            if (selected.getCategoryId().equals(category.getCategoryId())) {
                                toRemove = selected;
                                break;
                            }
                        }
                        if (toRemove != null) {
                            selectedCategories.remove(toRemove);
                        }
                    }
                }
            })
            .setPositiveButton("OK", new android.content.DialogInterface.OnClickListener() {
                @Override
                public void onClick(android.content.DialogInterface dialog, int which) {
                    categoryAdapter.notifyDataSetChanged();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showToppingSelectionDialog() {
        if (allToppings.isEmpty()) {
            Toast.makeText(this, "Loading toppings...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create dialog with RecyclerView
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_topping_selection, null);
        RecyclerView rvToppings = dialogView.findViewById(R.id.rvToppings);
        
        rvToppings.setLayoutManager(new LinearLayoutManager(this));
        ToppingSelectionAdapter adapter = new ToppingSelectionAdapter(allToppings, selectedToppings);
        rvToppings.setAdapter(adapter);
        
        new AlertDialog.Builder(this)
            .setTitle("Select Toppings")
            .setView(dialogView)
            .setPositiveButton("OK", (dialog, which) -> {
                // Update selected toppings
                selectedToppings.clear();
                selectedToppings.addAll(adapter.getSelectedToppings());
                toppingAdapter.notifyDataSetChanged();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void selectThumbnailImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        thumbnailPicker.launch(intent);
    }

    private void selectDetailImages() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        detailImagesPicker.launch(intent);
    }

    private void saveProduct() {
        // Validation
        String name = etProductName.getText().toString().trim();
        String price = etPrice.getText().toString().trim();
        String cost = etCost.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter product name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(price) || TextUtils.isEmpty(cost)) {
            Toast.makeText(this, "Please enter price and cost", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCategories.isEmpty()) {
            Toast.makeText(this, "Please select at least one category", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare request data
        RequestBody namePart = RequestBody.create(MediaType.parse("text/plain"), name);
        RequestBody descPart = null;
        String description = etProductDescription.getText().toString().trim();
        if (!TextUtils.isEmpty(description)) {
            descPart = RequestBody.create(MediaType.parse("text/plain"), description);
        }

        String status = rbActive.isChecked() ? "active" : "inactive";
        RequestBody statusPart = RequestBody.create(MediaType.parse("text/plain"), status);
        RequestBody pricePart = RequestBody.create(MediaType.parse("text/plain"), price);
        RequestBody costPart = RequestBody.create(MediaType.parse("text/plain"), cost);
        
        String upMPrice = etUpMPrice.getText().toString().trim();
        String upLPrice = etUpLPrice.getText().toString().trim();
        RequestBody upMPricePart = RequestBody.create(MediaType.parse("text/plain"), TextUtils.isEmpty(upMPrice) ? "0" : upMPrice);
        RequestBody upLPricePart = RequestBody.create(MediaType.parse("text/plain"), TextUtils.isEmpty(upLPrice) ? "0" : upLPrice);
        
        int isTopping = rbIsTopping.isChecked() ? 1 : 0;
        RequestBody isToppingPart = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(isTopping));
        
        String priority = etPriority.getText().toString().trim();
        RequestBody priorityPart = RequestBody.create(MediaType.parse("text/plain"), TextUtils.isEmpty(priority) ? "0" : priority);

        // Categories
        List<RequestBody> categoryParts = new ArrayList<>();
        for (CategoryDto cat : selectedCategories) {
            categoryParts.add(RequestBody.create(MediaType.parse("text/plain"), cat.getCategoryId()));
        }

        // Toppings
        List<RequestBody> toppingParts = null;
        if (!selectedToppings.isEmpty()) {
            toppingParts = new ArrayList<>();
            for (ToppingSelection topping : selectedToppings) {
                String json = String.format("{\"toppingId\":{\"topping_id\":\"%s\",\"extra_price\":\"%s\"}}",
                        topping.toppingId, topping.extraPrice);
                toppingParts.add(RequestBody.create(MediaType.parse("text/plain"), json));
            }
        }

        // Thumbnail image
        MultipartBody.Part thumbnailPart = null;
        if (thumbnailUri != null) {
            File thumbnailFile = FileUtils.getFileFromUri(this, thumbnailUri);
            if (thumbnailFile != null) {
                RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), thumbnailFile);
                thumbnailPart = MultipartBody.Part.createFormData("thumbnailImage", thumbnailFile.getName(), reqFile);
            }
        }

        // Detail images
        List<MultipartBody.Part> detailImageParts = null;
        if (!detailImageUris.isEmpty()) {
            detailImageParts = new ArrayList<>();
            for (Uri uri : detailImageUris) {
                File file = FileUtils.getFileFromUri(this, uri);
                if (file != null) {
                    RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
                    detailImageParts.add(MultipartBody.Part.createFormData("productDetailImages[]", file.getName(), reqFile));
                }
            }
        }

        // Make API call
        if (isEditMode) {
            updateProduct(namePart, descPart, statusPart, pricePart, costPart, upMPricePart, upLPricePart,
                    isToppingPart, priorityPart, thumbnailPart, categoryParts, toppingParts, detailImageParts);
        } else {
            addProduct(namePart, descPart, statusPart, pricePart, costPart, upMPricePart, upLPricePart,
                    isToppingPart, priorityPart, thumbnailPart, categoryParts, toppingParts, detailImageParts);
        }
    }

    private void addProduct(RequestBody name, RequestBody description, RequestBody status,
                           RequestBody price, RequestBody cost, RequestBody upMPrice, RequestBody upLPrice,
                           RequestBody isTopping, RequestBody priority, MultipartBody.Part thumbnail,
                           List<RequestBody> categories, List<RequestBody> toppings, List<MultipartBody.Part> detailImages) {
        
        apiService.addAdminProduct(name, description, status, price, cost, upMPrice, upLPrice, isTopping,
                priority, thumbnail, categories, toppings, detailImages)
                .enqueue(new Callback<ProductOperationResponseDto>() {
                    @Override
                    public void onResponse(Call<ProductOperationResponseDto> call, Response<ProductOperationResponseDto> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AdminAddProductActivity.this, "Product added successfully", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(AdminAddProductActivity.this, "Failed to add product", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ProductOperationResponseDto> call, Throwable t) {
                        Toast.makeText(AdminAddProductActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateProduct(RequestBody name, RequestBody description, RequestBody status,
                              RequestBody price, RequestBody cost, RequestBody upMPrice, RequestBody upLPrice,
                              RequestBody isTopping, RequestBody priority, MultipartBody.Part thumbnail,
                              List<RequestBody> categories, List<RequestBody> toppings, List<MultipartBody.Part> detailImages) {
        
        apiService.updateAdminProductMultipart(productId, name, description, status, price, cost, upMPrice, upLPrice,
                isTopping, priority, thumbnail, categories, toppings, detailImages)
                .enqueue(new Callback<ProductOperationResponseDto>() {
                    @Override
                    public void onResponse(Call<ProductOperationResponseDto> call, Response<ProductOperationResponseDto> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AdminAddProductActivity.this, "Product updated successfully", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(AdminAddProductActivity.this, "Failed to update product", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ProductOperationResponseDto> call, Throwable t) {
                        Toast.makeText(AdminAddProductActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
