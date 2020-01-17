package com.trangdv.orderfood.database;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import com.trangdv.orderfood.listener.OnDatabaseChangedListeners;
import com.trangdv.orderfood.model.Favorites;
import com.trangdv.orderfood.model.Order;

import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteAssetHelper {

    private static OnDatabaseChangedListeners changedListeners;

    private static final String DB_NAME = "OFDB.db";
    private static final int DB_VER = 1;

    public Database(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }

    public List<Order> getCarts() {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String[] sqlSelect = {"ProductName", "ProductId", "Quantity", "Price", "Discount", "Image"};
        String sqlTable = "OrderDetail";

        qb.setTables(sqlTable);
        Cursor c = qb.query(db, sqlSelect, null, null, null, null, null);
        final List<Order> result = new ArrayList<>();

        if (c.moveToFirst()) {
            do {
                result.add(new Order(c.getString(c.getColumnIndex("ProductId")),
                        c.getString(c.getColumnIndex("ProductName")),
                        c.getString(c.getColumnIndex("Quantity")),
                        c.getString(c.getColumnIndex("Price")),
                        c.getString(c.getColumnIndex("Discount")),
                        c.getString(c.getColumnIndex("Image"))
                ));
            } while (c.moveToNext());
        }
        db.close();
        return result;
    }

    public Order getItem(String productId) {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String[] sqlSelect = {"ProductName", "ProductId", "Quantity", "Price", "Discount", "Image"};
        String sqlTable = "OrderDetail";

        qb.setTables(sqlTable);
        Cursor c = qb.query(db, sqlSelect, null, null, null, null, null);
        Order order = null;
        if (c.moveToFirst()) {
            while (!c.getString(c.getColumnIndex("ProductId")).equals(productId)) {
                c.moveToNext();
            }
            order = new Order(c.getString(c.getColumnIndex("ProductId")),
                    c.getString(c.getColumnIndex("ProductName")),
                    c.getString(c.getColumnIndex("Quantity")),
                    c.getString(c.getColumnIndex("Price")),
                    c.getString(c.getColumnIndex("Discount")),
                    c.getString(c.getColumnIndex("Image"))
            );
        }

        db.close();
        return order;
    }

    public void addToCart(Order order) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("INSERT INTO OrderDetail(ProductId, ProductName, Quantity, Price, Discount, Image) VALUES('%s','%s','%s','%s','%s','%s');",
                order.getProductId(),
                order.getProductName(),
                order.getQuanlity(),
                order.getPrice(),
                order.getDiscount(),
                order.getImage());

        db.execSQL(query);
        db.close();
    }

    public void removeFromCart(String order) {
        SQLiteDatabase db = getReadableDatabase();

        String query = String.format("DELETE FROM OrderDetail WHERE ProductId='" + order + "'");
        db.execSQL(query);
        db.close();

    }

    public boolean IsProductExist(String id) {
        SQLiteDatabase db = getReadableDatabase();

        String query = String.format("SELECT * FROM OrderDetail WHERE ProductId='" + id + "'");
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() > 0) {
            return true;
        }
        return false;
    }

    public void updateCart(String id, String value) {
        SQLiteDatabase db = getWritableDatabase();
        String query = String.format("UPDATE OrderDetail SET Quantity='" + value + "' WHERE ProductId='" + id + "'");
        db.execSQL(query);
        db.close();
    }

    public void cleanCart() {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("DELETE FROM OrderDetail");
        db.execSQL(query);
        db.close();

    }

    //Favourites
    public void addToFavourites(Favorites food) {

        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("INSERT INTO Favorites(" +
                        "FoodId,FoodName,FoodPrice,FoodMenuId,FoodImage,FoodDescription,UserPhone,FoodDiscount)" +
                        "VALUES('%s','%s','%s','%s','%s','%s','%s','%s');",
                food.getFoodId(),
                food.getFoodName(),
                food.getFoodPrice(),
                food.getFoodMenuId(),
                food.getFoodImage(),
                food.getFoodDescription(),
                food.getUserPhone(),
                food.getFoodDiscount());
        db.execSQL(query);
    }

    public void removeFromFavourites(String foodId, String userPhone) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("DELETE FROM Favorites WHERE FoodId = '%s' and UserPhone = '%s' ;", foodId, userPhone);
        db.execSQL(query);
    }

    public boolean isFavourite(String foodId, String userPhone) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("SELECT * FROM Favorites WHERE FoodId = '%s' and UserPhone = '%s' ;", foodId, userPhone);
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.getCount() >0) {
            return true;
        }
        return false;
    }

    public List<Favorites> getAllFavorites(String userPhone) {

        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String[] sqlSelect = {"UserPhone","FoodId", "FoodName", "FoodPrice", "FoodDiscount", "FoodMenuId", "FoodImage", "FoodDescription"};
        String sqlTable = "Favorites";

        qb.setTables(sqlTable);
        Cursor c = qb.query(db, sqlSelect, "UserPhone=?", new String[]{userPhone}, null, null, null);

        final List<Favorites> result = new ArrayList<>();
        if (c.moveToFirst()) {
            do {
                result.add(new Favorites(
                        c.getString(c.getColumnIndex("FoodId")),
                        c.getString(c.getColumnIndex("FoodName")),
                        c.getString(c.getColumnIndex("FoodPrice")),
                        c.getString(c.getColumnIndex("FoodMenuId")),
                        c.getString(c.getColumnIndex("FoodImage")),
                        c.getString(c.getColumnIndex("FoodDescription")),
                        c.getString(c.getColumnIndex("UserPhone")),
                        c.getString(c.getColumnIndex("FoodDiscount"))
                ));
            } while (c.moveToNext());
        }
        return result;
    }
}