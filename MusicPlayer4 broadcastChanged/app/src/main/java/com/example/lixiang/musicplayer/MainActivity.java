package com.example.lixiang.musicplayer;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
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
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.tapadoo.alerter.Alerter;

import org.polaric.colorful.CActivity;
import org.polaric.colorful.ColorPickerDialog;
import org.polaric.colorful.Colorful;

import java.util.Arrays;
import java.util.Calendar;

import static android.R.attr.width;
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
import static com.example.lixiang.musicplayer.R.id.drawer_layout;
import static com.example.lixiang.musicplayer.R.id.main_toolbar;
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
    private SeekBar seekBar;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("OnCreate执行", "OnCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
//                mAnimationDrawable.start();
            }
        };
        mDrawerToggle.syncState();
        drawerLayout.setDrawerListener(mDrawerToggle);
        //Nac_view点击
        navigationView = (NavigationView) findViewById(R.id.nav_view);
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
                    Intent settings_intent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(settings_intent);
//                    Toast.makeText(MainActivity.this, "settings", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.nav_about) {
                    Intent activity_intent = new Intent(MainActivity.this, AboutActivity.class);
                    startActivity(activity_intent);
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });


        //此FLAG可使状态栏透明，且当前视图在绘制时，从屏幕顶端开始即top = 0开始绘制，这也是实现沉浸效果的基础
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);//可不加
        }

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

                            Intent intent = new Intent("service_broadcast");
                            intent.putExtra("Control", deleteAction);
                            intent.putExtra("DelayControl", duration);
                            sendBroadcast(intent);

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
            Data.initialMusicInfo(this);
            display();
            sendPermissionGranted();
            //重启界面
            Intent restart_intent = this.getPackageManager().getLaunchIntentForPackage(this.getPackageName());
            restart_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(restart_intent);
        }


    }

    @Override
    protected void onStart() {
        Log.v("OnStart执行", "OnStart");
        super.onStart();

        //申请动态权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            Data.initialMusicInfo(this);
            display();
            sendPermissionGranted();
        }

        //开服务
        ensureServiceStarted();

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
                AlphaAnimation alphaAnimation = new AlphaAnimation(slideOffset, 1 - slideOffset);
                main_control_ui.startAnimation(alphaAnimation);
                alphaAnimation.setFillAfter(true);//动画结束后保持状态
                alphaAnimation.setDuration(0);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                Log.v("State", "previousstate" + previousState);
                Log.v("State", "previousstate" + newState);
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

        //拖动seekbar
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    Intent intent = new Intent("service_broadcast");
                    intent.putExtra("Control", seektoAction);
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

        //起始页
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String start_page = sharedPref.getString("start_page", "");

        Log.v("初始页", "初始页" + start_page);
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
        ImageView about =(ImageView) findViewById(R.id.about);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.main_menu,popupMenu.getMenu());
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

                                    Intent intent = new Intent("service_broadcast");
                                    intent.putExtra("Control", deleteAction);
                                    intent.putExtra("DelayControl", duration);
                                    sendBroadcast(intent);

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
            }
        });

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
        }
        super.onDestroy();
    }
    //以下为公共方法

    public void display() {

        //初始化ScrollingUpPanel
        ChangeScrollingUpPanel(0);
        main_song_title = (TextView) findViewById(R.id.main_song_title);
        main_song_title.setText(Data.getTitle(Data.getPosition()));

    }

    public void sendPermissionGranted() {
        Intent intent = new Intent("permission_granted");
        sendBroadcast(intent);
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
        } else if (s2 != null) {
            animation_change_color(s2.getRgb());
        } else if (s3 != null) {
            animation_change_color(s3.getRgb());
        } else if (s5 != null) {
            animation_change_color(s5.getRgb());
        } else if (s6 != null) {
            animation_change_color(s6.getRgb());
        } else if (s7 != null) {
            animation_change_color(s7.getRgb());
        } else {
            //default color
        }

    }

    //播放，暂停按钮
    public void play_or_pause(View view) {
        Floatingbar = (FloatingActionButton) findViewById(R.id.play_or_pause);
        change_play_or_pause_state();
    }

    public void change_play_or_pause_state() {
        ensureServiceStarted();
        if (Data.getState() == playing) {
            //发送暂停广播
            Intent intent = new Intent("service_broadcast");
            intent.putExtra("Control", pauseAction);
            sendBroadcast(intent);
            Floatingbar.setImageResource(R.drawable.play_black);
        } else if (Data.getState() == pausing) {
            //发送恢复广播
            Intent intent = new Intent("service_broadcast");
            intent.putExtra("Control", playAction);
            sendBroadcast(intent);
            Floatingbar.setImageResource(R.drawable.pause_black);
        }
    }

    public void previous(View v) {
        ensureServiceStarted();
        //发送上一首广播
        Intent intent = new Intent("service_broadcast");
        intent.putExtra("Control", previousAction);
        sendBroadcast(intent);
    }

    public void next(View v) {
        ensureServiceStarted();
        //发送下一首广播
        Intent intent = new Intent("service_broadcast");
        intent.putExtra("Control", nextAction);
        sendBroadcast(intent);
    }

    public void changeRepeat(View v) {
        //playMode 0:列表重复 1:随机 2:单曲重复 3:顺序
        repeat_button = (ImageView) findViewById(R.id.repeat_button);
        shuffle_button = (ImageView) findViewById(R.id.shuffle_button);
        switch (Data.getPlayMode()) {
            case 3:
                Data.setPlayMode(0);
                Alerter.create(MainActivity.this)
                        .setTitle("提醒")
                        .setText("列表重复播放")
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
                        .setText("单曲重复播放")
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
        final int Int1 = Int;
        ImageView play_now_back_color = (ImageView) findViewById(R.id.play_now_back_color);
        TextView now_on_play_text = (TextView) findViewById(R.id.now_on_play_text);
        RelativeLayout control_layout = (RelativeLayout) findViewById(R.id.control_layout);
        ImageView play_now_cover = (ImageView) findViewById(R.id.play_now_cover);
        final RelativeLayout activity_now_play = (RelativeLayout) findViewById(R.id.activity_now_play);
        int cx = (control_layout.getLeft() + control_layout.getRight()) / 2;
        int cy = (play_now_cover.getTop());
        int finalRadius = Math.max(play_now_back_color.getWidth(), play_now_back_color.getHeight());
        if (cx != 0) {
            Animator anim = ViewAnimationUtils.createCircularReveal(play_now_back_color, cx, cy, 0, finalRadius);
            play_now_back_color.setBackgroundColor(Int);
            anim.setDuration(500);
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
        seekBar.setProgress(0);
        seekBar.setMax(Data.get_mediaDuration());
        seekBar.setPadding(0, 0, 0, 0);
        repeat_button = (ImageView) findViewById(R.id.repeat_button);
        shuffle_button = (ImageView) findViewById(R.id.shuffle_button);
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
        ImageView play_pause_button = (ImageView) findViewById(R.id.play_pause_button);
        if (Data.getState() == playing) {
            floatingActionButton.setImageResource(R.drawable.pause_black);
            play_pause_button.setImageResource(R.drawable.pause_black);
        } else if (Data.getState() == pausing) {
            floatingActionButton.setImageResource(R.drawable.play_black);
            play_pause_button.setImageResource(R.drawable.play_black);
        }

        //更新seekbar
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(Data.get_mediaCurrentPosition());
                handler.postDelayed(this, 500);
            }
        };
        handler.postDelayed(runnable, 500);
    }

    private class MsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //更新UI
            ImageView play_pause_button = (ImageView) findViewById(R.id.play_pause_button);
            FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.play_or_pause);
            main_song_title.setText(Data.getTitle(Data.getPosition()));
            if (intent.getIntExtra("UIChange", 0) == pauseAction) {
                play_pause_button.setImageResource(R.drawable.play_black);
                floatingActionButton.setImageResource(R.drawable.play_black);
            }
            if (intent.getIntExtra("UIChange", 0) == playAction) {
                play_pause_button.setImageResource(R.drawable.pause_black);
                floatingActionButton.setImageResource(R.drawable.pause_black);
            }
            if (intent.getIntExtra("UIChange", 0) == mediaChangeAction) {
                ChangeScrollingUpPanel(Data.getPosition());
            }
            if (intent.getIntExtra("onDestroy", 0) == 1) {
                finish();
            }
            if (intent.getIntExtra("viewPagerChange", -1) != -1) {
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

    private int getScreenHeight() {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;       // 屏幕高度（像素）
        float density = dm.density;         // 屏幕密度（0.75 / 1.0 / 1.5）
        // 算法:屏幕宽度（像素）/屏幕密度
        int screenHeight = (int) (height / density);// 屏幕高度(dp)
        return screenHeight;

    }

    private int getScreenWidth() {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;         // 屏幕宽度（像素）
        float density = dm.density;         // 屏幕密度（0.75 / 1.0 / 1.5）
        // 屏幕宽度算法:屏幕宽度（像素）/屏幕密度
        int screenWidth = (int) (width / density);  // 屏幕宽度(dp)
        return screenWidth;

    }

}
