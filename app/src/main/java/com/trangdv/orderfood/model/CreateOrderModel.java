package com.trangdv.orderfood.model;

import java.util.List;

public class CreateOrderModel {
    private boolean success;
    private String message;
    private List<CreateOrder> result;

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

    public List<CreateOrder> getResult() {
        return result;
    }

    public void setResult(List<CreateOrder> result) {
        this.result = result;
    }
}
