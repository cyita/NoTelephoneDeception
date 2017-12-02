package com.hku.yita.notelephonedeception;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by linlijuan on 2017/12/1.
 */


public class PhoneService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("服务创建");
    }

    @Override
    public void onDestroy() {
        System.out.println("服务销毁");
        super.onDestroy();
    }



}
