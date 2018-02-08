package com.example.lixiang.music;

import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import static android.R.attr.id;
import static com.example.lixiang.music.R.id.play_now_cover;
import static com.example.lixiang.music.getCover.getArtwork;

public class PlayNow extends AppCompatActivity {
    private  MediaPlayer mp ;
    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            releaseMediaPlayer();
        }
    };
    private boolean state = false;
    private SeekBar seekBar;
    private FloatingActionButton Floatingbar;
    private Handler mHandler = new Handler();
    private double starttime = 0;
    private double finaltime = 0;


    //menu
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }
    //menu clicked

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_now);
        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
        String title = intent.getStringExtra("title");
        String artist = intent.getStringExtra("artist");
        int id  = intent.getExtras().getInt("id");
        int album_id  = intent.getExtras().getInt("album_id");


        Log.d("path ","歌曲路径"+path);
        Log.d("title ","歌曲名称"+title);

        releaseMediaPlayer();// 释放资源
        mp = new MediaPlayer();


        try {

            mp.setDataSource(path);
           mp.prepare(); // 进行缓冲
        } catch (Exception e) {
            e.printStackTrace();
        }
        mp.start();
       mp.setOnCompletionListener(mCompletionListener);


        seekBar = (SeekBar) findViewById(R.id.seekBar);
        finaltime = mp.getDuration();
        starttime = mp.getCurrentPosition();
        seekBar.setMax((int) finaltime);
        seekBar.setProgress((int) starttime);
        mHandler.postDelayed(UpdateSongTime,100);

        //设置封面,自动封面获取颜色

        ImageView play_now_cover = (ImageView)findViewById(R.id.play_now_cover);

//        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
//        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
//        Glide
//                .with(this)
//                .load(uri)
//                .placeholder(R.drawable.isplaying)
//                .crossFade(100)
//                .into(play_now_cover);


        Bitmap cover = getArtwork(this,id,album_id,true);

        play_now_cover.setImageBitmap(cover);
        setBackColor(cover);

       //设置歌曲名，歌手
        TextView play_now_song = (TextView)findViewById(R.id.play_now_song);
        TextView play_now_singer =(TextView)findViewById(R.id.play_now_singer);
        play_now_song.setText(title);
        play_now_singer.setText(artist);


        //拖动seekbar同步歌曲
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mp != null && fromUser){
                    mp.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }
    //播放，暂停按钮
    public void play_or_pause(View view) {
        Floatingbar = (FloatingActionButton) findViewById(R.id.play);
        change_play_or_pause_state();
    }

    public void change_play_or_pause_state(){
        if (mp.isPlaying()) {
            mp.pause();
            Floatingbar.setImageResource(R.drawable.play_black);
        } else {
            mp.start();
            Floatingbar.setImageResource(R.drawable.pause_black);
        }
    }

    //同步seekbar与进度条时间
    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            starttime = mp.getCurrentPosition();
            seekBar.setProgress((int)starttime);
            mHandler.postDelayed(this, 100);
        }
    };
    //获取歌曲封面颜色并设置为背景
    private void setBackColor(Bitmap bitmap) {
        Palette p = Palette.from(bitmap).generate();
        Palette.Swatch s1 = p.getVibrantSwatch();
        Palette.Swatch s2 = p.getDarkVibrantSwatch();
        Palette.Swatch s3 = p.getLightVibrantSwatch();
        Palette.Swatch s4 = p.getMutedSwatch();
        Palette.Swatch s5 = p.getLightVibrantSwatch();
        Palette.Swatch s6 = p.getDarkVibrantSwatch();
        Palette.Swatch s7 = p.getDominantSwatch();
        LinearLayout activity_now_play = (LinearLayout) findViewById(R.id.activity_now_play);
        TextView now_on_play_text = (TextView) findViewById(R.id.now_on_play_text);
        if(s1 != null) {
            //get rgb
            activity_now_play.setBackgroundColor(s1.getRgb());
            now_on_play_text.setTextColor(s1.getRgb());
        }
        else if(s4 != null) {
            //get rgb
            activity_now_play.setBackgroundColor(s4.getRgb());
            now_on_play_text.setTextColor(s4.getRgb());
        }
        else {
            //default color

        }

    }



    @Override
    protected void onStop() {
        super.onStop();
        releaseMediaPlayer();
    }


    private void releaseMediaPlayer() {
        // If the media player is not null, then it may be currently playing a sound.
        if (mp != null) {
            // 不能用release不知为何...
            mp.reset();
//            mp.release();


        }
    }




}
