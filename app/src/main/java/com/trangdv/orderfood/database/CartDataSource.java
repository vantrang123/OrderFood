package com.trangdv.orderfood.database;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

public interface CartDataSource {
    Flowable<List<CartItem>> getAllCart(String userPhone);

    Single<Integer> countItemInCart(String userPhone, int restaurantId);

    Single<Integer> countCart(String userPhone);

    Single<Long> sumPrice(String userPhone);

    Single<CartItem> getItemInCart(String foodId, String userPhone, int restaurantId);

    Completable insertOrReplaceAll(CartItem... cartItems);

    Single<Integer> updateCart(CartItem... cartItems);

    Single<Integer> deleteCart(CartItem cart);

    Single<Integer> cleanCart(String fbid, String foodId);
}
