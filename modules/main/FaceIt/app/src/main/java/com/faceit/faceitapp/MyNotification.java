package com.faceit.faceitapp;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.Build;

public class MyNotification extends Application {
    //*Class for creating notification */
    public static final String CHANNEL_ID = "blockServiceChannel";

    @Override
    public void onCreate(){
        super.onCreate();
        createNotificationChannel();
    }
    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Blocking apps",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            serviceChannel.setLightColor(Color.BLUE);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}

