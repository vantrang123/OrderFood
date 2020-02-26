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
import com.trangdv.orderfood.model.Restaurant;

import java.util.List;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.ViewHolder> {
    private Context context;
    private List<Restaurant> restaurantList;
    private ItemListener itemListener;

    public RestaurantAdapter(Context context, List<Restaurant> restaurants, ItemListener itemListener) {
        this.context = context;
        this.restaurantList = restaurants;
        this.itemListener = itemListener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_restaurant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        bindView(holder, position);
    }

    private void bindView(final ViewHolder holder, final int i) {
        holder.tvRestaurantName.setText(restaurantList.get(i).getName());

        if (restaurantList.get(i).getBitmapImage() == null) {
            Glide.with(context)
                    .asBitmap()
                    .load(restaurantList.get(i).getImage())
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
                            restaurantList.get(i).setBitmapImage(resource);
                            return false;
                        }
                    })
                    .into(holder.ivRestaurantImage);

        } else {
            holder.ivRestaurantImage.setImageBitmap(restaurantList.get(i).getBitmapImage());
        }
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        protected ImageView ivRestaurantImage;
        protected TextView tvRestaurantName;
        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            ivRestaurantImage = itemView.findViewById(R.id.img_restaurant_image);
            tvRestaurantName = itemView.findViewById(R.id.tv_restaurant_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    itemListener.dispatchToMenuList(getAdapterPosition());
                }
            });
        }
    }

    public interface ItemListener {
        void dispatchToMenuList(int position);
    }
}
