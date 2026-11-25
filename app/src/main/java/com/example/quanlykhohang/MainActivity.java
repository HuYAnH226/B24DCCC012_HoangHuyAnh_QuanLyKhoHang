package com.example.quanlykhohang;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;

public final class MainActivity extends WarehouseActivity {
    private ProductAdapter adapter;
    private TextView totalProducts;
    private TextView totalValue;
    private TextView totalLowStock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        totalProducts = findViewById(R.id.total_products);
        totalValue = findViewById(R.id.total_inventory_value);
        totalLowStock = findViewById(R.id.total_low_stock);

        ListView list = findViewById(R.id.product_list);
        adapter = new ProductAdapter(this);
        list.setAdapter(adapter);
        list.setEmptyView(findViewById(R.id.empty_products));
        list.setOnItemClickListener((parent, view, position, id) -> {
            Product product = adapter.getItem(position);
            Intent intent = new Intent(this, ProductDetailActivity.class);
            intent.putExtra(ProductDetailActivity.PRODUCT_ID, product.getId());
            startActivity(intent);
        });

        findViewById(R.id.add_product).setOnClickListener(view ->
                startActivity(new Intent(this, AddProductActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<Product> products = database.getAllProducts();
        adapter.setProducts(products);
        totalProducts.setText(String.valueOf(products.size()));
        totalValue.setText(formatMoney(database.getTotalValue()));
        totalLowStock.setText(String.valueOf(database.countLowStock()));
    }
}
