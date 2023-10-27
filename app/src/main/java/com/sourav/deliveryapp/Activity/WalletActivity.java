package com.sourav.deliveryapp.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import dmax.dialog.SpotsDialog;
import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sourav.deliveryapp.Adapter.CompletedOrderAdapter;
import com.sourav.deliveryapp.Adapter.CompletedOrderWalletAdapter;
import com.sourav.deliveryapp.Api.ApiClient;
import com.sourav.deliveryapp.Api.ApiService;
import com.sourav.deliveryapp.Common.Common;
import com.sourav.deliveryapp.Fragment.CompletedFragment;
import com.sourav.deliveryapp.Model.Foods;
import com.sourav.deliveryapp.Model.Result;
import com.sourav.deliveryapp.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class WalletActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private String USER_ID, currentDate, TOT1AL, DAILY_TOTAL, DATE;
    private LinearLayout layEmpty;
    private TextView textView_creditAmount, textView_month_creditAmount, textViewDate;
    private Button buttonTransfer, buttonTransferHistory;
    private RecyclerView completedRecyclerView;
    public CompletedOrderWalletAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Foods> foodsList, total_food_list;
    private Date cDate;
    private boolean isRemain = false;
    android.app.AlertDialog waitingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        // Change status bar color
        changeStatusBarColor("#008577");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Wallet");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // getting views
        layEmpty = (LinearLayout) findViewById(R.id.empty_view);
        textView_creditAmount = (TextView) findViewById(R.id.textView_creditAmount);
        textView_month_creditAmount = (TextView) findViewById(R.id.textView_month_creditAmount);
        textViewDate = (TextView) findViewById(R.id.textViewDate);
        buttonTransfer = (Button) findViewById(R.id.buttonTransfer);
        buttonTransferHistory = (Button) findViewById(R.id.buttonTransferHistory);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(android.R.color.holo_blue_dark), getResources().getColor(android.R.color.holo_red_dark), getResources().getColor(android.R.color.holo_green_light), getResources().getColor(android.R.color.holo_orange_dark));

        completedRecyclerView = (RecyclerView) findViewById(R.id.completedRecyclerView);
        completedRecyclerView.setLayoutManager(new LinearLayoutManager(WalletActivity.this, LinearLayoutManager.VERTICAL, false));
        completedRecyclerView.setHasFixedSize(true);

        textViewDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog();
            }
        });

        cDate = new Date();
        currentDate = new SimpleDateFormat("EEE, dd MMM yyyy").format(cDate);
        DATE = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
        textViewDate.setText(currentDate);

        // show loader and fetch messages
        swipeRefreshLayout.post(
                new Runnable() {
                    @Override
                    public void run() {
                        Date cDate = new Date();
                        DATE = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
                        loadCompletedOrder();
                    }
                }
        );

        buttonTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (textView_month_creditAmount.getText().toString().equalsIgnoreCase(getApplicationContext().getResources().getString(R.string.currency_sign) + "0.00")) {
                    Toasty.error(getApplicationContext(), "To transfer wallet amount must be higher than ৳0.00", Toast.LENGTH_SHORT, true).show();
                } else {
                    new AlertDialog.Builder(WalletActivity.this)
                            .setTitle("Are you sure?")
                            .setMessage("Do you really want to transfer amount?")
                            .setCancelable(false)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    addTransferData();
                                }
                            })
                            .setNegativeButton(android.R.string.no, null).show();
                }
            }
        });

        buttonTransferHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WalletActivity.this, WalletTransferHistoryActivity.class));
            }
        });

        loadCompletedOrder();
    }

    private void addTransferData() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(WalletActivity.this);
        USER_ID = sharedPreferences.getString("USER_ID", null);

        waitingDialog = new SpotsDialog(WalletActivity.this);
        waitingDialog.show();
        waitingDialog.setMessage("Please wait ...");

        // Delaying action for 1 second
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < foodsList.size(); i++) {

                    //Defining retrofit api service
                    ApiService service = ApiClient.getClientVegetables().create(ApiService.class);
                    Call<Result> call = service.addWalletTransfer(
                            USER_ID,
                            foodsList.get(i).getId_order(),
                            foodsList.get(i).getTotal_price(),
                            DATE,
                            Common.getDateTime());

                    call.enqueue(new Callback<Result>() {
                        @Override
                        public void onResponse(Call<Result> call, Response<Result> response) {
                            if (response.body().getError()) {
                                Toasty.error(getApplicationContext(), response.body().getMessage(), Toast.LENGTH_SHORT, true).show();
                            } else {
                                Toasty.success(getApplicationContext(), response.body().getMessage(), Toast.LENGTH_SHORT, true).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Result> call, Throwable t) {
                            Toast.makeText(WalletActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                addTransferState();
            }
        }, 1000);
    }

    private void addTransferState() {

        // Delaying action for 1 second
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < foodsList.size(); i++) {

                    ApiService service = ApiClient.getClientVegetables().create(ApiService.class);
                    //defining the call
                    Call<Result> call = service.updateTransferState(
                            foodsList.get(i).getId_order(),
                            USER_ID,
                            "1"
                    );
                    //calling the api
                    call.enqueue(new Callback<Result>() {
                        @Override
                        public void onResponse(Call<Result> call, Response<Result> response) {
                        }

                        @Override
                        public void onFailure(Call<Result> call, Throwable t) {
                            Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
                waitingDialog.dismiss();
                Date cDate = new Date();
                DATE = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
                loadCompletedOrder();
            }
        }, 1000);
    }

    public void loadDailyCompletedOrder() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(WalletActivity.this);
        USER_ID = sharedPreferences.getString("USER_ID", null);

        Log.d("DATE: ", DATE);

        swipeRefreshLayout.setRefreshing(true);
        final android.app.AlertDialog waitingDialog = new SpotsDialog(WalletActivity.this);
        waitingDialog.show();
        waitingDialog.setMessage("Please wait ...");

        //Defining retrofit api service
        ApiService service = ApiClient.getClientVegetables().create(ApiService.class);
        Call<Result> call = service.getAllCompletedOrderForShipperDate(USER_ID, DATE);

        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                waitingDialog.dismiss();
                foodsList = response.body().getOrderList();
                if (response.body().getOrderList().size() > 0) {
                    swipeRefreshLayout.setRefreshing(false);
                    completedRecyclerView.setVisibility(View.VISIBLE);
                    adapter = new CompletedOrderWalletAdapter(response.body().getOrderList(), WalletActivity.this);
                    completedRecyclerView.setAdapter(adapter);
                    calculateMonthlyAmount();
                } else {
                    textView_month_creditAmount.setText(getApplicationContext().getResources().getString(R.string.currency_sign) + "0.00");
                    swipeRefreshLayout.setRefreshing(false);
                    completedRecyclerView.setVisibility(View.GONE);
                    Toast.makeText(WalletActivity.this, "No Order list", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Toast.makeText(WalletActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    public void loadCompletedOrder() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(WalletActivity.this);
        USER_ID = sharedPreferences.getString("USER_ID", null);

        swipeRefreshLayout.setRefreshing(true);
        final android.app.AlertDialog waitingDialog = new SpotsDialog(WalletActivity.this);
        waitingDialog.show();
        waitingDialog.setMessage("Please wait ...");

        //Defining retrofit api service
        ApiService service = ApiClient.getClientVegetables().create(ApiService.class);
        Call<Result> call = service.getAllCompletedOrderForShipper(USER_ID);

        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                waitingDialog.dismiss();
                if (response.body().getOrderList().size() > 0) {
                    total_food_list = response.body().getOrderList();
                    calculateAmount();
                } else {
                    loadDailyCompletedOrder();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Toast.makeText(WalletActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
                waitingDialog.dismiss();
            }
        });
    }

    private void calculateAmount() {
        // Calculate Total Price
        double total = 0.0;
        for (Foods foods : total_food_list) {
            // 0 - Pending, 1 - Transfer, 2 - Verified.
            if (!foods.getTransfer_state().equalsIgnoreCase(String.valueOf("2"))) {
                total += Double.parseDouble(foods.getTotal_price());
            }
        }
        //TOTAL = String.valueOf(total);
        textView_creditAmount.setText(this.getResources().getString(R.string.currency_sign) + String.format("%.2f", total));

        loadDailyCompletedOrder();
    }

    private void calculateMonthlyAmount() {
        // Calculate Total Price
        double total = 0.0;
        for (Foods foods : foodsList) {
            // 0 - Pending, 1 - Transfer, 2 - Verified.
            if (!foods.getTransfer_state().equalsIgnoreCase(String.valueOf("2"))) {
                total += Double.parseDouble(foods.getTotal_price());
            }
        }
        DAILY_TOTAL = String.valueOf(total);
        textView_month_creditAmount.setText(this.getResources().getString(R.string.currency_sign) + String.format("%.2f", total));
    }

    @Override
    public void onRefresh() {
        Date cDate = new Date();
        DATE = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
        textViewDate.setText(currentDate);
        loadCompletedOrder();
    }

    private void datePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int yy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH);
        int dd = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePicker = new DatePickerDialog(WalletActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String date, day = null, month = null;

                if (dayOfMonth < 10) {
                    day = "0" + String.valueOf(dayOfMonth);

                } else {
                    day = String.valueOf(dayOfMonth);
                }
                if (monthOfYear + 1 < 10) {
                    month = "0" + String.valueOf(monthOfYear + 1);
                } else {
                    month = String.valueOf(monthOfYear + 1);
                }

                DATE = year + "-" + month + "-" + day;
                //Common.sellDate = sell_date;
                DateFormat inputFormatter1 = new SimpleDateFormat("yyyy-MM-dd");
                Date date1 = null;
                try {
                    date1 = inputFormatter1.parse(DATE);
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                DateFormat outputFormatter1 = new SimpleDateFormat("EEE, dd MMM yyyy");
                String output1 = outputFormatter1.format(date1);
                textViewDate.setText(output1);
                loadCompletedOrder();
            }
        }, yy, mm, dd);
        datePicker.show();
    }


    private void changeStatusBarColor(String color) {
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(color));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

