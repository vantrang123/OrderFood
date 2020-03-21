package com.trangdv.orderfood.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.database.CartDataSource;
import com.trangdv.orderfood.database.CartDatabase;
import com.trangdv.orderfood.database.CartItem;
import com.trangdv.orderfood.database.LocalCartDataSource;
import com.trangdv.orderfood.model.FavoriteOnlyId;
import com.trangdv.orderfood.model.Food;
import com.trangdv.orderfood.retrofit.IAnNgonAPI;
import com.trangdv.orderfood.retrofit.RetrofitClient;
import com.trangdv.orderfood.utils.DialogUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class FoodListAdapter extends RecyclerView.Adapter<FoodListAdapter.ViewHolder> {
    private static final String TAG = "FoodListAdapter";
    private LayoutInflater mInflater;
    private List<Food> foods = new ArrayList<>();
    Context context;
    LinearLayoutManager layoutManager;
    ItemListener listener;

    Locale locale;
    NumberFormat fmt;

    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;
    CartDataSource cartDataSource;
    DialogUtils dialogUtils;

    public FoodListAdapter(Context context, List<Food> foods, ItemListener itemListener) {
        super();
        this.context = context;
        this.foods = foods;
        listener = itemListener;
        compositeDisposable = new CompositeDisposable();
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        dialogUtils = new DialogUtils();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mInflater = LayoutInflater.from(parent.getContext());
        View view = mInflater.inflate(R.layout.item_food, parent, false);
        locale = new Locale("vi", "VN");
        fmt = NumberFormat.getCurrencyInstance(locale);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
//        Locale locale = new Locale("vi", "VN");
//        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
//        int price = (Integer.parseInt(foods.get(position).getPrice()));
        double price = foods.get(position).getPrice();

        holder.tvNameFood.setText("Name: " + foods.get(position).getName());
        holder.tvPriceFood.setText("Price: " + fmt.format(price));
        holder.tvDiscountFood.setText("Discount: " + foods.get(position).getDiscount());

        if (foods.get(position).getBitmapImage() == null) {
            Glide.with(context)
                    .asBitmap()
                    .load(foods.get(position).getImage())
                    .centerCrop()
                    .fitCenter()
                    .placeholder(R.drawable.image_default)
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            foods.get(position).setBitmapImage(resource);
                            return false;
                        }
                    })
                    .into(holder.imgFood);
        } else {
            holder.imgFood.setImageBitmap(foods.get(position).getBitmapImage());
        }

        if (Common.currentFav != null && Common.currentFav.size() > 0) {
            if (Common.checkFavorite(foods.get(position).getId())) {
                holder.ivFavorite.setImageResource(R.drawable.ic_favorite_red);
                holder.ivFavorite.setTag(true);
            } else {
                holder.ivFavorite.setImageResource(R.drawable.ic_favorite_gray);
                holder.ivFavorite.setTag(false);
            }
        } else {
            holder.ivFavorite.setTag(false);
        }

    }

    @Override
    public int getItemCount() {
        return foods.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgFood;
        public TextView tvNameFood;
        public TextView tvPriceFood;
        public TextView tvDiscountFood;
        public ImageView ivFavorite, ivCart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.iv_food_image);
            tvNameFood = itemView.findViewById(R.id.tv_food_name);
            tvPriceFood = itemView.findViewById(R.id.tv_food_price);
            tvDiscountFood = itemView.findViewById(R.id.tv_food_discount);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);
            ivCart = itemView.findViewById(R.id.iv_cart);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.dispatchToFoodDetail(getLayoutPosition());
                }
            });

            ivFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ImageView fav = (ImageView)v;
                    if ((Boolean) v.getTag()) {
                        dialogUtils.showProgress(context);
                        removeFavorite(getAdapterPosition(), fav);
                    } else {
                        dialogUtils.showProgress(context);
                        insertFavorite(getAdapterPosition(), fav);
                    }
                }
            });

            ivCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogUtils.showProgress(context);
                    CartItem cartItem = new CartItem();
                    cartItem.setFoodId(foods.get(getLayoutPosition()).getId());
                    cartItem.setFoodName(foods.get(getLayoutPosition()).getName());
                    cartItem.setFoodPrice(foods.get(getLayoutPosition()).getPrice());
                    cartItem.setFoodImage(foods.get(getLayoutPosition()).getImage());
                    cartItem.setFoodQuantity(1);
                    cartItem.setUserPhone(Common.currentUser.getUserPhone());
                    cartItem.setRestaurantId(Common.currentRestaurant.getId());
                    cartItem.setFoodAddon("NOMAL");
                    cartItem.setFoodSize("NOMAL");
                    cartItem.setFoodExtraPrice(0.0);
                    cartItem.setFbid(Common.currentUser.getFbid());

                    compositeDisposable.add(
                            cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> {
                                                Toast.makeText(context, " added to Cart", Toast.LENGTH_SHORT).show();
                                                dialogUtils.dismissProgress();
                                            },
                                            throwable -> {
                                                Toast.makeText(context, "[ADD CART]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                dialogUtils.dismissProgress();
                                            })
                    );
                }
            });
        }
    }

    private void removeFavorite(int adapterPosition, ImageView fav) {
        compositeDisposable.add(
                anNgonAPI.removeFavorite(Common.API_KEY,
                        Common.currentUser.getFbid(),
                        foods.get(adapterPosition).getId(),
                        Common.currentRestaurant.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(favoriteModel -> {
                            if (favoriteModel.isSuccess() && favoriteModel.getMessage().contains("Success")) {
                                fav.setImageResource(R.drawable.ic_favorite_gray);
                                fav.setTag(false);
                                if (Common.currentFav != null) {
                                    Common.removeFa(foods.get(adapterPosition).getId());
                                }
                            }
                            dialogUtils.dismissProgress();
                        }, throwable -> {
                            dialogUtils.dismissProgress();
                        })
        );
    }

    private void insertFavorite(int position, ImageView fav) {
        compositeDisposable.add(
                anNgonAPI.insertFavorite(Common.API_KEY,
                        Common.currentUser.getFbid(),
                        foods.get(position).getId(),
                        Common.currentRestaurant.getId(),
                        Common.currentRestaurant.getName(),
                        foods.get(position).getName(),
                        foods.get(position).getImage(),
                        foods.get(position).getPrice())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(favoriteModel -> {
                            if (favoriteModel.isSuccess() && favoriteModel.getMessage().contains("Success")) {
                                fav.setImageResource(R.drawable.ic_favorite_red);
                                fav.setTag(true);
                                if (Common.currentFav != null) {
                                    Common.currentFav.add(new FavoriteOnlyId(foods.get(position).getId()));
                                }
                            }
                            dialogUtils.dismissProgress();
                        }, throwable -> {
                            Toast.makeText(context, "[ADD FAV]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            dialogUtils.dismissProgress();
                        })
        );
    }

    public void onStop() {
        compositeDisposable.clear();
    }

    public interface ItemListener {
        void dispatchToFoodDetail(int position);
    }
}
