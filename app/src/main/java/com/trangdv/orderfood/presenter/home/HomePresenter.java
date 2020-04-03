package com.trangdv.orderfood.presenter.home;

import android.content.Intent;
import android.widget.Toast;

import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.model.eventbus.FoodDetailEvent;
import com.trangdv.orderfood.retrofit.IAnNgonAPI;
import com.trangdv.orderfood.ui.fooddetail.FoodDetailActivity;
import com.trangdv.orderfood.view.IHomeView;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class HomePresenter implements IHomePresenter{
    IHomeView iHomeView;
    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;

    public HomePresenter(IHomeView iHomeView, IAnNgonAPI anNgonAPI, CompositeDisposable compositeDisposable) {
        this.iHomeView = iHomeView;
        this.anNgonAPI = anNgonAPI;
        this.compositeDisposable = compositeDisposable;
    }


    @Override
    public void getNumOfFood() {
        compositeDisposable.add(anNgonAPI.getMaxFood(Common.API_KEY)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(maxFoodModel -> {
                            if (maxFoodModel.isSuccess()) {
                                if (maxFoodModel.getResult().size() > 0) {
                                    iHomeView.onGetNumFoodSuccess(maxFoodModel.getResult().get(0).getMaxRowNum());
                                }
                            }
                        }
                        , throwable -> {
                        }
                ));
    }

    @Override
    public void getTenItemFood(int from, int to) {
        compositeDisposable.add(anNgonAPI.getAllFood(Common.API_KEY,
                from, to)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(foodModel -> {
                            if (foodModel.isSuccess()) {
                                iHomeView.onGetTenItemFoodSuccess(foodModel);
                            } else {
                                iHomeView.onError(foodModel.getMessage());
                            }
                        }
                        , throwable -> {
                        }
                ));
    }

    @Override
    public void getFoodById(int foodId) {
        compositeDisposable.add(
                anNgonAPI.getFoodById(Common.API_KEY, foodId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(foodModel -> {
                            iHomeView.onGetFoodSuccess(foodModel.getResult().get(0));

                        }, throwable -> {
                            iHomeView.onGetFoodError(throwable.getMessage());
                        })
        );
    }
}
