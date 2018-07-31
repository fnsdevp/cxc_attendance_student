package com.fnspl.hiplaedu_student.fragment;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fnspl.hiplaedu_student.Networking.NetworkUtility;
import com.fnspl.hiplaedu_student.R;
import com.fnspl.hiplaedu_student.activity.DashboardActivity;
import com.fnspl.hiplaedu_student.activity.LoginActivity;
import com.fnspl.hiplaedu_student.activity.NotificationHandleActivity;
import com.fnspl.hiplaedu_student.application.MainApplication;
import com.fnspl.hiplaedu_student.database.Db_helper;
import com.fnspl.hiplaedu_student.databinding.FragmentFacialRecognitionBinding;
import com.fnspl.hiplaedu_student.model.ProfileInfo;
import com.fnspl.hiplaedu_student.model.RoutinePeriod;
import com.fnspl.hiplaedu_student.model.ZoneInfo;
import com.fnspl.hiplaedu_student.utils.CONST;
import com.fnspl.hiplaedu_student.utils.MarshmallowPermissionHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.VerifyResult;
import com.navigine.naviginesdk.DeviceInfo;
import com.navigine.naviginesdk.NavigationThread;
import com.navigine.naviginesdk.NavigineSDK;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import io.paperdb.Paper;

public class FacialRecognitionFragment extends Fragment {

    private FragmentFacialRecognitionBinding binding_facialRecognition;
    private View mView;
    public static final int REQUEST_CAMERA_STORAGE_PERMISSION = 0;
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    private static int UPDATE_TIME = 2500;
    private Uri imageUri;
    private String mCurrentPhotoPath;
    private SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
    private static final int CROP_PIC_REQUEST_CODE = 3;
    private File file;
    private boolean isImageDetected = false;
    private ProgressDialog pDialog;
    private ZoneDetectionBroadcastReceiver mReceiver;
    private static boolean isInClass = false;
    private TimerTask mTimerTask = null;
    private Timer mTimer = new Timer();
    private Handler mHandler = new Handler();
    private ProfileInfo profileInfo;
    private boolean firstAttemptFailed = false;

    private long mErrorMessageTime = 0;
    private static final int ERROR_MESSAGE_TIMEOUT = 5000; // milliseconds
    private ArrayList<ZoneInfo> zoneInfos = new ArrayList<>();
    private ArrayList<PointF[]> zoneInfoPoint = new ArrayList<>();

    private Runnable mRunnable =
            new Runnable() {
                public void run() {
                    try {
                        //UPDATE_TIME = 5000;
                        if (binding_facialRecognition != null && binding_facialRecognition.btnSubmit != null
                                && binding_facialRecognition.btnTakeImage != null) {

                            if (MainApplication.Navigation != null) {

                                final long timeNow = NavigineSDK.currentTimeMillis();

                                if (mErrorMessageTime > 0 && timeNow > mErrorMessageTime + ERROR_MESSAGE_TIMEOUT) {
                                    mErrorMessageTime = 0;
                                }

                                if (MainApplication.Navigation.getMode() == NavigationThread.MODE_IDLE)
                                    MainApplication.Navigation.setMode(NavigationThread.MODE_NORMAL);

                                DeviceInfo mDeviceInfo = MainApplication.Navigation.getDeviceInfo();

                                if (mDeviceInfo != null && routinePeriod != null && calculateZone(mDeviceInfo)) {
                                    isInClass = true;
                                    binding_facialRecognition.btnSubmit.setBackground(getResources().getDrawable(R.drawable.green_button));
                                    binding_facialRecognition.btnTakeImage.setBackground(getResources().getDrawable(R.drawable.green_button));
                                } else {
                                    isInClass = false;
                                    binding_facialRecognition.btnSubmit.setBackground(getResources().getDrawable(R.drawable.gray_button));
                                    binding_facialRecognition.btnSubmit.setBackground(getResources().getDrawable(R.drawable.gray_button));
                                }

                            }
                        }
                    } catch (Exception e) {

                    }
                }
            };
    private RoutinePeriod routinePeriod;

    public FacialRecognitionFragment() {
        // Required empty public constructor
    }

    private class VerificationTask extends AsyncTask<Void, String, VerifyResult> {

        VerificationTask(UUID faceId0, UUID faceId1) {
            mFaceId0 = MainApplication.faceId;
            mFaceId1 = faceId1;
        }

        @Override
        protected VerifyResult doInBackground(Void... params) {
            // Get an instance of face service client to detect faces in image.
            FaceServiceClient faceServiceClient = MainApplication.getFaceServiceClient();
            try {
                publishProgress("Verifying...");

                // Start verification.
                return faceServiceClient.verify(
                        mFaceId0,      /* The first face ID to verify */
                        mFaceId1);     /* The second face ID to verify */
            } catch (Exception e) {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();

                publishProgress(e.getMessage());
                Log.e("Tester", e.getMessage().toString());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
            Log.e("Tester", "Request: Verifying face " + mFaceId0 + " and face " + mFaceId1);
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            //progressDialog.setMessage(progress[0]);
            progressDialog.setMessage(getResources().getString(R.string.dialog_msg));
            //setInfo(progress[0]);
        }

        @Override
        protected void onPostExecute(VerifyResult result) {
            if (progressDialog.isShowing())
                progressDialog.dismiss();

            if (result != null) {
                Log.e("Tester", "Response: Success. Face " + mFaceId0 + " and face "
                        + mFaceId1 + (result.isIdentical ? " " : " don't ")
                        + "belong to the same person");

                // Show the result on screen when verification is done.
                if (result.isIdentical) {
                    doMarkPresent();
                } else {
                    if (!firstAttemptFailed) {
                        firstAttemptFailed = true;
                        new DownloadImage().execute(NetworkUtility.IMAGE_BASEURL + "" + profileInfo.getPhoto());
                    } else {
                        if (getActivity() != null)
                            ((NotificationHandleActivity) getActivity()).setFragment(new ManualAttendanceFragment(), NotificationHandleActivity.MANUAL_ATTENDANCE);
                    }
                }
            } else {
                if (!firstAttemptFailed) {
                    firstAttemptFailed = true;
                    new DownloadImage().execute(NetworkUtility.IMAGE_BASEURL + "" + profileInfo.getPhoto());
                } else if (getActivity() != null) {
                    ((NotificationHandleActivity) getActivity()).setFragment(new ManualAttendanceFragment(), NotificationHandleActivity.MANUAL_ATTENDANCE);
                    Toast.makeText(getActivity(), getResources().getString(R.string.face_not_matched), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Background task of face detection.
    private class DetectionTask extends AsyncTask<InputStream, String, Face[]> {
        private boolean mSucceed = true;

        DetectionTask() {

        }

        @Override
        protected Face[] doInBackground(InputStream... params) {
            // Get an instance of face service client to detect faces in image.
            FaceServiceClient faceServiceClient = MainApplication.getFaceServiceClient();
            try {
                publishProgress("Detecting...");

                // Start detection.
                return faceServiceClient.detect(
                        params[0],  /* Input stream of image to detect */
                        true,       /* Whether to return face ID */
                        false,       /* Whether to return face landmarks */
                        /* Which face attributes to analyze, currently we support:
                           age,gender,headPose,smile,facialHair */
                        null);
            } catch (Exception e) {

                if (progressDialog.isShowing())
                    progressDialog.dismiss();

                mSucceed = false;
                publishProgress(e.getMessage());
                Log.e("Tester", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
            Log.e("Tester", "Request: Detecting in image" + 1);
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            //progressDialog.setMessage(progress[0]);
            progressDialog.setMessage(getResources().getString(R.string.dialog_msg));
            //setInfo(progress[0]);
        }

        @Override
        protected void onPostExecute(Face[] result) {
            // Show the result on screen when detection is done.
            // setUiAfterDetection(result, mSucceed);
            if (progressDialog.isShowing())
                progressDialog.dismiss();

            if (mSucceed && result.length > 0) {
                mFaceId1 = result[0].faceId;
                if (MainApplication.faceId != null && mFaceId1 != null) {
                    isImageDetected = true;
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), getResources().getString(R.string.face_detected), Toast.LENGTH_SHORT).show();
                } else {
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), getResources().getString(R.string.try_again), Toast.LENGTH_SHORT).show();
                }
            } else {
                if (getActivity() != null) {
                    ((NotificationHandleActivity) getActivity()).setFragment(new ManualAttendanceFragment(), NotificationHandleActivity.MANUAL_ATTENDANCE);
                    Toast.makeText(getActivity(), getResources().getString(R.string.face_not_detected), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private class GetNewFaceId extends AsyncTask<InputStream, String, Face[]> {
        private boolean mSucceed = true;

        GetNewFaceId() {

        }

        @Override
        protected Face[] doInBackground(InputStream... params) {
            // Get an instance of face service client to detect faces in image.
            FaceServiceClient faceServiceClient = MainApplication.getFaceServiceClient();
            try {
                publishProgress("Detecting...");

                // Start detection.
                return faceServiceClient.detect(
                        params[0],  /* Input stream of image to detect */
                        true,       /* Whether to return face ID */
                        false,       /* Whether to return face landmarks */
                        /* Which face attributes to analyze, currently we support:
                           age,gender,headPose,smile,facialHair */
                        null);
            } catch (Exception e) {
                if (progressDialog.isShowing())
                    progressDialog.dismiss();

                mSucceed = false;
                publishProgress(e.getMessage());
                Log.e("Tester", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            Log.e("Tester", "Request: Detecting in image" + 1);
            if (!progressDialog.isShowing())
                progressDialog.show();
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            //setInfo(progress[0]);
            progressDialog.setMessage(getResources().getString(R.string.dialog_msg));
        }

        @Override
        protected void onPostExecute(Face[] result) {
            // Show the result on screen when detection is done.
            // setUiAfterDetection(result, mSucceed);
            if (progressDialog.isShowing())
                progressDialog.dismiss();

            Paper.book().delete(CONST.UPDATE_PROFILE);

            if (mSucceed && result.length > 0) {
                UUID mFaceId = result[0].faceId;

                if (mFaceId != null) {
                    MainApplication.faceId = mFaceId;
                    new VerificationTask(MainApplication.faceId, mFaceId1).execute();
                } else {
                    if (Paper.book().read(CONST.PROFILE_INFO) != null) {
                        ProfileInfo profileInfo = Paper.book().read(CONST.PROFILE_INFO);
                        MainApplication.faceId = UUID.fromString(String.format("%s", profileInfo.getPhoto_id()));
                    }
                }
            } else {
                if (Paper.book().read(CONST.PROFILE_INFO) != null) {
                    ProfileInfo profileInfo = Paper.book().read(CONST.PROFILE_INFO);
                    MainApplication.faceId = UUID.fromString(String.format("%s", profileInfo.getPhoto_id()));

                    if (getActivity() != null) {
                        ((NotificationHandleActivity) getActivity()).setFragment(new ManualAttendanceFragment(), NotificationHandleActivity.MANUAL_ATTENDANCE);
                        Toast.makeText(getActivity(), getResources().getString(R.string.face_not_detected), Toast.LENGTH_SHORT).show();
                    }
                }else{
                    if (getActivity() != null) {
                        ((NotificationHandleActivity) getActivity()).setFragment(new ManualAttendanceFragment(), NotificationHandleActivity.MANUAL_ATTENDANCE);
                        Toast.makeText(getActivity(), getResources().getString(R.string.face_not_detected), Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }
    }

    // The IDs of the two faces to be verified.
    private UUID mFaceId0;
    private UUID mFaceId1;

    private Bitmap mBitmap1;


    // Progress dialog popped up when communicating with server.
    private ProgressDialog progressDialog;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(getResources().getString(R.string.progress_dialog_title));
        progressDialog.setCancelable(false);

        IntentFilter intentFilter = new IntentFilter(
                "android.intent.action.SUCCESSLOCATION");

        mReceiver = new ZoneDetectionBroadcastReceiver();
        getActivity().registerReceiver(mReceiver, intentFilter);

        binding_facialRecognition = DataBindingUtil.inflate(inflater, R.layout.fragment_facial_recognition, container, false);
        binding_facialRecognition.setFacialRecognition(FacialRecognitionFragment.this);
        mView = binding_facialRecognition.getRoot();
        init(mView);
        return mView;
    }

    private void init(View mView) {

        //String str = "fff16ff9-9652-42db-af5d-f63b482f0470";
        if (Paper.book().read(CONST.PROFILE_INFO) != null) {
            profileInfo = Paper.book().read(CONST.PROFILE_INFO);

            MainApplication.faceId = UUID.fromString(profileInfo.getPhoto_id());

        } else {
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
            }
        }

        Db_helper db_helper = new Db_helper(getActivity());
        if (db_helper != null) {
            //zoneInfos = db_helper.getAllZoneInfo();
            routinePeriod = db_helper.getRoutine(Paper.book().read(CONST.CURRENT_PERIOD, 0));
            if (routinePeriod != null) {
                ZoneInfo zoneInfo1 = db_helper.getZoneInfo("" + routinePeriod.getRoom_id());
                zoneInfos.add(zoneInfo1);
            } else {
                zoneInfos = db_helper.getAllZoneInfo();
            }

            for (ZoneInfo zoneInfo :
                    zoneInfos) {
                zoneInfoPoint.add(convertToPoints(zoneInfo));
            }
        }

        binding_facialRecognition.btnTakeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    if (isInClass) {
                        checkCameraPermission();
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.be_in_class), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex) {

                }
            }
        });

        binding_facialRecognition.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (isImageDetected && isInClass) {
                        new VerificationTask(MainApplication.faceId, mFaceId1).execute();
                    } else if (!isImageDetected) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.take_selfi), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.be_in_class), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex) {

                }
            }
        });

        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                mHandler.post(mRunnable);
            }
        };
        mTimer.schedule(mTimerTask, UPDATE_TIME, 1000);

    }

    public void checkCameraPermission() {
        if (Build.VERSION.SDK_INT > 22) {
            if (MarshmallowPermissionHelper.getStorageAndCameraPermission(FacialRecognitionFragment.this
                    , getActivity(), REQUEST_CAMERA_STORAGE_PERMISSION)) {
                takePhoto();
            }
        } else {
            takePhoto();
        }
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageUri = Uri.fromFile(getOutputMediaFile());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CameraDemo");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");
    }

    // Callback with the request from calling requestPermissions(...)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        switch (requestCode) {
            case REQUEST_CAMERA_STORAGE_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto();
                }
                return;
            }

            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == getActivity().RESULT_OK) {

            switch (requestCode) {

                case REQUEST_IMAGE_CAPTURE:

                    try {
                        //String path = file.getAbsolutePath();
                        //doCrop(imageUri);
                        file = new File(imageUri.getPath());

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        mBitmap1 = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

                        ImageLoader.getInstance().displayImage("file://" + file.getAbsolutePath(),
                                binding_facialRecognition.ivProfilePic, CONST.ErrorWithLoaderNormalCorner);

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        mBitmap1.compress(Bitmap.CompressFormat.JPEG, 0, stream);
                        byte[] imageInByte = stream.toByteArray();

                        detect(mBitmap1);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    try {
                        CropImage.ActivityResult result = CropImage.getActivityResult(data);
                        if (resultCode == getActivity().RESULT_OK) {
                            Uri resultUri = result.getUri();
                            file = new File(resultUri.getPath());
                            ImageLoader.getInstance().displayImage("file://" + file.getAbsolutePath(),
                                    binding_facialRecognition.ivProfilePic, CONST.ErrorWithLoaderNormalCorner);
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

                        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                            Exception error = result.getError();
                            Log.d("Test", "" + error.toString());
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    private void doCrop(Uri picUri) {
        CropImage.activity(picUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(getActivity());
    }

    // Start detecting in image specified by index.
    private void detect(Bitmap bitmap) {
        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        // Start a background task to detect faces in the image.
        new DetectionTask().execute(inputStream);

    }

    private void detectNew(Bitmap bitmap) {
        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        // Start a background task to detect faces in the image.
        new GetNewFaceId().execute(inputStream);

    }

    private void doMarkPresent() {

        if (Paper.book().read(CONST.PROFILE_INFO) != null) {
            ProfileInfo profileInfo = Paper.book().read(CONST.PROFILE_INFO);
            int currentPeriod = Paper.book().read(CONST.CURRENT_PERIOD, 0);

            Db_helper db_helper = new Db_helper(getActivity());
            RoutinePeriod routinePeriod = db_helper.getRoutine(currentPeriod);

            String urlParameters = "routine_history_id=" + Paper.book().read(CONST.CURRENT_PERIOD, 0) +
                    "&teacher_id=" + routinePeriod.getTeacher_details().getId() +
                    "&student_id=" + profileInfo.getId() +
                    "&in_time=" + new SimpleDateFormat("hh:mm a").format(new Date()) +
                    "&present=Y" +
                    "&attendance_type=auto";

            new APIRequest().execute(urlParameters);
        }
    }

    private class APIRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            HttpURLConnection urlConnection = null;
            try {
                Log.d("Tester", "Before request");
                URL url = new URL(NetworkUtility.BASEURL + NetworkUtility.ATTENDENCE_REQUEST);
                String urlParameters = params[0];

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
                connection.setDoOutput(true);

                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.writeBytes(urlParameters);
                dStream.flush();
                dStream.close();
                int responseCode = connection.getResponseCode();

                final StringBuilder output = new StringBuilder("Request URL " + url);
                output.append(System.getProperty("line.separator") + "Request Parameters " + urlParameters);
                output.append(System.getProperty("line.separator") + "Response Code " + responseCode);
                output.append(System.getProperty("line.separator") + "Type " + "POST");
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                StringBuilder responseOutput = new StringBuilder();

                while ((line = br.readLine()) != null) {
                    responseOutput.append(line);
                }
                br.close();

                result = responseOutput.toString();

            } catch (Exception e1) {
                e1.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                return result;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage(getString(R.string.dialog_msg));
            pDialog.setCancelable(false);
            pDialog.show();
            //macAddress = "d0:37:42:d4:b6:f1";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }

            try {
                JSONObject cmxResponse = new JSONObject(s);

                if (cmxResponse.getString("status").equalsIgnoreCase("success")) {

                    if (getActivity() != null)
                        Toast.makeText(getActivity(), cmxResponse.optString("message"), Toast.LENGTH_SHORT).show();

                    if (mReceiver != null && getActivity() != null) {
                        getActivity().unregisterReceiver(mReceiver);
                        mReceiver = null;
                    }

                    Paper.book().delete(CONST.TIME_LEFT);
                    Paper.book().write(CONST.ATTENDANCE_CONFIRMED, true);
                    Paper.book().delete(CONST.CLASS_STARTED);
                    Paper.book().delete(CONST.ATTENDANCE_STARTED);
                    Paper.book().delete(CONST.MANUAL_ATTENDANCE_STARTED);
                    Paper.book().delete(CONST.FACIAL_ATTENDANCE_STARTED);

                    ((NotificationHandleActivity) getActivity()).setFragment(new AttendanceSuccessFragment(), NotificationHandleActivity.ATTENDANCE_SUCCESS);

                } else {
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), cmxResponse.optString("message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                //Toast.makeText(getActivity(), "JSON Exception", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        if (!Paper.book().read(CONST.FACIAL_ATTENDANCE_STARTED, false)) {

            Paper.book().delete(CONST.FACIAL_ATTENDANCE_STARTED);
            Paper.book().delete(CONST.TIME_LEFT);

            if (Paper.book().read(CONST.CLASS_STARTED, false)) {
                Paper.book().delete(CONST.ATTENDANCE_STARTED);
                Paper.book().delete(CONST.TIME_LEFT);

                ((NotificationHandleActivity) getActivity()).setFragment(new ClassStartFragment(),
                        NotificationHandleActivity.CLASS_START);
            } else if (Paper.book().read(CONST.ATTENDANCE_STARTED, false)) {
                Paper.book().delete(CONST.CLASS_STARTED);
                Paper.book().delete(CONST.TIME_LEFT);

                ((NotificationHandleActivity) getActivity()).setFragment(new AttendanceStartFragment(),
                        NotificationHandleActivity.ATTENDANCE_START);
            } else {

                if (getActivity() != null) {
                    getActivity().startActivity(new Intent(getActivity(), DashboardActivity.class));
                    getActivity().finish();
                }
            }

        }

    }

    public class ZoneDetectionBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            /*isInClass = intent.getBooleanExtra(CONST.IS_IN_CLASS, false);
            String accuracy = intent.getStringExtra(CONST.ACCURACY);

            binding_facialRecognition.tvAcuracy.setText(isInClass + " " + accuracy);*/

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        //Paper.book().delete(CONST.TIME_LEFT);
        if (mReceiver != null && getActivity() != null) {
            getActivity().unregisterReceiver(mReceiver);
        }

        if (mTimer != null) {
            mTimer.cancel();
            mTimerTask.cancel();
        }

    }

    private boolean calculateZone(DeviceInfo mDeviceInfo) {

        for (int index = 0; index < zoneInfoPoint.size(); index++) {
            boolean inZone = contains(zoneInfoPoint.get(index), new PointF(mDeviceInfo.x, mDeviceInfo.y));
            if (inZone && zoneInfos.get(index).getId() == routinePeriod.getRoom_id()) {
                //currentZone = zoneInfos.get(index).getId();

                return true;
            } else if (inZone && zoneInfos.get(index).getId() != routinePeriod.getRoom_id()) {
                //currentZone = zoneInfos.get(index).getId();

                //return false;
            } else {
                //currentZone = zoneInfos.get(index).getId();

            }
        }

        return false;
    }

    private PointF[] convertToPoints(ZoneInfo zoneInfo) {
        PointF[] pointFs = new PointF[4];

        String[] pointsA = zoneInfo.getPointA().split(",");
        PointF pointA = new PointF(Float.parseFloat(pointsA[0]), Float.parseFloat(pointsA[1]));
        pointFs[0] = pointA;

        String[] pointsB = zoneInfo.getPointB().split(",");
        PointF pointB = new PointF(Float.parseFloat(pointsB[0]), Float.parseFloat(pointsB[1]));
        pointFs[1] = pointB;

        String[] pointsC = zoneInfo.getPointC().split(",");
        PointF pointC = new PointF(Float.parseFloat(pointsC[0]), Float.parseFloat(pointsC[1]));
        pointFs[2] = pointC;

        String[] pointsD = zoneInfo.getPointD().split(",");
        PointF pointD = new PointF(Float.parseFloat(pointsD[0]), Float.parseFloat(pointsD[1]));
        pointFs[3] = pointD;

        return pointFs;
    }

    public boolean contains(PointF[] points, PointF test) {
        int i;
        int j;
        boolean result = false;
        /*for (i = 0, j = points.length - 1; i < points.length; j = i++) {
            if ((points[i].y > test.y) != (points[j].y > test.y) &&
                    (test.x < (points[j].x - points[i].x) * (test.y - points[i].y) / (points[j].y - points[i].y) + points[i].x)) {
                result = !result;
            }
        }*/
        if (((points[0].x < test.x) && (points[0].y > test.y))) {
            if (((points[3].x < test.x) && (points[3].y < test.y))) {
                if (((points[1].x > test.x) && (points[1].y > test.y))) {
                    if (((points[2].x > test.x) && (points[2].y < test.y))) {
                        result = true;
                    }
                }
            }
        }
        return result;
    }

    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {
        private String TAG = "DownloadImage";

        private Bitmap downloadImageBitmap(String sUrl) {
            Bitmap bitmap = null;
            HttpURLConnection connection = null;
            try {
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    InputStream inputStream = new URL(sUrl).openStream();   // Download Image from URL
                    bitmap = BitmapFactory.decodeStream(inputStream);       // Decode Bitmap
                    inputStream.close();
                }else {
                    URL url = new URL(sUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    InputStream inputStream = connection.getInputStream();
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                    bitmap = BitmapFactory.decodeStream(bufferedInputStream);
                }

            } catch (Exception e) {
                Log.d(TAG, "Exception 1, Something went wrong!");
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            return downloadImageBitmap(params[0]);
        }

        protected void onPostExecute(Bitmap result) {
            Log.d(TAG, "Bitmap downloaded");
            progressDialog.dismiss();

            detectNew(result);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.show();
        }
    }


}
