package com.trangdv.orderfood.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.trangdv.orderfood.ui.BaseActivity;
import com.trangdv.orderfood.ui.MainActivity;
import com.trangdv.orderfood.utils.NetworkUtil;

public class InternetConnector extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String status = NetworkUtil.getConnectivityStatusString(context);
        if(status.isEmpty()) {
            status="No Internet Connection";
        }
//        Toast.makeText(context, status, Toast.LENGTH_LONG).show();

        try {
            new MainActivity().showInternetStatus(status);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
