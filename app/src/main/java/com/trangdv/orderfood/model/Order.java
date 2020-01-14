package com.trangdv.orderfood.model;

import android.graphics.Bitmap;
import android.os.StrictMode;

import java.io.Serializable;

public class Order {
    private String ProductId;
    private String ProductName;
    private String Quanlity;
    private String Price;
    private String Discount;
    private String Image;

    public Order() {
    }

    public Order(String productId, String productName, String quanlity, String price, String discount, String image) {
        ProductId = productId;
        ProductName = productName;
        Quanlity = quanlity;
        Price = price;
        Discount = discount;
        Image = image;

    }

    public String getProductId() {
        return ProductId;
    }

    public void setProductId(String productId) {
        ProductId = productId;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public String getQuanlity() {
        return Quanlity;
    }

    public void setQuanlity(String quanlity) {
        Quanlity = quanlity;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public String getDiscount() {
        return Discount;
    }

    public void setDiscount(String discount) {
        Discount = discount;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }
}
