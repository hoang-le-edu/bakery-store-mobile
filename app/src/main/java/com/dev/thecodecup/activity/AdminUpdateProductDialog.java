package com.dev.thecodecup.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.dev.thecodecup.R;
import com.dev.thecodecup.model.network.dto.AdminProductDto;

public class AdminUpdateProductDialog extends Dialog {
    private final AdminProductDto product;
    private final OnProductUpdateListener listener;

    public interface OnProductUpdateListener {
        void onProductUpdate(AdminProductDto updatedProduct);
    }

    public AdminUpdateProductDialog(@NonNull Context context, AdminProductDto product, OnProductUpdateListener listener) {
        super(context);
        this.product = product;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_update_product, null);
        setContentView(view);

        EditText etName = view.findViewById(R.id.etProductName);
        EditText etDescription = view.findViewById(R.id.etProductDescription);
        EditText etPrice = view.findViewById(R.id.etProductPrice);
        Button btnUpdate = view.findViewById(R.id.btnSaveProduct);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        if (product != null) {
            etName.setText(product.getProductName());
            etDescription.setText(product.getProductDescription());
            etPrice.setText(product.getProductPrice());
        }

        btnUpdate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String price = etPrice.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(price)) {
                Toast.makeText(getContext(), getContext().getString(R.string.please_enter_name_price), Toast.LENGTH_SHORT).show();
                return;
            }

            AdminProductDto updated = new AdminProductDto(
                product.getProductId(),
                name,
                description,
                price,
                product.getAvgRating(),
                product.getReviewCount(),
                product.getProductImageUrl()
            );
            if (listener != null) listener.onProductUpdate(updated);
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }
}
