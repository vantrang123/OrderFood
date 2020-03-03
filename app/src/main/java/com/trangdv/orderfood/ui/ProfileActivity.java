package com.trangdv.orderfood.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.trangdv.orderfood.R;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.ui.dialog.ConfirmLogoutDialog;
import com.trangdv.orderfood.utils.SharedPrefs;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvUserName, tvUserPhone, tvEdit, tvLogout;
    private ImageView ivBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        findViewById();

        setData();
    }

    private void setData() {
        tvUserName.setText(Common.currentUser.getName());
        tvUserPhone.setText(Common.currentUser.getUserPhone());
    }

    private void findViewById() {
        tvEdit = findViewById(R.id.tv_edit);
        tvUserName = findViewById(R.id.tv_userName);
        tvUserPhone = findViewById(R.id.tv_userPhone);
        tvLogout = findViewById(R.id.tv_logout);
        ivBack = findViewById(R.id.iv_back);

        tvLogout.setOnClickListener(this);
        ivBack.setOnClickListener(this);
    }

    private void confirmLogout() {
        new ConfirmLogoutDialog().show(getSupportFragmentManager(), "confirmlogutdialog");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_logout:
                confirmLogout();
                break;
            case R.id.iv_back:
                finish();
                break;
            default:
                break;
        }
    }
}
