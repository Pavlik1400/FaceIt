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
 * com.lauszus
 ******************************************************************************/

package com.faceit.faceitapp;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AlertDialog;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class AddUserActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = FaceRecognitionAppActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_CODE = 0;
    private ArrayList<Mat> images;
    private ArrayList<String> imagesLabels;
    private String[] uniqueLabels;
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mRgba, mGray;
    private Toast mToast;
    private boolean useEigenfaces = true;
    private float faceThreshold, distanceThreshold;
    private SharedPreferences prefs;
    private TinyDB tinydb;
    private Toolbar mToolbar;
    private NativeMethods.TrainFacesTask mTrainFacesTask;
    private int unique_photo_num_left = 2;

    /*
    * Show small messages to user
    * @param message - string message yuo want to show
    * @param duration - duration of message
    * */
    private void showToast(String message, int duration) {
        if (duration != Toast.LENGTH_SHORT && duration != Toast.LENGTH_LONG)
            throw new IllegalArgumentException();
        if (mToast != null && mToast.getView().isShown())
            mToast.cancel(); // Close the toast if it is already open
        mToast = Toast.makeText(this, message, duration);
        mToast.show();
    }

    /*
    * Ads a name label related to the last photo taken
    * @param string - name of user
    * */
    private void addLabel(String string) {
        String label = string.substring(0, 1).toUpperCase(Locale.US) + string.substring(1).trim().toLowerCase(Locale.US); // Make sure that the name is always uppercase and rest is lowercase
        imagesLabels.add(label); // Add label to list of labels
        Log.i(TAG, "Label: " + label);

        trainFaces(); // When we have finished setting the label, then retrain faces
    }

    /**
     * Train faces using stored images.
     * @return  Returns false if the task is already running.
     */
    private boolean trainFaces() {
        if (images.isEmpty())
            return true; // The array might be empty if the method is changed in the OnClickListener

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

    /*
    * Checks the callback from training algorithm and shows message result
    * */
    private NativeMethods.TrainFacesTask.Callback trainFacesTaskCallback = new NativeMethods.TrainFacesTask.Callback() {
        @Override
        public void onTrainFacesComplete(boolean result) {
            if (result)
                showToast("Photo added to database", Toast.LENGTH_SHORT);
            else
                showToast("Error", Toast.LENGTH_LONG);
        }
    };

    /*
    * Shows dialog where user can add new labels or
    * use existing to set a connection between users and photos
    * */
    private void showLabelsDialog() {
        Set<String> uniqueLabelsSet = new HashSet<>(imagesLabels); // Get all unique labels
        if (!uniqueLabelsSet.isEmpty()) { // Make sure that there are any labels
            // Inspired by: http://stackoverflow.com/questions/15762905/how-can-i-display-a-list-view-in-an-android-alert-dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(AddUserActivity.this);
            builder.setTitle("Select label:");
            builder.setPositiveButton("New face", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    showEnterLabelDialog();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    images.remove(images.size() - 1); // Remove last image
                }
            });
            builder.setCancelable(false); // Prevent the user from closing the dialog

            String[] uniqueLabels = uniqueLabelsSet.toArray(new String[uniqueLabelsSet.size()]); // Convert to String array for ArrayAdapter
            Arrays.sort(uniqueLabels); // Sort labels alphabetically
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(AddUserActivity.this, android.R.layout.simple_list_item_1, uniqueLabels) {
                @Override
                public @NonNull View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    TextView textView = (TextView) super.getView(position, convertView, parent);
                    if (getResources().getBoolean(R.bool.isTablet))
                        textView.setTextSize(20); // Make text slightly bigger on tablets compared to phones
                    else
                        textView.setTextSize(18); // Increase text size a little bit
                    return textView;
                }
            };
            ListView mListView = new ListView(AddUserActivity.this);
            mListView.setAdapter(arrayAdapter); // Set adapter, so the items actually show up
            builder.setView(mListView); // Set the ListView

            final AlertDialog dialog = builder.show(); // Show dialog and store in final variable, so it can be dismissed by the ListView

            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    dialog.dismiss();
                    addLabel(arrayAdapter.getItem(position));
                }
            });
        } else
            showEnterLabelDialog(); // If there is no existing labels, then ask the user for a new label
    }

    /*
    * Shows dialog of creating new label
    * */
    private void showEnterLabelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AddUserActivity.this);
        builder.setTitle("Please enter your name:");

        final EditText input = new EditText(AddUserActivity.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Submit", null); // Set up positive button, but do not provide a listener, so we can check the string before dismissing the dialog
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                images.remove(images.size() - 1); // Remove last image
            }
        });
        builder.setCancelable(false); // User has to input a name
        AlertDialog dialog = builder.create();

        // Source: http://stackoverflow.com/a/7636468/2175837
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button mButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String string = input.getText().toString().trim();
                        if (!string.isEmpty()) { // Make sure the input is valid
                            // If input is valid, dismiss the dialog and add the label to the array
                            dialog.dismiss();
                            addLabel(string);
                        }
                    }
                });
            }
        });

        // Show keyboard, so the user can start typing straight away
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        dialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_face_recognition_app);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar); // Sets the Toolbar to act as the ActionBar for this Activity window

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        tinydb = new TinyDB(this); // Used to store ArrayLists in the shared preferences

        // Limit constants
        faceThreshold = (float) 0.18; // Used for differentiating faces with each other
        distanceThreshold = (float) 0.07; // Used for checking face distances

        findViewById(R.id.take_picture_button).setOnClickListener(new View.OnClickListener() {
            NativeMethods.MeasureDistTask mMeasureDistTask;

            /*
            * Method that starts "Take photo" button
            * */
            @Override
            public void onClick(View v) {
                if (mMeasureDistTask != null && mMeasureDistTask.getStatus() != AsyncTask.Status.FINISHED) {
                    Log.i(TAG, "mMeasureDistTask is still running");
                    showToast("Still processing old image...", Toast.LENGTH_SHORT);
                    return;
                }
                if (mTrainFacesTask != null && mTrainFacesTask.getStatus() != AsyncTask.Status.FINISHED) {
                    Log.i(TAG, "mTrainFacesTask is still running");
                    showToast("Still training...", Toast.LENGTH_SHORT);
                    return;
                }

                Log.i(TAG, "Gray height: " + mGray.height() + " Width: " + mGray.width() + " total: " + mGray.total());
                if (mGray.total() == 0)
                    return;

                // Scale image in order to decrease computation time and make the image square,
                // so it does not crash on phones with different aspect ratios for the front
                // and back camera
                Size imageSize = new Size(200, 200);
                Imgproc.resize(mGray, mGray, imageSize);
                Log.i(TAG, "Small gray height: " + mGray.height() + " Width: " + mGray.width() + " total: " + mGray.total());

                Mat image = mGray.reshape(0, (int) mGray.total()); // Create column vector
                Log.i(TAG, "Vector height: " + image.height() + " Width: " + image.width() + " total: " + image.total());
                if (!images.isEmpty()){
                    if (image.height() != images.get(0).height()){
                        showToast("Size ERROR!!!", Toast.LENGTH_SHORT);
                        return;
                    }
                    if (image.width() != images.get(0).width()){
                        showToast("Size ERROR!!!", Toast.LENGTH_SHORT);
                        return;
                    }
                    if (image.total() != images.get(0).total()){
                        showToast("Size ERROR!!!", Toast.LENGTH_SHORT);
                        return;
                    }
                }

                images.add(image); // Add current image to the array


                // Calculate normalized Euclidean distance
                mMeasureDistTask = new NativeMethods.MeasureDistTask(useEigenfaces, measureDistTaskCallback);
                mMeasureDistTask.execute(image);

                showLabelsDialog();
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

                    if (faceDist < faceThreshold && minDist < distanceThreshold) { // 1. Near face space and near a face class
                        if (unique_photo_num_left < 1) {
                            showToast("User added: " + imagesLabels.get(minIndex), Toast.LENGTH_SHORT);
                            addLabel(imagesLabels.get(minIndex));
                            trainFaces();
                            tinydb.putListMat("images", images);
                            tinydb.putListString("imagesLabels", imagesLabels);
                            finish();
                        }else {
                            showToast("Please, more more photos from different edge", Toast.LENGTH_SHORT);
                            images.remove(images.size() - 1);
                        }
                    }
                    else if (faceDist < faceThreshold) { // 2. Near face space but not near a known face class
                        showToast("Face added", Toast.LENGTH_SHORT);
                        unique_photo_num_left = unique_photo_num_left - 1;
                    }
                    else if (minDist < distanceThreshold) { // 3. Distant from face space and near a face class
                        showToast("Face added.1", Toast.LENGTH_SHORT);
                        unique_photo_num_left = unique_photo_num_left - 1;
                    }
                    else { // 4. Distant from face space and not near a known face class.
                        showToast("Take more photos!!", Toast.LENGTH_SHORT);
                    }
                } else {
                    showToast("everything is wrong", Toast.LENGTH_SHORT);
                }
            } else {
                Log.w(TAG, "Array is null");
                if (uniqueLabels == null || uniqueLabels.length > 1)
                    showToast("Please, make some more photos", Toast.LENGTH_SHORT);
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

        // Store ArrayLists containing the images and labels
        if (images != null && imagesLabels != null) {
            tinydb.putListMat("images", images);
            tinydb.putListString("imagesLabels", imagesLabels);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Request permission if needed
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED/* || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED*/)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CODE);
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
                    images = tinydb.getListMat("images");
                    imagesLabels = tinydb.getListString("imagesLabels");

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
     * RGB and Grayscale vectors for photo processing release
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
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

