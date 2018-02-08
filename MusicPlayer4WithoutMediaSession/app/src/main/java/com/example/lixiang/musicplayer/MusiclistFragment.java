package com.example.lixiang.musicplayer;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import static com.example.lixiang.musicplayer.Data.mediaChangeAction;
import static com.example.lixiang.musicplayer.Data.playAction;
import static com.example.lixiang.musicplayer.Data.shuffleChangeAction;


/**
 * A simple {@link Fragment} subclass.
 */
public class MusiclistFragment extends Fragment {

    private ListView listView;// 列表对象
    private MusicListAdapter musicListAdapter;
    private View rootView;
    private RelativeLayout random_play_title;


    public MusiclistFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        rootView = inflater.inflate(R.layout.fragment_musiclist, container, false);
        //动态注册广播
        PermissionReceiver permissionReceiver = new PermissionReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("permission_granted");
        getActivity().registerReceiver(permissionReceiver, intentFilter);

        //动态注册广播
        ListviewFilterReceiver listviewFilterReceiver= new ListviewFilterReceiver();
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
        UIReceiver uiReceiver = new UIReceiver();
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
            Data.initialMusicInfo(getActivity());
            showMusicList();
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
