package com.trangdv.orderfood.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
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
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

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
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PlaceOrderActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener, IPlaceOrderView {
    private static final String TAG = "PlaceOrderActivity";
    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;
    CartDataSource cartDataSource;
    DialogUtils dialogUtils;
    IFCMService ifcmService;
    IPlaceOrderPresenter iPlaceOrderPresenter;


    private ImageView ivBack;
    private EditText edtDate;
    private TextView tvProceed, tvPhone, tvPrice, tvUserAddress, tvAddNewAddress;
    private CheckBox ckbDefaultAddress;
    private RadioButton rdiCod, rdiOnlinePayment;
    boolean isSelectedDate = false, isAddNewAddress = false;
    public List<Integer> restaurantIds;
    public List<CartItem> cartItemList;
    private List<CreateOrder> createOrderList;
    private int cartItemSize;
    private int count = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        findViewById();
        init();
        initView();
    }

    private void init() {
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        compositeDisposable = new CompositeDisposable();
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());
        dialogUtils = new DialogUtils();
        ifcmService = RetrofitFCMClient.getInstance(Common.fcmUrl).create(IFCMService.class);
        iPlaceOrderPresenter = new PlaceOrderPresenter(this, anNgonAPI, compositeDisposable);
    }

    private void findViewById() {
        ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(this);
        edtDate = findViewById(R.id.edt_date);
        edtDate.setOnClickListener(this);
        tvProceed = findViewById(R.id.tv_proceed);
        tvProceed.setOnClickListener(this);
        tvPhone = findViewById(R.id.tv_phone);
        tvPrice = findViewById(R.id.tv_price);
        tvUserAddress = findViewById(R.id.tv_userAddress);
        tvAddNewAddress = findViewById(R.id.tv_add_new_address);
        tvAddNewAddress.setOnClickListener(this);
        ckbDefaultAddress = findViewById(R.id.ckb_default_address);
        rdiCod = findViewById(R.id.rdi_cod);
        rdiOnlinePayment = findViewById(R.id.rdi_online_payment);

    }


    private void initView() {
        tvPhone.setText(Common.currentUser.getUserPhone());
        tvUserAddress.setText(Common.currentUser.getAddress());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.tv_add_new_address:
//                setupCheckBox();
                break;
            case R.id.edt_date:
                setupDate();
                break;
            case R.id.tv_proceed:
                dialogUtils.showProgress(this);
                setupProceed();
                break;
            default:
                break;
        }
    }

    private void setupProceed() {
        if (!isSelectedDate) {
            return;
        }
        if (!isAddNewAddress) {
            if (!ckbDefaultAddress.isChecked()) {
                return;
            } else {
                String dateString = edtDate.getText().toString();
                DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                try {
                    Date orderDate = dateFormat.parse(dateString);
                    Calendar calendar = Calendar.getInstance();
                    Date currentDate = dateFormat.parse(dateFormat.format(calendar.getTime()));
                    if (!DateUtils.isToday(orderDate.getTime())) {
                        if (orderDate.before(currentDate)) {
                            Toast.makeText(this, getResources().getString(R.string.txt_noti_choise_date_valid), Toast.LENGTH_SHORT).show();
                            dialogUtils.dismissProgress();
                            return;
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (rdiCod.isChecked()) {
                getOrderNumber(false);
            } else if (rdiOnlinePayment.isChecked()) {

            }
        }
    }

    private void setupDate() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(PlaceOrderActivity.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show(getSupportFragmentManager(), "DatePickerDialog");
    }

    private void setupCheckBox() {
        isAddNewAddress = true;
        ckbDefaultAddress.setChecked(false);
        View layout_add_new_address = LayoutInflater.from(this)
                .inflate(R.layout.layout_add_new_address, null);
        EditText edtNewAddress = layout_add_new_address.findViewById(R.id.edt_add_new_address);
    }

    private void getOrderNumber(boolean isOnlinePayment) {
        dialogUtils.showProgress(this);
        if (!isOnlinePayment) {
            String address = ckbDefaultAddress.isChecked() ? tvUserAddress.getText().toString() : "???";
            String date = edtDate.getText().toString();
            Double totalPrice = Double.valueOf(tvPrice.getText().toString());
            compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getFbid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(cartItems -> {
                        cartItemSize = cartItems.size();
                        restaurantIds = new ArrayList<>();
                        for (int i = 0; i < cartItems.size(); i++) {
                            cartItemList = new ArrayList<>();
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
                    }));
        }
    }

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
                                })
        );
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void displayPrice(SendTotalCashEvent event) {
        tvPrice.setText(event.getCash());
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        isSelectedDate = true;
        edtDate.setText(new StringBuilder()
                .append(monthOfYear + 1)
                .append("/")
                .append(dayOfMonth)
                .append("/")
                .append(year));
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
    public void onCreateOrderSuccess(CreateOrderModel createOrderModel, List<CartItem> cartItems) {
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

                                        cartDataSource.cleanCart(Common.currentUser.getFbid())
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new SingleObserver<Integer>() {
                                                    @Override
                                                    public void onSubscribe(Disposable d) {

                                                    }

                                                    @Override
                                                    public void onSuccess(Integer integer) {
                                                        count++;
                                                        while (count == restaurantIds.size()) {
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
                                                            break;
                                                        }
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
        dataSend.put(Common.NOTIFI_TITLE, "New Order");
        dataSend.put(Common.NOTIFI_CONTENT, "You have new order" /*+ createOrderModel.getResult().get(0)*/);

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

    }
}
