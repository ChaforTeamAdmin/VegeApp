package com.jby.admin.customer;

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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jby.admin.R;
import com.jby.admin.adapter.CustomerAdapter;
import com.jby.admin.database.CustomSqliteHelper;
import com.jby.admin.database.FrameworkClass;
import com.jby.admin.database.ResultCallBack;
import com.jby.admin.object.entity.CustomerObject;
import com.jby.admin.others.ExpandableHeightListView;
import com.jby.admin.others.NetworkConnection;
import com.jby.admin.others.SwipeDismissTouchListener;
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

import static com.jby.admin.database.CustomSqliteHelper.TB_CUSTOMER;
import static com.jby.admin.database.CustomSqliteHelper.TB_FAVOURITE_CUSTOMER;
import static com.jby.admin.shareObject.CustomToast.CustomToast;

public class CustomerDialog extends DialogFragment implements SearchView.OnQueryTextListener, AdapterView.OnItemClickListener, ResultCallBack {
    View rootView;
    private SearchView customerDialogSearch;
    private ExpandableHeightListView customerList;
    private ArrayList<CustomerObject> customerObjectArrayList;
    private CustomerAdapter customerAdapter;
    private boolean isCustomerList = true;
    //favourite list
    private TextView farmDialogLabelFavouriteCustomer;
    private ExpandableHeightListView favouriteCustomerList;
    private ArrayList<CustomerObject> favouriteCustomerArrayList;
    private CustomerAdapter favouriteCustomerAdapter;
    //not found layout
    private RelativeLayout notFoundLayout;
    private ImageView notFoundIcon;
    private TextView notFoundLabel;
    //progress bar
    private ProgressBar progressBar;

    private FrameworkClass tbFavouriteCustomer, tbCustomer;

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
        //not found layout
        notFoundLayout = rootView.findViewById(R.id.not_found_layout);
        notFoundIcon = rootView.findViewById(R.id.not_found_layout_icon);
        notFoundLabel = rootView.findViewById(R.id.not_found_layout_label);

        customerDialogSearch = rootView.findViewById(R.id.customer_dialog_search);
        customerList = rootView.findViewById(R.id.customer_dialog_customer_list);

        progressBar = rootView.findViewById(R.id.progress_bar);

        farmDialogLabelFavouriteCustomer = rootView.findViewById(R.id.customer_dialog_label_recent_choose);
        favouriteCustomerList = rootView.findViewById(R.id.customer_dialog_favourite_customer_list);

        favouriteCustomerArrayList = new ArrayList<>();
        customerObjectArrayList = new ArrayList<>();

        customerAdapter = new CustomerAdapter(getActivity(), customerObjectArrayList);
        favouriteCustomerAdapter = new CustomerAdapter(getActivity(), favouriteCustomerArrayList);

        handler = new Handler();
        customerDialogCallBack = (CustomerDialogCallBack) getParentFragment();
    }

    private void objectSetting() {
        customerList.setAdapter(customerAdapter);
        customerList.setExpanded(true);

        favouriteCustomerList.setAdapter(favouriteCustomerAdapter);
        favouriteCustomerList.setExpanded(true);

        customerDialogSearch.setOnQueryTextListener(this);

        customerList.setOnItemClickListener(this);
        favouriteCustomerList.setOnItemClickListener(this);
        setupNotFoundLayout();

        tbFavouriteCustomer = new FrameworkClass(getActivity(), this, new CustomSqliteHelper(getActivity()), TB_FAVOURITE_CUSTOMER);
        tbCustomer = new FrameworkClass(getActivity(), this, new CustomSqliteHelper(getActivity()), TB_CUSTOMER);

        checkingNetwork();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                readFavouriteCustomerFromLocal();

            }
        }, 50);
    }

    private void checkingNetwork(){
        if (new NetworkConnection(getActivity()).checkNetworkConnection()) {
            fetchAllCustomer();
        }
        else{
            readDataFromLocal("");
        }
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

    /*------------------------------------------------------------customer list purpose---------------------------------------------------------------------------*/
    private void fetchAllCustomer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("read", "1"));

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
                            Log.d("jsonObject", "product List farmer: " + jsonObjectLoginResponse);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                            tbCustomer.new Delete().perform();
                                            JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("customer");
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                tbCustomer.new create("customer_id, name, phone, address, created_at",
                                                        new String[]{
                                                                jsonArray.getJSONObject(i).getString("id"),
                                                                jsonArray.getJSONObject(i).getString("name"),
                                                                jsonArray.getJSONObject(i).getString("phone"),
                                                                jsonArray.getJSONObject(i).getString("address"),
                                                                (String) android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date())
                                                        }).perform();
                                            }
                                            readDataFromLocal("");
                                        }
                                    } catch (JSONException e) {
                                        CustomToast(getActivity(), "JSON Exception!");
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } else {
                            CustomToast(getActivity(), "Network Error!");
                        }
                    } catch (InterruptedException e) {
                        CustomToast(getActivity(), "Interrupted Exception!");
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        CustomToast(getActivity(), "Execution Exception!");
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        CustomToast(getActivity(), "Connection Time Out!");
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void readDataFromLocal(String query) {
        //read product list after stored
        tbCustomer.new Read("*")
                .where("name LIKE '%" + query + "%' OR address LIKE '%" + query + "%'")
                .perform();
    }

    private void setUpCustomerList(String result) {
        try {
            JSONArray jsonArray = new JSONObject(result).getJSONArray("result");
            if (jsonArray.length() > 0)
                for (int i = 0; i < jsonArray.length(); i++) {
                    customerObjectArrayList.add(new CustomerObject(
                            jsonArray.getJSONObject(i).getString("customer_id"),
                            jsonArray.getJSONObject(i).getString("name"),
                            jsonArray.getJSONObject(i).getString("phone"),
                            jsonArray.getJSONObject(i).getString("address")));
                }
            showProgressBar(false);
            customerAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        showNotFoundLayout();
    }

    /*----------------------------------------------------------------------favourite setting----------------------------------------------------------------------*/
    private void readFavouriteCustomerFromLocal() {
        //read product list after stored
        isCustomerList = false;
        tbFavouriteCustomer.new Read("*").orderByDesc("customer_id").perform();
    }

    private void storeFavouriteList(CustomerObject customerObject) {
        tbFavouriteCustomer.new create("customer_id, name, phone, address, created_at", new String[]{
                customerObject.getId(),
                customerObject.getName(),
                customerObject.getPhone(),
                customerObject.getAddress(),
                (String) android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date())
        }).perform();
    }

    private void setUpFavouriteList(String result) {
        int count = 0;
        try {
            JSONArray jsonArray = (new JSONObject(result).getJSONArray("result"));
            for (int i = 0; i < jsonArray.length(); i++) {
                if (favouriteCustomerArrayList.size() < 3) {
                    //add item into favouriteCustomerArrayList when size = 0
                    if (favouriteCustomerArrayList.size() <= 0) {
                        favouriteCustomerArrayList.add(new CustomerObject(
                                jsonArray.getJSONObject(i).getString("customer_id"),
                                jsonArray.getJSONObject(i).getString("name"),
                                jsonArray.getJSONObject(i).getString("phone"),
                                jsonArray.getJSONObject(i).getString("address")
                        ));
                    }
                    //favouriteCustomerArrayList.size > 0
                    else {
                        //check repeat values
                        for (int j = 0; j < favouriteCustomerArrayList.size(); j++) {
                            if (!favouriteCustomerArrayList.get(j).getName().equals(jsonArray.getJSONObject(i).getString("name")))
                                count++;
                        }
                        //if count == favourite.size() mean that one is new item
                        if (count == favouriteCustomerArrayList.size())
                            favouriteCustomerArrayList.add(new CustomerObject(
                                    jsonArray.getJSONObject(i).getString("customer_id"),
                                    jsonArray.getJSONObject(i).getString("name"),
                                    jsonArray.getJSONObject(i).getString("phone"),
                                    jsonArray.getJSONObject(i).getString("address")
                            ));
                        count = 0;
                    }
                } else break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
        isCustomerList = true;
    }

    /*--------------------------------------------------------------------search purpose-------------------------------------------------------------------*/
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(final String query) {
        showProgressBar(true);
        //hide favourite list
        hide(query.length() > 0);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                reset();
                readDataFromLocal(query);
            }
        }, 150);
        return false;
    }

    /*----------------------------------------------------------------------share setting----------------------------------------------------------------------*/
    private void reset() {
        customerObjectArrayList.clear();
        notifyDataSetChanged();
    }

    private void showProgressBar(final boolean show) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void setupNotFoundLayout() {
        notFoundIcon.setImageDrawable(getResources().getDrawable(R.drawable.not_found));
        notFoundLabel.setText("No customer is found from local!");
    }

    private void showNotFoundLayout() {
        notFoundLayout.setVisibility(customerObjectArrayList.size() > 0 ? View.GONE : View.VISIBLE);
    }

    private void notifyDataSetChanged() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                favouriteCustomerAdapter.notifyDataSetChanged();
                customerAdapter.notifyDataSetChanged();
            }
        });
    }

    private void hide(boolean hide) {
        favouriteCustomerList.setVisibility(hide ? View.GONE : View.VISIBLE);
        farmDialogLabelFavouriteCustomer.setVisibility(hide ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.customer_dialog_favourite_customer_list:
                customerDialogCallBack.selectedCustomer(favouriteCustomerArrayList.get(i));
                storeFavouriteList(favouriteCustomerArrayList.get(i));

                break;
            case R.id.customer_dialog_customer_list:
                customerDialogCallBack.selectedCustomer(customerObjectArrayList.get(i));
                storeFavouriteList(customerObjectArrayList.get(i));
                break;
        }
        dismiss();
    }


    /*-----------------------------------------------------------------------------sqlite purpose--------------------------------------------------------------*/
    @Override
    public void createResult(String status) {
    }

    @Override
    public void readResult(String result) {
        /*
         * for customer favourite list purpose
         * */
        if (!isCustomerList) {
            setUpFavouriteList(result);
        }
        /*
         * for offline customer list purpose
         * */
        else {
            setUpCustomerList(result);
        }
    }

    @Override
    public void updateResult(String status) {

    }

    @Override
    public void deleteResult(String status) {

    }

    public interface CustomerDialogCallBack {
        void selectedCustomer(CustomerObject customerObject);
    }
}
