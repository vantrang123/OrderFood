package com.trangdv.orderfood.model;

import java.util.List;

public class OrderIdModel {
    private boolean success;
    private String message;
    private List<OrderId> result;

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

    public List<OrderId> getResult() {
        return result;
    }

    public void setResult(List<OrderId> result) {
        this.result = result;
    }
}
