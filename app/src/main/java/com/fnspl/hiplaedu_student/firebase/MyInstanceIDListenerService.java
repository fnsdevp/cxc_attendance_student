package com.fnspl.hiplaedu_student.firebase;

import android.util.Log;

import com.fnspl.hiplaedu_student.utils.CONST;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import io.paperdb.Paper;

/**
 * Created by Administrator on 08/06/2017.
 */

public class MyInstanceIDListenerService extends FirebaseInstanceIdService {

    private String TAG = "Firebase";

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.e(TAG,"Refreshed token: " + refreshedToken);
        //Log.d(TAG, "Refreshed token: " + refreshedToken);
        Paper.book().write(CONST.TOKEN, refreshedToken);
    }

}
