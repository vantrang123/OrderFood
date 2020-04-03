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
import com.trangdv.orderfood.retrofit.IAnNgonAPI;
import com.trangdv.orderfood.retrofit.RetrofitClient;
import com.trangdv.orderfood.ui.dialog.ConfirmLogoutDialog;
import com.trangdv.orderfood.utils.DialogUtils;
import com.trangdv.orderfood.utils.SharedPrefs;

import java.util.zip.DeflaterInputStream;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;
    DialogUtils dialogUtils;

    private TextView tvUserName, tvUserPhone, tvUserAddress,  tvEdit, tvLogout;
        private ImageView ivBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        findViewById();
        init();
        setData();
    }

    private void init() {
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        compositeDisposable = new CompositeDisposable();
        dialogUtils = new DialogUtils();
    }

    private void setData() {
        tvUserName.setText(Common.currentUser.getName());
        tvUserPhone.setText(new StringBuffer("Số điện thoại: ").append(Common.currentUser.getUserPhone()));
        tvUserAddress.setText(new StringBuilder("Địa chỉ: ").append(Common.currentUser.getAddress()));
    }

    private void findViewById() {
        tvEdit = findViewById(R.id.tv_edit);
        tvUserName = findViewById(R.id.tv_userName);
        tvUserPhone = findViewById(R.id.tv_userPhone);
        tvUserAddress = findViewById(R.id.tv_user_address);
        tvLogout = findViewById(R.id.tv_logout);
        ivBack = findViewById(R.id.iv_back);

        tvLogout.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        tvEdit.setOnClickListener(this);
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
                onBackPressed();
                break;
            case R.id.tv_edit:
                dialogUtils.showProgress(this);
                updateUserInfo();
                break;
            default:
                break;
        }
    }

    private void updateUserInfo() {
        compositeDisposable.add(
                anNgonAPI.updateUserInfo(Common.API_KEY,
                        Common.currentUser.getUserPhone(),
                        Common.currentUser.getName(),
                        "",
                        Common.currentUser.getFbid(),
                        Common.currentUser.getPassword())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(updateUserModel -> {
                            if (updateUserModel.isSuccess()) {
                                dialogUtils.dismissProgress();
                            }
                        }, throwable -> {
                            dialogUtils.dismissProgress();
                        } ));
    }

    @Override
    protected void onStop() {
        dialogUtils.dismissProgress();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}
