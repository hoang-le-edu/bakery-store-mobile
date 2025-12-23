package com.dev.thecodecup.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dev.thecodecup.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class AdminBottomNavActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavAdmin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /** Mỗi Activity con trả về id item tương ứng trong bottom_nav_menu_admin */
    @IdRes
    protected abstract int getAdminMenuItemId();

    protected void setupAdminBottomNav() {
        bottomNavAdmin = findViewById(R.id.bottom_nav_admin);
        if (bottomNavAdmin == null) return;

        // Đánh dấu tab hiện tại (không kích listener)
        bottomNavAdmin.setSelectedItemId(getAdminMenuItemId());

        bottomNavAdmin.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // Nếu bấm lại đúng tab hiện tại thì không làm gì
            if (id == getAdminMenuItemId()) {
                return true;
            }

            Intent intent = null;

            if (id == R.id.navigation_admin_home) {
                intent = new Intent(this, AdminHomeActivity.class);

            } else if (id == R.id.navigation_admin_product) {
                intent = new Intent(this, AdminProductListActivity.class);

            } else if (id == R.id.navigation_admin_orders) {
                intent = new Intent(this, AdminOrdersActivity.class);

            } else if (id == R.id.navigation_admin_user) {
                intent = new Intent(this, AdminProfileActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
                // Trả về false để giao diện hiện tại không tự đổi trạng thái tab
                // Tab sẽ được đánh dấu đúng ở Activity mới (setupAdminBottomNav)
                return false;
            }

            return false;
        });
    }
}
