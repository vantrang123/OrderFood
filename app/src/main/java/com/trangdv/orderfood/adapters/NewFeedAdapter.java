package com.trangdv.orderfood.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.trangdv.orderfood.R;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.database.CartDataSource;
import com.trangdv.orderfood.database.CartDatabase;
import com.trangdv.orderfood.database.LocalCartDataSource;
import com.trangdv.orderfood.listener.ILoadMore;
import com.trangdv.orderfood.model.FavoriteOnlyId;
import com.trangdv.orderfood.model.Food;
import com.trangdv.orderfood.retrofit.IAnNgonAPI;
import com.trangdv.orderfood.retrofit.RetrofitClient;
import com.trangdv.orderfood.utils.DialogUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class NewFeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "NewFeedAdapter";
    private LayoutInflater mInflater;
    private List<Food> foodList = new ArrayList<>();
    Context context;
    LinearLayoutManager layoutManager;
    ItemListener listener;
    ILoadMore iLoadMore;
    private static final int VIEW_TYPE_LOADING = 1;
    private static final int VIEW_TYPE_ITEM = 0;

    Locale locale;
    NumberFormat fmt;

    IAnNgonAPI anNgonAPI;
    CompositeDisposable compositeDisposable;
    CartDataSource cartDataSource;
    DialogUtils dialogUtils;
    NestedScrollView nestedScrollView;
    RecyclerView rvNewFeed;
    private int totalItemCount = 0, visibleItemCount, pastVisibleItems;
    private boolean iLoading = false;

    public NewFeedAdapter(Context context, List<Food> foods, ItemListener itemListener, NestedScrollView nestedScrollView, RecyclerView rvNewFeed) {
        super();
        this.context = context;
        this.foodList = foods;
        listener = itemListener;
        compositeDisposable = new CompositeDisposable();
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());
        anNgonAPI = RetrofitClient.getInstance(Common.API_ANNGON_ENDPOINT).create(IAnNgonAPI.class);
        dialogUtils = new DialogUtils();
        this.nestedScrollView = nestedScrollView;
        this.rvNewFeed = rvNewFeed;

        initScrollListener();
    }

    private void initScrollListener() {
        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (v.getChildAt(v.getChildCount() - 1) != null) {
                    if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                            scrollY > oldScrollY) {
                        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) rvNewFeed.getLayoutManager();
                        totalItemCount = linearLayoutManager.getItemCount();
                        visibleItemCount = linearLayoutManager.getChildCount();
                        pastVisibleItems = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                        if (!iLoading && totalItemCount <= pastVisibleItems + visibleItemCount) {
                            if (iLoadMore != null) {
                                iLoadMore.onLoadMore();
                                iLoading = true;
                            }
                        }
                    }
                }
            }
        });
    }

    public void setLoaded() {
        iLoading = false;
    }

    public void setiLoadMore(ILoadMore iLoadMore) {
        this.iLoadMore = iLoadMore;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        locale = new Locale("vi", "VN");
        fmt = NumberFormat.getCurrencyInstance(locale);

        View itemView;
        mInflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_ITEM) {
            itemView = mInflater.inflate(R.layout.item_new_feed, parent, false);
            return new ViewHolder(itemView);
        } else {
            itemView = mInflater.inflate(R.layout.layout_loading_item, parent, false);
            return new LoadingHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            ViewHolder viewHolder = (ViewHolder) holder;
            double price = foodList.get(position).getPrice();

            viewHolder.tvNameFood.setText("Tên món: " + foodList.get(position).getName());
            viewHolder.tvPriceFood.setText(new StringBuilder("Giá: ").append(fmt.format(price)));
            viewHolder.tvDiscountFood.setText(new StringBuilder("Phí ship: ").append(fmt.format(foodList.get(position).getDiscount())).append("/km"));

            if (foodList.get(position).getBitmapImage() == null) {
                Glide.with(context)
                        .asBitmap()
                        .load(foodList.get(position).getImage())
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
                                foodList.get(position).setBitmapImage(resource);
                                return false;
                            }
                        })
                        .into(viewHolder.imgFood);
            } else {
                viewHolder.imgFood.setImageBitmap(foodList.get(position).getBitmapImage());
            }

            if (Common.currentFav != null && Common.currentFav.size() > 0) {
                if (Common.checkFavorite(foodList.get(position).getId())) {
                    viewHolder.ivFavorite.setImageResource(R.drawable.ic_favorite_red);
                    viewHolder.ivFavorite.setTag(true);
                } else {
                    viewHolder.ivFavorite.setImageResource(R.drawable.ic_favorite_gray);
                    viewHolder.ivFavorite.setTag(false);
                }
            } else {
                viewHolder.ivFavorite.setTag(false);
            }

        } else if (holder instanceof OrderAdapter.LoadingHolder) {
            OrderAdapter.LoadingHolder loadingHolder = (OrderAdapter.LoadingHolder) holder;
            loadingHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (foodList.get(position) == null) {
            return VIEW_TYPE_LOADING;
        } else return VIEW_TYPE_ITEM;
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
            tvNameFood = itemView.findViewById(R.id.tv_food_name);
            tvPriceFood = itemView.findViewById(R.id.tv_food_price);
            tvDiscountFood = itemView.findViewById(R.id.tv_food_discount);
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
                    dialogUtils.showProgress(context);
                    ImageView fav = (ImageView) v;
                    getRestaurantId(getAdapterPosition(), v, fav);
                }
            });
        }
    }

    private void getRestaurantId(int adapterPosition, View v, ImageView fav) {
        compositeDisposable.add(
                anNgonAPI.getRestaurantId(Common.API_KEY,
                        foodList.get(adapterPosition).getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(restaurantIdModel -> {
                            if (restaurantIdModel.isSuccess()) {
                                int currentRestaurantId = restaurantIdModel.getResult().get(0).getRestaurantId();
                                if ((Boolean) v.getTag()) {
                                    removeFavorite(adapterPosition, fav, currentRestaurantId);
                                } else {
                                    insertFavorite(adapterPosition, fav, currentRestaurantId);
                                }
                            }
                        }, throwable -> {
                            dialogUtils.dismissProgress();
                        }));
    }

    private void insertFavorite(int position, ImageView fav, int currentRestaurantId) {
        compositeDisposable.add(
                anNgonAPI.insertFavorite(Common.API_KEY,
                        Common.currentUser.getFbid(),
                        foodList.get(position).getId(),
                        currentRestaurantId,
                        foodList.get(position).getName(),
                        foodList.get(position).getImage(),
                        foodList.get(position).getPrice())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(favoriteModel -> {
                            if (favoriteModel.isSuccess() && favoriteModel.getMessage().contains("Success")) {
                                fav.setImageResource(R.drawable.ic_favorite_red);
                                fav.setTag(true);
                                if (Common.currentFav != null) {
                                    Common.currentFav.add(new FavoriteOnlyId(foodList.get(position).getId()));
                                }
                            }
                            dialogUtils.dismissProgress();
                        }, throwable -> {
                            Toast.makeText(context, "[ADD FAV]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            dialogUtils.dismissProgress();
                        })
        );
    }

    private void removeFavorite(int adapterPosition, ImageView fav, int currentRestaurantId) {
        compositeDisposable.add(
                anNgonAPI.removeFavorite(Common.API_KEY,
                        Common.currentUser.getFbid(),
                        foodList.get(adapterPosition).getId(),
                        currentRestaurantId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(favoriteModel -> {
                            if (favoriteModel.isSuccess() && favoriteModel.getMessage().contains("Success")) {
                                fav.setImageResource(R.drawable.ic_favorite_gray);
                                fav.setTag(false);
                                if (Common.currentFav != null) {
                                    Common.removeFav(foodList.get(adapterPosition).getId());
                                }
                            }
                            dialogUtils.dismissProgress();
                        }, throwable -> {

                            dialogUtils.dismissProgress();
                        })
        );
    }

    public void onStop() {
        compositeDisposable.clear();
    }

    public void addItem(List<Food> addedItems) {
        int startInsertedIndex = foodList.size();
        foodList.addAll(addedItems);
        notifyItemInserted(startInsertedIndex);
    }

    public void addNull() {
        foodList.add(null);
        notifyItemInserted(foodList.size() - 1);
    }

    public void removeNull() {
        foodList.remove(foodList.size() - 1);
        notifyItemRemoved(foodList.size());
    }

    public List<Food> getOrderList() {
        return foodList;
    }

    public class LoadingHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressbar);
        }
    }

    public interface ItemListener {
        void dispatchToFoodDetail(int position);
    }
}
