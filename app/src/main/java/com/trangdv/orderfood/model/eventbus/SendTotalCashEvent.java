package com.trangdv.orderfood.model.eventbus;

public class SendTotalCashEvent {
    private String cash;
    private int foodId;
    private int restaurauntId;

    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }

    public int getRestaurauntId() {
        return restaurauntId;
    }

    public void setRestaurauntId(int restaurauntId) {
        this.restaurauntId = restaurauntId;
    }

    public SendTotalCashEvent(String cash, int foodId, int restaurauntId) {
        this.cash = cash;
        this.foodId = foodId;
        this.restaurauntId = restaurauntId;
    }

    public String getCash() {
        return cash;
    }

    public void setCash(String cash) {
        this.cash = cash;
    }
}
