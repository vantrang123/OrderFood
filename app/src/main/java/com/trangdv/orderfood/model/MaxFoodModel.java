package com.trangdv.orderfood.model;

import java.util.List;

public class MaxFoodModel {
    private boolean success;
    private String message;
    private List<MaxFood> result;

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

    public List<MaxFood> getResult() {
        return result;
    }

    public void setResult(List<MaxFood> result) {
        this.result = result;
    }
}
