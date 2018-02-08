package com.example.lixiang.music;

/**
 * Created by lixiang on 2017/3/16.
 */

public class Data {
//程序常量
    public static int pausing = 0;
    public static int playing = 1;
    public static int resuming = 2;
    public static int initialize = -1;
    public static int play = 0;
    public static int previous = -1;
    public static int next = 1;
    public static  int seekto = 2;


//变量
    private static int mediaDuration = 0;
    private static int mediaCurrentPosition = 0;
    //playMode 0:列表重复 1:随机 2:单曲重复 3:顺序
    private static int playMode = 3;
    private static int position = 0;
    private static int  state = pausing;

    public static int get_mediaDuration() {
        return mediaDuration;
    }
    public static int get_mediaCurrentPosition() {
        return mediaCurrentPosition;
    }
    public static int getPlayMode(){
        return  playMode;
    }
    public static int getPosition(){ return position;}
    public static int getState(){return state;}

    public static void set_mediaDuration(int media_duration) {
        Data.mediaDuration = media_duration;
    }
    public static void set_mediaCurrentPosition(int media_CurrentPosition) {
        Data.mediaCurrentPosition = media_CurrentPosition;
    }
    public static void setPlayMode(int playMode){
        Data.playMode = playMode;
    }
    public static void setPosition(int position){
        Data.position = position;
    }
    public static void setState(int state){
        Data.state = state;
    }
}
