package com.trangdv.orderfood.ui.main;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

import com.trangdv.orderfood.R;
import com.trangdv.orderfood.adapters.OrderStatusAdapter;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.model.Order;
import com.trangdv.orderfood.retrofit.IAnNgonAPI;
import com.trangdv.orderfood.retrofit.RetrofitClient;
import com.trangdv.orderfood.utils.DialogUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class OrderStatusFragment extends Fragment implements OrderStatusAdapter.ItemListener {
    private static final String TAG = "OrderStatusFragment";
    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;
    DialogUtils dialogUtils;

    OrderStatusAdapter orderStatusAdapter;
    public RecyclerView rvListOrder;
    public RecyclerView.LayoutManager layoutManager;
    List<Order> orderList = new ArrayList<>();
    private int maxData = 0;
    boolean isLoading = false;

    LayoutAnimationController layoutAnimationController;

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
//        loadOrders();
        loadMaxOrder();
        initScrollListener();
    }

    private void init() {
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        compositeDisposable = new CompositeDisposable();
//        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());
        dialogUtils = new DialogUtils();

        layoutManager = new LinearLayoutManager(getActivity());
        rvListOrder.setLayoutManager(layoutManager);
    }

    private void initView() {
        layoutManager = new LinearLayoutManager(getActivity());
        rvListOrder.setLayoutManager(layoutManager);
        rvListOrder.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);
    }

    private void loadMoreData() {
        if (orderStatusAdapter.getItemCount() < maxData) {
            int from = orderStatusAdapter.getItemCount() +1;
//            orderList.add(null);
//            orderStatusAdapter.notifyItemInserted(orderList.size() -1);
            orderStatusAdapter.addNull();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadAllOrders(from, from + 3);
                }
            }, 3000);

            orderStatusAdapter.notifyDataSetChanged();
            isLoading = false;
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
                                    loadAllOrders(0, 3);
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

    private void loadAllOrders(int form, int to) {
//        dialogUtils.showProgress(getContext());
        compositeDisposable.add(anNgonAPI.getOrder(Common.API_KEY, Common.currentUser.getFbid(),
                form, to)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(orderModel -> {
                            if (orderModel.isSuccess()) {
                                if (orderModel.getResult().size() > 0) {
                                    if (orderStatusAdapter == null) {
                                        orderList = new ArrayList<>();
                                        orderList = orderModel.getResult();
                                        orderStatusAdapter = new OrderStatusAdapter(getContext(), orderList, this);
                                        rvListOrder.setAdapter(orderStatusAdapter);
                                        rvListOrder.setLayoutAnimation(layoutAnimationController);
                                    } else {
//                                        orderList.remove(orderList.size()-1);
                                        orderStatusAdapter.removeNull();
                                        orderList = orderModel.getResult();
                                        orderStatusAdapter.addItem(orderList);
                                        isLoading = false;
                                    }
                                }
                            } else {
                                orderStatusAdapter.notifyItemRemoved(orderStatusAdapter.getItemCount());
                            }
//                            dialogUtils.dismissProgress();
                        }
                        , throwable -> {
//                            dialogUtils.dismissProgress();
                            Toast.makeText(getContext(), "[ERROR]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
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
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == orderStatusAdapter.getItemCount() -1) {
                        loadMoreData();
                        isLoading = true;
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
