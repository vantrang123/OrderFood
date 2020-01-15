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
        View view = mInflater.inflate(R.layout.food_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Locale locale = new Locale("vi", "VN");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
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
        public ImageView ivLike;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.food_img);
            tvNameFood = itemView.findViewById(R.id.food_name);
            tvPriceFood = itemView.findViewById(R.id.food_price);
            tvDiscountFood = itemView.findViewById(R.id.food_discount);
            ivLike = itemView.findViewById(R.id.iv_heart);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.dispatchToFoodDetail(getLayoutPosition());
                }
            });

            ivLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "like", Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    public interface ItemListener {
        void dispatchToFoodDetail(int position);
    }
}
