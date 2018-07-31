package com.fnspl.hiplaedu_student.Networking;

import android.app.Activity;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class PostStringRequest {
    private String username;
    private String passord, type , deviceid;
    private Activity act;
    Map<String, String> mapobject;
    private StringRequestListener postStringRequestListener;
    private JSONObject jsonObject=null;
    private String networkurl="";

    public PostStringRequest(Activity act, Map<String, String> mapobject, StringRequestListener StringRequestListener, String type, String url) {
        this.mapobject = mapobject;
        this.act = act;
        this.postStringRequestListener = StringRequestListener;
        this.type = type;
        this.networkurl=url;
        postRequestCall();
    }

    private void postRequestCall() {

        postStringRequestListener.onStarted();
        Log.d("URL","URL" +networkurl);
        StringRequest stringrequest = new StringRequest(Request.Method.POST, networkurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(" login success response","response:" +response);
                        try {
                            postStringRequestListener.onSuccess(response, type);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse response = error.networkResponse;
                        if (response != null) {
                            int code = response.statusCode;
                            String errorMsg = new String(response.data);
                            Log.d("error responser","response:" +errorMsg);
                            try {
                                jsonObject=new JSONObject(errorMsg);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                           // String msg=jsonObject.optString("Please Try again After Some time");
                          //  Log.d("error response2","response:" +errorMsg);
                           //postStringRequestListener.onFailure(code, msg);
                        } else {
                            String errorMsg = error.getMessage();
                           postStringRequestListener.onFailure(0, errorMsg);
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders()  throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
               // params.put("apiKey","fhddfjdjetuetu5735uethhsh");
               // params.put("apiSecret","fssfyu5735thhetu36u367h");
                return params;
            }
            @Override
            public Map<String, String> getParams() {

                Map<String, String> params = new HashMap<>();
                return mapobject;
            }
        };


        stringrequest.setRetryPolicy(new DefaultRetryPolicy(600000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestqueue = Volley.newRequestQueue(act);
        requestqueue.add(stringrequest);



    }


}
