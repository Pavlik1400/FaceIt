package com.faceit.faceitapp;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DetailsActivity extends AppCompatActivity {

    private List<String> appsNames = new ArrayList<>();
    private List<String> appsPackageNames = new ArrayList<>();
    private List<String> appsLocked = new ArrayList<>();
    private List<Drawable> appsIcons = new ArrayList<>();

    private RecyclerView mainRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mainRecyclerView = findViewById(R.id.mainRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mainRecyclerView.setLayoutManager(layoutManager);


        HashMap<String, Drawable> icons = new HashMap<>();
        List<PackageInfo> packs = getPackageManager().getInstalledPackages(FaceRecognitionAppActivity.flags);
        for(int i=0;i<packs.size();i++) {
            PackageInfo p = packs.get(i);
            if ((p.versionName == null) | ((p.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1)) {
                continue;
            }
            Drawable icon = p.applicationInfo.loadIcon(getPackageManager());
            String package_name = p.packageName;
            icons.put(package_name, icon);
        }

        DataBase dbHelper = new DataBase(getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor allApps = db.rawQuery("select * from " + DataBase.FeedEntry.TABLE_NAME,null);

        while (allApps.moveToNext()){
            String name = allApps.getString(allApps.getColumnIndex(DataBase.FeedEntry.COLUMN_NAME_APP_NAME));
            String package_name = allApps.getString(allApps.getColumnIndex(DataBase.FeedEntry.COLUMN_NAME_PACKAGE_NAME));
            String locked = allApps.getString(allApps.getColumnIndex(DataBase.FeedEntry.COLUMN_NAME_LOCKED));
            Drawable icon = icons.get(package_name);

            appsNames.add(name);
            appsPackageNames.add(package_name);
            appsLocked.add(locked);
            appsIcons.add(icon);

            //Log.d("READING FROM DB", "Read app name: "+ name);
        }
        db.close();

        NewItemAdapter adapter = new NewItemAdapter(appsNames, appsPackageNames, appsLocked, appsIcons);

        mainRecyclerView.setAdapter(adapter);

        FaceRecognitionAppActivity.ShowHideProgressBar(0);

    }

}
