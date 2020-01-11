package com.trangdv.orderfood.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trangdv.orderfood.R;
import com.trangdv.orderfood.model.Category;

import java.util.ArrayList;
import java.util.List;


public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
    private Context context;
    private List<Category> categories = new ArrayList<>();

    public MenuAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categories = categoryList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.menu_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        bindView(holder, position);
    }

    private void bindView(ViewHolder holder, int i) {
        holder.tvMenuName.setText(categories.get(i).getName());

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        protected ImageView ivMenuImage;
        protected TextView tvMenuName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMenuImage = itemView.findViewById(R.id.img_menu_image);
            tvMenuName  = itemView.findViewById(R.id.tv_menu_name);
        }
    }
}
