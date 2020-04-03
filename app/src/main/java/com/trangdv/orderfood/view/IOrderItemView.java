package com.trangdv.orderfood.view;

import com.trangdv.orderfood.model.OrderDetail;

import java.util.List;

public interface IOrderItemView {
    void onAllOrderSuccess(List<OrderDetail> orderDetailList);
    void onAllOrderError(String message);
}
