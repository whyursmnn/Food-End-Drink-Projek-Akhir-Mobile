package com.example.footenddrink.ui.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.fragment.NavHostFragment;

import com.example.footenddrink.R;
import com.example.footenddrink.data.CartManager;
import com.example.footenddrink.data.CartItem;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.List;

public class CartFragment extends Fragment implements CartAdapter.OnCartItemActionListener {

    private RecyclerView rvCartItems;
    private CartAdapter cartAdapter;
    private TextView tvEmptyCart;
    private TextView tvTotalPrice;
    private Button btnCheckout;

    private CartManager cartManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        rvCartItems = view.findViewById(R.id.rv_cart_items);
        tvEmptyCart = view.findViewById(R.id.tv_empty_cart);
        tvTotalPrice = view.findViewById(R.id.tv_total_price);
        btnCheckout = view.findViewById(R.id.btn_checkout);

        rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        cartManager = CartManager.getInstance(getContext());
        cartAdapter = new CartAdapter(cartManager.getCartItems(), this);
        rvCartItems.setAdapter(cartAdapter);

        btnCheckout.setOnClickListener(v -> {
            if (cartManager.getCartItemCount() > 0) {

                NavHostFragment.findNavController(this).navigate(R.id.action_cartFragment_to_orderSummaryFragment);

            } else {
                Toast.makeText(getContext(), "Keranjang Anda kosong!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        List<CartItem> currentCart = cartManager.getCartItems();
        if (currentCart.isEmpty()) {
            rvCartItems.setVisibility(View.GONE);
            tvEmptyCart.setVisibility(View.VISIBLE);
            tvTotalPrice.setText("Rp 0");
            btnCheckout.setEnabled(false);
        } else {
            rvCartItems.setVisibility(View.VISIBLE);
            tvEmptyCart.setVisibility(View.GONE);
            btnCheckout.setEnabled(true);
            cartAdapter.updateData(currentCart);
            updateTotalPrice();
        }
    }

    private void updateTotalPrice() {
        double total = cartManager.getTotalPrice();
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        tvTotalPrice.setText(format.format(total));
    }


    @Override
    public void onQuantityChanged(int itemId, int newQuantity) {
        cartManager.updateItemQuantity(itemId, newQuantity);
        updateUI();
    }

    @Override
    public void onRemoveItem(int itemId) {
        cartManager.removeItemFromCart(itemId);
        Toast.makeText(getContext(), "Item dihapus dari keranjang.", Toast.LENGTH_SHORT).show();
        updateUI();
    }
}