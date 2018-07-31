package com.fnspl.hiplaedu_student.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

/**
 * Created on 15-Feb-17.
 */
public class MarshmallowPermissionHelper {

    /*Permission Checking for Storage And Camera*/

    public static boolean getStorageAndCameraPermission(final Fragment fragment, final Activity activity, final int REQUEST_CODE) {


        if (Build.VERSION.SDK_INT > 22) {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.CAMERA) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.READ_EXTERNAL_STORAGE) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    if (fragment != null)
                        fragment.requestPermissions(
                                new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_CODE);

                    else activity.requestPermissions(
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE);

                } else {


                    if (fragment != null)
                        fragment.requestPermissions(
                                new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_CODE);

                    else activity.requestPermissions(
                            new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE);


                }
                return false;
            } else
                return true;
        }
        else {
            return true;
        }

    }

    public static boolean getStoragePermission(final Fragment fragment, final Activity activity, final int REQUEST_CODE) {


        if (Build.VERSION.SDK_INT > 22) {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.READ_EXTERNAL_STORAGE) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    if (fragment != null)
                        fragment.requestPermissions(
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_CODE);

                    else activity.requestPermissions(
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE);

                } else {

                    if (fragment != null)
                        fragment.requestPermissions(
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_CODE);

                    else activity.requestPermissions(
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            REQUEST_CODE);

                }
                return false;
            } else
                return true;
        }
        else {
            return true;
        }

    }

    public static boolean getAllPermission(final Fragment fragment, final Activity activity, final int REQUEST_CODE) {


        if (Build.VERSION.SDK_INT > 22) {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED||
                    ContextCompat.checkSelfPermission(activity,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED||
                    ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED||
                    ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.RECEIVE_SMS)
                    != PackageManager.PERMISSION_GRANTED||
                    ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.BLUETOOTH)
                    != PackageManager.PERMISSION_GRANTED||
                    ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.BLUETOOTH_ADMIN)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.READ_EXTERNAL_STORAGE) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)||
                        ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.ACCESS_FINE_LOCATION)||
                        ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.ACCESS_COARSE_LOCATION)||
                        ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.READ_PHONE_STATE)||
                        ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.RECEIVE_SMS)||
                        ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.BLUETOOTH)||
                        ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.BLUETOOTH_ADMIN)) {

                    if (fragment != null)
                        fragment.requestPermissions(
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.READ_PHONE_STATE,
                                        Manifest.permission.RECEIVE_SMS,
                                        Manifest.permission.BLUETOOTH,
                                        Manifest.permission.BLUETOOTH_ADMIN},
                                REQUEST_CODE);

                    else activity.requestPermissions(
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.READ_PHONE_STATE,
                                    Manifest.permission.RECEIVE_SMS,
                                    Manifest.permission.BLUETOOTH,
                                    Manifest.permission.BLUETOOTH_ADMIN},
                            REQUEST_CODE);

                } else {

                    if (fragment != null)
                        fragment.requestPermissions(
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.READ_PHONE_STATE,
                                        Manifest.permission.RECEIVE_SMS,
                                        Manifest.permission.BLUETOOTH,
                                        Manifest.permission.BLUETOOTH_ADMIN},
                                REQUEST_CODE);

                    else activity.requestPermissions(
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.READ_PHONE_STATE,
                                    Manifest.permission.RECEIVE_SMS,
                                    Manifest.permission.BLUETOOTH,
                                    Manifest.permission.BLUETOOTH_ADMIN},
                            REQUEST_CODE);

                }
                return false;
            } else
                return true;
        }
        else {
            return true;
        }

    }

    public static boolean getReadContactPermission(final Fragment fragment, final Activity activity, final int REQUEST_CODE) {


        if (Build.VERSION.SDK_INT > 22) {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.READ_CONTACTS)) {

                    if (fragment != null)
                        fragment.requestPermissions(
                                new String[]{Manifest.permission.READ_CONTACTS},
                                REQUEST_CODE);

                    else activity.requestPermissions(
                            new String[]{Manifest.permission.READ_CONTACTS},
                            REQUEST_CODE);

                } else {

                    if (fragment != null)
                        fragment.requestPermissions(
                                new String[]{Manifest.permission.READ_CONTACTS},
                                REQUEST_CODE);

                    else activity.requestPermissions(
                            new String[]{Manifest.permission.READ_CONTACTS},
                            REQUEST_CODE);

                }
                return false;
            } else
                return true;
        }
        else {
            return true;
        }

    }

    public static boolean getBluetoothPermission(final Fragment fragment, final Activity activity, final int REQUEST_CODE) {


        if (Build.VERSION.SDK_INT > 22) {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED ||ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED ||ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.BLUETOOTH)
                    != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.BLUETOOTH_ADMIN)
                    != PackageManager.PERMISSION_GRANTED  ) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.ACCESS_FINE_LOCATION)||
                        ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.ACCESS_FINE_LOCATION)||
                        ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.BLUETOOTH)||
                        ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.BLUETOOTH_ADMIN)) {

                    if (fragment != null)
                        fragment.requestPermissions(
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.BLUETOOTH,
                                        Manifest.permission.BLUETOOTH_ADMIN},
                                REQUEST_CODE);

                    else activity.requestPermissions(
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.BLUETOOTH,
                                    Manifest.permission.BLUETOOTH_ADMIN},
                            REQUEST_CODE);

                } else {


                    if (fragment != null)
                        fragment.requestPermissions(
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.BLUETOOTH,
                                        Manifest.permission.BLUETOOTH_ADMIN},
                                REQUEST_CODE);

                    else activity.requestPermissions(
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.BLUETOOTH,
                                    Manifest.permission.BLUETOOTH_ADMIN},
                            REQUEST_CODE);


                }
                return false;
            } else
                return true;
        }
        else {
            return true;
        }

    }

    public static boolean getAllNaviginePermission(final Fragment fragment, final Activity activity, final int REQUEST_CODE) {


        if (Build.VERSION.SDK_INT > 22) {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED||
                    ContextCompat.checkSelfPermission(activity,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED|| ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.BLUETOOTH)
                    != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.BLUETOOTH_ADMIN)
                    != PackageManager.PERMISSION_GRANTED ) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.READ_EXTERNAL_STORAGE) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)||
                        ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.ACCESS_FINE_LOCATION)||
                        ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.ACCESS_COARSE_LOCATION)||
                        ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.READ_PHONE_STATE)||
                ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.BLUETOOTH)||
                        ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                Manifest.permission.BLUETOOTH_ADMIN)) {

                    if (fragment != null)
                        fragment.requestPermissions(
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.READ_PHONE_STATE,
                                        Manifest.permission.BLUETOOTH,
                                        Manifest.permission.BLUETOOTH_ADMIN},
                                REQUEST_CODE);

                    else activity.requestPermissions(
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.READ_PHONE_STATE,
                                    Manifest.permission.BLUETOOTH,
                                    Manifest.permission.BLUETOOTH_ADMIN},
                            REQUEST_CODE);

                } else {

                    if (fragment != null)
                        fragment.requestPermissions(
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.READ_PHONE_STATE,
                                        Manifest.permission.BLUETOOTH,
                                        Manifest.permission.BLUETOOTH_ADMIN},
                                REQUEST_CODE);

                    else activity.requestPermissions(
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.READ_PHONE_STATE,
                                    Manifest.permission.BLUETOOTH,
                                    Manifest.permission.BLUETOOTH_ADMIN},
                            REQUEST_CODE);

                }
                return false;
            } else
                return true;
        }
        else {
            return true;
        }

    }

}
