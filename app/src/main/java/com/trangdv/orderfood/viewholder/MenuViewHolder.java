package com.trangdv.orderfood.viewholder;

import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener {
    public MenuViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
