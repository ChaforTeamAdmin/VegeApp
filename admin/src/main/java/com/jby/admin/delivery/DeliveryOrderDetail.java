package com.jby.admin.delivery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jby.admin.R;
import com.jby.admin.adapter.DeliveryOrderDetailExpandableAdapter;
import com.jby.admin.object.ProductDetailChildObject;
import com.jby.admin.object.ProductDetailParentObject;
import com.jby.admin.others.NetworkConnection;
import com.jby.admin.others.NonScrollExpandableListView;
import com.jby.admin.shareObject.ApiDataObject;
import com.jby.admin.shareObject.ApiManager;
import com.jby.admin.shareObject.AsyncTaskManager;
import com.jby.admin.stock.dialog.AssignProductDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.admin.shareObject.CustomToast.CustomToast;
import static com.jby.admin.shareObject.VariableUtils.REFRESH_DELIVERY_ORDER_LIST;
import static com.jby.admin.shareObject.VariableUtils.REFRESH_STOCK_LIST;

public class DeliveryOrderDetail extends AppCompatActivity implements View.OnClickListener, ExpandableListView.OnChildClickListener,
        AssignProductDialog.AssignProductDialogCallBack {
    private NonScrollExpandableListView deliveryOrderDetailActivityListView;
    private DeliveryOrderDetailExpandableAdapter deliveryOrderDetailExpandableAdapter;
    private ArrayList<ProductDetailParentObject> productDetailParentObjectArrayList;

    private Toolbar toolbar;
    private LinearLayout toolBarCustomerLayout;
    //customer detail
    private CardView deliveryOrderDetailActivityCustomerLayout, deliveryOrderDetailActivityProductLayout;
    private TextView deliveryOrderDetailActivityCustomer, deliveryOrderDetailActivityCustomerAddress, deliveryOrderDetailActivityCustomerPhone;
    private String customer_name, customer_id, customer_address;
    //driver detail
    private TextView getDeliveryOrderDetailActivityDriver;
    //print detail
    private TextView deliveryOrderDetailActivityStatus, deliveryOrderDetailActivityPrintStatus;
    //add vege button
    private Button deliveryOrderDetailActivityAddButton;
    //not internet connection
    private RelativeLayout noInternetConnectionLayout;
    //not found layout
    private RelativeLayout notFoundLayout;
    private ImageView notFoundIcon;
    private TextView notFoundLabel;
    //progress bar
    private ProgressBar progressBar;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private String do_id = "", date = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_order_detail);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        toolbar = findViewById(R.id.toolbar);
        toolBarCustomerLayout = findViewById(R.id.actionbar_customer_layout);
        //not found layout
        notFoundLayout = findViewById(R.id.not_found_layout);
        notFoundIcon = findViewById(R.id.not_found_layout_icon);
        notFoundLabel = findViewById(R.id.not_found_layout_label);
        //no internet connection
        noInternetConnectionLayout = findViewById(R.id.no_connection_layout);
        //progress bar
        progressBar = findViewById(R.id.progress_bar);

        deliveryOrderDetailActivityListView = findViewById(R.id.activity_delivery_order_detail_list);
        deliveryOrderDetailActivityCustomerLayout = findViewById(R.id.activity_delivery_order_detail_customer_detail_layout);
        deliveryOrderDetailActivityProductLayout = findViewById(R.id.activity_delivery_order_detail_item_detail_layout);

        deliveryOrderDetailActivityCustomer = findViewById(R.id.activity_delivery_order_detail_customer);
        deliveryOrderDetailActivityCustomerAddress = findViewById(R.id.activity_delivery_order_detail_customer_address);
        deliveryOrderDetailActivityCustomerPhone = findViewById(R.id.activity_delivery_order_detail_customer_phone);
        getDeliveryOrderDetailActivityDriver = findViewById(R.id.activity_delivery_order_detail_driver);
        noInternetConnectionLayout = findViewById(R.id.no_connection_layout);

        deliveryOrderDetailActivityPrintStatus = findViewById(R.id.activity_delivery_order_detail_print_status);
        deliveryOrderDetailActivityStatus = findViewById(R.id.activity_delivery_order_detail_label_status);

        deliveryOrderDetailActivityAddButton = findViewById(R.id.activity_delivery_order_detail_add_button);

        productDetailParentObjectArrayList = new ArrayList<>();
        deliveryOrderDetailExpandableAdapter = new DeliveryOrderDetailExpandableAdapter(this, productDetailParentObjectArrayList);
    }

    private void objectSetting() {
        toolBarCustomerLayout.setVisibility(View.GONE);
        deliveryOrderDetailActivityAddButton.setOnClickListener(this);
        deliveryOrderDetailActivityListView.setAdapter(deliveryOrderDetailExpandableAdapter);
        deliveryOrderDetailActivityListView.setOnChildClickListener(this);
        if (getIntent().getExtras() != null) {
            do_id = getIntent().getExtras().getString("do_id");
            date = getIntent().getExtras().getString("date");
        }
        setupActionBar();
        //not found layout
        setupNotFoundLayout();
        checkInternetConnection(null);
    }

    private void setupActionBar() {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("#DO" + setPlaceHolder(do_id));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private String setPlaceHolder(String do_id) {
        StringBuilder do_idBuilder = new StringBuilder(do_id);
        for (int i = do_idBuilder.length(); i < 5; i++) {
            do_idBuilder.insert(0, "0");
        }
        return do_idBuilder.toString();
    }

    private void setupNotFoundLayout() {
        notFoundIcon.setImageDrawable(getResources().getDrawable(R.drawable.not_found));
        notFoundLabel.setText("No Item Found!");
    }

    public void checkInternetConnection(View view) {
        boolean networkConnection = new NetworkConnection(this).checkNetworkConnection();
        if (networkConnection) fetchParentData();

        noInternetConnectionLayout.setVisibility(networkConnection ? View.GONE : View.VISIBLE);
        deliveryOrderDetailActivityCustomerLayout.setVisibility(networkConnection ? View.VISIBLE : View.GONE);
        deliveryOrderDetailActivityProductLayout.setVisibility(networkConnection ? View.VISIBLE : View.GONE);
        showProgressBar(networkConnection);
    }

    /*
     * listview get parent data
     * */
    /*---------------------------------------------------------------------fetch parent item--------------------------------------------------------------*/
    private void fetchParentData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("do_id", do_id));
                apiDataObjectArrayList.add(new ApiDataObject("detail", "1"));

                asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
                        new ApiManager().delivery,
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
                            Log.d("jsonObject", "jsonObject: " + jsonObjectLoginResponse);
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                setDeliveryOrderDetail(jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("delivery_order_detail"));
                                setupListValue(jsonObjectLoginResponse);
                            }
                        } else {
                            CustomToast(getApplicationContext(), "Network Error!");
                        }

                    } catch (InterruptedException e) {
                        CustomToast(getApplicationContext(), "Interrupted Exception!");
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        CustomToast(getApplicationContext(), "Execution Exception!");
                        e.printStackTrace();
                    } catch (JSONException e) {
                        CustomToast(getApplicationContext(), "JSON Exception!");
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        CustomToast(getApplicationContext(), "Connection Time Out!");
                        e.printStackTrace();
                    }
                }
                setVisibility();
            }
        }).start();
    }

    private void setupListValue(JSONObject jsonObject) {
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("value").getJSONObject(0).getJSONArray("delivery_order_item");
            ;
            for (int i = 0; i < jsonArray.length(); i++) {
                int position = -1;
                String productName = jsonArray.getJSONObject(i).getString("name");

                for (int j = 0; j < productDetailParentObjectArrayList.size(); j++) {
                    //if found mean that same child record under the same date
                    if (productDetailParentObjectArrayList.get(j).getName().equals(productName)) {
                        position = j;
                        //stop looping
                        break;
                    }
                }
                //mean this date is never added yet so create a new group view
                if (position == -1) {
                    productDetailParentObjectArrayList.add(new ProductDetailParentObject(
                            jsonArray.getJSONObject(i).getString("id"),
                            jsonArray.getJSONObject(i).getString("name"),
                            jsonArray.getJSONObject(i).getString("picture"),
                            jsonArray.getJSONObject(i).getString("type"),
                            jsonArray.getJSONObject(i).getString("price"),
                            jsonArray.getJSONObject(i).getString("total_quantity"),
                            setChildArray(jsonArray.getJSONObject(i))));

                    productDetailParentObjectArrayList.get(productDetailParentObjectArrayList.size() - 1).setQuantity(countParentTotalQuantity(productDetailParentObjectArrayList.size() - 1));
                }
                // if the same date(group view) is added then add the child item
                else {
//                    productDetailParentObjectArrayList.get(position).getProductDetailChildObjectArrayList().add(setChildObject(jsonArray.getJSONObject(i)));
                    /*
                     * check same farmer is existed within one product or not
                     * */
                    int childPosition = -1;
                    String farmer = jsonArray.getJSONObject(i).getString("farmer");
                    for (int j = 0; j < productDetailParentObjectArrayList.get(position).getProductDetailChildObjectArrayList().size(); j++) {
                        if (productDetailParentObjectArrayList.get(position).getProductDetailChildObjectArrayList().get(j).getFarmerName().equals(farmer)) {
                            childPosition = j;
                            //stop looping
                            break;
                        }
                    }
                    //create new child
                    if (childPosition == -1)
                        productDetailParentObjectArrayList.get(position).getProductDetailChildObjectArrayList().add(setChildObject(jsonArray.getJSONObject(i)));
                        //if exist then add weight into the same farmer within one product,
                    else {
                        productDetailParentObjectArrayList.get(position).getProductDetailChildObjectArrayList().get(childPosition).joinAllWeight(jsonArray.getJSONObject(i).getString("weight"));
                        productDetailParentObjectArrayList.get(position).getProductDetailChildObjectArrayList().get(childPosition).totalQuantity(jsonArray.getJSONObject(i).getString("total_quantity"));
                    }
                    productDetailParentObjectArrayList.get(position).setQuantity(countParentTotalQuantity(position));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    private String countParentTotalQuantity(int position) {
        int totalQuantity = 0;
        for (int k = 0; k < productDetailParentObjectArrayList.get(position).getProductDetailChildObjectArrayList().size(); k++) {
            totalQuantity = totalQuantity + Integer.valueOf(productDetailParentObjectArrayList.get(position).getProductDetailChildObjectArrayList().get(k).getQuantity());
        }
        return String.valueOf(totalQuantity);
    }


    /*----------------------------------------------------------------------fetch child item---------------------------------------------------------------*/

    private ArrayList<ProductDetailChildObject> setChildArray(JSONObject jsonObject) {
        ArrayList<ProductDetailChildObject> productDetailChildObjectArrayList = new ArrayList<>();
        productDetailChildObjectArrayList.add(setChildObject(jsonObject));
        return productDetailChildObjectArrayList;
    }

    private ProductDetailChildObject setChildObject(JSONObject jsonObject) {
        ProductDetailChildObject object = new ProductDetailChildObject();
        try {
            object.setId(jsonObject.getString("id"));
            object.setWeight(jsonObject.getString("weight"));
            object.setQuantity(jsonObject.getString("total_quantity"));
            object.setFarmerName(jsonObject.getString("farmer"));
            object.setFarmerID(jsonObject.getString("farmer_id"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    private void setVisibility() {
        final boolean show = productDetailParentObjectArrayList.size() > 0;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deliveryOrderDetailActivityListView.setVisibility(show ? View.VISIBLE : View.GONE);
                deliveryOrderDetailActivityCustomer.setVisibility(show ? View.VISIBLE : View.GONE);
                deliveryOrderDetailActivityProductLayout.setVisibility(show ? View.VISIBLE : View.GONE);
                notFoundLayout.setVisibility(show ? View.GONE : View.VISIBLE);
                showProgressBar(false);

                notifyDataSetChanged();
            }
        });
    }

    private void notifyDataSetChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deliveryOrderDetailExpandableAdapter.notifyDataSetChanged();
            }
        });
    }

    /*
     * setup delivery order detail
     * */
    private void setDeliveryOrderDetail(final JSONArray jsonArray) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    customer_id = jsonArray.getJSONObject(0).getString("customer_id");
                    customer_name = jsonArray.getJSONObject(0).getString("customer");
                    customer_address = jsonArray.getJSONObject(0).getString("customer_address");

                    deliveryOrderDetailActivityCustomer.setText(customer_name);
                    deliveryOrderDetailActivityCustomerAddress.setText(customer_address);
                    deliveryOrderDetailActivityCustomerPhone.setText(jsonArray.getJSONObject(0).getString("customer_phone"));

                    getDeliveryOrderDetailActivityDriver.setText(jsonArray.getJSONObject(0).getString("driver"));

                    deliveryOrderDetailActivityPrintStatus.setText(jsonArray.getJSONObject(0).getString("print_status").equals("1") ? "Printed" : "Not Print Yet");
                    deliveryOrderDetailActivityPrintStatus.setTextColor(jsonArray.getJSONObject(0).getString("print_status").equals("1") ? getResources().getColor(R.color.blue) : getResources().getColor(R.color.green));

                    deliveryOrderDetailActivityStatus.setText(jsonArray.getJSONObject(0).getString("status").equals("2") ? "Delivered" : "Processing");
                    deliveryOrderDetailActivityStatus.setTextColor(jsonArray.getJSONObject(0).getString("status").equals("2") ? getResources().getColor(R.color.green) : getResources().getColor(R.color.blue));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /*
     * intent to stock fragment for adding item
     * */
    private void addVegetable() {
        Intent data = new Intent();
        data.putExtra("id", customer_id);
        data.putExtra("name", customer_name);
        data.putExtra("address", customer_address);
        data.putExtra("do_id", do_id);
        setResult(REFRESH_STOCK_LIST, data);
        onBackPressed();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_delivery_order_detail_add_button:
                addVegetable();
                break;
        }
    }

    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
        Bundle bundle = new Bundle();
        bundle.putString("do_id", do_id);
        bundle.putString("product_id", productDetailParentObjectArrayList.get(i).getId());
        bundle.putString("weight", productDetailParentObjectArrayList.get(i).getProductDetailChildObjectArrayList().get(i1).getWeight());
        bundle.putString("farmer_name", productDetailParentObjectArrayList.get(i).getProductDetailChildObjectArrayList().get(i1).getFarmerName());
        bundle.putString("farmer_id", productDetailParentObjectArrayList.get(i).getProductDetailChildObjectArrayList().get(i1).getFarmerID());
        bundle.putString("from_where", "delivery_order_detail");

        DialogFragment dialogFragment = new AssignProductDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getSupportFragmentManager(), "");
        return false;
    }

    /*---------------------------------------------------------------------delete do----------------------------------------------------------------------------*/
    public void deleteReceiveOrderConfirmation(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("Are you sure that you want to this purchase order? \nOnce delete can't be undone!");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int i) {
                        showProgressBar(true);
                        deleteDO();
                        dialog.cancel();
                    }
                });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /*---------------------------------------------------------------------fetch parent item--------------------------------------------------------------*/
    private void deleteDO() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("do_id", do_id));
                apiDataObjectArrayList.add(new ApiDataObject("delete", "1"));

                asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
                        new ApiManager().delivery,
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
                            Log.d("jsonObject", "jsonObject: " + jsonObjectLoginResponse);
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                CustomToast(getApplicationContext(), "Delete Successfully");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setResult(REFRESH_DELIVERY_ORDER_LIST);
                                        onBackPressed();
                                    }
                                });
                            }
                        } else {
                            CustomToast(getApplicationContext(), "Network Error!");
                        }

                    } catch (InterruptedException e) {
                        CustomToast(getApplicationContext(), "Interrupted Exception!");
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        CustomToast(getApplicationContext(), "Execution Exception!");
                        e.printStackTrace();
                    } catch (JSONException e) {
                        CustomToast(getApplicationContext(), "JSON Exception!");
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        CustomToast(getApplicationContext(), "Connection Time Out!");
                        e.printStackTrace();
                    }
                }
                showProgressBar(false);
            }
        }).start();
    }

    /*--------------------------------------------------------------------assign dialog call back----------------------------------------------------------------------------*/
    @Override
    public void updateListViewQuantity(String status) {

    }

    @Override
    public void setDeliveryProductIDList(String id, String deliveryOrderID) {

    }

    @Override
    public void reset() {
        productDetailParentObjectArrayList.clear();
        checkInternetConnection(null);
    }

    @Override
    public void orderByPriority() {

    }

    @Override
    public void updateUnavailableStockArrayList(String selectedID, String farmerID) {

    }

    /*------------------------------------------------------------other----------------------------------------------------------------------------------*/
    private void showProgressBar(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
