package com.sourav.deliveryapp.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sourav.deliveryapp.Activity.LoginActivity;
import com.sourav.deliveryapp.Activity.ShowOrderHistoryActivity;
import com.sourav.deliveryapp.Api.ApiClient;
import com.sourav.deliveryapp.Api.ApiService;
import com.sourav.deliveryapp.Helper.NotificationHelper;
import com.sourav.deliveryapp.Model.Result;
import com.sourav.deliveryapp.R;

import java.util.Map;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        updateTokenToServer();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if(remoteMessage.getData() != null)
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                sendNotificationAPI26(remoteMessage);
            }
            else
            {
                sendNotification(remoteMessage);
            }
        }

    }

    private void sendNotification(RemoteMessage remoteMessage) {
        // Get Information From Message
        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("message");

        Intent intent = new Intent(this, ShowOrderHistoryActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pIntent)
                .setOngoing(true)
                .setShowWhen(true)
                .setAutoCancel(true)
                .setSound(defaultSoundUri);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(new Random().nextInt(), builder.build());

    }

    private void sendNotificationAPI26(RemoteMessage remoteMessage) {
        // Get Information From Message
        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String message = data.get("message");

        // From API level 26, we need implement Notification Channel
        NotificationHelper helper;
        Notification.Builder builder;

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        helper = new NotificationHelper(this);
        builder = helper.getNotification(title,message,defaultSoundUri);
        helper.getManager().notify(new Random().nextInt(), builder.build());
    }

    private void updateTokenToServer() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String USER_ID = sharedPreferences.getString("USER_ID", null);

        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {

                        //building retrofit object
                        ApiService service = ApiClient.getClientVegetables().create(ApiService.class);

                        //defining the call
                        Call<Result> call = service.updateUserToken(USER_ID, instanceIdResult.getToken());
                        //calling the api
                        call.enqueue(new Callback<Result>() {
                            @Override
                            public void onResponse(Call<Result> call, Response<Result> response) {
                                Log.e("MA_Debug: ", response.body().getMessage());
                            }

                            @Override
                            public void onFailure(Call<Result> call, Throwable t) {
                                Log.e("MA_Debug: ", t.getMessage());
                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MyFirebaseMessaging.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}