package com.trangdv.orderfood.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.adapters.FoodListAdapter;
import com.trangdv.orderfood.model.Food;
import com.trangdv.orderfood.ui.fooddetail.FoodDetailFragment;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS;
import static com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL;

public class FoodListFragment extends Fragment implements FoodListAdapter.ItemListener {
    private static final String TAG = "FoodListFragment";
    FirebaseDatabase database;
    DatabaseReference foodList;
    RecyclerView rvListFood;
    RecyclerView.LayoutManager layoutManager;
    FoodListAdapter foodListAdapter;
    String categoryId = "";
    String categoryName = "";
    List<Food> foods = new ArrayList<>();
    Toolbar toolbar;

    private ShimmerFrameLayout mShimmerViewContainer;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_food_list, container, false);
        rvListFood = view.findViewById(R.id.rv_food);

        toolbar = view.findViewById(R.id.toolbar);
        mShimmerViewContainer = view.findViewById(R.id.shimmer_view_container);

        if (getActivity().getIntent() != null) {
            categoryId = getActivity().getIntent().getStringExtra("CategoryId");
            categoryName = getActivity().getIntent().getStringExtra("CategoryName");
            toolbar.setTitle(categoryName);
        }

        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back, getActivity().getTheme()));
        ((FoodActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        AppBarLayout.LayoutParams toolbarLayoutParams = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        toolbarLayoutParams.setScrollFlags(SCROLL_FLAG_SCROLL | SCROLL_FLAG_ENTER_ALWAYS);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Foods");

        initView();


        if (categoryId != null) {
            fetchData(categoryId);
        }

        foodListAdapter = new FoodListAdapter(getContext(), foods, this);
        rvListFood.setAdapter(foodListAdapter);
    }

    private void initView() {
        layoutManager = new LinearLayoutManager(getActivity());
        rvListFood.setLayoutManager(layoutManager);
    }

    private void fetchData(String categoryId) {
        foods.clear();

        foodList.orderByChild("menuId").equalTo(categoryId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    Food food = dsp.getValue(Food.class);
                    food.setKey(dsp.getKey());
                    foods.add(food);
                }
                foodListAdapter.notifyDataSetChanged();
                //
                mShimmerViewContainer.stopShimmerAnimation();
                mShimmerViewContainer.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void dispatchToFoodDetail(int position) {
        ((FoodActivity) getActivity()).replace(FoodDetailFragment.newInstance(foods.get(position).getKey()));
    }

    @Override
    public void onResume() {
        super.onResume();
//        foods.clear();
        mShimmerViewContainer.startShimmerAnimation();
    }

    @Override
    public void onPause() {
        super.onPause();
        mShimmerViewContainer.stopShimmerAnimation();
    }
}
