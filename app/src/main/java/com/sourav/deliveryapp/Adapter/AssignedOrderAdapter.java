package com.sourav.deliveryapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sourav.deliveryapp.Activity.MainActivity;
import com.sourav.deliveryapp.Activity.SingleOrderActivity;
import com.sourav.deliveryapp.Api.ApiService;
import com.sourav.deliveryapp.Api.ApiURL;
import com.sourav.deliveryapp.Api.IFCMService;
import com.sourav.deliveryapp.Common.Common;
import com.sourav.deliveryapp.Fragment.AssignedFragment;
import com.sourav.deliveryapp.Helper.CheckPermission;
import com.sourav.deliveryapp.Model.DataMessage;
import com.sourav.deliveryapp.Model.Foods;
import com.sourav.deliveryapp.Model.MyResponse;
import com.sourav.deliveryapp.Model.Result;
import com.sourav.deliveryapp.R;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.Manifest.permission.CALL_PHONE;

public class AssignedOrderAdapter extends RecyclerView.Adapter<AssignedOrderAdapter.ViewHolder> {

    private List<Foods> foodList;
    private Fragment context;
    private int lastPosition = -1;
    private String item;


    public AssignedOrderAdapter(List<Foods> foodList, Fragment context) {
        this.foodList = foodList;
        this.context = context;
    }

    @Override
    public AssignedOrderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_all_order_history_items, parent, false);
        return new AssignedOrderAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final AssignedOrderAdapter.ViewHolder holder, final int position) {
        final Foods food = foodList.get(position);

        /**
         *  Animation Part
         */
        /*Animation animation = AnimationUtils.loadAnimation(context,
                (position > lastPosition) ? R.anim.load_down_anim : R.anim.load_up_anim);
        holder.itemView.startAnimation(animation);
        lastPosition = position;*/

        holder.textViewOrderID.setText("ID: " + food.getId_order());
        holder.textViewName.setText(food.getUsername());
        holder.textViewAddress.setText(food.getAddress());
        holder.textViewArea.setText("Area: " + food.getArea());

        String strCurrentDate = food.getOrder_date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date newDate = null;
        try {
            newDate = format.parse(strCurrentDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        format = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
        String date = format.format(newDate);

        holder.textViewOrderDate.setText(date);

        DecimalFormat df2 = new DecimalFormat("####0.00");
        double price = Double.parseDouble(food.getTotal_price());

        holder.textViewItemPrice.setText(new StringBuilder(context.getResources().getString(R.string.currency_sign)).append(df2.format(price)));
        holder.textViewOrderStatus.setText(Common.convertCodeToStatus(food.getOrder_status()));
        holder.textViewPayment.setText(food.getPayment_method() + " - " + food.getPayment_state());

        holder.textViewOrderStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final AlertDialog.Builder[] alertDialog = {new AlertDialog.Builder(context.getContext())};
                alertDialog[0].setTitle("Update status For " + food.getId_order());
                alertDialog[0].setMessage("Please Choose a status");

                View myView = LayoutInflater.from(context.getContext()).inflate(R.layout.update_status_layout, null);

                String[] arraySpinner = new String[]{
                        "Accept", "Shipping", "Complete", "Cancel"};
                final Spinner spinner = (Spinner) myView.findViewById(R.id.spinner_orderStatus);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(context.getContext(), android.R.layout.simple_spinner_item, arraySpinner);
                adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
                spinner.setAdapter(adapter);

                alertDialog[0].setView(myView);
                alertDialog[0].setCancelable(false);

                if (food.getOrder_status().equalsIgnoreCase("2")) {
                    spinner.setSelection(0);
                } else if (food.getOrder_status().equalsIgnoreCase("4")) {
                    spinner.setSelection(1);
                } else if (food.getOrder_status().equalsIgnoreCase("5")) {
                    spinner.setSelection(2);
                } else if (food.getOrder_status().equalsIgnoreCase("6")) {
                    spinner.setSelection(3);
                }

                final String[] finalStatus_ID = {""};
                final String[] payment_state = {""};
                alertDialog[0].setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        item = String.valueOf(spinner.getSelectedItem());

                        if(item.equalsIgnoreCase("Accept"))
                        {
                            finalStatus_ID[0] = "2";
                            payment_state[0] = "Unpaid";
                        }
                        else if(item.equalsIgnoreCase("Shipping"))
                        {
                            finalStatus_ID[0] = "4";
                            payment_state[0] = "Unpaid";
                        }
                        else if(item.equalsIgnoreCase("Complete"))
                        {
                            finalStatus_ID[0] = "5";
                            payment_state[0] = "Paid";
                        }
                        else if(item.equalsIgnoreCase("Cancel"))
                        {
                            finalStatus_ID[0] = "6";
                            payment_state[0] = "Unpaid";
                        }


                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getContext());
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
                                food.getId_order(),
                                USER_ID,
                                finalStatus_ID[0],
                                payment_state[0],
                                Common.getDateTime());

                        call.enqueue(new Callback<Result>() {
                            @Override
                            public void onResponse(Call<Result> call, Response<Result> response) {

                                String status = "";
                                if (spinner.getSelectedItemPosition() == 0) {
                                    status = "Accepted";
                                } else if (spinner.getSelectedItemPosition() == 1) {
                                    status = "On The Way";
                                } else if (spinner.getSelectedItemPosition() == 2) {
                                    status = "Completed";
                                } else if (spinner.getSelectedItemPosition() == 3) {
                                    status = "Cancelled";
                                }
                                sendOrderUpdateNotification(food.getId_order(), food.getId_user(), status);

                                //Toast.makeText(context, response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                ((AssignedFragment) context).loadAssignedOrder();
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
        });

        holder.buttonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckPermission checkPermission = new CheckPermission(context.getContext());

                if (checkPermission.checkSinglePermission(CALL_PHONE)) {
                    Intent dialIntent = new Intent();
                    dialIntent.setAction(Intent.ACTION_DIAL);
                    dialIntent.setData(Uri.parse("tel:" + food.getPhone()));
                    context.startActivity(dialIntent);

                } else {
                    checkPermission.requestForSinglePermission(CALL_PHONE);
                }
            }
        });

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context.getContext(), SingleOrderActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("ID_ORDER", food.getId_order());
                Common.id_order = food.getId_order();
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewName, textViewOrderID, textViewOrderDate, textViewAddress, textViewItemPrice, textViewOrderStatus,
                textViewArea, textViewPayment;
        public Button buttonCall;
        public LinearLayout linearLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            textViewName = (TextView) itemView.findViewById(R.id.textViewName);
            textViewAddress = (TextView) itemView.findViewById(R.id.textViewAddress);
            textViewArea = (TextView) itemView.findViewById(R.id.textViewArea);
            textViewOrderID = (TextView) itemView.findViewById(R.id.textViewOrderID);
            textViewOrderDate = (TextView) itemView.findViewById(R.id.textViewOrderDate);
            textViewItemPrice = (TextView) itemView.findViewById(R.id.textViewItemPrice);
            textViewOrderStatus = (TextView) itemView.findViewById(R.id.textViewOrderStatus);
            textViewPayment = (TextView) itemView.findViewById(R.id.textViewPayment);
            buttonCall = (Button) itemView.findViewById(R.id.buttonCall);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.linearLay);
        }
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    public void updateList(List<Foods> list) {
        this.foodList = list;
        notifyDataSetChanged();
    }

    private void sendOrderUpdateNotification(final String id_order, final String id_user, final String status) {

        //building retrofit object
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiURL.SERVER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //Defining retrofit api service
        ApiService service = retrofit.create(ApiService.class);

        //defining the call
        Call<Result> call = service.getUserToken(id_user, "0");
        //calling the api
        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {

                Map<String, String> datasend = new HashMap<>();
                datasend.put("title", context.getResources().getString(R.string.main_app_name));
                datasend.put("message", "Your order #" + id_order + " has been " + status);

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
                                        Toast.makeText(context.getContext(), "Order Updated", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context.getContext(), "Send Notification failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<MyResponse> call, Throwable t) {
                                Toast.makeText(context.getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });

            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Log.d("Msg: ", "CheckOut");
                Toast.makeText(context.getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}