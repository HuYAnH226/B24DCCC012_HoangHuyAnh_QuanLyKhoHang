package com.example.quanlykhohang;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public final class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "warehouse.db";
    private static final int DATABASE_VERSION = 1;
    private static final String USERS = "users";
    private static final String PRODUCTS = "products";
    private static final String BY_ID = "id = ?";
    private static final String[] PRODUCT_COLUMNS = {
            "id", "name", "price", "quantity", "category", "active", "description"
    };

    public DatabaseHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT NOT NULL UNIQUE, "
                + "password TEXT NOT NULL)");
        db.execSQL("CREATE TABLE products ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL CHECK(length(trim(name)) > 0), "
                + "price INTEGER NOT NULL CHECK(typeof(price) = 'integer' AND price > 0), "
                + "quantity INTEGER NOT NULL CHECK(typeof(quantity) = 'integer' "
                + "AND quantity BETWEEN 0 AND 2147483647), "
                + "category TEXT NOT NULL CHECK(category IN "
                + "('Điện tử', 'Thực phẩm', 'Văn phòng phẩm', 'Gia dụng', 'Khác')), "
                + "active INTEGER NOT NULL CHECK(active IN (0, 1)), "
                + "description TEXT)");

        insertUser(db, "admin", "123456");
        db.insertOrThrow(PRODUCTS, null, productValues(new Product(
                "Chuột không dây", 150000, 20, "Điện tử", true,
                "Chuột không dây kết nối 2.4GHz, pin dùng lâu")));
        db.insertOrThrow(PRODUCTS, null, productValues(new Product(
                "Bàn phím cơ", 450000, 3, "Điện tử", true,
                "Bàn phím cơ switch xanh, có đèn nền")));
        db.insertOrThrow(PRODUCTS, null, productValues(new Product(
                "Sổ tay A5", 25000, 40, "Văn phòng phẩm", true,
                "Sổ tay bìa cứng khổ A5, giấy dày")));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new IllegalStateException(
                "Missing migration from version " + oldVersion + " to " + newVersion);
    }

    public boolean checkLogin(String username, String password) {
        Cursor cursor = getReadableDatabase().query(USERS, new String[]{"id"},
                "username = ? AND password = ?", new String[]{username, password},
                null, null, null, "1");
        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    public long insertUser(String username, String password) {
        return insertUser(getWritableDatabase(), username, password);
    }

    private long insertUser(SQLiteDatabase db, String username, String password) {
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        return db.insertOrThrow(USERS, null, values);
    }

    public long insertProduct(Product product) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            requireStockValueFits(readTotalValue(db), product.getPrice(), product.getQuantity());
            long id = db.insertOrThrow(PRODUCTS, null, productValues(product));
            db.setTransactionSuccessful();
            return id;
        } finally {
            db.endTransaction();
        }
    }

    public ArrayList<Product> getAllProducts() {
        ArrayList<Product> products = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(PRODUCTS, PRODUCT_COLUMNS,
                null, null, null, null, "id DESC");
        try {
            while (cursor.moveToNext()) {
                products.add(readProduct(cursor));
            }
            return products;
        } finally {
            cursor.close();
        }
    }

    public Product getProductById(int id) {
        return getProductById(getReadableDatabase(), id);
    }

    private Product getProductById(SQLiteDatabase db, int id) {
        Cursor cursor = db.query(PRODUCTS, PRODUCT_COLUMNS, BY_ID, idArgs(id),
                null, null, null, "1");
        try {
            return cursor.moveToFirst() ? readProduct(cursor) : null;
        } finally {
            cursor.close();
        }
    }

    public int updateQuantity(int id, int quantity) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            Product product = getProductById(db, id);
            if (product == null) {
                return 0;
            }
            long otherStockValue = readTotalValue(db) - product.getStockValue();
            requireStockValueFits(otherStockValue, product.getPrice(), quantity);
            ContentValues values = new ContentValues();
            values.put("quantity", quantity);
            int updated = db.update(PRODUCTS, values, BY_ID, idArgs(id));
            db.setTransactionSuccessful();
            return updated;
        } finally {
            db.endTransaction();
        }
    }

    public int deleteProduct(int id) {
        return getWritableDatabase().delete(PRODUCTS, BY_ID, idArgs(id));
    }

    public long getTotalValue() {
        return readTotalValue(getReadableDatabase());
    }

    private long readTotalValue(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT COALESCE(SUM(price * quantity), 0) FROM products",
                null);
        try {
            return cursor.moveToFirst() ? cursor.getLong(0) : 0L;
        } finally {
            cursor.close();
        }
    }

    public int countLowStock() {
        Cursor cursor = getReadableDatabase().rawQuery(
                "SELECT COUNT(*) FROM products WHERE quantity < ?",
                new String[]{String.valueOf(Product.LOW_STOCK_THRESHOLD)});
        try {
            return cursor.moveToFirst() ? cursor.getInt(0) : 0;
        } finally {
            cursor.close();
        }
    }

    private static void requireStockValueFits(long currentValue, long price, int quantity) {
        if (quantity > 0 && price > (Long.MAX_VALUE - currentValue) / quantity) {
            throw new ArithmeticException("Stock value exceeds SQLite INTEGER range");
        }
    }

    private static String[] idArgs(int id) {
        return new String[]{String.valueOf(id)};
    }

    private static ContentValues productValues(Product product) {
        ContentValues values = new ContentValues();
        values.put("name", product.getName());
        values.put("price", product.getPrice());
        values.put("quantity", product.getQuantity());
        values.put("category", product.getCategory());
        values.put("active", product.isActive() ? 1 : 0);
        values.put("description", product.getDescription());
        return values;
    }

    private static Product readProduct(Cursor cursor) {
        return new Product(cursor.getInt(0), cursor.getString(1), cursor.getLong(2),
                cursor.getInt(3), cursor.getString(4), cursor.getInt(5) == 1,
                cursor.getString(6));
    }
}
