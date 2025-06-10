package com.example.footenddrink.ui.ordersummary;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.footenddrink.LoginActivity;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footenddrink.R;
import com.example.footenddrink.data.CartItem;
import com.example.footenddrink.data.CartManager;
import com.example.footenddrink.ui.cart.CartAdapter;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public class OrderSummaryFragment extends Fragment {

    private RecyclerView rvSummaryItems;
    private CartAdapter cartAdapter;
    private TextView tvEmptySummary;
    private TextView tvSubtotalSummary;
    private TextView tvDeliveryFeeSummary;
    private TextView tvFinalTotalSummary;
    private Button btnPlaceOrder;

    private CartManager cartManager;
    private static final double DELIVERY_FEE = 15000;

    private static final String GOOGLE_APPS_SCRIPT_URL = "https://script.google.com/macros/s/AKfycbwVtrJxdVY8hE36VmhQZEdsoK08NavpB7fzWQVuMzyX3n3bcyDiUVj6RUsAUlp7lb9H/exec"; // <-- INI YANG HARUS DIGANTI!


    public interface GoogleSheetsApiService {
        @FormUrlEncoded
        @POST("exec")
        Call<Map<String, String>> sendOrderData(@Field("data") String jsonData);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_summary, container, false);

        rvSummaryItems = view.findViewById(R.id.rv_summary_items);
        tvEmptySummary = view.findViewById(R.id.tv_empty_summary);
        tvSubtotalSummary = view.findViewById(R.id.tv_subtotal_summary);
        tvDeliveryFeeSummary = view.findViewById(R.id.tv_delivery_fee_summary);
        tvFinalTotalSummary = view.findViewById(R.id.tv_final_total_summary); // Pastikan ID ini benar di layout
        btnPlaceOrder = view.findViewById(R.id.btn_place_order);

        cartManager = CartManager.getInstance(getContext());
        rvSummaryItems.setLayoutManager(new LinearLayoutManager(getContext()));

        cartAdapter = new com.example.footenddrink.ui.cart.CartAdapter(cartManager.getCartItems(), null);
        rvSummaryItems.setAdapter(cartAdapter);

        btnPlaceOrder.setOnClickListener(v -> placeOrder());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSummaryUI();
    }

    private void updateSummaryUI() {
        List<CartItem> currentCart = cartManager.getCartItems();
        if (currentCart.isEmpty()) {
            rvSummaryItems.setVisibility(View.GONE);
            tvEmptySummary.setVisibility(View.VISIBLE);
            tvSubtotalSummary.setText("Rp 0");
            tvDeliveryFeeSummary.setText("Rp 0");
            tvFinalTotalSummary.setText("Rp 0");
            btnPlaceOrder.setEnabled(false);
        } else {
            rvSummaryItems.setVisibility(View.VISIBLE);
            tvEmptySummary.setVisibility(View.GONE);
            btnPlaceOrder.setEnabled(true);
            cartAdapter.updateData(currentCart);

            double subtotal = cartManager.getTotalPrice();
            double finalTotal = subtotal + DELIVERY_FEE;

            NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));

            tvSubtotalSummary.setText(format.format(subtotal));
            tvDeliveryFeeSummary.setText(format.format(DELIVERY_FEE));
            tvFinalTotalSummary.setText(format.format(finalTotal));
        }
    }

    private void placeOrder() {
        if (cartManager.getCartItemCount() > 0) {
            sendOrderToGoogleSheets();
        } else {
            Toast.makeText(getContext(), "Keranjang Anda kosong. Tidak ada yang bisa dipesan.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendOrderToGoogleSheets() {
        btnPlaceOrder.setEnabled(false);
        Toast.makeText(getContext(), "Mengirim pesanan...", Toast.LENGTH_SHORT).show();

        String orderId = "ORDER_" + System.currentTimeMillis();
        String username = requireActivity().getSharedPreferences(LoginActivity.AUTH_PREFS, Context.MODE_PRIVATE)
                .getString(LoginActivity.KEY_USERNAME, "Guest");
        double totalPrice = cartManager.getTotalPrice() + DELIVERY_FEE;
        List<CartItem> itemsInCart = cartManager.getCartItems();

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", orderId);
        orderData.put("username", username);
        orderData.put("totalPrice", NumberFormat.getCurrencyInstance(new Locale("in", "ID")).format(totalPrice));

        List<Map<String, String>> itemList = new ArrayList<>();
        for (CartItem item : itemsInCart) {
            Map<String, String> itemMap = new HashMap<>();
            itemMap.put("name", item.getName());
            itemMap.put("quantity", String.valueOf(item.getQuantity()));
            itemMap.put("price", item.getPrice());
            itemList.add(itemMap);
        }
        orderData.put("items", itemList);

        Gson gson = new Gson();
        String jsonPayload = gson.toJson(orderData);
        Log.d("OrderSummary", "JSON Payload: " + jsonPayload);


        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);

        String baseUrl;
        try {

            baseUrl = GOOGLE_APPS_SCRIPT_URL.substring(0, GOOGLE_APPS_SCRIPT_URL.lastIndexOf("/") + 1);
        } catch (StringIndexOutOfBoundsException e) {
            Log.e("OrderSummary", "GOOGLE_APPS_SCRIPT_URL is malformed or empty: " + GOOGLE_APPS_SCRIPT_URL, e);
            Toast.makeText(getContext(), "URL Google Sheets API tidak valid! Periksa konsol.", Toast.LENGTH_LONG).show();
            btnPlaceOrder.setEnabled(true);
            return;
        }


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        GoogleSheetsApiService apiService = retrofit.create(GoogleSheetsApiService.class);

        Call<Map<String, String>> call = apiService.sendOrderData(jsonPayload);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                btnPlaceOrder.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    Map<String, String> result = response.body();
                    String status = result.get("status");
                    String message = result.get("message");
                    Log.d("OrderSummary", "Google Sheets API Response: Status=" + status + ", Message=" + message);

                    if ("SUCCESS".equals(status)) {
                        Toast.makeText(getContext(), "Pesanan berhasil ditempatkan & tercatat!", Toast.LENGTH_LONG).show();
                        cartManager.clearCart();

                        boolean popped = NavHostFragment.findNavController(OrderSummaryFragment.this).popBackStack(R.id.homeFragment, true); // true = inclusive, homeFragment juga di-pop

                        if (popped) {
                            Log.d("OrderSummary", "Successfully popped back to HomeFragment.");
                        } else {

                            Log.w("OrderSummary", "PopBackStack to HomeFragment failed or HomeFragment was not in stack. Redirecting to MainActivity.");
                            android.content.Intent intent = new android.content.Intent(requireActivity(), com.example.footenddrink.MainActivity.class);
                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            requireActivity().finish();
                        }

                    } else {
                        Toast.makeText(getContext(), "Gagal mencatat pesanan: " + message, Toast.LENGTH_LONG).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e("OrderSummary", "Google Sheets API Failed: Code=" + response.code() + ", Body=" + errorBody);
                        Toast.makeText(getContext(), "Gagal mengirim pesanan (Code: " + response.code() + ")", Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Gagal mengirim pesanan (IO Error)", Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                btnPlaceOrder.setEnabled(true);
                Log.e("OrderSummary", "Google Sheets API Network Failure: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Gagal mengirim pesanan (Jaringan): " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}