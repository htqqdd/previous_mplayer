package com.example.lixiang.musicplayer2;

import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;




public class NowPlay extends AppCompatActivity {
    private MediaPlayer mp = new MediaPlayer();
    private boolean state = false;
    private SeekBar  seekBar;
    private FloatingActionButton Floatingbar;
    private Handler mHandler = new Handler();
    private double starttime = 0;
    private double finaltime = 0;
    public static int onetime = 0;


    //menu
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }
    //menu clicked

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {//得到被点击的item的itemId
            case R.id.Setting://对应的ID就是在add方法中所设定的Id
                Toast.makeText(this, "设置", Toast.LENGTH_LONG).show();
                break;
            default:
                finish();
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_play);

        //添加歌曲
        mp = MediaPlayer.create(this, R.raw.qingmang);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        finaltime = mp.getDuration();
        starttime = mp.getCurrentPosition();
        if (onetime == 0){
            seekBar.setMax((int) finaltime);
            onetime = 1;
        }
        seekBar.setProgress((int) starttime);
        mHandler.postDelayed(UpdateSongTime,100);

        //自动封面获取颜色
        setBackColor(R.drawable.cover2);





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
        if (mp.isPlaying()) {
            mp.pause();
            Floatingbar.setImageResource(R.drawable.play_black);
        } else {
            mp.start();
            Floatingbar.setImageResource(R.drawable.pause_balck);
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
    private void setBackColor(int imageId) {
        Palette p = Palette.from(BitmapFactory.decodeResource(this.getResources(), imageId)).generate();
        Palette.Swatch s1 = p.getDarkVibrantSwatch();
        LinearLayout rootview = (LinearLayout) findViewById(R.id.activity_now_play);
        TextView now_on_play_text = (TextView) findViewById(R.id.now_on_play_text);
        rootview.setBackgroundColor(s1.getRgb());
        now_on_play_text.setTextColor(s1.getRgb());
    }



    @Override
    protected void onStop() {
        super.onStop();
        finish();

    }


}