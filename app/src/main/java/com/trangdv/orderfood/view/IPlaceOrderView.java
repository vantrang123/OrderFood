package com.trangdv.orderfood.view;

import com.trangdv.orderfood.database.CartItem;
import com.trangdv.orderfood.model.CreateOrderModel;

import java.util.List;

public interface IPlaceOrderView {
    void onCreateOrderSuccess(CreateOrderModel createOrderModel, List<CartItem> cartItems);
    void onCreateOrderError(String message);
}
