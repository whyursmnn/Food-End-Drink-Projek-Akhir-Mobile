package com.example.footenddrink.ui.cart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.footenddrink.R;
import com.example.footenddrink.data.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private OnCartItemActionListener listener;

    public interface OnCartItemActionListener {
        void onQuantityChanged(int itemId, int newQuantity);
        void onRemoveItem(int itemId);
    }

    public CartAdapter(List<CartItem> cartItems, OnCartItemActionListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    public void updateData(List<CartItem> newItems) {
        this.cartItems.clear();
        this.cartItems.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName;
        TextView tvPrice;
        TextView tvQuantity;
        Button btnDecrement;
        Button btnIncrement;
        ImageButton btnRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_cart_item_image);
            tvName = itemView.findViewById(R.id.tv_cart_item_name);
            tvPrice = itemView.findViewById(R.id.tv_cart_item_price);
            tvQuantity = itemView.findViewById(R.id.tv_cart_item_quantity);
            btnDecrement = itemView.findViewById(R.id.btn_cart_decrement);
            btnIncrement = itemView.findViewById(R.id.btn_cart_increment);
            btnRemove = itemView.findViewById(R.id.btn_remove_item);
        }

        public void bind(CartItem item, OnCartItemActionListener listener) {
            tvName.setText(item.getName());
            tvPrice.setText(item.getPrice());
            tvQuantity.setText(String.valueOf(item.getQuantity()));

            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getImageUrl())
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_placeholder)
                        .into(ivImage);
            } else {
                ivImage.setImageResource(R.drawable.ic_no_image);
            }

            btnDecrement.setOnClickListener(v -> {
                if (item.getQuantity() > 1) {
                    listener.onQuantityChanged(item.getId(), item.getQuantity() - 1);
                } else {
                    listener.onRemoveItem(item.getId());
                }
            });

            btnIncrement.setOnClickListener(v -> {
                listener.onQuantityChanged(item.getId(), item.getQuantity() + 1);
            });

            btnRemove.setOnClickListener(v -> {
                listener.onRemoveItem(item.getId());
            });
        }
    }
}