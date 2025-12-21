package com.dev.thecodecup.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dev.thecodecup.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseBottomNavActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNav;

    /** Mỗi activity con trả về id menu đang active, vd: R.id.navigation_home */
    @IdRes
    protected abstract int getBottomNavMenuItemId();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lưu ý: setContentView() phải gọi ở activity con,
        // sau đó mới gọi setupBottomNav() bên dưới.
    }

    protected void setupBottomNav() {
        bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(getBottomNavMenuItemId());

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            // Đang ở đúng tab rồi -> giữ nó sáng, không đi đâu cả
            if (itemId == getBottomNavMenuItemId()) {
                return true;
            }

            Intent intent = null;

            if (itemId == R.id.navigation_home) {
                intent = new Intent(this, HomeActivity.class);
            } else if (itemId == R.id.navigation_product) {
                intent = new Intent(this, ProductListActivity.class);
            } else if (itemId == R.id.navigation_cart) {
                intent = new Intent(this, CartActivity.class);
            } else if (itemId == R.id.navigation_profile) {
                intent = new Intent(this, ProfileActivity.class);
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);

                // ⬇⬇⬇ QUAN TRỌNG: trả về false để KHÔNG đổi tab ở activity hiện tại
                return false;
            }

            return false;
        });
    }
}
