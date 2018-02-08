package com.example.lixiang.musicplayer;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.support.v7.graphics.Palette;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.tapadoo.alerter.Alert;
import com.tapadoo.alerter.Alerter;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static android.R.attr.action;
import static android.R.attr.bitmap;
import static android.R.attr.duration;
import static android.os.Build.VERSION_CODES.M;
import static com.example.lixiang.musicplayer.Data.deleteAction;
import static com.example.lixiang.musicplayer.Data.initialize;
import static com.example.lixiang.musicplayer.Data.mediaChangeAction;
import static com.example.lixiang.musicplayer.Data.nextAction;
import static com.example.lixiang.musicplayer.Data.pauseAction;
import static com.example.lixiang.musicplayer.Data.pausing;
import static com.example.lixiang.musicplayer.Data.playAction;
import static com.example.lixiang.musicplayer.Data.playing;
import static com.example.lixiang.musicplayer.Data.previousAction;
import static com.example.lixiang.musicplayer.Data.sc_playAction;
import static com.example.lixiang.musicplayer.Data.seektoAction;
import static com.example.lixiang.musicplayer.Data.serviceStarted;
import static com.example.lixiang.musicplayer.R.drawable.next;
import static com.example.lixiang.musicplayer.getCover.getArtwork;

public class PlayService extends Service implements AudioManager.OnAudioFocusChangeListener {

    private String path;
    private MediaPlayer mediaPlayer;
    private int position = 0;
    private ServiceReceiver serviceReceiver;
    private boolean onetime = true;
    private static Handler handler;
    private static final int NOTIFICATION_ID = 101;
    private AudioManager audioManager;

    //Handle incoming phone calls
    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;
    private Notification notification;
    private Timer timer;
    private TimerTask task;


    private PendingIntent pendingIntent(int action) {
        Intent intent = new Intent("service_broadcast");
        intent.putExtra("Control", action);
        return PendingIntent.getBroadcast(this, action, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public PlayService() {
    }


    public class musicBinder extends Binder {
        public PlayService getService() {
            return PlayService.this;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();

        Data.setServiceStarted(true);

        Log.v("服务是否开启", "onCreate" + Data.getServiceState());

        //监听耳机状态广播
        IntentFilter head_set_intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, head_set_intentFilter);

        //动态注册广播
        serviceReceiver = new ServiceReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("service_broadcast");
        registerReceiver(serviceReceiver, intentFilter);
        //处理通话状态
        callStateListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Data.setServiceStarted(true);
        Log.v("服务是否开启", "startCommand" + Data.getServiceState());
        requestAudioFocus();

        releaseMedia();
        mediaPlayer = new MediaPlayer();

        if (intent.getIntExtra("ACTION", -2) == initialize) {
            Data.setPosition(0);
        }
        if (intent.getIntExtra("ACTION", -2) == sc_playAction) {
            Log.v("接收到playAction", "startCommand" + Data.getServiceState());
            if (Data.getInfoInitialized() != 3) {
                Log.v("重新载入数据", "重新载入数据");
                Data.initialMusicInfo(this);
                Data.initialMusicPlaytimes(this);
                Data.initialMusicDate(this);
            }
            if (Data.getPlayMode() != 1) {
                play(Data.getPosition());
                Log.v("发送play方法", "startCommand" + Data.getServiceState());
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
        return new musicBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onAudioFocusChange(int i) {
        switch (i) {
            case AudioManager.AUDIOFOCUS_GAIN:
                Log.v("AUDIOFOCUS_GAIN", "焦点获得");
                //The service gained audio focus, so it needs to start playing.
                if (mediaPlayer == null) mediaPlayer = new MediaPlayer();
                else if (!mediaPlayer.isPlaying()) resume();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                //The service lost audio focus, the user probably moved to playing media on another app, so release the media player.
                Log.v("AUDIOFOCUS_LOSS", "焦点丢失");
                pause();
                releaseMedia();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                Log.v("FOCUS_LOSS_TRANSIENT", "焦点暂时丢失");
                //Focus lost for a short time, pause the MediaPlayer.
                if (mediaPlayer.isPlaying()) pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, probably a notification arrived on the device, lower the playback volume.
                Log.v("AUDIOFOCUS_LOSS_CAN", "焦点更短暂丢失");
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                Boolean lost_focus = sharedPref.getBoolean("lost_focus", false);
                if (lost_focus) {
                    if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                }
                break;
        }
    }

    public void releaseMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void play(final int position) {
        Log.v("play方法执行", "play" + Data.getServiceState());
        requestAudioFocus();
        if (mediaPlayer == null) mediaPlayer = new MediaPlayer();
        mediaPlayer.reset();
        path = Data.getData(position);
        if (onetime) {
            Intent intent = new Intent("play_broadcast");
            intent.putExtra("UIChange", initialize);
            sendBroadcast(intent);
        }
        Intent intent = new Intent("play_broadcast");
        intent.putExtra("UIChange", mediaChangeAction);
        sendBroadcast(intent);
        Data.setState(playing);
        new buildNotificationTask().execute(playing);
        Data.setPosition(position);
        try {
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare(); // 进行缓冲
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Data.set_mediaDuration(mediaPlayer.getDuration());
        mediaPlayer.start();
        //储存播放次数
        new savePlayTimesTask().execute();

    }

    public void seekto(int currentPosition) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(currentPosition);
        }
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
            if (intent.getIntExtra("ACTION", -2) == playAction) {
                Log.v("接收到playAction", "startCommand" + Data.getServiceState());
                if (Data.getPlayMode() != 1) {
                    play(Data.getPosition());
                    Log.v("发送play方法", "startCommand" + Data.getServiceState());
                } else {
                    Data.setPosition(randomPosition());
                    play(Data.getPosition());
                }
            }
            if (intent.getIntExtra("Control", 0) == previousAction) {
                previous();
            }
            if (intent.getIntExtra("Control", 0) == nextAction) {
                next();
            }
            if (intent.getIntExtra("Control", 0) == pauseAction) {
                pause();
            }
            if (intent.getIntExtra("Control", 0) == playAction) {
                resume();
            }
            if (intent.getIntExtra("Control", 0) == deleteAction) {
                deleteService(intent.getIntExtra("DelayControl", 0));
            }
        }
    }

    public void deleteService(int time) {
        if (time != 0) {
            AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
            // 设置定时
            int duration = time * 60 * 1000;
            long setTime = duration + SystemClock.elapsedRealtime();
            //设置定时任务
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, setTime, pendingIntent(deleteAction));
        } else {
            pause();
            removeNotification();
        }
    }

    public void previous() {
        new previousTask().execute();
    }

    public void next() {
        new nextTask().execute();
    }

    public void pause() {
        mediaPlayer.pause();
        Intent intent = new Intent("play_broadcast");
        intent.putExtra("UIChange", pauseAction);
        sendBroadcast(intent);
        if (Data.getState() == playing) {
            new buildNotificationTask().execute(pausing);
        }
        Data.setState(pausing);
    }

    public void resume() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        } else if (mediaPlayer == null) {
            play(Data.getPosition());
        }
        Intent intent = new Intent("play_broadcast");
        intent.putExtra("UIChange", playAction);
        Data.setState(playing);
        sendBroadcast(intent);
        new buildNotificationTask().execute(playing);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //关闭MainActivity
//        Intent intent = new Intent("play_broadcast");
//        intent.putExtra("onDestroy", 1);
//        sendBroadcast(intent);
        Data.setServiceStarted(false);
        Log.v("服务是否开启", "onDestroy" + Data.getServiceState());
        releaseMedia();
        removeNotification();
        removeAudioFocus();
        unregisterReceiver(serviceReceiver);
        unregisterReceiver(becomingNoisyReceiver);
    }


    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }


    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }

    //Handle incoming phone calls
    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pause();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resume();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    //Becoming noisy
    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(PlayService.this);
            Boolean headset_unplug = sharedPref.getBoolean("headset_unplug", false);
            if (headset_unplug) {
                pause();
            }
        }
    };

    public class nextTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            //判断是否有用户设置下一曲
            if (Data.getNextMusic() == -1) {
//0:列表重复
                if (Data.getPlayMode() == 0) {
                    if (Data.IsRecent()) {
                        if (Data.getRecent_position() < Data.getDateSublist().size() - 1) {
                            Data.setPosition(Data.getDateSublist().get(Data.getRecent_position() + 1).getPosition());
                            Data.setRecent_position(Data.getRecent_position() + 1);
                            return Data.getPosition();
                        } else {
                            Data.setPosition(Data.getDateSublist().get(0).getPosition());
                            Data.setRecent_position(0);
                            return Data.getPosition();
                        }
                    } else if (Data.IsFavourite()) {

                        if (Data.getFavourite_position() < Data.getTimessublist().size() - 1) {
                            Data.setPosition(Data.findPositionById(Data.getTimessublist().get(Data.getFavourite_position() + 1).getId()));
                            Data.setFavourite_position(Data.getFavourite_position() + 1);
                            return Data.getPosition();
                        } else {
                            Data.setPosition(Data.findPositionById(Data.getTimessublist().get(0).getId()));
                            Data.setFavourite_position(0);
                            return Data.getPosition();
                        }

                    } else {

                        if (Data.getPosition() < Data.getCursor().getCount() - 1) {
                            Data.setPosition(Data.getPosition() + 1);
                            return Data.getPosition();
                        } else {
                            Data.setPosition(0);
                            return Data.getPosition();
                        }
                    }

                } else if (Data.getPlayMode() == 1) {//1:随机
                    Data.setPosition(randomPosition());
                    return Data.getPosition();
                } else if (Data.getPlayMode() == 2) {//2:单曲重复
                    return Data.getPosition();
                } else if (Data.getPlayMode() == 3) {//3:顺序
                    if (Data.IsRecent()) {

                        if (Data.getRecent_position() >= Data.getDateSublist().size() - 1) {
                            return null;
                        } else {
                            Data.setPosition(Data.getDateSublist().get(Data.getRecent_position() + 1).getPosition());
                            Data.setRecent_position(Data.getRecent_position() + 1);
                            return Data.getPosition();
                        }

                    } else if (Data.IsFavourite()) {

                        if (Data.getFavourite_position() >= Data.getTimessublist().size() - 1) {
                            return null;
                        } else {
                            Data.setPosition(Data.findPositionById(Data.getTimessublist().get(Data.getFavourite_position() + 1).getId()));
                            Data.setFavourite_position(Data.getFavourite_position() + 1);
                            return Data.getPosition();
                        }

                    } else {

                        if (Data.getPosition() >= Data.getCursor().getCount() - 1) {
                            return null;
                        } else {
                            Data.setPosition(Data.getPosition() + 1);
                            return Data.getPosition();
                        }
                    }

                }

            } else {
                Data.setPosition(Data.getNextMusic());
                Data.setNextMusic(-1);
                return Data.getNextMusic();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (o != null) {
                play((int) o);
            } else {
                Toast.makeText(PlayService.this, "没有下一曲了", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class previousTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            if (Data.getPlayMode() == 0) {//0:列表重复
                if (Data.IsRecent()) {

                    if (Data.getRecent_position() <= 0) {
                        Data.setPosition(Data.getDateSublist().get(Data.getDateSublist().size() - 1).getPosition());
                        Data.setRecent_position(Data.getDateSublist().size() - 1);
                        return Data.getPosition();
                    } else {
                        Data.setPosition(Data.getDateSublist().get(Data.getRecent_position() - 1).getPosition());
                        Data.setRecent_position(Data.getRecent_position() - 1);
                        return Data.getPosition();
                    }

                } else if (Data.IsFavourite()) {

                    if (Data.getFavourite_position() == 0) {
                        Data.setPosition(Data.findPositionById(Data.getTimessublist().get(Data.getTimessublist().size() - 1).getId()));
                        Data.setFavourite_position(Data.getTimessublist().size() - 1);
                        return Data.getPosition();
                    } else {
                        Data.setPosition(Data.findPositionById(Data.getTimessublist().get(Data.getFavourite_position() - 1).getId()));
                        Data.setFavourite_position(Data.getFavourite_position() - 1);
                        return Data.getPosition();
                    }

                } else {

                    if (Data.getPosition() == 0) {
                        Data.setPosition(Data.getCursor().getCount() - 1);
                        return Data.getPosition();
                    } else {
                        Data.setPosition(Data.getPosition() - 1);
                        return Data.getPosition();
                    }
                }

            } else if (Data.getPlayMode() == 1) {// 1:随机
                Data.setPosition(randomPosition());
                return Data.getPosition();
            } else if (Data.getPlayMode() == 2) {//2:单曲重复
                return Data.getPosition();

            } else {//3:顺序
                if (Data.IsRecent()) {

                    if (Data.getRecent_position() == 0) {
                        return null;
                    } else {
                        Data.setPosition(Data.getDateSublist().get(Data.getRecent_position() - 1).getPosition());
                        Data.setRecent_position(Data.getRecent_position() - 1);
                        return Data.getPosition();
                    }

                } else if (Data.IsFavourite()) {

                    if (Data.getFavourite_position() == 0) {
                        return null;
                    } else {
                        Data.setPosition(Data.findPositionById(Data.getTimessublist().get(Data.getFavourite_position() - 1).getId()));
                        Data.setFavourite_position(Data.getFavourite_position() - 1);
                        return Data.getPosition();
                    }

                } else {

                    if (Data.getPosition() == 0) {
                        return null;
                    } else {
                        Data.setPosition(Data.getPosition() - 1);
                        return Data.getPosition();
                    }

                }
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if (o != null) {
                play((int) o);
            } else {
                Toast.makeText(PlayService.this, "没有上一曲了", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class buildNotificationTask extends AsyncTask<String, Integer, state_image_color> {
        @Override
        protected state_image_color doInBackground(String... strings) {
            String state = strings[0];
            Bitmap largeIcon = getArtwork(PlayService.this, Data.getId(Data.getPosition()), Data.getAlbumId(Data.getPosition()), true);
            Palette p = Palette.from(largeIcon).generate();
            Palette.Swatch s1 = p.getVibrantSwatch();
            Palette.Swatch s2 = p.getDarkVibrantSwatch();
            Palette.Swatch s3 = p.getLightVibrantSwatch();
            Palette.Swatch s4 = p.getMutedSwatch();
            Palette.Swatch s5 = p.getLightVibrantSwatch();
            Palette.Swatch s6 = p.getDarkVibrantSwatch();
            Palette.Swatch s7 = p.getDominantSwatch();
            int color = 0;
            if (s1 != null) {
                color = s1.getRgb();
            } else if (s4 != null) {
                color = s4.getRgb();
            } else if (s2 != null) {
                color = s2.getRgb();
            } else if (s3 != null) {
                color = s3.getRgb();
            } else if (s5 != null) {
                color = s5.getRgb();
            } else if (s6 != null) {
                color = s6.getRgb();
            } else if (s7 != null) {
                color = s7.getRgb();
            } else {
                color = getResources().getColor(R.color.colorPrimary);
            }
            return new state_image_color(state, color, largeIcon);
        }

        @Override
        protected void onPostExecute(state_image_color state_image_color) {
            super.onPostExecute(state_image_color);
            String playState = state_image_color.getState();
            Bitmap largeIcon = state_image_color.getImage();
            int color = state_image_color.getColor();
            int notificationAction = R.drawable.ic_pause_black_24dp;//needs to be initialized

            PendingIntent play_pauseIntent = null;

            //Build a new notification according to the current state of the MediaPlayer
            if (playState == playing) {
                notificationAction = R.drawable.ic_pause_black_24dp;
                //create the pause action
                play_pauseIntent = pendingIntent(pauseAction);
            } else if (playState == pausing) {
                notificationAction = R.drawable.ic_play_arrow_black_24dp;
                //create the play action
                play_pauseIntent = pendingIntent(playAction);
            }

            Intent startMain = new Intent(PlayService.this, MainActivity.class);
            PendingIntent startMainActivity = PendingIntent.getActivity(PlayService.this, 0, startMain, PendingIntent.FLAG_UPDATE_CURRENT);

            notification = new Notification.Builder(PlayService.this)
                    // Show controls on lock screen even when user hides sensitive content.
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setLargeIcon(largeIcon)
                    .setSmallIcon(R.drawable.ic_album_black_24dp)
//                    .setDeleteIntent(pendingIntent(deleteAction))
                    .setColor(color)
                    .setContentIntent(startMainActivity)
                    .setOngoing(Data.getState() == playing)//该选项会导致手表通知不显示
                    // Add media control buttons that invoke intents in your media service
                    .addAction(R.drawable.ic_fast_rewind_black_24dp, "SkiptoPrevious", pendingIntent(previousAction)) // #0
                    .addAction(notificationAction, "Play or Pause", play_pauseIntent)  // #1
                    .addAction(R.drawable.ic_fast_forward_black_24dp, "SkiptoNext", pendingIntent(nextAction))     // #2
                    // Apply the media style template
                    .setStyle(new Notification.MediaStyle()
                                    .setShowActionsInCompactView(0, 1, 2)
//                        .setMediaSession(new MediaSession(this,"player").getSessionToken())
                    )
                    .setContentTitle(Data.getTitle(Data.getPosition()))
                    .setContentText(Data.getArtist(Data.getPosition()))
                    .build();

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(NOTIFICATION_ID, notification);

        }

    }

    public class state_image_color {
        public Bitmap mImage;
        public int mColor;
        public String mState;

        public state_image_color(String state, int color, Bitmap image) {
            mImage = image;
            mColor = color;
            mState = state;
        }

        public Bitmap getImage() {
            return mImage;
        }

        public int getColor() {
            return mColor;
        }

        public String getState() {
            return mState;
        }
    }

    public class savePlayTimesTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            int Playtimes = Data.findPlayTimesById(Data.getId(Data.getPosition()));
            Playtimes++;
            SharedPreferences.Editor editor = getSharedPreferences("playtimes", MODE_PRIVATE).edit();
            editor.putInt(String.valueOf(Data.getId(Data.getPosition())), Playtimes);
            editor.apply();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            Log.v("Playtimes", "播放次数" + Data.findPlayTimesById(Data.getId(Data.getPosition())));
        }
    }

}
