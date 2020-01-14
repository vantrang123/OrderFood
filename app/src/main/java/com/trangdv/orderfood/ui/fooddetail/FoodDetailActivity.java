package com.trangdv.orderfood.ui.fooddetail;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.trangdv.orderfood.R;

public class FoodDetailActivity extends AppCompatActivity {
    FragmentManager fragmentManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);
        fragmentManager = getSupportFragmentManager();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle!= null) {
            String productId = bundle.getString("productId", "");
            replace(FoodDetailFragment.newInstance(productId));
        } else {
            onBackPressed();
        }
    }

    void replace(Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container_food_detail, fragment)
                .commit();
    }
}
