package com.example.lixiang.musicplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;

import static com.example.lixiang.musicplayer.R.id.main_toolbar;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        Toolbar setting_toolbar = (Toolbar) findViewById(R.id.setting_toolbar);
        setting_toolbar.setTitleTextColor(getResources().getColor(R.color.colorCustomAccent));
        setting_toolbar.setTitle("设置");
        setSupportActionBar(setting_toolbar);//设置返回键可用
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }
private void dialog(View v){
    ColorPickerDialog.newBuilder().setColor(0xFF000000).setDialogTitle(R.string.colorPickerDialogTitle).show(this);
}

}
