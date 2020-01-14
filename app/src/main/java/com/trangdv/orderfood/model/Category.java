package com.trangdv.orderfood.model;

import android.graphics.Bitmap;

public class Category {
    private String Name;
    private String Image;
    private Bitmap bitmapImage;
    private String key;

    public Category() {
    }

    public Category(String name, String image) {
        Name = name;
        Image = image;
    }

    public Category(String name) {

    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setBitmapImage(Bitmap bitmapImage) {
        this.bitmapImage = bitmapImage;
    }

    public Bitmap getBitmapImage() {
        return bitmapImage;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
