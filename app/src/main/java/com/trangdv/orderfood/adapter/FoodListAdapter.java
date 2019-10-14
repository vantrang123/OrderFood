package com.trangdv.orderfood.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.model.Food;

import java.util.ArrayList;
import java.util.List;

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
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvNameFood.setText("Name: " + foods.get(position).getName());
        holder.tvPriceFood.setText("Price: " + foods.get(position).getPrice());
        holder.tvDiscountFood.setText("Discount: " + foods.get(position).getDiscount());
        Picasso.with(context)
                .load(foods.get(position).getImage())
                .into(holder.imgFood);
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFood = itemView.findViewById(R.id.food_img);
            tvNameFood = itemView.findViewById(R.id.food_name);
            tvPriceFood = itemView.findViewById(R.id.food_price);
            tvDiscountFood = itemView.findViewById(R.id.food_discount);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.dispatchToFoodDetail(getLayoutPosition());
                }
            });


        }
    }


    public interface ItemListener{
        void dispatchToFoodDetail(int position);
    }
}
