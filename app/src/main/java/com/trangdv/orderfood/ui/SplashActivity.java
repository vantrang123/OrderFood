package com.trangdv.orderfood.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.model.User;
import com.trangdv.orderfood.utils.SharedPrefs;

import static com.trangdv.orderfood.ui.LoginActivity.SAVE_USER;

public class SplashActivity extends AppCompatActivity {

    private final static int LOCATION_PERMISSION_REQUEST = 1001;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    public static final String CHECK_ALREADLY_LOGIN = "check already login";
    private static final String TAG = "SplashActivity";

    private LocationRequest mLocationRequest;
    private static int UPDATE_INTERVAL = 1000;
    private static int FASTEST_INTERVAL = 5000;
    private static int DISPLACEMENT = 10;
    protected int TIME_LOADING = 3000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        onAfterView();
    }

    private void permission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestRuntimePermission();
        } else {
            if (checkPlayServices()) {
                createLocationRequest();
            }
        }
    }

    void onAfterView() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                onNext();
                permission();
            }
        }, TIME_LOADING);
    }

    public void onNext() {
        Intent intent;
        int value = Integer.valueOf(SharedPrefs.getInstance().get(CHECK_ALREADLY_LOGIN, Integer.class, 0));
        if (value == 0) {
            intent = new Intent(this, LoginActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }

        startActivity(intent);
        finish();
    }

    private void requestRuntimePermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, LOCATION_PERMISSION_REQUEST);
    }

    private boolean checkPlayServices() {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GoogleApiAvailability.getInstance().isUserResolvableError(resultCode)) {
                GoogleApiAvailability.getInstance().getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                Toast.makeText(this, "This device does not support Maps!!", Toast.LENGTH_LONG).show();
            }
        }
        return true;
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            onNext();
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            onNext();
        }
    }
}
