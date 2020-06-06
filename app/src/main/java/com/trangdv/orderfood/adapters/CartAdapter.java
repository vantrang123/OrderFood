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
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.database.CartDataSource;
import com.trangdv.orderfood.database.CartDatabase;
import com.trangdv.orderfood.database.CartItem;
import com.trangdv.orderfood.database.LocalCartDataSource;
import com.trangdv.orderfood.listener.IOnImageViewAdapterClickListener;
import com.trangdv.orderfood.listener.OnDatabaseChangedListeners;
import com.trangdv.orderfood.model.Order;
import com.trangdv.orderfood.model.eventbus.CaculatePriceEvent;
import com.trangdv.orderfood.utils.SwipeLayout;

import org.greenrobot.eventbus.EventBus;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> implements OnDatabaseChangedListeners {

    private List<CartItem> cartItemList = new ArrayList<>();
    private Context context;
    CartDataSource cartDataSource;

    ItemListener listener;

    public CartAdapter(List<CartItem> cartItems, Context context, ItemListener listener) {
        this.cartItemList = cartItems;
        this.context = context;
        this.listener = listener;
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.layout_swipe_item_cart, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.tvQuantity.setText(String.valueOf(cartItemList.get(position).getFoodQuantity()));

        Locale locale = new Locale("vi", "VN");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        Double price = cartItemList.get(position).getFoodPrice();
        holder.tvPrice.setText(fmt.format(price));

        holder.tvName.setText(cartItemList.get(position).getFoodName());

        Glide.with(context)
                .asBitmap()
                .centerCrop()
                .fitCenter()
                .placeholder(R.drawable.image_default)
                .load(cartItemList.get(position).getFoodImage())
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        holder.ivImage.setImageBitmap(resource);
                        return false;
                    }
                })
                .into(holder.ivImage);

        Double finalResult = cartItemList.get(position).getFoodPrice() * cartItemList.get(position).getFoodQuantity();
        holder.tvPrice.setText(fmt.format(finalResult));

        // event
        holder.setListener(((view, position1, isDecrease, isDelete) -> {
            if (!isDelete) {
                if (isDecrease) {
                    if (cartItemList.get(position).getFoodQuantity()>1) {
                        cartItemList.get(position).setFoodQuantity(cartItemList.get(position).getFoodQuantity()-1);
                    }
                } else {
                    if (cartItemList.get(position).getFoodQuantity()<99) {
                        cartItemList.get(position).setFoodQuantity(cartItemList.get(position).getFoodQuantity()+1);
                    }
                }

                cartDataSource.updateCart(cartItemList.get(position))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Integer>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onSuccess(Integer integer) {
                                holder.tvQuantity.setText(String.valueOf(cartItemList.get(position).getFoodQuantity()));

                                EventBus.getDefault().postSticky(new CaculatePriceEvent());
                            }

                            @Override
                            public void onError(Throwable e) {

                            }
                        });
            } else {
                cartDataSource.deleteCart(cartItemList.get(position))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Integer>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onSuccess(Integer integer) {
                                cartItemList.remove(integer-1);
                                notifyItemRemoved(integer-1);
                                EventBus.getDefault().postSticky(new CaculatePriceEvent());
                            }

                            @Override
                            public void onError(Throwable e) {

                            }
                        });
            }
        }));
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView tvName, tvPrice, tvQuantity;
        public ImageView ivPlus, ivImage, ivSub, ivDelete;
        SwipeLayout swipeLayout;

        public RelativeLayout viewBackground, viewForeground;

        IOnImageViewAdapterClickListener onCaculatePriceListener;

        public void setListener(IOnImageViewAdapterClickListener itemListener) {
            onCaculatePriceListener = itemListener;
        }

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_food_image);
            tvName = itemView.findViewById(R.id.tv_food_name);
            tvPrice = itemView.findViewById(R.id.tv_food_price);
            ivPlus = itemView.findViewById(R.id.iv_increase);
            ivSub = itemView.findViewById(R.id.iv_decrease);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            swipeLayout = itemView.findViewById(R.id.swipe_layout);
            ivDelete = itemView.findViewById(R.id.iv_delete);

            ivSub.setOnClickListener(this);
            ivPlus.setOnClickListener(this);
            ivDelete.setOnClickListener(this);

            swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.showDialogOptions(getAdapterPosition());
                }
            });


        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_decrease:
                    onCaculatePriceListener.onCaculatePriceListener(v, getAdapterPosition(), true, false);
                    break;
                case R.id.iv_increase:
                    onCaculatePriceListener.onCaculatePriceListener(v, getAdapterPosition(), false, false);
                    break;
                case R.id.iv_delete:
                    if (getAdapterPosition() != NO_POSITION) {
                        onCaculatePriceListener.onCaculatePriceListener(v, getAdapterPosition(), false, true);
                    }
                    break;
                default:
                    break;
            }
        }

    }

    public void removeItem(int position) {
        cartItemList.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Order item, int position) {
        notifyItemInserted(position);
    }

    public interface ItemListener {
        void showDialogOptions(int position);
    }
}
