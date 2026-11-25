package com.example.quanlykhohang;

import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

public final class AddProductActivity extends WarehouseActivity {
    private EditText nameInput;
    private EditText priceInput;
    private EditText quantityInput;
    private Spinner categoryInput;
    private Switch activeInput;
    private EditText descriptionInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        setupToolbar(R.string.add_title);
        nameInput = findViewById(R.id.product_name_input);
        priceInput = findViewById(R.id.product_price_input);
        quantityInput = findViewById(R.id.product_quantity_input);
        categoryInput = findViewById(R.id.product_category_input);
        activeInput = findViewById(R.id.product_active_input);
        descriptionInput = findViewById(R.id.product_description_input);

        ArrayAdapter<CharSequence> categories = ArrayAdapter.createFromResource(
                this, R.array.product_categories, android.R.layout.simple_spinner_item);
        categories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryInput.setAdapter(categories);
        findViewById(R.id.save_product).setOnClickListener(view -> saveProduct());
    }

    private void saveProduct() {
        String name = nameInput.getText().toString().trim();
        if (name.isEmpty()) {
            showInputError(nameInput, R.string.name_required);
            return;
        }

        long price;
        try {
            price = Long.parseLong(priceInput.getText().toString().trim());
        } catch (NumberFormatException error) {
            showInputError(priceInput, R.string.price_invalid);
            return;
        }
        if (price <= 0) {
            showInputError(priceInput, R.string.price_invalid);
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityInput.getText().toString().trim());
        } catch (NumberFormatException error) {
            showInputError(quantityInput, R.string.quantity_invalid);
            return;
        }
        if (quantity < 0) {
            showInputError(quantityInput, R.string.quantity_invalid);
            return;
        }

        Product product = new Product(name, price, quantity,
                categoryInput.getSelectedItem().toString(), activeInput.isChecked(),
                descriptionInput.getText().toString().trim());
        try {
            database.insertProduct(product);
            finish();
        } catch (ArithmeticException error) {
            showMessage(R.string.stock_value_limit);
        } catch (SQLiteException error) {
            showMessage(R.string.save_failed);
        }
    }

    private void showInputError(EditText field, int message) {
        field.requestFocus();
        field.setError(getString(message));
    }
}
