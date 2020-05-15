package com.faceit.faceitapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.service.autofill.UserData;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.ArrayList;
import java.util.Arrays;

public class UsersDataBase extends SQLiteOpenHelper {
    static final String TABLE_USERS = "users";
    static final String COLUMN_IMAGES = "name";
    static final String COLUMN_LABELS = "face_data";
    static final String _ID = "_id";

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "USERS.db";

    private static final String SQL_CREATE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_IMAGES + " TEXT," +
                    COLUMN_LABELS + " TEXT" + ")";

    private static final String SQL_DELETE_USERS =
            "DROP TABLE IF EXISTS " + TABLE_USERS;


    public UsersDataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_USERS);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void putImages(ArrayList<Mat> images){
        SQLiteDatabase db = this.getWritableDatabase();
        if (images == null)
            return;
        ArrayList<String> arrayImages = new ArrayList<String>();

        for (Mat image : images) {
            int size = (int) (image.total() * image.channels());
            byte[] data = new byte[size];
            image.get(0, 0, data);
            String dataString = new String(Base64.encode(data, Base64.DEFAULT));
            arrayImages.add(dataString);
        }
        String[] myStringList = arrayImages.toArray(new String[arrayImages.size()]);
        String resultString = TextUtils.join("‚‗‚", myStringList);
        ContentValues value = new ContentValues();
        value.put(UsersDataBase.COLUMN_IMAGES, resultString);

        Cursor labelCursor = db.rawQuery("select * from " + UsersDataBase.TABLE_USERS,null);

        if (!labelCursor.moveToNext()){
            value.put(UsersDataBase.COLUMN_LABELS, "");
            db.insert(UsersDataBase.TABLE_USERS, null, value);
            Log.d("INSERTINGINIMAGES", resultString);
        }
        else{
            String oldImage =  labelCursor.getString(labelCursor.getColumnIndex(UsersDataBase.COLUMN_LABELS));
            db.update(UsersDataBase.TABLE_USERS, value,
                    UsersDataBase.COLUMN_IMAGES + " = ?", new String[] {oldImage});
            Log.d("UPDATINGIMAGES", resultString);
        }

    }

    public void putLabels(ArrayList<String> labels){
        SQLiteDatabase db = this.getWritableDatabase();
        String[] myStringList = labels.toArray(new String[labels.size()]);
        String resultString = TextUtils.join("‚‗‚", myStringList);
        ContentValues value = new ContentValues();
        value.put(UsersDataBase.COLUMN_LABELS, resultString);

        Cursor imagesCursor = db.rawQuery("select * from " + UsersDataBase.TABLE_USERS,null);

        if (!imagesCursor.moveToNext()){
            value.put(UsersDataBase.COLUMN_IMAGES, "");
            db.insert(UsersDataBase.TABLE_USERS, null, value);
            Log.d("INSERTINGINLABELS", resultString);
        }
        else{
            String oldLabel =  imagesCursor.getString(imagesCursor.getColumnIndex(UsersDataBase.COLUMN_LABELS));
            db.update(UsersDataBase.TABLE_USERS, value,
                    UsersDataBase.COLUMN_LABELS + " = ?", new String[] {oldLabel});
            Log.d("UPDATINGLABELS", resultString);
        }
    }

    public ArrayList<Mat> getImages(){
        SQLiteDatabase db = this.getWritableDatabase();

        ArrayList<Mat> result = new ArrayList<>();

        String stringLabel;
        Cursor imageCursor = db.rawQuery("select * from " + UsersDataBase.TABLE_USERS,null);
        if (!imageCursor.moveToNext())
            stringLabel = "";
        else
            stringLabel = imageCursor.getString(imageCursor.getColumnIndex(UsersDataBase.COLUMN_IMAGES));
        imageCursor.close();

        ArrayList<String> arrayImages = new ArrayList<String>(Arrays.asList(TextUtils.split(stringLabel, "‚‗‚")));
        for (String image : arrayImages) {
            byte[] data = Base64.decode(image, Base64.DEFAULT);
            Mat mat = new Mat(data.length, 1, CvType.CV_8U);
            mat.put(0, 0, data);
            result.add(mat);
        }
        return result;
    }

    public ArrayList<String> getLabels(){
        SQLiteDatabase db = this.getWritableDatabase();

        String stringLabels;
        Cursor labelCursor = db.rawQuery("select * from " + UsersDataBase.TABLE_USERS, null);

        if (!labelCursor.moveToNext())
            stringLabels = "";
        else
            stringLabels = labelCursor.getString(labelCursor.getColumnIndex(UsersDataBase.COLUMN_LABELS));
        labelCursor.close();

        return new ArrayList<String>(Arrays.asList(TextUtils.split(stringLabels, "‚‗‚")));
    }
}
