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
import com.trangdv.orderfood.model.Suggestion;

import java.util.ArrayList;
import java.util.List;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.ViewHolder> {
    private Context context;
    private List<Suggestion> suggestions = new ArrayList<>();
    private ItemListener itemListener;

    public SuggestionAdapter(Context context, List<Suggestion> suggestions, ItemListener itemListener) {
        this.context = context;
        this.suggestions = suggestions;
        this.itemListener = itemListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_suggestion, parent, false);
        return new SuggestionAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        bindView(holder, position);
    }

    private void bindView(ViewHolder holder, int i) {
        holder.tvSuggestionName.setText(suggestions.get(i).getName());

        if (suggestions.get(i).getBitmapImage() == null) {
            Glide.with(context)
                    .asBitmap()
                    .load(suggestions.get(i).getImage())
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
                            suggestions.get(i).setBitmapImage(resource);
                            return false;
                        }
                    })
                    .into(holder.ivSuggestionImage);

        } else {
            holder.ivSuggestionImage.setImageBitmap(suggestions.get(i).getBitmapImage());
        }
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        protected ImageView ivSuggestionImage;
        protected TextView tvSuggestionName;
        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            ivSuggestionImage = itemView.findViewById(R.id.img_suggestion_image);
            tvSuggestionName = itemView.findViewById(R.id.tv_suggestion_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    itemListener.dispatchToFoodDetail(getAdapterPosition());
                }
            });
        }
    }

    public interface ItemListener {
        void dispatchToFoodDetail(int position);
    }
}
