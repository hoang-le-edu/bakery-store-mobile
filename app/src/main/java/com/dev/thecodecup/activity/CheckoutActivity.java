package com.dev.thecodecup.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dev.thecodecup.R;
import com.dev.thecodecup.model.network.api.BakeryJavaBridge;
import com.dev.thecodecup.model.network.api.CheckoutCallback;
import com.dev.thecodecup.model.network.api.CheckoutRequest;
import com.dev.thecodecup.model.network.api.PaymentLinkCallback;
import com.dev.thecodecup.model.network.api.PaymentLinkResponse;
import com.dev.thecodecup.model.network.api.SuccessResponse;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText edtReceiverName;
    private TextInputEditText edtReceiverPhone;
    private TextInputEditText edtStreetAddress;
    private Spinner spinnerProvince;
    private Spinner spinnerDistrict;
    private Spinner spinnerWard;
    private RadioGroup radioGroupPayment;
    private TextView txtOrderTotal;
    private TextView txtShippingFee;
    private TextView txtFinalTotal;
    private MaterialButton btnPlaceOrder;

    private ArrayList<String> selectedOrderDetailIds;
    private int orderTotal = 0;
    private int shippingFee = 30000;

    // Address data
    private List<Province> provinces = new ArrayList<>();
    private List<District> districts = new ArrayList<>();
    private List<Ward> wards = new ArrayList<>();

    private String selectedProvinceId = "";
    private String selectedDistrictId = "";
    private String selectedWardCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Get data from intent
        selectedOrderDetailIds = getIntent().getStringArrayListExtra("ORDER_DETAIL_IDS");
        orderTotal = getIntent().getIntExtra("ORDER_TOTAL", 0);

        if (selectedOrderDetailIds == null || selectedOrderDetailIds.isEmpty()) {
            Toast.makeText(this, "No products to checkout", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadAddressData();
        updatePrices();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        edtReceiverName = findViewById(R.id.edtReceiverName);
        edtReceiverPhone = findViewById(R.id.edtReceiverPhone);
        edtStreetAddress = findViewById(R.id.edtStreetAddress);
        spinnerProvince = findViewById(R.id.spinnerProvince);
        spinnerDistrict = findViewById(R.id.spinnerDistrict);
        spinnerWard = findViewById(R.id.spinnerWard);
        radioGroupPayment = findViewById(R.id.radioGroupPayment);
        txtOrderTotal = findViewById(R.id.txtOrderTotal);
        txtShippingFee = findViewById(R.id.txtShippingFee);
        txtFinalTotal = findViewById(R.id.txtFinalTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());

        btnPlaceOrder.setOnClickListener(v -> placeOrder());

        spinnerProvince.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position,
                    long id) {
                if (position > 0 && position <= provinces.size()) {
                    Province province = provinces.get(position - 1);
                    selectedProvinceId = province.id;
                    loadDistricts(province.id);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        spinnerDistrict.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position,
                    long id) {
                if (position > 0 && position <= districts.size()) {
                    District district = districts.get(position - 1);
                    selectedDistrictId = district.id;
                    loadWards(district.id);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        spinnerWard.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position,
                    long id) {
                if (position > 0 && position <= wards.size()) {
                    Ward ward = wards.get(position - 1);
                    selectedWardCode = ward.code;
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void updatePrices() {
        txtOrderTotal.setText(formatPrice(orderTotal) + "₫");
        txtShippingFee.setText(formatPrice(shippingFee) + "₫");
        int finalTotal = orderTotal + shippingFee;
        txtFinalTotal.setText(formatPrice(finalTotal) + "₫");
    }

    private void loadAddressData() {
        try {
            InputStream is = getAssets().open("tinh_tp.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            JSONArray jsonArray = new JSONArray(json);

            provinces.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                provinces.add(new Province(
                        obj.getString("code"),
                        obj.getString("name")));
            }

            List<String> provinceNames = new ArrayList<>();
            provinceNames.add("-- Select Province/City --");
            for (Province p : provinces) {
                provinceNames.add(p.name);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, provinceNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerProvince.setAdapter(adapter);

        } catch (Exception e) {
            Log.e("CheckoutActivity", "Error loading provinces", e);
            Toast.makeText(this, "Error loading province list", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDistricts(String provinceId) {
        try {
            InputStream is = getAssets().open("quan_huyen.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            JSONArray jsonArray = new JSONArray(json);

            districts.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                if (obj.getString("parent_code").equals(provinceId)) {
                    districts.add(new District(
                            obj.getString("code"),
                            obj.getString("name"),
                            obj.getString("parent_code")));
                }
            }

            List<String> districtNames = new ArrayList<>();
            districtNames.add("-- Select District --");
            for (District d : districts) {
                districtNames.add(d.name);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, districtNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDistrict.setAdapter(adapter);
            spinnerDistrict.setSelection(0);

            // Reset ward spinner
            wards.clear();
            List<String> emptyWards = new ArrayList<>();
            emptyWards.add("-- Select Ward --");
            ArrayAdapter<String> wardAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, emptyWards);
            wardAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerWard.setAdapter(wardAdapter);

        } catch (Exception e) {
            Log.e("CheckoutActivity", "Error loading districts", e);
        }
    }

    private void loadWards(String districtId) {
        try {
            InputStream is = getAssets().open("xa_phuong.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            JSONArray jsonArray = new JSONArray(json);

            wards.clear();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                if (obj.getString("parent_code").equals(districtId)) {
                    wards.add(new Ward(
                            obj.getString("code"),
                            obj.getString("name"),
                            obj.getString("parent_code")));
                }
            }

            List<String> wardNames = new ArrayList<>();
            wardNames.add("-- Select Ward --");
            for (Ward w : wards) {
                wardNames.add(w.name);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, wardNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerWard.setAdapter(adapter);
            spinnerWard.setSelection(0);

        } catch (Exception e) {
            Log.e("CheckoutActivity", "Error loading wards", e);
        }
    }

    private void placeOrder() {
        // Validate inputs
        String receiverName = edtReceiverName.getText().toString().trim();
        String receiverPhone = edtReceiverPhone.getText().toString().trim();
        String streetAddress = edtStreetAddress.getText().toString().trim();

        if (receiverName.isEmpty()) {
            Toast.makeText(this, "Please enter receiver name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (receiverPhone.isEmpty()) {
            Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedProvinceId.isEmpty() || selectedDistrictId.isEmpty() || selectedWardCode.isEmpty()) {
            Toast.makeText(this, "Please select complete address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (streetAddress.isEmpty()) {
            Toast.makeText(this, "Please enter street address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get payment method
        int checkedId = radioGroupPayment.getCheckedRadioButtonId();
        String paymentMethod = (checkedId == R.id.radioCOD) ? "Cash" : "Banking";

        // Build full address
        String fullAddress = streetAddress + ", " + getSelectedWardName() + ", " +
                getSelectedDistrictName() + ", " + getSelectedProvinceName();

        // Create checkout request
        CheckoutRequest request = new CheckoutRequest(
                selectedOrderDetailIds,
                receiverName,
                fullAddress,
                paymentMethod,
                "", // voucher
                "", // voucher_shipping
                "", // note
                selectedProvinceId,
                selectedDistrictId,
                selectedWardCode,
                streetAddress,
                receiverPhone,
                shippingFee,
                0 // discount_number
        );

        final ProgressDialog dialog = ProgressDialog.show(this, null, "Placing order...", true, false);

        BakeryJavaBridge.INSTANCE.proceedCheckout(this, request, new CheckoutCallback() {
            @Override
            public void onResult(Response<SuccessResponse> response, Throwable error) {
                dialog.dismiss();

                if (error != null) {
                    Log.e("CheckoutActivity", "Checkout error", error);
                    Toast.makeText(CheckoutActivity.this,
                            "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }

                if (response != null && response.isSuccessful() && response.body() != null) {
                    Toast.makeText(CheckoutActivity.this,
                            response.body().getMessage(), Toast.LENGTH_SHORT).show();

                    if ("Banking".equals(paymentMethod)) {
                        // TODO: Get order_id from response and create payment link
                        createPaymentLink("dummy-order-id");
                    } else {
                        // Cash payment - go to success screen
                        Toast.makeText(CheckoutActivity.this,
                                "Order placed successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(CheckoutActivity.this,
                            "Order failed: " + (response != null ? response.code() : "Unknown"),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createPaymentLink(String orderId) {
        final ProgressDialog dialog = ProgressDialog.show(this, null,
                "Creating payment link...", true, false);

        BakeryJavaBridge.INSTANCE.createPaymentLink(this, orderId, new PaymentLinkCallback() {
            @Override
            public void onResult(Response<PaymentLinkResponse> response, Throwable error) {
                dialog.dismiss();

                if (error != null) {
                    Log.e("CheckoutActivity", "Payment link error", error);
                    Toast.makeText(CheckoutActivity.this,
                            "Payment link error: " + error.getMessage(),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (response != null && response.isSuccessful() && response.body() != null) {
                    PaymentLinkResponse paymentResponse = response.body();

                    // Open payment screen
                    Intent intent = new Intent(CheckoutActivity.this, PaymentActivity.class);
                    intent.putExtra("CHECKOUT_URL", paymentResponse.getData().getCheckoutUrl());
                    intent.putExtra("QR_CODE_URL", paymentResponse.getData().getQrCode());
                    intent.putExtra("ORDER_ID", paymentResponse.getData().getOrder_id());
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CheckoutActivity.this,
                            "Failed to create payment link", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getSelectedProvinceName() {
        for (Province p : provinces) {
            if (p.id.equals(selectedProvinceId)) {
                return p.name;
            }
        }
        return "";
    }

    private String getSelectedDistrictName() {
        for (District d : districts) {
            if (d.id.equals(selectedDistrictId)) {
                return d.name;
            }
        }
        return "";
    }

    private String getSelectedWardName() {
        for (Ward w : wards) {
            if (w.code.equals(selectedWardCode)) {
                return w.name;
            }
        }
        return "";
    }

    private String formatPrice(int price) {
        return String.format("%,d", price).replace(",", ".");
    }

    // Inner classes for address data
    static class Province {
        String id;
        String name;

        Province(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    static class District {
        String id;
        String name;
        String parentCode;

        District(String id, String name, String parentCode) {
            this.id = id;
            this.name = name;
            this.parentCode = parentCode;
        }
    }

    static class Ward {
        String code;
        String name;
        String parentCode;

        Ward(String code, String name, String parentCode) {
            this.code = code;
            this.name = name;
            this.parentCode = parentCode;
        }
    }
}
