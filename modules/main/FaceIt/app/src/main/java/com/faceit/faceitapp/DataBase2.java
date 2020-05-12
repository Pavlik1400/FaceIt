package com.faceit.faceitapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DataBase2 extends SQLiteOpenHelper {
    static final String TABLE_PROFILES = "profiles";
    static final String COLUMN_PROFILE_NAME = "name";
    static final String COLUMN_PROFILE_STATUS = "is_chosen";
    static final String COLUMN_LOCKED_APPS = "locked";
    static final String _ID = "_id";

    static final String TABLE_ALL_APPS = "all_apps";
    static final String COLUMN_APP_NAME = "name";
    static final String COLUMN_PACKAGE_NAME = "package_name";



    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "PROFILES.db";

    private static final String SQL_CREATE_PROFILES =
            "CREATE TABLE " + TABLE_PROFILES + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_PROFILE_NAME + " TEXT," +
                    COLUMN_PROFILE_STATUS + " TEXT," +
                    COLUMN_LOCKED_APPS + " TEXT" + ")";

    private static final String SQL_CREATE_APPS =
            "CREATE TABLE " + TABLE_ALL_APPS + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_APP_NAME + " TEXT," +
                    COLUMN_PACKAGE_NAME + " TEXT" + ")";

    private static final String SQL_DELETE_PROFILES =
            "DROP TABLE IF EXISTS " + TABLE_PROFILES;

    private static final String SQL_DELETE_APPS =
            "DROP TABLE IF EXISTS " + TABLE_ALL_APPS;

    public DataBase2(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PROFILES);
        db.execSQL(SQL_CREATE_APPS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_PROFILES);
        db.execSQL(SQL_DELETE_APPS);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    void createNewProfile(String profileName){
        // Creates new empty profile with name [profileName].

        // access to database
        SQLiteDatabase db = this.getWritableDatabase();

        // create new row with given name, it is selected by default, no locked apps
        ContentValues values = new ContentValues();
        values.put(DataBase2.COLUMN_PROFILE_NAME, profileName);
        values.put(DataBase2.COLUMN_PROFILE_STATUS, "true");
        values.put(DataBase2.COLUMN_LOCKED_APPS, "");

        db.insert(DataBase2.TABLE_PROFILES, null, values);

    }

    boolean hasProfile(){
        // Returns true if there is at least one profile
        // access to database
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor searchCursor = db.rawQuery("select * from " + DataBase2.TABLE_PROFILES,null);

        return searchCursor.moveToNext();
    }

    void setLocked(String profileName, String package_name){
        // Adds app with given package name to given profile
        // access to database
        SQLiteDatabase db = this.getWritableDatabase();

        // get current list of locked apps
        Cursor lockedAppCursor = db.query(
                DataBase2.TABLE_PROFILES, null,
                DataBase2.COLUMN_PROFILE_NAME + " = ?", new String[] {profileName},
                null, null, null);
        lockedAppCursor.moveToNext();

        String locked_apps = lockedAppCursor.getString(lockedAppCursor.getColumnIndex(DataBase2.COLUMN_LOCKED_APPS));

        // Change list of locked applications
        locked_apps += package_name + ",";

        // update value of locked apps
        ContentValues new_locked = new ContentValues();
        new_locked.put(DataBase2.COLUMN_LOCKED_APPS, locked_apps);

        db.update(DataBase2.TABLE_PROFILES, new_locked,
                DataBase2.COLUMN_PROFILE_NAME + " = ?", new String[] {profileName});
    }

    void unsetLocked(String profileName, String packageName){
        // access to database
        SQLiteDatabase db = this.getWritableDatabase();

        // get old list of locked apps
        Cursor lockedAppsCursor = db.query(
                DataBase2.TABLE_PROFILES, null,
                DataBase2.COLUMN_PROFILE_NAME + " = ?", new String[] {profileName},
                null, null, null);
        lockedAppsCursor.moveToNext();
        // delete given app
        String locked_apps = lockedAppsCursor.getString(lockedAppsCursor.getColumnIndex(DataBase2.COLUMN_LOCKED_APPS));
        String new_locked_apps = locked_apps.replaceAll(packageName + ",", "");

        // update value
        ContentValues value = new ContentValues();
        value.put(DataBase2.COLUMN_LOCKED_APPS, new_locked_apps);
        db.update(DataBase2.TABLE_PROFILES, value,
                DataBase2.COLUMN_PROFILE_NAME + " = ?", new String[] {profileName});
    }

    boolean isLocked(String profileName, String packageName){
        // access to database
        SQLiteDatabase db = this.getWritableDatabase();

        // get locked apps for given profile
        Cursor lockedAppsCursor = db.query(
                DataBase2.TABLE_PROFILES, null,
                DataBase2.COLUMN_PROFILE_NAME + " = ?", new String[] {profileName},
                null, null, null);
        lockedAppsCursor.moveToNext();

        String locked_apps = lockedAppsCursor.getString(lockedAppsCursor.getColumnIndex(DataBase2.COLUMN_LOCKED_APPS));

        return locked_apps.contains(packageName);

    }

    ArrayList<String> getAllLocked(String profileName){
        // access to database
        SQLiteDatabase db = this.getWritableDatabase();

        // get old list of locked apps
        Cursor lockedAppsCursor = db.query(
                DataBase2.TABLE_PROFILES, null,
                DataBase2.COLUMN_PROFILE_NAME + " = ?", new String[] {profileName},
                null, null, null);
        lockedAppsCursor.moveToNext();
        // delete given app
        String locked_apps = lockedAppsCursor.getString(lockedAppsCursor.getColumnIndex(DataBase2.COLUMN_LOCKED_APPS));
        ArrayList<String> res = new ArrayList<>();
        String[] locked_apps_arr = locked_apps.split(",");

        for (String app: locked_apps_arr){
            if (!app.equals("")){
                res.add(app);
            }
        }
        return res;
    }

    String getChosenProfile(){
        // access to database
        SQLiteDatabase db = this.getWritableDatabase();

        // Cursor that allow to iterate through all proofiles
        Cursor allProfiles = db.rawQuery("select * from " + DataBase2.TABLE_PROFILES,null);

        while (allProfiles.moveToNext()){
            String profileName = allProfiles.getString(allProfiles.getColumnIndex(DataBase2.COLUMN_PROFILE_NAME));
            String isChosen = allProfiles.getString(allProfiles.getColumnIndex(DataBase2.COLUMN_PROFILE_STATUS));
            if (isChosen.equals("true")){
                return profileName;
            }
        }
        return "ERROR";
    }

    void setChosenProfile(String profileName){
        // access to database
        SQLiteDatabase db = this.getWritableDatabase();

        // get name of currently chosen profile
        String oldProfile = getChosenProfile();

        // unset old chosen profile in database
        ContentValues unsetterChosenProfile = new ContentValues();
        unsetterChosenProfile.put(DataBase2.COLUMN_PROFILE_STATUS, "false");
        db.update(DataBase2.TABLE_PROFILES, unsetterChosenProfile,
                  DataBase2.COLUMN_PROFILE_NAME, new String[] {oldProfile});

        ContentValues setterChosenProfile = new ContentValues();
        setterChosenProfile.put(DataBase2.COLUMN_PROFILE_STATUS, "true");
        db.update(DataBase2.TABLE_PROFILES, setterChosenProfile,
                DataBase2.COLUMN_PROFILE_NAME, new String[] {profileName});
    }

    ArrayList<String> getAllApps(){
        // Returns all apps apps in ArrayList. Each element has `name,package_name` format
        // Access database
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String> res = new ArrayList<>();

        // get all installed apps
        Cursor allApps = db.rawQuery("select * from " + DataBase2.TABLE_ALL_APPS,null);

        while (allApps.moveToNext()){
            String name = allApps.getString(allApps.getColumnIndex(DataBase2.COLUMN_APP_NAME));
            String package_name = allApps.getString(allApps.getColumnIndex(DataBase2.COLUMN_PACKAGE_NAME));
            res.add(name + "," + package_name);
        }
        return res;
    }

    void addApp(String name, String package_name){
        // Access database
        SQLiteDatabase db = this.getWritableDatabase();

        //save data
        ContentValues value = new ContentValues();
        value.put(DataBase2.COLUMN_APP_NAME, name);
        value.put(DataBase2.COLUMN_PACKAGE_NAME, package_name);
        db.insert(DataBase2.TABLE_ALL_APPS, null, value);
    }

    boolean containsApp(String package_name){
        // Access database
        SQLiteDatabase db = this.getWritableDatabase();

        // get current list of locked apps
        Cursor searchCursor = db.query(
                DataBase2.TABLE_ALL_APPS, null,
                DataBase2.COLUMN_PACKAGE_NAME + " = ?", new String[] {package_name},
                null, null, null);

        return searchCursor.moveToNext();
    }
}
