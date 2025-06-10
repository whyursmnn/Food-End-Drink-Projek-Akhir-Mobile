package com.example.footenddrink.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FoodDrinkApiResponse {
    @SerializedName("menuItems")
    private List<FoodDrinkItem> menuItems;


    @SerializedName("totalMenuItems")
    private int totalMenuItems;

    public List<FoodDrinkItem> getMenuItems() {
        return menuItems;
    }

    public int getTotalMenuItems() {
        return totalMenuItems;
    }
}