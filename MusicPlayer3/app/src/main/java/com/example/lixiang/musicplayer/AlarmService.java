package com.example.lixiang.musicplayer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

public class AlarmService extends Service {
    public AlarmService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
        // 设置1秒定时
        int duration = intent.getIntExtra("duration",1)*60*1000;
        long setTime = duration + SystemClock.elapsedRealtime();
        Intent i = new Intent(this,AlarmReceiver.class);
        PendingIntent p = PendingIntent.getBroadcast(this,0,i,0);
        //设置定时任务
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,setTime,p);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
