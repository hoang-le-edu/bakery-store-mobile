package com.dev.thecodecup.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dev.thecodecup.BuildConfig;
import com.dev.thecodecup.R;
import com.dev.thecodecup.model.auth.AuthManager;
import com.dev.thecodecup.model.network.NetworkModule;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText, phoneEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private TextView loginTextView;

    private FirebaseAuth firebaseAuth;
    private final OkHttpClient http = new OkHttpClient();
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register); // Sử dụng layout bạn vừa tạo

        // Khởi tạo Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // View Binding
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.regEmailEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        passwordEditText = findViewById(R.id.regPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        loginTextView = findViewById(R.id.loginTextView);

        // Xử lý sự kiện Đăng ký
        registerButton.setOnClickListener(v -> doRegister());

        // Xử lý sự kiện Quay lại Đăng nhập
        loginTextView.setOnClickListener(v -> finish()); // Đóng activity hiện tại để quay lại Login

    }

    private void doRegister() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String pass = passwordEditText.getText().toString().trim();
        String confirmPass = confirmPasswordEditText.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(confirmPass)) {
            Toast.makeText(this, "Confirm password does not match", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog dlg = ProgressDialog.show(this, null, "Registering...", true, false);

        // 1. Đăng ký tài khoản Firebase
        firebaseAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Cập nhật tên hiển thị (tùy chọn) - Vẫn giữ nguyên bất đồng bộ
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            user.updateProfile(profileUpdates).addOnCompleteListener(updateTask -> {
                                Log.d("Register", "Firebase profile updated or failed, continuing to get token.");
                            });

                            // SỬA LỖI: Lấy ID Token một cách bất đồng bộ
                            user.getIdToken(true).addOnCompleteListener(tokenTask -> {
                                if (tokenTask.isSuccessful()) {
                                    String idToken = tokenTask.getResult().getToken();
                                    // 2. Gọi API backend sau khi có ID Token
                                    registerBackendApi(dlg, user, name, phone, pass, idToken);
                                } else {
                                    // Lỗi lấy token
                                    uiFail(dlg, "Lấy ID Token thất bại: " + tokenTask.getException().getMessage());
                                    user.delete(); // Nên xóa user Firebase nếu không thể gọi backend
                                }
                            });
                        } else {
                            uiFail(dlg, "Đăng ký Firebase thành công nhưng không lấy được User.");
                        }
                    } else {
                        // Đăng ký Firebase thất bại
                        uiFail(dlg, "Đăng ký Firebase thất bại: " + task.getException().getMessage());
                    }
                });
    }

    // Cập nhật hàm này để nhận ID Token
    private void registerBackendApi(ProgressDialog dlg, FirebaseUser firebaseUser, String name, String phone, String password, String idToken) {
        new Thread(() -> {
            try {
                // idToken đã được truyền vào, KHÔNG cần gọi getResult().getToken() nữa

                // Sử dụng email từ firebaseUser vì nó là email đã được xác thực
                String email = firebaseUser.getEmail();

                String url = BuildConfig.API_BASE_URL + "api/auth/register";

                JSONObject body = new JSONObject();
                body.put("name", name);
                body.put("email", email);
                body.put("phone_number", phone);
                body.put("password", password);
                body.put("c_password", password);
                body.put("firebase_uid", firebaseUser.getUid());

                Request req = new Request.Builder()
                        .url(url)
                        // Gửi idToken để backend có thể xác minh (nếu cần)
                        .header("Authorization", "Bearer " + idToken)
                        .post(RequestBody.create(body.toString().getBytes(StandardCharsets.UTF_8), JSON))
                        .build();

                Response res = http.newCall(req).execute();
                String text = res.body() != null ? res.body().string() : "";

                if (res.isSuccessful()) {
                    JSONObject j = new JSONObject(text);
                    // Kiểm tra cấu trúc JSON trả về trước khi gọi getJSONObject
                    if (j.optJSONObject("data") != null) {
                        String apiToken = j.getJSONObject("data").getString("token");

                        // 3. Lưu token API và chuyển Activity
                        runOnUiThread(() -> {
                            AuthManager.INSTANCE.setTokens(apiToken, "", 3600L);
                            NetworkModule.INSTANCE.setTokenProvider(() ->
                                    AuthManager.INSTANCE.getValidIdTokenBlocking()
                            );

                            dlg.dismiss();
                            Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, ProductListActivity.class));
                            finish();
                        });
                    } else {
                        uiFail(dlg, "Đăng ký thất bại: Phản hồi API không có trường 'data' hợp lệ.");
                        firebaseUser.delete();
                    }
                } else {
                    // Lỗi từ backend API (Laravel)
                    String msg = parseApiError(text);
                    uiFail(dlg, "Đăng ký thất bại: " + msg);
                    firebaseUser.delete();
                }
            } catch (Exception e) {
                uiFail(dlg, "Lỗi kết nối hoặc xử lý dữ liệu: " + e.getMessage());
            }
        }).start();
    }

    private void uiFail(ProgressDialog dlg, String msg) {
        runOnUiThread(() -> {
            dlg.dismiss();
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        });
    }

    private String parseApiError(String body) {
        try {
            JSONObject j = new JSONObject(body);
            String message = j.optString("message", "Lỗi không xác định");
            return message;
        } catch (Exception ignored) {
            return "Đăng ký thất bại, không thể phân tích phản hồi.";
        }
    }
}