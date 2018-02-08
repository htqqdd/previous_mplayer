package com.example.lixiang.music;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

import static android.R.attr.data;
import static android.R.attr.path;
import static android.os.Build.VERSION_CODES.M;
import static com.example.lixiang.music.MainActivity.initialize;
import static com.example.lixiang.music.MainActivity.pausing;
import static com.example.lixiang.music.MainActivity.play;
import static com.example.lixiang.music.MainActivity.playing;
import static com.example.lixiang.music.MainActivity.randomplay;

public class PlayService extends Service {
    private Intent intent;
    private String path;
    private MediaPlayer mediaPlayer;
    private int[] _ids;// 保存音乐ID临时数组
    private int[] _albumids;
    private String[] _artists;// 保存艺术家
    private String[] _titles;
    private String[] _data;// 标题临时数组
    private Cursor cursor;
    private String[] media_music_info;
    private int position = 0;

    private ServiceReceiver serviceReceiver;
    public PlayService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        //初始化音乐信息
        media_music_info = new String[]{
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID};

        cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, media_music_info,
                null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        cursor.moveToFirst();// 将游标移动到初始位置
        _ids = new int[cursor.getCount()];// 返回int的一个列
        _artists = new String[cursor.getCount()];// 返回String的一个列
        _titles = new String[cursor.getCount()];// 返回String的一个列
        _data = new String[cursor.getCount()];
        _albumids = new int[cursor.getCount()];
        Log.v("total","总数"+cursor.getCount());
        for (int i = 0; i < cursor.getCount(); i++) {
            _ids[i] = cursor.getInt(3);
            _titles[i] = cursor.getString(0);
            _artists[i] = cursor.getString(2);
            _data[i] = cursor.getString(5);
            _albumids[i] = cursor.getInt(6);
            cursor.moveToNext();// 将游标移到下一行
        }
        //动态注册广播
        serviceReceiver = new ServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("service_broadcast");
        registerReceiver(serviceReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        releaseMedia();
        mediaPlayer = new MediaPlayer();
        if (intent.getIntExtra("ACTION",-2) == initialize){
            position = 0;
        }
        if (intent.getIntExtra("ACTION",-2) == play) {
            position = intent.getIntExtra("listPosition", 0);
            play(position);
        } else if (intent.getIntExtra("ACTION",-2) == randomplay){
            Data.setPlayMode(1);
            position = randomPosition();
            play(position);
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                next();

            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
//        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }
//release Media
    public void releaseMedia(){
        if (mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    //play
    public void play(int position){
        mediaPlayer.reset();
        path = _data[position];
        Log.v("position","play方法的position"+position);
        Intent intent = new Intent("play_broadcast");
        intent.putExtra("position",position);
        intent.putExtra("state",playing);
        sendBroadcast(intent);
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare(); // 进行缓冲
        } catch (Exception e) {
            e.printStackTrace();
        }
        Data.set_mediaDuration(mediaPlayer.getDuration());
        mediaPlayer.start();

    }
    //生成随机数[0-n)
    public int randomPosition(){
        Random random = new Random();
        int random_position = random.nextInt(cursor.getCount() -1);
        return  random_position;
    }
    private class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra("previous",0) == -1){
                previous();
            }
            if (intent.getIntExtra("next",0) == 1){
                next();
            }
            if (intent.getIntExtra("pause",0) == 2){
                pause();
            }
            if (intent.getIntExtra("resume",0) == 3){
                resume();
            }
        }

    }
    public void previous(){
        if (Data.getPlayMode() == 0){
            if (position == 0){
                position = cursor.getCount()-1;
                play(position);
            } else{
                position = position - 1;
                play(position);
            }
        } else if (Data.getPlayMode() ==1){
            position = randomPosition();
            play(position);
        } else if (Data.getPlayMode() ==2){
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
        } else {
            if (position == 0){
                Toast.makeText(PlayService.this, "没有上一曲了", Toast.LENGTH_SHORT).show();
            } else{
                position = position - 1;
                play(position);
            }
        }
    }
    public void next(){
        if (Data.getPlayMode() == 0){
            if (position < cursor.getCount()-1){
                position++;
                play(position);
            } else {
                position = 0;
                play(position);
            }

        }else if (Data.getPlayMode() == 1){
            position = randomPosition();
            play(position);
        }else if (Data.getPlayMode() == 2) {
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
        } else if (Data.getPlayMode() == 3){
            if (position >= cursor.getCount()-1){
                Log.v("position","此时位置"+position);
                Toast.makeText(PlayService.this, "没有下一曲了", Toast.LENGTH_SHORT).show();
            } else {
                position++;
                play(position);
            }

        }
    }
    public void pause(){
        mediaPlayer.pause();
        Intent intent = new Intent("play_broadcast");
        intent.putExtra("position",position);
        Log.v("position","暂停的position"+position);
        intent.putExtra("state",pausing);
        sendBroadcast(intent);
    }
    public void resume(){
        if (mediaPlayer != null){
        mediaPlayer.start();}
        Intent intent = new Intent("play_broadcast");
        intent.putExtra("position",position);
        intent.putExtra("state",playing);
        sendBroadcast(intent);
    }

}
