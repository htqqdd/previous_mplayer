package com.example.lixiang.music_player;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {
    public SimpleFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new RecommendFragment();
        } else if (position == 1) {
            return new MusiclistFragment();
        } else {
            return new DownloadFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
//        if (position == 0) {
//            return "建议";
//        } else if (position == 1) {
//            return "歌曲";
//        } else{
//            return "下载";
//        }
        return "";
    }
}
