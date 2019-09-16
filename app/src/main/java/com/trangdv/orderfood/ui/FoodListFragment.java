package com.trangdv.orderfood.ui;

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

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.listener.ItemClickListener;
import com.trangdv.orderfood.model.Food;
import com.trangdv.orderfood.viewholder.FoodViewHolder;

public class FoodListFragment extends Fragment {

    FirebaseDatabase database;
    DatabaseReference foodList;
    RecyclerView recycler_food;
    RecyclerView.LayoutManager layoutManager;

    String categoryId = "";
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_food_list, container, false);
        recycler_food = view.findViewById(R.id.rv_food);
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
        recycler_food.setLayoutManager(layoutManager);
    }

    private void fetchData(String categoryId) {
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(Food.class,
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
        recycler_food.setAdapter(adapter);
    }
}
