package com.example.lixiang.musicplayer;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Random;

import static com.example.lixiang.musicplayer.Data.initialize;
import static com.example.lixiang.musicplayer.Data.next;
import static com.example.lixiang.musicplayer.Data.pausing;
import static com.example.lixiang.musicplayer.Data.play;
import static com.example.lixiang.musicplayer.Data.playing;
import static com.example.lixiang.musicplayer.Data.previous;
import static com.example.lixiang.musicplayer.Data.resuming;
import static com.example.lixiang.musicplayer.Data.seekto;

public class PlayService extends Service {
    private String path;
    private MediaPlayer mediaPlayer;
    private int position = 0;

    private ServiceReceiver serviceReceiver;
    private boolean onetime = true;
    private static Handler handler;

    public PlayService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();

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
        if (intent.getIntExtra("ACTION", -2) == initialize) {
            Data.setPosition(0);
        }
        if (intent.getIntExtra("ACTION", -2) == play) {
            if (Data.getPlayMode() != 1) {
                play(Data.getPosition());
            } else {
                Data.setPosition(randomPosition());
                play(Data.getPosition());
            }
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

    public void releaseMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void play(int position) {
        mediaPlayer.reset();
        path = Data.getData(position);
        Intent intent = new Intent("play_broadcast");
        Data.setState(playing);
        sendBroadcast(intent);
//        Data.setPosition(position);

        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare(); // 进行缓冲
        } catch (Exception e) {
            e.printStackTrace();
        }
        Data.set_mediaDuration(mediaPlayer.getDuration());
        mediaPlayer.start();
        //储存播放次数
        int Playtimes = Data.findPlayTimesById(Data.getId(position));
        Playtimes++;
        SharedPreferences.Editor editor = getSharedPreferences("playtimes", MODE_PRIVATE).edit();
        editor.putInt(String.valueOf(Data.getId(position)), Playtimes);
        editor.apply();
        Log.v("Playtimes", "播放次数" + Data.findPlayTimesById(Data.getId(position)));

        //发送更新seekbar广播
        handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    Data.set_mediaCurrentPosition(mediaPlayer.getCurrentPosition());
                    handler.postDelayed(this, 500);
                }
            }
        };
        handler.postDelayed(runnable, 500);

    }

    //生成随机数[0-n)
    public int randomPosition() {
        Random random = new Random();
        int random_position = random.nextInt(Data.getCursor().getCount() - 1);
        return random_position;
    }

    private class ServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra("Control", 0) == previous) {
                previous();
            }
            if (intent.getIntExtra("Control", 0) == next) {
                next();
            }
            if (intent.getIntExtra("Control", 0) == seekto) {
                mediaPlayer.seekTo(Data.get_mediaCurrentPosition());
            }
            if (Data.getState() == pausing) {
                pause();
            }
            if (Data.getState() == resuming) {
                resume();
            }
        }
    }

    public void previous() {
        if (Data.getPlayMode() == 0) {//0:列表重复
            if (Data.IsRecent()){

                if (Data.getRecent_position() <= 0 ) {
                    Data.setPosition(Data.getDateSublist().get(Data.getDateSublist().size() -1 ).getPosition());
                    Data.setRecent_position(Data.getDateSublist().size() -1);
                    play(Data.getPosition());
                } else {
                    Data.setPosition(Data.getDateSublist().get(Data.getRecent_position() - 1).getPosition());
                    Data.setRecent_position(Data.getRecent_position() - 1);
                    play(Data.getPosition());
                }

            } else if (Data.IsFavourite()){

                if (Data.getFavourite_position() == 0) {
                    Data.setPosition(Data.findPositionById(Data.getTimessublist().get(Data.getTimessublist().size() -1).getId()));
                    Data.setFavourite_position(Data.getTimessublist().size() -1);
                    play(Data.getPosition());
                } else {
                    Data.setPosition(Data.findPositionById(Data.getTimessublist().get(Data.getFavourite_position() - 1).getId()));
                    Data.setFavourite_position(Data.getFavourite_position() - 1);
                    play(Data.getPosition());
                }

            }else {

                if (Data.getPosition() == 0) {
                    Data.setPosition(Data.getCursor().getCount() - 1);
                    play(Data.getPosition());
                } else {
                    Data.setPosition(Data.getPosition() - 1);
                    play(Data.getPosition());
                }
            }

        } else if (Data.getPlayMode() == 1) {// 1:随机

            Data.setPosition(randomPosition());
            play(Data.getPosition());

        } else if (Data.getPlayMode() == 2) {//2:单曲重复

            mediaPlayer.seekTo(0);
            mediaPlayer.start();

        } else {//3:顺序
            if (Data.IsRecent()){

                if (Data.getRecent_position() == 0 ) {
                    Toast.makeText(PlayService.this, "没有上一曲了", Toast.LENGTH_SHORT).show();
                } else {
                    Data.setPosition(Data.getDateSublist().get(Data.getRecent_position()-1).getPosition());
                    Data.setRecent_position(Data.getRecent_position()-1);
                    play(Data.getPosition());
                }

            }else if (Data.IsFavourite()){

                if (Data.getFavourite_position() == 0) {
                    Toast.makeText(PlayService.this, "没有上一曲了", Toast.LENGTH_SHORT).show();
                } else {
                    Data.setPosition(Data.findPositionById(Data.getTimessublist().get(Data.getFavourite_position() -1).getId()));
                    Data.setFavourite_position(Data.getFavourite_position() -1);
                    play(Data.getPosition());
                }

            }else {

                if (Data.getPosition() == 0) {
                    Toast.makeText(PlayService.this, "没有上一曲了", Toast.LENGTH_SHORT).show();
                } else {
                    Data.setPosition(Data.getPosition() - 1);
                    play(Data.getPosition());
                }

            }
        }
    }

    public void next() {

        if (Data.getNextMusic() == -1) {//判断是否有用户设置下一曲

            if (Data.getPlayMode() == 0) {//0:列表重复
                if (Data.IsRecent()) {
                    if (Data.getRecent_position() < Data.getDateSublist().size() -1 ) {
                        Data.setPosition(Data.getDateSublist().get(Data.getRecent_position()+1).getPosition());
                        Data.setRecent_position(Data.getRecent_position()+1);
                        play(Data.getPosition());
                    } else {
                        Data.setPosition(Data.getDateSublist().get(0).getPosition());
                        Data.setRecent_position(0);
                        play(Data.getPosition());
                    }
                } else if (Data.IsFavourite()) {

                    if (Data.getFavourite_position() < Data.getTimessublist().size() -1) {
                        Data.setPosition(Data.findPositionById(Data.getTimessublist().get(Data.getFavourite_position()+1).getId()));
                        Data.setFavourite_position(Data.getFavourite_position()+1);
                        play(Data.getPosition());
                    } else {
                        Data.setPosition(Data.findPositionById(Data.getTimessublist().get(0).getId()));
                        Data.setFavourite_position(0);
                        play(Data.getPosition());
                    }

                } else {

                    if (Data.getPosition() < Data.getCursor().getCount() - 1) {
                        Data.setPosition(Data.getPosition() + 1);
                        play(Data.getPosition());
                    } else {
                        Data.setPosition(0);
                        play(Data.getPosition());
                    }
                }

            } else if (Data.getPlayMode() == 1) {//1:随机
                Data.setPosition(randomPosition());
                play(Data.getPosition());
            } else if (Data.getPlayMode() == 2) {//2:单曲重复
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
            } else if (Data.getPlayMode() == 3) {//3:顺序
                if (Data.IsRecent()){

                    if (Data.getRecent_position() >= Data.getDateSublist().size() -1 ) {
                        Toast.makeText(PlayService.this, "没有下一曲了", Toast.LENGTH_SHORT).show();
                    } else {
                        Data.setPosition(Data.getDateSublist().get(Data.getRecent_position()+1).getPosition());
                        Data.setRecent_position(Data.getRecent_position()+1);
                        play(Data.getPosition());
                    }

                }
                else if (Data.IsFavourite()){

                    if (Data.getFavourite_position() >= Data.getTimessublist().size() -1) {
                        Toast.makeText(PlayService.this, "没有下一曲了", Toast.LENGTH_SHORT).show();
                    } else {
                        Data.setPosition(Data.findPositionById(Data.getTimessublist().get(Data.getFavourite_position()+1).getId()));
                        Data.setFavourite_position(Data.getFavourite_position()+1);
                        play(Data.getPosition());
                    }

                }
                else{

                    if (Data.getPosition() >= Data.getCursor().getCount() - 1) {
                        Log.v("position", "此时位置" + position);
                        Toast.makeText(PlayService.this, "没有下一曲了", Toast.LENGTH_SHORT).show();
                    } else {
                        Data.setPosition(Data.getPosition() + 1);
                        play(Data.getPosition());
                    }
                }

            }

        } else {
            Data.setPosition(Data.getNextMusic());
            play(Data.getNextMusic());
            Data.setNextMusic(-1);
        }
    }

    public void pause() {
        mediaPlayer.pause();
        Intent intent = new Intent("play_broadcast");
        Data.setState(pausing);
        sendBroadcast(intent);
    }

    public void resume() {
        if (onetime && Data.getPosition() == 0) {
            play(Data.getPosition());
            onetime = false;
        } else {
            if (mediaPlayer != null) {
                mediaPlayer.start();
            }
            Intent intent = new Intent("play_broadcast");
            Data.setState(playing);
            sendBroadcast(intent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //初始化
        Intent intent = new Intent("play_broadcast");
        Data.setState(pausing);
        sendBroadcast(intent);
        Data.set_mediaDuration(0);
        Data.set_mediaCurrentPosition(0);
        Data.setPlayMode(3);
        Data.setPosition(0);
        releaseMedia();
        unregisterReceiver(serviceReceiver);
    }

}
