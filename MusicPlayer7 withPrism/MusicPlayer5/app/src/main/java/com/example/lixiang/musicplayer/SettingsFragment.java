package com.example.lixiang.musicplayer;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.stylingandroid.prism.Prism;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Prism prism;
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);

        // --- 创建 Prism 实例 ---------------------
        AppBarLayout appBarLayout = (AppBarLayout) getActivity().findViewById(R.id.settingsbarlayout);
        prism = Prism.Builder.newInstance()
                .background(appBarLayout)
                .background(getActivity().getWindow())
                .build();
        // ----------------------------------------

        new setColorTask().execute();

    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (key.equals("primary_color")) {
            String primary_color = sharedPref.getString("primary_color", "");
            Log.v("颜色","颜色"+primary_color);
            switch (primary_color) {
                case "red":
                    prism.setColor(getResources().getColor(R.color.md_red_500));
                    break;
                case "pink":
                    prism.setColor(getResources().getColor(R.color.md_pink_500));
                    break;
                case "purple":
                    prism.setColor(getResources().getColor(R.color.md_purple_500));
                    break;
                case "deep_purple":
                    prism.setColor(getResources().getColor(R.color.md_deep_purple_500));
                    break;
                case "indigo":
                    prism.setColor(getResources().getColor(R.color.md_indigo_500));
                    break;
                case "blue":
                    prism.setColor(getResources().getColor(R.color.md_blue_500));
                    break;
                case "light_blue":
                    prism.setColor(getResources().getColor(R.color.md_light_blue_500));
                    break;
                case "cyan":
                    prism.setColor(getResources().getColor(R.color.md_cyan_500));
                    break;
                case "teal":
                    prism.setColor(getResources().getColor(R.color.md_teal_500));
                    break;
                case "green":
                    prism.setColor(getResources().getColor(R.color.md_green_500));
                    break;
                case "light_green":
                    prism.setColor(getResources().getColor(R.color.md_light_green_500));
                    break;
                case "lime":
                    prism.setColor(getResources().getColor(R.color.md_lime_500));
                    break;
                case "yellow":
                    prism.setColor(getResources().getColor(R.color.md_yellow_500));
                    break;
                case "amber":
                    prism.setColor(getResources().getColor(R.color.md_amber_500));
                    break;
                case "orange":
                    prism.setColor(getResources().getColor(R.color.md_orange_500));
                    break;
                case "deep_orange":
                    prism.setColor(getResources().getColor(R.color.md_deep_orange_500));
                    break;
                default:
            }
            //重启界面
//            Intent restart_intent = getActivity().getPackageManager().getLaunchIntentForPackage(getActivity().getPackageName());
//            restart_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(restart_intent);
        }
//        if (key.equals("accent_color")) {
//            String accent_color = sharedPref.getString("accent_color", "");
//            Log.v("颜色","颜色"+accent_color);
//            switch (accent_color) {
//                case "red":
//                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.RED).apply();
//                    break;
//                case "pink":
//                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.PINK).apply();
//                    break;
//                case "purple":
//                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.PURPLE).apply();
//                    break;
//                case "deep_purple":
//                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.DEEP_PURPLE).apply();
//                    break;
//                case "indigo":
//                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.INDIGO).apply();
//                    break;
//                case "blue":
//                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.BLUE).apply();
//                    break;
//                case "light_blue":
//                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.LIGHT_BLUE).apply();
//                    break;
//                case "cyan":
//                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.CYAN).apply();
//                    break;
//                case "teal":
//                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.TEAL).apply();
//                    break;
//                case "green":
//                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.GREEN).apply();
//                    break;
//                case "light_green":
//                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.LIGHT_GREEN).apply();
//                    break;
//                case "lime":
//                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.LIME).apply();
//                    break;
//                case "yellow":
//                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.YELLOW).apply();
//                    break;
//                case "amber":
//                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.AMBER).apply();
//                    break;
//                case "orange":
//                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.ORANGE).apply();
//                    break;
//                case "deep_orange":
//                    Colorful.config(getActivity()).accentColor(Colorful.ThemeColor.CYAN).apply();
//                    break;
//                default:
//            }
//            //重启界面
//            Intent restart_intent = getActivity().getPackageManager().getLaunchIntentForPackage(getActivity().getPackageName());
//            restart_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(restart_intent);
//        }
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

    private class setColorTask extends AsyncTask{
        @Override
        protected Object doInBackground(Object[] objects) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String primary_color = sharedPref.getString("primary_color", "");
            switch (primary_color) {
                case "red":
                    return R.color.md_red_500;
                case "pink":
                    return R.color.md_pink_500;
                case "purple":
                    return R.color.md_purple_500;
                case "deep_purple":
                    return R.color.md_deep_purple_500;
                case "indigo":
                    return R.color.md_indigo_500;
                case "blue":
                    return R.color.md_blue_500;
                case "light_blue":
                    return R.color.md_light_blue_500;
                case "cyan":
                    return R.color.md_cyan_500;
                case "teal":
                    return R.color.md_teal_500;
                case "green":
                    return R.color.md_green_500;
                case "light_green":
                    return R.color.md_light_green_500;
                case "lime":
                    return R.color.md_lime_500;
                case "yellow":
                    return R.color.md_yellow_500;
                case "amber":
                    return R.color.md_amber_500;
                case "orange":
                    return R.color.md_orange_500;
                case "deep_orange":
                    return R.color.md_deep_orange_500;
                default:
            }
            return R.color.md_teal_500;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            prism.setColor(getResources().getColor((int)o));
        }
    }
}
