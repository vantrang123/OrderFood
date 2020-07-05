package com.trangdv.orderfood.ui.main;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.snackbar.Snackbar;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.database.CartDataSource;
import com.trangdv.orderfood.database.CartDatabase;
import com.trangdv.orderfood.database.CartItem;
import com.trangdv.orderfood.database.LocalCartDataSource;
import com.trangdv.orderfood.model.Order;
import com.trangdv.orderfood.adapters.CartAdapter;
import com.trangdv.orderfood.model.eventbus.CaculatePriceEvent;
import com.trangdv.orderfood.model.eventbus.RestaurantLoadEvent;
import com.trangdv.orderfood.model.eventbus.SendTotalCashEvent;
import com.trangdv.orderfood.retrofit.IAnNgonAPI;
import com.trangdv.orderfood.retrofit.RetrofitClient;
import com.trangdv.orderfood.ui.PlaceOrderActivity;
import com.trangdv.orderfood.utils.DialogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CartFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener,
        CartAdapter.ItemListener {
    private static final String TAG = "CartFragment";

    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    DialogUtils dialogUtils;
    CartDataSource cartDataSource;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private final static int LOCATION_PERMISSION_REQUEST = 1001;
    private static int UPDATE_INTERVAL = 1000;
    private static int FASTEST_INTERVAL = 5000;
    private static int DISPLACEMENT = 10;
    private int position;

    private ConstraintLayout constraintLayout;


    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    CartAdapter cartAdapter;
    List<CartItem> cartItemList = new ArrayList<>();
    public List<Integer> restaurantIds;
    private int cartItemSize;

    TextView tvTotalPrice, tvOrder;

    String latitude = "16.000";
    String longitude = "108.000";
    private double shipFee;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestRuntimePermission();
        } else {
            if (checkPlayServices()) {
                buildingGoogleApiClient();
                createLocationRequest();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        constraintLayout = view.findViewById(R.id.container_cart);

        initView(view);

        init();

        return view;
    }

    private void init() {
        cartAdapter = new CartAdapter(cartItemList, getActivity(), this);
        recyclerView.setAdapter(cartAdapter);

        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());
        dialogUtils = new DialogUtils();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
//        loadListFood();
        getAllItemInCart();
//        caculateShipFee();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;
    }

    private void deleteCart(int position) {
        cartItemList.remove(position);

        //refresh
        cartAdapter.notifyItemRemoved(position);
    }

    private void initView(View view) {
        recyclerView = view.findViewById(R.id.rv_cart);

        tvTotalPrice = view.findViewById(R.id.tv_total);
        tvOrder = view.findViewById(R.id.tv_order);
        tvOrder.setEnabled(false);

        tvOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tvOrder.isEnabled()) {
                    CartItem cartItem = cartItemList.get(position);
                    Double total = Double.valueOf(tvTotalPrice.getText().toString()) + Math.round(shipFee*100)/100;
                    EventBus.getDefault().postSticky(new SendTotalCashEvent(total.toString(), cartItem.getFoodId(), cartItem.getRestaurantId()));
                    startActivity(new Intent(getContext(), PlaceOrderActivity.class));
                    getActivity().overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                } else {
                    Toast.makeText(getActivity(), getString(R.string.txt_cart_empty), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getAllItemInCart() {
        cartItemList.clear();
        compositeDisposable.add(
                cartDataSource.getAllCart(Common.currentUser.getFbid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(cartItems -> {
                            if (cartItems.isEmpty()) {
                                tvOrder.setBackground(getResources().getDrawable(R.drawable.bg_button_cart_gray));
                            } else {
                                tvOrder.setBackground(getResources().getDrawable(R.drawable.bg_button_cart));
                                cartItemList.clear();
                                cartItemList.addAll(cartItems);
                                cartAdapter.notifyDataSetChanged();
                            }
                        }, throwable -> {

                        }));
    }

    private void caculateCartTotalPrice() {
        cartDataSource.sumPrice(String.valueOf(cartItemList.get(position).getFoodId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Long aLong) {
                        if (aLong ==0 ){
//                            tvOrder.setBackground(getResources().getDrawable(R.drawable.bg_button_cart_gray));
                        } else {
//                            tvOrder.setBackground(getResources().getDrawable(R.drawable.bg_button_cart));
                        }

                        tvTotalPrice.setText(String.valueOf(aLong));
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e.getMessage().contains("Query returned empty")) {
                            tvTotalPrice.setText("0");
                        }
                    }
                });
    }

    private void changeStatus() {
        /*//Calculate total price
        total = 0;
        for (Order order : carts)
            total += (float) (Integer.parseInt(order.getPrice())) * (Integer.parseInt(order.getQuanlity()));
        Locale locale = new Locale("vi", "VN");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        //
        float tax = (float) (total * 0.06);
        float profit = (float) (total * 0.3);
        total += tax + profit;

        totalPrice = total;
        tvTotalPrice.setText(fmt.format(total));*/
    }


    private void showAlertDialog() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);
        alertDialog.setTitle("One more step!");
        alertDialog.setMessage("Enter your address");

        final EditText edtAddress = new EditText(getActivity());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );

        edtAddress.setLayoutParams(lp);
        alertDialog.setView(edtAddress);
        alertDialog.setIcon(R.drawable.ic_menu_cart);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                /*Request request = new Request(
                        Common.currentUser.getUserPhone(),
                        Common.currentUser.getName(),
                        edtAddress.getText().toString(),
                        tvTotalPrice.getText().toString(),
                        latitude,
                        longitude,
                        carts
                );*/


                dialog.dismiss();
                /*new Database(getActivity().getBaseContext()).cleanCart();
                carts.clear();
                cartAdapter.notifyDataSetChanged();
                changeStatus();*/
                sendOrderStatusToServer();

            }
        });


        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();

    }

//    private void caculateShipFee() {
//        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getFbid())
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(cartItems -> {
//                    cartItemSize = cartItems.size();
//                    restaurantIds = new ArrayList<>();
//                    for (int i = 0; i < cartItems.size(); i++) {
//                        cartItemList = new ArrayList<>();
//                        int restaurantId = cartItems.get(i).getRestaurantId();
//                        if (!restaurantIds.contains(restaurantId)) {
//                            restaurantIds.add(restaurantId);
//                            for (CartItem cartItem : cartItems) {
//                                if (cartItem.getRestaurantId() == restaurantId)
//                                    cartItemList.add(cartItem);
//                            }
////                               getRestaurantById(String.valueOf(restaurantId), cartItemList.get(0).getFoodDiscount());
//                        }
//
//                    }
//                }, throwable -> {
//                    dialogUtils.dismissProgress();
//                }));
//    }

//    private void getRestaurantById(String id, Double discount) {
//        compositeDisposable.add(
//                anNgonAPI.getRestaurantById(Common.API_KEY, id)
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(restaurantModel -> {
//                                    if (restaurantModel.isSuccess()) {
//                                        Location restaurantLocation = new Location("");
//                                        restaurantLocation.setLatitude(restaurantModel.getResult().get(0).getLat().doubleValue());
//                                        restaurantLocation.setLongitude(restaurantModel.getResult().get(0).getLng().doubleValue());
//                                        shipFee += Common.userLocation.distanceTo(restaurantLocation)*discount;
//                                        Log.d(TAG, "getRestaurantById: "+ Math.round(shipFee*100)/100 + "||" +Common.userLocation.distanceTo(restaurantLocation));
//                                    }
//                                    dialogUtils.dismissProgress();
//                                },
//                                throwable -> {
////                                    EventBus.getDefault().post(new RestaurantLoadEvent(false, throwable.getMessage()));
//                                    dialogUtils.dismissProgress();
//                                })
//        );
//    }

    @Override
    public void onResume() {
        super.onResume();
//        ((MainActivity) getActivity()).navigationView.getMenu().getItem(1).setChecked(true);
//        ((MainActivity) getActivity()).setScrollBar(0);
        checkPlayServices();
        getAllItemInCart();
//        caculateShipFee();
//        loadListFood();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void sendOrderStatusToServer() {

    }


    //location
    private void requestRuntimePermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, LOCATION_PERMISSION_REQUEST);
    }

    private boolean checkPlayServices() {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GoogleApiAvailability.getInstance().isUserResolvableError(resultCode)) {
                GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                Toast.makeText(getContext(), "This device does not support Maps!!", Toast.LENGTH_LONG).show();
            }
        }
        return true;
    }

    protected synchronized void buildingGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();

    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestRuntimePermission();
        } else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();

                this.latitude = String.valueOf(latitude);
                this.longitude = String.valueOf(longitude);
                Common.userLocation = mLastLocation;

            } else {
                //Toast.makeText(this,"Cannot retrieve the location!!",Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void showUndoDelete(String name, final int i, final Order deletedItem) {
        Snackbar snackbar = Snackbar
                .make(constraintLayout, name + " removed from carts!", Snackbar.LENGTH_SHORT);
        snackbar.setAction("UNDO", new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                changeStatus();
            }
        });
        snackbar.setActionTextColor(Color.YELLOW);
        snackbar.show();
    }

    public void removeItem(int position) {
//        changeStatus();
        cartDataSource.deleteCart(cartItemList.get(position))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        cartItemList.remove(integer-1);
                        cartAdapter.notifyItemRemoved(integer-1);
                        EventBus.getDefault().postSticky(new CaculatePriceEvent());
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    @Override
    public void showDialogOptions(int position) {
        ((MainActivity) getActivity()).showBottomSheet(position, cartItemList.get(position).getFoodId());
    }

    @Override
    public void enableButonOrder(boolean isEnable, int position) {
        tvOrder.setEnabled(isEnable);
        this.position = position;
        caculateCartTotalPrice();
    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void caculatePrice(CaculatePriceEvent event) {
        if (event!=null) {
            caculateCartTotalPrice();
        }
    }
}
