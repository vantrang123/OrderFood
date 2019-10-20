package com.trangdv.orderfood.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.squareup.picasso.Picasso;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.listener.OnDatabaseChangedListeners;
import com.trangdv.orderfood.model.Order;
import com.trangdv.orderfood.ui.MainActivity;

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
        View itemView = inflater.inflate(R.layout.cart_list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextDrawable drawable = TextDrawable.builder().buildRound("" + listData.get(position).getQuanlity(), Color.RED);
        holder.img_cart_count.setImageDrawable(drawable);

        Locale locale = new Locale("vi", "VN");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        int price = (Integer.parseInt(listData.get(position).getPrice())) * (Integer.parseInt(listData.get(position).getQuanlity()));
        holder.tv_price.setText(fmt.format(price));
        holder.tv_cart_name.setText(listData.get(position).getProductName());
        Picasso.with(context)
                .load(listData.get(position).getImage())
                .into(holder.img_cart_image);

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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img_cart_image = itemView.findViewById(R.id.thumbnail);
            tv_cart_name = itemView.findViewById(R.id.cart_item_name);
            tv_price = itemView.findViewById(R.id.cart_item_price);
            img_cart_count = itemView.findViewById(R.id.cart_item_count);

            viewBackground = itemView.findViewById(R.id.view_background);
            viewForeground = itemView.findViewById(R.id.view_foreground);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.showDialogOptions(getLayoutPosition());
                }
            });

            /*itemView.setOnCreateContextMenuListener(this);*/
        }

        /*@Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.setHeaderTitle(Common.DELETE);
            contextMenu.add(0, 0, getAdapterPosition(), Common.DELETE);
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
        void showDialogOptions(int position);
    }
}
