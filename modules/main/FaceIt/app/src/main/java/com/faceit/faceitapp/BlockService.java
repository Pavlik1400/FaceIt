/*
 Copyright (C) 2020  PVY Soft. All rights reserved.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 Contact info:
 PVY Soft
 email: pvysoft@gmail.com
*/

package com.faceit.faceitapp;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import static com.faceit.faceitapp.Notification.CHANNEL_ID;

/**
 * Class that implements blocking service.
 * This service monitor running apps and block
 * those that supposed to be blocked
 */
public class BlockService extends Service {
    private Timer timer;
    private boolean recognition_running = false;
    private String allowed_app = "None";
    private double time_from_last_open=-1;

    /**
     * Method for creating the service
     */
    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Method that starts service (can be called many times but I wouldn't do this)
     * Creating notification that would allow run service in the foreground and therefore not to
     * be killed by system
     * @param intent intent is a simple message object that is used
     *               to communicate between android components
     * @param flags tell the system how to run the service
     * @param startId number that identifies service for the system
     * @return mode of running
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /* Intent to open the main activity when notification is pressed */
        Intent notificationIntent = new Intent(this, FaceRecognitionAppActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Lock process is running")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification); //start notification
        runService(); //actually service running
        return START_STICKY; //means that after killing system would try to restart service again
    }

    /**
     * Method for handling destruction
     * of the service
     */
    @Override
    public void onDestroy() {
        super.onDestroy();//kill service
        stopTimerTask();//kill timer
    }

    /**
     * Method for communication with other
     * parts of the program
     * @param intent is a simple message object that is used
     *              to communicate between android components
     * @return null
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //*Allows communication between UI, maybe later I will implement it */
        return null;
    }

    /**
     * Method search for the last active app
     * @return Returns package name of the last active service or process
     */
    private String getActiveApps() {
        String currentApp = "NULL";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if (!mySortedMap.isEmpty()) {
                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                }
            }
        } else {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfo = am.getRunningAppProcesses();
            currentApp = runningAppProcessInfo.get(0).processName;
        }
        return currentApp;
    }

    /**
     * Method that organises monitoring active apps
     * and calling for block activity
     * Work of service is based on Timer that
     * allows to control how often
     * program updates info about current app running
     */
    public void runService(){

        timer = new Timer();
        // Get database
        final DataBase db = new DataBase(getApplicationContext());

        TimerTask timerTask = new TimerTask() {
            public void run() {
                String current_app_package = getActiveApps();
                // if locked do activity
                if (!current_app_package.equals("NULL") && !current_app_package.equals("android")) {
                    if (db.isLocked(db.getChosenProfile(), current_app_package) && !recognition_running && allowed_app.equals("None")) {
                        allowed_app = current_app_package;
                        recognition_running = true;
                        Intent dialogIntent = new Intent(BlockService.this, UserLockRecognitionActivity.class);
                        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(dialogIntent);
                    } else if (current_app_package.equals(allowed_app) && recognition_running) {
                        time_from_last_open = System.currentTimeMillis();
                        recognition_running = false;
                    } else if (!current_app_package.equals(allowed_app) &&
                            !recognition_running && (System.currentTimeMillis() - time_from_last_open > 500)) {
                        allowed_app = "None";
                    }
                }

                Log.d("CURRENT APP", current_app_package);
                }
            };
        timer.schedule(timerTask, 0, 200);
    }

    /**
     * Method that properly kills
     * the Timer
     */
    public void stopTimerTask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
