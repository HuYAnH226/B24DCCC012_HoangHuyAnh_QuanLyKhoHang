package com.example.quanlykhohang;

import android.app.AlertDialog;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public final class ProductDetailActivity extends WarehouseActivity {
    public static final String PRODUCT_ID = "PRODUCT_ID";

    private int productId;
    private TextView name;
    private TextView category;
    private TextView status;
    private TextView price;
    private TextView quantity;
    private TextView lowStock;
    private TextView stockValue;
    private TextView description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        setupToolbar(R.string.detail_title);
        productId = getIntent().getIntExtra(PRODUCT_ID, -1);

        name = findViewById(R.id.detail_name);
        category = findViewById(R.id.detail_category);
        status = findViewById(R.id.detail_status);
        price = findViewById(R.id.detail_price);
        quantity = findViewById(R.id.detail_quantity);
        lowStock = findViewById(R.id.detail_low_stock);
        stockValue = findViewById(R.id.detail_stock_value);
        description = findViewById(R.id.detail_description);
        findViewById(R.id.receive_one).setOnClickListener(view -> changeQuantity(1));
        findViewById(R.id.sell_one).setOnClickListener(view -> changeQuantity(-1));
        findViewById(R.id.detail_back).setOnClickListener(view -> finish());
        findViewById(R.id.delete_product).setOnClickListener(view -> confirmDelete());
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderProduct();
    }

    private void renderProduct() {
        Product product = loadProduct();
        if (product == null) {
            return;
        }
        name.setText(product.getName());
        category.setText(product.getCategory());
        status.setText(product.isActive() ? R.string.active_product : R.string.inactive_product);
        price.setText(formatMoney(product.getPrice()));
        quantity.setText(String.valueOf(product.getQuantity()));
        quantity.setTextColor(getColor(product.isLowStock() ? R.color.danger : R.color.ink));
        lowStock.setVisibility(product.isLowStock() ? View.VISIBLE : View.GONE);
        stockValue.setText(formatMoney(product.getStockValue()));
        String notes = product.getDescription();
        description.setText(notes == null || notes.trim().isEmpty()
                ? getString(R.string.no_description) : notes);
    }

    private Product loadProduct() {
        Product product = productId > 0 ? database.getProductById(productId) : null;
        if (product == null) {
            showMessage(R.string.product_missing);
            finish();
        }
        return product;
    }

    private void changeQuantity(int delta) {
        Product product = loadProduct();
        if (product == null) {
            return;
        }
        int current = product.getQuantity();
        if (delta < 0 && current == 0) {
            showMessage(R.string.out_of_stock);
            return;
        }
        if (delta > 0 && current == Integer.MAX_VALUE) {
            showMessage(R.string.quantity_limit);
            return;
        }
        try {
            database.updateQuantity(productId, current + delta);
            renderProduct();
        } catch (ArithmeticException error) {
            showMessage(R.string.stock_value_limit);
        } catch (SQLiteException error) {
            showMessage(R.string.save_failed);
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_product)
                .setMessage(getString(R.string.delete_confirmation, name.getText()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete_action, (dialog, which) -> deleteProduct())
                .show();
    }

    private void deleteProduct() {
        try {
            database.deleteProduct(productId);
            finish();
        } catch (SQLiteException error) {
            showMessage(R.string.delete_failed);
        }
    }
}
