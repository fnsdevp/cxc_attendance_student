package com.fnspl.hiplaedu_student.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.fnspl.hiplaedu_student.Networking.NetworkUtility;
import com.fnspl.hiplaedu_student.R;
import com.fnspl.hiplaedu_student.application.MainApplication;
import com.fnspl.hiplaedu_student.databinding.ActivityOtpVerificationBinding;
import com.fnspl.hiplaedu_student.beaconeManager.Constants;
import com.fnspl.hiplaedu_student.model.ProfileInfo;
import com.fnspl.hiplaedu_student.receiver.SmsReceiver;
import com.fnspl.hiplaedu_student.utils.CONST;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import io.paperdb.Paper;

public class OtpVerificationActivity extends BaseActivity {

    private ActivityOtpVerificationBinding binding_otp;
    private ProgressDialog pDialog;
    private String sessionId="", phoneEmail, otp="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Constants.activity = OtpVerificationActivity.this;

        MainApplication.isOTPScreen = true;

        if(getIntent().getStringExtra(CONST.SESSION_ID)!=null){
            sessionId=getIntent().getStringExtra(CONST.SESSION_ID);
            otp=getIntent().getStringExtra(CONST.OTP);
            phoneEmail=getIntent().getStringExtra(CONST.PHONE_NUMBER);
        }

        binding_otp = DataBindingUtil.setContentView(OtpVerificationActivity.this, R.layout.activity_otp_verification);
        binding_otp.setOtp(OtpVerificationActivity.this);

        SmsReceiver.bindListener(new SmsReceiver.SmsListener() {
            @Override
            public void messageReceived(String messageText) {
                if(binding_otp.etOtp.getText().length()!=0) {
                    binding_otp.etOtp.setText(messageText);
                    binding_otp.etOtp.setSelection(messageText.length());
                }
            }
        });

        binding_otp.btnOtpSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setHideSoftKeyboard();

                doLogin();
            }
        });

        binding_otp.tvSendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSendOTP();
            }
        });

        if(otp!=null){
            //binding_otp.etOtp.setText(""+otp);
        }
    }

    private void doLogin() {

        String androidId = Settings.System.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        String urlParameters = "otp="+binding_otp.etOtp.getText().toString().trim()+
                "&device_id="+androidId+
                "&session_id="+sessionId+
                "&device_type=Android&" +
                "device_token="+ Paper.book().read(CONST.TOKEN,"");

        if(binding_otp.etOtp.getText().toString().trim().isEmpty()){
            Toast.makeText(getApplicationContext(), getString(R.string.enter_your_otp), Toast.LENGTH_SHORT).show();
        }else {
            new APIRequest().execute(urlParameters);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private class APIRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            HttpURLConnection urlConnection = null;
            try {
                Log.d("Tester", "Before request");
                URL url = new URL(NetworkUtility.BASEURL+ NetworkUtility.OTP_VERIFY);
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

            pDialog = new ProgressDialog(OtpVerificationActivity.this);
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

                    GsonBuilder builder = new GsonBuilder();
                    builder.setPrettyPrinting();
                    Gson gson = builder.create();

                    ProfileInfo profile = gson.fromJson(cmxResponse.getJSONArray("user_details").getJSONObject(0).toString() , ProfileInfo.class) ;
                    profile.setClassName(cmxResponse.getJSONArray("user_details").getJSONObject(0).getString("class"));
                    Paper.book().write(CONST.PROFILE_INFO, profile);

                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.logging_in), Toast.LENGTH_SHORT).show();

                    goToNextPage();
                } else {
                    Toast.makeText(getApplicationContext(), cmxResponse.optString("message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), getString(R.string.wrong_otp), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void goToNextPage() {
        if(Paper.book().read(CONST.CLASS_STARTED,false)){
            startActivity(new Intent(this, NotificationHandleActivity.class).putExtra(CONST.NOTIFICATION_TYPE, NotificationHandleActivity.CLASS_START));
            overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
            supportFinishAfterTransition();
        }else if(Paper.book().read(CONST.ATTENDANCE_STARTED,false)){
            startActivity(new Intent(this, NotificationHandleActivity.class).putExtra(CONST.NOTIFICATION_TYPE, NotificationHandleActivity.ATTENDANCE_START));
            overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
            supportFinishAfterTransition();
        }else if(Paper.book().read(CONST.FACIAL_ATTENDANCE_STARTED,false)){
            startActivity(new Intent(this, NotificationHandleActivity.class).putExtra(CONST.NOTIFICATION_TYPE, NotificationHandleActivity.FACIAL_ATTENDANCE));
            overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
            supportFinishAfterTransition();
        }else if(Paper.book().read(CONST.MANUAL_ATTENDANCE_STARTED,false)){
            startActivity(new Intent(this, NotificationHandleActivity.class).putExtra(CONST.NOTIFICATION_TYPE, NotificationHandleActivity.MANUAL_ATTENDANCE));
            overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
            supportFinishAfterTransition();
        }else if(Paper.book().read(CONST.ATTENDANCE_CONFIRMED,false)){
            startActivity(new Intent(this, NotificationHandleActivity.class).putExtra(CONST.NOTIFICATION_TYPE, NotificationHandleActivity.ATTENDANCE_SUCCESS));
            overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
            supportFinishAfterTransition();
        }else {
            startActivity(new Intent(this, DashboardActivity.class));
            overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
            supportFinishAfterTransition();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        MainApplication.isOTPScreen = false;
    }

    private void doSendOTP() {
        String androidId = Settings.System.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        String urlParameters = "username="+phoneEmail+
                "&device_id="+androidId+
                "&device_type=Android"+
                "&device_token="+ Paper.book().read(CONST.TOKEN,"jvdjhvd");

            new LoginRequest().execute(urlParameters);
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

            pDialog = new ProgressDialog(OtpVerificationActivity.this);
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

                    sessionId=cmxResponse.optString(CONST.SESSION_ID);
                    otp=cmxResponse.optString(CONST.OTP);

                    if(otp!=null){
                        //binding_otp.etOtp.setText(""+otp);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), cmxResponse.optString("message"), Toast.LENGTH_SHORT).show();

                }
            } catch (JSONException e) {
                e.printStackTrace();
                //Toast.makeText(getApplicationContext(), "Wrong OTP. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
