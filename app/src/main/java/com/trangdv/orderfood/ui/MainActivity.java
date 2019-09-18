package com.trangdv.orderfood.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.trangdv.orderfood.R;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.model.User;
import com.trangdv.orderfood.service.ListenOrder;
import com.trangdv.orderfood.utils.SharedPrefs;

import static com.trangdv.orderfood.ui.LoginActivity.SAVE_USER;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    FragmentManager fragmentManager;
    Toolbar toolbar;
    private TextView txtUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);
        fragmentManager = getSupportFragmentManager();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //get user from share pref
        User user = SharedPrefs.getInstance().get(SAVE_USER, User.class);
        Common.currentUser = user;

        View headerView = navigationView.getHeaderView(0);
        txtUserName = headerView.findViewById(R.id.tv_username);
        txtUserName.setText(Common.currentUser.getName());

        Intent service = new Intent(MainActivity.this, ListenOrder.class);
        startService(service);
        String sFragment = getIntent().getStringExtra("startFragment");
        if (sFragment != null) {
            OrderStatus();
        } else {
            Home();
        }
    }
    public void replace(Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    public void Home() {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }

    public void Cart() {
        /*getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new CartFragment())
                .addToBackStack(null)
                .commit();*/
        Intent intent = new Intent(this, Cart.class);
        startActivity(intent);
    }

    public void OrderStatus() {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, new OrderStatusFragment())
                .commit();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
//                transaction.hide(getSupportFragmentManager().findFragmentById(R.id.fragment_container));
                Home();
                Toast.makeText(MainActivity.this, "menu", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_cart:
//                transaction.hide(getSupportFragmentManager().findFragmentById(R.id.fragment_container));
                Cart();
                Toast.makeText(MainActivity.this, "cart", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_status:
                OrderStatus();
                Toast.makeText(MainActivity.this, "order status", Toast.LENGTH_SHORT).show();
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


}
