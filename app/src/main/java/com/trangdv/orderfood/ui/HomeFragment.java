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
import com.trangdv.orderfood.adapters.SuggestionAdapter;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.model.BannerData;
import com.trangdv.orderfood.model.Category;
import com.trangdv.orderfood.model.Suggestion;
import com.trangdv.orderfood.model.Token;
import com.trangdv.orderfood.viewholder.MenuViewHolder;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.trangdv.orderfood.viewholder.NetViewHolder;
import com.zhpan.bannerview.BannerViewPager;
import com.zhpan.bannerview.adapter.OnPageChangeListenerAdapter;
import com.zhpan.bannerview.constants.IndicatorSlideMode;
import com.zhpan.bannerview.indicator.IndicatorView;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements MenuAdapter.ItemListener, SuggestionAdapter.ItemListener {


    FirebaseDatabase database;
    DatabaseReference category, banner, suggestion;
    RecyclerView recycler_menu, recycler_suggestion;

    RecyclerView.LayoutManager layoutManager;
    LinearLayoutManager linearLayoutManager;
    SwipeRefreshLayout refreshLayout;

    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;
    MenuAdapter menuAdapter;
    SuggestionAdapter suggestionAdapter;
    List<Category> categories = new ArrayList<>();
    List<Suggestion> suggestions = new ArrayList<>();
    BannerViewPager<BannerData, NetViewHolder> mViewPager;
    List<BannerData> banners = new ArrayList<>();
    IndicatorView mIndicatorView;
    RelativeLayout mRlIndicator;
    TextView mTvTitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Menu");
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recycler_menu = view.findViewById(R.id.rv_menu);
        recycler_suggestion = view.findViewById(R.id.rv_suggestion);
        refreshLayout = view.findViewById(R.id.swr_menu);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        mRlIndicator = view.findViewById(R.id.layout_indicator);
        mViewPager = view.findViewById(R.id.banner_view);
        mTvTitle = view.findViewById(R.id.tv_title);
        mIndicatorView = view.findViewById(R.id.indicator_view);

        initBanner();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database = FirebaseDatabase.getInstance();
        category = database.getReference("Categories");
        banner = database.getReference("Banner");
        suggestion = database.getReference("Suggestions");

        // token
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(getActivity(),  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                Log.e("newToken",newToken);

                updateTokenShipper(newToken);
            }
        });

        //load menu
        layoutManager = new GridLayoutManager(getContext(), 2);
        recycler_menu.setLayoutManager(layoutManager);
        menuAdapter = new MenuAdapter(getContext(),categories, this);
        recycler_menu.setAdapter(menuAdapter);

        //suggestion
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recycler_suggestion.setLayoutManager(linearLayoutManager);
        suggestionAdapter = new SuggestionAdapter(getContext(), suggestions, this);
        recycler_suggestion.setAdapter(suggestionAdapter);

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
        category.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categories.clear();

                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    Category category = dsp.getValue(Category.class);
                    category.setKey(dsp.getKey());
                    categories.add(category);
                }
                menuAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        refreshLayout.setRefreshing(false);
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

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).navigationView.getMenu().getItem(0).setChecked(true);
        if (mViewPager != null) {
            mViewPager.startLoop();
        }
    }


    @Override
    public void dispatchToFoodList(int position) {
        Intent intent = new Intent(getActivity(), FoodActivity.class);
        intent.putExtra("CategoryId", categories.get(position).getKey());
        intent.putExtra("CategoryName", categories.get(position).getName());
        startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mViewPager != null) {
            mViewPager.stopLoop();
        }
    }

    @Override
    public void dispatchToFoodDetail(int position) {

    }
}
