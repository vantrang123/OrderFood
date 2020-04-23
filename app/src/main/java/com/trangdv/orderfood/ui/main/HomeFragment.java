package com.trangdv.orderfood.ui.main;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.adapters.NewFeedAdapter;
import com.trangdv.orderfood.adapters.RestaurantAdapter;
import com.trangdv.orderfood.adapters.SuggestionAdapter;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.listener.ILoadMore;
import com.trangdv.orderfood.model.Food;
import com.trangdv.orderfood.model.FoodModel;
import com.trangdv.orderfood.model.HotFood;
import com.trangdv.orderfood.model.Restaurant;
import com.trangdv.orderfood.model.Suggestion;
import com.trangdv.orderfood.model.Token;
import com.trangdv.orderfood.model.eventbus.FoodDetailEvent;
import com.trangdv.orderfood.model.eventbus.MenuItemEvent;
import com.trangdv.orderfood.model.eventbus.RestaurantLoadEvent;
import com.trangdv.orderfood.presenter.home.HomePresenter;
import com.trangdv.orderfood.presenter.home.IHomePresenter;
import com.trangdv.orderfood.retrofit.IAnNgonAPI;
import com.trangdv.orderfood.retrofit.RetrofitClient;
import com.trangdv.orderfood.ui.fooddetail.FoodDetailActivity;
import com.trangdv.orderfood.utils.DialogUtils;
import com.trangdv.orderfood.ui.menu.MenuActivity;
import com.trangdv.orderfood.utils.GpsUtils;

import com.trangdv.orderfood.view.IHomeView;
import com.trangdv.orderfood.viewholder.NetViewHolder;
import com.zhpan.bannerview.BannerViewPager;
import com.zhpan.bannerview.adapter.OnPageChangeListenerAdapter;
import com.zhpan.bannerview.constants.IndicatorSlideMode;
import com.zhpan.bannerview.indicator.IndicatorView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class HomeFragment extends Fragment implements SuggestionAdapter.ItemListener,
        RestaurantAdapter.ItemListener, NewFeedAdapter.ItemListener, ILoadMore, IHomeView {
    private static final String TAG = "HomeFragment";
    IHomePresenter iHomePresenter;
    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;
    DialogUtils dialogUtils;

    FirebaseDatabase database;
    DatabaseReference banner, suggestion;
    private RecyclerView rvSuggestion, rvRestaurant, rvNewFeed;
    private NestedScrollView nestedScrollView;

    RecyclerView.LayoutManager layoutManagerNewFeed;
    LinearLayoutManager linearLayoutManager;
    SwipeRefreshLayout refreshLayout;

    private SuggestionAdapter suggestionAdapter;
    private RestaurantAdapter restaurantAdapter;
    private NewFeedAdapter newFeedAdapter;

    private List<Suggestion> suggestions = new ArrayList<>();
    private List<Restaurant> restaurantList = new ArrayList<>();
    private List<Food> foodList = new ArrayList<>();
    private BannerViewPager<HotFood, NetViewHolder> mViewPager;
    private List<HotFood> hotFoodList = new ArrayList<>();
    IndicatorView mIndicatorView;
    RelativeLayout mRlIndicator;
    TextView mTvTitle;
    View layoutRestaurant, layoutNewFeed, layout_suggestion, layout_banner;
    private boolean loaded = false;
    private int maxData = 0;

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
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Home");
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        findViewById(view);
        initView();
        init();
        initBanner();
        return view;
    }

    private void findViewById(View view) {
        rvSuggestion = view.findViewById(R.id.rv_suggestion);
        rvRestaurant = view.findViewById(R.id.rv_restaurant);
        rvNewFeed = view.findViewById(R.id.rv_new_feed);
        refreshLayout = view.findViewById(R.id.swr_menu);
        mRlIndicator = view.findViewById(R.id.layout_indicator);
        mViewPager = view.findViewById(R.id.banner_view);
        mTvTitle = view.findViewById(R.id.tv_title);
        mIndicatorView = view.findViewById(R.id.indicator_view);
        layoutRestaurant = view.findViewById(R.id.layout_restaurant);
        layout_suggestion = view.findViewById(R.id.layout_suggestion);
        layout_banner = view.findViewById(R.id.layout_banner);
        layoutNewFeed = view.findViewById(R.id.layout_new_feed);
        nestedScrollView = view.findViewById(R.id.nestedScrollView);
    }

    private void initView() {
        refreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        layoutManagerNewFeed = new LinearLayoutManager(getActivity());
        rvNewFeed.setLayoutManager(layoutManagerNewFeed);
    }

    private void init() {
        compositeDisposable = new CompositeDisposable();
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        iHomePresenter = new HomePresenter(this, anNgonAPI, compositeDisposable);
        dialogUtils = new DialogUtils();
    }

    public void requestNearbyRestaurant(double latitude, double longitude, int distance) {
        dialogUtils.showProgress(getContext());

        compositeDisposable.add(
                anNgonAPI.getNearbyRestaurant(Common.API_KEY, latitude, longitude, distance)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(restaurantModel -> {
                                    if (restaurantModel.isSuccess()) {
                                        restaurantList.clear();
                                        restaurantList.addAll(restaurantModel.getResult());
                                        restaurantAdapter.notifyDataSetChanged();
                                        layoutRestaurant.setVisibility(View.VISIBLE);
                                        loaded = true;
                                    } else {
                                    }

                                    dialogUtils.dismissProgress();
                                },
                                throwable -> {
                                    dialogUtils.dismissProgress();
                                })
        );
    }

    private void loadHotFood() {
        dialogUtils.showProgress(getContext());
        compositeDisposable.add(
                anNgonAPI.getHotFood(Common.API_KEY)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(hotFoodModel -> {
                                    if (hotFoodModel.isSuccess()) {
                                        hotFoodList.clear();
                                        hotFoodList.addAll(hotFoodModel.getResult());
                                        mViewPager.create(hotFoodList);
                                        layout_banner.setVisibility(View.VISIBLE);
                                    } else {
                                    }
                                    dialogUtils.dismissProgress();
                                },
                                throwable -> {
                                    dialogUtils.dismissProgress();
                                })
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database = FirebaseDatabase.getInstance();
        banner = database.getReference("Banner");
        suggestion = database.getReference("Suggestions");

        // token
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(getActivity(),  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                Log.e("newToken",newToken);

//                updateTokenShipper(newToken);
            }
        });

        //suggestion
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvSuggestion.setLayoutManager(linearLayoutManager);
        suggestionAdapter = new SuggestionAdapter(getContext(), suggestions, this);
        rvSuggestion.setAdapter(suggestionAdapter);

        // restaurant near
        LinearLayoutManager restaurantLayout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvRestaurant.setLayoutManager(restaurantLayout);
        restaurantAdapter = new RestaurantAdapter(getContext(), restaurantList, this);
        rvRestaurant.setAdapter(restaurantAdapter);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchNearRestaurant();
                foodList.clear();
                iHomePresenter.getNumOfFood();
                refreshLayout.setRefreshing(false);
                loadHotFood();
            }
        });
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (!loaded) {
                    fetchNearRestaurant();
                    iHomePresenter.getNumOfFood();
                    refreshLayout.setRefreshing(false);
                    loadHotFood();
                } else {
                    showDataLoaded();
                }

            }
        });
    }

    public void scrollToTop() {
        nestedScrollView.fullScroll(View.FOCUS_UP);
        nestedScrollView.scrollTo(0,0);
    }

    private void showDataLoaded() {
        layoutRestaurant.setVisibility(View.VISIBLE);
        restaurantAdapter = new RestaurantAdapter(getContext(), restaurantList, this);
        rvRestaurant.setAdapter(restaurantAdapter);

        layoutNewFeed.setVisibility(View.VISIBLE);
        newFeedAdapter = new NewFeedAdapter(getContext(), foodList, this, nestedScrollView, rvNewFeed);
        rvNewFeed.setAdapter(newFeedAdapter);
        newFeedAdapter.setiLoadMore(this);

        mViewPager.create(hotFoodList);
        layout_banner.setVisibility(View.VISIBLE);

    }

    private void fetchNearRestaurant() {
        if (!((MainActivity)getActivity()).isGPS) {
            new GpsUtils(getContext()).turnGPSOn(new GpsUtils.onGpsListener() {
                @Override
                public void gpsStatus(boolean isGPSEnable) {
                    // turn on GPS
                    ((MainActivity)getActivity()).isGPS = isGPSEnable;
                    ((MainActivity)getActivity()).isContinue = false;
                    ((MainActivity)getActivity()).getLocation();
                }
            });
        } else {
            ((MainActivity)getActivity()).isContinue = false;
            ((MainActivity)getActivity()).getLocation();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.e("HomeFragment", "onDetach: ");
    }

    private void initBanner() {
        mViewPager
                .setAutoPlay(true)
                .setIndicatorSlideMode(IndicatorSlideMode.WORM)
                .setInterval(5000)
                .setScrollDuration(1200)
                .setIndicatorRadius(getResources().getDimensionPixelSize(R.dimen.dp_3))
                .setIndicatorView(mIndicatorView)
                .setIndicatorColor(getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorPrimary))
                .setHolderCreator(NetViewHolder::new)
                .setOnPageChangeListener(new OnPageChangeListenerAdapter() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        HotFood hotFood = mViewPager.getList().get(position);
                        mTvTitle.setText(hotFood.getName());
                    }
                })
                .setOnPageClickListener(this::onPageClicked);
    }

    private void onPageClicked(int position) {
        HotFood hotFood = mViewPager.getList().get(position);
        iHomePresenter.getFoodById(hotFood.getItemId());
    }

    private void updateTokenShipper(String token) {

        DatabaseReference tokens = database.getReference("Tokens");
        Token data = new Token(token, false);
        tokens.child(Common.currentUser.getUserPhone()).setValue(data);
    }

    // listen EventBus
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void processRestaurantLoadEvent(RestaurantLoadEvent event) {
        if (event.isSuccess()) {
            restaurantList.addAll(event.getRestaurantList());
            restaurantAdapter.notifyDataSetChanged();
            layoutRestaurant.setVisibility(View.VISIBLE);

        } else {
            Toast.makeText(getContext(), "[RESTAURANT LOAD]" + event.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
//        ((MainActivity) getActivity()).navigationView.getMenu().getItem(0).setChecked(true);
        if (mViewPager != null) {
            mViewPager.startLoop();
        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        dialogUtils.dismissProgress();
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mViewPager != null) {
            mViewPager.stopLoop();
        }
    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void dispatchToMenuList(int position) {
        Common.currentRestaurant = restaurantList.get(position);
        EventBus.getDefault().postSticky(new MenuItemEvent(true, restaurantList.get(position)));
        getActivity().startActivity(new Intent(getContext(), MenuActivity.class));
        Common.animateStart(getContext());
    }

    @Override
    public void dispatchToFoodDetail(int position) {
        EventBus.getDefault().postSticky(new FoodDetailEvent(true, foodList.get(position)));
        startActivity(new Intent(getContext(), FoodDetailActivity.class));
        Common.animateStart(getContext());
    }

    @Override
    public void onLoadMore() {
        if (newFeedAdapter.getItemCount() < maxData) {
            int from = newFeedAdapter.getItemCount() + 1;
            newFeedAdapter.addNull();
//            getTenItemFood(from, from + 10);
            iHomePresenter.getTenItemFood(from, from + 10);
        }
    }

    @Override
    public void onGetNumFoodSuccess(int num) {
        maxData = num;
        newFeedAdapter = null;
//        getTenItemFood(0, 10);
        iHomePresenter.getTenItemFood(0, 10);
    }

    @Override
    public void onGetTenItemFoodSuccess(FoodModel foodModel) {
        if (foodModel.getResult().size() > 0) {
            if (newFeedAdapter == null) {
                foodList = new ArrayList<>();
                foodList = foodModel.getResult();
                newFeedAdapter = new NewFeedAdapter(getContext(), foodList, this, nestedScrollView, rvNewFeed);
                newFeedAdapter.setiLoadMore(this);
                rvNewFeed.setAdapter(newFeedAdapter);
            } else {
                newFeedAdapter.removeNull();
                foodList = foodModel.getResult();
                newFeedAdapter.addItem(foodList);
                foodList.clear();
                foodList.addAll(newFeedAdapter.getOrderList());
            }
        }
        loaded = true;
        layoutNewFeed.setVisibility(View.VISIBLE);
        newFeedAdapter.setLoaded();
    }

    @Override
    public void onError(String message) {
        newFeedAdapter.notifyItemRemoved(newFeedAdapter.getItemCount());
        newFeedAdapter.setLoaded();
    }

    @Override
    public void onGetFoodSuccess(Food food) {
        EventBus.getDefault().postSticky(new FoodDetailEvent(true, food));
        startActivity(new Intent(getContext(), FoodDetailActivity.class));
        Common.animateStart(getContext());
    }

    @Override
    public void onGetFoodError(String message) {
        Toast.makeText(getActivity(), "[GET FOOD]" + message, Toast.LENGTH_SHORT).show();
        dialogUtils.dismissProgress();
    }
}
