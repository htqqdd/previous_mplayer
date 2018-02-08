package com.example.lixiang.music_player;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.afollestad.aesthetic.Aesthetic;
import com.afollestad.aesthetic.NavigationViewMode;
import com.bumptech.glide.Glide;

import static com.example.lixiang.music_player.R.color.text_color_secondary;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preference);


    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (key.equals("default_color")) {
            Aesthetic.get().colorPrimary(sharedPref.getInt("default_color", 0)).colorStatusBarAuto().colorNavigationBarAuto().apply();
        }
        if (key.equals("accent_color")) {
            Aesthetic.get().colorAccent(sharedPref.getInt("accent_color", 0)).colorStatusBarAuto().colorNavigationBarAuto().apply();
        }
        if (key.equals("main_theme")) {
            Aesthetic.get().isDark(true).activityTheme(R.style.AppDarkTheme).colorCardViewBackgroundRes(R.color.night_cardview).apply();
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View navHeader = inflater.inflate(R.layout.nav_header,(ViewGroup) getActivity().findViewById(R.id.nav_header));
            ImageView imageView = (ImageView) navHeader.findViewById(R.id.header);
            if (sharedPref.getString("main_theme", null).equals("night")){
                Glide.with(getActivity()).load(R.drawable.nav_black).into(imageView);
//                imageView.setImageResource(R.drawable.nav_black);
            }else if (sharedPref.getString("main_theme", null).equals("day")) {
                Aesthetic.get().isDark(false).activityTheme(R.style.AppTheme).colorCardViewBackgroundRes(R.color.day_cardview).apply();
                Glide.with(getActivity()).load(R.drawable.nav).into(imageView);
//                imageView.setImageResource(R.drawable.nav);
            }else {
                Aesthetic.get().isDark(true).activityTheme(R.style.AppAmoledTheme).colorCardViewBackgroundRes(R.color.night_cardview).apply();
                Glide.with(getActivity()).load(R.drawable.nav_black).into(imageView);
//                imageView.setImageResource(R.drawable.nav_black);
            }
            Data.firstOpen = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

}
