package com.trangdv.orderfood.ui.fooddetail;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.appbar.AppBarLayout;
import com.google.gson.Gson;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.adapters.AddonAdapter;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.database.CartDataSource;
import com.trangdv.orderfood.database.CartDatabase;
import com.trangdv.orderfood.database.CartItem;
import com.trangdv.orderfood.database.LocalCartDataSource;
import com.trangdv.orderfood.model.Food;
import com.trangdv.orderfood.model.Size;
import com.trangdv.orderfood.model.eventbus.AddonEventChange;
import com.trangdv.orderfood.model.eventbus.AddonLoadEvent;
import com.trangdv.orderfood.model.eventbus.FoodDetailEvent;
import com.trangdv.orderfood.model.eventbus.SizeLoadEvent;
import com.trangdv.orderfood.retrofit.IAnNgonAPI;
import com.trangdv.orderfood.retrofit.RetrofitClient;
import com.trangdv.orderfood.utils.DialogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class FoodDetailActivity extends AppCompatActivity {
    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    CartDataSource cartDataSource;
    DialogUtils dialogUtils;
    Food selectedFood;

    TextView tvFoodName, tvFoodPrice, tvFoodDescription, tvCart;
    ImageView imgFoodImage;
    RadioGroup radioGroup;
    RecyclerView rvAddon;
    Toolbar toolbar;
    AppBarLayout appBarLayout;

    Double originalPrice;
    private double sizePrice = 0.0;
    private String sizeSelected;
    private Double addonPrice = 0.0;
    private String foodName;
    private double extraPrice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);
        findViewById();
        init();
        initView();
    }

    private void init() {
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());

        dialogUtils = new DialogUtils();
    }

    private void findViewById() {
        tvFoodName = findViewById(R.id.tv_food_name);
        tvFoodPrice = findViewById(R.id.tv_food_price);
        tvFoodDescription = findViewById(R.id.tv_food_description);
        imgFoodImage = findViewById(R.id.iv_food_image);
        radioGroup = findViewById(R.id.rdi_group_size);
        rvAddon = findViewById(R.id.rv_add_on);
        appBarLayout = findViewById(R.id.appbar_fd);
        toolbar = findViewById(R.id.toolbar);
        tvCart = findViewById(R.id.tv_add_cart);
        tvFoodDescription = findViewById(R.id.tv_food_description);
        tvFoodName = findViewById(R.id.tv_food_name);
        tvFoodPrice = findViewById(R.id.tv_food_price);
        imgFoodImage = findViewById(R.id.iv_food_image);
    }

    private void initView() {
        toolbar.setTitle("Food Detail");
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back, this.getTheme()));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        radioGroup.setOrientation(LinearLayout.HORIZONTAL);

        appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                switch (state) {
                    case COLLAPSED:
                        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                        toolbar.setTitle(foodName);
                        break;
                    case IDLE:
                    case EXPANDED:
                        toolbar.setBackgroundColor(getResources().getColor(R.color.transparent));
                        toolbar.setTitle("");
                        break;
                    default:
                        break;
                }
            }
        });

        tvCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CartItem cartItem = new CartItem();
                cartItem.setFoodId(selectedFood.getId());
                cartItem.setFoodName(selectedFood.getName());
                cartItem.setFoodPrice(selectedFood.getPrice());
                cartItem.setFoodImage(selectedFood.getImage());
                cartItem.setFoodQuantity(1);
                cartItem.setUserPhone(Common.currentUser.getUserPhone());
                cartItem.setRestaurantId(Common.currentRestaurant.getId());
                cartItem.setFoodAddon(new Gson().toJson(Common.addonList));
                cartItem.setFoodSize(sizeSelected);
                cartItem.setFoodExtraPrice(extraPrice);

                compositeDisposable.add(
                        cartDataSource.insertOrReplaceAll(cartItem)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> {
                                            Toast.makeText(FoodDetailActivity.this," added to Cart", Toast.LENGTH_SHORT).show();
                                        },
                                        throwable -> {
                                            Toast.makeText(FoodDetailActivity.this, "[ADD CART]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                        })
                );
            }
        });
    }

    private void setValue(Food food) {
        selectedFood = food;
        foodName = food.getName();
        toolbar.setTitle(food.getName());
        tvFoodName.setText(food.getName());
        originalPrice = food.getPrice();
        tvFoodPrice.setText(String.valueOf(originalPrice));
        tvFoodDescription.setText(food.getDescription());

        if (food.getBitmapImage() == null) {
            Glide.with(this)
                    .asBitmap()
                    .load(food.getImage())
                    .fitCenter()
                    .centerCrop()
                    .placeholder(R.drawable.image_default)
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            food.setBitmapImage(resource);
                            return false;
                        }
                    })
                    .into(imgFoodImage);
        } else {
            imgFoodImage.setImageBitmap(food.getBitmapImage());
        }

        if (food.isSize() && food.isAddon()) {
            compositeDisposable.add(anNgonAPI.getSizeOfFood(Common.API_KEY, food.getId())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(sizeModel -> {
                        EventBus.getDefault().post(new SizeLoadEvent(true, sizeModel.getResult()));

                        // load addon
                        compositeDisposable.add(anNgonAPI.getAddonOfFood(Common.API_KEY, food.getId())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(addonModel -> {
                                    EventBus.getDefault().post(new AddonLoadEvent(true, addonModel.getResult()));
                                    dialogUtils.dismissProgress();
                                }, throwable -> {
                                    dialogUtils.dismissProgress();
                                })
                        );

                    }, throwable -> {
                        dialogUtils.dismissProgress();
                    })
            );

        } else {
            if (food.isSize()) {
                compositeDisposable.add(anNgonAPI.getSizeOfFood(Common.API_KEY, food.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(sizeModel -> {
                            EventBus.getDefault().post(new SizeLoadEvent(true, sizeModel.getResult()));

                        }, throwable -> {

                        })
                );

            }
            if (food.isAddon()) {
                compositeDisposable.add(anNgonAPI.getAddonOfFood(Common.API_KEY, food.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(addonModel -> {
                            EventBus.getDefault().post(new AddonLoadEvent(true, addonModel.getResult()));
                            dialogUtils.dismissProgress();
                        }, throwable -> {
                            dialogUtils.dismissProgress();
                        })
                );
            } else {
                dialogUtils.dismissProgress();
            }
        }

    }

    private void catulatePrice() {
        extraPrice = 0.0;
        double newPrice;

        extraPrice += sizePrice;
        extraPrice += addonPrice;

        newPrice = originalPrice + extraPrice;

        tvFoodPrice.setText(String.valueOf(newPrice));
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void displayFoodDetail(FoodDetailEvent event) {
        if (event.isSuccess()) {
            setValue(event.getFood());
        } else {


        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void displaySize(SizeLoadEvent event) {
        if (event.isSuccess()) {
            for (Size size : event.getSizeList()) {
                RadioButton radioButton = new RadioButton(this);
                radioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            sizePrice = size.getExtraPrice();
                        } else {
                            sizePrice = -size.getExtraPrice();
                        }

                        catulatePrice();
                        sizeSelected = size.getDescription();
                    }
                });

                RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                radioButton.setLayoutParams(params);
                radioButton.setText(size.getDescription());
                radioButton.setTag(size.getExtraPrice());
                radioGroup.addView(radioButton);
            }

        } else {

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void displayAddon(AddonLoadEvent event) {
        if (event.isSuccess()) {
            rvAddon.setHasFixedSize(true);
            rvAddon.setLayoutManager(new LinearLayoutManager(this));
            rvAddon.setAdapter(new AddonAdapter(this, event.getAddonList()));
        } else {

        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void priceChange(AddonEventChange event) {
        if (event.isAdd()) {
            addonPrice += event.getAddon().getExtraPrice();
        } else {
            addonPrice -= event.getAddon().getExtraPrice();
        }

        catulatePrice();
    }

    @Override
    public void onStart() {
        super.onStart();
        dialogUtils.showProgress(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}
