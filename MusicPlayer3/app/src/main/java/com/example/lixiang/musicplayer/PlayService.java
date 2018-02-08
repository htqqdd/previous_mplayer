package com.example.lixiang.musicplayer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import java.util.Random;
import static com.example.lixiang.musicplayer.Data.initialize;
import static com.example.lixiang.musicplayer.Data.mediaChangeAction;
import static com.example.lixiang.musicplayer.Data.nextAction;
import static com.example.lixiang.musicplayer.Data.pauseAction;
import static com.example.lixiang.musicplayer.Data.pausing;
import static com.example.lixiang.musicplayer.Data.playAction;
import static com.example.lixiang.musicplayer.Data.playing;
import static com.example.lixiang.musicplayer.Data.previousAction;
import static com.example.lixiang.musicplayer.Data.seektoAction;
import static com.example.lixiang.musicplayer.getCover.getArtwork;

public class PlayService extends Service{

    public static final String ACTION_PLAY = "com.example.lixiang.musicplayer.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.lixiang.musicplayer.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.example.lixiang.musicplayer.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.example.lixiang.musicplayer.ACTION_NEXT";
    public static final String ACTION_STOP = "com.example.lixiang.musicplayer.ACTION_STOP";
    private String path;
    private MediaPlayer mediaPlayer;
    private int position = 0;

    private ServiceReceiver serviceReceiver;
    private boolean onetime = true;
    private static Handler handler;

    //MediaSession
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;


    //AudioPlayer notification ID
    private static final int NOTIFICATION_ID = 101;


    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, PlayService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, 0, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, 0, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, 0, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, 0, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    public PlayService() {}

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
        if (intent.getIntExtra("ACTION", -2) == playAction) {
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
        if (mediaSessionManager == null) {
            try {
                initMediaSession();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
            buildNotification(pausing);
        }

        handleIncomingActions(intent);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
//        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
//        mSession.release();
        return super.onUnbind(intent);
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
        intent.putExtra("UIChange",mediaChangeAction);
        sendBroadcast(intent);
        Data.setState(playing);

//        //更新notification
        updateMetaData();
        buildNotification(playing);
        Data.setPosition(position);

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
            if (intent.getIntExtra("Control", 0) == previousAction) {
                previous();
            }
            if (intent.getIntExtra("Control", 0) == nextAction) {
                next();
            }
            if (intent.getIntExtra("Control", 0) == seektoAction) {
                mediaPlayer.seekTo(Data.get_mediaCurrentPosition());
            }
            if (intent.getIntExtra("Control", 0) == pauseAction) {
                pause();
            }
            if (intent.getIntExtra("Control", 0) == playAction) {
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
        intent.putExtra("UIChange",pauseAction);
        sendBroadcast(intent);
        Data.setState(pausing);
        buildNotification(pausing);
    }

    public void resume() {
//        if (onetime && Data.getPosition() == 0) {
            play(Data.getPosition());
//            onetime = false;
//        } else {
//            if (mediaPlayer != null ) {
//                mediaPlayer.start();
//            }
            Intent intent = new Intent("play_broadcast");
            intent.putExtra("UIChange",playAction);
            Data.setState(playing);
            sendBroadcast(intent);
            buildNotification(playing);
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        //初始化
//        Intent intent = new Intent("play_broadcast");
//        Data.setState(pausing);
//        sendBroadcast(intent);
//        Data.set_mediaDuration(0);
//        Data.set_mediaCurrentPosition(0);
//        Data.setPlayMode(3);
//        Data.setPosition(0);
        releaseMedia();
        removeNotification();
        unregisterReceiver(serviceReceiver);
    }

//初始化MediaSession
    private void initMediaSession() throws RemoteException {

        if (mediaSessionManager != null) return; //mediaSessionManager exists
        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        transportControls = mediaSession.getController().getTransportControls();
        mediaSession.setActive(true);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        updateMetaData();

        // Attach Callback to receive MediaSession updates
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                resume();
                super.onPlay();
            }

            @Override
            public void onPause() {
                pause();
                super.onPause();
            }

            @Override
            public void onSkipToNext() {
                next();
                super.onSkipToNext();
            }

            @Override
            public void onSkipToPrevious() {
                previous();
                super.onSkipToPrevious();
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                //Stop the service
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
    }

    private void updateMetaData() {
        Bitmap albumArt = getArtwork(this, Data.getId(Data.getPosition()), Data.getAlbumId(Data.getPosition()), true);
        // Update the current metadata
        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, Data.getArtist(Data.getPosition()))
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, Data.getAlbum(Data.getPosition()))
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, Data.getTitle(Data.getPosition()))
                .build());
    }

    //创建通知
    private void buildNotification(int playState) {

        int notificationAction = R.drawable.ic_pause_black_24dp;//needs to be initialized
        PendingIntent play_pauseAction = null;

        //Build a new notification according to the current state of the MediaPlayer
        if (playState == playing) {
            notificationAction = R.drawable.ic_pause_black_24dp;
            //create the pause action
            play_pauseAction = playbackAction(1);
        } else if (playState == pausing) {
            notificationAction = R.drawable.ic_play_arrow_black_24dp;
            //create the play action
            play_pauseAction = playbackAction(0);
        }

        Bitmap largeIcon = getArtwork(this, Data.getId(Data.getPosition()), Data.getAlbumId(Data.getPosition()), true);

        // Create a new Notification
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setShowWhen(false)
                // Set the Notification style
                .setStyle(new NotificationCompat.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(mediaSession.getSessionToken())
                        // Show our playback controls in the compact notification view.
                        .setShowActionsInCompactView(0, 1, 2))
                // Set the Notification color
                .setColor(getResources().getColor(R.color.colorPrimary))
                // Set the large and small icons
                .setLargeIcon(largeIcon)
                .setSmallIcon(R.drawable.ic_album_black_24dp)
                // Set Notification content information

                .setContentText(Data.getArtist(Data.getPosition()))
                .setContentTitle(Data.getTitle(Data.getPosition()))
                .setContentInfo(Data.getAlbum(Data.getPosition()))
                // Add playback actions

                .addAction(R.drawable.ic_fast_rewind_black_24dp,"previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(R.drawable.ic_fast_forward_black_24dp, "next", playbackAction(2));

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;
        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }
}
