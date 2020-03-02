package com.trangdv.orderfood.ui.menu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.adapters.MenuAdapter;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.database.CartDataSource;
import com.trangdv.orderfood.database.CartDatabase;
import com.trangdv.orderfood.database.LocalCartDataSource;
import com.trangdv.orderfood.model.Category;
import com.trangdv.orderfood.model.User;
import com.trangdv.orderfood.model.eventbus.FoodListEvent;
import com.trangdv.orderfood.model.eventbus.MenuItemEvent;
import com.trangdv.orderfood.retrofit.IAnNgonAPI;
import com.trangdv.orderfood.retrofit.RetrofitClient;
import com.trangdv.orderfood.ui.food.FoodActivity;
import com.trangdv.orderfood.utils.SharedPrefs;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.trangdv.orderfood.ui.LoginActivity.SAVE_USER;

public class MenuActivity extends AppCompatActivity implements MenuAdapter.ItemListener {

    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    CartDataSource cartDataSource;

    private List<Category> categoryList = new ArrayList<>();
    private MenuAdapter menuAdapter;
    private ShimmerFrameLayout mShimmerViewContainer;

    private Toolbar toolbar;
    private RecyclerView rvMenu;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        findViewById();

        init();
        setupToolBar();

        countCartByRestaurant();
    }

    private void findViewById() {
        rvMenu = findViewById(R.id.rv_menu);
        toolbar = findViewById(R.id.toolbar);
        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);
    }

    private void init() {
        layoutManager = new LinearLayoutManager(this);
        rvMenu.setLayoutManager(layoutManager);
        menuAdapter = new MenuAdapter(this, categoryList, this);
        rvMenu.setAdapter(menuAdapter);

        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());

    }

    private void setupToolBar() {
        toolbar.setTitle("Menu");
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back, this.getTheme()));
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void countCartByRestaurant() {
        cartDataSource.countItemInCart(Common.currentUser.getUserPhone(),
                Common.currentRestaurant.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
//                        Toast.makeText(MenuActivity.this, "[COUNT CART]" + String.valueOf(integer), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
//                        Toast.makeText(MenuActivity.this, "[COUNT CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        mShimmerViewContainer.startShimmerAnimation();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        countCartByRestaurant();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mShimmerViewContainer.stopShimmerAnimation();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void loadMenuByRestaurant(MenuItemEvent event) {
        if (event.isSuccess()) {
            toolbar.setTitle(event.getRestaurant().getName());
            // request category by restaurant id
            compositeDisposable.add(
                    anNgonAPI.getCategories(Common.API_KEY, event.getRestaurant().getId())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(menuModel -> {
                                categoryList.addAll(menuModel.getResult());
                                menuAdapter.notifyDataSetChanged();
                                mShimmerViewContainer.stopShimmerAnimation();
                                mShimmerViewContainer.setVisibility(View.GONE);
                            }, throwable -> {
                                Toast.makeText(this, "[GET CATEGORY]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            })
            );


        } else {

        }
    }

    @Override
    public void dispatchToFoodList(int position) {
        EventBus.getDefault().postSticky(new FoodListEvent(true, categoryList.get(position)));
        startActivity(new Intent(this, FoodActivity.class));
    }
}
