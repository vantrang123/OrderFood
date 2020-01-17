package com.trangdv.orderfood.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.listener.OnDatabaseChangedListeners;
import com.trangdv.orderfood.model.Order;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> implements OnDatabaseChangedListeners {

    private List<Order> listData = new ArrayList<>();
    private Context context;
    public RelativeLayout viewBackground, viewForeground;
    ItemListener listener;

    public CartAdapter(List<Order> listData, Context context, ItemListener listener) {
        this.listData = listData;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        TextDrawable drawable = TextDrawable.builder().buildRound("" + listData.get(position).getQuanlity(), Color.RED);
        holder.img_cart_count.setImageDrawable(drawable);

        Locale locale = new Locale("vi", "VN");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        int price = (Integer.parseInt(listData.get(position).getPrice()));
        holder.tv_price.setText(fmt.format(price));
        holder.tv_cart_name.setText(listData.get(position).getProductName());
        /*Picasso.with(context)
                .load(listData.get(position).getImage())
                .into(holder.img_cart_image);*/
        Glide.with(context)
                .asBitmap()
                .centerCrop()
                .fitCenter()
                .placeholder(R.drawable.image_default)
                .load(listData.get(position).getImage())
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        holder.img_cart_image.setImageBitmap(resource);
                        return false;
                    }
                })
                .into(holder.img_cart_image);


        holder.viewForeground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.showDialogOptions(position, listData.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }

    @Override
    public void onNewDatabaseEntryAdded() {

    }

    @Override
    public void onNewDatabaseEntryRemoved() {

    }

    @Override
    public void onNewDatabaseEntryRenamed() {

    }


    public class ViewHolder extends RecyclerView.ViewHolder /*implements View.OnCreateContextMenuListener*/ {

        public TextView tv_cart_name, tv_price;
        public ImageView img_cart_count, img_cart_image;

        public RelativeLayout viewBackground, viewForeground;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            img_cart_image = itemView.findViewById(R.id.iv_cart_image);
            tv_cart_name = itemView.findViewById(R.id.cart_item_name);
            tv_price = itemView.findViewById(R.id.cart_item_price);
            img_cart_count = itemView.findViewById(R.id.cart_item_count);

            viewBackground = itemView.findViewById(R.id.view_background);
            viewForeground = itemView.findViewById(R.id.view_foreground);

        }

        /*@Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.setHeaderTitle(Common.DELETE);
            contextMenu.add(0, 0, gaddToCartetAdapterPosition(), Common.DELETE);
        }*/

    }

    public void removeItem(int position) {
        listData.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position);
    }

    public void restoreItem(Order item, int position) {
        listData.add(position, item);
        // notify item added by position
        notifyItemInserted(position);
    }

    public interface ItemListener {
        void showDialogOptions(int position, Order order);
    }
}
