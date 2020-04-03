package com.trangdv.orderfood.view;

import com.trangdv.orderfood.model.Food;
import com.trangdv.orderfood.model.FoodModel;

public interface IHomeView {
    void onGetNumFoodSuccess(int num);
    void onGetTenItemFoodSuccess(FoodModel foodModel);
    void onError(String message);
    void onGetFoodSuccess(Food food);
    void onGetFoodError(String message);
}