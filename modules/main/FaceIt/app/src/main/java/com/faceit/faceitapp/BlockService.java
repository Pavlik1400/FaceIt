package com.faceit.faceitapp;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.faceit.faceitapp.DataBase2;
import com.faceit.faceitapp.R;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import static com.faceit.faceitapp.MyNotification.CHANNEL_ID;

public class BlockService extends Service {
    private Timer timer; //timer for scheduling service work
    private boolean recognition_running = false;
    private String allowed_app = "None";
    private double time_from_last_open=-1;


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //*Method that starts service (can be called many times but I wouldn't do this)*/
        /*Creating notification that would allow run service in the foreground and therefore not to
         * be killed by system*/

        // Intent to open the main activity when notification is pressed
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

    @Override
    public void onDestroy() {
        super.onDestroy();//kill service
        stopTimerTask();//kill timer
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //*Allows communication between UI, maybe later I will implement it */
        return null;
    }

    private String getActiveApps() {
        //*Returns package name of the last active service or process*/
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

    public void runService(){
        //*Method for running service
        // argument is a package name of app to be blocked*/

        timer = new Timer();
        // Get database
        final DataBase2 db = new DataBase2(getApplicationContext());

        TimerTask timerTask = new TimerTask() {
            public void run() {
                String current_app_package = getActiveApps();
                // if locked do activity
                if (!current_app_package.equals("NULL")) {
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
                            !recognition_running && (System.currentTimeMillis() - time_from_last_open > 800)) {
                        allowed_app = "None";
                    }
                }

                Log.d("CURRENT APP", current_app_package);
                }
            };
        timer.schedule(timerTask, 0, 200);
    }
    public void stopTimerTask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
