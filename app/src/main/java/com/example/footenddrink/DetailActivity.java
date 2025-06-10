package com.example.footenddrink;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.footenddrink.data.CartManager;
import com.example.footenddrink.model.FoodDrinkItem; // Pastikan import ini dari package model

public class DetailActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "extra_item_id";
    public static final String EXTRA_ITEM_NAME = "extra_item_name";
    public static final String EXTRA_ITEM_DESCRIPTION = "extra_item_description";
    public static final String EXTRA_ITEM_IMAGE_URL = "extra_item_image_url";
    public static final String EXTRA_ITEM_PRICE = "extra_item_price";

    private TextView tvQuantity;
    private int currentQuantity = 1;

    private FoodDrinkItem currentItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        TextView tvName = findViewById(R.id.tv_detail_name);
        TextView tvDescription = findViewById(R.id.tv_detail_description);
        ImageView ivImage = findViewById(R.id.iv_detail_image);
        TextView tvPrice = findViewById(R.id.tv_detail_price);
        tvQuantity = findViewById(R.id.tv_quantity);
        Button btnDecrement = findViewById(R.id.btn_decrement);
        Button btnIncrement = findViewById(R.id.btn_increment);
        Button btnAddToCart = findViewById(R.id.btn_add_to_cart);

        if (getIntent() != null) {
            String id = getIntent().getStringExtra(EXTRA_ITEM_ID);
            String name = getIntent().getStringExtra(EXTRA_ITEM_NAME);
            String description = getIntent().getStringExtra(EXTRA_ITEM_DESCRIPTION);
            String imageUrl = getIntent().getStringExtra(EXTRA_ITEM_IMAGE_URL);
            String price = getIntent().getStringExtra(EXTRA_ITEM_PRICE);


            Double priceDouble = null;
            if (price != null && !price.isEmpty()) {
                try {

                    String cleanPrice = price.replace("Rp", "").replace(".", "").replace(",", "").trim();
                    priceDouble = Double.parseDouble(cleanPrice);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }

            currentItem = new FoodDrinkItem(Integer.parseInt(id), name, imageUrl, description, priceDouble);


            tvName.setText(name);
            tvDescription.setText(description);
            tvPrice.setText(price);

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_error)
                        .into(ivImage);
            } else {
                ivImage.setImageResource(R.drawable.ic_no_image);
            }
        }

        updateQuantityText();

        btnDecrement.setOnClickListener(v -> {
            if (currentQuantity > 1) {
                currentQuantity--;
                updateQuantityText();
            }
        });

        btnIncrement.setOnClickListener(v -> {
            currentQuantity++;
            updateQuantityText();
        });

        btnAddToCart.setOnClickListener(v -> {
            if (currentItem != null) {
                CartManager.getInstance(this).addItemToCart(currentItem, currentQuantity);
                Toast.makeText(this, currentQuantity + " " + currentItem.getName() + " ditambahkan ke keranjang!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateQuantityText() {
        tvQuantity.setText(String.valueOf(currentQuantity));
    }
}