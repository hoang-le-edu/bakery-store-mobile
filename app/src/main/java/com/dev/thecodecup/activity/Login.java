// app/src/main/java/com/dev/thecodecup/activity/Login.java
package com.dev.thecodecup.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dev.thecodecup.BuildConfig;
import com.dev.thecodecup.R;
import com.dev.thecodecup.model.auth.AuthManager; // Kotlin object ở bước trước
import com.dev.thecodecup.model.network.NetworkModule; // chỉ để đảm bảo đã init trước khi call API
import com.dev.thecodecup.BuildConfig;   // ← CHÍNH XÁC, trùng với namespace
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import kotlin.jvm.functions.Function0;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Đăng nhập Firebase (Identity Toolkit - VerifyPassword)
 * -> Lưu idToken/refreshToken/expiresIn vào AuthManager
 * -> Chuyển sang ProductListActivity.
 */
public class Login extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;

    private final OkHttpClient http = new OkHttpClient();
    private final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login); // layout bạn đã gửi
        // view binding (khớp id trong login.xml)
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> doLogin());
    }

//    private void doLogin() {
//        String email = String.valueOf(emailEditText.getText()).trim();
//        String pass  = String.valueOf(passwordEditText.getText()).trim();
//
//        if (email.isEmpty() || pass.isEmpty()) {
//            Toast.makeText(this, "Nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        ProgressDialog dlg = ProgressDialog.show(this, null, "Đang đăng nhập…", true, false);
//
//        new Thread(() -> {
//            try {
//                // Endpoint VerifyPassword (REST)
//                // https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=API_KEY
//                String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key="
//                        + BuildConfig.FIREBASE_WEB_API_KEY;
//
//                JSONObject body = new JSONObject();
//                body.put("email", email);
//                body.put("password", pass);
//                body.put("returnSecureToken", true);
//
//                Request req = new Request.Builder()
//                        .url(url)
//                        .post(RequestBody.create(body.toString().getBytes(StandardCharsets.UTF_8), JSON))
//                        .build();
//
//                Response res = http.newCall(req).execute();
//                String text = res.body() != null ? res.body().string() : "";
//
//                if (res.isSuccessful()) {
//                    JSONObject j = new JSONObject(text);
//                    String idToken = j.optString("idToken", "");
//                    String refreshToken = j.optString("refreshToken", "");
//                    long expiresIn = parseLongSafe(j.optString("expiresIn", "3600"), 3600L);
//
//                    if (idToken.isEmpty()) {
//                        uiFail(dlg, "Không nhận được idToken.");
//                        return;
//                    }
//
//                    // Lưu token để NetworkModule interceptor tự gắn Authorization
//                    runOnUiThread(() -> {
//                        AuthManager.INSTANCE.setTokens(idToken, refreshToken, expiresIn);
//                        NetworkModule.INSTANCE.setTokenProvider(new Function0<String>() {
//                            @Override
//                            public String invoke() {
//                                return AuthManager.INSTANCE.getValidIdTokenBlocking();
//                            }
//                        });
//                        dlg.dismiss();
//                        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
//                        startActivity(new Intent(this, ProductListActivity.class));
//                        finish();
//                    });
//                } else {
//                    // lỗi từ Firebase
//                    String msg = parseFirebaseError(text);
//                    uiFail(dlg, msg);
//                }
//            } catch (IOException | JSONException e) {
//                uiFail(dlg, "Lỗi kết nối: " + e.getMessage());
//            }
//        }).start();
//    }

    private void doLogin() {
        String email = emailEditText.getText().toString().trim();
        String pass = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Nhập email và mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog dlg = ProgressDialog.show(
                this, null, "Đang đăng nhập...", true, false);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    dlg.dismiss();

                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user == null) {
                            uiFail(dlg, "Không nhận được user từ Firebase");
                            return;
                        }

                        user.getIdToken(true).addOnCompleteListener(tokenTask -> {
                            if (tokenTask.isSuccessful()) {
                                String idToken = tokenTask.getResult().getToken();

                                // Lưu token vào AuthManager + interceptor như cũ
                                AuthManager.INSTANCE.setTokens(idToken, "", 3600L);
                                NetworkModule.INSTANCE.setTokenProvider(() ->
                                        AuthManager.INSTANCE.getValidIdTokenBlocking()
                                );

                                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, ProductListActivity.class));
                                finish();
                            } else {
                                uiFail(dlg, "Không lấy được idToken: " + tokenTask.getException().getMessage());
                            }
                        });
                    } else {
                        uiFail(dlg, "Đăng nhập thất bại: " + task.getException().getMessage());
                    }
                });
    }

    private void uiFail(ProgressDialog dlg, String msg) {
        runOnUiThread(() -> {
            dlg.dismiss();
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        });
    }

    private long parseLongSafe(String s, long def) {
        try { return Long.parseLong(s); } catch (Exception ignored) { return def; }
    }

    private String parseFirebaseError(String body) {
        try {
            JSONObject j = new JSONObject(body);
            JSONObject e = j.optJSONObject("error");
            if (e != null) {
                String message = e.optString("message", "");
                if (!message.isEmpty()) return message;
            }
        } catch (Exception ignored) {}
        return "Đăng nhập thất bại.";
    }
}
