package com.fnspl.hiplaedu_student.fragment;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fnspl.hiplaedu_student.Networking.NetworkUtility;
import com.fnspl.hiplaedu_student.R;
import com.fnspl.hiplaedu_student.activity.DashboardActivity;
import com.fnspl.hiplaedu_student.activity.NotificationHandleActivity;
import com.fnspl.hiplaedu_student.adapter.RoutineAdapter;
import com.fnspl.hiplaedu_student.adapter.WeekListAdapter;
import com.fnspl.hiplaedu_student.database.Db_helper;
import com.fnspl.hiplaedu_student.databinding.FragmentRoutineBinding;
import com.fnspl.hiplaedu_student.model.ProfileInfo;
import com.fnspl.hiplaedu_student.model.RoutinePeriod;
import com.fnspl.hiplaedu_student.model.Subject;
import com.fnspl.hiplaedu_student.model.Teacher_details;
import com.fnspl.hiplaedu_student.model.ZoneInfo;
import com.fnspl.hiplaedu_student.utils.CONST;
import com.fnspl.hiplaedu_student.widget.Dialogs;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 */
public class RoutineFragment extends Fragment implements RoutineAdapter.OnProductClickListener {

    private FragmentRoutineBinding binding_routine;
    private View mView;
    private RoutineAdapter mAdapter;
    private WeekListAdapter subjectListAdapter;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    private String selectedDate="";

    public RoutineFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding_routine = DataBindingUtil.inflate(inflater, R.layout.fragment_routine, container, false);
        binding_routine.setRoutine(RoutineFragment.this);
        mView = binding_routine.getRoot();
        init(mView);
        return mView;
    }

    private void init(View mView) {
        subjectListAdapter = new WeekListAdapter(getActivity(), new ArrayList<Subject>());
        subjectListAdapter.setOnDrawableForYouClickListener(new WeekListAdapter.OnDrawableBrowseItemClickListener() {
            @Override
            public void onSubjectItemClick(String date) {
                selectedDate = date;
                fetchRoutine(selectedDate);
            }
        });
        binding_routine.subjectList.setAdapter(subjectListAdapter);

        binding_routine.rvRoutine.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new RoutineAdapter(getActivity(), new ArrayList<RoutinePeriod>());
        mAdapter.setOnProductClickListener(this);
        binding_routine.rvRoutine.setAdapter(mAdapter);

        selectedDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        fetchRoutine(selectedDate);
    }

    private void fetchRoutine(String date) {
        ProfileInfo profileInfo = Paper.book().read(CONST.PROFILE_INFO);

        String urlParameters = "userid=" + profileInfo.getId() +
                "&usertype=student" +
                "&date=" + date +
                "&device_type=Android";

        new RoutineFetch().execute(urlParameters);
    }



    @Override
    public void onNavigate(final RoutinePeriod routinePeriod, int position) {
        Dialogs.dialogFetchImage(getActivity(), new Dialogs.OnOptionSelect() {
            @Override
            public void openNavigine() {

                openMap(routinePeriod);

            }

            @Override
            public void openNormalApp() {
                if(getActivity()!=null){
                    //getActivity().startActivity(new Intent(getActivity(), NavigationActivity.class));
                }
            }
        });

    }

    @Override
    public void onRoutineClick(RoutinePeriod routinePeriod, int position) {
        try {

            Date currentDateTime = new Date();

            Date classStartTime = dateFormat.parse(new SimpleDateFormat("yyyy-MM-dd").format(currentDateTime)
                    + " " + routinePeriod.getStartTime());

            Date classEndTime = dateFormat.parse(new SimpleDateFormat("yyyy-MM-dd").format(currentDateTime)
                    + " " + routinePeriod.getEndTime());

            if(currentDateTime.compareTo(classStartTime)>0 && currentDateTime.compareTo(classEndTime)<0){
                doCalculation();
            }else{
                Toast.makeText(getActivity(), getResources().getString(R.string.routine_not_running), Toast.LENGTH_SHORT).show();
            }

        }catch (Exception ex){

        }
    }

    private void doCalculation() {
        if(Paper.book().read(CONST.CLASS_STARTED,false)){
            startActivity(new Intent(getActivity(), NotificationHandleActivity.class).putExtra(CONST.NOTIFICATION_TYPE, NotificationHandleActivity.CLASS_START));
            getActivity().overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
            getActivity().supportFinishAfterTransition();

        }else if(Paper.book().read(CONST.ATTENDANCE_STARTED,false)){
            startActivity(new Intent(getActivity(), NotificationHandleActivity.class).putExtra(CONST.NOTIFICATION_TYPE, NotificationHandleActivity.ATTENDANCE_START));
            getActivity().overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
            getActivity().supportFinishAfterTransition();

        }else if(Paper.book().read(CONST.FACIAL_ATTENDANCE_STARTED,false)){
            startActivity(new Intent(getActivity(), NotificationHandleActivity.class).putExtra(CONST.NOTIFICATION_TYPE, NotificationHandleActivity.FACIAL_ATTENDANCE));
            getActivity().overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
            getActivity().supportFinishAfterTransition();

        }else if(Paper.book().read(CONST.MANUAL_ATTENDANCE_STARTED,false)){
            startActivity(new Intent(getActivity(), NotificationHandleActivity.class).putExtra(CONST.NOTIFICATION_TYPE, NotificationHandleActivity.MANUAL_ATTENDANCE));
            getActivity().overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
            getActivity().supportFinishAfterTransition();

        }else if(Paper.book().read(CONST.ATTENDANCE_CONFIRMED,false)){
            startActivity(new Intent(getActivity(), NotificationHandleActivity.class).putExtra(CONST.NOTIFICATION_TYPE, NotificationHandleActivity.ATTENDANCE_SUCCESS));
            getActivity().overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
            getActivity().supportFinishAfterTransition();

        }else {

        }
    }

    private void openMap(RoutinePeriod routinePeriod) {
        Db_helper db_helper = new Db_helper(getActivity());
        ZoneInfo zoneInfo = db_helper.getZoneInfo(String.format("%s", routinePeriod.getRoom_id() ));
        if(zoneInfo!=null) {
            String[] location = zoneInfo.getCenterPoint().split(",");

            NavigineMapDialogNew mapDialog = new NavigineMapDialogNew();
            Bundle bundle = new Bundle();
            bundle.putString(CONST.POINTX, location[0]);
            bundle.putString(CONST.POINTY, location[1]);
            mapDialog.setArguments(bundle);
            if(mapDialog!=null &&  mapDialog.getDialog()!=null
                    && mapDialog.getDialog().isShowing()) {
                //dialog is showing so do something
            } else {
                //dialog is not showing
                mapDialog.show(getChildFragmentManager(), "mapDialog");
            }
        }else{
            Toast.makeText(getActivity(), getResources().getString(R.string.no_info_available), Toast.LENGTH_SHORT).show();
        }
    }

    private class RoutineFetch extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            HttpURLConnection urlConnection = null;
            try {
                Log.d("Tester", "Before request");
                URL url = new URL(NetworkUtility.BASEURL + NetworkUtility.ROUTINE_FETCH);
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

            // Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
            //macAddress = getMacAddr();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            List<RoutinePeriod> routinePeriodsList = new ArrayList<>();

            try {
                JSONObject response = new JSONObject(s);

                if (response.getString("status").equalsIgnoreCase("success")) {

                    GsonBuilder builder = new GsonBuilder();
                    builder.setPrettyPrinting();
                    Gson gson = builder.create();

                    JSONObject user_routine = response.getJSONArray("user_routine").getJSONObject(0);
                    JSONArray routineArray = user_routine.getJSONArray("routine");

                    for (int i = 0; i < routineArray.length(); i++) {
                        RoutinePeriod routinePeriod = gson.fromJson(routineArray.getJSONObject(i).toString(),
                                RoutinePeriod.class);
                        Teacher_details teacher_details = gson.fromJson(routineArray.getJSONObject(i).getJSONObject("teacher_details")
                                .toString(), Teacher_details.class);

                        routinePeriod.setTeacher_details(teacher_details);

                        routinePeriodsList.add(routinePeriod);
                    }

                    Collections.sort(routinePeriodsList, new DateComparator());

                    mAdapter.notifyDataChange(routinePeriodsList);

                } else {
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), getResources().getString(R.string.no_record_available), Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();

                if (getActivity() != null)
                    Toast.makeText(getActivity(), getResources().getString(R.string.no_record_available), Toast.LENGTH_SHORT).show();
            }
        }

    }

    public class DateComparator implements Comparator<RoutinePeriod> {

        @Override
        public int compare(RoutinePeriod emp1, RoutinePeriod emp2) {
            try {
                Date startDate = dateFormat.parse(selectedDate + " " + emp1.getStartTime());
                Date endDate = dateFormat.parse(selectedDate + " " + emp2.getStartTime());

                return endDate.compareTo(startDate);
            }catch (Exception ex){
                ex.printStackTrace();
            }
            return 0;
        }
    }

}
