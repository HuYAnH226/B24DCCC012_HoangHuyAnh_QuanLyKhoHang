package com.example.quanlykhohang;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.Collections;
import java.util.List;

final class ProductAdapter extends BaseAdapter {
    private final MainActivity activity;
    private final LayoutInflater inflater;
    private List<Product> products = Collections.emptyList();

    ProductAdapter(MainActivity activity) {
        this.activity = activity;
        inflater = LayoutInflater.from(activity);
    }

    void setProducts(List<Product> products) {
        this.products = products;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public Product getItem(int position) {
        return products.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            row = inflater.inflate(R.layout.item_product, parent, false);
            row.setTag(new ProductViews(row));
        }
        ProductViews views = (ProductViews) row.getTag();
        Product product = getItem(position);
        boolean lowStock = product.isLowStock();
        views.name.setText(product.getName());
        views.price.setText(activity.formatMoney(product.getPrice()));
        views.category.setText(product.getCategory());
        views.quantity.setText(activity.getString(R.string.quantity_short, product.getQuantity()));
        views.quantity.setTextColor(activity.getColor(lowStock ? R.color.danger : R.color.ink));
        views.lowStock.setVisibility(lowStock ? View.VISIBLE : View.GONE);
        return row;
    }

    private static final class ProductViews {
        final TextView name;
        final TextView price;
        final TextView category;
        final TextView quantity;
        final TextView lowStock;

        ProductViews(View row) {
            name = row.findViewById(R.id.item_name);
            price = row.findViewById(R.id.item_price);
            category = row.findViewById(R.id.item_category);
            quantity = row.findViewById(R.id.item_quantity);
            lowStock = row.findViewById(R.id.item_low_stock);
        }
    }
}
