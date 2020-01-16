package com.trangdv.orderfood.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.trangdv.orderfood.utils.NetworkUtil;

public class InternetConnector extends BroadcastReceiver {
    private BroadcastListener listener;

    public InternetConnector(BroadcastListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String status = NetworkUtil.getConnectivityStatusString(context);
        if(status.isEmpty()) {
            status="No Internet Connection";
        }
//        Toast.makeText(context, status, Toast.LENGTH_LONG).show();
//        new MainActivity().showInternetStatus(status);
        listener.updateUI(status);
    }

    public interface BroadcastListener {
        void updateUI(String status);
    }
}
