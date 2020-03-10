package com.trangdv.orderfood.ui.dialog;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.trangdv.orderfood.R;
import com.trangdv.orderfood.ui.SplashActivity;

import static android.app.Activity.RESULT_OK;

public class TurnOnGPSDialog extends DialogFragment implements View.OnClickListener {
    String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;

    private TextView tvYes, tvNo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            this.getDialog().setCanceledOnTouchOutside(true);
        }
        View view = inflater.inflate(R.layout.dialog_require_turn_on_gps, container, false);
        findViewById(view);
        return view;
    }

    private void findViewById(View view) {
        tvYes = view.findViewById(R.id.tv_yes);
        tvNo = view.findViewById(R.id.tv_no);

        tvYes.setOnClickListener(this);
        tvNo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_yes:
                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                dismiss();
                break;
            case R.id.tv_no:
                dismiss();
                ((SplashActivity)getActivity()).finish();
                break;
            default:
                break;
        }
    }
}
