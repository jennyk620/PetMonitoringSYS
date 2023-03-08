package com.example.hpilitev3;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

//백그라운드 서비스를 위한 강제종료
public class ForecdTerminationService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) { //핸들링 하는 부분
        stopSelf(); //서비스 종료
    }
}
