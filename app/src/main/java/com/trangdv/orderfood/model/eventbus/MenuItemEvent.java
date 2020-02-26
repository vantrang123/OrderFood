package com.trangdv.orderfood.model.eventbus;

import com.trangdv.orderfood.model.Restaurant;

public class MenuItemEvent {
    private boolean success;
    private Restaurant restaurant;

    public MenuItemEvent(boolean success, Restaurant restaurant) {
        this.success = success;
        this.restaurant = restaurant;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }
}
