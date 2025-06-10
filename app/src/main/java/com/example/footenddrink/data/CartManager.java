package com.example.footenddrink.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.footenddrink.model.FoodDrinkItem;
import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class CartManager {
    private static CartManager instance;
    private List<CartItem> cartItems;
    private Context context;
    private static final String PREFS_NAME = "McDonaldsAppCartPrefs";
    private static final String CART_KEY = "shopping_cart";

    private CartManager(Context context) {
        this.context = context.getApplicationContext();
        loadCart();
    }

    public static synchronized CartManager getInstance(Context context) {
        if (instance == null) {
            instance = new CartManager(context);
        }
        return instance;
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }


    public void addItemToCart(FoodDrinkItem item, int quantity) {
        if (quantity <= 0) return;


        String formattedPrice = "Rp 0";
        if (item.getPrice() != null) {
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
            formattedPrice = format.format(item.getPrice());
        }

        boolean found = false;
        for (CartItem cartItem : cartItems) {
            if (cartItem.getId() == item.getId()) {
                cartItem.setQuantity(cartItem.getQuantity() + quantity);
                found = true;
                break;
            }
        }
        if (!found) {

            cartItems.add(new CartItem(item.getId(), item.getName(), item.getImageUrl(), formattedPrice, quantity));
        }
        saveCart();
    }

    public void updateItemQuantity(int itemId, int newQuantity) {
        if (newQuantity <= 0) {
            removeItemFromCart(itemId);
            return;
        }
        for (CartItem cartItem : cartItems) {
            if (cartItem.getId() == itemId) {
                cartItem.setQuantity(newQuantity);
                break;
            }
        }
        saveCart();
    }

    public void removeItemFromCart(int itemId) {
        cartItems.removeIf(item -> item.getId() == itemId);
        saveCart();
    }

    public void clearCart() {
        cartItems.clear();
        saveCart();
    }

    private void saveCart() {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String jsonCart = gson.toJson(cartItems);
        editor.putString(CART_KEY, jsonCart);
        editor.apply();
    }

    private void loadCart() {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String jsonCart = preferences.getString(CART_KEY, null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<CartItem>>(){}.getType();
        cartItems = gson.fromJson(jsonCart, type);
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
    }

    public int getCartItemCount() {
        return cartItems.size();
    }

    public double getTotalPrice() {
        double total = 0;
        for (CartItem item : cartItems) {
            try {

                String priceStr = item.getPrice()
                        .replace("Rp", "")
                        .replace(".", "")
                        .replace(",", ".")
                        .trim();
                double itemPrice = Double.parseDouble(priceStr);
                total += itemPrice * item.getQuantity();
            } catch (NumberFormatException e) {
                e.printStackTrace();

            }
        }
        return total;
    }
}