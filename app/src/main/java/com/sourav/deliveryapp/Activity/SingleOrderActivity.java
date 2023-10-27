package com.sourav.deliveryapp.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.animation.LayoutTransition;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sourav.deliveryapp.Adapter.SingleOrderStatusAdapter;
import com.sourav.deliveryapp.Api.ApiClient;
import com.sourav.deliveryapp.Api.ApiService;
import com.sourav.deliveryapp.Api.ApiURL;
import com.sourav.deliveryapp.Api.IFCMService;
import com.sourav.deliveryapp.Common.Common;
import com.sourav.deliveryapp.Fragment.AssignedFragment;
import com.sourav.deliveryapp.Helper.SendMailTask;
import com.sourav.deliveryapp.Model.DataMessage;
import com.sourav.deliveryapp.Model.MyResponse;
import com.sourav.deliveryapp.Model.Order;
import com.sourav.deliveryapp.Model.Result;
import com.sourav.deliveryapp.R;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN;

public class SingleOrderActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    RelativeLayout relativeLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerViewOrderItems;
    TextView textViewOrderID, textViewOrderDate, textViewTotal_items, textViewItems_Price, textViewAddress;
    TextView textViewOrderStatus;
    TextView textViewDelDateTime;
    Button btnUpdateStatus;
    ImageView imageViewBackButton;
    public SingleOrderStatusAdapter adapter;
    public String id_order, item, email, order_status, USER_ID, TOTAL_PRICE, SUBTOTAL;
    android.app.AlertDialog waitingDialog;
    public List<Order> orderList;
    public Date cDate;
    public String currentDate;
    public String emailBody1 = "", emailBody2 = "", emailBody3 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_order);

        id_order = getIntent().getStringExtra("ID_ORDER");

        if (SDK_INT >= JELLY_BEAN) {
            enableChangingTransition();
        }

        // getting views
        relativeLayout = (RelativeLayout) findViewById(R.id.LayMain);
        imageViewBackButton = (ImageView) findViewById(R.id.imageViewBack);
        textViewOrderID = (TextView) findViewById(R.id.textViewOrderID);
        textViewOrderDate = (TextView) findViewById(R.id.textViewOrderDate);
        textViewTotal_items = (TextView) findViewById(R.id.textViewTotalItems);
        textViewItems_Price = (TextView) findViewById(R.id.textViewItems_Price);
        textViewAddress = (TextView) findViewById(R.id.textViewAddress);
        textViewOrderStatus = (TextView) findViewById(R.id.textViewOrderStatus);
        btnUpdateStatus = (Button) findViewById(R.id.btnUpdateStatus);
        textViewDelDateTime = (TextView) findViewById(R.id.textViewDelDateTime);
        recyclerViewOrderItems = (RecyclerView) findViewById(R.id.recycler_view_OrderItems);
        recyclerViewOrderItems.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerViewOrderItems.setHasFixedSize(true);
        recyclerViewOrderItems.setItemAnimator(new DefaultItemAnimator());
        recyclerViewOrderItems.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(SingleOrderActivity.this);
        // show loader and fetch messages
        swipeRefreshLayout.post(
                new Runnable() {
                    @Override
                    public void run() {
                        //load Order details
                        loadOrderDetails(id_order);
                        //load Order items
                        loadOrderItems(id_order);
                    }
                }
        );

        if(Common.fragment_state.equalsIgnoreCase("COMPLETED"))
        {
            btnUpdateStatus.setVisibility(View.GONE);
        }

        imageViewBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        btnUpdateStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final AlertDialog.Builder[] alertDialog = {new AlertDialog.Builder(SingleOrderActivity.this)};
                alertDialog[0].setTitle("Update status For " + id_order);
                alertDialog[0].setMessage("Please Choose a status");

                View myView = LayoutInflater.from(SingleOrderActivity.this).inflate(R.layout.update_status_layout, null);

                if(Common.fragment_state.equalsIgnoreCase("SHIPPING"))
                {
                    String[] arraySpinner = new String[]{
                            "Complete", "Cancel"};
                    final Spinner spinner = (Spinner) myView.findViewById(R.id.spinner_orderStatus);
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(SingleOrderActivity.this, android.R.layout.simple_spinner_item, arraySpinner);
                    adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
                    spinner.setAdapter(adapter);

                    alertDialog[0].setView(myView);
                    alertDialog[0].setCancelable(false);

                    /*if (order_status.equalsIgnoreCase("2")) {
                        spinner.setSelection(0);
                    } else if (order_status.equalsIgnoreCase("4")) {
                        spinner.setSelection(1);
                    } else */if (order_status.equalsIgnoreCase("5")) {
                        spinner.setSelection(0);
                    } else if (order_status.equalsIgnoreCase("6")) {
                        spinner.setSelection(1);
                    }


                    final String[] finalStatus_ID = {""};
                    final String[] payment_state = {""};
                    alertDialog[0].setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            item = String.valueOf(spinner.getSelectedItem());

                            /*if(item.equalsIgnoreCase("Accept"))
                        {
                            finalStatus_ID[0] = "2";
                            payment_state[0] = "Unpaid";
                        }
                        else if(item.equalsIgnoreCase("Shipping"))
                        {
                            finalStatus_ID[0] = "4";
                            payment_state[0] = "Unpaid";
                        }
                        else */if(item.equalsIgnoreCase("Complete"))
                            {
                                cDate = new Date();
                                currentDate = new SimpleDateFormat("EEE, dd MMM yyyy").format(cDate);
                                finalStatus_ID[0] = "5";
                                payment_state[0] = "Paid";
                                showPaymentDialog(TOTAL_PRICE);
                                emailBody1 = "You have received an order.<br/>" + "<br/>" + "Order ID: " + id_order + "<br/>Date Added: " + currentDate + "<br/>Order Status: Completed";
                                sendEmail(id_order, email);
                            }
                            else if(item.equalsIgnoreCase("Cancel"))
                            {
                                finalStatus_ID[0] = "6";
                                payment_state[0] = "Unpaid";
                            }


                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SingleOrderActivity.this);
                            String USER_ID = sharedPreferences.getString("USER_ID", null);

                            //Update Order Status
                            //building retrofit object
                            Retrofit retrofit = new Retrofit.Builder()
                                    .baseUrl(ApiURL.SERVER_URL)
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build();

                            //Defining retrofit api service
                            ApiService service = retrofit.create(ApiService.class);
                            Call<Result> call = service.updateOrderStatus(
                                    id_order,
                                    USER_ID,
                                    finalStatus_ID[0],
                                    payment_state[0],
                                    Common.getDateTime());

                            call.enqueue(new Callback<Result>() {
                                @Override
                                public void onResponse(Call<Result> call, Response<Result> response) {

                                    String status = "";
                                    if (spinner.getSelectedItemPosition() == 0) {
                                        //status = "Accepted";
                                        status = "Completed";
                                    } else if (spinner.getSelectedItemPosition() == 1) {
                                        //status = "On The Way";
                                        status = "Cancelled";
                                    }/* else if (spinner.getSelectedItemPosition() == 2) {
                                    status = "Completed";
                                } else if (spinner.getSelectedItemPosition() == 3) {
                                    status = "Cancelled";
                                }*/
                                    sendOrderUpdateNotification(id_order, status);
                                    loadOrderDetails(id_order);
                                    loadOrderItems(id_order);
                                }

                                @Override
                                public void onFailure(Call<Result> call, Throwable t) {

                                }
                            });
                        }
                    });
                    alertDialog[0].setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    final android.app.AlertDialog dialog = alertDialog[0].create();
                    alertDialog[0].show();
                    Button b = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (b != null) {
                        b.setTextColor(Color.parseColor("#FF8A65"));
                    }
                }
                else
                {

                }
            }
        });

        //load Order details
        loadOrderDetails(id_order);

        //load Order items
        loadOrderItems(id_order);

        waitingDialog = new SpotsDialog(SingleOrderActivity.this);
        waitingDialog.show();
        waitingDialog.setMessage("Pleas wait ...");
    }



    private void loadOrderDetails(final String id_order) {

        swipeRefreshLayout.setRefreshing(true);

        //Defining retrofit api service
        ApiService service = ApiClient.getClientVegetables().create(ApiService.class);
        Call<Result> call = service.getOrderDetails(id_order);

        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                relativeLayout.setVisibility(View.VISIBLE);

                textViewOrderID.setText("Order No: " + id_order);
                USER_ID = response.body().getOrderDetails().get(0).getId_user();
                Common.User_ID =  response.body().getOrderDetails().get(0).getId_user();
                //Date
                String strCurrentDate = response.body().getOrderDetails().get(0).getOrder_date();
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Date newDate = null;
                try {
                    newDate = format.parse(strCurrentDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                format = new SimpleDateFormat("MMM dd, yyyy hh:mm");
                String date = format.format(newDate);
                textViewOrderDate.setText("Order date: " + date);
                textViewItems_Price.setText(getResources().getString(R.string.currency_sign)+ response.body().getOrderDetails().get(0).getTotal_price());
                TOTAL_PRICE = response.body().getOrderDetails().get(0).getTotal_price();
                textViewAddress.setText("Shipping Address:\n" + response.body().getOrderDetails().get(0).getUsername() + "\n" +
                        response.body().getOrderDetails().get(0).getAddress() + "\nArea: " +
                        response.body().getOrderDetails().get(0).getArea() + "\nPhone: " +
                        response.body().getOrderDetails().get(0).getPhone() + "\nEmail: " +
                        response.body().getOrderDetails().get(0).getEmail() + "\nNote: " +
                        response.body().getOrderDetails().get(0).getComment() + "\nPayment: " +
                        response.body().getOrderDetails().get(0).getPayment_method() + " - " +
                        response.body().getOrderDetails().get(0).getPayment_state());

                textViewOrderStatus.setText("Status: " + Common.convertCodeToStatus(response.body().getOrderDetails().get(0).getOrder_status()));
                order_status = response.body().getOrderDetails().get(0).getOrder_status();

                email = response.body().getOrderDetails().get(0).getEmail();

                if(Common.convertCodeToStatus(response.body().getOrderDetails().get(0).getOrder_status()).equalsIgnoreCase("Completed"))
                {
                    String strCurrentDate3 = response.body().getOrderDetails().get(0).getFood_delivery_date();
                    SimpleDateFormat format3 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    Date newDate3 = null;
                    try {
                        newDate3 = format3.parse(strCurrentDate3);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    format3 = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
                    String date3 = format3.format(newDate3);
                    textViewDelDateTime.setText("Delivered at: " + date3);
                }
                else
                {
                    String strCurrentDate2 = response.body().getOrderDetails().get(0).getDelivery_date();
                    SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
                    Date newDate2 = null;
                    try {
                        newDate2 = format2.parse(strCurrentDate2);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    format2 = new SimpleDateFormat("MMM dd, yyyy");
                    String date2 = format2.format(newDate2);

                    textViewDelDateTime.setText("Expected delivery: " + date2 + ", " + response.body().getOrderDetails().get(0).getDelivery_time());
                }

                swipeRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void sendEmail(String orderNO, String userEmail) {
        String fromEmail = SingleOrderActivity.this.getResources().getString(R.string.admin_user_email);
        String fromPassword = SingleOrderActivity.this.getResources().getString(R.string.admin_user_email_password);
        String toEmails = userEmail;
        List<String> toEmailList = Arrays.asList(toEmails
                .split("\\s*,\\s*"));
        String emailSubject = this.getResources().getString(R.string.main_app_name) + " - Order #" + orderNO;
        String emailBody = emailBody1 + "<br/><br/>" + emailBody2 + "<br/>" + emailBody3;
        new SendMailTask(SingleOrderActivity.this).execute(fromEmail,
                fromPassword, toEmailList, emailSubject, emailBody);
    }


    public void loadOrderItems(String id_order) {

        swipeRefreshLayout.setRefreshing(true);

        //Defining retrofit api service
        ApiService service = ApiClient.getClientVegetables().create(ApiService.class);
        Call<Result> call = service.getOrderItemsAdmin(id_order);

        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                emailBody2 = "<br/>Items<br/>";
                orderList = response.body().getOrderItems();
                // Calculate Total Price
                double sub_total = 0.0;
                for (Order order : orderList)
                {
                    emailBody2 = emailBody2 + order.getFood_quantity() + "x " + order.getFood_name() + " [" + order.getFood_min_unit_amount() + " " + order.getFood_unit() + "] - " + order.getFood_total_price() + " TAKA<br/>";
                    sub_total += (Double.parseDouble(order.getFood_total_price()));
                }

                SUBTOTAL = String.valueOf(sub_total);

                emailBody3 = "<br/>Totals<br/>Total: " + TOTAL_PRICE + " TAKA";

                if (response.body().getOrderItems().size() > 1) {
                    textViewTotal_items.setText(response.body().getOrderItems().size() + " items");
                } else {
                    textViewTotal_items.setText(response.body().getOrderItems().size() + " item");
                }

                adapter = new SingleOrderStatusAdapter(response.body().getOrderItems(), SingleOrderActivity.this);
                recyclerViewOrderItems.setAdapter(adapter);

                waitingDialog.dismiss();
                swipeRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);

            }
        });

    }

    private void showPaymentDialog(String amount) {

        LayoutInflater layoutInflater = LayoutInflater.from(SingleOrderActivity.this);
        final View view = layoutInflater.inflate(R.layout.custom_payment_dialog, null);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SingleOrderActivity.this);
        //alertDialogBuilder.setTitle("Select Payment Type");

        alertDialogBuilder.setView(view);
        final TextView textViewAmount = (TextView) view.findViewById(R.id.textViewAmount);

        DecimalFormat df2 = new DecimalFormat("####0.00");
        double price = Double.parseDouble(amount);

        textViewAmount.setText(new StringBuilder(SingleOrderActivity.this.getResources().getString(R.string.currency_sign)).append(df2.format(price)));

        alertDialogBuilder
                .setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(getApplicationContext(), "Submitted successfully", Toast.LENGTH_SHORT).show();
            }
        }).setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(getApplicationContext(), "Submitted successfully", Toast.LENGTH_SHORT).show();
            }
        });

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @TargetApi(JELLY_BEAN)
    private void enableChangingTransition() {
        ViewGroup animatedRoot = (ViewGroup) findViewById(R.id.animated_root);
        animatedRoot.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
    }

    @Override
    public void onRefresh() {
        //load Order details
        loadOrderDetails(id_order);
        //load Order items
        loadOrderItems(id_order);
    }

    private void sendOrderUpdateNotification(final String id_order, final String status) {

        //building retrofit object
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiURL.SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //Defining retrofit api service
        ApiService service = retrofit.create(ApiService.class);

        //defining the call
        Call<Result> call = service.getUserToken(Common.User_ID, "0");
        //calling the api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                Map<String, String> datasend = new HashMap<>();
                datasend.put("title", SingleOrderActivity.this.getResources().getString(R.string.main_app_name));
                datasend.put("message", "Your order #" + id_order + " has been " + status);
                datasend.put("type", "Order");


                DataMessage dataMessage = new DataMessage();
                if (response.body().getToken().getToken() != null) {
                    dataMessage.setTo(response.body().getToken().getToken());
                }
                dataMessage.setData(datasend);
                IFCMService ifcmService = Common.getFCMService();
                ifcmService.sendNotification(dataMessage)
                        .enqueue(new Callback<MyResponse>() {
                            @Override
                            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                if (response.code() == 200) {
                                    if (response.body().success == 1) {
                                        Toast.makeText(SingleOrderActivity.this, "Order Updated", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(SingleOrderActivity.this, "Send Notification failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<MyResponse> call, Throwable t) {
                                Toast.makeText(SingleOrderActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Log.d("Msg: ", "CheckOut");
                Toast.makeText(SingleOrderActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }
}