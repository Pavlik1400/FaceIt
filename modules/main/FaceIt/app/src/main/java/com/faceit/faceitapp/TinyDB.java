/*
 * Copyright 2014 KC Ochibili
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 *  The "‚‗‚" character is not a comma, it is the SINGLE LOW-9 QUOTATION MARK unicode 201A
 *  and unicode 2017 that are used for separating the items in a list.
 */

// Modified version of: https://github.com/kcochibili/TinyDB--Android-Shared-Preferences-Turbo
// Added getListMat and putListMat methods

package com.faceit.faceitapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;



public class TinyDB {

    private SharedPreferences preferences;

    public TinyDB(Context appContext) {
        preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
    }
    public ArrayList<String> getListString(String key) {
        return new ArrayList<String>(Arrays.asList(TextUtils.split(preferences.getString(key, ""), "‚‗‚")));
    }


    public ArrayList<Mat> getListMat(String key){
        ArrayList<String> objStrings = getListString(key);
        ArrayList<Mat> objects =  new ArrayList<Mat>();

        for (String jObjString : objStrings) {
            byte[] data = Base64.decode(jObjString, Base64.DEFAULT);
            Mat mat = new Mat(data.length, 1, CvType.CV_8U);
            mat.put(0, 0, data);
            objects.add(mat);
        }
        return objects;
    }

    /**
     * Put ArrayList of String into SharedPreferences with 'key' and save
     * @param key SharedPreferences key
     * @param stringList ArrayList of String to be added
     */
    public void putListString(String key, ArrayList<String> stringList) {
        checkForNullKey(key);
        String[] myStringList = stringList.toArray(new String[stringList.size()]);
        preferences.edit().putString(key, TextUtils.join("‚‗‚", myStringList)).apply();
    }


    public void putListMat(String key, ArrayList<Mat> objArray){
        checkForNullKey(key);
        ArrayList<String> objStrings = new ArrayList<String>();

        for (Mat mat : objArray) {
            int size = (int) (mat.total() * mat.channels());
            byte[] data = new byte[size];
            mat.get(0, 0, data);
            String dataString = new String(Base64.encode(data, Base64.DEFAULT));
            objStrings.add(dataString);
        }
        putListString(key, objStrings);
    }

    /**
     * Remove SharedPreferences item with 'key'
     * @param key SharedPreferences key
     */
    public void remove(String key) {
        preferences.edit().remove(key).apply();
    }


    /**
     * Retrieve all values from SharedPreferences. Do not modify collection return by method
     * @return a Map representing a list of key/value pairs from SharedPreferences
     */
    public Map<String, ?> getAll() {
        return preferences.getAll();
    }

    /**
     * null keys would corrupt the shared pref file and make them unreadable this is a preventive measure
     */
    public void checkForNullKey(String key){
        if (key == null){
            throw new NullPointerException();
        }
    }
}
