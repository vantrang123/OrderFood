package com.trangdv.orderfood.presenter.placeorder;

import com.trangdv.orderfood.database.CartItem;

import java.util.List;

public interface IPlaceOrderPresenter {
    void createOrder(String address, String date, double totalPrice, List<CartItem> cartItems, int restaurantId, String lat, String lng);
}
