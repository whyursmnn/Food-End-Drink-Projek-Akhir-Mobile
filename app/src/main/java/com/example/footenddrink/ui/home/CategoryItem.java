package com.example.footenddrink.ui.home;

public class CategoryItem {
    private String name;
    private int iconResId;

    public CategoryItem(String name, int iconResId) {
        this.name = name;
        this.iconResId = iconResId;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }
}