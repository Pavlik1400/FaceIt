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

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.InputType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Locale;

public class UserLockRecognitionActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = FaceRecognitionAppActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_CODE = 0;
    private ArrayList<Mat> images;
    private ArrayList<String> imagesLabels;
    private String[] uniqueLabels;
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mRgba, mGray;
    private Toast mToast;
    private boolean useEigenfaces;
    private float faceThreshold, distanceThreshold;
    private int maximumImages;
    private SharedPreferences prefs;
    private UsersDataBase usersDB;
    private Toolbar mToolbar;
    private NativeMethods.TrainFacesTask mTrainFacesTask;
    private final int delay_photo_take = 500;
    private Handler handler = new Handler();
    private NativeMethods.MeasureDistTask mMeasureDistTask;
    private boolean flag_password_correct = false;

    private void showToast(String message, int duration) {
        if (duration != Toast.LENGTH_SHORT && duration != Toast.LENGTH_LONG)
            throw new IllegalArgumentException();
        if (mToast != null && mToast.getView().isShown())
            mToast.cancel(); // Close the toast if it is already open
        mToast = Toast.makeText(this, message, duration);
        mToast.show();
    }

    /**
     * Train faces using stored images.
     * @return  Returns false if the task is already running.
     */
    private boolean trainFaces() {
        if (images.isEmpty()) {
            showToast("Images are empty", Toast.LENGTH_SHORT);
            return true; // The array might be empty if the method is changed in the OnClickListener
        }

        if (mTrainFacesTask != null && mTrainFacesTask.getStatus() != AsyncTask.Status.FINISHED) {
            Log.i(TAG, "mTrainFacesTask is still running");
            return false;
        }

        Mat imagesMatrix = new Mat((int) images.get(0).total(), images.size(), images.get(0).type());
        for (int i = 0; i < images.size(); i++)
            images.get(i).copyTo(imagesMatrix.col(i)); // Create matrix where each image is represented as a column vector

        Log.i(TAG, "Images height: " + imagesMatrix.height() + " Width: " + imagesMatrix.width() + " total: " + imagesMatrix.total());

        // Train the face recognition algorithms in an asynchronous task, so we do not skip any frames
        Log.i(TAG, "Training Eigenfaces");


        mTrainFacesTask = new NativeMethods.TrainFacesTask(imagesMatrix, trainFacesTaskCallback);
        mTrainFacesTask.execute();

        return true;
    }

    private void showEnterPasswordDialog() {
        handler.removeCallbacksAndMessages(null);

        AlertDialog.Builder builder = new AlertDialog.Builder(UserLockRecognitionActivity.this);
        builder.setTitle("Please enter your password:");

        final EditText input = new EditText(UserLockRecognitionActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Submit", null); // Set up positive button, but do not provide a listener, so we can check the string before dismissing the dialog
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                scheduleRecognition();
            }
        });
        builder.setCancelable(false); // User has to input a password
        AlertDialog dialog = builder.create();

        // Source: http://stackoverflow.com/a/7636468/2175837
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button mButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                mButton.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onClick(View view) {
                        String password = input.getText().toString().trim();
                        if (!password.isEmpty()) { // Make sure the input is valid
                            // If input is valid, dismiss the dialog and start password in database check
                            dialog.dismiss();
                            DataBase db = new DataBase(getApplicationContext());
                            try {
                                flag_password_correct = db.checkPassword(password);
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                            if (flag_password_correct) {
                                showToast("Password correct", Toast.LENGTH_SHORT);
                                finish();
                            }
                            else {
                                scheduleRecognition();
                            }
                        }else {
                            showToast("Invalid password", Toast.LENGTH_SHORT);
                            scheduleRecognition();
                        }
                    }
                });
            }
        });

        // Show keyboard, so the user can start typing straight away
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        dialog.show();
    }

    private NativeMethods.TrainFacesTask.Callback trainFacesTaskCallback = new NativeMethods.TrainFacesTask.Callback() {
        @Override
        public void onTrainFacesComplete(boolean result) {
            if (result)
                showToast("Please, take photo to recognise", Toast.LENGTH_SHORT);
            else
                showToast("Error", Toast.LENGTH_LONG);
        }
    };

    public void recognise(){
        {

            if (mMeasureDistTask != null && mMeasureDistTask.getStatus() != AsyncTask.Status.FINISHED) {
                Log.i(TAG, "mMeasureDistTask is still running");
                //showToast("Still processing old image...", Toast.LENGTH_SHORT);
                return;
            }
            if (mTrainFacesTask != null && mTrainFacesTask.getStatus() != AsyncTask.Status.FINISHED) {
                Log.i(TAG, "mTrainFacesTask is still running");
                //showToast("Still training...", Toast.LENGTH_SHORT);
                return;
            }

            Log.i(TAG, "Gray height: " + mGray.height() + " Width: " + mGray.width() + " total: " + mGray.total());
            if (mGray.total() == 0)
                return;

            // Scale image in order to decrease computation time and make the image square,
            // so it does not crash on phones with different aspect ratios for the front
            // and back camera
            Size imageSize = new Size(700, 700);
            Imgproc.resize(mGray, mGray, imageSize);
            Log.i(TAG, "Small gray height: " + mGray.height() + " Width: " + mGray.width() + " total: " + mGray.total());

            Mat image = mGray.reshape(0, (int) mGray.total()); // Create column vector
            Log.i(TAG, "Vector height: " + image.height() + " Width: " + image.width() + " total: " + image.total());

            if (!images.isEmpty()){
                if (image.height() != images.get(0).height()){
                    //showToast("Size ERROR!!!", Toast.LENGTH_SHORT);
                    return;
                }
                if (image.width() != images.get(0).width()){
                    //showToast("Size ERROR!!!", Toast.LENGTH_SHORT);
                    return;
                }
                if (image.total() != images.get(0).total()){
                    //showToast("Size ERROR!!!", Toast.LENGTH_SHORT);
                    return;
                }
            }

            // Calculate normalized Euclidean distance
            mMeasureDistTask = new NativeMethods.MeasureDistTask(useEigenfaces, measureDistTaskCallback);
            mMeasureDistTask.execute(image);
        }
    }

    public void scheduleRecognition() {
        handler.postDelayed(new Runnable() {
            public void run() {
                recognise();  // this method will starts every DELAY miliseconds
                handler.postDelayed(this, delay_photo_take);
            }
        }, delay_photo_take);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_recognise);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar); // Sets the Toolbar to act as the ActionBar for this Activity window

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        useEigenfaces = true;

        // Set radio button based on value stored in shared preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        usersDB = new UsersDataBase(getApplicationContext());
        faceThreshold = (float) 0.05; // Get initial value
        distanceThreshold = (float) 0.05; // Get initial value
        maximumImages = 50; // Get initial value

        findViewById(R.id.take_picture_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEnterPasswordDialog();
            }
        });

        /*
        * Flip camera animation on double tap
        * */
        final GestureDetector mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // Show flip animation when the camera is flipped due to a double tap
                flipCameraAnimation();
                return true;
            }
        });

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_java_surface_view);
        mOpenCvCameraView.setCameraIndex(prefs.getInt("mCameraIndex", CameraBridgeViewBase.CAMERA_ID_FRONT));
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                scheduleRecognition();
            }
        }, 1000);   //1 second
    }

    /*
    * Callback from user in database search.
    * */
    private NativeMethods.MeasureDistTask.Callback measureDistTaskCallback = new NativeMethods.MeasureDistTask.Callback() {
        @Override
        public void onMeasureDistComplete(Bundle bundle) {
            if (bundle == null) {
                showToast("Failed to measure distance", Toast.LENGTH_LONG);
                return;
            }

            float minDist = bundle.getFloat(NativeMethods.MeasureDistTask.MIN_DIST_FLOAT);
            if (minDist != -1) {
                int minIndex = bundle.getInt(NativeMethods.MeasureDistTask.MIN_DIST_INDEX_INT);
                float faceDist = bundle.getFloat(NativeMethods.MeasureDistTask.DIST_FACE_FLOAT);
                if (imagesLabels.size() > minIndex) { // Just to be sure
                    Log.i(TAG, "dist[" + minIndex + "]: " + minDist + ", face dist: " + faceDist + ", label: " + imagesLabels.get(minIndex));

                    String minDistString = String.format(Locale.US, "%.4f", minDist);
                    String faceDistString = String.format(Locale.US, "%.4f", faceDist);

                    if (faceDist < faceThreshold && minDist < distanceThreshold) { // 1. Near face space and near a face class
                        showToast("Recognised: " + imagesLabels.get(minIndex), Toast.LENGTH_LONG);
                        if (mOpenCvCameraView != null)
                            mOpenCvCameraView.disableView();
                        handler.removeCallbacksAndMessages(null);
                        finish();
                    }
                    else if (faceDist < faceThreshold) { // 2. Near face space but not near a known face class
                        //showToast("Unknown face", Toast.LENGTH_LONG);
                    }
                    else if (minDist < distanceThreshold) { // 3. Distant from face space and near a face class
                        //showToast("False recognition", Toast.LENGTH_LONG);
                        //images.remove(images.size() - 1); // Remove last image
                    }
                    else { // 4. Distant from face space and not near a known face class.
                        //showToast("Image is not a face", Toast.LENGTH_LONG);
                        //images.remove(images.size() - 1); // Remove last image
                    }
                } else {
                    //showToast("everything is wrong", Toast.LENGTH_LONG);
                }
            } else {
                Log.w(TAG, "Array is null");
                if (uniqueLabels == null || uniqueLabels.length > 1) {
                    showToast("No users found in database, please, add one", Toast.LENGTH_SHORT);
                    startActivity(new Intent(UserLockRecognitionActivity.this, FaceRecognitionAppActivity.class));
                }
            }
        }
    };

    /*
     * Permission checker
     * */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadOpenCV();
                } else {
                    showToast("Permission required!", Toast.LENGTH_LONG);
                    finish();
                }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Request permission if needed
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED/* || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED*/)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA/*, Manifest.permission.WRITE_EXTERNAL_STORAGE*/}, PERMISSIONS_REQUEST_CODE);
        else
            loadOpenCV();
    }

    /*
     * Loads Native (C++) libs and database of images and username labels
     * */
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    NativeMethods.loadNativeLibraries(); // Load native libraries after(!) OpenCV initialization
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();

                    // Read images and labels from shared preferences
                    images = usersDB.getImages();
                    imagesLabels = usersDB.getLabels();

                    Log.i(TAG, "Number of images: " + images.size()  + ". Number of labels: " + imagesLabels.size());
                    if (!images.isEmpty()) {
                        trainFaces(); // Train images after they are loaded
                        Log.i(TAG, "Images height: " + images.get(0).height() + " Width: " + images.get(0).width() + " total: " + images.get(0).total());
                    }
                    Log.i(TAG, "Labels: " + imagesLabels);

                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    /*
     * Loads OpenCV
     * */
    private void loadOpenCV() {
        if (!OpenCVLoader.initDebug(true)) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    /*
     * RGB and Grayscale vectors for photo processing creation
     * */
    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    /*
     * RGB and Grayscale vectors for photo processing creation
     * */
    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    /*
     * Takes and preprocess photo
     * */
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat mGrayTmp = inputFrame.gray();
        Mat mRgbaTmp = inputFrame.rgba();

        // Flip image to get mirror effect
        int orientation = mOpenCvCameraView.getScreenOrientation();
        if (mOpenCvCameraView.isEmulator()) // Treat emulators as a special case
            Core.flip(mRgbaTmp, mRgbaTmp, 1); // Flip along y-axis
        else {
            switch (orientation) { // RGB image
                case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
                    if (mOpenCvCameraView.mCameraIndex == CameraBridgeViewBase.CAMERA_ID_FRONT)
                        Core.flip(mRgbaTmp, mRgbaTmp, 0); // Flip along x-axis
                    else
                        Core.flip(mRgbaTmp, mRgbaTmp, -1); // Flip along both axis
                    break;
                case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
                    if (mOpenCvCameraView.mCameraIndex == CameraBridgeViewBase.CAMERA_ID_FRONT)
                        Core.flip(mRgbaTmp, mRgbaTmp, 1); // Flip along y-axis
                    break;
            }
            switch (orientation) { // Grayscale image
                case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
                    Core.transpose(mGrayTmp, mGrayTmp); // Rotate image
                    if (mOpenCvCameraView.mCameraIndex == CameraBridgeViewBase.CAMERA_ID_FRONT)
                        Core.flip(mGrayTmp, mGrayTmp, -1); // Flip along both axis
                    else
                        Core.flip(mGrayTmp, mGrayTmp, 1); // Flip along y-axis
                    break;
                case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
                    Core.transpose(mGrayTmp, mGrayTmp); // Rotate image
                    if (mOpenCvCameraView.mCameraIndex == CameraBridgeViewBase.CAMERA_ID_BACK)
                        Core.flip(mGrayTmp, mGrayTmp, 0); // Flip along x-axis
                    break;
                case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
                    if (mOpenCvCameraView.mCameraIndex == CameraBridgeViewBase.CAMERA_ID_FRONT)
                        Core.flip(mGrayTmp, mGrayTmp, 1); // Flip along y-axis
                    break;
                case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
                    Core.flip(mGrayTmp, mGrayTmp, 0); // Flip along x-axis
                    if (mOpenCvCameraView.mCameraIndex == CameraBridgeViewBase.CAMERA_ID_BACK)
                        Core.flip(mGrayTmp, mGrayTmp, 1); // Flip along y-axis
                    break;
            }
        }

        mGray = mGrayTmp;
        mRgba = mRgbaTmp;

        return mRgba;
    }

    @Override
    public void onBackPressed() {

    }

    /*
     * Menu for camera icon
     * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_face_recognition_app, menu);
        // Show rear camera icon if front camera is currently used and front camera icon if back camera is used
        MenuItem menuItem = menu.findItem(R.id.flip_camera);
        if (mOpenCvCameraView.mCameraIndex == CameraBridgeViewBase.CAMERA_ID_FRONT)
            menuItem.setIcon(R.drawable.ic_camera_front_white_24dp);
        else
            menuItem.setIcon(R.drawable.ic_camera_rear_white_24dp);
        return true;
    }

    /*
     * Camera flip animation
     * */
    private void flipCameraAnimation() {
        // Flip the camera
        mOpenCvCameraView.flipCamera();

        // Do flip camera animation
        View v = mToolbar.findViewById(R.id.flip_camera);
        ObjectAnimator animator = ObjectAnimator.ofFloat(v, "rotationY", v.getRotationY() + 180.0f);
        animator.setDuration(500);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                supportInvalidateOptionsMenu(); // This will call onCreateOptionsMenu()
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    /*
     * Camera flip by pressing camera icon
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.flip_camera:
                flipCameraAnimation();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
