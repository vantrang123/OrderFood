package com.trangdv.orderfood.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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


public class FoodActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference foodList;
    RecyclerView recycler_food;
    RecyclerView.LayoutManager layoutManager;

    String categoryId = "";
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);

        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Foods");

        initView();

        if (getIntent() != null) {
            categoryId = getIntent().getStringExtra("CategoryId");
        }
        if (!categoryId.isEmpty() && categoryId != null) {
            fetchData(categoryId);
        }
    }

    private void fetchData(String categoryId) {
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(Food.class,
                R.layout.food_item,
                FoodViewHolder.class,
                foodList.orderByChild("menuId").equalTo(categoryId)) {
            @Override
            protected void populateViewHolder(FoodViewHolder foodViewHolder, Food food, int i) {
                foodViewHolder.food_name.setText("Name: "+food.getName());
                foodViewHolder.food_price.setText("Price: "+food.getPrice());
                foodViewHolder.food_discount.setText("Discount: "+food.getDiscount());
                Picasso.with(getBaseContext())
                        .load(food.getImage())
                        .into(foodViewHolder.food_image);

                foodViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Toast.makeText(FoodActivity.this, "ok", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };
        recycler_food.setAdapter(adapter);
    }

    private void initView() {
        recycler_food = findViewById(R.id.rv_food);
        layoutManager = new LinearLayoutManager(this);
        recycler_food.setLayoutManager(layoutManager);
    }
    
    
}
