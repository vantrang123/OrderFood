package com.trangdv.orderfood.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.model.Favorite;
import com.trangdv.orderfood.retrofit.IAnNgonAPI;
import com.trangdv.orderfood.retrofit.RetrofitClient;
import com.trangdv.orderfood.utils.DialogUtils;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {
    DialogUtils dialogUtils;
    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;

    private Context context;
    private List<Favorite> favoritesList;
    ItemListener itemListener;
    Locale locale;
    NumberFormat fmt;

    public FavoritesAdapter(Context context, List<Favorite> favoritesList, ItemListener itemListener) {
        this.context = context;
        this.favoritesList = favoritesList;
        this.itemListener = itemListener;
        dialogUtils = new DialogUtils();
        compositeDisposable = new CompositeDisposable();
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_favorite, parent, false);
        locale = new Locale("vi", "VN");
        fmt = NumberFormat.getCurrencyInstance(locale);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Double price = favoritesList.get(position).getPrice();

        holder.tvNameFood.setText("Name: " + favoritesList.get(position).getFoodName());
        holder.tvPriceFood.setText("Price: " + fmt.format(price));

        Glide.with(context)
                .asBitmap()
                .load(favoritesList.get(position).getFoodImage())
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
                        holder.imgFood.setImageBitmap(resource);
                        return false;
                    }
                })
                .into(holder.imgFood);

    }

    @Override
    public int getItemCount() {
        return favoritesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgFood;
        public TextView tvNameFood;
        public TextView tvPriceFood;
        public TextView tvDiscountFood;
        public ImageView ivFavorite;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.iv_food_image);
            tvNameFood = itemView.findViewById(R.id.tv_food_name);
            tvPriceFood = itemView.findViewById(R.id.tv_food_price);
            tvDiscountFood = itemView.findViewById(R.id.tv_food_discount);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemListener.dispatchToFoodDetail(getLayoutPosition(), favoritesList.get(getLayoutPosition()).getFoodId());
                }
            });

            ivFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogUtils.showProgress(context);
                    ImageView fav = (ImageView) v;
                    getRestaurantId(getAdapterPosition(), v, fav);
                }
            });
        }
    }

    private void getRestaurantId(int adapterPosition, View v, ImageView fav) {
        compositeDisposable.add(
                anNgonAPI.getRestaurantId(Common.API_KEY,
                        favoritesList.get(adapterPosition).getFoodId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(restaurantIdModel -> {
                            if (restaurantIdModel.isSuccess()) {
                                int currentRestaurantId = restaurantIdModel.getResult().get(0).getRestaurantId();
                                removeFavorite(adapterPosition, fav, currentRestaurantId);
                            }
                        }, throwable -> {
                            dialogUtils.dismissProgress();
                        }));
    }

    private void removeFavorite(int adapterPosition, ImageView fav, int currentRestaurantId) {
        compositeDisposable.add(
                anNgonAPI.removeFavorite(Common.API_KEY,
                        Common.currentUser.getFbid(),
                        favoritesList.get(adapterPosition).getFoodId(),
                        currentRestaurantId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(favoriteModel -> {
                            if (favoriteModel.isSuccess() && favoriteModel.getMessage().contains("Success")) {
                                fav.setImageResource(R.drawable.ic_favorite_gray);
                                fav.setTag(false);
                                if (Common.currentFav != null) {
                                    Common.removeFav(favoritesList.get(adapterPosition).getFoodId());
                                    favoritesList.remove(adapterPosition);
                                    notifyItemRemoved(adapterPosition);
                                }
                            }
                            dialogUtils.dismissProgress();
                        }, throwable -> {

                            dialogUtils.dismissProgress();
                        })
        );
    }

    public void removeItem(int position) {
        favoritesList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Favorite item, int position){
        favoritesList.add(position,item);
        notifyItemInserted(position);
    }

    public Favorite getItem(int position){
        return favoritesList.get(position);
    }

    public void onStop() {
        compositeDisposable.clear();
    }

    public interface ItemListener {
        void dispatchToFoodDetail(int position, int foodId);
    }
}
