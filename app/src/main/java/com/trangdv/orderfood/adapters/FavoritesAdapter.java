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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.model.Favorites;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {
    private Context context;
    private List<Favorites> favoritesList;
    ItemListener itemListener;
    Locale locale;
    NumberFormat fmt;

    public FavoritesAdapter(Context context, List<Favorites> favoritesList, ItemListener itemListener) {
        this.context = context;
        this.favoritesList = favoritesList;
        this.itemListener = itemListener;
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
        int price = (Integer.parseInt(favoritesList.get(position).getFoodPrice()));

        holder.tvNameFood.setText("Name: " + favoritesList.get(position).getFoodName());
        holder.tvPriceFood.setText("Price: " + fmt.format(price));
        holder.tvDiscountFood.setText("Discount: " + favoritesList.get(position).getFoodDiscount());

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
            tvNameFood = itemView.findViewById(R.id.food_name);
            tvPriceFood = itemView.findViewById(R.id.food_price);
            tvDiscountFood = itemView.findViewById(R.id.food_discount);
            ivFavorite = itemView.findViewById(R.id.iv_increase);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemListener.dispatchToFoodDetail(getLayoutPosition(), favoritesList.get(getLayoutPosition()).getFoodId());
                }
            });

            ivFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "like", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void removeItem(int position){
        favoritesList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Favorites item, int position){
        favoritesList.add(position,item);
        notifyItemInserted(position);
    }

    public Favorites getItem(int position){
        return favoritesList.get(position);
    }

    public interface ItemListener {
        void dispatchToFoodDetail(int position, String foodId);
    }
}
