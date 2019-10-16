package com.trangdv.orderfood.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trangdv.orderfood.R;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.model.Request;

import java.util.ArrayList;
import java.util.List;

public class OrderStatusAdapter extends RecyclerView.Adapter<OrderStatusAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private List<Request> requests = new ArrayList<>();
    List<String> listIds = new ArrayList<>();
    Context context;
    LinearLayoutManager layoutManager;
    ItemListener listener;

    public TextView tvOrderId, tvOrderStatus, tvOrderPhone, tvOrderAddres;

    public OrderStatusAdapter(Context context, List<Request> requests, List<String> ids, ItemListener itemListener) {
        super();
        this.context = context;
        this.requests = requests;
        this.listIds = ids;
        listener = itemListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mInflater = LayoutInflater.from(parent.getContext());
        View view = mInflater.inflate(R.layout.order_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvOrderId.setText(listIds.get(position));
        holder.tvOrderStatus.setText(Common.convertCodeToStatus(requests.get(position).getStatus()));
        holder.tvOrderPhone.setText(requests.get(position).getPhone());
        holder.tvOrderAddres.setText(requests.get(position).getAddress());
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvOrderId, tvOrderStatus, tvOrderPhone, tvOrderAddres;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvOrderAddres = itemView.findViewById(R.id.tv_order_address);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvOrderPhone = itemView.findViewById(R.id.tv_order_phone);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.dispatchToOrderDetail(getLayoutPosition());
                }
            });
        }
    }

    public interface ItemListener {
        void dispatchToOrderDetail(int position);
    }
}
