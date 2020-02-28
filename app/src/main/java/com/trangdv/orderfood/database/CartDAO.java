package com.trangdv.orderfood.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface CartDAO {
    @Query("SELECT * FROM Cart WHERE userPhone=:userPhone AND restaurantId=:restaurantId")
    Flowable<List<CartItem>> getAllCart(String userPhone, int restaurantId);

    @Query("SELECT COUNT(*) from Cart WHERE userPhone=:userPhone AND restaurantId=:restaurantId")
    Single<Integer> countItemInCart(String userPhone, int restaurantId);

    @Query("SELECT SUM(foodPrice*foodQuantity) + (foodExtraPrice*foodQuantity) from Cart where userPhone=:userPhone AND restaurantId=:restaurantId")
    Single<Long> sumPrice(String userPhone, int restaurantId);

    @Query("SELECT * from Cart where foodId=:foodId AND userPhone=:userPhone AND restaurantId=:restaurantId")
    Single<CartItem> getItemInCart(String foodId, String userPhone, int restaurantId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertOrReplaceAll(CartItem... cartItems);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    Single<Integer> updateCart(CartItem... cartItems);

    @Delete
    Single<Integer> deleteCart(CartItem cart);

    @Query("DELETE FROM Cart WHERE userPhone=:userPhone AND restaurantId=:restaurantId")
    Single<Integer> cleanCart(String userPhone, int restaurantId);
}
