package com.trangdv.orderfood.common;

import com.trangdv.orderfood.model.User;
import com.trangdv.orderfood.remote.APIService;
import com.trangdv.orderfood.remote.FCMRetrofitClient;

public class Common {
    public static User currentUser;
    public static final String DELETE = "Delete";
    public static String PHONE_TEXT = "userPhone";
    private static final String fcmUrl="https://fcm.googleapis.com/";

    public static String convertCodeToStatus(String code){

        if(code.equals("0"))
            return "Placed";
        else if(code.equals("1"))
            return "Preparing Orders";
        else if(code.equals("2"))
            return "Shipping";
        else
            return "Delivered";
    }

    public static APIService getFCMClient(){
        return FCMRetrofitClient.getClient(fcmUrl).create(APIService.class);
    }
}
