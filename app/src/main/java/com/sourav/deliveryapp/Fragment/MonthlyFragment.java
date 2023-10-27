package com.sourav.deliveryapp.Fragment;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dewinjm.monthyearpicker.MonthYearPickerDialog;
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment;
import com.sourav.deliveryapp.Adapter.CompletedOrderWalletAdapter;
import com.sourav.deliveryapp.Api.ApiClient;
import com.sourav.deliveryapp.Api.ApiService;
import com.sourav.deliveryapp.Model.Foods;
import com.sourav.deliveryapp.Model.Result;
import com.sourav.deliveryapp.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MonthlyFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private String USER_ID, currentDate, DATE, PENDING, TRANSFERRED, VERIFIED;
    private EditText edit_search;
    private LinearLayout layEmpty;
    private TextView textViewNo, textViewDate, textViewPending, textViewTransferred, textViewVerified;
    private RecyclerView recyclerView;
    public CompletedOrderWalletAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Foods> foodsList, totalReceivedAmountList;
    private Date cDate;

    public MonthlyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_daily_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        layEmpty = (LinearLayout) view.findViewById(R.id.empty_view);
        textViewNo = (TextView) view.findViewById(R.id.textViewNo);
        textViewDate = (TextView) view.findViewById(R.id.textViewDate);
        textViewPending = (TextView) view.findViewById(R.id.textViewPending);
        textViewTransferred = (TextView) view.findViewById(R.id.textViewTransferred);
        textViewVerified = (TextView) view.findViewById(R.id.textViewVerified);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(android.R.color.holo_blue_dark), getResources().getColor(android.R.color.holo_red_dark), getResources().getColor(android.R.color.holo_green_light), getResources().getColor(android.R.color.holo_orange_dark));

        edit_search = (EditText) view.findViewById(R.id.edit_search);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));

        cDate = new Date();
        currentDate = new SimpleDateFormat("EEE, dd MMM yyyy").format(cDate);
        DATE = new SimpleDateFormat("yyyy-MM-dd").format(cDate);
        textViewDate.setText(currentDate);

        final int yearSelected;
        int monthSelected;

        //Set default values
        Calendar calendar = Calendar.getInstance();
        yearSelected = calendar.get(Calendar.YEAR);
        monthSelected = calendar.get(Calendar.MONTH);

        final String month;
        if (monthSelected + 1 < 10) {
            month = "0" + String.valueOf(monthSelected + 1);
        } else {
            month = String.valueOf(monthSelected + 1);
        }
        String monthName = getMonthName(month);
        textViewDate.setText(monthName + " - " + yearSelected);

        loadMonthlyData(month, String.valueOf(yearSelected));

        // show loader and fetch messages
        swipeRefreshLayout.post(
                new Runnable() {
                    @Override
                    public void run() {
                        loadMonthlyData(month, String.valueOf(yearSelected));
                    }
                }
        );



        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        textViewDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMonthYearDialog();
            }
        });

        edit_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {

                // filter your list from your input
                filter(s.toString());
                //you can use runnable postDelayed like 500 ms to delay search text
            }
        });

    }

    public void loadMonthlyData(String month, String year) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        USER_ID = sharedPreferences.getString("USER_ID", null);

        swipeRefreshLayout.setRefreshing(true);
        final android.app.AlertDialog waitingDialog = new SpotsDialog(getContext());
        waitingDialog.show();
        waitingDialog.setMessage("Please wait ...");

        //Defining retrofit api service
        ApiService service = ApiClient.getClientVegetables().create(ApiService.class);
        Call<Result> call = service.getAllCompletedOrderForShipperMonth(USER_ID, year, month);

        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                if (!response.body().getError()) {
                    waitingDialog.dismiss();
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), "No Order list", Toast.LENGTH_SHORT).show();
                } else {
                    foodsList = response.body().getOrderList();
                    if(foodsList.size() > 1)
                    {
                        textViewNo.setText(foodsList.size() + " orders found!");
                    }
                    else
                    {
                        textViewNo.setText(foodsList.size() + " order found!");
                    }
                    waitingDialog.dismiss();
                    swipeRefreshLayout.setRefreshing(false);
                    adapter = new CompletedOrderWalletAdapter(response.body().getOrderList(), getContext());
                    recyclerView.setAdapter(adapter);
                    calculateAmount();
                }
            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    void filter(String text) {
        List<Foods> temp = new ArrayList();
        for (Foods d : foodsList) {
            //or use .equal(text) with you want equal match
            //use .toLowerCase() for better matches
            if (d.getId_order().toLowerCase().contains(text.toLowerCase())
                    || d.getTotal_price().toLowerCase().contains(text.toLowerCase())
                    || d.getAddress().toLowerCase().contains(text.toLowerCase())
                    || d.getOrder_date().toLowerCase().contains(text.toLowerCase())) {
                temp.add(d);
            }
        }
        //update recyclerview
        adapter.updateList(temp);
    }

    @Override
    public void onRefresh() {
        final int yearSelected;
        int monthSelected;

        //Set default values
        Calendar calendar = Calendar.getInstance();
        yearSelected = calendar.get(Calendar.YEAR);
        monthSelected = calendar.get(Calendar.MONTH);

        final String month;
        if (monthSelected + 1 < 10) {
            month = "0" + String.valueOf(monthSelected + 1);
        } else {
            month = String.valueOf(monthSelected + 1);
        }
        String monthName = getMonthName(month);
        textViewDate.setText(monthName + " - " + yearSelected);

        loadMonthlyData(month, String.valueOf(yearSelected));
    }

    private void showMonthYearDialog() {
        int yearSelected;
        int monthSelected;

        //Set default values
        Calendar calendar = Calendar.getInstance();
        yearSelected = calendar.get(Calendar.YEAR);
        monthSelected = calendar.get(Calendar.MONTH);

        MonthYearPickerDialogFragment dialogFragment = MonthYearPickerDialogFragment
                .getInstance(monthSelected, yearSelected);

        dialogFragment.show(getFragmentManager(), null);

        dialogFragment.setOnDateSetListener(new MonthYearPickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(int year, int monthOfYear) {
                String month;
                if (monthOfYear + 1 < 10) {
                    month = "0" + String.valueOf(monthOfYear + 1);
                } else {
                    month = String.valueOf(monthOfYear + 1);
                }
                String monthName = getMonthName(month);
                textViewDate.setText(monthName + " - " + year);
                loadMonthlyData(month, String.valueOf(year));
            }
        });
    }


    private void calculateAmount() {
        // Calculate Total Price
        double pending = 0.0, transferred = 0.0, verified = 0.0;
        for (Foods foods : foodsList) {
            // 0 - Pending, 1 - Transfer, 2 - Verified.
            if(foods.getTransfer_state().equalsIgnoreCase(String.valueOf("0")))
            {
                pending += Double.parseDouble(foods.getTotal_price());
            }
            else if(foods.getTransfer_state().equalsIgnoreCase(String.valueOf("1")))
            {
                transferred += Double.parseDouble(foods.getTotal_price());
            }
            else if(foods.getTransfer_state().equalsIgnoreCase(String.valueOf("2")))
            {
                verified += Double.parseDouble(foods.getTotal_price());
            }
        }

        PENDING = String.valueOf(pending);
        TRANSFERRED = String.valueOf(transferred);
        VERIFIED = String.valueOf(verified);

        textViewPending.setText(this.getResources().getString(R.string.currency_sign) + String.format("%.2f", pending));
        textViewTransferred.setText(this.getResources().getString(R.string.currency_sign) + String.format("%.2f", transferred));
        textViewVerified.setText(this.getResources().getString(R.string.currency_sign) + String.format("%.2f", verified));
    }


    private String getMonthName(String month) {
        if(month.equalsIgnoreCase("01"))
        {
            return "January";
        }
        else if(month.equalsIgnoreCase("02"))
        {
            return "February";
        }
        else if(month.equalsIgnoreCase("03"))
        {
            return "March";
        }
        else if(month.equalsIgnoreCase("04"))
        {
            return "April";
        }
        else if(month.equalsIgnoreCase("05"))
        {
            return "May";
        }
        else if(month.equalsIgnoreCase("06"))
        {
            return "Jun";
        }
        else if(month.equalsIgnoreCase("07"))
        {
            return "July";
        }
        else if(month.equalsIgnoreCase("08"))
        {
            return "August";
        }
        else if(month.equalsIgnoreCase("09"))
        {
            return "September";
        }
        else if(month.equalsIgnoreCase("10"))
        {
            return "October";
        }
        else if(month.equalsIgnoreCase("11"))
        {
            return "November";
        }
        else {
            return "December";
        }
    }

}
