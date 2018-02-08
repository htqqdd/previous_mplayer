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
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import static com.example.lixiang.musicplayer.Data.initialize;
import static com.example.lixiang.musicplayer.Data.next;
import static com.example.lixiang.musicplayer.Data.pausing;
import static com.example.lixiang.musicplayer.Data.play;
import static com.example.lixiang.musicplayer.Data.playing;
import static com.example.lixiang.musicplayer.Data.previous;
import static com.example.lixiang.musicplayer.Data.resuming;
import static com.example.lixiang.musicplayer.Data.seekto;
import static com.example.lixiang.musicplayer.R.id.main_song_title;
import static com.example.lixiang.musicplayer.R.id.seekBar;
import static com.example.lixiang.musicplayer.getCover.getArtwork;
import static com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState.COLLAPSED;
import static com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState.EXPANDED;

public class MainActivity extends AppCompatActivity {
    private TextView main_song_title;
    private SeekBar seekBar;
    private FloatingActionButton Floatingbar;
    private String path;
    private String title;
    private int album_id;
    private String artist;
    private int id;

    private MsgReceiver msgReceiver;

    private ImageView shuffle_button;
    private ImageView repeat_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        SimpleFragmentPagerAdapter adapter = new SimpleFragmentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        //新标题栏
        Toolbar main_toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        main_toolbar.setTitleTextColor(getResources().getColor(R.color.colorCustomAccent));
        setSupportActionBar(main_toolbar);

        //此FLAG可使状态栏透明，且当前视图在绘制时，从屏幕顶端开始即top = 0开始绘制，这也是实现沉浸效果的基础
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);//可不加
        }

        //申请动态权限
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }else {
            Data.initialMusicInfo(this);
            display();
            sendPermissionGranted();
        }

        //开服务
        Intent intent = new Intent();
        intent.putExtra("ACTION",initialize);
        Data.setPosition(0);
        intent.setClass(this, PlayService.class);
        startService(intent);

        //动态注册广播
        msgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("play_broadcast");
        registerReceiver(msgReceiver, intentFilter);

        //上滑面板
        SlidingUpPanelLayout mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                CardView main_control_ui = (CardView) findViewById(R.id.main_control_ui);
                AlphaAnimation alphaAnimation = new AlphaAnimation(slideOffset, 1-slideOffset);
                main_control_ui.startAnimation(alphaAnimation);
                alphaAnimation.setFillAfter(true);//动画结束后保持状态
                alphaAnimation.setDuration(0);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

            }
        });

        //更新seekbar

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(Data.get_mediaCurrentPosition());
                handler.postDelayed(this, 500);
            }
        };
        handler.postDelayed(runnable, 500);
        //拖动seekbar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Intent intent = new Intent("service_broadcast");
                    intent.putExtra("Control", seekto);
                    Data.set_mediaCurrentPosition(progress);
                    sendBroadcast(intent);
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
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
            Toast.makeText(this, "您拒绝了该权限，程序将无法使用哦", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Data.initialMusicInfo(this);
            display();
            sendPermissionGranted();
        }
    }


    //以下为公共方法

    //播放，暂停按钮
    public void play_or_pause(View view) {
        Floatingbar = (FloatingActionButton) findViewById(R.id.play_or_pause);
        change_play_or_pause_state();
    }

    public void change_play_or_pause_state() {
        if (Data.getState() == playing) {
            //发送暂停广播
            Intent intent = new Intent("service_broadcast");
            Data.setState(pausing);
            sendBroadcast(intent);
            Floatingbar.setImageResource(R.drawable.play_black);
        } else if (Data.getState() == pausing) {
            //发送恢复广播
            Intent intent = new Intent("service_broadcast");
            Data.setState(resuming);
            sendBroadcast(intent);
            Floatingbar.setImageResource(R.drawable.pause_black);
        }
    }

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

    @Override
    public void onBackPressed() {
        SlidingUpPanelLayout mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        if (mLayout != null &&
                (mLayout.getPanelState() == EXPANDED || mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)){
            mLayout.setPanelState(COLLAPSED);
        }else{
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this,PlayService.class));
        unregisterReceiver(msgReceiver);
    }

    public void display() {

//        showMusicList();
        //初始化ScrollingUpPanel
        ChangeScrollingUpPanel(0);
        main_song_title = (TextView) findViewById(R.id.main_song_title);
        main_song_title.setText(Data.getTitle(Data.getPosition()));

    }
    public void sendPermissionGranted(){
        Intent intent = new Intent("permission_granted");
        sendBroadcast(intent);
    }



    public void previous(View v) {
        //发送上一首广播
        Intent intent = new Intent("service_broadcast");
        intent.putExtra("Control", previous);
        sendBroadcast(intent);
    }

    public void next(View v) {
        //发送下一首广播
        Intent intent = new Intent("service_broadcast");
        intent.putExtra("Control", next);
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
            activity_now_play.setBackgroundColor(Int);
        }
        now_on_play_text.setTextColor(Int);
    }

    public void ChangeScrollingUpPanel(int position) {
        path = Data.getData(position);
        title = Data.getTitle(position);
        album_id = Data.getAlbumId(position);
        artist = Data.getArtist(position);
        id = Data.getId(position);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setPadding(0,0,0,0);
        seekBar.setMax(Data.get_mediaDuration());

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

        //设置封面,自动封面获取颜色

        ImageView play_now_cover = (ImageView) findViewById(R.id.play_now_cover);

        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(sArtworkUri, Data.getAlbumId(Data.getPosition()));
        Glide
                .with(this)
                .load(uri)
                .placeholder(R.drawable.default_album)
                .crossFade(300)
                .into(play_now_cover);


        Bitmap cover = getArtwork(this, id, Data.getAlbumId(Data.getPosition()), true);
        setBackColor(cover);

        //设置歌曲名，歌手
        TextView play_now_song = (TextView) findViewById(R.id.play_now_song);
        TextView play_now_singer = (TextView) findViewById(R.id.play_now_singer);
        play_now_song.setText(title);
        play_now_singer.setText(artist);

        //设置播放按钮
        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.play_or_pause);
        if (Data.getState() == playing) {
            floatingActionButton.setImageResource(R.drawable.pause_black);
        } else if (Data.getState() == pausing) {
            floatingActionButton.setImageResource(R.drawable.play_black);
        }


    }
    private class MsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //更新UI
            ImageView play_pause_button = (ImageView) findViewById( R.id.play_pause_button);
            main_song_title.setText(Data.getTitle(Data.getPosition()));
            if (Data.getState() == pausing) {
                play_pause_button.setImageResource(R.drawable.play_black);
            } else if (Data.getState() == playing){
                play_pause_button.setImageResource(R.drawable.pause_black);
            }
            ChangeScrollingUpPanel(Data.getPosition());
        }

    }
    public void play_now_menu_button(View v){
        menu_util.popupMenu(this,v, Data.getPosition());
    }



}
