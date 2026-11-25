package com.example.quanlykhohang;

import android.app.Activity;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.widget.TextView;
import android.widget.Toast;
import java.text.NumberFormat;
import java.util.Locale;

abstract class WarehouseActivity extends Activity {
    private static final Locale VIETNAMESE = Locale.forLanguageTag("vi-VN");
    private final NumberFormat numberFormat = NumberFormat.getIntegerInstance(VIETNAMESE);
    protected DatabaseHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = new DatabaseHelper(this);
    }

    @Override
    public void setContentView(int layoutResId) {
        super.setContentView(layoutResId);
        if (Build.VERSION.SDK_INT >= 35) {
            View root = findViewById(R.id.screen_root);
            root.setOnApplyWindowInsetsListener((view, insets) -> {
                Insets safeArea = insets.getInsets(
                        WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout()
                                | WindowInsets.Type.ime());
                view.setPadding(safeArea.left, safeArea.top, safeArea.right, safeArea.bottom);
                return WindowInsets.CONSUMED;
            });
            root.requestApplyInsets();
        }
    }

    protected void setupToolbar(int title) {
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(title);
        findViewById(R.id.toolbar_back).setOnClickListener(view -> finish());
    }

    protected String formatMoney(long amount) {
        return getString(R.string.money_vnd, numberFormat.format(amount));
    }

    protected void showMessage(int message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        database.close();
        super.onDestroy();
    }
}
