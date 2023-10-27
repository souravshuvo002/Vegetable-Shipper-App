package com.sourav.deliveryapp.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.sourav.deliveryapp.Api.ApiClient;
import com.sourav.deliveryapp.Api.ApiService;
import com.sourav.deliveryapp.Common.Common;
import com.sourav.deliveryapp.Model.Result;
import com.sourav.deliveryapp.R;

public class LoginActivity extends AppCompatActivity {

    public AppCompatButton btn_login;
    private TextInputEditText editTextPhone, editTextPassword;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private static final int REQUEST_CODE = 7171;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        makeFullScreen();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);
        editor = sharedPreferences.edit();

        // getting views
        editTextPhone = (TextInputEditText) findViewById(R.id.input_phone);
        editTextPassword = (TextInputEditText) findViewById(R.id.input_password);

        btn_login = (AppCompatButton) findViewById(R.id.btn_login);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginAdmin();
            }
        });
    }

    public void makeFullScreen() {
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    private void loginAdmin() {
        final android.app.AlertDialog waitingDialog = new SpotsDialog(LoginActivity.this);
        waitingDialog.show();
        waitingDialog.setMessage("Please wait ...");


        //Defining retrofit api service
        ApiService service = ApiClient.getClientVegetables().create(ApiService.class);
        Call<Result> call = service.loginShipper("+88" + editTextPhone.getText().toString(), editTextPassword.getText().toString());

        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                waitingDialog.dismiss();

                if (!response.body().getError()) {
                    // Clearing shared preferences
                    editor.clear();
                    editor.commit();

                    editor.putString("USERNAME", response.body().getUser().getUsername());
                    editor.putString("PHONE", response.body().getUser().getPhone());
                    editor.putString("PASSWORD", editTextPassword.getText().toString());
                    editor.putString("USER_ID", response.body().getUser().getId());
                    editor.apply();

                    Common.User_phone = editTextPhone.getText().toString();
                    Common.User_ID = response.body().getUser().getId();

                    updateTokenToServer();

                    Toast.makeText(getApplicationContext(), "Welcome back " + response.body().getUser().getUsername(), Toast.LENGTH_LONG).show();

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
                        Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
    }
}
