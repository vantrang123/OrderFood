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
import com.trangdv.orderfood.model.Category;

import java.util.ArrayList;
import java.util.List;


public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
    private Context context;
    private List<Category> categories = new ArrayList<>();
    private ItemListener itemListener;

    public MenuAdapter(Context context, List<Category> categoryList, ItemListener itemListener) {
        this.context = context;
        this.categories = categoryList;
        this.itemListener = itemListener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_menu, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        bindView(holder, position);
    }

    private void bindView(final ViewHolder holder, final int i) {
        holder.tvMenuName.setText(categories.get(i).getName());

        if (categories.get(i).getBitmapImage() == null) {
            Glide.with(context)
                    .asBitmap()
                    .load(categories.get(i).getImage())
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
                            categories.get(i).setBitmapImage(resource);
                            return false;
                        }
                    })
                    .into(holder.ivMenuImage);

        } else {
            holder.ivMenuImage.setImageBitmap(categories.get(i).getBitmapImage());
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        protected ImageView ivMenuImage;
        protected TextView tvMenuName;
        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            ivMenuImage = itemView.findViewById(R.id.img_menu_image);
            tvMenuName  = itemView.findViewById(R.id.tv_menu_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    itemListener.dispatchToFoodList(getAdapterPosition());
                }
            });
        }
    }

    public interface ItemListener {
        void dispatchToFoodList(int position);
    }
}
