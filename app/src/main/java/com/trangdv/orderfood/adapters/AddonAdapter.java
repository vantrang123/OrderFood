package com.trangdv.orderfood.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trangdv.orderfood.R;
import com.trangdv.orderfood.common.Common;
import com.trangdv.orderfood.model.Addon;
import com.trangdv.orderfood.model.eventbus.AddonEventChange;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class AddonAdapter extends RecyclerView.Adapter<AddonAdapter.ViewHolder> {
    private List<Addon> addonList = new ArrayList<>();
    private Context context;

    public AddonAdapter(Context context, List<Addon> addonList) {
        this.context = context;
        this.addonList = addonList;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_addon, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.ckbAddon.setText(new StringBuilder(addonList.get(position).getName())
        .append(" +(" +context.getString(R.string.menu_my))
        .append(addonList.get(position).getExtraPrice())
        .append(")"));

        holder.ckbAddon.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Common.addonList.add(addonList.get(position));
                    EventBus.getDefault().postSticky(new AddonEventChange(true, addonList.get(position)));
                } else {
                    Common.addonList.remove(addonList.get(position));
                    EventBus.getDefault().postSticky(new AddonEventChange(false, addonList.get(position)));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return addonList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        protected CheckBox ckbAddon;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ckbAddon = itemView.findViewById(R.id.ckb_addon);
        }
    }
}
