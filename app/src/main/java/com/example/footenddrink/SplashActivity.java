package com.example.footenddrink;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_TIME_OUT = 2000;
    private static final String TAG = "SplashDebug";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = getSharedPreferences(LoginActivity.AUTH_PREFS, Context.MODE_PRIVATE);
                boolean isLoggedIn = prefs.getBoolean(LoginActivity.KEY_IS_LOGGED_IN, false);
                Log.d(TAG, "Status login dari SharedPreferences: " + isLoggedIn);

                Intent i;
                if (isLoggedIn) {
                    i = new Intent(SplashActivity.this, MainActivity.class);
                    Log.d(TAG, "Mengarahkan ke MainActivity (sudah login).");
                } else {
                    i = new Intent(SplashActivity.this, LoginActivity.class);
                    Log.d(TAG, "Mengarahkan ke LoginActivity (belum login).");
                }
                startActivity(i);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}