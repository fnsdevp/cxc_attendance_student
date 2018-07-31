package com.fnspl.hiplaedu_student.fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
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
import com.fnspl.hiplaedu_student.adapter.SubjectListAdapter;
import com.fnspl.hiplaedu_student.database.Db_helper;
import com.fnspl.hiplaedu_student.databinding.FragmentAttendenceReportBinding;
import com.fnspl.hiplaedu_student.model.AttendanceData;
import com.fnspl.hiplaedu_student.model.ProfileInfo;
import com.fnspl.hiplaedu_student.model.RoutinePeriod;
import com.fnspl.hiplaedu_student.model.Subject;
import com.fnspl.hiplaedu_student.model.SubjectReport;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.paperdb.Paper;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;

/**
 * A simple {@link Fragment} subclass.
 */
public class AttendenceReportFragment extends Fragment {

    private FragmentAttendenceReportBinding binding_report;
    private View mView;
    private ColumnChartData data;
    private boolean hasAxes = true;
    private boolean hasAxesNames = true;
    private boolean hasLabels = true;
    private boolean hasLabelForSelected = false;
    protected String[] mMonths = new String[]{
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", ""
    };
    private ProgressDialog pDialog;
    private SubjectListAdapter subjectListAdapter;

    public AttendenceReportFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding_report = DataBindingUtil.inflate(inflater, R.layout.fragment_attendence_report, container, false);
        mView = binding_report.getRoot();
        init(mView);
        return mView;
    }

    private void init(View mView) {
        subjectListAdapter = new SubjectListAdapter(getActivity(), new ArrayList<Subject>());
        subjectListAdapter.setOnDrawableForYouClickListener(new SubjectListAdapter.OnDrawableBrowseItemClickListener() {
            @Override
            public void onSubjectItemClick(int position, Subject subject) {
                requestAttendanceReport(subject);
            }
        });
        binding_report.subjectList.setAdapter(subjectListAdapter);

        //chart.setOnValueTouchListener(new ValueTouchListener());
        requestSubjectList();
    }

    private void requestAttendanceReport(Subject subject) {
        if (Paper.book().read(CONST.PROFILE_INFO) != null) {
            ProfileInfo profileInfo = Paper.book().read(CONST.PROFILE_INFO);

            String urlParameters = "student_id=" + profileInfo.getId() +
                    "&subject_id=" + subject.getSubject_id();

            new APIRequest().execute(urlParameters);
        }
    }

    private void requestSubjectList() {
        if (Paper.book().read(CONST.PROFILE_INFO) != null) {
            ProfileInfo profileInfo = Paper.book().read(CONST.PROFILE_INFO);

            String urlParameters = "student_id=" + profileInfo.getId();

            new SubjectListRequest().execute(urlParameters);
        }
    }

    private void generateDefaultData(List<AttendanceData> subjectReport) {
        int numSubcolumns = 1;
        int numColumns = 0;
        int totalPercentage = 0;
        // Column can have many subcolumns, here by default I use 1 subcolumn in each of 8 columns.
        List<Column> columns = new ArrayList<Column>();
        List<SubcolumnValue> values;
        for (int i = 0; i < mMonths.length; ++i) {

            values = new ArrayList<SubcolumnValue>();
            if (i == mMonths.length - 1) {
                values.add(new SubcolumnValue((float) 110, getResources().getColor(R.color.colorWhiteTransparent)));
            } else  {

                try {
                    int percentage = 0;
                    percentage = (int) (Float.parseFloat(subjectReport.get(i).getPercentage()));

                    totalPercentage = totalPercentage+percentage;

                    if(percentage>0) {
                        numColumns++;
                        values.add(new SubcolumnValue((float) percentage, getResources().getColor(R.color.textColorGreen)));
                    }else{
                        values.add(new SubcolumnValue((float) percentage, getResources().getColor(R.color.colorWhiteTransparent)));
                    }
                } catch (Exception e) {

                }
            }

            Column column = new Column(values);
            column.setHasLabels(hasLabels);
            column.setHasLabelsOnlyForSelected(hasLabelForSelected);
            columns.add(column);
        }

        data = new ColumnChartData(columns);

        try {
            binding_report.tvTotalPercentage.setText((int) (totalPercentage / numColumns) + "%");
        }catch (Exception ex){
            binding_report.tvTotalPercentage.setText("0%");
        }

        if (hasAxes) {
            Axis axisX = new Axis();
            Axis axisY = new Axis().setHasLines(true);
            if (hasAxesNames) {
                axisY.setName("Attendance Percentage");
                axisX.setName("Month");
            }
            List<AxisValue> xAxisValues = new ArrayList<>();
            List<AxisValue> yAxisValues = new ArrayList<>();

            for (int i = 0; i < mMonths.length; i++) {
                AxisValue axisValue = new AxisValue(i);
                axisValue.setLabel(mMonths[i]);
                xAxisValues.add(axisValue);
            }

            for (int i = 0; i <= 100; i = i + 5) {
                AxisValue axisValue = new AxisValue(i);
                axisValue.setLabel("" + i);
                yAxisValues.add(axisValue);
            }

            axisX.setValues(xAxisValues);
            axisY.setValues(yAxisValues);

            data.setAxisXBottom(axisX);
            data.setAxisYLeft(axisY);
        } else {
            data.setAxisXBottom(null);
            data.setAxisYLeft(null);
        }

        binding_report.chart.setColumnChartData(data);

    }

    private class APIRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            HttpURLConnection urlConnection = null;
            try {
                Log.d("Tester", "Before request");
                URL url = new URL(NetworkUtility.BASEURL + NetworkUtility.ATTENDANCE_REPORT_BY_YEAR);
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

                    GsonBuilder builder = new GsonBuilder();
                    builder.setPrettyPrinting();
                    Gson gson = builder.create();

                    AttendanceData[] currentAttendanceData = gson.fromJson(cmxResponse.getJSONArray("list").toString(),
                            AttendanceData[].class);

                    List<AttendanceData> attendanceDataList = Arrays.asList(currentAttendanceData);

                    generateDefaultData(attendanceDataList);

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

    private class SubjectListRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            HttpURLConnection urlConnection = null;
            try {
                Log.d("Tester", "Before request");
                URL url = new URL(NetworkUtility.BASEURL + NetworkUtility.SUBJECT_LIST);
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

                    GsonBuilder builder = new GsonBuilder();
                    builder.setPrettyPrinting();
                    Gson gson = builder.create();

                    Subject[] currentAttendanceData = gson.fromJson(cmxResponse.getJSONArray("subject").toString(),
                            Subject[].class);

                    List<Subject> subjectList = Arrays.asList(currentAttendanceData);
                    subjectListAdapter.notifyDataChange(subjectList);
                    requestAttendanceReport(subjectList.get(0));

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

}


