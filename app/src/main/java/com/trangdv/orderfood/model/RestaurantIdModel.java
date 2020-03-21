package com.trangdv.orderfood.model;

import java.util.List;

public class RestaurantIdModel {
    private boolean success;
    private String message;
    private List<RestaurantId> result;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<RestaurantId> getResult() {
        return result;
    }

    public void setResult(List<RestaurantId> result) {
        this.result = result;
    }
}
