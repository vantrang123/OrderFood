package com.trangdv.orderfood.ui.main;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.LayerDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.os.Handler;

import android.view.GestureDetector;
import android.view.MenuItem;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;

import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.trangdv.orderfood.R;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.database.CartDataSource;
import com.trangdv.orderfood.database.CartDatabase;
import com.trangdv.orderfood.database.LocalCartDataSource;
import com.trangdv.orderfood.model.Food;
import com.trangdv.orderfood.model.Order;
import com.trangdv.orderfood.model.User;
import com.trangdv.orderfood.providers.CustomBadgeProvider;
import com.trangdv.orderfood.receiver.InternetConnector;
import com.trangdv.orderfood.retrofit.IAnNgonAPI;
import com.trangdv.orderfood.retrofit.RetrofitClient;
import com.trangdv.orderfood.ui.ProfileActivity;
import com.trangdv.orderfood.ui.SearchActivity;
import com.trangdv.orderfood.ui.dialog.ClickItemCartDialog;
import com.trangdv.orderfood.ui.dialog.ConfirmLogoutDialog;
import com.trangdv.orderfood.utils.SharedPrefs;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import it.sephiroth.android.library.bottomnavigation.BadgeProvider;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;

import static com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS;
import static com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED;
import static com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED;
import static com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL;
import static com.trangdv.orderfood.ui.LoginActivity.SAVE_USER;


public class MainActivity extends AppCompatActivity
        implements InternetConnector.BroadcastListener, View.OnClickListener {

    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    CustomBadgeProvider provider;
    CartDataSource cartDataSource;

    FragmentManager fragmentManager;
    Toolbar toolbar;
    private TextView txtUserName;
    private TextView tvStatus;
    private BottomNavigation mBottomNavigation;
    private ImageView ivUser;
    View rlSearch, rlSearchBg;
    String sFragment = null;
    //    NavigationView navigationView;
    BottomSheetBehavior mBottomSheetBehavior;
    GestureDetector mGestureDetector;
    HomeFragment homeFragment;
    CartFragment cartFragment;
    OrderStatusFragment orderStatusFragment;
    FavoritesFragment favoritesFragment;

    boolean doubleBackToExitPressedOnce = false;
    private BroadcastReceiver InternetReceiver = null;
    private int subscreensOnTheStack = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById();

        init();


//        getCurrentUser();

        initializeBottomNavigation(savedInstanceState);

        toolbar.setTitle("Home");
        setSupportActionBar(toolbar);
        fragmentManager = getSupportFragmentManager();
        /*DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);*/

        //get user from share pref
        User user = SharedPrefs.getInstance().get(SAVE_USER, User.class);
        Common.currentUser = user;

//        final View headerView = navigationView.getHeaderView(0);
//        txtUserName = headerView.findViewById(R.id.tv_username);
//        txtUserName.setText(Common.currentUser.getName());

        /*Intent service = new Intent(MainActivity.this, ListenOrder.class);
        startService(service);
        sFragment = getIntent().getStringExtra("startFragment");*/
//        if (sFragment != null) {
//            OrderStatus();
//        } else {
//            Home();
//        }
        homeFragment = new HomeFragment();
        orderStatusFragment = new OrderStatusFragment();
        cartFragment = new CartFragment();
        favoritesFragment = new FavoritesFragment();

        View bottomSheet = findViewById(R.id.nsv_internet_notify);
        tvStatus = findViewById(R.id.tv_status_internet);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setPeekHeight(0);
        initGestureDetector();

        InternetReceiver = new InternetConnector(this);
        broadcastIntent();
        Home();

        countCart();
    }

    private void init() {
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());

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
                switch (i1) {
                    case 0:
                        Home();
                        break;
                    case 1:
                        OrderStatus();
                        break;
                    case 2:
                        Cart();
                        provider.remove(i);
                        break;
                    case 3:
                        Favorite();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onMenuItemReselect(int i, int i1, boolean b) {

            }
        });

    }


    private void findViewById() {
        mBottomNavigation = findViewById(R.id.bottomNavigation);
        toolbar = findViewById(R.id.toolbar);
        rlSearch = findViewById(R.id.rlSearch);
        rlSearchBg = findViewById(R.id.rlSearchBg);
        ivUser = findViewById(R.id.iv_user);

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
        new ClickItemCartDialog(position, foodId).show(getSupportFragmentManager(), "dialog");
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

    public void setScrollBar(int i) {
        AppBarLayout.LayoutParams toolbarLayoutParams = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        toolbarLayoutParams.setScrollFlags(i);

    }

    public void Home() {
        //setScrollBar(1);
        setScrollBar(SCROLL_FLAG_SCROLL | SCROLL_FLAG_ENTER_ALWAYS);
        /*fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commit();*/
        replace(homeFragment);
//        navigationView.getMenu().getItem(0).setChecked(true);
    }

    public void Cart() {
//        setScrollBar(0);
        /*fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new CartFragment())
                .addToBackStack(null)
                .commit();*/
        setScrollBar(SCROLL_FLAG_SCROLL | SCROLL_FLAG_ENTER_ALWAYS);
        replace(cartFragment);
    }

    public void OrderStatus() {
//        setScrollBar(1);
        /*setScrollBar(SCROLL_FLAG_SCROLL | SCROLL_FLAG_ENTER_ALWAYS);
        fragmentManager.beginTransaction()
                //.replace(R.id.fragment_container, new OrderStatusFragment())
                .replace(R.id.fragment_container, new OrderStatusFragment())
                .addToBackStack(null)
                .commit();*/
        setScrollBar(SCROLL_FLAG_SCROLL | SCROLL_FLAG_ENTER_ALWAYS);
        replace(orderStatusFragment);
    }

    public void Favorite() {
        setScrollBar(SCROLL_FLAG_SCROLL | SCROLL_FLAG_ENTER_ALWAYS);
        replace(favoritesFragment);
    }


    public Fragment getFragmentCurrent() {
        return getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    void replace(Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null).commit();
        subscreensOnTheStack++;
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
                        provider.show(R.id.nav_cart, integer);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    @Override
    public void onBackPressed() {
        /*DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else */
        if (doubleBackToExitPressedOnce) {
            finishAffinity();
            System.exit(0);
            return;
        } else {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

            if (!(fragment instanceof HomeFragment)) {
                while (subscreensOnTheStack > 0) {
                    subscreensOnTheStack--;
                    getSupportFragmentManager().popBackStackImmediate();
                }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*// Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        // Get the notifications MenuItem and LayerDrawable (layer-list)
        MenuItem item = menu.findItem(R.id.action_search);
        MenuItemCompat.setActionView(item, R.layout.actionbar_badge_layout);
        RelativeLayout notifCount = (RelativeLayout)   MenuItemCompat.getActionView(item);

        TextView tv = (TextView) notifCount.findViewById(R.id.actionbar_notifcation_textview);
        tv.setText("12");

        return super.onCreateOptionsMenu(menu);*/
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*@SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {

            case R.id.nav_home:
                if (item.isChecked()) item.setChecked(false);
                else {
                    Home();
//                    Toast.makeText(MainActivity.this, "menu", Toast.LENGTH_SHORT).show();
                }
                item.setChecked(true);
                break;

            *//*case R.id.nav_status:
                if (item.isChecked()) item.setChecked(false);
                else {
                    Cart();
//                    Toast.makeText(MainActivity.this, "carts", Toast.LENGTH_SHORT).show();
                }
                item.setChecked(true);
                break;*//*

            case R.id.nav_status:
                if (item.isChecked()) item.setChecked(false);
                else {
                    OrderStatus();
//                    Toast.makeText(MainActivity.this, "order status", Toast.LENGTH_SHORT).show();
                }
                item.setChecked(true);
                break;
            case R.id.nav_favorites:
                if (item.isChecked()) item.setChecked(false);
                else {
                    Favorite();
                }
                item.setChecked(true);
                break;

            case R.id.nav_exit:
                ConfirmLogout();
                break;
                *//*SharedPrefs.getInstance().clear();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();*//*
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }*/



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
        countCart();
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
}
