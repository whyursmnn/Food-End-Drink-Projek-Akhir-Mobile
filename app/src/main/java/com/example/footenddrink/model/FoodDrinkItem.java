package com.example.footenddrink.model;

import com.google.gson.annotations.SerializedName;

public class FoodDrinkItem {
    @SerializedName("id")
    private int id;
    @SerializedName("title")
    private String name;
    @SerializedName("image")
    private String imageUrl;
    @SerializedName("restaurantChain")
    private String restaurantChain;
    @SerializedName("price")
    private Double price;


    public FoodDrinkItem(int id, String name, String imageUrl, String restaurantChain, Double price) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.restaurantChain = restaurantChain;
        this.price = price;
    }


    public FoodDrinkItem(int id, String name, String description, String imageUrl, String priceStr) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.restaurantChain = description;
        try {

            this.price = (priceStr != null && !priceStr.isEmpty()) ? Double.parseDouble(priceStr.replace("Rp", "").replace(".", "").replace(",", "").trim()) : null;
        } catch (NumberFormatException e) {
            this.price = null;
        }
    }



    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }


    public String getDescription() {
        return restaurantChain != null ? restaurantChain : "Informasi restoran tidak tersedia.";
    }

    public Double getPrice() {
        return price;
    }

    public void setRestaurantChain(String restaurantChain) {
        this.restaurantChain = restaurantChain;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}