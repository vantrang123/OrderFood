package com.trangdv.orderfood.retrofit;

import com.trangdv.orderfood.model.AddonModel;
import com.trangdv.orderfood.model.CreateOrderModel;
import com.trangdv.orderfood.model.FavoriteModel;
import com.trangdv.orderfood.model.FavoriteOnlyIdModel;
import com.trangdv.orderfood.model.FoodModel;
import com.trangdv.orderfood.model.HotFoodModel;
import com.trangdv.orderfood.model.MaxFoodModel;
import com.trangdv.orderfood.model.MaxOrderModel;
import com.trangdv.orderfood.model.MenuModel;
import com.trangdv.orderfood.model.OrderDetailModel;
import com.trangdv.orderfood.model.OrderIdModel;
import com.trangdv.orderfood.model.OrderModel;
import com.trangdv.orderfood.model.RestaurantIdModel;
import com.trangdv.orderfood.model.RestaurantModel;
import com.trangdv.orderfood.model.SizeModel;
import com.trangdv.orderfood.model.TokenModel;
import com.trangdv.orderfood.model.UpdateOrderModel;
import com.trangdv.orderfood.model.UpdateUserModel;
import com.trangdv.orderfood.model.UserModel;


import io.reactivex.Observable;
import lombok.Generated;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface IAnNgonAPI {
    @GET("user")
    Observable<UserModel> getUser(@Query("key") String apiKey,
                                  @Query("userPhone") String userPhone,
                                  @Query("password") String password);

    @GET("restaurant")
    Observable<RestaurantModel> getRestaurant(@Query("key") String apiKey);

    @GET("menu")
    Observable<MenuModel> getCategories(@Query("key") String apiKey,
                                        @Query("restaurantId") int restaurantId);

    @GET("food")
    Observable<FoodModel> getFoodOfMenu(@Query("key") String apiKey,
                                        @Query("menuId") int menuId);

    @GET("allfood")
    Observable<FoodModel> getAllFood(@Query("key") String apiKey,
                                       @Query("from") int from,
                                       @Query("to") int to);

    @GET("maxfood")
    Observable<MaxFoodModel> getMaxFood(@Query("key") String apiKey);

    @GET("searchFood")
    Observable<FoodModel> searchFood(@Query("key") String apiKey,
                                     @Query("foodName") String foodName,
                                     @Query("menuId") int menuId);

    @GET("foodById")
    Observable<FoodModel> getFoodById(@Query("key") String apiKey,
                                      @Query("foodId") int foodId);

    @GET("size")
    Observable<SizeModel> getSizeOfFood(@Query("key") String apiKey,
                                        @Query("foodId") int foodId);

    @GET("addon")
    Observable<AddonModel> getAddonOfFood(@Query("key") String apiKey,
                                          @Query("foodId") int foodId);

    @GET("favorite")
    Observable<FavoriteModel> getFavoriteByUser(@Query("key") String apiKey,
                                                @Query("fbid") String fbid);

    @GET("favorite")
    Observable<FavoriteOnlyIdModel> getFavoriteOnlyId(@Query("key") String apiKey,
                                                      @Query("fbid") String fbid);

    @GET("order")
    Observable<OrderModel> getOrder(@Query("key") String apiKey,
                                    @Query("orderFBID") String orderFBID,
                                    @Query("from") int from,
                                    @Query("to") int to);

    @GET("orderId")
    Observable<OrderIdModel> getOrderId(@Query("key") String apiKey,
                                        @Query("orderFBID") String orderFBID);

    @GET("orderdetailbyrestaurant")
    Observable<OrderDetailModel> getOrderDetailModel(@Query("key") String apiKey,
                                                     @Query("orderId") int orderId,
                                                     @Query("restaurantId") int restaurantId);

    @PUT("updateOrder")
    @FormUrlEncoded
    Observable<UpdateOrderModel> updateOrderStatus(@Field("key") String apiKey,
                                                   @Field("orderId") int orderId,
                                                   @Field("orderStatus") int orderStatus);

    @GET("hotfood")
    Observable<HotFoodModel> getHotFood(@Query("key") String apiKey);

    @GET("maxorder")
    Observable<MaxOrderModel> getMaxOrder(@Query("key") String apiKey,
                                          @Query("orderFBID") String orderFBID);

    @GET("nearbyrestaurant")
    Observable<RestaurantModel> getNearbyRestaurant(@Query("key") String apiKey,
                                                    @Query("lat") Double lat,
                                                    @Query("lng") Double lng,
                                                    @Query("distance") int distance);

    @GET("restaurantById")
    Observable<RestaurantModel> getRestaurantById(@Query("key") String apiKey,
                                                  @Query("restaurantId") String restaurantId);

    @POST("user")
    @FormUrlEncoded
    Observable<UpdateUserModel> updateUserInfo(@Field("key") String apiKey,
                                               @Field("userPhone") String userPhone,
                                               @Field("userName") String userName,
                                               @Field("userAddress") String userAddress,
                                               @Field("fbid") String fbid,
                                               @Field("userPassword") String userPassword);
    @GET("findrestaurantid")
    Observable<RestaurantIdModel> getRestaurantId(@Query("key") String apiKey,
                                                  @Query("foodId") int foodId);

    @POST("favorite")
    @FormUrlEncoded
    Observable<FavoriteModel> insertFavorite(@Field("key") String apiKey,
                                             @Field("fbid") String fbid,
                                             @Field("foodId") int foodId,
                                             @Field("restaurantId") int restaurantId,
                                             @Field("foodName") String foodName,
                                             @Field("foodImage") String foodImage,
                                             @Field("price") double price);

    @POST("createOrder")
    @FormUrlEncoded
    Observable<CreateOrderModel> createOrder(@Field("key") String key,
                                             @Field("orderFBID") String orderFBID,
                                             @Field("orderPhone") String orderPhone,
                                             @Field("orderName") String orderName,
                                             @Field("orderAddress") String orderAddress,
                                             @Field("orderDate") String orderDate,
                                             @Field("transactionId") String transactionId,
                                             @Field("cod") boolean cod,
                                             @Field("totalPrice") Double totalPrice,
                                             @Field("numOfItem") int numOfItem,
                                             @Field("restaurantId") int restaurantId);

    @POST("updateOrder")
    @FormUrlEncoded
    Observable<UpdateOrderModel> updateOrder(@Field("key") String key,
                                             @Field("orderId") String orderId,
                                             @Field("orderDetail") String orderDetail);

    @DELETE("favorite")
    Observable<FavoriteModel> removeFavorite(@Query("key") String apiKey,
                                             @Query("fbid") String fbid,
                                             @Query("foodId") int foodId,
                                             @Query("restaurantId") int restaurantId);

    @GET("token")
    Observable<TokenModel> getToken(@Query("key") String apiKey,
                                    @Query("fbid") String fbid);

    @POST("token")
    @FormUrlEncoded
    Observable<TokenModel> updateToken(@Field("key") String apiKey,
                                       @Field("fbid") String fbid,
                                       @Field("token") String token);
}
