package com.example.lixiang.musicplayer;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.polaric.colorful.Colorful;

import static com.example.lixiang.musicplayer.Data.mediaChangeAction;
import static com.example.lixiang.musicplayer.Data.playAction;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusiclistFragment extends Fragment {

    private FastScrollRecyclerView fastScrollRecyclerView;// 列表对象
    private View rootView;
    private RelativeLayout random_play_title;
    private list_PermissionReceiver list_permissionReceiver;
    private UIReceiver uiReceiver;


    public MusiclistFragment() {
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(list_permissionReceiver);
        getActivity().unregisterReceiver(uiReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //动态注册广播
        list_permissionReceiver = new list_PermissionReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("list_permission_granted");
        getActivity().registerReceiver(list_permissionReceiver, intentFilter);


        rootView = inflater.inflate(R.layout.fragment_musiclist, container, false);
        fastScrollRecyclerView = (FastScrollRecyclerView) rootView.findViewById(R.id.fastScrollRecyclerView);
        new getColorTask().execute();


        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            showMusicList();
            Log.v("执行显示2","执行2");
        }


//删除歌曲更新界面广播
        uiReceiver = new UIReceiver();
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("ChangeUI_broadcast");
        getActivity().registerReceiver(uiReceiver, intentFilter2);
        return rootView;


    }

    private class UIReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //重启应用和界面
            Intent restart_intent = getActivity().getPackageManager().getLaunchIntentForPackage(getActivity().getPackageName());
            restart_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(restart_intent);
        }
    }


    private class list_PermissionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //显示界面
            Log.v("接收初始广播","接收");
            showMusicList();
        }
    }


public class getColorTask extends AsyncTask{
    @Override
    protected Object doInBackground(Object[] objects) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
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
        int color = getResources().getColor((int)o);
        fastScrollRecyclerView.setThumbColor(color);
        fastScrollRecyclerView.setPopupBgColor(color);
        Data.setColorAccentSetted((int) o);
    }
}
    private void showMusicList(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        fastScrollRecyclerView.setLayoutManager(layoutManager);
        fastScrollRecyclerView.setItemAnimator(new DefaultItemAnimator());
        FastScrollListAdapter adapter = new FastScrollListAdapter();
        fastScrollRecyclerView.setAdapter(adapter);
    }

}
