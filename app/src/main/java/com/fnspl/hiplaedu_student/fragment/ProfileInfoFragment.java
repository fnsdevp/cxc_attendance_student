package com.fnspl.hiplaedu_student.fragment;


import android.app.ProgressDialog;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fnspl.hiplaedu_student.Networking.NetworkUtility;
import com.fnspl.hiplaedu_student.R;
import com.fnspl.hiplaedu_student.activity.DashboardActivity;
import com.fnspl.hiplaedu_student.activity.NotificationHandleActivity;
import com.fnspl.hiplaedu_student.application.MainApplication;
import com.fnspl.hiplaedu_student.databinding.FragmentProfileInfoBinding;
import com.fnspl.hiplaedu_student.model.ProfileInfo;
import com.fnspl.hiplaedu_student.utils.CONST;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileInfoFragment extends Fragment {

    private FragmentProfileInfoBinding binding_profileInfo;
    private View mView;
    private UUID mFaceId;

    public ProfileInfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding_profileInfo = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_info, container, false);
        binding_profileInfo.setProfileInfo(ProfileInfoFragment.this);
        mView = binding_profileInfo.getRoot();
        init(mView);
        return mView;
    }

    private void init(View mView) {
        profileInfoUpdate();
    }

    private void profileInfoUpdate() {
        try {
            if (Paper.book().read(CONST.PROFILE_INFO) != null) {
                ProfileInfo profileInfo = Paper.book().read(CONST.PROFILE_INFO);
                ImageLoader.getInstance().displayImage(NetworkUtility.IMAGE_BASEURL + "" + profileInfo.getPhoto(),
                        binding_profileInfo.ivProfilePic, CONST.ErrorWithLoaderRoundedCorner);

                binding_profileInfo.tvName.setText(String.format("%s", profileInfo.getName()));
                binding_profileInfo.tvSemester.setText(String.format(" %s", profileInfo.getClassName()));
                binding_profileInfo.tvSection.setText(String.format(" %s", profileInfo.getSection()));
                binding_profileInfo.tvStream.setText(String.format(" %s", profileInfo.getClassName()));
                binding_profileInfo.tvYear.setText(String.format(" %s", "2018"));
                binding_profileInfo.tvPhone.setText(String.format("%s", profileInfo.getPhone()));
                binding_profileInfo.tvEmail.setText(String.format("%s", profileInfo.getEmail()));
                binding_profileInfo.tvAddress.setText(String.format("%s", profileInfo.getAddress()));
                MainApplication.faceId = UUID.fromString(String.format("%s", profileInfo.getPhoto_id()));
                //MainApplication.faceId = UUID.fromString(String.format("%s", profileInfo.getPersistedFaceId()));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Paper.book().read(CONST.UPDATE_PROFILE, false)) {
            updateInfo();
        }

    }

    private void updateInfo() {
        ProfileInfo profileInfo = Paper.book().read(CONST.PROFILE_INFO);
        if (profileInfo != null) {
            String urlParameters = "user_id=" + profileInfo.getId() +
                    "&user_type=student";

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
                URL url = new URL(NetworkUtility.BASEURL + NetworkUtility.PROFILE_UPDATE);
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


        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject cmxResponse = new JSONObject(s);

                if (cmxResponse.getString("status").equalsIgnoreCase("success")) {

                    GsonBuilder builder = new GsonBuilder();
                    builder.setPrettyPrinting();
                    Gson gson = builder.create();

                    ProfileInfo profile = gson.fromJson(cmxResponse.getJSONArray("userDetails").getJSONObject(0).toString(), ProfileInfo.class);
                    profile.setClassName(cmxResponse.getJSONArray("userDetails").getJSONObject(0).getString("class"));
                    Paper.book().write(CONST.PROFILE_INFO, profile);

                    profileInfoUpdate();

                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

}
