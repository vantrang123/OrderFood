package com.trangdv.orderfood.presenter.home;

public interface IHomePresenter {
    void getNumOfFood();
    void getTenItemFood(int from, int to);
    void getFoodById(int foodId);
}
