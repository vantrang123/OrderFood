package com.trangdv.orderfood.presenter.orderdetail;

import com.trangdv.orderfood.adapters.OrderItemAdapter;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.retrofit.IAnNgonAPI;
import com.trangdv.orderfood.view.IOrderItemView;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class OrderItemPresenter implements IOrderItemPresenter {
    IOrderItemView iOrderItemView;
    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;

    public OrderItemPresenter(IOrderItemView iOrderItemView, IAnNgonAPI anNgonAPI, CompositeDisposable compositeDisposable) {
        this.iOrderItemView = iOrderItemView;
        this.anNgonAPI = anNgonAPI;
        this.compositeDisposable = compositeDisposable;
    }

    @Override
    public void getAllOrderItem() {
        compositeDisposable.add(anNgonAPI.getOrderDetailModel(Common.API_KEY, Common.currentOrder.getOrderId(), Common.currentOrder.getRestaurantId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(orderDetailModel -> {
                            if (orderDetailModel.isSuccess()) {
                                if (orderDetailModel.getResult().size() > 0) {
                                    iOrderItemView.onAllOrderSuccess(orderDetailModel.getResult());
                                }
                            }
                            iOrderItemView.onAllOrderError(orderDetailModel.getMessage());
                        }
                        , throwable -> {
                            iOrderItemView.onAllOrderError(throwable.getMessage());
                        }
                ));
    }
}
