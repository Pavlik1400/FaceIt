/*
This class is DataBase of users, that stores users and
data about their face. UserDataBase extends from SQLiteOpenHelper
 */

package com.faceit.faceitapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Arrays;

public class UsersDataBase extends SQLiteOpenHelper {
    // String that represents table and columns
    static final String TABLE_USERS = "users";
    static final String COLUMN_IMAGES = "name";
    static final String COLUMN_LABELS = "face_data";
    static final String _ID = "_id";

    // Additional information about DataBase
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "USERS.db";

    // String that is executed when DataBase is created.
    // This command in SQLite syntax Creates table and columns
    private static final String SQL_CREATE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_IMAGES + " TEXT," +
                    COLUMN_LABELS + " TEXT" + ")";

    // This String is executed when DataBase is deleted
    private static final String SQL_DELETE_USERS =
            "DROP TABLE IF EXISTS " + TABLE_USERS;

    /**
     * Constructor of UserDataBase.
     * @param context - context of application
     */
    public UsersDataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * is called when data base is created
     * @param db - SQLite data base. SQLiteDatabase object that represents database itself
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USERS);
    }

    /**
     * Called when db is upgraded (I never call this methos)
     * @param db - SQLiteDataBase that represents database itself
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
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
        value.put(UsersDataBase.COLUMN_IMAGES, resultString);

        // Get Cursor pointing on the only row in DB
        Cursor labelCursor = db.rawQuery("select * from " + UsersDataBase.TABLE_USERS,null);

        // if db is empty, than insert value. Default value of labels is ""
        if (!labelCursor.moveToNext()){
            value.put(UsersDataBase.COLUMN_LABELS, "");
            db.insert(UsersDataBase.TABLE_USERS, null, value);
        }
        // else update value in DB
        else{
            String oldImage =  labelCursor.getString(labelCursor.getColumnIndex(UsersDataBase.COLUMN_LABELS));
            db.update(UsersDataBase.TABLE_USERS, value,
                    UsersDataBase.COLUMN_IMAGES + " = ?", new String[] {oldImage});
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
        value.put(UsersDataBase.COLUMN_LABELS, resultString);

        // get cursor pointing at the only row
        Cursor imagesCursor = db.rawQuery("select * from " + UsersDataBase.TABLE_USERS,null);

        // if db is empty - insert
        if (!imagesCursor.moveToNext()){
            value.put(UsersDataBase.COLUMN_IMAGES, "");
            db.insert(UsersDataBase.TABLE_USERS, null, value);
        }
        // else update
        else{
            String oldLabel =  imagesCursor.getString(imagesCursor.getColumnIndex(UsersDataBase.COLUMN_LABELS));
            db.update(UsersDataBase.TABLE_USERS, value,
                    UsersDataBase.COLUMN_LABELS + " = ?", new String[] {oldLabel});
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
        Cursor imageCursor = db.rawQuery("select * from " + UsersDataBase.TABLE_USERS,null);
        // if is is empty than default value is ""
        if (!imageCursor.moveToNext())
            stringLabel = "";
        // else get value from db
        else
            stringLabel = imageCursor.getString(imageCursor.getColumnIndex(UsersDataBase.COLUMN_IMAGES));
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
        Cursor labelCursor = db.rawQuery("select * from " + UsersDataBase.TABLE_USERS, null);
        // if empty - return default value - ""
        if (!labelCursor.moveToNext())
            stringLabels = "";
        // Else get value from db
        else
            stringLabels = labelCursor.getString(labelCursor.getColumnIndex(UsersDataBase.COLUMN_LABELS));
        labelCursor.close();

        // return value converted to ArrayList<String>
        return new ArrayList<String>(Arrays.asList(TextUtils.split(stringLabels, "‚‗‚")));
    }
}
