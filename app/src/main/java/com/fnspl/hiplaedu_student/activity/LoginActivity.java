package com.fnspl.hiplaedu_student.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.fnspl.hiplaedu_student.Networking.NetworkUtility;
import com.fnspl.hiplaedu_student.R;
import com.fnspl.hiplaedu_student.application.MainApplication;
import com.fnspl.hiplaedu_student.database.Db_helper;
import com.fnspl.hiplaedu_student.databinding.ActivityLoginBinding;
import com.fnspl.hiplaedu_student.beaconeManager.Constants;
import com.fnspl.hiplaedu_student.model.ZoneInfo;
import com.fnspl.hiplaedu_student.retrofit.RestHandler;
import com.fnspl.hiplaedu_student.utils.CONST;
import com.fnspl.hiplaedu_student.utils.MarshmallowPermissionHelper;
import com.navigine.naviginesdk.NavigineSDK;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import io.paperdb.Paper;

public class LoginActivity extends BaseActivity {

    private static final int REQUEST_ALL_PERMISSION = 1000;
    private ActivityLoginBinding binding_login;
    private RestHandler rest;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Constants.activity = LoginActivity.this;

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);

        setBluetoothEnable(true);

        binding_login = DataBindingUtil.setContentView(LoginActivity.this, R.layout.activity_login);
        binding_login.setLogin(LoginActivity.this);

        binding_login.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setHideSoftKeyboard();

                doLogin();
            }
        });

        //MarshmallowPermissionHelper.getAllPermission(null,this,0);

        //String macAddress = getMacAddr();
    }

    private void doLogin() {
        String androidId = Settings.System.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        String urlParameters = "username="+binding_login.etUsername.getText().toString().trim()+
                "&device_id="+androidId+
                "&device_type=Android"+
                "&device_token="+ Paper.book().read(CONST.TOKEN,"jvdjhvd");

        if(binding_login.etUsername.getText().toString().trim().isEmpty()){
            Toast.makeText(getApplicationContext(), getString(R.string.enter_your_email_phone), Toast.LENGTH_SHORT).show();
        }else {
            new LoginRequest().execute(urlParameters);
        }
    }

    private class LoginRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            HttpURLConnection urlConnection = null;
            try {
                Log.d("Tester", "Before request");
                URL url = new URL(NetworkUtility.BASEURL+ NetworkUtility.LOGIN);
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

            pDialog = new ProgressDialog(LoginActivity.this);
            pDialog.setMessage(getString(R.string.dialog_msg));
            pDialog.setCancelable(false);

            pDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
            Log.d("OTP",""+s);
            try {
                JSONObject cmxResponse = new JSONObject(s);

                if (cmxResponse.getString("status").equalsIgnoreCase("success")) {
                    Toast.makeText(getApplicationContext(), cmxResponse.optString("message"), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, OtpVerificationActivity.class);
                    intent.putExtra(CONST.SESSION_ID, cmxResponse.optString(CONST.SESSION_ID));
                    intent.putExtra(CONST.OTP, cmxResponse.optString(CONST.OTP));
                    intent.putExtra(CONST.PHONE_NUMBER, binding_login.etUsername.getText().toString());
                    startActivity(intent);
                    overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
                    supportFinishAfterTransition();
                } else {
                    Toast.makeText(getApplicationContext(), cmxResponse.optString("message"), Toast.LENGTH_SHORT).show();

                }
            } catch (JSONException e) {
                e.printStackTrace();
                //Toast.makeText(getApplicationContext(), "Wrong OTP. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean setBluetoothEnable(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = false;

        if(bluetoothAdapter != null) {
            isEnabled = bluetoothAdapter.isEnabled();
        }else{
            return false ;
        }

        if (enable && !isEnabled) {
            return bluetoothAdapter.enable();
        } else if (!enable && isEnabled) {
            return bluetoothAdapter.disable();
        }
        // No need to change bluetooth state
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (haveNetworkConnection()) {
            checkNavinginePermissions();
        }else{
            showNetConnectionDialog();
        }

    }

    private void showNetConnectionDialog() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(LoginActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(LoginActivity.this);
        }
        builder.setTitle("No Internet Connection")
                .setMessage("Please enable your network connection to proceed")
                .setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                        if (haveNetworkConnection()) {
                            checkNavinginePermissions();
                        } else {
                            showNetConnectionDialog();
                        }
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void checkNavinginePermissions() {
        if (Build.VERSION.SDK_INT > 22) {
            if (MarshmallowPermissionHelper.getAllNaviginePermission(null
                    , this, REQUEST_ALL_PERMISSION)) {
                if(!MainApplication.isNavigineInitialized) {
                    (new InitTask(this)).execute();
                }else{
                    if(Paper.book().read(CONST.PROFILE_INFO,null)!=null){
                        startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                        overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
                        supportFinishAfterTransition();
                    }
                }
            }
        } else {
            if(!MainApplication.isNavigineInitialized) {
                (new InitTask(this)).execute();
            }else{
                if(Paper.book().read(CONST.PROFILE_INFO,null) != null){
                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                    overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
                    supportFinishAfterTransition();
                }
            }
        }
    }

    // Callback with the request from calling requestPermissions(...)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        switch (requestCode) {
            case REQUEST_ALL_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[3] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[4] == PackageManager.PERMISSION_GRANTED) {
                    if(!MainApplication.isNavigineInitialized) {
                        (new InitTask(this)).execute();
                    }
                }
                return;
            }

            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    class InitTask extends AsyncTask<Void, Void, Boolean> {
        private Context mContext = null;
        private String mErrorMsg = null;

        public InitTask(Context context) {
            mContext = context.getApplicationContext();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            if (!MainApplication.initialize(getApplicationContext())) {
                mErrorMsg = "Error downloading location information! Please, try again later or contact technical support";
                return Boolean.FALSE;
            }
            Log.d("AAAAAA", "Initialized!");
            if (!NavigineSDK.loadLocation(MainApplication.LOCATION_ID, 30)) {
                mErrorMsg = "Error downloading location information! Please, try again later or contact technical support";
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(pDialog.isShowing()){
                pDialog.dismiss();
            }
            if (result.booleanValue()) {
                // Starting main activity

            } else {
                MainApplication.isNavigineInitialized = false;
                Toast.makeText(mContext, mErrorMsg, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(!pDialog.isShowing()){
                pDialog.show();
            }
        }
    }

}
