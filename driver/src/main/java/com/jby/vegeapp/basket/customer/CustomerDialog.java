package com.jby.vegeapp.basket.customer;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.vegeapp.R;
import com.jby.vegeapp.adapter.FarmerAdapter;
import com.jby.vegeapp.database.CustomSqliteHelper;
import com.jby.vegeapp.database.FrameworkClass;
import com.jby.vegeapp.database.ResultCallBack;
import com.jby.vegeapp.object.FarmerObject;
import com.jby.vegeapp.others.ExpandableHeightListView;
import com.jby.vegeapp.others.SwipeDismissTouchListener;
import com.jby.vegeapp.shareObject.ApiDataObject;
import com.jby.vegeapp.shareObject.ApiManager;
import com.jby.vegeapp.shareObject.AsyncTaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.vegeapp.database.CustomSqliteHelper.TB_BASKET_FAVOURITE_CUSTOMER;


public class CustomerDialog extends DialogFragment implements SearchView.OnQueryTextListener, AdapterView.OnItemClickListener, ResultCallBack {
    View rootView;
    private SearchView customerDialogSearch;
    private ExpandableHeightListView customerList;
    private ArrayList<FarmerObject> customerObjectArrayList;
    private FarmerAdapter customerAdapter;
    private TextView customerDialogLabelFarmer;
    //favourite list
    private TextView customerDialogLabelFavouriteFarmer;
    private ExpandableHeightListView favouriteCustomerList;
    private ArrayList<FarmerObject> favouriteCustomerArrayList;
    private FarmerAdapter favouriteCustomerAdapter;

    private FrameworkClass frameworkClass;

    private Handler handler;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;

    public CustomerDialogCallBack customerDialogCallBack;

    public CustomerDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.farmer_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        customerDialogSearch = rootView.findViewById(R.id.farmer_dialog_search);
        customerList = rootView.findViewById(R.id.farmer_dialog_farmer_list);

        customerDialogLabelFavouriteFarmer = rootView.findViewById(R.id.farmer_dialog_label_recent_choose);
        favouriteCustomerList = rootView.findViewById(R.id.farmer_dialog_favourite_farmer_list);

        favouriteCustomerArrayList = new ArrayList<>();
        customerObjectArrayList = new ArrayList<>();

        customerAdapter = new FarmerAdapter(getActivity(), customerObjectArrayList);
        favouriteCustomerAdapter = new FarmerAdapter(getActivity(), favouriteCustomerArrayList);

        handler = new Handler();
        customerDialogCallBack = (CustomerDialogCallBack) getActivity();
    }

    private void objectSetting() {
        customerList.setAdapter(customerAdapter);
        customerList.setExpanded(true);

        favouriteCustomerList.setAdapter(favouriteCustomerAdapter);
        favouriteCustomerList.setExpanded(true);

        customerDialogSearch.setOnQueryTextListener(this);

        customerList.setOnItemClickListener(this);
        favouriteCustomerList.setOnItemClickListener(this);

        Bundle bundle = getArguments();
        //check whether open from pick up activity or basket activity
        frameworkClass = new FrameworkClass(getActivity(), this, new CustomSqliteHelper(getActivity()), TB_BASKET_FAVOURITE_CUSTOMER);
        
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchAllFarmer();
                frameworkClass.new Read("*").orderByDesc("id").perform();
            }
        },200);
    }

    private void fetchAllFarmer(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("fetch", "1"));

        asyncTaskManager = new AsyncTaskManager(
                getActivity(),
                new ApiManager().customer,
                new ApiManager().getResultParameter(
                        "",
                        new ApiManager().setData(apiDataObjectArrayList),
                        ""
                )
        );
        asyncTaskManager.execute();

        if (!asyncTaskManager.isCancelled()) {

            try {
                jsonObjectLoginResponse = asyncTaskManager.get(30000, TimeUnit.MILLISECONDS);

                if (jsonObjectLoginResponse != null) {
                    Log.d("jsonObject", "jsonObject: " +  jsonObjectLoginResponse);
                    if(jsonObjectLoginResponse.getString("status").equals("1")){
                        JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("customer");
                        for(int i = 0; i < jsonArray.length(); i++)
                        {
                            customerObjectArrayList.add(new FarmerObject(
                                    jsonArray.getJSONObject(i).getString("id"),
                                    jsonArray.getJSONObject(i).getString("name"),
                                    jsonArray.getJSONObject(i).getString("phone"),
                                    jsonArray.getJSONObject(i).getString("address")));
                        }
                    }
                }
                else {
                    Toast.makeText(getActivity(), "Network Error!", Toast.LENGTH_SHORT).show();
                }

            } catch (InterruptedException e) {
                Toast.makeText(getActivity(), "Interrupted Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (ExecutionException e) {
                Toast.makeText(getActivity(), "Execution Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (JSONException e) {
                Toast.makeText(getActivity(), "JSON Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (TimeoutException e) {
                Toast.makeText(getActivity(), "Connection Time Out!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        customerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            Objects.requireNonNull(d.getWindow()).setLayout(width, height);
            d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            d.getWindow().setWindowAnimations(R.style.dialog_up_down);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Dialog d = getDialog();
        Objects.requireNonNull(d.getWindow()).getDecorView().setOnTouchListener(new SwipeDismissTouchListener(d.getWindow().getDecorView(), null,
                new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(Object token) {
                        return true;
                    }

                    @Override
                    public void onDismiss(View view, Object token) {
                        dismiss();
                    }
                }));
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        //hide favourite list
        if(query.length() > 0) hide(true);
        else hide(false);

        searchFromArrayList(query);
        return false;
    }

    private void searchFromArrayList(String query){
        ArrayList<FarmerObject> searchList = new ArrayList<>();
        for(int i = 0 ; i < customerObjectArrayList.size(); i++){
            if(customerObjectArrayList.get(i).getName().contains(query)) {
              searchList.add(customerObjectArrayList.get(i));
            }
        }
        customerAdapter = new FarmerAdapter(getActivity(), searchList);
        customerList.setAdapter(customerAdapter);
        customerAdapter.notifyDataSetChanged();
    }

    private void hide(boolean hide){
        if(hide){
            favouriteCustomerList.setVisibility(View.GONE);
            customerDialogLabelFavouriteFarmer.setVisibility(View.GONE);
        }
        else {
            favouriteCustomerList.setVisibility(View.VISIBLE);
            customerDialogLabelFavouriteFarmer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.farmer_dialog_favourite_farmer_list:
                Log.d("haha","customer: " + favouriteCustomerArrayList.get(i).getId());
                customerDialogCallBack.selectedItem(favouriteCustomerArrayList.get(i).getName(), favouriteCustomerArrayList.get(i).getId(), favouriteCustomerArrayList.get(i).getAddress(), favouriteCustomerArrayList.get(i).getPhone());
                break;
            case R.id.farmer_dialog_farmer_list:
                customerDialogCallBack.selectedItem(customerObjectArrayList.get(i).getName(), customerObjectArrayList.get(i).getId(), customerObjectArrayList.get(i).getAddress(), customerObjectArrayList.get(i).getPhone());
                break;
        }
        dismiss();
    }

    @Override
    public void createResult(String status) {

    }

    @Override
    public void readResult(String result) {
        Log.d("haha", "haha: " + result);
        JSONObject jsonObject = null;
        int count = 0;
        try {
            jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("result");

            for(int i = 0; i < jsonArray.length(); i++){
                if(favouriteCustomerArrayList.size() < 3){
                    Log.d("haha", "haha: big loop");
                    //add item into favouriteFarmerArrayList when size = 0
                    if(favouriteCustomerArrayList.size() <= 0){
                        favouriteCustomerArrayList.add(new FarmerObject(
                                jsonArray.getJSONObject(i).getString("customer_id"),
                                jsonArray.getJSONObject(i).getString("name"),
                                "",
                                jsonArray.getJSONObject(i).getString("address")
                        ));
                    }
                    //favouriteFarmerArrayList.size > 0
                    else{
                        //check repeat values
                        for(int j = 0; j < favouriteCustomerArrayList.size(); j++){
                            if(!favouriteCustomerArrayList.get(j).getName().equals(jsonArray.getJSONObject(i).getString("name"))) count++;
                        }
                        //if count == favourite.size() mean that one is new item
                        if(count == favouriteCustomerArrayList.size())
                            favouriteCustomerArrayList.add(new FarmerObject(
                                    jsonArray.getJSONObject(i).getString("customer_id"),
                                    jsonArray.getJSONObject(i).getString("name"),
                                    "",
                                    jsonArray.getJSONObject(i).getString("address")
                            ));
                        count = 0;
                    }
                }
                else break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        favouriteCustomerAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateResult(String status) {

    }

    @Override
    public void deleteResult(String status) {

    }

    public interface CustomerDialogCallBack{
        void selectedItem(String name, String id, String address, String phone);
    }
}
