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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class is data base that contains -
 * 1) password, 2) information about profiles and blocked apps there
 */
public class DataBase extends SQLiteOpenHelper {
    // Strings that are used for creating and accessing table
    // with profiles and locked applications
    static final String TABLE_PROFILES = "profiles";
    static final String COLUMN_PROFILE_NAME = "name";
    static final String COLUMN_PROFILE_STATUS = "is_chosen";
    static final String COLUMN_LOCKED_APPS = "locked";
    static final String _ID = "_id";

    // Strings that are used for creating and accessing table with password
    static final String TABLE_PASSWORD = "password_table";
    static final String COLUMN_PASSWORD = "password";

    // String that represents table and columns
    static final String TABLE_USERS = "users";
    static final String COLUMN_IMAGES = "name";
    static final String COLUMN_LABELS = "face_data";

    // Additional information about data base
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "PROFILES.db";

    // Initializing String for table with profiles
    private static final String SQL_CREATE_PROFILES =
            "CREATE TABLE " + TABLE_PROFILES + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_PROFILE_NAME + " TEXT," +
                    COLUMN_PROFILE_STATUS + " TEXT," +
                    COLUMN_LOCKED_APPS + " TEXT" + ")";

    // Initializing String for table with password
    private static final String SQL_CREATE_PASSWORD =
            "CREATE TABLE " + TABLE_PASSWORD + " (" +
                    _ID + " TEXT," +
                    COLUMN_PASSWORD + " TEXT" + ")";

    // Initializing String for table with users
    private static final String SQL_CREATE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_IMAGES + " TEXT," +
                    COLUMN_LABELS + " TEXT" + ")";

    // String for deleting for table with profiles
    private static final String SQL_DELETE_PROFILES =
            "DROP TABLE IF EXISTS " + TABLE_PROFILES;

    // String for deleting for table with password
    private static final String SQL_DELETE_PASSWORD =
            "DROP TABLE IF EXISTS " + TABLE_PASSWORD;

    //  String for deleting for table with users
    private static final String SQL_DELETE_USERS =
            "DROP TABLE IF EXISTS " + TABLE_USERS;

    /**
     * Just default constructor
     * @param context - context of application
     */
    public DataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    /**
     * is called when data base is created. Creates three tables
     * @param db - SQLite data base. SQLiteDatabase object that represents database itself
     */
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PROFILES);
        db.execSQL(SQL_CREATE_PASSWORD);
        db.execSQL(SQL_CREATE_USERS);
    }

    /**
     * Called when db is upgraded (I never call this method)
     * @param db - SQLiteDataBase that represents database itself
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_PROFILES);
        db.execSQL(SQL_DELETE_PASSWORD);
        db.execSQL(SQL_DELETE_USERS);
        onCreate(db);
    }

    /**
     * Called when db is downgraded (I never cal this method)
     * @param db - SQLiteDataBase that represents database itself
     * @param oldVersion
     * @param newVersion
     */
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * static method for encoding String using sha256
     * @param text - String for encoding
     * @return - String encoded with sha256 algorithm
     * @throws NoSuchAlgorithmException MessageDigest class used for encoding
     * requires this exception that is thrown if you input wrong algorithm. In this application
     * it will never be thrown
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    static private String encode(String text) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        return String.format("%0" + (hash.length*2) + "X", new BigInteger(1, hash));
    }

    /**
     * saves new password in the Data Base
     * @param password - String password
     * @throws NoSuchAlgorithmException MessageDigest class used for encoding
     *      * requires this exception that is thrown if you input wrong algorithm. In this application
     *      * it will never be thrown
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    void setPassword(String password) throws NoSuchAlgorithmException {
        // access to data base
        SQLiteDatabase db = this.getWritableDatabase();

        // Create value with new password
        ContentValues value = new ContentValues();
        value.put(DataBase.COLUMN_PASSWORD, encode(password));

        // Cursor pointing at the password row
        Cursor passwordCursor = db.rawQuery("select * from " + DataBase.TABLE_PASSWORD,null);

        // if there is a password in DB, than update
        if (passwordCursor.moveToNext()){
            String oldPassword = passwordCursor.getString(passwordCursor.getColumnIndex(DataBase.COLUMN_PASSWORD));
            db.update(DataBase.TABLE_PASSWORD, value,
                    DataBase.COLUMN_PASSWORD + " = ?", new String[] {oldPassword});
        }
        // if there is no password then insert
        else {
            db.insert(DataBase.TABLE_PASSWORD, null, value);
        }

    }

    /**
     * @return true if there is password in db else false
     */
    boolean hasPassword(){
        // access to db
        SQLiteDatabase db = this.getWritableDatabase();
        // Cursor pointing at row.
        Cursor passwordCursor = db.rawQuery("select * from " + DataBase.TABLE_PASSWORD,null);
        return passwordCursor.moveToNext();
    }

    /**
     * Check whether password if correct
     * @param password
     * @return True if <password> == password in db
     * @throws NoSuchAlgorithmException MessageDigest class used for encoding
     * requires this exception that is thrown if you input wrong algorithm. In this application
     * it will never be thrown
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    boolean checkPassword(String password) throws NoSuchAlgorithmException {
        // Access to db
        SQLiteDatabase db = this.getWritableDatabase();
        // encode inputed password
        String encodedPassword = encode(password);

        // cursor pointing at password
        Cursor passwordCursor = db.rawQuery("select * from " + DataBase.TABLE_PASSWORD,null);
        passwordCursor.moveToNext();
        // get password
        String actualHashedPassword = passwordCursor.getString(passwordCursor.getColumnIndex(DataBase.COLUMN_PASSWORD));
        passwordCursor.close();
        return actualHashedPassword.equals(encodedPassword);
    }

    /**
     * Created new profile in the DB
     * @param profileName - name of profile
     * @param status - true if new profile will be chosen else false
     */
    void createNewProfile(String profileName, String status){
        // access to database
        SQLiteDatabase db = this.getWritableDatabase();

        // create new row with given name with no locked apps
        ContentValues values = new ContentValues();
        values.put(DataBase.COLUMN_PROFILE_NAME, profileName);
        values.put(DataBase.COLUMN_PROFILE_STATUS, status);
        values.put(DataBase.COLUMN_LOCKED_APPS, "");

        // insert profile
        db.insert(DataBase.TABLE_PROFILES, null, values);

    }

    /**
     * Deleted profile with given name
     * @param profileName - name of profile
     */
    void deleteProfile(String profileName){
        // access to database
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(DataBase.TABLE_PROFILES, DataBase.COLUMN_PROFILE_NAME + " = ?",
                  new String[] {profileName});
    }

    /**
     * @return true if there is at least one profile
     */
    boolean hasProfile(){
        // access to database
        SQLiteDatabase db = this.getWritableDatabase();
        // cursor pointing at all profiles
        Cursor searchCursor = db.rawQuery("select * from " + DataBase.TABLE_PROFILES,null);

        return searchCursor.moveToNext();
    }

    /**
     * Adds app with given package name to given profile
     * @param profileName - name of profile
     * @param package_name - package name of application
     */
    void setLocked(String profileName, String package_name){
        // access to database
        SQLiteDatabase db = this.getWritableDatabase();

        // get current list of locked apps
        Cursor lockedAppCursor = db.query(
                DataBase.TABLE_PROFILES, null,
                DataBase.COLUMN_PROFILE_NAME + " = ?", new String[] {profileName},
                null, null, null);
        lockedAppCursor.moveToNext();
        String locked_apps = lockedAppCursor.getString(lockedAppCursor.getColumnIndex(DataBase.COLUMN_LOCKED_APPS));

        // Change list of locked applications
        locked_apps += package_name + ",";

        // update value of locked apps in data base
        ContentValues new_locked = new ContentValues();
        new_locked.put(DataBase.COLUMN_LOCKED_APPS, locked_apps);

        db.update(DataBase.TABLE_PROFILES, new_locked,
                DataBase.COLUMN_PROFILE_NAME + " = ?", new String[] {profileName});
    }

    /**
     * Deletes app from locked list in given profile
     * @param profileName - name of profile
     * @param packageName - package name of app
     */
    void unsetLocked(String profileName, String packageName){
        // access to database
        SQLiteDatabase db = this.getWritableDatabase();

        // get old list of locked apps
        Cursor lockedAppsCursor = db.query(
                DataBase.TABLE_PROFILES, null,
                DataBase.COLUMN_PROFILE_NAME + " = ?", new String[] {profileName},
                null, null, null);
        lockedAppsCursor.moveToNext();
        String locked_apps = lockedAppsCursor.getString(lockedAppsCursor.getColumnIndex(DataBase.COLUMN_LOCKED_APPS));

        // delete app
        String new_locked_apps = locked_apps.replaceAll(packageName + ",", "");

        // update value
        ContentValues value = new ContentValues();
        value.put(DataBase.COLUMN_LOCKED_APPS, new_locked_apps);
        db.update(DataBase.TABLE_PROFILES, value,
                DataBase.COLUMN_PROFILE_NAME + " = ?", new String[] {profileName});
    }

    /**
     * Returns true if app is locked in given profile
     * @param profileName - name of profile
     * @param packageName - package name of application for checking
     * @return true/false
     */
    boolean isLocked(String profileName, String packageName){
        // access to database
        SQLiteDatabase db = this.getWritableDatabase();

        // get locked apps for given profile
        Cursor lockedAppsCursor = db.query(
                DataBase.TABLE_PROFILES, null,
                DataBase.COLUMN_PROFILE_NAME + " = ?", new String[] {profileName},
                null, null, null);
        lockedAppsCursor.moveToNext();
        String locked_apps = lockedAppsCursor.getString(lockedAppsCursor.getColumnIndex(DataBase.COLUMN_LOCKED_APPS));

        // return is this String contains given app
        return locked_apps.contains(packageName);

    }

    /**
     * Returns list of all locked apps of given profile
     * @param profileName - name of profile
     * @return ArrayList oof package names
     */
    ArrayList<String> getAllLocked(String profileName){
        // access to database
        SQLiteDatabase db = this.getWritableDatabase();

        // get list of locked apps in String
        Cursor lockedAppsCursor = db.query(
                DataBase.TABLE_PROFILES, null,
                DataBase.COLUMN_PROFILE_NAME + " = ?", new String[] {profileName},
                null, null, null);
        lockedAppsCursor.moveToNext();
        String locked_apps = lockedAppsCursor.getString(lockedAppsCursor.getColumnIndex(DataBase.COLUMN_LOCKED_APPS));
        ArrayList<String> res = new ArrayList<>();

        // convert to String[]
        String[] locked_apps_arr = locked_apps.split(",");

        // Convert to ArrayList
        for (String app: locked_apps_arr){
            if (!app.equals("")){
                res.add(app);
            }
        }
        return res;
    }

    /**
     * @return name of chosen profile
     */
    String getChosenProfile(){
        // access to database
        SQLiteDatabase db = this.getWritableDatabase();

        // Cursor that allow to iterate through all profiles
        Cursor allProfiles = db.rawQuery("select * from " + DataBase.TABLE_PROFILES,null);

        while (allProfiles.moveToNext()){
            // get name
            String profileName = allProfiles.getString(allProfiles.getColumnIndex(DataBase.COLUMN_PROFILE_NAME));
            String isChosen = allProfiles.getString(allProfiles.getColumnIndex(DataBase.COLUMN_PROFILE_STATUS));
            if (isChosen.equals("true")){
                return profileName;
            }
        }
        return "ERROR"; // nvere happens
    }

    /**
     * @return ArrayList of names of all profiles
     */
    ArrayList<String> getAllProfiles(){
        // access to database
        SQLiteDatabase db = this.getWritableDatabase();

        ArrayList<String> res = new ArrayList<>();
        // get all profiles
        Cursor allProfiles = db.rawQuery("select * from " + DataBase.TABLE_PROFILES,null);

        // add all of then to result
        while(allProfiles.moveToNext()){
            res.add(allProfiles.getString(allProfiles.getColumnIndex(DataBase.COLUMN_PROFILE_NAME)));
        }
        return res;
    }

    /**
     * Set given profile as chosen (automatically unsets old one)
     * @param profileName - name of profile
     */
    void setChosenProfile(String profileName){
        // access to database
        SQLiteDatabase db = this.getWritableDatabase();

        // get name of currently chosen profile
        String oldProfile = getChosenProfile();

        // unset old chosen profile in database
        ContentValues unsetterChosenProfile = new ContentValues();
        unsetterChosenProfile.put(DataBase.COLUMN_PROFILE_STATUS, "false");
        db.update(DataBase.TABLE_PROFILES, unsetterChosenProfile,
                  DataBase.COLUMN_PROFILE_NAME + " = ?", new String[] {oldProfile});

        // Set given profile
        ContentValues setterChosenProfile = new ContentValues();
        setterChosenProfile.put(DataBase.COLUMN_PROFILE_STATUS, "true");
        db.update(DataBase.TABLE_PROFILES, setterChosenProfile,
                DataBase.COLUMN_PROFILE_NAME + " = ?", new String[] {profileName});
    }

    /**
     * Puts Images in the database
     * @param images - array list of matrixes that represent images
     */
    public void putImages(ArrayList<Mat> images){
        // Access to DB
        SQLiteDatabase db = this.getWritableDatabase();

        // Check input on 'nullness'
        if (images == null)
            return;

        // Convert ArrayLists<Mat> to ArrayList<String>
        ArrayList<String> arrayImages = new ArrayList<String>();
        for (Mat image : images) {
            int size = (int) (image.total() * image.channels());
            byte[] data = new byte[size];
            image.get(0, 0, data);
            String dataString = new String(Base64.encode(data, Base64.DEFAULT));
            arrayImages.add(dataString);
        }

        // Convert ArrayList<String> to String[]
        String[] myStringList = arrayImages.toArray(new String[arrayImages.size()]);
        // Convert String[] to just String
        String resultString = TextUtils.join("‚‗‚", myStringList);

        // put String images converted to String to value
        ContentValues value = new ContentValues();
        value.put(DataBase.COLUMN_IMAGES, resultString);

        // Get Cursor pointing on the only row in DB
        Cursor labelCursor = db.rawQuery("select * from " + DataBase.TABLE_USERS,null);

        // if db is empty, than insert value. Default value of labels is ""
        if (!labelCursor.moveToNext()){
            value.put(DataBase.COLUMN_LABELS, "");
            db.insert(DataBase.TABLE_USERS, null, value);
        }
        // else update value in DB
        else{
            String oldImage =  labelCursor.getString(labelCursor.getColumnIndex(DataBase.COLUMN_LABELS));
            db.update(DataBase.TABLE_USERS, value,
                    DataBase.COLUMN_IMAGES + " = ?", new String[] {oldImage});
        }

    }

    /**
     * Puts labels in the database. Labels connect photos to usernames
     * @param labels - array of Strings
     */
    public void putLabels(ArrayList<String> labels){
        // Access to database
        SQLiteDatabase db = this.getWritableDatabase();

        // Convert ArrayList<String> to String[]
        String[] myStringList = labels.toArray(new String[labels.size()]);
        // Convert String[] to String
        String resultString = TextUtils.join("‚‗‚", myStringList);
        // Put converted to String labels in the value
        ContentValues value = new ContentValues();
        value.put(DataBase.COLUMN_LABELS, resultString);

        // get cursor pointing at the only row
        Cursor imagesCursor = db.rawQuery("select * from " + DataBase.TABLE_USERS,null);

        // if db is empty - insert
        if (!imagesCursor.moveToNext()){
            value.put(DataBase.COLUMN_IMAGES, "");
            db.insert(DataBase.TABLE_USERS, null, value);
        }
        // else update
        else{
            String oldLabel =  imagesCursor.getString(imagesCursor.getColumnIndex(DataBase.COLUMN_LABELS));
            db.update(DataBase.TABLE_USERS, value,
                    DataBase.COLUMN_LABELS + " = ?", new String[] {oldLabel});
        }
    }

    /**
     * Returns images stored in the database
     * @return array list of matrxes, there each matrix represents image
     */
    public ArrayList<Mat> getImages(){
        // Access to database
        SQLiteDatabase db = this.getWritableDatabase();

        ArrayList<Mat> result = new ArrayList<>();
        String stringLabel;

        // get cursor pointing at the only row
        Cursor imageCursor = db.rawQuery("select * from " + DataBase.TABLE_USERS,null);
        // if is is empty than default value is ""
        if (!imageCursor.moveToNext())
            stringLabel = "";
            // else get value from db
        else
            stringLabel = imageCursor.getString(imageCursor.getColumnIndex(DataBase.COLUMN_IMAGES));
        imageCursor.close();

        // Convert String to ArrayList <String>
        ArrayList<String> arrayImages = new ArrayList<String>(Arrays.asList(TextUtils.split(stringLabel, "‚‗‚")));

        // Convert Array:ist<String> to ArrayList<Mat>
        for (String image : arrayImages) {
            byte[] data = Base64.decode(image, Base64.DEFAULT);
            Mat mat = new Mat(data.length, 1, CvType.CV_8U);
            mat.put(0, 0, data);
            result.add(mat);
        }
        return result;
    }

    /**
     * Returns labels stored in the database
     * @return array of Strings, where each string is label
     */
    public ArrayList<String> getLabels(){
        // Access data base
        SQLiteDatabase db = this.getWritableDatabase();

        String stringLabels;

        // // get cursor pointing at the only row
        Cursor labelCursor = db.rawQuery("select * from " + DataBase.TABLE_USERS, null);
        // if empty - return default value - ""
        if (!labelCursor.moveToNext())
            stringLabels = "";
            // Else get value from db
        else
            stringLabels = labelCursor.getString(labelCursor.getColumnIndex(DataBase.COLUMN_LABELS));
        labelCursor.close();

        // return value converted to ArrayList<String>
        return new ArrayList<String>(Arrays.asList(TextUtils.split(stringLabels, "‚‗‚")));
    }
}
