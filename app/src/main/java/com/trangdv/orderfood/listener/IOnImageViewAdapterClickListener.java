package com.trangdv.orderfood.listener;

import android.view.View;

public interface IOnImageViewAdapterClickListener {
    void onCaculatePriceListener(View view, int position, boolean isDecrease, boolean isDelete);
}
