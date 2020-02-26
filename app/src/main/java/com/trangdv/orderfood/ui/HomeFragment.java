package com.trangdv.orderfood.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.adapters.MenuAdapter;
import com.trangdv.orderfood.adapters.RestaurantAdapter;
import com.trangdv.orderfood.adapters.SuggestionAdapter;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.model.BannerData;
import com.trangdv.orderfood.model.Category;
import com.trangdv.orderfood.model.Restaurant;
import com.trangdv.orderfood.model.Suggestion;
import com.trangdv.orderfood.model.Token;
import com.trangdv.orderfood.model.eventbus.MenuItemEvent;
import com.trangdv.orderfood.model.eventbus.RestaurantLoadEvent;
import com.trangdv.orderfood.retrofit.IAnNgonAPI;
import com.trangdv.orderfood.retrofit.RetrofitClient;
import com.trangdv.orderfood.ui.food.FoodActivity;
import com.trangdv.orderfood.ui.menu.MenuActivity;
import com.trangdv.orderfood.viewholder.MenuViewHolder;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
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

public class HomeFragment extends Fragment implements SuggestionAdapter.ItemListener, RestaurantAdapter.ItemListener {

    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    FirebaseDatabase database;
    DatabaseReference banner, suggestion;
    RecyclerView recycler_suggestion, recycler_restaurant;

    RecyclerView.LayoutManager layoutManager;
    LinearLayoutManager linearLayoutManager;
    SwipeRefreshLayout refreshLayout;

    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;
    MenuAdapter menuAdapter;
    SuggestionAdapter suggestionAdapter;
    RestaurantAdapter restaurantAdapter;

    List<Suggestion> suggestions = new ArrayList<>();
    List<Restaurant> restaurants = new ArrayList<>();
    BannerViewPager<BannerData, NetViewHolder> mViewPager;
    List<BannerData> banners = new ArrayList<>();
    IndicatorView mIndicatorView;
    RelativeLayout mRlIndicator;
    TextView mTvTitle;
    View layoutRestaurant;
    View layout_suggestion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Menu");
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        findViewById(view);

        init();

        initBanner();
        return view;
    }

    private void init() {
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
    }

    private void findViewById(View view) {

        recycler_suggestion = view.findViewById(R.id.rv_suggestion);
        recycler_restaurant = view.findViewById(R.id.rv_restaurant);
        refreshLayout = view.findViewById(R.id.swr_menu);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        mRlIndicator = view.findViewById(R.id.layout_indicator);
        mViewPager = view.findViewById(R.id.banner_view);
        mTvTitle = view.findViewById(R.id.tv_title);
        mIndicatorView = view.findViewById(R.id.indicator_view);
        layoutRestaurant = view.findViewById(R.id.layout_restaurant);
        layout_suggestion = view.findViewById(R.id.layout_suggestion);
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
        recycler_suggestion.setLayoutManager(linearLayoutManager);
        suggestionAdapter = new SuggestionAdapter(getContext(), suggestions, this);
        recycler_suggestion.setAdapter(suggestionAdapter);

        // load restaurant
        LinearLayoutManager restaurantLayout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recycler_restaurant.setLayoutManager(restaurantLayout);
        restaurantAdapter = new RestaurantAdapter(getContext(), restaurants, this);
        recycler_restaurant.setAdapter(restaurantAdapter);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchData();
            }
        });
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                fetchData();
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.e("HomeFragment", "onDetach: ");
    }

    public void fetchData() {
        banner.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                banners.clear();

                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    BannerData bannerData = dsp.getValue(BannerData.class);
                    bannerData.setKey(dsp.getKey());
                    banners.add(bannerData);
                }

                mViewPager.create(banners);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        suggestion.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                suggestions.clear();

                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    Suggestion suggestion = dsp.getValue(Suggestion.class);
                    suggestion.setKey(dsp.getKey());
                    suggestions.add(suggestion);
                }
                suggestionAdapter.notifyDataSetChanged();
                layout_suggestion.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        fetchRestaurant();

        refreshLayout.setRefreshing(false);
    }

    private void fetchRestaurant() {
        restaurants.clear();
        compositeDisposable.add(
          anNgonAPI.getRestaurant(Common.API_KEY)
                  .subscribeOn(Schedulers.io())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(restaurantModel -> {
                              EventBus.getDefault().post(new RestaurantLoadEvent(true, restaurantModel.getResult()));
                  },
                          throwable -> {
                              EventBus.getDefault().post(new RestaurantLoadEvent(false, throwable.getMessage()));
                          })
        );
    }

    private void initBanner() {
        mViewPager
                .setAutoPlay(true)
                .setIndicatorSlideMode(IndicatorSlideMode.WORM)
                .setInterval(5000)
                .setScrollDuration(1200)
                .setIndicatorRadius(getResources().getDimensionPixelSize(R.dimen.dp_3))
                .setIndicatorView(mIndicatorView)
                .setIndicatorColor(getResources().getColor(R.color.colorOrange), getResources().getColor(R.color.colorPrimary))
                .setHolderCreator(NetViewHolder::new)
                .setOnPageChangeListener(new OnPageChangeListenerAdapter() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        BannerData bannerData = mViewPager.getList().get(position);
                        mTvTitle.setText(bannerData.getName());
                    }
                })
                .setOnPageClickListener(this::onPageClicked);
    }

    private void onPageClicked(int position) {
        BannerData bannerData = mViewPager.getList().get(position);
        Toast.makeText(getContext(), "position:" + position + " " + bannerData.getName(), Toast.LENGTH_SHORT).show();
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
            restaurants.addAll(event.getRestaurantList());
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
        ((MainActivity) getActivity()).navigationView.getMenu().getItem(0).setChecked(true);
        if (mViewPager != null) {
            mViewPager.startLoop();
        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
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
    public void dispatchToFoodDetail(int position) {

    }

    @Override
    public void dispatchToMenuList(int position) {
        Common.currentRestaurant = restaurants.get(position);
        EventBus.getDefault().postSticky(new MenuItemEvent(true, restaurants.get(position)));
        getActivity().startActivity(new Intent(getContext(), MenuActivity.class));
    }
}
