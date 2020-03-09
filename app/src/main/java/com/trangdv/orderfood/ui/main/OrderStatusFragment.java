package com.trangdv.orderfood.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.adapters.OrderStatusAdapter;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.database.CartDataSource;
import com.trangdv.orderfood.database.CartDatabase;
import com.trangdv.orderfood.database.LocalCartDataSource;
import com.trangdv.orderfood.model.Order;
import com.trangdv.orderfood.model.Request;
import com.trangdv.orderfood.retrofit.IAnNgonAPI;
import com.trangdv.orderfood.retrofit.RetrofitClient;
import com.trangdv.orderfood.utils.DialogUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class OrderStatusFragment extends Fragment implements OrderStatusAdapter.ItemListener {
    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;
    DialogUtils dialogUtils;

    OrderStatusAdapter orderStatusAdapter;
    public RecyclerView rvListOrder;
    public RecyclerView.LayoutManager layoutManager;

    List<Order> orderList = new ArrayList<>();

    /*public static OrderStatusFragment newInstance(String phone) {
        OrderStatusFragment orderStatus = new OrderStatusFragment();
        Bundle bundle = new Bundle();
        bundle.putString("FoodId", phone);
        orderStatus.setArguments(bundle);
        return orderStatus;
    }*/

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orderstatus, container, false);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Order");

        rvListOrder = view.findViewById(R.id.listOrders);
        initView();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init();
        loadOrders();
    }

    private void init() {
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        compositeDisposable = new CompositeDisposable();
//        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());
        dialogUtils = new DialogUtils();

        layoutManager = new LinearLayoutManager(getActivity());
        rvListOrder.setLayoutManager(layoutManager);
        orderStatusAdapter = new OrderStatusAdapter(getContext(), orderList, this);
        rvListOrder.setAdapter(orderStatusAdapter);
    }

    private void initView() {
        layoutManager = new LinearLayoutManager(getActivity());
        rvListOrder.setLayoutManager(layoutManager);

    }

    private void loadOrders() {
        dialogUtils.showProgress(getContext());
        compositeDisposable.add(anNgonAPI.getOrder(Common.API_KEY, Common.currentUser.getFbid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(orderModel -> {
                    if (orderModel.isSuccess()) {
                        if (orderModel.getResult().size() > 0) {
                            orderList.clear();
                            orderList.addAll(orderModel.getResult());
                            orderStatusAdapter.notifyDataSetChanged();

                        }
                    }
                    dialogUtils.dismissProgress();
                        }
                , throwable -> {
                            dialogUtils.dismissProgress();
                            Toast.makeText(getContext(), "[ERROR]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void dispatchToOrderDetail(int position) {

    }
}
