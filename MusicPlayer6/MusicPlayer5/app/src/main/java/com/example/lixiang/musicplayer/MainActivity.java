package com.example.lixiang.musicplayer;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.TimePickerDialog;
import android.app.usage.UsageEvents;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gyf.barlibrary.ImmersionBar;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.tapadoo.alerter.Alerter;

import org.polaric.colorful.CActivity;
import org.polaric.colorful.ColorPickerDialog;
import org.polaric.colorful.Colorful;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import static android.R.attr.bitmap;
import static android.R.attr.logo;
import static android.R.attr.tag;
import static android.R.attr.width;
import static android.view.View.GONE;
import static com.example.lixiang.musicplayer.Data.Recent_position;
import static com.example.lixiang.musicplayer.Data.deleteAction;
import static com.example.lixiang.musicplayer.Data.findPositionById;
import static com.example.lixiang.musicplayer.Data.initialize;
import static com.example.lixiang.musicplayer.Data.mediaChangeAction;
import static com.example.lixiang.musicplayer.Data.nextAction;
import static com.example.lixiang.musicplayer.Data.pauseAction;
import static com.example.lixiang.musicplayer.Data.pausing;
import static com.example.lixiang.musicplayer.Data.playAction;
import static com.example.lixiang.musicplayer.Data.playing;
import static com.example.lixiang.musicplayer.Data.previousAction;
import static com.example.lixiang.musicplayer.Data.seektoAction;
import static com.example.lixiang.musicplayer.Data.serviceStarted;
import static com.example.lixiang.musicplayer.Data.shuffleChangeAction;
import static com.example.lixiang.musicplayer.R.id.control_layout;
import static com.example.lixiang.musicplayer.R.id.drawer_layout;
import static com.example.lixiang.musicplayer.R.id.main_toolbar;
import static com.example.lixiang.musicplayer.R.id.play_now_back_color;
import static com.example.lixiang.musicplayer.R.id.play_now_cover;
import static com.example.lixiang.musicplayer.R.id.play_pause_button;
import static com.example.lixiang.musicplayer.R.id.random_play_button;
import static com.example.lixiang.musicplayer.R.id.random_play_text;
import static com.example.lixiang.musicplayer.R.id.start;
import static com.example.lixiang.musicplayer.getCover.getArtwork;
import static com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState.COLLAPSED;
import static com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState.DRAGGING;
import static com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelState.EXPANDED;
import static java.security.AccessController.getContext;

public class MainActivity extends CActivity {
    private TextView main_song_title;
    private static SeekBar seekBar;;
    private FloatingActionButton Floatingbar;
    private String path;
    private String title;
    private int album_id;
    private String artist;
    private int id;
    private ViewPager viewPager;
    private MsgReceiver msgReceiver;
    private ImageView shuffle_button;
    private ImageView repeat_button;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private CardView main_control_ui;
    private boolean isfromSc = false;
    private int screenHeight;
    private int flag = 0;
    private int cx = 0;
    private int cy = 0;
    private int finalRadius = 0;
    private FloatingActionButton floatingActionButton;
    private ImageView play_now_back_color;
    private TextView now_on_play_text;
    private RelativeLayout main_control_layout;
    private RelativeLayout control_layout;
    private ImageView play_now_cover;
    private ImageView play_pause_button;
    private TextView play_now_song;
    private TextView play_now_singer;
    private PlayService playService;
    public static Handler handler;
    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("OnCreate执行", "OnCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化全局变量
        floatingActionButton = (FloatingActionButton) findViewById(R.id.play_or_pause);
        play_now_back_color = (ImageView) findViewById(R.id.play_now_back_color);
        now_on_play_text = (TextView) findViewById(R.id.now_on_play_text);
        main_control_layout = (RelativeLayout) findViewById(R.id.main_control_layout);
        control_layout = (RelativeLayout) findViewById(R.id.control_layout);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        repeat_button = (ImageView) findViewById(R.id.repeat_button);
        shuffle_button = (ImageView) findViewById(R.id.shuffle_button);
        main_song_title = (TextView) findViewById(R.id.main_song_title);
        Floatingbar = (FloatingActionButton) findViewById(R.id.play_or_pause);
        play_now_cover = (ImageView) findViewById(R.id.play_now_cover);
        play_pause_button = (ImageView) findViewById(R.id.play_pause_button);
        play_now_song = (TextView) findViewById(R.id.play_now_song);
        play_now_singer = (TextView) findViewById(R.id.play_now_singer);
        navigationView = (NavigationView) findViewById(R.id.nav_view);



        //多屏幕尺寸适应
        new screenAdaptionTask().execute();

       //沉浸状态栏
        ImmersionBar.with(this).barAlpha(0.3f).statusBarView(R.id.immersion_view).init();

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        SimpleFragmentPagerAdapter adapter = new SimpleFragmentPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    navigationView.setCheckedItem(R.id.suggestion_view);
                } else if (position == 1) {
                    navigationView.setCheckedItem(R.id.music_list_view);
                } else {
                    navigationView.setCheckedItem(R.id.download_view);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


        //新标题栏
        Toolbar main_toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        main_toolbar.setTitleTextColor(getResources().getColor(R.color.colorCustomAccent));
        setSupportActionBar(main_toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); //设置返回键可用
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //侧滑栏动画
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, main_toolbar, R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
//                mAnimationDrawable.stop();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                switch (flag) {
                    case 0:
                        break;
                    case R.id.nav_settings:
                        Intent settings_intent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(settings_intent);
                        flag = 0;
                        break;
                    case R.id.nav_about:
                        Intent about_intent = new Intent(MainActivity.this, AboutActivity.class);
                        startActivity(about_intent);
                        flag = 0;
                        break;
                    default:
                }
//                mAnimationDrawable.start();
            }
        };
        mDrawerToggle.syncState();
        drawerLayout.setDrawerListener(mDrawerToggle);
        //Nac_view点击
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.suggestion_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.suggestion_view) {
                    viewPager.setCurrentItem(0);
                } else if (id == R.id.music_list_view) {
                    viewPager.setCurrentItem(1);
                } else if (id == R.id.download_view) {
                    viewPager.setCurrentItem(2);
                } else if (id == R.id.nav_settings) {
                    flag = id;
                } else if (id == R.id.nav_about) {
                    flag = id;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        //申请动态权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            new initialTask().execute();
        }

        //动态注册广播
        msgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("play_broadcast");
        registerReceiver(msgReceiver, intentFilter);

        //上滑面板
        SlidingUpPanelLayout mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mLayout.setOverlayed(true);
        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                main_control_ui = (CardView) findViewById(R.id.main_control_ui);
                AlphaAnimation alphaAnimation = new AlphaAnimation(slideOffset, 1 - slideOffset);
                main_control_ui.startAnimation(alphaAnimation);
                alphaAnimation.setFillAfter(true);//动画结束后保持状态
                alphaAnimation.setDuration(0);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (previousState == DRAGGING && newState == EXPANDED) {
                    //禁止手势滑动
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                }
                if (previousState == DRAGGING && newState == COLLAPSED) {
                    //恢复手势滑动
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                }
            }
        });

        ImageView back = (ImageView) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SlidingUpPanelLayout mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
                if (mLayout != null &&
                        (mLayout.getPanelState() == EXPANDED || mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
                    mLayout.setPanelState(COLLAPSED);
                }
            }
        });
        ImageView about = (ImageView) findViewById(R.id.about);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.main_menu, popupMenu.getMenu());
                popupMenu.show();
//                MenuItem search = popupMenu.getMenu().findItem(R.id.search);
                MenuItem sleeper = popupMenu.getMenu().findItem(R.id.sleeper);
                MenuItem equalizer = popupMenu.getMenu().findItem(R.id.equalizer);
//                search.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//                    @Override
//                    public boolean onMenuItemClick(MenuItem menuItem) {
//                        viewPager.setCurrentItem(2);
//                        return true;
//                    }
//                });
                sleeper.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        //Timepicker Dialog
                        final java.util.Calendar c = java.util.Calendar.getInstance();
                        final int hourNow = c.get(java.util.Calendar.HOUR_OF_DAY);
                        final int minuteNow = c.get(Calendar.MINUTE);
                        new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourPicked, int minutePicked) {
                                int duration = (hourPicked - hourNow) * 60 + minutePicked - minuteNow;
                                if (hourPicked >= hourNow && duration > 0) {
                                    playService.deleteService(duration);
                                    Toast.makeText(MainActivity.this, "已经定时为" + duration + "分钟后关闭", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "所选时间须为当天，且距当前时间2小时内", Toast.LENGTH_LONG).show();
                                }
                            }
                        }, hourNow, minuteNow, true).show();
                        return true;
                    }
                });
                equalizer.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        Intent intent = new Intent(MainActivity.this, searchActivity.class);
                        startActivity(intent);
                        Toast.makeText(MainActivity.this, "Equalizer", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
            }
        });

        new setColorTask().execute();
//        SharedPreferences pref = getSharedPreferences("last_music",MODE_PRIVATE);
//        Data.setPosition(pref.getInt("Id",0));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
//        MenuItem search = menu.findItem(R.id.search);
        MenuItem sleeper = menu.findItem(R.id.sleeper);
        MenuItem equalizer = menu.findItem(R.id.equalizer);
//        search.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem menuItem) {
//                viewPager.setCurrentItem(2);
//                return true;
//            }
//        });
        sleeper.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                //Timepicker Dialog
                final java.util.Calendar c = java.util.Calendar.getInstance();
                final int hourNow = c.get(java.util.Calendar.HOUR_OF_DAY);
                final int minuteNow = c.get(Calendar.MINUTE);
                new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourPicked, int minutePicked) {
                        int duration = (hourPicked - hourNow) * 60 + minutePicked - minuteNow;
                        if (hourPicked >= hourNow && duration > 0) {
                            playService.deleteService(duration);
                            Toast.makeText(MainActivity.this, "已经定时为" + duration + "分钟后关闭", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "所选时间须为当天，且距当前时间2小时内", Toast.LENGTH_LONG).show();
                        }
                    }
                }, hourNow, minuteNow, true).show();

                return true;
            }
        });
        equalizer.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Toast.makeText(MainActivity.this, "Equalizer", Toast.LENGTH_SHORT).show();
                return false;
            }
        });


        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "您拒绝了该权限，程序将无法使用哦", Toast.LENGTH_SHORT).show();
            finish();
        } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            new initialTask().execute();
        }


    }

    @Override
    protected void onStart() {
        Log.v("OnStart执行", "OnStart");
        super.onStart();

        ensureServiceStarted();

        //绑定服务
        Intent bindIntent = new Intent(this, PlayService.class);
        bindService(bindIntent, conn, BIND_AUTO_CREATE);

        //拖动seekbar

        seekBar.setPadding(0,0,0,0);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    playService.seekto(progress*1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //更新seekbar
        updateSeekBar();

        //设置起始页
        if (isfromSc == false) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            String start_page = sharedPref.getString("start_page", "");
            switch (start_page) {
                case "suggestion":
                    viewPager.setCurrentItem(0);
                    break;
                case "list":
                    viewPager.setCurrentItem(1);
                    break;
                case "cloud":
                    viewPager.setCurrentItem(2);
                    break;
                default:
            }
        }

    }

    @Override
    public void onBackPressed() {
        SlidingUpPanelLayout mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        if (mLayout != null &&
                (mLayout.getPanelState() == EXPANDED || mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            mLayout.setPanelState(COLLAPSED);
        } else if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(msgReceiver);
        Log.v("OnDestory执行", "OnDestory");
        if (Data.getState() == pausing) {
            stopService(new Intent(this, PlayService.class));
//            SharedPreferences.Editor editor = getSharedPreferences("last_music",MODE_PRIVATE).edit();
//            editor.putInt("Id",Data.getPosition());
//            editor.apply();
        }
        super.onDestroy();
    }
    //以下为公共方法

    public void display() {
        //初始化ScrollingUpPanel
        ChangeScrollingUpPanel(Data.getPosition());
        main_song_title.setText(Data.getTitle(Data.getPosition()));
    }

    public void sendPermissionGranted() {
        Intent intent = new Intent("permission_granted");
        sendBroadcast(intent);
        Intent intent2 = new Intent("list_permission_granted");
        sendBroadcast(intent2);
        Log.v("发送初始广播","发送2");
    }

    //播放，暂停按钮
    public void play_or_pause(View view) {
        change_play_or_pause_state();
    }

    public void change_play_or_pause_state() {
//        ensureServiceStarted();
        if (Data.getState() == playing) {
            //发送暂停广播
            playService.pause();
//            Intent intent = new Intent("service_broadcast");
//            intent.putExtra("Control", pauseAction);
//            sendBroadcast(intent);
            Floatingbar.setImageResource(R.drawable.play_black);
        } else if (Data.getState() == pausing) {
            playService.resume();

            //发送恢复广播
//            Intent intent = new Intent("service_broadcast");
//            intent.putExtra("Control", playAction);
//            sendBroadcast(intent);
            Floatingbar.setImageResource(R.drawable.pause_black);
        }
    }

    public void previous(View v) {
//        ensureServiceStarted();
        playService.previous();

        //发送上一首广播
//        Intent intent = new Intent("service_broadcast");
//        intent.putExtra("Control", previousAction);
//        sendBroadcast(intent);
    }

    public void next(View v) {
//        ensureServiceStarted();
        playService.next();

        //发送下一首广播
//        Intent intent = new Intent("service_broadcast");
//        intent.putExtra("Control", nextAction);
//        sendBroadcast(intent);
    }

    public void changeRepeat(View v) {
        //playMode 0:列表重复 1:随机 2:单曲重复 3:顺序

        switch (Data.getPlayMode()) {
            case 3:
                Data.setPlayMode(0);
                Alerter.create(MainActivity.this)
                        .setTitle("提醒")
                        .setText("列表循环播放")
                        .setDuration(500)
                        .setBackgroundColor(Data.getColorAccentSetted())
                        .show();
                repeat_button.setImageResource(R.drawable.repeat);
                shuffle_button.setImageResource(R.drawable.shuffle_grey);
                break;
            case 0:
                Data.setPlayMode(2);
                Alerter.create(MainActivity.this)
                        .setTitle("提醒")
                        .setText("单曲循环播放")
                        .setDuration(500)
                        .setBackgroundColor(Data.getColorAccentSetted())
                        .show();
                repeat_button.setImageResource(R.drawable.repeat_one);
                shuffle_button.setImageResource(R.drawable.shuffle_grey);
                break;
            case 2:
                Data.setPlayMode(3);
                Alerter.create(MainActivity.this)
                        .setTitle("提醒")
                        .setText("顺序播放")
                        .setDuration(500)
                        .setBackgroundColor(Data.getColorAccentSetted())
                        .show();
                repeat_button.setImageResource(R.drawable.repeat_grey);
                shuffle_button.setImageResource(R.drawable.shuffle_grey);
                break;
            case 1:
                Data.setPlayMode(0);
                Alerter.create(MainActivity.this)
                        .setTitle("提醒")
                        .setText("列表重复播放")
                        .setDuration(500)
                        .setBackgroundColor(Data.getColorAccentSetted())
                        .show();
                shuffle_button.setImageResource(R.drawable.shuffle_grey);
                repeat_button.setImageResource(R.drawable.repeat);
                break;
            default:
        }
    }

    public void changeShuffle(View v) {
        if (Data.getPlayMode() == 1) {
            Data.setPlayMode(3);
            Alerter.create(MainActivity.this)
                    .setTitle("提醒")
                    .setText("顺序播放")
                    .setDuration(500)
                    .setBackgroundColor(Data.getColorAccentSetted())
                    .show();
            shuffle_button.setImageResource(R.drawable.shuffle_grey);
            repeat_button.setImageResource(R.drawable.repeat_grey);
        } else {
            Data.setPlayMode(1);
            Alerter.create(MainActivity.this)
                    .setTitle("提醒")
                    .setText("随机播放")
                    .setDuration(500)
                    .setBackgroundColor(Data.getColorAccentSetted())
                    .show();
            shuffle_button.setImageResource(R.drawable.shuffle);
            repeat_button.setImageResource(R.drawable.repeat_grey);
        }
    }

    public void animation_change_color(int Int) {
        if (cx==0){
        cx = floatingActionButton.getLeft() + main_control_layout.getLeft()+floatingActionButton.getWidth()/2;
        cy = control_layout.getTop()-seekBar.getTop()+floatingActionButton.getTop()+floatingActionButton.getHeight()/2;
        finalRadius = Math.max(play_now_back_color.getWidth(), play_now_back_color.getHeight());}
        final int Int1 = Int;
        final RelativeLayout activity_now_play = (RelativeLayout) findViewById(R.id.activity_now_play);
        if (cx != 0) {
            Animator anim = ViewAnimationUtils.createCircularReveal(play_now_back_color, cx, cy, 0, finalRadius);
            play_now_back_color.setBackgroundColor(Int);
            anim.setDuration(600);
            anim.start();
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    activity_now_play.setBackgroundColor(Int1);
                }
            });
        }
        now_on_play_text.setTextColor(Int);
    }

    public void ChangeScrollingUpPanel(int position) {
        path = Data.getData(position);
        title = Data.getTitle(position);
        album_id = Data.getAlbumId(position);
        artist = Data.getArtist(position);
        id = Data.getId(position);
        seekBar.setProgress(0);
        seekBar.setMax(Data.get_mediaDuration(position)/1000);
//设置播放模式按钮
        //playMode 0:列表重复 1:随机 2:单曲重复 3:顺序
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
        Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
        Uri uri = ContentUris.withAppendedId(sArtworkUri, Data.getAlbumId(Data.getPosition()));
        Glide.with(this).load(uri).placeholder(R.drawable.default_album).crossFade(300).into(play_now_cover);
        new setBackColorTask().execute();

        //设置歌曲名，歌手
        play_now_song.setText(title);
        play_now_singer.setText(artist);

        //设置播放按钮

        if (Data.getState() == playing) {
            floatingActionButton.setImageResource(R.drawable.pause_black);
            play_pause_button.setImageResource(R.drawable.pause_black);
        } else if (Data.getState() == pausing) {
            floatingActionButton.setImageResource(R.drawable.play_black);
            play_pause_button.setImageResource(R.drawable.play_black);
        }


    }

    private class MsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //更新UI
            if (intent.getIntExtra("UIChange", 0) == pauseAction) {
                play_pause_button.setImageResource(R.drawable.play_black);
                floatingActionButton.setImageResource(R.drawable.play_black);
            }
            if (intent.getIntExtra("UIChange", 0) == playAction) {
                play_pause_button.setImageResource(R.drawable.pause_black);
                floatingActionButton.setImageResource(R.drawable.pause_black);
            }
            if (intent.getIntExtra("UIChange", 0) == mediaChangeAction) {
                main_song_title.setText(Data.getTitle(Data.getPosition()));
                ChangeScrollingUpPanel(Data.getPosition());
            }
            if (intent.getIntExtra("onDestroy", 0) == 1) {
                finish();
            }
            if (intent.getIntExtra("viewPagerChange", -1) != -1) {
                isfromSc = true;
                viewPager.setCurrentItem(intent.getIntExtra("viewPagerChange", -1));
            }
        }

    }

    public void play_now_menu_button(View v) {
        menu_util.popupMenu(this, v, Data.getPosition());
    }

    private void ensureServiceStarted() {
        if (Data.getServiceState() == false) {
            Intent intent = new Intent();
            intent.putExtra("ACTION", initialize);
            Data.setPosition(0);
            intent.setClass(this, PlayService.class);
            startService(intent);

        }
    }

    private void getScreenDimension() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenHeight = dm.heightPixels;
    }

    private class setBackColorTask extends AsyncTask {
        @Override
        protected Object doInBackground(Object[] objects) {
            Bitmap cover = getArtwork(MainActivity.this, id, Data.getAlbumId(Data.getPosition()), true);
            Palette p = Palette.from(cover).generate();
            final Palette.Swatch s1 = p.getVibrantSwatch();
            Palette.Swatch s2 = p.getDarkVibrantSwatch();
            Palette.Swatch s3 = p.getLightVibrantSwatch();
            final Palette.Swatch s4 = p.getMutedSwatch();
            Palette.Swatch s5 = p.getLightVibrantSwatch();
            Palette.Swatch s6 = p.getDarkVibrantSwatch();
            Palette.Swatch s7 = p.getDominantSwatch();
            if (s1 != null) {
                return s1.getRgb();
            } else if (s4 != null) {
                return s4.getRgb();
            } else if (s2 != null) {
                return s2.getRgb();
            } else if (s3 != null) {
                return s3.getRgb();
            } else if (s5 != null) {
                return s5.getRgb();
            } else if (s6 != null) {
                return s6.getRgb();
            } else if (s7 != null) {
                return s7.getRgb();
            } else {
                //default color
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            animation_change_color((int) o);
        }
    }

    private class initialTask extends AsyncTask{
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            display();
            sendPermissionGranted();
            Log.v("发送初始广播","发送");
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            Data.initialMusicInfo(MainActivity.this);
            return null;

        }
    }


    private class screenAdaptionTask extends AsyncTask{
        @Override
        protected Object doInBackground(Object[] objects) {
            getScreenDimension();
            return (int) (screenHeight * 0.6);
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            RelativeLayout.LayoutParams lp_play_now_cover = (RelativeLayout.LayoutParams) play_now_cover.getLayoutParams();
            lp_play_now_cover.height = (int) o;
            play_now_cover.setLayoutParams(lp_play_now_cover);
        }
    }

    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //返回一个MsgService对象
            playService = ((PlayService.musicBinder) service).getService();
        }
    };

    private void updateSeekBar(){
        Timer mTimer = new Timer();
        TimerTask task =new TimerTask() {
            @Override
            public void run() {
                mediaPlayer = playService.getMediaPlayer();
                if (mediaPlayer != null && mediaPlayer.isPlaying()){
                    seekBar.setProgress(playService.getMediaPlayer().getCurrentPosition()/1000);}
            }
        };
        mTimer.schedule(task,500,1000);
    }

    private class setColorTask extends AsyncTask{
        @Override
        protected Object doInBackground(Object[] objects) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            String primary_color = sharedPref.getString("primary_color", "");
            switch (primary_color) {
                case "red":
                    return R.color.md_red_500;
                case "pink":
                    return R.color.md_pink_500;
                case "purple":
                    return R.color.md_purple_500;
                case "deep_purple":
                    return R.color.md_deep_purple_500;
                case "indigo":
                    return R.color.md_indigo_500;
                case "blue":
                    return R.color.md_blue_500;
                case "light_blue":
                    return R.color.md_light_blue_500;
                case "cyan":
                    return R.color.md_cyan_500;
                case "teal":
                    return R.color.md_teal_500;
                case "green":
                    return R.color.md_green_500;
                case "light_green":
                    return R.color.md_light_green_500;
                case "lime":
                    return R.color.md_lime_500;
                case "yellow":
                    return R.color.md_yellow_500;
                case "amber":
                    return R.color.md_amber_500;
                case "orange":
                    return R.color.md_orange_500;
                case "deep_orange":
                    return R.color.md_deep_orange_500;
                default:
            }
            return R.color.md_teal_500;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            int color = getResources().getColor((int)o);
            View immersionView = findViewById(R.id.immersion_view);
            immersionView.setBackgroundColor(color);
            Data.setColorPrimarySetted((int) o);
        }
    }

}
