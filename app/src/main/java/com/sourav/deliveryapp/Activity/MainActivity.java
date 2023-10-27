package com.sourav.deliveryapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sourav.deliveryapp.Api.ApiClient;
import com.sourav.deliveryapp.Api.ApiService;
import com.sourav.deliveryapp.Model.Result;
import com.sourav.deliveryapp.R;

public class MainActivity extends AppCompatActivity {
    public TextView textViewName;
    public LinearLayout linearLayAllOrders, linearLayWallet, linearLayProfile;

    public String NAME, USER_ID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Change status bar color
        changeStatusBarColor("#008577");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Shipper App");
        setSupportActionBar(toolbar);

        subscribeToPushService();


        // getting views
        textViewName = (TextView) findViewById(R.id.textViewName);
        linearLayAllOrders = (LinearLayout) findViewById(R.id.linearLayAllOrders);
        linearLayWallet = (LinearLayout) findViewById(R.id.linearLayWallet);
        linearLayProfile = (LinearLayout) findViewById(R.id.linearLayProfile);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        NAME = sharedPreferences.getString("USERNAME", null);
        USER_ID = sharedPreferences.getString("USER_ID", null);
        textViewName.setText(NAME);

        linearLayAllOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ShowOrderHistoryActivity.class));
            }
        });

        linearLayWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, WalletActivity.class));
            }
        });

        linearLayProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        });

        updateTokenToServer();
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
                        Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void changeStatusBarColor(String color) {
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(color));
        }
    }

    public void subscribeToPushService() {
        FirebaseMessaging.getInstance().subscribeToTopic("shipper");
        FirebaseMessaging.getInstance().subscribeToTopic("all");
    }
}
