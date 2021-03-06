package com.trangdv.orderfood.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.os.Handler;

import android.view.GestureDetector;
import android.view.MenuItem;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.model.Order;
import com.trangdv.orderfood.model.User;
import com.trangdv.orderfood.receiver.InternetConnector;
import com.trangdv.orderfood.utils.SharedPrefs;

import static com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS;
import static com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL;
import static com.trangdv.orderfood.ui.LoginActivity.SAVE_USER;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, InternetConnector.BroadcastListener {

    FragmentManager fragmentManager;
    Toolbar toolbar;
    private TextView txtUserName;
    private TextView tvStatus;
    String sFragment = null;
    NavigationView navigationView;
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
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);
        fragmentManager = getSupportFragmentManager();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //get user from share pref
        User user = SharedPrefs.getInstance().get(SAVE_USER, User.class);
        Common.currentUser = user;

        final View headerView = navigationView.getHeaderView(0);
        txtUserName = headerView.findViewById(R.id.tv_username);
        txtUserName.setText(Common.currentUser.getName());

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

    public void showBottomSheet(int position, Order order) {
        new ClickItemCartDialog(position, order).show(getSupportFragmentManager(), "dialog");
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
        navigationView.getMenu().getItem(0).setChecked(true);
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


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (doubleBackToExitPressedOnce) {
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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

    @SuppressWarnings("StatementWithEmptyBody")
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

            case R.id.nav_cart:
                if (item.isChecked()) item.setChecked(false);
                else {
                    Cart();
//                    Toast.makeText(MainActivity.this, "carts", Toast.LENGTH_SHORT).show();
                }
                item.setChecked(true);
                break;

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
                SharedPrefs.getInstance().clear();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(InternetReceiver);
    }

    @Override
    public void updateUI(String status) {
        showInternetStatus(status);
    }
}
