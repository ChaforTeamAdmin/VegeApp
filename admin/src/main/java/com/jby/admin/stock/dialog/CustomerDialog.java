package com.jby.admin.stock.dialog;

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
import android.widget.Toast;

import com.jby.admin.R;
import com.jby.admin.adapter.CustomerAdapter;
import com.jby.admin.database.FrameworkClass;
import com.jby.admin.object.CustomerObject;
import com.jby.admin.others.ExpandableHeightListView;
import com.jby.admin.shareObject.ApiDataObject;
import com.jby.admin.shareObject.ApiManager;
import com.jby.admin.shareObject.AsyncTaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CustomerDialog extends DialogFragment implements SearchView.OnQueryTextListener, AdapterView.OnItemClickListener {
    View rootView;
    private SearchView customerDialogSearch;
    private ExpandableHeightListView customerList;
    private ArrayList<CustomerObject> customerObjectArrayList;
    private CustomerAdapter customerAdapter;
    
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
        rootView = inflater.inflate(R.layout.customer_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        customerDialogSearch = rootView.findViewById(R.id.farmer_dialog_search);
        customerList = rootView.findViewById(R.id.farmer_dialog_farmer_list);
        
        customerObjectArrayList = new ArrayList<>();

        customerAdapter = new CustomerAdapter(getActivity(), customerObjectArrayList);
        
        handler = new Handler();
        customerDialogCallBack = (CustomerDialogCallBack) getActivity();
    }

    private void objectSetting() {
        customerList.setAdapter(customerAdapter);
        customerList.setExpanded(true);

        customerDialogSearch.setOnQueryTextListener(this);

        customerList.setOnItemClickListener(this);
        Bundle bundle = getArguments();
        
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchAllFarmer();
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
                            customerObjectArrayList.add(new CustomerObject(
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
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        searchFromArrayList(query);
        return false;
    }

    private void searchFromArrayList(String query){
        ArrayList<CustomerObject> searchList = new ArrayList<>();
        for(int i = 0 ; i < customerObjectArrayList.size(); i++){
            if(customerObjectArrayList.get(i).getName().contains(query)) {
              searchList.add(customerObjectArrayList.get(i));
            }
        }
        customerAdapter = new CustomerAdapter(getActivity(), searchList);
        customerList.setAdapter(customerAdapter);
        customerAdapter.notifyDataSetChanged();
    }
    
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        customerDialogCallBack.selectedItem(customerObjectArrayList.get(i).getName(), customerObjectArrayList.get(i).getId(), customerObjectArrayList.get(i).getAddress());
        dismiss();
    }
    
    public interface CustomerDialogCallBack{
        void selectedItem(String name, String id, String address);
    }
}
