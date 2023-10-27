package com.sourav.deliveryapp.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.sourav.deliveryapp.Api.ApiClient;
import com.sourav.deliveryapp.Api.ApiService;
import com.sourav.deliveryapp.Api.ApiURL;
import com.sourav.deliveryapp.Common.Common;
import com.sourav.deliveryapp.Model.Result;
import com.sourav.deliveryapp.R;
import com.squareup.picasso.Picasso;

public class ChangePasswordActivity extends AppCompatActivity {

    private ImageView imageViewProfile;

    private TextInputEditText editTextUserName, editTextAddress, editTextEmail,
            editTextPhone, editTextPassword, editTextConPassword;
    private Button btn_confirm;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Change status bar color
        changeStatusBarColor("#008577");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Edit Password");

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ChangePasswordActivity.this);
        editor = sharedPreferences.edit();

        // getting views
        imageViewProfile = (ImageView) findViewById(R.id.show_profile_image);
        editTextUserName = (TextInputEditText) findViewById(R.id.editTextUserName);
        editTextAddress = (TextInputEditText) findViewById(R.id.editTextAddress);
        editTextEmail = (TextInputEditText) findViewById(R.id.editTextEmail);
        editTextPhone = (TextInputEditText) findViewById(R.id.editTextPhone);
        editTextPassword = (TextInputEditText) findViewById(R.id.editTextPassword);
        editTextConPassword = (TextInputEditText) findViewById(R.id.editTextConPassword);

        btn_confirm = (Button) findViewById(R.id.btn_confirm);

        // get Customer Data
        getCustomerData();

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateCustomerInfo();
            }
        });
    }

    private void updateCustomerInfo() {

        if (editTextPassword.getText().toString().equals(editTextConPassword.getText().toString())) {
            final android.app.AlertDialog waitingDialog = new SpotsDialog(ChangePasswordActivity.this);
            waitingDialog.show();
            waitingDialog.setMessage("Please wait ...");


            //Defining retrofit api service
            ApiService service = ApiClient.getClientVegetables().create(ApiService.class);
            Call<Result> call = service.updateCustomerInfo(Common.User_ID,
                    editTextUserName.getText().toString(),
                    editTextPassword.getText().toString(),
                    editTextPhone.getText().toString(),
                    editTextEmail.getText().toString(),
                    editTextAddress.getText().toString());

            call.enqueue(new Callback<Result>() {
                @Override
                public void onResponse(Call<Result> call, Response<Result> response) {
                    waitingDialog.dismiss();

                    if (!response.body().getError()) {

                        editor.clear();
                        editor.commit();

                        editor.putString("USERNAME", response.body().getUser().getUsername());
                        editor.putString("PHONE", response.body().getUser().getPhone());
                        editor.putString("PASSWORD", editTextPassword.getText().toString());
                        editor.putString("USER_ID", response.body().getUser().getId());
                        editor.apply();

                        Toast.makeText(getApplicationContext(), response.body().getMessage(), Toast.LENGTH_LONG).show();
                        editTextUserName.setText(response.body().getUser().getUsername());
                        editTextAddress.setText(response.body().getUser().getAddress());
                        editTextPhone.setText(response.body().getUser().getPhone());
                        editTextEmail.setText(response.body().getUser().getEmail());
                        editTextPassword.setText("");
                        editTextConPassword.setText("");
                        onBackPressed();
                    } else {
                        Toast.makeText(ChangePasswordActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Result> call, Throwable t) {
                    Toast.makeText(ChangePasswordActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "Password doesn't match", Toast.LENGTH_LONG).show();
        }
    }

    private void getCustomerData() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ChangePasswordActivity.this);
        String PHONE = sharedPreferences.getString("PHONE", null);

        final android.app.AlertDialog waitingDialog = new SpotsDialog(ChangePasswordActivity.this);
        waitingDialog.show();
        waitingDialog.setMessage("Please wait ...");


        //Defining retrofit api service
        ApiService service = ApiClient.getClientVegetables().create(ApiService.class);
        Call<Result> call = service.getUser(PHONE);

        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                waitingDialog.dismiss();

                if (!response.body().getError()) {
                    if (response.body().getUser().getImage_url().equalsIgnoreCase("")) {
                        Picasso.with(getApplicationContext())
                                .load(R.drawable.profile_avatar_2)
                                .into(imageViewProfile);
                    } else {
                        Picasso.with(getApplicationContext())
                                .load(ApiURL.SERVER_URL + response.body().getUser().getImage_url())
                                .into(imageViewProfile);
                    }
                    Common.User_ID = response.body().getUser().getId();
                    editTextUserName.setText(response.body().getUser().getUsername());
                    editTextAddress.setText(response.body().getUser().getAddress());
                    editTextPhone.setText(response.body().getUser().getPhone());
                    editTextEmail.setText(response.body().getUser().getEmail());
                    editTextPassword.setText("");
                    editTextConPassword.setText("");
                } else {
                    Toast.makeText(ChangePasswordActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Toast.makeText(ChangePasswordActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
    }

    private void changeStatusBarColor(String color) {
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(color));
        }
    }
}

