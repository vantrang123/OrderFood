package com.trangdv.orderfood.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.trangdv.orderfood.R;

import com.trangdv.orderfood.model.Food;
import com.trangdv.orderfood.viewholder.FoodViewHolder;


public class FoodActivity extends AppCompatActivity {

    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

    FragmentManager fragmentManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);
        fragmentManager = getSupportFragmentManager();

        /*fragmentManager.beginTransaction()
                .replace(R.id.fragment_container_food, new FoodListFragment())
                .commit();*/
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container_food, new TestFoodListFragment())
                .commit();
    }

    void replace(Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container_food, fragment)
                .addToBackStack(null)
                .commit();
    }
}
