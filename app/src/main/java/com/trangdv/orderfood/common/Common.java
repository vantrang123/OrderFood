package com.trangdv.orderfood.common;

import com.trangdv.orderfood.model.Addon;
import com.trangdv.orderfood.model.FavoriteOnlyId;
import com.trangdv.orderfood.model.Restaurant;
import com.trangdv.orderfood.model.User;
import com.trangdv.orderfood.remote.APIService;
import com.trangdv.orderfood.remote.FCMRetrofitClient;

import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class Common {
    public static final String API_KEY = "1234";
    public static User currentUser;
    public static Restaurant currentRestaurant;
    public static final String DELETE = "Delete";
    public static String PHONE_TEXT = "userPhone";
    private static final String fcmUrl = "https://fcm.googleapis.com/";

    public static final String API_ANNGON_ENDPOINT = "http://192.168.137.1:3000";
    public static Set<Addon> addonList = new HashSet<>();
    public static List<FavoriteOnlyId> currentFavOfRestaurant;


    public static String convertCodeToStatus(int code) {
        switch (code) {
            case 0:
                return "Placed";
            case 1:
                return "Shipping";
            case 2:
                return "Shipped";
            case -1:
                return "Cancelled";
            default:
                return "Cancelled";
        }
    }

    public static APIService getFCMClient() {
        return FCMRetrofitClient.getClient(fcmUrl).create(APIService.class);
    }

    public static boolean checkFavorite(int id) {
        boolean result = false;
        for (FavoriteOnlyId item : currentFavOfRestaurant) {
            if (item.getFoodId() == id) {
                result = true;
            }
        }
        return result;
    }

    public static void removeFavorite(int id) {
        for (FavoriteOnlyId item : currentFavOfRestaurant) {
            if (item.getFoodId() == id) {
                currentFavOfRestaurant.remove(item);
            }
        }
    }
}
