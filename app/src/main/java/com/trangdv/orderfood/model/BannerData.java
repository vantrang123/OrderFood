package com.trangdv.orderfood.model;

public class BannerData {

    private String key;
    private String image;
    private String name;

    public BannerData() {

    }

    public BannerData(String imagePath, String title) {
        this.image = imagePath;
        this.name = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
