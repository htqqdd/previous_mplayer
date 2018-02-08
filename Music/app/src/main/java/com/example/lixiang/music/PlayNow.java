package com.example.lixiang.music;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.DrmInitData;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import static android.R.attr.animation;
import static android.R.attr.id;
import static android.R.attr.path;
import static com.example.lixiang.music.MainActivity.pausing;
import static com.example.lixiang.music.MainActivity.play;
import static com.example.lixiang.music.MainActivity.playing;
import static com.example.lixiang.music.R.id.activity_now_play;
import static com.example.lixiang.music.R.id.control_layout;
import static com.example.lixiang.music.R.id.now_on_play_text;
import static com.example.lixiang.music.R.id.play_now_back_color;
import static com.example.lixiang.music.R.id.play_now_cover;
import static com.example.lixiang.music.R.id.repeat_button;
import static com.example.lixiang.music.getCover.getArtwork;

public class PlayNow extends AppCompatActivity {
    //    private  MediaPlayer mp ;
//    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
//        @Override
//        public void onCompletion(MediaPlayer mp) {
//            releaseMediaPlayer();
//        }
//    };
    private SeekBar seekBar;
    private FloatingActionButton Floatingbar;
    private Handler mHandler = new Handler();
    private double starttime = 0;
    private double finaltime = 0;
    private int[] _ids;// 保存音乐ID临时数组
    private int[] _albumids;
    private String[] _artists;// 保存艺术家
    private String[] _titles;
    private String[] _data;// 标题临时数组
    private Cursor cursor;
    private String[] media_music_info;
    private MediaPlayer mp = new MediaPlayer();
    private String path;
    private String title;
    private int album_id;
    private String artist;
    private int id;
    private int position;
    private MsgReceiver msgReceiver;
    private int state;
    private ImageView shuffle_button;
    private ImageView repeat_button;


    //menu
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }
    //menu clicked

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
////        Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();
//        return true;
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_now);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//此FLAG可使状态栏透明，且当前视图在绘制时，从屏幕顶端开始即top = 0开始绘制，这也是实现沉浸效果的基础
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);//可不加
        }

        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0);
        state = intent.getIntExtra("state", -2);
        initialMusicInfo();
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        ChangeActivity(position);

        //动态注册广播
        msgReceiver = new PlayNow.MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("play_broadcast");
        registerReceiver(msgReceiver, intentFilter);


//        handler = new MyHandler();

//
//
//        //拖动seekbar同步歌曲
//        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if (mp != null && fromUser){
//                    mp.seekTo(progress);
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });
//
    }

    //播放，暂停按钮
    public void play_or_pause(View view) {
        Floatingbar = (FloatingActionButton) findViewById(R.id.play_or_pause);
        change_play_or_pause_state();
    }

    public void change_play_or_pause_state() {
        if (state == playing) {
            //发送暂停广播
            Intent intent = new Intent("service_broadcast");
            intent.putExtra("pause", 2);
            sendBroadcast(intent);
            Floatingbar.setImageResource(R.drawable.play_black);
        } else if (state == pausing) {
            //发送恢复广播
            Intent intent = new Intent("service_broadcast");
            intent.putExtra("resume", 3);
            sendBroadcast(intent);
            Floatingbar.setImageResource(R.drawable.pause_black);
        }
    }

    //
//    //同步seekbar与进度条时间
//    private Runnable UpdateSongTime = new Runnable() {
//        public void run() {
//            starttime = mp.getCurrentPosition();
//            seekBar.setProgress((int)starttime);
//            mHandler.postDelayed(this, 100);
//        }
//    };
    //获取歌曲封面颜色并设置为背景
    private void setBackColor(Bitmap bitmap) {
        Palette p = Palette.from(bitmap).generate();
        final Palette.Swatch s1 = p.getVibrantSwatch();
        Palette.Swatch s2 = p.getDarkVibrantSwatch();
        Palette.Swatch s3 = p.getLightVibrantSwatch();
        final Palette.Swatch s4 = p.getMutedSwatch();
        Palette.Swatch s5 = p.getLightVibrantSwatch();
        Palette.Swatch s6 = p.getDarkVibrantSwatch();
        Palette.Swatch s7 = p.getDominantSwatch();
//        int cx = (control_layout.getLeft() + control_layout.getRight()) / 2;
//        int cy = (play_now_cover.getTop()) ;
//        int finalRadius = Math.max(play_now_back_color.getWidth(), play_now_back_color.getHeight());

        if (s1 != null) {
            animation_change_color(s1.getRgb());
        } else if (s4 != null) {
            animation_change_color(s4.getRgb());
        } else if (s2 != null){
            animation_change_color(s2.getRgb());
        }else if (s3 != null){
            animation_change_color(s3.getRgb());
        }else if (s5 != null){
            animation_change_color(s5.getRgb());
        }else if (s6 != null){
            animation_change_color(s6.getRgb());
        }else if (s7 != null){
            animation_change_color(s7.getRgb());
        }else {
            //default color
        }

    }

    public void initialMusicInfo() {
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
        Log.v("total", "总数" + cursor.getCount());
        for (int i = 0; i < cursor.getCount(); i++) {
            _ids[i] = cursor.getInt(3);
            _titles[i] = cursor.getString(0);
            _artists[i] = cursor.getString(2);
            _data[i] = cursor.getString(5);
            _albumids[i] = cursor.getInt(6);
            cursor.moveToNext();// 将游标移到下一行
        }

    }

    private class MsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //拿到position，更新UI
            position = intent.getIntExtra("position", 0);
            state = intent.getIntExtra("state", -2);
            ChangeActivity(position);
        }

    }

    private void ChangeActivity(int position) {
        path = _data[position];
        title = _titles[position];
        album_id = _albumids[position];
        artist = _artists[position];
        id = _ids[position];

//设置播放模式按钮
        repeat_button = (ImageView) findViewById(R.id.repeat_button);
        shuffle_button = (ImageView) findViewById(R.id.shuffle_button);

        if (Data.getPlayMode() == 0) {
            repeat_button.setImageResource(R.drawable.repeat);
            shuffle_button.setImageResource(R.drawable.shuffle_grey);
        }
        if (Data.getPlayMode() == 1) {
            shuffle_button.setImageResource(R.drawable.shuffle);
            repeat_button.setImageResource(R.drawable.repeat_grey);

        }
        if (Data.getPlayMode() == 2) {
            repeat_button.setImageResource(R.drawable.repeat_one);
            shuffle_button.setImageResource(R.drawable.shuffle_grey);

        }
        if (Data.getPlayMode() == 3) {
            repeat_button.setImageResource(R.drawable.repeat_grey);
            shuffle_button.setImageResource(R.drawable.shuffle_grey);
        }

        seekBar.setMax(Data.get_mediaDuration());
        Log.v("mediaDuration", "隐喻长度" + Data.get_mediaDuration());
        seekBar.setPadding(0, 0, 0, 0);


        //设置封面,自动封面获取颜色

        ImageView play_now_cover = (ImageView) findViewById(R.id.play_now_cover);

        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        Glide
                .with(this)
                .load(uri)
                .placeholder(R.drawable.default_album)
                .crossFade(300)
                .into(play_now_cover);


        Bitmap cover = getArtwork(this, id, album_id, true);
//        play_now_cover.setImageBitmap(cover);
        setBackColor(cover);

        //设置歌曲名，歌手
        TextView play_now_song = (TextView) findViewById(R.id.play_now_song);
        TextView play_now_singer = (TextView) findViewById(R.id.play_now_singer);
        play_now_song.setText(title);
        play_now_singer.setText(artist);

        //设置播放按钮
        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.play_or_pause);
        if (state == playing) {
            floatingActionButton.setImageResource(R.drawable.pause_black);
        } else if (state == pausing) {
            floatingActionButton.setImageResource(R.drawable.play_black);
        }

    }

    public void previous(View v) {
        //发送上一首广播
        Intent intent = new Intent("service_broadcast");
        intent.putExtra("previous", -1);
        sendBroadcast(intent);
    }

    public void next(View v) {
        //发送下一首广播
        Intent intent = new Intent("service_broadcast");
        intent.putExtra("next", 1);
        sendBroadcast(intent);
    }

    public void changeRepeat(View v) {
        switch (Data.getPlayMode()) {
            case 3:
                repeat_button.setImageResource(R.drawable.repeat);
                shuffle_button.setImageResource(R.drawable.shuffle_grey);
                Data.setPlayMode(0);
                break;
            case 0:
                repeat_button.setImageResource(R.drawable.repeat_one);
                shuffle_button.setImageResource(R.drawable.shuffle_grey);
                Data.setPlayMode(2);
                break;
            case 2:
                repeat_button.setImageResource(R.drawable.repeat_grey);
                shuffle_button.setImageResource(R.drawable.shuffle_grey);
                Data.setPlayMode(3);
            case 1:
                shuffle_button.setImageResource(R.drawable.shuffle_grey);
                Data.setPlayMode(3);
                repeat_button.setImageResource(R.drawable.repeat_grey);
            default:
        }
    }

    public void changeShuffle(View v) {
        if (Data.getPlayMode() != 1) {
            shuffle_button.setImageResource(R.drawable.shuffle);
            Data.setPlayMode(1);
            repeat_button.setImageResource(R.drawable.repeat_grey);
        } else {
            shuffle_button.setImageResource(R.drawable.shuffle_grey);
            Data.setPlayMode(3);
            repeat_button.setImageResource(R.drawable.repeat_grey);
        }
    }

public void animation_change_color(int Int){
    final int Int1 = Int;
    ImageView play_now_back_color = (ImageView) findViewById(R.id.play_now_back_color) ;
    TextView now_on_play_text = (TextView) findViewById(R.id.now_on_play_text);
    RelativeLayout control_layout = (RelativeLayout) findViewById(R.id.control_layout);
    ImageView play_now_cover = (ImageView) findViewById(R.id.play_now_cover);
    final RelativeLayout activity_now_play = (RelativeLayout) findViewById(R.id.activity_now_play);
    int cx = (control_layout.getLeft() + control_layout.getRight()) / 2;
    int cy = (play_now_cover.getTop()) ;
    int finalRadius = Math.max(play_now_back_color.getWidth(), play_now_back_color.getHeight());
    if (cx != 0) {
        Animator anim = ViewAnimationUtils.createCircularReveal(play_now_back_color, cx, cy, 0, finalRadius);
        play_now_back_color.setBackgroundColor(Int);
        anim.setDuration( 500 );
        anim.start();
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                activity_now_play.setBackgroundColor(Int1);
            }
        });
    } else {
        play_now_back_color.setBackgroundColor(Int);
    }
    now_on_play_text.setTextColor(Int);
}
}
