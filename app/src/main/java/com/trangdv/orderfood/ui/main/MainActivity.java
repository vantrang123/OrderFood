package com.trangdv.orderfood.ui.main;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

import android.os.Handler;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.trangdv.orderfood.AppConstants;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.adapters.ViewPagerAdapter;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.database.CartDataSource;
import com.trangdv.orderfood.database.CartDatabase;
import com.trangdv.orderfood.database.LocalCartDataSource;
import com.trangdv.orderfood.model.User;
import com.trangdv.orderfood.providers.CustomBadgeProvider;
import com.trangdv.orderfood.receiver.InternetConnector;
import com.trangdv.orderfood.retrofit.IAnNgonAPI;
import com.trangdv.orderfood.retrofit.RetrofitClient;
import com.trangdv.orderfood.ui.ProfileActivity;
import com.trangdv.orderfood.ui.SearchActivity;
import com.trangdv.orderfood.ui.dialog.ClickItemCartDialog;
import com.trangdv.orderfood.utils.GpsUtils;
import com.trangdv.orderfood.utils.SharedPrefs;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;

import static com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS;
import static com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL;
import static com.trangdv.orderfood.ui.LoginActivity.SAVE_USER;


public class MainActivity extends AppCompatActivity
        implements InternetConnector.BroadcastListener, View.OnClickListener {
    private static final String TAG = "MainActivity";

    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;
    CustomBadgeProvider provider;
    CartDataSource cartDataSource;

    FragmentManager fragmentManager;
    Toolbar toolbar;
    private TextView txtUserName;
    private TextView tvStatus;
    private BottomNavigation mBottomNavigation;
    private ViewPager viewPager;
    private ImageView ivUser;
    View rlSearch, rlSearchBg;
    String sFragment = null;
    BottomSheetBehavior mBottomSheetBehavior;
    GestureDetector mGestureDetector;

    boolean doubleBackToExitPressedOnce = false;
    private BroadcastReceiver InternetReceiver = null;
    private int subscreensOnTheStack = -1;

    private FusedLocationProviderClient mFusedLocationClient;
    private double wayLatitude = 0.0, wayLongitude = 0.0;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    public boolean isGPS = false;
    public boolean isContinue = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById();
        init();
        initializeBottomNavigation(savedInstanceState);

        toolbar.setTitle("Home");
        setSupportActionBar(toolbar);
        fragmentManager = getSupportFragmentManager();

        //get user from share pref
        User user = SharedPrefs.getInstance().get(SAVE_USER, User.class);
        Common.currentUser = user;

        View bottomSheet = findViewById(R.id.nsv_internet_notify);
        tvStatus = findViewById(R.id.tv_status_internet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setPeekHeight(0);
        initGestureDetector();

        InternetReceiver = new InternetConnector(this);
        broadcastIntent();
        countCart();
    }

    private void init() {
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());
        compositeDisposable = new CompositeDisposable();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10 * 1000); // 10 seconds
        locationRequest.setFastestInterval(5 * 1000); // 5 seconds

        new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
                isGPS = isGPSEnable;
                isContinue = false;
                getLocation();
            }
        });

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        wayLatitude = location.getLatitude();
                        wayLongitude = location.getLongitude();
                        if (!isContinue) {
                            if (getFragmentCurrent() instanceof HomeFragment) {
                                ((HomeFragment) getFragmentCurrent()).requestNearbyRestaurant(wayLatitude, wayLongitude, 10);
                            }

                        } else {
                            Toast.makeText(MainActivity.this, "THREE " + wayLatitude + "," + wayLongitude, Toast.LENGTH_LONG).show();
                        }
                        if (!isContinue && mFusedLocationClient != null) {
                            mFusedLocationClient.removeLocationUpdates(locationCallback);
                        }
                    }
                }
            }
        };

//        viewPagerAdapter = new ViewPagerAdapter(this, 4);
    }

    public void getLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    AppConstants.LOCATION_REQUEST);

        } else {
            if (isContinue) {
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            } else {
                mFusedLocationClient.getLastLocation().addOnSuccessListener(MainActivity.this, location -> {
                    if (location != null) {
                        wayLatitude = location.getLatitude();
                        wayLongitude = location.getLongitude();
                        if (getFragmentCurrent() instanceof HomeFragment) {
                            ((HomeFragment) getFragmentCurrent()).requestNearbyRestaurant(wayLatitude, wayLongitude, 10);
                        }
                    } else {
                        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    }
                });
            }
        }
    }

    private void initializeBottomNavigation(final Bundle savedInstanceState) {
        if (null == savedInstanceState) {
            mBottomNavigation.setDefaultSelectedIndex(0);
            provider = (CustomBadgeProvider) mBottomNavigation.getBadgeProvider();
        }
        mBottomNavigation.setDefaultSelectedIndex(0);
        mBottomNavigation.setMenuItemSelectionListener(new BottomNavigation.OnMenuItemSelectionListener() {
            @Override
            public void onMenuItemSelect(int i, int i1, boolean b) {
                if (b) {
                    mBottomNavigation.getBadgeProvider().remove(i);
                    if (null != viewPager) {
                        viewPager.setCurrentItem(i1);
                    }
                }
            }

            @Override
            public void onMenuItemReselect(int i, int i1, boolean b) {

            }
        });
        mBottomNavigation.setMenuChangedListener(parent -> {
            viewPager.setAdapter(new ViewPagerAdapter(MainActivity.this, parent.getMenuItemCount()));
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(final int position) {
                    if (mBottomNavigation.getSelectedIndex() != position) {
                        mBottomNavigation.setSelectedIndex(position, false);
                    }
                }

                @Override
                public void onPageScrollStateChanged(final int state) { }
            });
        });

    }


    private void findViewById() {
        mBottomNavigation = findViewById(R.id.bottomNavigation);
        toolbar = findViewById(R.id.toolbar);
        rlSearch = findViewById(R.id.rlSearch);
        rlSearchBg = findViewById(R.id.rlSearchBg);
        ivUser = findViewById(R.id.iv_user);
        viewPager = findViewById(R.id.vp_main);

        rlSearchBg.setOnClickListener(this);
        ivUser.setOnClickListener(this);
    }

    private void initGestureDetector() {
        mGestureDetector = new GestureDetector(this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            return true;
                        }
                        return super.onSingleTapConfirmed(e);
                    }
                });
    }

    public void showBottomSheet(int position, int foodId) {
        new ClickItemCartDialog(position, foodId).show(getSupportFragmentManager(), "click item cart dialog");
    }

    public void showInternetStatus(final String status) {
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED && !status.equals("Quay lại trực tuyến")) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            changeStatus(status);
        } else if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            changeStatus(status);
        }
    }

    private void changeStatus(String status) {
        tvStatus.setText(status);
        if (status.equals("Quay lại trực tuyến")) {
            tvStatus.setBackgroundColor(getResources().getColor(R.color.colorGreen));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    closeBottomSheet();
                }
            }, 2000);
        } else {
            tvStatus.setBackgroundColor(getResources().getColor(R.color.description));
        }
    }

    private void closeBottomSheet() {
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    public Fragment getFragmentCurrent() {
        return getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.vp_main + ":" + viewPager.getCurrentItem());
    }

    private void countCart() {
        cartDataSource.countCart(Common.currentUser.getFbid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        /*if (!(getFragmentCurrent() instanceof CartFragment)) {
                            if (integer != 0)
                                provider.show(R.id.nav_cart, integer);
                        }*/
                        if (integer != 0)
                            provider.show(R.id.nav_cart, integer);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finishAffinity();
            System.exit(0);
            return;
        } else {
            Fragment fragment = getFragmentCurrent();

            if (!(fragment instanceof HomeFragment)) {
                viewPager.setCurrentItem(0);
                mBottomNavigation.setSelectedIndex(0, false);
            } else {
                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            }
        }
    }

    public void broadcastIntent() {
        registerReceiver(InternetReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onStart() {
        super.onStart();
//        broadcastIntent();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        unregisterReceiver(InternetReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        countCart();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
        unregisterReceiver(InternetReceiver);
    }

    @Override
    public void updateUI(String status) {
        showInternetStatus(status);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlSearchBg:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                break;
            case R.id.iv_user:
                Intent intent1 = new Intent(this, ProfileActivity.class);
                startActivity(intent1);
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppConstants.GPS_REQUEST) {
                Log.d(TAG, "onActivityResult : " + "GPSsssssss");
                isContinue = false;
                getLocation();
            }
        }
    }
}
