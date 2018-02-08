package com.example.lixiang.music;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import static com.example.lixiang.music.Data.initialize;
import static com.example.lixiang.music.Data.pausing;
import static com.example.lixiang.music.Data.play;
import static com.example.lixiang.music.Data.playing;
import static com.example.lixiang.music.R.menu.main;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private int[] _ids;// 保存音乐ID临时数组
    private int[] _albumids;
    private String[] _artists;// 保存艺术家
    private String[] _titles;
    private String[] _data;// 标题临时数组
    private ListView listView;// 列表对象
    private Cursor cursor;
    private MusicListAdapter musicListAdapter;
    private String[] media_music_info;
    private MsgReceiver msgReceiver;
    private TextView main_song_title;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_DENIED){
            Toast.makeText(this, "您拒绝了该权限，程序将无法使用哦", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            display();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //新标题栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //申请动态权限
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }else {
            display();
        }

        //开服务
        Intent intent = new Intent();
        intent.putExtra("ACTION",initialize);
        Data.setPosition(0);
        intent.setClass(MainActivity.this, PlayService.class);
        startService(intent);

        //动态注册广播
        msgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("play_broadcast");
        registerReceiver(msgReceiver, intentFilter);

        main_song_title = (TextView) findViewById(R.id.main_song_title);
        main_song_title.setText(_titles[Data.getPosition()]);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
            // Handle the camera action
        if (id == R.id.nav_music_gallery) {

        } else if (id == R.id.nav_album_gallery) {

        } else if (id == R.id.nav_setting) {

        } else if (id == R.id.nav_website) {
                Uri webpage = Uri.parse("http://www.baidu.com");
                Intent web_intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (web_intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(web_intent);
                }

        } else if (id == R.id.nav_send) {
            Intent email_intent = new Intent(Intent.ACTION_SENDTO);
            email_intent.setData(Uri.parse("mailto:")); // only email apps should handle this
            email_intent.putExtra(Intent.EXTRA_EMAIL, "954186546@qq.com");
            email_intent.putExtra(Intent.EXTRA_SUBJECT, "应用使用反馈");
            if (email_intent.resolveActivity(getPackageManager()) != null) {
                startActivity(email_intent);
            }

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this,PlayService.class));
    }

    private void display() {
        //获取listview
        listView = (ListView) findViewById(R.id.music_list);
        showMusicList();
        //Listview点击，打开服务
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("ACTION",play);
                Data.setPosition(position);
                intent.setClass(MainActivity.this, PlayService.class);
                startService(intent);

            }
        });
    }


    private void showMusicList() {
        /**
         * 定义查找音乐信息数组，1.标题 2.音乐时间 3.艺术家 4.音乐id 5.显示名字 6.数据
         */
        media_music_info = new String[]{MediaStore.Audio.Media.TITLE,
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
        for (int i = 0; i < cursor.getCount(); i++) {
            _ids[i] = cursor.getInt(3);
            _titles[i] = cursor.getString(0);
            _artists[i] = cursor.getString(2);
           _data[i] = cursor.getString(5);
            _albumids[i] = cursor.getInt(6);
            cursor.moveToNext();// 将游标移到下一行
        }
        musicListAdapter = new MusicListAdapter(this, cursor);
        listView.setAdapter(musicListAdapter);
    }


    public void test (View v){
        Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();
    }
    public void start_play_now(View v){
        Intent intent = new Intent(this,PlayNow.class);
        startActivity(intent);
    }
    public void random_play(View v) {
        //随机播放
        Data.setPlayMode(1);
        Intent intent = new Intent();
        intent.putExtra("ACTION", play);
        intent.setClass(MainActivity.this, PlayService.class);
        startService(intent);
    }


    private class MsgReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            //更新UI
            ImageView play_pause_button = (ImageView) findViewById( R.id.play_pause_button);
            main_song_title.setText(_titles[Data.getPosition()]);
            if (Data.getState() == pausing) {
                play_pause_button.setImageResource(R.drawable.play_black);
            } else if (Data.getState() == playing){
                play_pause_button.setImageResource(R.drawable.pause_black);
            }
        }

    }

}
