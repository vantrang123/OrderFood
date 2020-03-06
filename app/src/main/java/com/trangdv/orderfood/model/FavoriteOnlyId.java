package com.trangdv.orderfood.model;

public class FavoriteOnlyId {
    public FavoriteOnlyId(int foodId) {
        this.foodId = foodId;
    }

    private int foodId;

    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }
}
