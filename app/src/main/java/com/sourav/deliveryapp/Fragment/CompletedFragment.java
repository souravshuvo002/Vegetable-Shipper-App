package com.sourav.deliveryapp.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sourav.deliveryapp.Adapter.AssignedOrderAdapter;
import com.sourav.deliveryapp.Adapter.CompletedOrderAdapter;
import com.sourav.deliveryapp.Api.ApiClient;
import com.sourav.deliveryapp.Api.ApiService;
import com.sourav.deliveryapp.Model.Foods;
import com.sourav.deliveryapp.Model.Result;
import com.sourav.deliveryapp.R;

import java.util.ArrayList;
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

public class CompletedFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private String USER_ID;
    private EditText edit_search;
    private LinearLayout layEmpty;
    private TextView textViewNo;
    private RecyclerView completedRecyclerView;
    public CompletedOrderAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Foods> foodsList;

    public CompletedFragment() {
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
        return inflater.inflate(R.layout.layout_completed_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        layEmpty = (LinearLayout) view.findViewById(R.id.empty_view);
        textViewNo = (TextView) view.findViewById(R.id.textViewNo);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(android.R.color.holo_blue_dark), getResources().getColor(android.R.color.holo_red_dark), getResources().getColor(android.R.color.holo_green_light), getResources().getColor(android.R.color.holo_orange_dark));

        edit_search = (EditText) view.findViewById(R.id.edit_search);
        completedRecyclerView = (RecyclerView) view.findViewById(R.id.completedRecyclerView);
        completedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        completedRecyclerView.setHasFixedSize(true);
        completedRecyclerView.setItemAnimator(new DefaultItemAnimator());
        completedRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));

        // show loader and fetch messages
        swipeRefreshLayout.post(
                new Runnable() {
                    @Override
                    public void run() {
                        loadCompletedOrder();
                    }
                }
        );

        // load shipping order
        loadCompletedOrder();


        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


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

    public void loadCompletedOrder() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        USER_ID = sharedPreferences.getString("USER_ID", null);

        swipeRefreshLayout.setRefreshing(true);
        final android.app.AlertDialog waitingDialog = new SpotsDialog(getContext());
        waitingDialog.show();
        waitingDialog.setMessage("Please wait ...");

        //Defining retrofit api service
        ApiService service = ApiClient.getClientVegetables().create(ApiService.class);
        Call<Result> call = service.getAllCompletedOrderForShipper(USER_ID);

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
                    adapter = new CompletedOrderAdapter(response.body().getOrderList(), CompletedFragment.this);
                    completedRecyclerView.setAdapter(adapter);
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
        loadCompletedOrder();
    }

}
