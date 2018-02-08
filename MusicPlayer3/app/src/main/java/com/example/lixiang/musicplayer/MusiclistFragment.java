package com.example.lixiang.musicplayer;


import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import static android.R.attr.fragment;
import static com.example.lixiang.musicplayer.Data.initialize;
import static com.example.lixiang.musicplayer.Data.next;
import static com.example.lixiang.musicplayer.Data.pausing;
import static com.example.lixiang.musicplayer.Data.play;
import static com.example.lixiang.musicplayer.Data.playing;
import static com.example.lixiang.musicplayer.Data.previous;
import static com.example.lixiang.musicplayer.Data.resuming;
import static com.example.lixiang.musicplayer.Data.seekto;
import static com.example.lixiang.musicplayer.R.id.main_song_title;
import static com.example.lixiang.musicplayer.getCover.getArtwork;
import static com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState.COLLAPSED;
import static com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState.EXPANDED;

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
                intent.putExtra("ACTION", play);
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
                Intent intent = new Intent();
                intent.putExtra("ACTION", play);
                intent.setClass(getActivity(), PlayService.class);
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


}
