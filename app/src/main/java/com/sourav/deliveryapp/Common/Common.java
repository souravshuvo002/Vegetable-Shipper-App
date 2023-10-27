package com.sourav.deliveryapp.Common;

import android.widget.Toast;

import com.sourav.deliveryapp.Activity.SingleOrderActivity;
import com.sourav.deliveryapp.Adapter.SingleOrderStatusAdapter;
import com.sourav.deliveryapp.Api.ApiClient;
import com.sourav.deliveryapp.Api.ApiService;
import com.sourav.deliveryapp.Api.FCMClient;
import com.sourav.deliveryapp.Api.IFCMService;
import com.sourav.deliveryapp.Helper.SendMailTask;
import com.sourav.deliveryapp.Model.Order;
import com.sourav.deliveryapp.Model.Result;
import com.sourav.deliveryapp.Model.User;
import com.sourav.deliveryapp.R;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class Common {

    public static String User_email = "";
    public static String User_phone = "";
    public static String User_ID = "";
    public static User currentUser;
    public static String fragment_state = "";

    public static String menu_id = "";
    public static String menu_name = "";
    public static String id_order = "";


    private static final String FCM_API = "https://fcm.googleapis.com/";

    public static IFCMService getFCMService(){
        return FCMClient.getClient(FCM_API).create(IFCMService.class);
    }

    public static String convertCodeToStatus(String code)
    {
        if(code.equals("1"))
            return "Pending";
        else if(code.equals("2"))
            return "Accepted";
        else if(code.equals("3"))
            return "Rejected";
        else if(code.equals("4"))
            return "Shipping";
        else if(code.equals("5"))
            return "Completed";
        else if(code.equals("6"))
            return "Cancelled";
        else
            return "No result";
    }

    public static String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
