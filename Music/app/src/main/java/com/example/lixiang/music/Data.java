package com.example.lixiang.music;

/**
 * Created by lixiang on 2017/3/16.
 */

public class Data {
    private static int mediaDuration = 0;
    private static int mediaCurrentPosition = 0;
    //playMode 0:列表重复 1:随机 2:单曲重复 3:顺序
    private static int playMode = 3;

    public static int get_mediaDuration() {
        return mediaDuration;
    }
    public static int get_mediaCurrentPosition() {
        return mediaCurrentPosition;
    }
    public static int getPlayMode(){
        return  playMode;
    }

    public static void set_mediaDuration(int media_duration) {
        Data.mediaDuration = media_duration;
    }
    public static void set_mediaCurrentPosition(int media_CurrentPosition) {
        Data.mediaCurrentPosition = media_CurrentPosition;
    }
    public static void setPlayMode(int playMode){
        Data.playMode = playMode;
    }
}
