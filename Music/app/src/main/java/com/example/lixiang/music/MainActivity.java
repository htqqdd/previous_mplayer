package com.example.lixiang.music;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import static com.example.lixiang.music.getCover.getArtwork;


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

    private static final String MUSIC_LIST = "com.example.lixiang.music.list";
    /**
     * 定义查找音乐信息数组，1.标题 2.音乐时间 3.艺术家 4.音乐id 5.显示名字 6.数据
     */
    String[] media_music_info = new String[]{MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //新标题栏
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //获取listview
        listView = (ListView) findViewById(R.id.music_list);
        showMusicList();



        // ListView点击，打开play_now
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.putExtra("path", _data[position]);
                intent.putExtra("title", _titles[position]);
                intent.putExtra("artist", _artists[position]);
                intent.putExtra("id", _ids[position]);
                intent.putExtra("album_id", _albumids[position]);
                intent.setClass(MainActivity.this, PlayNow.class);
                startActivity(intent);



//                try {
//                    mp.reset();// 把各项参数恢复到初始状态
//                    mp.setDataSource(_data[position]);
//                    mp.prepare(); // 进行缓冲
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                mp.start();



//                mp.setDataSource(_data[position]);
//                Intent intent = new Intent();
//                intent.putExtra("url", _data[position]);
//                intent.setClass(MainActivity.this, PlayService.class);
//                startService(intent);



            }
        });



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
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
        getMenuInflater().inflate(R.menu.main, menu);
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


    private void showMusicList() {
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




}
