package com.trangdv.orderfood.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.trangdv.orderfood.R;
import com.trangdv.orderfood.adapters.OrderAdapter;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.model.Order;
import com.trangdv.orderfood.retrofit.IAnNgonAPI;
import com.trangdv.orderfood.retrofit.RetrofitClient;
import com.trangdv.orderfood.ui.orderdetail.OrderDetailActivity;
import com.trangdv.orderfood.utils.DialogUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class OrderFragment extends Fragment implements OrderAdapter.ItemListener {
    private static final String TAG = "OrderFragment";
    private static final int REQUEST_CODE_ORDER_DETAIL = 2020;
    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;
    DialogUtils dialogUtils;

    OrderAdapter orderAdapter;
    public RecyclerView rvListOrder;
    public RecyclerView.LayoutManager layoutManager;
    SwipeRefreshLayout refreshLayout;
    List<Order> orderList = new ArrayList<>();
    private int maxData = 0;
    boolean isLoading = false;
    boolean loaded = false;
    private int idItemSelected;

    LayoutAnimationController layoutAnimationController;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (loaded) {
            outState.putBoolean("loaded", true);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            loaded = savedInstanceState.getBoolean("loaded", false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order, container, false);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Order");

        findViewById(view);
        initView();
        return view;
    }

    private void findViewById(View view) {
        rvListOrder = view.findViewById(R.id.listOrders);
        refreshLayout = view.findViewById(R.id.swr_order);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
        initScrollListener();
    }

    private void init() {
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        compositeDisposable = new CompositeDisposable();
//        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());
        dialogUtils = new DialogUtils();

        layoutManager = new LinearLayoutManager(getActivity());
        rvListOrder.setLayoutManager(layoutManager);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                orderList.clear();
                loadMaxOrder();
            }
        });
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (!loaded) {
                    loadMaxOrder();
                } else {
                    showDataLoaded();
                }
            }
        });
    }

    private void showDataLoaded() {
        orderAdapter = new OrderAdapter(getContext(), orderList, this);
        rvListOrder.setAdapter(orderAdapter);
    }

    private void initView() {
        layoutManager = new LinearLayoutManager(getActivity());
        rvListOrder.setLayoutManager(layoutManager);
//        rvListOrder.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
    }

    private void loadMoreData() {
        if (orderAdapter.getItemCount() < maxData) {
            int from = orderAdapter.getItemCount() + 1;
            orderAdapter.addNull();
            loadAllOrders(from, from + 10);
        } else {
        }
    }

    private void loadMaxOrder() {
        dialogUtils.showProgress(getContext());
        compositeDisposable.add(anNgonAPI.getMaxOrder(Common.API_KEY, Common.currentUser.getFbid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(maxOrderModel -> {
                            if (maxOrderModel.isSuccess()) {
                                if (maxOrderModel.getResult().size() > 0) {
                                    maxData = maxOrderModel.getResult().get(0).getMaxRowNum();
                                    orderAdapter = null;
                                    loadAllOrders(0, 10);
                                }
                            }
                            dialogUtils.dismissProgress();
                        }
                        , throwable -> {
                            dialogUtils.dismissProgress();
                            Toast.makeText(getContext(), "[ERROR LOAD MAX ORDER]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));
        refreshLayout.setRefreshing(false);
    }

    private void loadAllOrders(int from, int to) {
        dialogUtils.showProgress(getContext());
        compositeDisposable.add(anNgonAPI.getOrder(Common.API_KEY, Common.currentUser.getFbid(),
                from, to)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(orderModel -> {
                            if (orderModel.isSuccess()) {
                                if (orderModel.getResult().size() > 0) {
                                    if (orderAdapter == null) {
                                        orderList = new ArrayList<>();
                                        orderList = orderModel.getResult();
                                        orderAdapter = new OrderAdapter(getContext(), orderList, this);
                                        rvListOrder.setAdapter(orderAdapter);
                                        rvListOrder.setLayoutAnimation(layoutAnimationController);
                                    } else {
                                        orderAdapter.removeNull();
                                        orderList = orderModel.getResult();
                                        orderAdapter.addItem(orderList);
                                        orderList.addAll(orderAdapter.getOrderList());
                                    }
                                }
                                loaded = true;
                            } else {
                                orderAdapter.notifyItemRemoved(orderAdapter.getItemCount());
                            }
                            dialogUtils.dismissProgress();
                            isLoading = false;
                        }
                        , throwable -> {
                            dialogUtils.dismissProgress();
                            isLoading = false;
                        }
                ));
    }

    private void initScrollListener() {
        rvListOrder.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (!isLoading) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == orderAdapter.getItemCount() -1) {
                        isLoading = true;
                        loadMoreData();

                    }
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        dialogUtils.dismissProgress();
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
        idItemSelected = position;
        startActivityForResult(new Intent(getContext(), OrderDetailActivity.class), REQUEST_CODE_ORDER_DETAIL);
    }
}
