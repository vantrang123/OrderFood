package com.trangdv.orderfood.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.shape.RoundedCornerTreatment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.adapters.MenuAdapter;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.listener.ItemClickListener;
import com.trangdv.orderfood.model.Category;
import com.trangdv.orderfood.model.Token;
import com.trangdv.orderfood.viewholder.MenuViewHolder;

import com.firebase.ui.database.FirebaseRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class HomeFragment extends Fragment implements MenuAdapter.ItemListener {

    FirebaseDatabase database;
    DatabaseReference category;
    RecyclerView recycler_menu;

    RecyclerView.LayoutManager layoutManager;
    SwipeRefreshLayout refreshLayout;

    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;
    MenuAdapter menuAdapter;
    List<Category> categories = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Menu");
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        recycler_menu = view.findViewById(R.id.rv_menu);
        refreshLayout = view.findViewById(R.id.swr_menu);
        refreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        database = FirebaseDatabase.getInstance();
        category = database.getReference("Categories");

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

        refreshLayout.setRefreshing(false);
    }

    private void updateTokenShipper(String token) {

        DatabaseReference tokens = database.getReference("Tokens");
        Token data = new Token(token, false);
        tokens.child(Common.currentUser.getPhone()).setValue(data);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).navigationView.getMenu().getItem(0).setChecked(true);
    }


    @Override
    public void dispatchToFoodList(int position) {
        Intent intent = new Intent(getActivity(), FoodActivity.class);
        intent.putExtra("CategoryId", categories.get(position).getKey());
        intent.putExtra("CategoryName", categories.get(position).getName());
        startActivity(intent);
    }
}
