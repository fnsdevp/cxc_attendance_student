package com.fnspl.hiplaedu_student.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import com.fnspl.hiplaedu_student.R;
import com.fnspl.hiplaedu_student.databinding.ActivityRegistrationBinding;
import com.fnspl.hiplaedu_student.beaconeManager.Constants;
import com.fnspl.hiplaedu_student.utils.CONST;
import com.fnspl.hiplaedu_student.utils.MarshmallowPermissionHelper;
import com.fnspl.hiplaedu_student.widget.Dialogs;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RegistrationActivity extends BaseActivity implements Dialogs.OnOptionSelect, View.OnClickListener {

    public static final int REQUEST_CAMERA_STORAGE_PERMISSION = 0;
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_OPEN_GALLERY = 2;
    private static final String LOG_TAG = "Test";
    private Uri imageUri;
    private String mCurrentPhotoPath;
    private SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
    private static final int CROP_PIC_REQUEST_CODE = 3;
    private File file;

    Bitmap image;
    String datapath = "";
    private ActivityRegistrationBinding binding_registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Constants.activity = RegistrationActivity.this;

        binding_registration = DataBindingUtil.setContentView(this, R.layout.activity_registration);
        binding_registration.setRegistration(RegistrationActivity.this);

        binding_registration.ivProfilePic.setOnClickListener(this);
    }

    public void checkCameraPermission() {
        if (Build.VERSION.SDK_INT > 22) {
            if (MarshmallowPermissionHelper.getStorageAndCameraPermission(null
                    , this, REQUEST_CAMERA_STORAGE_PERMISSION)) {
                Dialogs.dialogFetchImage(this, this);
            }
        } else {
            Dialogs.dialogFetchImage(this, this);
        }
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageUri = Uri.fromFile(getOutputMediaFile());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    private void getFromGallery() {
        Intent getFromGallery = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        getFromGallery.setType("image/*");
        if (getFromGallery.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(getFromGallery, REQUEST_OPEN_GALLERY);
        }
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
                    Dialogs.dialogFetchImage(this, this);
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
        if (resultCode == RESULT_OK) {

            switch (requestCode) {

                case REQUEST_IMAGE_CAPTURE:

                    try {
                        //String path = file.getAbsolutePath();
                        doCrop(imageUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case REQUEST_OPEN_GALLERY:

                    try {
                        Uri uri = data.getData();
                        String[] projection = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(projection[0]);
                        String picturePath = cursor.getString(columnIndex); // returns null
                        cursor.close();
                        file = new File(picturePath);
                        imageUri = Uri.fromFile(file);
                        doCrop(imageUri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                    try {
                        CropImage.ActivityResult result = CropImage.getActivityResult(data);
                        if (resultCode == RESULT_OK) {
                            Uri resultUri = result.getUri();
                            file = new File(resultUri.getPath());
                            ImageLoader.getInstance().displayImage("file://" + file.getAbsolutePath(),
                                    binding_registration.ivProfilePic, CONST.ErrorWithLoaderRoundedCorner);
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
                .start(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_profile_pic:
                checkCameraPermission();
                break;
        }
    }

    @Override
    public void openNavigine() {

    }

    @Override
    public void openNormalApp() {

    }
}
