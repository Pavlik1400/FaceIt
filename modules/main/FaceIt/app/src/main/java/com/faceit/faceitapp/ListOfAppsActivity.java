/**
 * Class that represents activity with all apps
 * List is made with recyclerView. This activity
 * Finds information about all installed apps and puts it in the
 * recyclerView with adapter `AppAdapter`
 */

package com.faceit.faceitapp;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ListOfAppsActivity extends AppCompatActivity {

    // names, package names, statuses and icons are kept in different containers
    private ArrayList<String> appsNames = new ArrayList<>();
    private ArrayList<String> appsPackageNames = new ArrayList<>();
    private ArrayList<String> appsLocked = new ArrayList<>();
    private ArrayList<Drawable> appsIcons = new ArrayList<>();

    // init main recyclerView
    private RecyclerView appsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        // assign recyclerView and set linear layout manager
        appsRecyclerView = findViewById(R.id.appsRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        appsRecyclerView.setLayoutManager(layoutManager);

        // This huge block of code finds all installed apps,
        // Filters them and saves in the containers inited above
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
                appsNames.add(p.applicationInfo.loadLabel(getPackageManager()).toString());
                appsPackageNames.add(p.packageName);
                appsIcons.add(applicationIcon);
            }
        }

        // Get list of locked apps from db
        DataBase db = new DataBase(getApplicationContext());
        appsLocked = db.getAllLocked(db.getChosenProfile());
        db.close();

        // set adapter
        AppAdapter adapter = new AppAdapter(appsNames, appsPackageNames, appsLocked, appsIcons);
        appsRecyclerView.setAdapter(adapter);

        // hide loading bar
        FaceRecognitionAppActivity.ShowHideProgressBar(0);

    }

}
