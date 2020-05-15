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

/*
 * Code sourse (by Lauszus, GNU GPL v2.0 License): https://github.com/Lauszus/FaceRecognitionApp
 * */

package com.faceit.faceitapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.opencv.core.Mat;

/**
 * This is class for using native C++ face recognition algorithms
 */
// All computations is done in an asynchronous task, so we do not skip any frames
class NativeMethods {
    private static final String TAG = FaceRecognitionAppActivity.class.getSimpleName() + "/" + NativeMethods.class.getSimpleName();

    static void loadNativeLibraries() {
        System.loadLibrary("face-lib");
    }

    /**
     * Class for training recognition algorithms in AsyncTask
     */
    static class TrainFacesTask extends AsyncTask<Void, Void, Boolean> {
        private final Mat images, classes;
        private final Callback callback;
        private Exception error;

        /**
         * Receives callback from TrainFacesTask when it is done
         */
        interface Callback {
            void onTrainFacesComplete(boolean result);
        }

        /**
         * Constructor used for Eigenfaces.
         * @param images Matrix containing all images as column vectors.
         */
        TrainFacesTask(Mat images, Callback callback) {
            this(images, null, callback);
        }

        /**
         * Constructor used for Fisherfaces.
         * @param images  Matrix containing all images as column vectors.
         * @param classes Vector containing classes for each image.
         */
        TrainFacesTask(Mat images, Mat classes, Callback callback) {
            this.images = images;
            this.classes = classes;
            this.callback = callback;
        }

        /**
        * Starts task of training from image database
        * */
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (classes == null)
                    TrainFaces(images.getNativeObjAddr(), 0); // Train Eigenfaces
                else
                    TrainFaces(images.getNativeObjAddr(), classes.getNativeObjAddr()); // Train Fisherfaces
                return true;
            } catch (Exception e) {
                error = e;
                return false;
            }
        }

        /**
        * Callback of training
        * */
        @Override
        protected void onPostExecute(Boolean result) {
            callback.onTrainFacesComplete(result);
            if (result)
                Log.i(TAG, "Done training images");
            else
                Log.e(TAG, error.getMessage());
        }
    }

    /**
    * Class for processing and recognising images in AsyncTask
    */
    static class MeasureDistTask extends AsyncTask<Mat, Void, Bundle> {
        static final String MIN_DIST_FLOAT = "minDist";
        static final String MIN_DIST_INDEX_INT = "minDistIndex";
        static final String DIST_FACE_FLOAT = "distFace";

        private final Callback callback;
        private final boolean useEigenfaces;
        private Exception error;

        /**
         * Receives callback from MeasureDistTask when it is done
         */
        interface Callback {
            void onMeasureDistComplete(Bundle bundle);
        }

        MeasureDistTask(boolean useEigenfaces, Callback callback) {
            this.useEigenfaces = useEigenfaces;
            this.callback = callback;
        }

        /**
        * Starts task of photo recognition
        * */
        @Override
        protected Bundle doInBackground(Mat... mat) {
            float[] minDist = new float[] { -1 };
            int[] minDistIndex = new int[1];
            float[] faceDist = new float[1];
            try {
                MeasureDist(mat[0].getNativeObjAddr(), minDist, minDistIndex, faceDist, useEigenfaces);
            } catch (Exception e) {
                error = e;
                return null;
            }
            Bundle bundle = new Bundle();
            bundle.putFloat(MIN_DIST_FLOAT, minDist[0]);
            bundle.putInt(MIN_DIST_INDEX_INT, minDistIndex[0]);
            bundle.putFloat(DIST_FACE_FLOAT, faceDist[0]);
            return bundle;
        }

        @Override
        protected void onPostExecute(Bundle bundle) {
            callback.onMeasureDistComplete(bundle);
            if (bundle != null)
                Log.i(TAG, "Done measuring distance");
            else
                Log.e(TAG, error.getMessage());
        }
    }

    /**
     * Train faces recognition.
     * @param addrImages    Address for matrix containing all images as column vectors.
     * @param addrClasses   Address for vector containing classes for each image.
     *                      This must be a incrementing list starting at 1.
     *                      If set to NULL, then Eigenfaces will be used.
     *                      If this is set, then Fisherfaces will be used.
     */
    private static native void TrainFaces(long addrImages, long addrClasses);

    /**
     * Measure euclidean distance between the weight of the image compared to all weights.
     * @param addrImage     Vector containing the image.
     * @param minDist       Returns a list of sorted distances to images
     * @param minDistIndex  Returns the index of the closest distance
     * @param faceDist      Retuns the distance to facespace
     * @param useEigenfaces Set to true if Eigenfaces are used. If set to false,
     *                      then Fisherfaces will be used.
     */
    private static native void MeasureDist(long addrImage, float[] minDist, int[] minDistIndex, float[] faceDist, boolean useEigenfaces);
}
