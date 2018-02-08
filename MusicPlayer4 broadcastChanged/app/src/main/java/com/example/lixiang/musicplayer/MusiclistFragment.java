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
import static com.example.lixiang.musicplayer.R.id.random_play_button;
import static com.example.lixiang.musicplayer.R.id.random_play_text;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusiclistFragment extends Fragment {

    private ListView listView;// 列表对象
    private MusicListAdapter musicListAdapter;
    private View rootView;
    private RelativeLayout random_play_title;
    private list_PermissionReceiver list_permissionReceiver;
    private ListviewFilterReceiver listviewFilterReceiver;
    private UIReceiver uiReceiver;


    public MusiclistFragment() {
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(list_permissionReceiver);
        getActivity().unregisterReceiver(listviewFilterReceiver);
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

        //动态注册广播
        listviewFilterReceiver= new ListviewFilterReceiver();
        IntentFilter Filter = new IntentFilter();
        intentFilter.addAction("listview_filter");
        getActivity().registerReceiver(listviewFilterReceiver, Filter);

        rootView = inflater.inflate(R.layout.fragment_musiclist, container, false);
        listView = (ListView) rootView.findViewById(R.id.music_list);
        new getColorTask().execute();


//        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//            showMusicList();
//        }


        //获取listview
        listView = (ListView) rootView.findViewById(R.id.music_list);
        //Listview点击，打开服务
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent("service_broadcast");
                Data.setPlayMode(3);
                Data.setFavourite(false);
                Data.setRecent(false);
                intent.putExtra("ACTION", playAction);
                Data.setPosition(position);
                getActivity().sendBroadcast(intent);
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
                Intent intent = new Intent("service_broadcast");
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


    public void test(View v) {
        Toast.makeText(getActivity(), "test", Toast.LENGTH_SHORT).show();
    }

    private MusicListAdapter.MyClickListener mListener = new MusicListAdapter.MyClickListener() {
        @Override
        public void myOnClick(final int position, View v) {
            menu_util.popupMenu(getActivity(), v, position);
        }
    };

    private class list_PermissionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //显示界面
            Log.v("接收初始广播","接收");
            new showMusicListTask().execute();
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
        TextView random_play_text = (TextView) rootView.findViewById(R.id.random_play_text);
        ImageView random_play_button = (ImageView) rootView.findViewById(R.id.random_play_button);
        random_play_button.setColorFilter(getResources().getColor((int)o));
        random_play_text.setTextColor(getResources().getColor((int)o));
        Data.setColorAccentSetted((int)o);
    }
}
private class showMusicListTask extends AsyncTask{
    @Override
    protected Object doInBackground(Object[] objects) {
        musicListAdapter = new MusicListAdapter(getActivity(), Data.getCursor(), mListener);
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        listView.setAdapter(musicListAdapter);
        listView.setTextFilterEnabled(true);
    }
}

}
