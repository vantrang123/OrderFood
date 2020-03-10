package com.trangdv.orderfood.ui;

import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.load.engine.Resource;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.model.Restaurant;
import com.trangdv.orderfood.model.eventbus.MenuItemEvent;
import com.trangdv.orderfood.model.eventbus.RestaurantLoadEvent;
import com.trangdv.orderfood.retrofit.IAnNgonAPI;
import com.trangdv.orderfood.retrofit.RetrofitClient;
import com.trangdv.orderfood.ui.menu.MenuActivity;
import com.trangdv.orderfood.utils.DialogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class NearbyRestaurntActivity extends FragmentActivity implements OnMapReadyCallback {

    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;
    DialogUtils dialogUtils;

    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;
    Marker userMarker;
    boolean isFirtLoad = false;

    private GoogleMap mMap;
    private static int UPDATE_INTERVAL = 1000;
    private static int FASTEST_INTERVAL = 5000;
    private static float DISPLACEMENT = 10f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_restaurnt);
        initView();
        init();

    }

    private void initView() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void init() {
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        dialogUtils = new DialogUtils();
        compositeDisposable = new CompositeDisposable();

        builLocationResquest();
        builLocationCallback();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void builLocationResquest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void builLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                currentLocation = locationResult.getLastLocation();
                addMarkerAndMoveCamere(currentLocation);

                if (!isFirtLoad) {
                    isFirtLoad = !isFirtLoad;
                    requestNearbyRestaurant(currentLocation.getLatitude(), currentLocation.getLongitude(), 10);
                }
            }
        };
    }

    private void requestNearbyRestaurant(double latitude, double longitude, int distance) {
        dialogUtils.showProgress(this);

        // Add a marker in Sydney and move the camera
        LatLng yourLocation = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(yourLocation).title("Your Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(yourLocation));

        compositeDisposable.add(
                anNgonAPI.getNearbyRestaurant(Common.API_KEY, latitude, longitude, distance)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(restaurantModel -> {
                                    if (restaurantModel.isSuccess()) {
                                        addRestaurantMarker(restaurantModel.getResult());
                                    } else {

                                    }

                                    dialogUtils.dismissProgress();
                                },
                                throwable -> {
                                    dialogUtils.dismissProgress();
                                })
        );
    }

    private void addRestaurantMarker(List<Restaurant> result) {
        for (Restaurant restaurant : result) {
            mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.restaurant_marker))
                    .position(new LatLng(restaurant.getLat(), restaurant.getLng()))
                    .snippet(new StringBuilder().append(restaurant.getId()).append(".").append(restaurant.getName()).toString()));
        }
    }

    private void addMarkerAndMoveCamere(Location currentLocation) {
        if (userMarker != null) {
            userMarker.remove();
            LatLng userLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            userMarker = mMap.addMarker(new MarkerOptions().position(userLatLng).title(Common.currentUser.getName()));
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(userLatLng, 17);
            mMap.animateCamera(yourLocation);
        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
            if (!success) {
                Log.e("ERROR_MAP", "Load style error");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("ERROR_MAP", "Resources not found");
        }

        mMap.setOnInfoWindowClickListener(marker -> {
            String id = marker.getTitle().substring(0, marker.getTitle().indexOf(","));
            if (!TextUtils.isEmpty(id)) {
                compositeDisposable.add(
                        anNgonAPI.getRestaurantById(Common.API_KEY, id)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(restaurantModel -> {
                                            if (restaurantModel.isSuccess()) {
                                                Common.currentRestaurant = restaurantModel.getResult().get(0);
                                                EventBus.getDefault().postSticky(new MenuItemEvent(true, Common.currentRestaurant));
                                                startActivity(new Intent(this, MenuActivity.class));
                                                finish();
                                            }
                                            dialogUtils.dismissProgress();
                                        },
                                        throwable -> {
                                            EventBus.getDefault().post(new RestaurantLoadEvent(false, throwable.getMessage()));
                                            dialogUtils.dismissProgress();
                                        })
                );
            }
        });
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onDestroy();
    }
}
