package com.example.footenddrink.util;

import com.example.footenddrink.model.FoodDrinkApiResponse;
import com.example.footenddrink.model.FoodDrinkItem;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("food/menuItems/search")
    Call<FoodDrinkApiResponse> searchMenuItems(
                                                @Query("query") String query,
                                                @Query("number") int number,
                                                @Query("apiKey") String apiKey,
                                                @Query("offset") int offset
    );
}