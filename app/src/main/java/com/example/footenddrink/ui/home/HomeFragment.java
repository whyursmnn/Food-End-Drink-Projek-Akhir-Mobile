package com.example.footenddrink.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.example.footenddrink.DetailActivity;
import com.example.footenddrink.MainActivity;
import com.example.footenddrink.R;
import com.example.footenddrink.model.FoodDrinkItem;
import com.example.footenddrink.model.FoodDrinkApiResponse;
import com.example.footenddrink.util.ApiService;


import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class HomeFragment extends Fragment implements FoodDrinkAdapter.OnItemClickListener, CategoryAdapter.OnCategoryClickListener {

    private RecyclerView rvItems;
    private FoodDrinkAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvErrorMessage;
    private Button btnRetry;

    private RecyclerView rvCategories;
    private CategoryAdapter categoryAdapter;

    private ImageView ivBanner;
    private SearchView searchViewMenu;


    private ExecutorService executorService;
    private Handler mainHandler;

    private static final String BASE_URL = "https://api.spoonacular.com/";
    private static final String API_KEY = "a7e7c9b6d0f54933894da9f2e5203325";
    private static final int ITEMS_PER_PAGE = 20;
    private static final int VISIBLE_THRESHOLD = 5;

    private int currentOffset = 0;
    private boolean isLoading = false;
    private boolean hasMoreData = true;
    private String currentQuery = "all";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvItems = view.findViewById(R.id.rv_items);
        progressBar = view.findViewById(R.id.progress_bar);
        tvErrorMessage = view.findViewById(R.id.tv_error_message);
        btnRetry = view.findViewById(R.id.btn_retry);
        ivBanner = view.findViewById(R.id.iv_banner);
        searchViewMenu = view.findViewById(R.id.search_view_menu);



        if (ivBanner != null && getContext() != null) {
            Glide.with(requireContext()).load(R.drawable.img_banner_fried_chicken).into(ivBanner);
        }



        rvCategories = view.findViewById(R.id.rv_categories);
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        List<CategoryItem> categories = getDummyCategories();
        categoryAdapter = new CategoryAdapter(categories, this);
        rvCategories.setAdapter(categoryAdapter);


        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        rvItems.setLayoutManager(gridLayoutManager);
        adapter = new FoodDrinkAdapter(new ArrayList<>(), this);
        rvItems.setAdapter(adapter);

        rvItems.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int totalItemCount = gridLayoutManager.getItemCount();
                int lastVisibleItem = gridLayoutManager.findLastVisibleItemPosition();

                if (dy > 0 && !isLoading && hasMoreData) {
                    if (lastVisibleItem + VISIBLE_THRESHOLD >= totalItemCount) {
                        isLoading = true;
                        currentOffset += ITEMS_PER_PAGE;
                        Log.d("HomeFragment", "Loading more data, offset: " + currentOffset);
                        fetchMenuItems(currentQuery, ITEMS_PER_PAGE, currentOffset, false);
                    }
                }
            }
        });

        searchViewMenu.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentQuery = query;
                currentOffset = 0;
                hasMoreData = true;
                adapter.updateData(new ArrayList<>());
                Toast.makeText(getContext(), "Mencari: '" + query + "'", Toast.LENGTH_SHORT).show();
                fetchMenuItems(currentQuery, ITEMS_PER_PAGE, currentOffset, true);
                searchViewMenu.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });


        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        btnRetry.setOnClickListener(v -> loadInitialData());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadInitialData();
    }

    private void loadInitialData() {
        currentQuery = "all";
        currentOffset = 0;
        hasMoreData = true;
        adapter.updateData(new ArrayList<>());
        fetchMenuItems(currentQuery, ITEMS_PER_PAGE, currentOffset, true);
    }

    private void fetchMenuItems(String query, int number, int offset, boolean clearExisting) {
        if (offset == 0 && clearExisting) {
            progressBar.setVisibility(View.VISIBLE);
            tvErrorMessage.setVisibility(View.GONE);
            btnRetry.setVisibility(View.GONE);
            rvItems.setVisibility(View.GONE);
        }

        isLoading = true;

        if (!isNetworkAvailable()) {
            progressBar.setVisibility(View.GONE);
            tvErrorMessage.setVisibility(View.VISIBLE);
            btnRetry.setVisibility(View.VISIBLE);
            tvErrorMessage.setText("Tidak ada koneksi internet. Silakan coba refresh.");
            Toast.makeText(getContext(), "Tidak ada koneksi internet.", Toast.LENGTH_SHORT).show();
            isLoading = false;
            rvItems.setVisibility(View.GONE);
            adapter.updateData(new ArrayList<>());

            return;
        }



        executorService.execute(() -> {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            ApiService apiService = retrofit.create(ApiService.class);

            Call<FoodDrinkApiResponse> call = apiService.searchMenuItems(query, number, API_KEY, offset);

            call.enqueue(new Callback<FoodDrinkApiResponse>() {
                @Override
                public void onResponse(Call<FoodDrinkApiResponse> call, Response<FoodDrinkApiResponse> response) {
                    mainHandler.post(() -> {
                        isLoading = false;
                        progressBar.setVisibility(View.GONE);


                        if (!isAdded() || getView() == null) {
                            Log.w("HomeFragment", "Fragment not attached or view destroyed after API response, skipping banner load.");
                            return;
                        }


                        if (response.isSuccessful() && response.body() != null && response.body().getMenuItems() != null) {
                            List<FoodDrinkItem> newItems = response.body().getMenuItems();
                            int totalResults = response.body().getTotalMenuItems();

                            if (clearExisting) {
                                adapter.updateData(newItems);
                            } else {
                                adapter.addData(newItems);
                            }

                            rvItems.setVisibility(View.VISIBLE);



                            if (offset == 0 && ivBanner != null && getContext() != null) {
                                if (!newItems.isEmpty() && newItems.get(0).getImageUrl() != null && !newItems.get(0).getImageUrl().trim().isEmpty()) {
                                    Glide.with(requireContext())
                                            .load(newItems.get(0).getImageUrl())
                                            .placeholder(R.drawable.ic_placeholder)
                                            .error(R.drawable.ic_error)
                                            .into(ivBanner);
                                } else if (newItems.isEmpty()) {
                                    Glide.with(requireContext()).load(R.drawable.img_banner_fried_chicken).into(ivBanner);
                                }
                            } else if (offset == 0 && ivBanner != null && getContext() != null) {
                                Glide.with(requireContext()).load(R.drawable.img_banner_fried_chicken).into(ivBanner);
                            }



                            if (adapter.getItemCount() >= totalResults || newItems.size() < number) {
                                hasMoreData = false;
                                Log.d("HomeFragment", "API mengembalikan kurang dari yang diminta, mengasumsikan tidak ada data lagi. Total di adapter: " + adapter.getItemCount() + ", API total: " + totalResults);
                            } else {
                                hasMoreData = true;
                                Log.d("HomeFragment", "More data available. Current items in adapter: " + adapter.getItemCount() + ", API total: " + totalResults);
                            }

                            if (offset == 0 && newItems.isEmpty()) {
                                tvErrorMessage.setVisibility(View.VISIBLE);
                                tvErrorMessage.setText("Tidak ada hasil untuk '" + query + "' dengan gambar yang valid.");
                                btnRetry.setVisibility(View.VISIBLE);
                            } else {
                                tvErrorMessage.setVisibility(View.GONE);
                                btnRetry.setVisibility(View.GONE);
                            }
                            Toast.makeText(getContext(), "Data untuk '" + query + "' dimuat. (" + newItems.size() + " baru)", Toast.LENGTH_SHORT).show();
                            if (offset == 0) {
                                saveProductsToSharedPreferences(adapter.getAllItems());
                            }
                        } else {
                            Log.e("HomeFragment", "API request failed. Code: " + response.code() + ", Message: " + response.message());
                            if (response.code() == 402) {
                                tvErrorMessage.setText("Kuota API habis. Silakan coba lagi nanti atau periksa dashboard Spoonacular Anda.");
                            } else if (response.code() == 429) {
                                tvErrorMessage.setText("Terlalu banyak permintaan. Silakan tunggu sebentar.");
                            } else {
                                tvErrorMessage.setText("Gagal mengambil data dari API: " + response.message());
                            }
                            tvErrorMessage.setVisibility(View.VISIBLE);
                            btnRetry.setVisibility(View.VISIBLE);
                            Toast.makeText(getContext(), "Gagal mengambil data dari API.", Toast.LENGTH_LONG).show();

                            if (adapter.getItemCount() == 0) {
                                loadProductsFromSharedPreferences();
                            }

                            if (isAdded() && ivBanner != null && getContext() != null) {
                                Glide.with(requireContext()).load(R.drawable.img_banner_fried_chicken).into(ivBanner);
                            }

                        }
                    });
                }

                @Override
                public void onFailure(Call<FoodDrinkApiResponse> call, Throwable t) {
                    mainHandler.post(() -> {
                        isLoading = false;
                        progressBar.setVisibility(View.GONE);


                        if (isAdded() && ivBanner != null && getContext() != null) {
                            Glide.with(requireContext()).load(R.drawable.img_banner_fried_chicken).into(ivBanner);
                        }


                        Log.e("HomeFragment", "Network or API call failure: " + t.getMessage(), t);
                        tvErrorMessage.setVisibility(View.VISIBLE);
                        btnRetry.setVisibility(View.VISIBLE);
                        tvErrorMessage.setText("Kesalahan jaringan: " + t.getMessage());
                        Toast.makeText(getContext(), "Kesalahan jaringan.", Toast.LENGTH_LONG).show();
                        if (adapter.getItemCount() == 0) {
                            loadProductsFromSharedPreferences();
                        }
                    });
                }
            });
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        return true;
                    }
                }
            } else {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        }
        return false;
    }

    private void saveProductsToSharedPreferences(List<FoodDrinkItem> products) {
        SharedPreferences preferences = requireActivity().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String jsonItems = gson.toJson(products);
        editor.putString("cached_mc_products", jsonItems);
        editor.apply();
        Log.d("HomeFragment", "Data saved to SharedPreferences: " + products.size() + " items");
    }

    private void loadProductsFromSharedPreferences() {
        SharedPreferences preferences = requireActivity().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE);
        String jsonItems = preferences.getString("cached_mc_products", null);

        if (jsonItems != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<FoodDrinkItem>>(){}.getType();
            List<FoodDrinkItem> cachedItems = gson.fromJson(jsonItems, listType);
            if (cachedItems != null && !cachedItems.isEmpty()) {
                adapter.updateData(cachedItems);
                rvItems.setVisibility(View.VISIBLE);
                tvErrorMessage.setVisibility(View.GONE);
                btnRetry.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Menampilkan data offline dari cache. (" + cachedItems.size() + " items)", Toast.LENGTH_SHORT).show();
                hasMoreData = false;
                Log.d("HomeFragment", "Data loaded from SharedPreferences: " + cachedItems.size() + " items");
                return;
            }
        }
        tvErrorMessage.setVisibility(View.VISIBLE);
        tvErrorMessage.setText("Tidak ada data yang tersedia. Silakan cek koneksi internet.");
        btnRetry.setVisibility(View.VISIBLE);
        rvItems.setVisibility(View.GONE);
        Log.d("HomeFragment", "No data in SharedPreferences or failed to load from it.");
    }

    private List<CategoryItem> getDummyCategories() {
        List<CategoryItem> categories = new ArrayList<>();

        categories.add(new CategoryItem("All", R.drawable.action));
        categories.add(new CategoryItem("Burgers", R.drawable.burger));
        categories.add(new CategoryItem("Pizza", R.drawable.pizza));
        categories.add(new CategoryItem("Pasta", R.drawable.spaghetti));
        categories.add(new CategoryItem("Desserts", R.drawable.sweets));
        categories.add(new CategoryItem("Drinks", R.drawable.softdrink));
        categories.add(new CategoryItem("Chicken", R.drawable.friedchicken));
        categories.add(new CategoryItem("Salad", R.drawable.salad));
        return categories;
    }

    @Override
    public void onItemClick(FoodDrinkItem item) {
        Intent intent = new Intent(getContext(), DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_ITEM_ID, String.valueOf(item.getId()));
        intent.putExtra(DetailActivity.EXTRA_ITEM_NAME, item.getName());
        intent.putExtra(DetailActivity.EXTRA_ITEM_DESCRIPTION, item.getDescription());
        intent.putExtra(DetailActivity.EXTRA_ITEM_IMAGE_URL, item.getImageUrl());

        String formattedPrice = "Harga Tidak Tersedia";
        if (item.getPrice() != null) {
            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
            formattedPrice = format.format(item.getPrice());
        }
        intent.putExtra(DetailActivity.EXTRA_ITEM_PRICE, formattedPrice);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    @Override
    public void onCategoryClick(CategoryItem category) {

        if (category.getName().equals("All")) {
            currentQuery = "all";
        } else {
            currentQuery = category.getName();
        }
        currentOffset = 0;
        hasMoreData = true;
        adapter.updateData(new ArrayList<>());
        Toast.makeText(getContext(), "Memuat: " + category.getName(), Toast.LENGTH_SHORT).show();
        fetchMenuItems(currentQuery, ITEMS_PER_PAGE, currentOffset, true);
        searchViewMenu.setQuery(currentQuery, false);
        searchViewMenu.clearFocus();
    }
}