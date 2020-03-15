package com.trangdv.orderfood.adapters;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trangdv.orderfood.R;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.model.Order;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_LOADING = 1;
    private static final int VIEW_TYPE_ITEM = 0;
    private LayoutInflater mInflater;
    private List<Order> orderList = new ArrayList<>();
    Context context;
    ItemListener listener;
    SimpleDateFormat simpleDateFormat;

    public OrderAdapter(Context context, List<Order> requests, ItemListener itemListener) {
        super();
        this.context = context;
        this.orderList = requests;
        listener = itemListener;
        simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy");
    }

    public void addItem(List<Order> addedItems) {
        int startInsertedIndex = orderList.size();
        orderList.addAll(addedItems);
        notifyItemInserted(startInsertedIndex-1);
        removeNull(startInsertedIndex, addedItems.size());

    }

    public void addNull() {
        orderList.add(null);
        notifyItemInserted(orderList.size() - 1);
    }

    public void removeNull(int size,int itemAdd) {
        orderList.remove(size - itemAdd );
        notifyItemRemoved(size - itemAdd );
    }

    @Override
    public int getItemViewType(int position) {
        if (orderList.get(position) == null) {
            return VIEW_TYPE_LOADING;
        } else return VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        mInflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_ITEM) {
            itemView = mInflater.inflate(R.layout.item_order, parent, false);
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
            viewHolder.tvOrderNumOfItem.setText(new StringBuilder(String.valueOf(orderList.get(position).getNumOfItem())));
            viewHolder.tvOrderAddres.setText(new StringBuilder(orderList.get(position).getOrderAddress()));
            viewHolder.tvOrderPhone.setText(new StringBuilder(orderList.get(position).getOrderPhone()));
            viewHolder.tvOrderPrice.setText(new StringBuilder(String.valueOf(orderList.get(position).getTotalPrice())));
            viewHolder.tvOrerDate.setText(new StringBuilder(simpleDateFormat.format(orderList.get(position).getOrderDate())));
            viewHolder.tvOrderId.setText(new StringBuilder(String.valueOf(orderList.get(position).getOrderId())));
            viewHolder.tvOrderStatus.setText(Common.convertCodeToStatus(orderList.get(position).getOrderStatus()));

            if (orderList.get(position).isCod()) {
                viewHolder.tvOrderCod.setText(new StringBuilder("Cash On Delivery"));
            } else {
                viewHolder.tvOrderCod.setText(new StringBuilder("TransID: ").append(orderList.get(position).getTransactionId()));
            }
        } else if (holder instanceof LoadingHolder) {
            LoadingHolder loadingHolder = (LoadingHolder) holder;
            loadingHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvOrderId, tvOrderStatus, tvOrderPhone, tvOrderAddres, tvOrderCod, tvOrerDate, tvOrderPrice, tvOrderNumOfItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvOrderAddres = itemView.findViewById(R.id.tv_order_address);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvOrderPhone = itemView.findViewById(R.id.tv_order_phone);
            tvOrderCod = itemView.findViewById(R.id.tv_order_cod);
            tvOrerDate = itemView.findViewById(R.id.tv_order_date);
            tvOrderPrice = itemView.findViewById(R.id.tv_order_price);
            tvOrderNumOfItem = itemView.findViewById(R.id.tv_order_num_of_item);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.dispatchToOrderDetail(getLayoutPosition());
                }
            });
        }
    }

    public class LoadingHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progressbar);
        }
    }

    public interface ItemListener {
        void dispatchToOrderDetail(int position);
    }
}
