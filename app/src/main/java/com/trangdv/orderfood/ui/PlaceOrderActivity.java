package com.trangdv.orderfood.ui;

import android.content.Intent;
import android.os.Bundle;
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
import com.trangdv.orderfood.database.LocalCartDataSource;
import com.trangdv.orderfood.model.FCMSendData;
import com.trangdv.orderfood.model.eventbus.SendTotalCashEvent;
import com.trangdv.orderfood.remote.IFCMService;
import com.trangdv.orderfood.retrofit.IAnNgonAPI;
import com.trangdv.orderfood.retrofit.RetrofitClient;
import com.trangdv.orderfood.retrofit.RetrofitFCMClient;
import com.trangdv.orderfood.ui.main.MainActivity;
import com.trangdv.orderfood.utils.DialogUtils;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PlaceOrderActivity extends AppCompatActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;
    CartDataSource cartDataSource;
    DialogUtils dialogUtils;
    IFCMService ifcmService;


    private ImageView ivBack;
    private EditText edtDate;
    private TextView tvProceed, tvPhone, tvPrice, tvUserAddress, tvAddNewAddress;
    private CheckBox ckbDefaultAddress;
    private RadioButton rdiCod, rdiOnlinePayment;
    boolean isSelectedDate = false, isAddNewAddress = false;

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
            compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getFbid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(cartItems -> {
                                compositeDisposable.add(
                                        anNgonAPI.createOrder(Common.API_KEY,
                                                Common.currentUser.getFbid(),
                                                Common.currentUser.getUserPhone(),
                                                Common.currentUser.getName(),
                                                address,
                                                edtDate.getText().toString(),
                                                Common.currentRestaurant.getId(),
                                                "NONE",
                                                true,
                                                Double.valueOf(tvPrice.getText().toString()),
                                                cartItems.size()
                                        )
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(createOrderModel -> {
                                                            if (createOrderModel.isSuccess()) {
                                                                compositeDisposable.add(
                                                                        anNgonAPI.updateOrder(Common.API_KEY,
                                                                                String.valueOf(createOrderModel.getResult().get(0).getOrderNumber()),
                                                                                new Gson().toJson(cartItems)
                                                                        )
                                                                                .subscribeOn(Schedulers.io())
                                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                                .subscribe(updateOrderModel -> {
                                                                                            if (updateOrderModel.isSuccess()) {
                                                                                                cartDataSource.cleanCart(Common.currentUser.getFbid())
                                                                                                        .subscribeOn(Schedulers.io())
                                                                                                        .observeOn(AndroidSchedulers.mainThread())
                                                                                                        .subscribe(new SingleObserver<Integer>() {
                                                                                                            @Override
                                                                                                            public void onSubscribe(Disposable d) {

                                                                                                            }

                                                                                                            @Override
                                                                                                            public void onSuccess(Integer integer) {
                                                                                                                /*Intent intent = new Intent(PlaceOrderActivity.this, MainActivity.class);
                                                                                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                                                startActivity(intent);
                                                                                                                finish();*/
                                                                                                                Map<String,String> dataSend = new HashMap<>();
                                                                                                                dataSend.put(Common.NOTIFI_TITLE, "New Order");
                                                                                                                dataSend.put(Common.NOTIFI_CONTENT, "You have new order" + createOrderModel.getResult().get(0));

                                                                                                                FCMSendData sendData = new FCMSendData(Common.createTopicSender(
                                                                                                                        Common.getTopicChannel(
                                                                                                                                Common.currentRestaurant.getId()
                                                                                                                        )), dataSend);

                                                                                                                compositeDisposable.add(ifcmService.sendNotification(sendData)
                                                                                                                        .subscribeOn(Schedulers.io())
                                                                                                                        .observeOn(AndroidSchedulers.mainThread())
                                                                                                                        .subscribe(fcmResponse -> {
                                                                                                                            Intent intent = new Intent(PlaceOrderActivity.this, MainActivity.class);
                                                                                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                                                            startActivity(intent);
                                                                                                                            finish();
                                                                                                                            dialogUtils.dismissProgress();
                                                                                                                        }, throwable -> {
                                                                                                                            Intent intent = new Intent(PlaceOrderActivity.this, MainActivity.class);
                                                                                                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                                                                            startActivity(intent);
                                                                                                                            finish();
                                                                                                                            dialogUtils.dismissProgress();
                                                                                                                        } )
                                                                                                                );
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
                                                        },
                                                        throwable -> {
                                                            dialogUtils.dismissProgress();
//                                                            Toast.makeText(this, "[ERROR]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                        })
                                );
                            },
                            throwable -> {
                                Toast.makeText(this, "[ERROR]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                dialogUtils.dismissProgress();
                            }));
        }
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
}
