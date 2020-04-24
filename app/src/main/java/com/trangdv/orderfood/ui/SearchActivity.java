package com.trangdv.orderfood.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.adapters.FoodListAdapter;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.model.Food;
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

public class SearchActivity extends AppCompatActivity implements View.OnClickListener, FoodListAdapter.ItemListener {

    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    DialogUtils dialogUtils;

    List<Food> foodList = new ArrayList<>();

    private RecyclerView rvSearch;
    private RecyclerView.LayoutManager layoutManager;
    private FoodListAdapter foodListAdapter;
    private ImageView ivBack, ivSearch;
    private EditText edtSearch;
    private SlidrInterface slidr;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        findViewById();
        init();
    }

    private void findViewById() {
        rvSearch = findViewById(R.id.rv_search);
        ivBack = findViewById(R.id.iv_back);
        ivSearch = findViewById(R.id.iv_search);
        edtSearch = findViewById(R.id.edt_Search);

        ivSearch.setOnClickListener(this);
        ivBack.setOnClickListener(this);

    }

    private void init() {
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        layoutManager = new LinearLayoutManager(this);
        rvSearch.setLayoutManager(layoutManager);
        foodListAdapter = new FoodListAdapter(SearchActivity.this, foodList, this);
        rvSearch.setAdapter(foodListAdapter);
        dialogUtils = new DialogUtils();
        slidr = Slidr.attach(this);
    }

    private void searchFood(int menuId, String keySearch) {
        dialogUtils.showProgress(this);
        foodList.clear();
        compositeDisposable.add(anNgonAPI.searchFood(Common.API_KEY, keySearch, menuId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(foodModel -> {
                            if (foodModel.isSuccess()) {
                                foodList.addAll(foodModel.getResult());
                                foodListAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(SearchActivity.this, "[SEARCH FOOD RESULT]" + foodModel.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            dialogUtils.dismissProgress();
                        },
                        throwable -> {
                            Toast.makeText(SearchActivity.this, "[SEARCH FOOD]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            dialogUtils.dismissProgress();
                        }
                ));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.iv_search:
                String keySearch = edtSearch.getText().toString().trim();
                if (!TextUtils.isEmpty(keySearch)) {
                    searchFood(1, keySearch);
                }

                break;
            default:
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Override
    public void dispatchToFoodDetail(int position) {
        EventBus.getDefault().postSticky(new FoodDetailEvent(true, foodList.get(position)));
        startActivity(new Intent(this, FoodDetailActivity.class));
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.right_to_left);
    }
}

