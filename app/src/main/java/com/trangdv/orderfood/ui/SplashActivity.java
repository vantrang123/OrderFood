package com.trangdv.orderfood.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.trangdv.orderfood.R;
import com.trangdv.orderfood.model.User;
import com.trangdv.orderfood.utils.SharedPrefs;

public class SplashActivity extends AppCompatActivity {

    protected int TIME_LOADING = 3000;
    public static final String CHECK = "check";
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        onAfterView();
    }

    void onAfterView() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onNext();
            }
        }, TIME_LOADING);
    }

    public void onNext() {
        Intent intent;
        try {
            SharedPrefs.getInstance().getInstance().get(CHECK, User.class).toString();
            Log.e(TAG, SharedPrefs.getInstance().getInstance().get(CHECK, User.class).toString());
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } catch (NullPointerException e) {
            Log.e(TAG, "null");
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
