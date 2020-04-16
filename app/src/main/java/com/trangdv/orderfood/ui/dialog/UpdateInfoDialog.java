package com.trangdv.orderfood.ui.dialog;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.ui.ProfileActivity;
import com.trangdv.orderfood.ui.SplashActivity;

import static android.app.Activity.RESULT_OK;

public class UpdateInfoDialog extends DialogFragment implements View.OnClickListener {
    String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;

    private TextView tvYes, tvNo;
    private TextInputEditText edtName, edtAddress;
    private String name, address;

    public UpdateInfoDialog(String name, String address) {
        this.name = name;
        this.address = address;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            this.getDialog().setCanceledOnTouchOutside(true);
        }
        View view = inflater.inflate(R.layout.dialog_require_turn_on_gps, container, false);
        findViewById(view);

        init();
        return view;
    }

    private void findViewById(View view) {
        tvYes = view.findViewById(R.id.tv_yes);
        tvNo = view.findViewById(R.id.tv_no);
        edtName = view.findViewById(R.id.edt_name);
        edtAddress = view.findViewById(R.id.edt_address);

        tvYes.setOnClickListener(this);
        tvNo.setOnClickListener(this);
    }

    private void init() {
        if (!TextUtils.isEmpty(name))
            edtName.setText(name);
        if (!TextUtils.isEmpty(address))
            edtAddress.setText(address);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_yes:
                ((ProfileActivity)getActivity()).updateUserInfo(edtName.getText().toString().trim(), edtAddress.getText().toString().trim());
                dismiss();
                break;
            case R.id.tv_no:
                dismiss();
                break;
            default:
                break;
        }
    }
}
