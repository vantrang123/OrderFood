package com.trangdv.orderfood.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.database.CartDataSource;
import com.trangdv.orderfood.database.CartDatabase;
import com.trangdv.orderfood.database.CartItem;
import com.trangdv.orderfood.database.LocalCartDataSource;
import com.trangdv.orderfood.model.CreateOrder;
import com.trangdv.orderfood.model.CreateOrderModel;
import com.trangdv.orderfood.model.FCMSendData;
import com.trangdv.orderfood.model.eventbus.SendTotalCashEvent;
import com.trangdv.orderfood.presenter.placeorder.IPlaceOrderPresenter;
import com.trangdv.orderfood.presenter.placeorder.PlaceOrderPresenter;
import com.trangdv.orderfood.remote.IFCMService;
import com.trangdv.orderfood.retrofit.IAnNgonAPI;
import com.trangdv.orderfood.retrofit.RetrofitClient;
import com.trangdv.orderfood.retrofit.RetrofitFCMClient;
import com.trangdv.orderfood.ui.main.MainActivity;
import com.trangdv.orderfood.utils.DialogUtils;
import com.trangdv.orderfood.view.IPlaceOrderView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PlaceOrderActivity extends AppCompatActivity implements View.OnClickListener, IPlaceOrderView {
    private static final String TAG = "PlaceOrderActivity";
    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;
    CartDataSource cartDataSource;
    DialogUtils dialogUtils;
    IFCMService ifcmService;
    IPlaceOrderPresenter iPlaceOrderPresenter;

    private SlidrInterface slidr;
    private ImageView ivBack;
    private TextView tvProceed, tvPhone, tvPrice, tvAddNewAddress, tvDate;
    private CheckBox ckbDefaultAddress;
    private RadioButton rdiCod, rdiOnlinePayment, rdiUserAddress, rdiUserLocation;
    public List<CartItem> cartItemList;
    private String foodId, lat = "", lng = "";
    private int restaurantId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);
        findViewById();
        init();
        initView();
        setupDate();
    }

    private void init() {
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        compositeDisposable = new CompositeDisposable();
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());
        dialogUtils = new DialogUtils();
        ifcmService = RetrofitFCMClient.getInstance(Common.fcmUrl).create(IFCMService.class);
        iPlaceOrderPresenter = new PlaceOrderPresenter(this, anNgonAPI, compositeDisposable);
        slidr = Slidr.attach(this);
    }

    private void findViewById() {
        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(this);
        tvDate = findViewById(R.id.tv_date);
        tvProceed = findViewById(R.id.tv_proceed);
        tvProceed.setOnClickListener(this);
        tvPhone = findViewById(R.id.tv_phone);
        tvPrice = findViewById(R.id.tv_price);
        tvAddNewAddress = findViewById(R.id.tv_add_new_address);
        tvAddNewAddress.setOnClickListener(this);
        rdiCod = findViewById(R.id.rdi_cod);
        rdiOnlinePayment = findViewById(R.id.rdi_online_payment);
        rdiUserAddress = findViewById(R.id.rdi_user_addrres);
        rdiUserLocation = findViewById(R.id.rdi_user_location);

    }


    private void initView() {
        tvPhone.setText(Common.currentUser.getUserPhone());
        rdiUserAddress.setText(Common.currentUser.getAddress());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.tv_add_new_address:
                changeAddress();
                break;
            case R.id.tv_proceed:
                setupProceed();
                break;
            default:
                break;
        }
    }

    private void setupProceed() {

        dialogUtils.showProgress(this);
        if (rdiUserLocation.isChecked()) {
            lat = String.valueOf(Common.userLocation.getLatitude());
            lng = String.valueOf(Common.userLocation.getLongitude());
        } else if (rdiUserAddress.isChecked()) {
            lat = "";
            lng = "";
        }
        if (rdiCod.isChecked()) {
            getOrderNumber(false);
        } else if (rdiOnlinePayment.isChecked()) {
            new SweetAlertDialog(this)
                    .setContentText("Hiện tại chưa hỗ trợ thanh toán trực tuyến!")
                    .setTitleText("Opps..")
                    .show();
        }
    }

    private void setupDate() {
        Calendar now = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("'Ngày: 'MM/dd/yy' Lúc: 'HH:mm", Locale.getDefault());
        tvDate.setText(dateFormat.format(now.getTime()));
    }

    private void changeAddress() {
        startActivityForResult(new Intent(this, ProfileActivity.class), Common.REQUEST_CODE_CHANGE_ADDRESS);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    private void getOrderNumber(boolean isOnlinePayment) {
        dialogUtils.showProgress(this);
        if (!isOnlinePayment) {
            String address = rdiUserAddress.getText().toString();
            String date = tvDate.getText().toString();
            Double totalPrice = Double.valueOf(tvPrice.getText().toString());
            /*compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getFbid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(cartItems -> {
                        cartItemSize = cartItems.size();
                        restaurantIds = new ArrayList<>();
                        for (int i = 0; i < cartItems.size(); i++) {

                            int restaurantId = cartItems.get(i).getRestaurantId();
                            if (!restaurantIds.contains(restaurantId)) {
                                restaurantIds.add(restaurantId);
                                for (CartItem cartItem : cartItems) {
                                    if (cartItem.getRestaurantId() == restaurantId)
                                        cartItemList.add(cartItem);
                                }
//                                iPlaceOrderPresenter.createOrder(address, date, totalPrice, cartItemList, restaurantId);
                                createOrder(address, date, totalPrice, cartItemList, restaurantId);
                            }

                        }
                        }, throwable -> {
                        dialogUtils.dismissProgress();
                    }));*/

            compositeDisposable.add(cartDataSource.getItemInCart(foodId, Common.currentUser.getFbid(), restaurantId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(cartItem -> {
                                cartItemList = new ArrayList<>();
                                cartItemList.add(cartItem);
                                iPlaceOrderPresenter.createOrder(address, date, totalPrice, cartItemList, restaurantId, lat, lng);
//                                createOrder(address, date, totalPrice, cartItemList, restaurantId);
                            },
                            throwable -> {
                                dialogUtils.dismissProgress();
                            }));
        }
    }

/*
    private void createOrder(String address, String date, Double totalPrice, List<CartItem> cartItemList, int restaurantId) {
        compositeDisposable.add(
                anNgonAPI.createOrder(Common.API_KEY,
                        Common.currentUser.getFbid(),
                        Common.currentUser.getUserPhone(),
                        Common.currentUser.getName(),
                        address,
                        date,
                        "NONE",
                        true,
                        totalPrice,
                        cartItemList.size(),
                        restaurantId
                )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(createOrderModel -> {
                                    if (createOrderModel.isSuccess()) {
                                        updateOrder(createOrderModel.getResult().get(0).getOrderNumber(), cartItemList);
                                    }
                                },
                                throwable -> {
                                    dialogUtils.dismissProgress();
                                })
        );
    }
*/

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void displayPrice(SendTotalCashEvent event) {
        tvPrice.setText(event.getCash());
        foodId = String.valueOf(event.getFoodId());
        restaurantId = event.getRestaurauntId();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        dialogUtils.dismissProgress();
        EventBus.getDefault().unregister(this);
        super.onStop();
    }


    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    @Override
    public void onCreateOrderSuccess(CreateOrderModel createOrderModel, List<CartItem> cartItems) {
        updateOrder(createOrderModel.getResult().get(0).getOrderNumber(), cartItemList);
    }

    private void updateOrder(int orderNumber, List<CartItem> cartItems) {
        compositeDisposable.add(
                anNgonAPI.updateOrder(Common.API_KEY,
                        String.valueOf(orderNumber),
                        new Gson().toJson(cartItems)
                )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(updateOrderModel -> {
                                    if (updateOrderModel.isSuccess()) {
                                        sendNotificatonToRestaurant(cartItems.get(0).getRestaurantId());

                                        cartDataSource.cleanCart(Common.currentUser.getFbid(), foodId)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new SingleObserver<Integer>() {
                                                    @Override
                                                    public void onSubscribe(Disposable d) {

                                                    }

                                                    @Override
                                                    public void onSuccess(Integer integer) {
                                                        Intent intent = new Intent(PlaceOrderActivity.this, MainActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                        new SweetAlertDialog(PlaceOrderActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                                                .setTitleText(getResources().getString(R.string.title_dialog_order_success))
                                                                .setContentText(getResources().getString(R.string.content_dialog_order_success))
                                                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                                    @Override
                                                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                                        startActivity(intent);
                                                                        finish();
                                                                    }
                                                                })
                                                                .show();
                                                    }

                                                    @Override
                                                    public void onError(Throwable e) {
                                                        dialogUtils.dismissProgress();

                                                    }
                                                });
                                    }

                                },
                                throwable -> {
                                    dialogUtils.dismissProgress();
                                })
        );
    }

    private void sendNotificatonToRestaurant(int i) {
        Map<String, String> dataSend = new HashMap<>();
        dataSend.put(Common.NOTIFI_TITLE, "Đơn hàng mới");
        dataSend.put(Common.NOTIFI_CONTENT, "Bạn có đơn hàng mới" /*+ createOrderModel.getResult().get(0)*/);

        FCMSendData sendData = new FCMSendData(Common.createTopicSender(
                Common.getTopicChannel(
                        i
                )), dataSend);

        compositeDisposable.add(ifcmService.sendNotification(sendData)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fcmResponse -> {
                    dialogUtils.dismissProgress();
                }, throwable -> {
                    dialogUtils.dismissProgress();
                })
        );
    }

    @Override
    public void onCreateOrderError(String message) {
        dialogUtils.dismissProgress();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.REQUEST_CODE_CHANGE_ADDRESS && resultCode == Activity.RESULT_OK && data != null) {
            rdiUserAddress.setText(Common.currentUser.getAddress());
        }
    }
}
