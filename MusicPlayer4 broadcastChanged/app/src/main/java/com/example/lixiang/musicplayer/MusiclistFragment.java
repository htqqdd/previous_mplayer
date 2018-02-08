package com.example.lixiang.musicplayer;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

import org.polaric.colorful.Colorful;

import static com.example.lixiang.musicplayer.Data.mediaChangeAction;
import static com.example.lixiang.musicplayer.Data.playAction;
import static com.example.lixiang.musicplayer.Data.shuffleChangeAction;
import static com.example.lixiang.musicplayer.R.id.random_play_text;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusiclistFragment extends Fragment {

    private ListView listView;// 列表对象
    private MusicListAdapter musicListAdapter;
    private View rootView;
    private RelativeLayout random_play_title;
    private PermissionReceiver permissionReceiver;
    private ListviewFilterReceiver listviewFilterReceiver;
    private UIReceiver uiReceiver;


    public MusiclistFragment() {
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(permissionReceiver);
        getActivity().unregisterReceiver(listviewFilterReceiver);
        getActivity().unregisterReceiver(uiReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        rootView = inflater.inflate(R.layout.fragment_musiclist, container, false);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String accent_color = sharedPref.getString("accent_color", "");
        String primary_color = sharedPref.getString("primary_color", "");
        TextView random_play_text = (TextView) rootView.findViewById(R.id.random_play_text);
        ImageView random_play_button = (ImageView) rootView.findViewById(R.id.random_play_button);
        switch (primary_color) {
            case "red":
                Data.setColorPrimarySetted(R.color.md_red_500);
                break;
            case "pink":
                Data.setColorPrimarySetted(R.color.md_pink_500);
                break;
            case "purple":
                Data.setColorPrimarySetted(R.color.md_purple_500);
                break;
            case "deep_purple":
                Data.setColorPrimarySetted(R.color.md_deep_purple_500);
                break;
            case "indigo":
                Data.setColorPrimarySetted(R.color.md_indigo_500);
                break;
            case "blue":
                Data.setColorPrimarySetted(R.color.md_blue_500);
                break;
            case "light_blue":
                Data.setColorPrimarySetted(R.color.md_light_blue_500);
                break;
            case "cyan":
                Data.setColorPrimarySetted(R.color.md_cyan_500);
                break;
            case "teal":
                Data.setColorPrimarySetted(R.color.md_teal_500);
                break;
            case "green":
                Data.setColorPrimarySetted(R.color.md_green_500);
                break;
            case "light_green":
                Data.setColorPrimarySetted(R.color.md_light_green_500);
                break;
            case "lime":
                Data.setColorPrimarySetted(R.color.md_lime_500);
                break;
            case "yellow":
                Data.setColorPrimarySetted(R.color.md_yellow_500);
                break;
            case "amber":
                Data.setColorPrimarySetted(R.color.md_amber_500);
                break;
            case "orange":
                Data.setColorPrimarySetted(R.color.md_orange_500);
                break;
            case "deep_orange":
                Data.setColorPrimarySetted(R.color.md_deep_orange_500);
                break;
            default:
        }
        switch (accent_color) {
            case "red":
                Data.setColorAccentSetted(R.color.md_red_500);
                random_play_button.setColorFilter(getResources().getColor(R.color.md_red_500));
                random_play_text.setTextColor(getResources().getColor(R.color.md_red_500));
                break;
            case "pink":
                Data.setColorAccentSetted(R.color.md_pink_500);
                random_play_button.setColorFilter(getResources().getColor(R.color.md_pink_500));
                random_play_text.setTextColor(getResources().getColor(R.color.md_pink_500));
                break;
            case "purple":
                Data.setColorAccentSetted(R.color.md_purple_500);
                random_play_button.setColorFilter(getResources().getColor(R.color.md_purple_500));
                random_play_text.setTextColor(getResources().getColor(R.color.md_purple_500));
                break;
            case "deep_purple":
                Data.setColorAccentSetted(R.color.md_deep_purple_500);
                random_play_button.setColorFilter(getResources().getColor(R.color.md_deep_purple_500));
                random_play_text.setTextColor(getResources().getColor(R.color.md_deep_purple_500));
                break;
            case "indigo":
                Data.setColorAccentSetted(R.color.md_indigo_500);
                random_play_button.setColorFilter(getResources().getColor(R.color.md_indigo_500));
                random_play_text.setTextColor(getResources().getColor(R.color.md_indigo_500));
                break;
            case "blue":
                Data.setColorAccentSetted(R.color.md_blue_500);
                random_play_button.setColorFilter(getResources().getColor(R.color.md_blue_500));
                random_play_text.setTextColor(getResources().getColor(R.color.md_blue_500));
                break;
            case "light_blue":
                Data.setColorAccentSetted(R.color.md_light_blue_500);
                random_play_button.setColorFilter(getResources().getColor(R.color.md_light_blue_500));
                random_play_text.setTextColor(getResources().getColor(R.color.md_light_blue_500));
                break;
            case "cyan":
                Data.setColorAccentSetted(R.color.md_cyan_500);
                random_play_button.setColorFilter(getResources().getColor(R.color.md_cyan_500));
                random_play_text.setTextColor(getResources().getColor(R.color.md_cyan_500));
                break;
            case "teal":
                Data.setColorAccentSetted(R.color.md_teal_500);
                random_play_button.setColorFilter(getResources().getColor(R.color.md_teal_500));
                random_play_text.setTextColor(getResources().getColor(R.color.md_teal_500));
                break;
            case "green":
                Data.setColorAccentSetted(R.color.md_green_500);
                random_play_button.setColorFilter(getResources().getColor(R.color.md_green_500));
                random_play_text.setTextColor(getResources().getColor(R.color.md_green_500));
                break;
            case "light_green":
                Data.setColorAccentSetted(R.color.md_light_green_500);
                random_play_button.setColorFilter(getResources().getColor(R.color.md_light_green_500));
                random_play_text.setTextColor(getResources().getColor(R.color.md_light_green_500));
                break;
            case "lime":
                Data.setColorAccentSetted(R.color.md_lime_500);
                random_play_button.setColorFilter(getResources().getColor(R.color.md_lime_500));
                random_play_text.setTextColor(getResources().getColor(R.color.md_lime_500));
                break;
            case "yellow":
                Data.setColorAccentSetted(R.color.md_yellow_500);
                random_play_button.setColorFilter(getResources().getColor(R.color.md_yellow_500));
                random_play_text.setTextColor(getResources().getColor(R.color.md_yellow_500));
                break;
            case "amber":
                Data.setColorAccentSetted(R.color.md_amber_500);
                random_play_button.setColorFilter(getResources().getColor(R.color.md_amber_500));
                random_play_text.setTextColor(getResources().getColor(R.color.md_amber_500));
                break;
            case "orange":
                Data.setColorAccentSetted(R.color.md_orange_500);
                random_play_button.setColorFilter(getResources().getColor(R.color.md_orange_500));
                random_play_text.setTextColor(getResources().getColor(R.color.md_orange_500));
                break;
            case "deep_orange":
                Data.setColorAccentSetted(R.color.md_deep_orange_500);
                random_play_button.setColorFilter(getResources().getColor(R.color.md_deep_orange_500));
                random_play_text.setTextColor(getResources().getColor(R.color.md_deep_orange_500));
                break;
            default:
        }
        //动态注册广播
        permissionReceiver = new PermissionReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("permission_granted");
        getActivity().registerReceiver(permissionReceiver, intentFilter);

        //动态注册广播
        listviewFilterReceiver= new ListviewFilterReceiver();
        IntentFilter Filter = new IntentFilter();
        intentFilter.addAction("listview_filter");
        getActivity().registerReceiver(listviewFilterReceiver, Filter);

//        showMusicList();

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            showMusicList();
        }


        //获取listview
        listView = (ListView) rootView.findViewById(R.id.music_list);
        //Listview点击，打开服务
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent();
                Data.setPlayMode(3);
                Data.setFavourite(false);
                Data.setRecent(false);
                intent.putExtra("ACTION", playAction);
                Data.setPosition(position);
                intent.setClass(getActivity(), PlayService.class);
                getActivity().startService(intent);
            }
        });


        //随机播放点击栏
        random_play_title = (RelativeLayout) rootView.findViewById(R.id.random_play_title);
        random_play_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //随机播放
                Data.setPlayMode(1);
                Data.setFavourite(false);
                Data.setRecent(false);
                //打开服务播放
                Intent intent = new Intent(getActivity(), PlayService.class);
                intent.putExtra("ACTION", playAction);
                getActivity().startService(intent);
            }
        });


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
//            Data.initialMusicInfo(getActivity());
//            showMusicList();
//            display();

        }
    }

    public void showMusicList() {
        musicListAdapter = new MusicListAdapter(getActivity(), Data.getCursor(), mListener);
        listView = (ListView) rootView.findViewById(R.id.music_list);
        listView.setAdapter(musicListAdapter);
        listView.setTextFilterEnabled(true);
    }

    public void test(View v) {
        Toast.makeText(getActivity(), "test", Toast.LENGTH_SHORT).show();
    }

    private MusicListAdapter.MyClickListener mListener = new MusicListAdapter.MyClickListener() {
        @Override
        public void myOnClick(final int position, View v) {
            menu_util.popupMenu(getActivity(), v, position);
        }
    };

    private class PermissionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //显示界面
            showMusicList();
        }
    }

private class ListviewFilterReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        String newText = intent.getStringExtra("Filter");
        Log.v("Filter","Filter时"+newText);
        if (TextUtils.isEmpty((newText))){
            listView.clearTextFilter();
        }else{
            listView.setFilterText(newText);}
    }
}

}
