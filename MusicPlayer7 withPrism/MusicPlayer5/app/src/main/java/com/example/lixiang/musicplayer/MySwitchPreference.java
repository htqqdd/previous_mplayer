package com.example.lixiang.musicplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import java.util.ArrayList;

/**
 * Created by lixiang on 2017/8/4.
 */

public class MySwitchPreference extends SwitchPreference {
    private View mView;
    public MySwitchPreference(Context context) {
        super(context);
    }


    public MySwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MySwitchPreference(Context context, AttributeSet attrs,
                                int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
//        mView = view;
//        new setColorTask().execute();

    }

    private class setColorTask extends AsyncTask{
        @Override
        protected Object doInBackground(Object[] objects) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            String accent_color = sharedPref.getString("accent_color", "");
            switch (accent_color) {
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
            return R.color.md_pink_500;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (Build.VERSION.SDK_INT >= 23) {
                int color1 = getContext().getResources().getColor(R.color.md_grey_50);
                int color2 = getContext().getResources().getColor((int) o);
                int states[][] = {{android.R.attr.state_checked}, {}};
                int colors[] = {color2, color1};
                int color3 = getContext().getResources().getColor(R.color.md_grey_500);
                int colors2[] = {color2, color3};
                Switch theSwitch = (Switch) mView.findViewById(android.R.id.switch_widget);
                theSwitch.setThumbTintList(new ColorStateList(states, colors));
                theSwitch.setTrackTintList(new ColorStateList(states, colors2));
            }
        }
    }
}
