package com.trangdv.orderfood.presenter.placeorder;

import android.content.Intent;

import com.google.gson.Gson;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.database.CartItem;
import com.trangdv.orderfood.model.FCMSendData;
import com.trangdv.orderfood.retrofit.IAnNgonAPI;
import com.trangdv.orderfood.ui.PlaceOrderActivity;
import com.trangdv.orderfood.ui.main.MainActivity;
import com.trangdv.orderfood.view.IPlaceOrderView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PlaceOrderPresenter implements IPlaceOrderPresenter{
    IPlaceOrderView iPlaceOrderView;
    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;

    public PlaceOrderPresenter(IPlaceOrderView iPlaceOrderView, IAnNgonAPI anNgonAPI, CompositeDisposable compositeDisposable) {
        this.iPlaceOrderView = iPlaceOrderView;
        this.anNgonAPI = anNgonAPI;
        this.compositeDisposable = compositeDisposable;
    }

    @Override
    public void createOrder(String address, String date, double totalPrice, List<CartItem> cartItems, int restaurantId, String lat, String lng) {
        compositeDisposable.add(
                anNgonAPI.createOrder(Common.API_KEY,
                        Common.currentUser.getFbid(),
                        Common.currentUser.getUserPhone(),
                        Common.currentUser.getName(),
                        address,
                        date,
                        "NONE",
                        true,
                        totalPrice,
                        cartItems.size(),
                        restaurantId,
                        lat,
                        lng
                )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(createOrderModel -> {
                                    if (createOrderModel.isSuccess()) {
                                        iPlaceOrderView.onCreateOrderSuccess(createOrderModel, cartItems);
                                    }
                                },
                                throwable -> {
                                    iPlaceOrderView.onCreateOrderError(throwable.getMessage());
                                })
        );
    }

}
