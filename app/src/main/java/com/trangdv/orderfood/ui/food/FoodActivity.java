package com.trangdv.orderfood.ui.food;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;
import com.trangdv.orderfood.R;

import com.trangdv.orderfood.model.Food;
import com.trangdv.orderfood.viewholder.FoodViewHolder;


public class FoodActivity extends AppCompatActivity {

    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;
    FragmentManager fragmentManager;
    private SlidrInterface slidr;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);
        slidr = Slidr.attach(this);
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container_food, new FoodListFragment())
                .commit();
    }


    void replace(Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container_food, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.right_to_left);
    }
}
