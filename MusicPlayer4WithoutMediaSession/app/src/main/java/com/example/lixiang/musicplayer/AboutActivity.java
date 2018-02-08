package com.example.lixiang.musicplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import static com.example.lixiang.musicplayer.R.id.setting_toolbar;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar about_toolbar = (Toolbar) findViewById(R.id.about_toolbar);
        about_toolbar.setTitleTextColor(getResources().getColor(R.color.colorCustomAccent));
        about_toolbar.setTitle("关于");
        setSupportActionBar(about_toolbar);//设置返回键可用
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
