package com.example.applocker4;

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
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import static com.example.applocker4.MyNotification.CHANNEL_ID;

public class BlockService extends Service {
    private Timer timer; //timer for scheduling service work

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
        Intent notificationIntent = new Intent(this, MainActivity.class);
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
        DataBase dbHelper = new DataBase(BlockService.this);
        final SQLiteDatabase db = dbHelper.getReadableDatabase();

        TimerTask timerTask = new TimerTask() {
            public void run() {

                String current_app_package = getActiveApps();

                // create parameters fot finding app in DataBase by package name
                // We need only information about locking
                String[] projection = {DataBase.FeedEntry._ID, DataBase.FeedEntry.COLUMN_NAME_LOCKED};
                String selection = DataBase.FeedEntry.COLUMN_NAME_PACKAGE_NAME + " = ?";
                String[] selectionArgs = { current_app_package };

                // Search
                Cursor currentAppCursor = db.query(
                        DataBase.FeedEntry.TABLE_NAME, projection, selection,
                        selectionArgs,null, null, null
                );

                // If found in data base
                if (currentAppCursor.moveToNext()) {
                    // Get lock status
                    String lockedStatus = currentAppCursor.getString(currentAppCursor.
                            getColumnIndex(DataBase.FeedEntry.COLUMN_NAME_LOCKED));

                    // if locked do activity
                    if (lockedStatus.equals("true")) {

                        backToHome(); //return to home screen
                    }

                    Log.d("CURRENT APP", current_app_package);
                }
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
    public void backToHome(){
        //*Return user to home screen*/
        Intent runTest = new Intent(getApplicationContext(), TestActivity.class);
        runTest.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(runTest);
//        Intent intent;
//        intent = new Intent(Intent.ACTION_MAIN);
//        intent.addCategory(Intent.CATEGORY_HOME);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
    }
}

