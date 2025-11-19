package com.dev.thecodecup.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dev.thecodecup.R;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText newPasswordEditText, confirmPasswordEditText;
    private Button confirmResetButton;
    private String oobCode = null; // Biến lưu oobCode nhận được từ link

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_password);

        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        confirmResetButton = findViewById(R.id.confirmResetButton);

        // BƯỚC 1: Lấy oobCode từ Intent (App Link)
        oobCode = getOobCodeFromIntent(getIntent());

        if (oobCode == null) {
            // Nếu không có oobCode, báo lỗi và đóng Activity
            Toast.makeText(this, "Lỗi: Không tìm thấy mã khôi phục.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        confirmResetButton.setOnClickListener(v -> doPasswordReset());
    }

    // Phương thức trích xuất oobCode từ Intent (App Link)
    private String getOobCodeFromIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null) {
            // Firebase sẽ gửi oobCode qua query parameter
            return data.getQueryParameter("oobCode");
        }
        return null;
    }

    // BƯỚC 2: Xử lý đổi mật khẩu
    private void doPasswordReset() {
        String newPass = newPasswordEditText.getText().toString().trim();
        String confirmPass = confirmPasswordEditText.getText().toString().trim();

        if (newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ mật khẩu mới.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPass.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp.", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog dlg = ProgressDialog.show(this, null, "Đang xác nhận đổi mật khẩu...", true, false);

        // BƯỚC 3: Gọi API Firebase confirmPasswordReset
        FirebaseAuth.getInstance().confirmPasswordReset(oobCode, newPass)
                .addOnCompleteListener(task -> {
                    dlg.dismiss();
                    if (task.isSuccessful()) {
                        // Thành công!
                        Toast.makeText(this,
                                "Đổi mật khẩu thành công! Bạn có thể đăng nhập lại.",
                                Toast.LENGTH_LONG).show();

                        // Chuyển về màn hình đăng nhập và đóng Activity này
                        Intent loginIntent = new Intent(ResetPasswordActivity.this, Login.class);
                        loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(loginIntent);
                        finish();
                    } else {
                        // Lỗi (oobCode hết hạn, sai...)
                        Toast.makeText(this,
                                "Đổi mật khẩu thất bại: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}