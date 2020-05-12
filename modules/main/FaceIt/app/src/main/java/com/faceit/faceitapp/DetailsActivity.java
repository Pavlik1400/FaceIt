package com.faceit.faceitapp;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.faceit.faceitapp.DataBase2;
import com.faceit.faceitapp.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DetailsActivity extends AppCompatActivity {

    private ArrayList<String> appsNames = new ArrayList<>();
    private ArrayList<String> appsPackageNames = new ArrayList<>();
    private ArrayList<String> appsLocked = new ArrayList<>();
    private ArrayList<Drawable> appsIcons = new ArrayList<>();

    private RecyclerView appsRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        appsRecyclerView = findViewById(R.id.appsRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        appsRecyclerView.setLayoutManager(layoutManager);

        // Create hash map package_name - icon
        HashMap<String, Drawable> icons = new HashMap<>();
        List<PackageInfo> packs = getPackageManager().getInstalledPackages(com.faceit.faceitapp.FaceRecognitionAppActivity.flags);
        for(int i=0;i<packs.size();i++) {
            PackageInfo p = packs.get(i);
            if ((p.versionName == null) | ((p.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1)) {
                continue;
            }
            Drawable icon = p.applicationInfo.loadIcon(getPackageManager());
            String package_name = p.packageName;
            icons.put(package_name, icon);
        }

        DataBase2 db = new DataBase2(getApplicationContext());

        appsLocked = db.getAllLocked(db.getChosenProfile());

        ArrayList<String> allApps = db.getAllApps();
        for (String app: allApps){
            String[] app_arr = app.split(",");

            String name = app_arr[0];
            String package_name = app_arr[1];
            Drawable icon = icons.get(package_name);

            appsNames.add(name);
            appsPackageNames.add(package_name);
            appsIcons.add(icon);
        }

        Log.d("SIZEOF", "appsNames: " + allApps.size());




        db.close();

        NewItemAdapter adapter = new NewItemAdapter(appsNames, appsPackageNames, appsLocked, appsIcons);

        appsRecyclerView.setAdapter(adapter);

        FaceRecognitionAppActivity.ShowHideProgressBar(0);

    }

}
