package com.sourav.deliveryapp.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.content.CursorLoader;
import dmax.dialog.SpotsDialog;
import id.zelory.compressor.Compressor;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.sourav.deliveryapp.Api.ApiClient;
import com.sourav.deliveryapp.Api.ApiService;
import com.sourav.deliveryapp.Api.ApiURL;
import com.sourav.deliveryapp.Common.Common;
import com.sourav.deliveryapp.Helper.CheckPermission;
import com.sourav.deliveryapp.Model.Result;
import com.sourav.deliveryapp.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class EditUserInfoActivity extends AppCompatActivity {

    private ImageView imageViewProfile;
    private TextInputEditText editTextUserName, editTextEmail, editTextAddress,
            editTextPhone, editTextPassword, editTextConPassword;
    private Button btn_confirm;


    public static final String DATE_FORMAT = "yyyyMMdd_HHmmss";
    public static final String IMAGE_DIRECTORY = "Vegetable";
    private static final int PICK_CAMERA_IMAGE = 2;
    private static final int PICK_GALLERY_IMAGE = 1;
    private File file;
    private File sourceFile;
    private File compressedImageFile;
    private SimpleDateFormat dateFormatter;
    private Uri imageCaptureUri;

    private boolean isSelected = false;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_info);

        // Change status bar color
        changeStatusBarColor("#008577");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Edit Profile");

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(EditUserInfoActivity.this);
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


        // for Camera Error fix
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        // make directory for temp image
        file = new File(Environment.getExternalStorageDirectory() + "/" + IMAGE_DIRECTORY);
        if (!file.exists()) {
            file.mkdirs();
        }

        dateFormatter = new SimpleDateFormat(
                DATE_FORMAT, Locale.US);

        // get Customer Data
        getCustomerData();

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isSelected)
                {
                    updateCustomerInfo();
                }
                else
                {
                    updateCustomerProfileInfo();
                }
            }
        });

        imageViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImageFromGallery();
            }
        });
    }

    private void updateCustomerProfileInfo() {
        if(editTextPassword.getText().toString().equals(editTextConPassword.getText().toString())) {

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(EditUserInfoActivity.this);
            String USER_ID = sharedPreferences.getString("USER_ID", null);

            RequestBody user_id = RequestBody.create(MediaType.parse("text/plain"), USER_ID);
            RequestBody username = RequestBody.create(MediaType.parse("text/plain"), editTextUserName.getText().toString());
            RequestBody password = RequestBody.create(MediaType.parse("text/plain"), editTextPassword.getText().toString());
            RequestBody phone = RequestBody.create(MediaType.parse("text/plain"), editTextPhone.getText().toString());
            RequestBody email = RequestBody.create(MediaType.parse("text/plain"), editTextEmail.getText().toString());
            RequestBody address = RequestBody.create(MediaType.parse("text/plain"), editTextAddress.getText().toString());

            RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), compressedImageFile);

            MultipartBody.Part body = MultipartBody.Part.createFormData("image", compressedImageFile.getName(), requestFile);

            final android.app.AlertDialog waitingDialog = new SpotsDialog(EditUserInfoActivity.this);
            waitingDialog.show();
            waitingDialog.setMessage("Please wait ...");
            //building retrofit object
            ApiService service = ApiClient.getClientVegetables().create(ApiService.class);

            //defining the call
            Call<Result> call = service.updateUserWithImage(user_id, username, password, phone, email, address, body);

            //calling the api
            call.enqueue(new Callback<Result>() {
                @Override
                public void onResponse(Call<Result> call, Response<Result> response) {
                    if (!response.body().getError()) {

                        editor.clear();
                        editor.commit();

                        editor.putString("USERNAME", response.body().getUser().getUsername());
                        editor.putString("PHONE", response.body().getUser().getPhone());
                        editor.putString("PASSWORD", editTextPassword.getText().toString());
                        editor.putString("USER_ID", response.body().getUser().getId());
                        editor.apply();

                        Toast.makeText(getApplicationContext(), response.body().getMessage(), Toast.LENGTH_SHORT).show();
                        editTextUserName.setText(response.body().getUser().getUsername());
                        editTextAddress.setText(response.body().getUser().getAddress());
                        editTextPhone.setText(response.body().getUser().getPhone());
                        editTextEmail.setText(response.body().getUser().getEmail());
                        editTextPassword.setText("");
                        editTextConPassword.setText("");
                        waitingDialog.dismiss();
                        onBackPressed();
                    }
                    else {
                        Toast.makeText(EditUserInfoActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Result> call, Throwable t) {
                    Log.e("Message: ", t.getMessage());
                    Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                    waitingDialog.dismiss();
                }
            });
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Password doesn't match", Toast.LENGTH_LONG).show();
        }
    }

    private void updateCustomerInfo() {

        if(editTextPassword.getText().toString().equals(editTextConPassword.getText().toString()))
        {

            final android.app.AlertDialog waitingDialog = new SpotsDialog(EditUserInfoActivity.this);
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
                        Toast.makeText(EditUserInfoActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Result> call, Throwable t) {
                    Toast.makeText(EditUserInfoActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Password doesn't match", Toast.LENGTH_LONG).show();
        }
    }

    private void getCustomerData() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(EditUserInfoActivity.this);
        String PHONE = sharedPreferences.getString("PHONE", null);

        final android.app.AlertDialog waitingDialog = new SpotsDialog(EditUserInfoActivity.this);
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
                    if(response.body().getUser().getImage_url().equalsIgnoreCase(""))
                    {
                        Picasso.with(getApplicationContext())
                                .load(R.drawable.profile_avatar_2)
                                .into(imageViewProfile);
                    }
                    else
                    {
                        Picasso.with(getApplicationContext())
                                .load(ApiURL.SERVER_URL + response.body().getUser().getImage_url())
                                .into(imageViewProfile);
                    }
                    editTextUserName.setText(response.body().getUser().getUsername());
                    editTextAddress.setText(response.body().getUser().getAddress());
                    editTextPhone.setText(response.body().getUser().getPhone());
                    editTextEmail.setText(response.body().getUser().getEmail());
                    editTextPassword.setText("");
                    editTextConPassword.setText("");
                } else {
                    Toast.makeText(EditUserInfoActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Toast.makeText(EditUserInfoActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Image take from Gallery
    private void selectImageFromGallery() {
        CheckPermission checkPermission = new CheckPermission(EditUserInfoActivity.this);
        if (checkPermission.checkPermission()) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_GALLERY_IMAGE);
        } else {
            checkPermission.requestPermission();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                case PICK_GALLERY_IMAGE:
                    Uri imageUri = data.getData();
                    //imageView.setImageURI(imageUri);
                    Glide.with(this).load(imageUri).into(imageViewProfile);
                    //filePath = getRealPathFromURI(imageUri);
                    sourceFile = new File(getRealPathFromURI(imageUri));
                    isSelected = true;
                    compressImageWithZetbaitsuLibrary(sourceFile);       // Here i'm compress the image

                    break;
                case PICK_CAMERA_IMAGE:
                    if (imageCaptureUri == null) {
                        Toast.makeText(getApplicationContext(), "Uri empty", Toast.LENGTH_LONG).show();
                    } else {
                        //imageView.setImageURI(imageCaptureUri);
                        Glide.with(this).load(imageCaptureUri).into(imageViewProfile);
                        compressImageWithZetbaitsuLibrary(sourceFile);
                    }

                    break;
            }
        }
    }

    private void compressImageWithZetbaitsuLibrary(File sourceFile) {
        new Compressor(this)
                .setMaxWidth(1024)
                .setMaxHeight(1024)
                .setQuality(80)
                .setCompressFormat(Bitmap.CompressFormat.WEBP)
                .setDestinationDirectoryPath(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES + "/" + IMAGE_DIRECTORY).getAbsolutePath())
                .compressToFileAsFlowable(sourceFile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<File>() {
                    @Override
                    public void accept(File file) {
                        compressedImageFile = file;
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
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
