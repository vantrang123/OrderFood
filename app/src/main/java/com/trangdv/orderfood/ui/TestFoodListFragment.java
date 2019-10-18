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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.adapters.FoodListAdapter;
import com.trangdv.orderfood.model.Food;

import java.util.ArrayList;
import java.util.List;

public class TestFoodListFragment extends Fragment implements FoodListAdapter.ItemListener {
    private static final String TAG = "TestFoodListFragment";
    FirebaseDatabase database;
    DatabaseReference foodList;
    RecyclerView rvListFood;
    RecyclerView.LayoutManager layoutManager;
    FoodListAdapter foodListAdapter;
    String categoryId = "";
    String foodId;
    //FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;
    List<String> foodIds = new ArrayList<>();
    List<Food> foods = new ArrayList<>();
    Toolbar toolbar;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_food_list, container, false);
        rvListFood = view.findViewById(R.id.rv_food);

        this.toolbar = ((FoodActivity) getActivity()).toolbar;
        ((FoodActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FoodActivity) getActivity()).finish();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Foods");

        initView();

        if (getActivity().getIntent() != null) {
            categoryId = getActivity().getIntent().getStringExtra("CategoryId");
        }
        if (!categoryId.isEmpty() && categoryId != null) {
            fetchData(categoryId);

        }
    }

    private void initView() {
        layoutManager = new LinearLayoutManager(getActivity());
        rvListFood.setLayoutManager(layoutManager);
    }

    private void fetchData(String categoryId) {
        /*adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodList.orderByChild("menuId").equalTo(categoryId)) {
            @Override
            protected void populateViewHolder(FoodViewHolder foodViewHolder, final Food food,final int i) {
                foodViewHolder.food_name.setText("Name: " + food.getName());
                foodViewHolder.food_price.setText("Price: " + food.getPrice());
                foodViewHolder.food_discount.setText("Discount: " + food.getDiscount());
                Picasso.with(getContext())
                        .load(food.getImage())
                        .into(foodViewHolder.food_image);

                foodViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Toast.makeText(getContext(), adapter.getRef(i).getKey(), Toast.LENGTH_SHORT).show();
                        ((FoodActivity) getActivity()).replace(FoodDetailFragment.newInstance(adapter.getRef(i).getKey()));
                    }
                });
            }
        };
        rvListFood.setAdapter(adapter);*/
        foodList.orderByChild("menuId").equalTo(categoryId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    foodId = dsp.getKey();
                    foodIds.add(foodId);
                    Food food = dsp.getValue(Food.class);
                    foods.add(food);
                }
                viewData(foods);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        /*foodList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //List<String> lst = new ArrayList<String>();

                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    Food food = dsp.getValue(Food.class);
                    foods.add(food);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
    }

    private void viewData(List<Food> foodList) {
        foodListAdapter = new FoodListAdapter(getContext(), foodList, this);
        rvListFood.setAdapter(foodListAdapter);

    }

    @Override
    public void dispatchToFoodDetail(int position) {
        ((FoodActivity) getActivity()).replace(FoodDetailFragment.newInstance(foodIds.get(position)));
    }

    @Override
    public void onResume() {
        super.onResume();
        foodIds.clear();
        foods.clear();
    }
}
