package com.trangdv.orderfood.common;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;

import com.trangdv.orderfood.R;
import com.trangdv.orderfood.model.Addon;
import com.trangdv.orderfood.model.FavoriteOnlyId;
import com.trangdv.orderfood.model.Order;
import com.trangdv.orderfood.model.Restaurant;
import com.trangdv.orderfood.model.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Common {
    public static final String API_KEY = "1234";
    public static User currentUser;
    public static Restaurant currentRestaurant;
    public static Order currentOrder;
    public static int currentRestaurantId;
    public static final String DELETE = "Delete";
    public static String PHONE_TEXT = "userPhone";
    public static final String fcmUrl = "https://fcm.googleapis.com/";
    public static final String REMENBER_FBID = "REMENBER_FBID";
    public static final String API_KEY_TAG = "API_KEY";
    public static final String NOTIFI_TITLE = "title";
    public static final String NOTIFI_CONTENT = "content";

    public static final String API_ANNGON_ENDPOINT = "http://192.168.1.6:3000";
    //    public static final String API_ANNGON_ENDPOINT = "http://192.168.43.205:3000";
    public static Set<Addon> addonList = new HashSet<>();
    public static List<FavoriteOnlyId> currentFav;
    public static final String SAVE_LOCATION = "save location";

    public static String convertCodeToStatus(int code) {
        switch (code) {
            case 0:
                return "Đã đặt";
            case 1:
                return "Đã chấp nhận";
            case 2:
                return "Đã chấp nhận";
            case 3:
                return "Đã chấp nhận";
            case 4:
                return "Đang giao";
            case 5:
                return "Đã giao";
            case -1:
                return "Đã hủy";
            default:
                return "Đã hủy";
        }
    }

    public static boolean checkFavorite(int id) {
        boolean result = false;
        for (FavoriteOnlyId item : currentFav) {
            if (item.getFoodId() == id) {
                result = true;
            }
        }
        return result;
    }

    public static void removeFav(int id) {
        for (FavoriteOnlyId item : currentFav) {
            if (item.getFoodId() == id) {
                currentFav.remove(item);
            }
        }
    }

    public static void showNotification(Context context, int notiId, String title, String body, Intent intent) {
        PendingIntent pendingIntent = null;
        if (intent != null)
            pendingIntent = PendingIntent.getActivity(context, notiId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        String NOTIFICATION_CHANNEL_ID = "an_ngon";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "An Ngon Notifications", NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.setDescription("An Ngon Client App");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                NOTIFICATION_CHANNEL_ID);

        builder.setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.app_icon));

        if (pendingIntent != null)
            builder.setContentIntent(pendingIntent);
        Notification mNotification = builder.build();
        notificationManager.notify(notiId, mNotification);


    }

    public static String getTopicChannel(int restaurantId) {
        return new StringBuilder("Restaurant_").append(restaurantId).toString();
    }

    public static String createTopicSender(String topicChannel) {
        return new StringBuilder("/topics/").append(topicChannel).toString();
    }

    public static void animateStart(Context context) {
        Activity act = (Activity)context;
        act.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }
    public static void animateFinish(Context context) {
        Activity act = (Activity)context;
        act.overridePendingTransition(0, R.anim.right_to_left);
    }
}
