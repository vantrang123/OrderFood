package com.trangdv.orderfood.ui.main;

import android.content.Intent;
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

import com.trangdv.orderfood.R;
import com.trangdv.orderfood.adapters.FavoritesAdapter;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.database.CartItem;
import com.trangdv.orderfood.model.Favorite;
import com.trangdv.orderfood.model.eventbus.FoodDetailEvent;
import com.trangdv.orderfood.retrofit.IAnNgonAPI;
import com.trangdv.orderfood.retrofit.RetrofitClient;
import com.trangdv.orderfood.ui.fooddetail.FoodDetailActivity;
import com.trangdv.orderfood.utils.DialogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class FavoritesFragment extends Fragment implements FavoritesAdapter.ItemListener {
    DialogUtils dialogUtils;
    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    private List<Favorite> favoriteList = new ArrayList<>();
    RecyclerView rvFavorite;
    RecyclerView.LayoutManager layoutManager;
    FavoritesAdapter favoritesAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Favorite");
        findViewById(view);
        initView();
        init();
        return view;
    }



    private void init() {
        dialogUtils = new DialogUtils();
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
    }

    private void initView() {
        layoutManager = new LinearLayoutManager(getActivity());
        rvFavorite.setLayoutManager(layoutManager);
        favoritesAdapter = new FavoritesAdapter(getContext(),favoriteList,this);
        rvFavorite.setAdapter(favoritesAdapter);
    }

    private void findViewById(View v) {
        rvFavorite = v.findViewById(R.id.rv_favorite);
    }

    private void loadFavorite() {
        dialogUtils.showProgress(getContext());

        compositeDisposable.add(
                anNgonAPI.getFavoriteByUser(Common.API_KEY, Common.currentUser.getFbid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(favoriteModel -> {
                            if (favoriteModel.isSuccess()) {
                                favoriteList.clear();
                                favoriteList.addAll(favoriteModel.getResult());
                                favoritesAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(getActivity(), "[GET FAV]" + favoriteModel.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            dialogUtils.dismissProgress();

                        }, throwable -> {
                            Toast.makeText(getActivity(), "[GET FAV]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            dialogUtils.dismissProgress();
                        })
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadFavorite();
    }

    @Override
    public void dispatchToFoodDetail(int position, int foodId) {
        dialogUtils.showProgress(getContext());

        compositeDisposable.add(
                anNgonAPI.getFoodById(Common.API_KEY, foodId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(foodModel -> {
                            dialogUtils.dismissProgress();
                            EventBus.getDefault().postSticky(new FoodDetailEvent(true, foodModel.getResult().get(0)));
                            startActivity(new Intent(getContext(), FoodDetailActivity.class));

                        }, throwable -> {
                            Toast.makeText(getActivity(), "[GET FOOD]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            dialogUtils.dismissProgress();
                        })
        );
    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        if (favoritesAdapter != null) {
            favoritesAdapter.onStop();
        }
        super.onDestroy();
    }
}
