package com.sourav.deliveryapp.Helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.widget.Button;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class NetWorkConfig{

    private Activity activity;
    public static final int RequestPermissionCode = 1;

    public NetWorkConfig(){}

    public NetWorkConfig(Activity activity){
        this.activity = activity;
    }
    /**
     * Checks whether internet connection is available or not
     * @return
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //  check single Permission
    public Boolean checkSinglePermission(String permissionName){

        int  PermissionOk = ContextCompat.checkSelfPermission(activity, permissionName);
        return  PermissionOk == PackageManager.PERMISSION_GRANTED;
    }

    // request for single permission
    public void requestForSinglePermission(String requestName){
        ActivityCompat.requestPermissions( (Activity) activity, new String[]
                {requestName}, RequestPermissionCode);
    }

    public void createNetErrorDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("You need a network connection to use this application. Please turn on mobile network or Wi-Fi in Settings.")
                .setTitle("Unable to connect")
                .setCancelable(false)
                .setPositiveButton("Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(Settings.ACTION_SETTINGS);
                                activity.startActivity(i);
                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //activity.finish();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
        Button b = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        if(b != null) {
            b.setTextColor(Color.parseColor("#000000"));
        }
        Button b2 = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        if(b2 != null) {
            b2.setTextColor(Color.parseColor("#000000"));
        }
    }
}