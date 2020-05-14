/*******************************************************************************
 * Copyright (C) 2016 Kristian Sloth Lauszus. All rights reserved.
 *
 * This software may be distributed and modified under the terms of the GNU
 * General Public License version 2 (GPL2) as published by the Free Software
 * Foundation and appearing in the file GPL2.TXT included in the packaging of
 * this file. Please note that GPL2 Section 2[b] requires that all works based
 * on this software must also be made publicly available under the terms of
 * the GPL2 ("Copyleft").
 *
 * Contact information
 * -------------------
 *
 * Kristian Sloth Lauszus
 * Web      :  http://www.lauszus.com
 * e-mail   :  lauszus@gmail.com
 ******************************************************************************/

package com.faceit.faceitapp;


import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class FaceRecognitionAppActivity extends AppCompatActivity {
    // Gets ArrayList of installed applications
    // Gets ArrayList of installed applications
    private ArrayList<AppInfo> getInstalledApps() {

        ArrayList<AppInfo> res = new ArrayList<>();
        // Get list of aps with given flags (filters)
        final PackageManager packageManager = getApplicationContext().getPackageManager();
        final List<PackageInfo> allInstalledPackages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA);
        for(PackageInfo p : allInstalledPackages) {
            // add only apps with application icon
            Intent intentOfStartActivity = packageManager.getLaunchIntentForPackage(p.packageName);
            if(intentOfStartActivity == null)
                continue;
            Drawable applicationIcon = null;
            try {
                applicationIcon = packageManager.getActivityIcon(intentOfStartActivity);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if(applicationIcon != null && !packageManager.getDefaultActivityIcon().equals(applicationIcon) && !getApplicationContext().getPackageName().equals(p.packageName)) {
                AppInfo newInfo = new AppInfo();
                newInfo.app_name = p.applicationInfo.loadLabel(getPackageManager()).toString();
                newInfo.package_name = p.packageName;
                res.add(newInfo);
            }
        }
        return res;
    }


    private static Button showButton; // Button that shows
    public static ProgressBar loadingProgressBar;

    // If input 1 then shows loading bar, hides button else show button hide bar
    public static void ShowHideProgressBar(int option){
        if (option == 1){
            loadingProgressBar.setVisibility(View.VISIBLE);
            showButton.setVisibility(View.INVISIBLE);
        }
        else{
            loadingProgressBar.setVisibility(View.INVISIBLE);
            showButton.setVisibility(View.VISIBLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init button, progress bar
        showButton = findViewById(R.id.showButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);




        /* Check whether needed permission is granted
        and if not, open settings to let user grant it
         */
        AppOpsManager appOps = (AppOpsManager) this.getSystemService(Context.APP_OPS_SERVICE);
        assert appOps != null;
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), this.getPackageName());
        boolean granted = (mode == AppOpsManager.MODE_ALLOWED);
        if (!granted) {
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }

        // Show loading bar
        ShowHideProgressBar(1);

        // Get list of apps and initialize writable database
        ArrayList<AppInfo> apps = getInstalledApps();
        DataBase db = new DataBase(getApplicationContext());
        if (!db.hasProfile() || !db.hasPassword()) {
            db.createNewProfile("Default", "true");
            Intent setPasswordIntent = new Intent(getApplicationContext(), PasswordActivity.class);
            setPasswordIntent.putExtra("mode", "set");
            startActivity(setPasswordIntent);
        }
        else{
            Intent checkPasswordIntent = new Intent(getApplicationContext(), PasswordActivity.class);
            checkPasswordIntent.putExtra("mode", "check");
            startActivity(checkPasswordIntent);
        }

        // iterate through found applications and update db if needed
        for (AppInfo app: apps){
            String app_name = app.app_name;
            String package_name = app.package_name;

            if (!db.containsApp(package_name)){
                db.addApp(app_name, package_name);
            }

        }

        ShowHideProgressBar(0);

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent showDetailsActivity = new Intent(getApplicationContext(),
                        ListOfAppsActivity.class);
                ShowHideProgressBar(1);
                startActivity(showDetailsActivity);
            }
        };

        Button adduserButton = findViewById(R.id.adduserbutton);
        adduserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FaceRecognitionAppActivity.this, AddUserActivity.class));
            }
        });

        showButton.setOnClickListener(onClickListener);

        db.close();


    }


    public void startService(View v) {
        /*
         * Binds button with starting service
         */
        Intent serviceIntent = new Intent(this, BlockService.class);
        ContextCompat.startForegroundService(this, serviceIntent); /*start foreground service
        which won`t be killed by system(less likely)*/
    }

    public void stopService(View v) {
        /*
         * Binds button with stopping service
         */
        Intent serviceIntent = new Intent(this, BlockService.class);
        stopService(serviceIntent);
    }

    public void showProfiles(View v){
        Intent showProfilesIntent = new Intent(this, ProfilesActivity.class);
        startActivity(showProfilesIntent);
    }

    public void passwordActivity(View v){
        Intent passwordIntent = new Intent(this, PasswordActivity.class);
        passwordIntent.putExtra("mode", "update");
        startActivity(passwordIntent);
    }
}
