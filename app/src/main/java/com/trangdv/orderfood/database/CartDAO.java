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
    @Query("SELECT * FROM Cart WHERE fbid=:fbid")
    Flowable<List<CartItem>> getAllCart(String fbid);

    @Query("SELECT COUNT(*) from Cart WHERE fbid=:fbid AND restaurantId=:restaurantId")
    Single<Integer> countItemInCart(String fbid, int restaurantId);

    @Query("SELECT COUNT(*) from Cart WHERE fbid=:fbid")
    Single<Integer> countCart(String fbid);

    @Query("SELECT SUM(foodPrice*foodQuantity) + (foodExtraPrice*foodQuantity) from Cart where foodId=:foodId")
    Single<Long> sumPrice(String foodId);

    @Query("SELECT * from Cart where foodId=:foodId AND fbid=:fbid AND restaurantId=:restaurantId")
    Single<CartItem> getItemInCart(String foodId, String fbid, int restaurantId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable insertOrReplaceAll(CartItem... cartItems);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    Single<Integer> updateCart(CartItem... cartItems);

    @Delete
    Single<Integer> deleteCart(CartItem cart);

    @Query("DELETE FROM Cart WHERE foodId=:foodId AND fbid=:fbid")
    Single<Integer> cleanCart(String fbid, String foodId);
}
