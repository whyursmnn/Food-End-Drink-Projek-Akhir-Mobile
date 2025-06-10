package com.example.footenddrink.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.footenddrink.LoginActivity;
import com.example.footenddrink.MainActivity;
import com.example.footenddrink.R;

public class SettingsFragment extends Fragment {

    private RadioGroup rgTheme;
    private RadioButton rbLightTheme;
    private RadioButton rbDarkTheme;
    private Button btnLogout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        rgTheme = view.findViewById(R.id.rg_theme);
        rbLightTheme = view.findViewById(R.id.rb_light_theme);
        rbDarkTheme = view.findViewById(R.id.rb_dark_theme);
        btnLogout = view.findViewById(R.id.btn_logout);

        SharedPreferences preferences = requireActivity().getSharedPreferences(MainActivity.PREFS_NAME, requireActivity().MODE_PRIVATE);
        boolean useDarkTheme = preferences.getBoolean(MainActivity.PREF_DARK_THEME, false);

        if (useDarkTheme) {
            rbDarkTheme.setChecked(true);
        } else {
            rbLightTheme.setChecked(true);
        }

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            SharedPreferences.Editor editor = preferences.edit();
            if (checkedId == R.id.rb_dark_theme) {
                editor.putBoolean(MainActivity.PREF_DARK_THEME, true);
            } else {
                editor.putBoolean(MainActivity.PREF_DARK_THEME, false);
            }
            editor.apply();

            requireActivity().recreate();
        });


        btnLogout.setOnClickListener(v -> {
            performLogout();
        });


        return view;
    }


    private void performLogout() {

        SharedPreferences authPrefs = requireActivity().getSharedPreferences(LoginActivity.AUTH_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = authPrefs.edit();


        Toast.makeText(getContext(), "Berhasil logout", Toast.LENGTH_SHORT).show();


        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

}