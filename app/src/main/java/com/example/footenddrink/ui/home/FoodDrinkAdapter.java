package com.example.footenddrink.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.footenddrink.R;
import com.example.footenddrink.model.FoodDrinkItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodDrinkAdapter extends RecyclerView.Adapter<FoodDrinkAdapter.FoodDrinkViewHolder> {

    private List<FoodDrinkItem> items;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(FoodDrinkItem item);
    }

    public FoodDrinkAdapter(List<FoodDrinkItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateData(List<FoodDrinkItem> newItems) {
        this.items.clear();
        this.items.addAll(newItems);
        notifyDataSetChanged();
    }


    public void addData(List<FoodDrinkItem> newItems) {
        int startPosition = this.items.size();
        this.items.addAll(newItems);
        notifyItemRangeInserted(startPosition, newItems.size());
    }


    public List<FoodDrinkItem> getAllItems() {
        return new ArrayList<>(items);
    }

    @NonNull
    @Override
    public FoodDrinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food_drink, parent, false);
        return new FoodDrinkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodDrinkViewHolder holder, int position) {
        FoodDrinkItem item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class FoodDrinkViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName;
        TextView tvOriginalPrice;
        TextView tvPrice;
        TextView tvDeliveryTime;
        TextView tvRating;

        public FoodDrinkViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_item_image);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvOriginalPrice = itemView.findViewById(R.id.tv_item_original_price);
            tvPrice = itemView.findViewById(R.id.tv_item_price);
            tvDeliveryTime = itemView.findViewById(R.id.tv_item_delivery_time);
            tvRating = itemView.findViewById(R.id.tv_item_rating);
        }

        public void bind(FoodDrinkItem item, OnItemClickListener listener) {
            if (item == null) {
                itemView.setVisibility(View.GONE);
                return;
            }

            tvName.setText(item.getName() != null && !item.getName().isEmpty() ? item.getName() : "Nama Produk");

            if (item.getPrice() != null) {
                NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
                tvPrice.setText(format.format(item.getPrice()));
            } else {
                tvPrice.setText("Rp 0");
            }

            tvOriginalPrice.setText("");
            tvOriginalPrice.setVisibility(View.GONE);
            tvDeliveryTime.setText("20-30 min | Gratis Ongkir");
            tvRating.setText("4.5");


            String imageUrl = item.getImageUrl();
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(ivImage);
            } else {
                ivImage.setImageResource(R.drawable.ic_no_image);
            }

            itemView.setOnClickListener(v -> listener.onItemClick(item));
        }
    }
}