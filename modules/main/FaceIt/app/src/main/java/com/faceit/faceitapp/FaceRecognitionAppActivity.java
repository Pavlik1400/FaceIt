/**
 * This is class with main activity. Loads all buttons, sets onClickListeners to them, etc
 */

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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class FaceRecognitionAppActivity extends AppCompatActivity {

    // Init loading progress bar
    public static ProgressBar loadingProgressBar;

    /**
     * If input 1 then shows loading bar, hides button else show button hide bar
     * @param option - 1/0
     */
    public static void ShowHideProgressBar(int option){
        if (option == 1){
            loadingProgressBar.setVisibility(View.VISIBLE);
        }
        else{
            loadingProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init progress bar
        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        // Show loading bar
        ShowHideProgressBar(1);

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

        // Init data base
        DataBase db = new DataBase(getApplicationContext());

        // If there is no profile or no password in db,
        // than create password and new profile
        if (!db.hasProfile() || !db.hasPassword()) {
            db.createNewProfile("Default", "true");
            Intent setPasswordIntent = new Intent(getApplicationContext(), PasswordActivity.class);
            setPasswordIntent.putExtra("mode", "set");
            startActivity(setPasswordIntent);
        }
        // Else check password
        else{
            Intent checkPasswordIntent = new Intent(getApplicationContext(), PasswordActivity.class);
            checkPasswordIntent.putExtra("mode", "check");
            startActivity(checkPasswordIntent);
        }

        // hide loading bar
        ShowHideProgressBar(0);

        db.close();
    }

    /**
     * Binds button that shows all application
     * @param v - button View
     */
    public void showInstalledApps(View v){
        Intent showDetailsActivity = new Intent(getApplicationContext(),
                ListOfAppsActivity.class);
        ShowHideProgressBar(1); // Loading takes some time, so show bar
        startActivity(showDetailsActivity);
    }

    /**
     * Binds button with starting service
     * @param v - button View
     */
    public void startService(View v) {
        Intent serviceIntent = new Intent(this, BlockService.class);
        ContextCompat.startForegroundService(this, serviceIntent); /*start foreground service
        which won`t be killed by system(less likely)*/
    }

    /**
     * Binds button with stopping service
     * @param v - button View
     */
    public void stopService(View v) {
        Intent serviceIntent = new Intent(this, BlockService.class);
        stopService(serviceIntent);
    }

    /**
     * Binds button that opens profiles menu
     * @param v - button View
     */
    public void showProfiles(View v){
        Intent showProfilesIntent = new Intent(this, ProfilesActivity.class);
        startActivity(showProfilesIntent);
    }

    /**
     * Binds button that opens update password menu
     * @param v - button View
     */
    public void passwordActivity(View v){
        Intent passwordIntent = new Intent(this, PasswordActivity.class);
        passwordIntent.putExtra("mode", "update");
        startActivity(passwordIntent);
    }

    /**
     * Binds button that adds new User
     * @param v - button View
     */
    public void addUser(View v){
        startActivity(new Intent(FaceRecognitionAppActivity.this, AddUserActivity.class));
    }
}
