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
import com.trangdv.orderfood.database.Database;
import com.trangdv.orderfood.model.Favorites;
import com.trangdv.orderfood.model.Food;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodListAdapter extends RecyclerView.Adapter<FoodListAdapter.ViewHolder> {
    private static final String TAG = "FoodListAdapter";
    private LayoutInflater mInflater;
    private List<Food> foods = new ArrayList<>();
    Context context;
    LinearLayoutManager layoutManager;
    ItemListener listener;

    Locale locale;
    NumberFormat fmt;
    Database database;

    public FoodListAdapter(Context context, List<Food> foods, ItemListener itemListener) {
        super();
        this.context = context;
        this.foods = foods;
        listener = itemListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mInflater = LayoutInflater.from(parent.getContext());
        View view = mInflater.inflate(R.layout.item_food, parent, false);
        locale = new Locale("vi", "VN");
        fmt = NumberFormat.getCurrencyInstance(locale);
        database = new Database(context);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
//        Locale locale = new Locale("vi", "VN");
//        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        int price = (Integer.parseInt(foods.get(position).getPrice()));

        holder.tvNameFood.setText("Name: " + foods.get(position).getName());
        holder.tvPriceFood.setText("Price: " + fmt.format(price));
        holder.tvDiscountFood.setText("Discount: " + foods.get(position).getDiscount());

        if (foods.get(position).getBitmapImage()==null) {
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

        if (database.isFavourite(foods.get(position).getFoodId(), Common.currentUser.getPhone()))
            holder.ivFavorite.setImageResource(R.drawable.ic_favorite_red);

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
        public ImageView ivFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.iv_food_image);
            tvNameFood = itemView.findViewById(R.id.food_name);
            tvPriceFood = itemView.findViewById(R.id.food_price);
            tvDiscountFood = itemView.findViewById(R.id.food_discount);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.dispatchToFoodDetail(getLayoutPosition());
                }
            });

            ivFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Favorites favorites = new Favorites();
                    favorites.setFoodId(foods.get(getLayoutPosition()).getFoodId());
                    favorites.setFoodName(foods.get(getLayoutPosition()).getName());
                    favorites.setFoodDescription(foods.get(getLayoutPosition()).getDescription());
                    favorites.setFoodImage(foods.get(getLayoutPosition()).getImage());
                    favorites.setFoodMenuId(foods.get(getLayoutPosition()).getMenuId());
                    favorites.setUserPhone(Common.currentUser.getPhone());
                    favorites.setFoodPrice(foods.get(getLayoutPosition()).getPrice());
                    favorites.setFoodDiscount(foods.get(getLayoutPosition()).getDiscount());

                    if (!database.isFavourite(foods.get(getLayoutPosition()).getFoodId(), Common.currentUser.getPhone())) {

                        database.addToFavourites(favorites);
                        ivFavorite.setImageResource(R.drawable.ic_favorite_red);
                        Toast.makeText(context, "" + foods.get(getLayoutPosition()).getName() +
                                " was added to Favourites", Toast.LENGTH_SHORT).show();
                    } else {
                        database.removeFromFavourites(foods.get(getLayoutPosition()).getFoodId(), Common.currentUser.getPhone());
                        ivFavorite.setImageResource(R.drawable.ic_favorite_gray);
                        Toast.makeText(context, "" + foods.get(getLayoutPosition()).getName() +
                                " was removed from Favourites", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    public interface ItemListener {
        void dispatchToFoodDetail(int position);
    }
}
