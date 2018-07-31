package com.fnspl.hiplaedu_student.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public String getResourceString(int stringId){
         return getResources().getString(stringId);
    }

    @NonNull
    public String getTrim(int id) {
        EditText v = (EditText) findViewById(id);
        return v.getText().toString().trim();
    }

    public boolean validateEmail(String thisname) {
        String regexStrforEmail = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if(!thisname.matches(regexStrforEmail))
            return false;

        return true;
    }

    public void showCustomeDialog(String title, String message, String buttonOk, boolean flag1, final boolean flag, final OnOkCancelListner onOkCancelListner) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting Netural "Cancel" Button
        alertDialog.setPositiveButton(buttonOk, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // User pressed Cancel button. Write Logic Here
                onOkCancelListner.onOk();
            }
        });

        if (flag) {
            alertDialog.setCancelable(false);
        }

        if (flag1) {
            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // User pressed Cancel button. Write Logic Here
                    onOkCancelListner.onCancel();
                }
            });
        }
        // Showing Alert Message
        alertDialog.show();
    }

    public interface OnOkCancelListner {
        void onOk();
        void onCancel();
    }

    public ProgressDialog PD(boolean showLoadingText, String loaderText) {
        ProgressDialog PD = new ProgressDialog(this);
        PD.setMessage("" + loaderText);
        PD.setCancelable(false);
        PD.show();
        // dialog.setMessage(Message);
        return PD;
    }

    public boolean isDataAvailable() {
        ConnectivityManager conxMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        NetworkInfo mobileNwInfo = conxMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNwInfo = conxMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return ((mobileNwInfo != null && mobileNwInfo.isConnected()) || (wifiNwInfo != null && wifiNwInfo.isConnected()));
    }

    public void setHideSoftKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            View view = getCurrentFocus();
            //If no view currently has focus, create a new one, just so we can grab a window token from it
            if (view == null) {
                view = new View(this);
            }
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public boolean validate1(String thisname) {
        String regexStrforPhn = "^[0-9]*$";

        if(thisname.length()>13)
            return false;
        if(thisname.length()<10)
            return false;
        return thisname.matches(regexStrforPhn);
    }

    public boolean checkLocationService() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        return !(!gps_enabled && !network_enabled);
    }

    public String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            //handle exception
        }
        return "";
    }

    public void connectToWiFi(String ssid ,String key,Context ctx) {

        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        wifiConfig.preSharedKey = String.format("\"%s\"", key);
        WifiManager wifiManager = (WifiManager) ctx.getSystemService(ctx.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        int networkId = wifiManager.getConnectionInfo().getNetworkId();
        wifiManager.removeNetwork(networkId);
        wifiManager.saveConfiguration();
        //remember id
        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();

    }

    public boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

}
