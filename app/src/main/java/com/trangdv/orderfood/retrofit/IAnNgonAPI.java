package com.trangdv.orderfood.retrofit;

import com.trangdv.orderfood.model.AddonModel;
import com.trangdv.orderfood.model.FoodModel;
import com.trangdv.orderfood.model.MenuModel;
import com.trangdv.orderfood.model.RestaurantModel;
import com.trangdv.orderfood.model.SizeModel;
import com.trangdv.orderfood.model.UpdateUserModel;
import com.trangdv.orderfood.model.UserModel;


import io.reactivex.Observable;
import lombok.Generated;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface IAnNgonAPI {
    @GET("user")
    Observable<UserModel> getUser(@Query("key") String apiKey,
                                  @Query("fbid") String fbid);

    @GET("restaurant")
    Observable<RestaurantModel> getRestaurant(@Query("key") String apiKey);

    @GET("menu")
    Observable<MenuModel> getCategories(@Query("key") String apiKey,
                                        @Query("restaurantId") int restaurantId);

    @GET("food")
    Observable<FoodModel> getFoodOfMenu(@Query("key") String apiKey,
                                        @Query("menuId") int menuId);

    @GET("searchFood")
    Observable<FoodModel> searchFood(@Query("key") String apiKey,
                                     @Query("foodName") String foodName,
                                        @Query("menuId") int menuId);

    @GET("size")
    Observable<SizeModel> getSizeOfFood(@Query("key") String apiKey,
                                          @Query("foodId") int foodId);

    @GET("addon")
    Observable<AddonModel> getAddonOfFood(@Query("key") String apiKey,
                                        @Query("foodId") int foodId);

    @POST("user")
    @FormUrlEncoded
    Observable<UpdateUserModel> updateUserInfo(@Field("key") String apiKey,
                                               @Field("userPhone") String userPhone,
                                               @Field("userName") String userName,
                                               @Field("userAddress") String userAddress,
                                               @Field("fbid") String fbid);
}
