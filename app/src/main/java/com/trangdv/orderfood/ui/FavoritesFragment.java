package com.trangdv.orderfood.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trangdv.orderfood.R;
import com.trangdv.orderfood.adapters.FavoritesAdapter;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.database.Database;
import com.trangdv.orderfood.ui.fooddetail.FoodDetailActivity;

public class FavoritesFragment extends Fragment implements FavoritesAdapter.ItemListener {
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
        return view;
    }

    private void initView() {
        layoutManager = new LinearLayoutManager(getActivity());
        rvFavorite.setLayoutManager(layoutManager);
    }

    private void findViewById(View v) {
        rvFavorite = v.findViewById(R.id.rv_favorite);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        favoritesAdapter = new FavoritesAdapter(getContext(), new Database(getContext()).getAllFavorites(Common.currentUser.getUserPhone()), this);
        rvFavorite.setAdapter(favoritesAdapter);
    }

    @Override
    public void dispatchToFoodDetail(int position, String foodId) {
        Intent intent = new Intent(getActivity(), FoodDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("productId", foodId);
//        bundle.putInt("quantity", Integer.valueOf(order.getQuanlity()));
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
