package com.trangdv.orderfood.database;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public class LocalCartDataSource implements CartDataSource{
    private CartDAO cartDAO;

    public LocalCartDataSource(CartDAO cartDAO) {
        this.cartDAO = cartDAO;
    }

    @Override
    public Flowable<List<CartItem>> getAllCart(String userPhone) {
        return cartDAO.getAllCart(userPhone);
    }

    @Override
    public Single<Integer> countItemInCart(String userPhone, int restaurantId) {
        return cartDAO.countItemInCart(userPhone, restaurantId);
    }

    @Override
    public Single<Integer> countCart(String userPhone) {
        return cartDAO.countCart(userPhone);
    }

    @Override
    public Single<Long> sumPrice(String userPhone) {
        return cartDAO.sumPrice(userPhone);
    }

    @Override
    public Single<CartItem> getItemInCart(String foodId, String userPhone, int restaurantId) {
        return cartDAO.getItemInCart(foodId, userPhone, restaurantId);
    }

    @Override
    public Completable insertOrReplaceAll(CartItem... cartItems) {
        return cartDAO.insertOrReplaceAll(cartItems);
    }

    @Override
    public Single<Integer> updateCart(CartItem... cartItems) {
        return cartDAO.updateCart(cartItems);
    }

    @Override
    public Single<Integer> deleteCart(CartItem cart) {
        return cartDAO.deleteCart(cart);
    }

    @Override
    public Single<Integer> cleanCart(String userPhone) {
        return cartDAO.cleanCart(userPhone);
    }
}
