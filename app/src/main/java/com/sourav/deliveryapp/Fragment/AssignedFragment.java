package com.sourav.deliveryapp.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sourav.deliveryapp.Activity.MainActivity;
import com.sourav.deliveryapp.Adapter.AssignedOrderAdapter;
import com.sourav.deliveryapp.Api.ApiClient;
import com.sourav.deliveryapp.Api.ApiService;
import com.sourav.deliveryapp.Model.Area;
import com.sourav.deliveryapp.Model.Foods;
import com.sourav.deliveryapp.Model.Result;
import com.sourav.deliveryapp.R;

import java.util.ArrayList;
import java.util.HashSet;
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

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class AssignedFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private String USER_ID;
    private EditText edit_search;
    private LinearLayout layEmpty;
    private TextView textViewNo;
    private RecyclerView assignedRecyclerView;
    public AssignedOrderAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Foods> foodsList;
    private ArrayList<String> areaList;
    private Spinner spinner_area;

    public AssignedFragment() {
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
        return inflater.inflate(R.layout.layout_assigned_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        layEmpty = (LinearLayout) view.findViewById(R.id.empty_view);
        textViewNo = (TextView) view.findViewById(R.id.textViewNo);
        spinner_area = (Spinner) view.findViewById(R.id.spinner_area);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(android.R.color.holo_blue_dark), getResources().getColor(android.R.color.holo_red_dark), getResources().getColor(android.R.color.holo_green_light), getResources().getColor(android.R.color.holo_orange_dark));

        edit_search = (EditText) view.findViewById(R.id.edit_search);
        assignedRecyclerView = (RecyclerView) view.findViewById(R.id.assignedRecyclerView);
        assignedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        assignedRecyclerView.setHasFixedSize(true);
        assignedRecyclerView.setItemAnimator(new DefaultItemAnimator());
        assignedRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        
        // show loader and fetch messages
        swipeRefreshLayout.post(
                new Runnable() {
                    @Override
                    public void run() {
                        loadAssignedOrder();
                    }
                }
        );

        // load Assigned order
        loadAssignedOrder();


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

    public void loadAssignedOrder() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        USER_ID = sharedPreferences.getString("USER_ID", null);

        swipeRefreshLayout.setRefreshing(true);
        final android.app.AlertDialog waitingDialog = new SpotsDialog(getContext());
        waitingDialog.show();
        waitingDialog.setMessage("Please wait ...");

        //Defining retrofit api service
        ApiService service = ApiClient.getClientVegetables().create(ApiService.class);
        Call<Result> call = service.getAllAssignedOrderForShipper(USER_ID);

        call.enqueue(new Callback<Result>() {
            @Override
            public void onResponse(Call<Result> call, Response<Result> response) {
                if (!response.body().getError()) {
                    waitingDialog.dismiss();
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getContext(), "No Order list", Toast.LENGTH_SHORT).show();
                } else {
                    foodsList = response.body().getOrderList();
                    loadArea(foodsList);
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
                    adapter = new AssignedOrderAdapter(response.body().getOrderList(), AssignedFragment.this);
                    assignedRecyclerView.setAdapter(adapter);
                }

            }

            @Override
            public void onFailure(Call<Result> call, Throwable t) {
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void loadArea(List<Foods> foodsList) {
        areaList = new ArrayList<String>();
        for(int i=0;i<foodsList.size();i++)
        {
            areaList.add(foodsList.get(i).getArea());
        }

        HashSet hs = new HashSet();
        hs.addAll(areaList);
        areaList.clear();
        areaList.addAll(hs);

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, areaList);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner_area.setAdapter(dataAdapter);

        spinner_area.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String string = spinner_area.getSelectedItem().toString();
                /*shipperName = string;
                shipperID = shipperMap.get(string);*/
                filterArea(string);
                //textViewNo.setText(temp.size() + " items found!");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
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
                    || d.getOrder_date().toLowerCase().contains(text.toLowerCase())
                    || d.getArea().toLowerCase().contains(text.toLowerCase())) {
                temp.add(d);
            }
        }
        //update recyclerview
        adapter.updateList(temp);
        textViewNo.setText(temp.size() + " items found!");
    }

    void filterArea(String text) {
        List<Foods> temp = new ArrayList();
        for (Foods d : foodsList) {
            //or use .equal(text) with you want equal match
            //use .toLowerCase() for better matches
            if (d.getArea().toLowerCase().contains(text.toLowerCase())) {
                temp.add(d);
            }
        }
        //update recyclerview
        adapter.updateList(temp);
        textViewNo.setText(temp.size() + " items found!");
    }


    @Override
    public void onRefresh() {
        loadAssignedOrder();
    }

}
