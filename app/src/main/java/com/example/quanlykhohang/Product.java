package com.example.quanlykhohang;

public final class Product {
    public static final int LOW_STOCK_THRESHOLD = 5;

    private final int id;
    private final String name;
    private final long price;
    private final int quantity;
    private final String category;
    private final boolean active;
    private final String description;

    public Product(String name, long price, int quantity, String category,
                   boolean active, String description) {
        this(0, name, price, quantity, category, active, description);
    }

    public Product(int id, String name, long price, int quantity, String category,
                   boolean active, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.active = active;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getCategory() {
        return category;
    }

    public boolean isActive() {
        return active;
    }

    public String getDescription() {
        return description;
    }

    public boolean isLowStock() {
        return quantity < LOW_STOCK_THRESHOLD;
    }

    public long getStockValue() {
        return price * quantity;
    }
}
