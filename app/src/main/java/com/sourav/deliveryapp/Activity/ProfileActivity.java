package com.sourav.deliveryapp.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.sourav.deliveryapp.Api.ApiClient;
import com.sourav.deliveryapp.Api.ApiService;
import com.sourav.deliveryapp.Helper.NetWorkConfig;
import com.sourav.deliveryapp.Model.Result;
import com.sourav.deliveryapp.R;

public class ProfileActivity extends AppCompatActivity {

    public LinearLayout layOrderInfo, layLogOutInfo, layPersonalInfo, layChanePasswordInfo, layNotificationInfo;
    public TextView textViewUserName, textViewUserPhone, textViewUserEmail, textViewUserAddress, textViewContactEdit;
    private NetWorkConfig netWorkConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        netWorkConfig = new NetWorkConfig(ProfileActivity.this);

        // Change status bar color
        changeStatusBarColor("#008577");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile");


        textViewContactEdit = (TextView) findViewById(R.id.textViewContactEdit);
        textViewUserName = (TextView) findViewById(R.id.textViewUserName);
        textViewUserPhone = (TextView) findViewById(R.id.textViewUserPhone);
        textViewUserEmail = (TextView) findViewById(R.id.textViewUserEmail);
        textViewUserAddress = (TextView) findViewById(R.id.textViewUserAddress);
        layPersonalInfo = (LinearLayout) findViewById(R.id.layPersonalInfo);
        layOrderInfo = (LinearLayout) findViewById(R.id.layOrderInfo);
        layChanePasswordInfo = (LinearLayout) findViewById(R.id.layChanePasswordInfo);
        layLogOutInfo = (LinearLayout) findViewById(R.id.layLogOutInfo);
        //layNotificationInfo = (LinearLayout) findViewById(R.id.layNotificationInfo);

        textViewContactEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, EditUserInfoActivity.class));
            }
        });

        layOrderInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, ShowOrderHistoryActivity.class));
            }
        });
        layChanePasswordInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class));
            }
        });
        /*layNotificationInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ProfileActivity.this, NotificationActivity.class));
            }
        });*/
        layLogOutInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogOut();
            }
        });

        // get Customer Data
        getCustomerData();
    }

    private void getCustomerData() {
        final android.app.AlertDialog waitingDialog = new SpotsDialog(ProfileActivity.this);
        waitingDialog.show();
        waitingDialog.setMessage("Please wait ...");

        String PHONE;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ProfileActivity.this);
        PHONE = sharedPreferences.getString("PHONE", null);

        //Defining retrofit api service
        ApiService service = ApiClient.getClientVegetables().create(ApiService.class);
        Call<Result> call = service.getUser(PHONE);

        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                waitingDialog.dismiss();

                if (!response.body().getError()) {

                    layPersonalInfo.setVisibility(View.VISIBLE);
                    textViewUserName.setText(response.body().getUser().getUsername());
                    textViewUserPhone.setText(response.body().getUser().getPhone());
                    textViewUserEmail.setText(response.body().getUser().getEmail());
                    textViewUserAddress.setText(response.body().getUser().getAddress());
                } else {
                    Toast.makeText(ProfileActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getCustomerData();
    }

    private void changeStatusBarColor(String color) {
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(color));
        }
    }

    private void LogOut() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ProfileActivity.this);
        alertDialog.setTitle("Log out Application");
        alertDialog.setMessage("Do you really want to log out?");
        alertDialog.setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.setNegativeButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ProfileActivity.this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                // Clearing Data from Shared Preferences
                editor.clear();
                editor.commit();

                // Clear All Activity
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                Toast.makeText(ProfileActivity.this, "Successfully logged out!", Toast.LENGTH_LONG).show();
                finish();
            }
        });
        AlertDialog dialog = alertDialog.create();
        dialog.show();
        Button b = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if (b != null) {
            b.setTextColor(Color.parseColor("#FF8A65"));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        getCustomerData();
    }
}
