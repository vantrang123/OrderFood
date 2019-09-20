package com.trangdv.orderfood.common;

import com.trangdv.orderfood.model.User;

public class Common {
    public static User currentUser;
    public static final String DELETE = "Delete";

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
}
