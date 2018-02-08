package com.example.lixiang.musicplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by lixiang on 2017/8/4.
 */

public class MyPreferenceCategory extends PreferenceCategory {
    private View mView;

    public MyPreferenceCategory(Context context) {
        super(context);
    }

    public MyPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyPreferenceCategory(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mView = view;
        new setColorTask().execute();
    }

    @Override
    public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener) {
        super.setOnPreferenceChangeListener(onPreferenceChangeListener);
        new setColorTask().execute();
    }

    private class setColorTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            String primary_color = sharedPref.getString("accent_color", "");
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
            TextView titleView = (TextView) mView.findViewById(android.R.id.title);
            titleView.setTextColor(getContext().getResources().getColor((int) o));
        }
    }
}
