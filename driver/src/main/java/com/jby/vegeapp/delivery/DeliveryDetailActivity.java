package com.jby.vegeapp.delivery;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.jby.vegeapp.R;
import com.jby.vegeapp.adapter.DeliveryExpandableAdapter;
import com.jby.vegeapp.delivery.dialog.DelivertProductListDialog;
import com.jby.vegeapp.object.CustomerObject;
import com.jby.vegeapp.object.ProductChildObject;
import com.jby.vegeapp.object.ProductParentObject;
import com.jby.vegeapp.others.NetworkConnection;
import com.jby.vegeapp.others.NonScrollExpandableListView;
import com.jby.vegeapp.shareObject.ApiDataObject;
import com.jby.vegeapp.shareObject.ApiManager;
import com.jby.vegeapp.shareObject.AsyncTaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.vegeapp.shareObject.CustomToast.CustomToast;
import static com.jby.vegeapp.shareObject.VariableUtils.MY_PERMISSIONS_REQUEST_PHONE_CALL;
import static com.jby.vegeapp.shareObject.VariableUtils.UPDATE_LIST;

public class DeliveryDetailActivity extends AppCompatActivity implements DeliveryExpandableAdapter.ProductExpandableAdapterCallBack,
        ExpandableListView.OnChildClickListener {
    private NonScrollExpandableListView deliveryDetailActivityListView;
    private DeliveryExpandableAdapter deliveryDetailExpandableAdapter;
    private ArrayList<ProductParentObject> productObjectArrayList;
    private Toolbar toolbar;
    //farmer detail
    private CardView deliveryDetailActivityCustomerLayout, deliveryDetailActivityProductLayout;
    private TextView deliveryDetailActivityCustomer, deliveryDetailActivityCustomerAddress, deliveryDetailActivityCustomerPhone;
    //not internet connection
    private RelativeLayout noInternetConnectionLayout;
    //not found layout
    private RelativeLayout notFoundLayout;
    private ImageView notFoundIcon;
    private TextView notFoundLabel;
    //progress bar
    private ProgressBar progressBar;

    private CustomerObject customerObject;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_detail);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        toolbar = findViewById(R.id.toolbar);
        //not found layout
        notFoundLayout = findViewById(R.id.not_found_layout);
        notFoundIcon = findViewById(R.id.not_found_layout_icon);
        notFoundLabel = findViewById(R.id.not_found_layout_label);
        //no internet connection
        noInternetConnectionLayout = findViewById(R.id.no_connection_layout);
        //progress bar
        progressBar = findViewById(R.id.progress_bar);

        deliveryDetailActivityListView = findViewById(R.id.activity_delivery_detail_list);
        deliveryDetailActivityCustomerLayout = findViewById(R.id.activity_delivery_detail_customer_detail_layout);
        deliveryDetailActivityProductLayout = findViewById(R.id.activity_delivery_detail_item_detail_layout);

        deliveryDetailActivityCustomer = findViewById(R.id.activity_delivery_detail_customer);
        deliveryDetailActivityCustomerAddress = findViewById(R.id.activity_delivery_detail_customer_address);
        deliveryDetailActivityCustomerPhone = findViewById(R.id.activity_delivery_detail_customer_phone);
        noInternetConnectionLayout = findViewById(R.id.no_connection_layout);

        productObjectArrayList = new ArrayList<>();
        deliveryDetailExpandableAdapter = new DeliveryExpandableAdapter(this, productObjectArrayList, this);
    }

    private void objectSetting() {
        deliveryDetailActivityListView.setAdapter(deliveryDetailExpandableAdapter);
        deliveryDetailActivityListView.setOnChildClickListener(this);
        if (getIntent().getExtras() != null) {
            customerObject = (CustomerObject) getIntent().getExtras().getSerializable("customer");
            deliveryDetailActivityCustomerAddress.setText(customerObject.getAddress());
            deliveryDetailActivityCustomerPhone.setText(customerObject.getPhone());
            deliveryDetailActivityCustomer.setText(customerObject.getName());
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
        getSupportActionBar().setTitle("Customer");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void setupNotFoundLayout() {
        notFoundIcon.setImageDrawable(getResources().getDrawable(R.drawable.no_item_to_deliver_icon));
        notFoundLabel.setText("No item to delivey!");
    }

    public void checkInternetConnection(View view) {
        boolean networkConnection = new NetworkConnection(this).checkNetworkConnection();
        if (networkConnection) fetchParentData();

        noInternetConnectionLayout.setVisibility(networkConnection ? View.GONE : View.VISIBLE);
        deliveryDetailActivityCustomerLayout.setVisibility(networkConnection ? View.VISIBLE : View.GONE);
        deliveryDetailActivityProductLayout.setVisibility(networkConnection ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(networkConnection ? View.VISIBLE : View.GONE);
    }

    /*---------------------------------------------------------------------fetch parent item--------------------------------------------------------------*/
    private void fetchParentData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("customer_id", customerObject.getId()));

                asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
                        new ApiManager().deliver,
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
                                setUpDeliveryListValue(jsonObjectLoginResponse);
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

    private void setUpDeliveryListValue(JSONObject jsonObject) {
        //close all group
//        closeOtherChildView(-1);
        if (productObjectArrayList.size() > 0) productObjectArrayList.clear();
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("value").getJSONObject(0).getJSONArray("customer_item");
            ;
            for (int i = 0; i < jsonArray.length(); i++) {
                int position = -1;
                String productName = jsonArray.getJSONObject(i).getString("name");

                for (int j = 0; j < productObjectArrayList.size(); j++) {
                    //if found mean that same child record under the same date
                    if (productObjectArrayList.get(j).getName().equals(productName)) {
                        position = j;
                        //stop looping
                        break;
                    }
                }
                //mean this date is never added yet so create a new group view
                if (position == -1) {
                    productObjectArrayList.add(new ProductParentObject(
                            jsonArray.getJSONObject(i).getString("id"),
                            jsonArray.getJSONObject(i).getString("picture"),
                            jsonArray.getJSONObject(i).getString("name"),
                            jsonArray.getJSONObject(i).getString("total_quantity"),
                            jsonArray.getJSONObject(i).getString("type"),
                            setChildArray(jsonArray.getJSONObject(i))));
                }
                // if the same date(group view) is added then add the child item
                else {
                    productObjectArrayList.get(position).getProductChildObjectArrayList().add(setChildObject(jsonArray.getJSONObject(i)));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    private void setVisibility() {
        final boolean show = productObjectArrayList.size() > 0;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deliveryDetailActivityListView.setVisibility(show ? View.VISIBLE : View.GONE);
                deliveryDetailActivityCustomerLayout.setVisibility(show ? View.VISIBLE : View.GONE);
                deliveryDetailActivityProductLayout.setVisibility(show ? View.VISIBLE : View.GONE);
                notFoundLayout.setVisibility(show ? View.GONE : View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                notifyDataSetChanged();
            }
        });
    }

    private void notifyDataSetChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deliveryDetailExpandableAdapter.notifyDataSetChanged();
            }
        });
    }

    /*----------------------------------------------------------------------fetch child item---------------------------------------------------------------*/

    private ArrayList<ProductChildObject> setChildArray(JSONObject jsonObject) {
        ArrayList<ProductChildObject> transactionChildObjectArrayList = new ArrayList<>();
        try {
            transactionChildObjectArrayList.add(new ProductChildObject(
                    jsonObject.getString("id"),
                    jsonObject.getString("weight"),
                    jsonObject.getString("total_quantity"),
                    jsonObject.getString("farmer"),
                    "deliver"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return transactionChildObjectArrayList;
    }

    private ProductChildObject setChildObject(JSONObject jsonObject) {
        ProductChildObject object = null;
        try {
            object = new ProductChildObject(
                    jsonObject.getString("id"),
                    jsonObject.getString("weight"),
                    jsonObject.getString("total_quantity"),
                    jsonObject.getString("farmer"),
                    "deliver");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int j, long l) {
        Bundle bundle = new Bundle();
        bundle.putString("product", productObjectArrayList.get(i).getName());
        bundle.putString("farmer", productObjectArrayList.get(i).getProductChildObjectArrayList().get(j).getFarmer());
        bundle.putString("product_id", productObjectArrayList.get(i).getId());
        bundle.putString("product_pic", productObjectArrayList.get(i).getPicture());
        bundle.putString("weight", productObjectArrayList.get(i).getProductChildObjectArrayList().get(j).getWeight());
        bundle.putString("customer_id", customerObject.getId());

        DialogFragment dialogFragment = new DelivertProductListDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getSupportFragmentManager(), "");
        return true;
    }

    /*------------------------------------------------------------------complete---------------------------------------------------------------------------*/
    public void confirmationDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("Are you sure that you have finished your delivery?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        missionComplete();
                        dialog.cancel();
                    }
                });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void missionComplete() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("customer_id", customerObject.getId()));
                apiDataObjectArrayList.add(new ApiDataObject("complete", "1"));
                asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
                        new ApiManager().deliver,
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
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                CustomToast(getApplicationContext(), "Delivery Successfully!");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
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
            }
        }).start();
    }

    /*-------------------------------------------------------------navigation purpose----------------------------------------------------------------------*/
    public void openMap(View view) {
        String uri = "";
        Intent intent;
        LatLng latLng = getLatLng(customerObject.getAddress());
        try {
            uri = String.format(Locale.ENGLISH, "geo:" + latLng.latitude + "," + latLng.longitude + "?q=" + latLng.latitude + "," + latLng.longitude + " ( " + customerObject.getName() + " )");
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            try {
                Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(unrestrictedIntent);
            } catch (ActivityNotFoundException innerEx) {
                showSnackBar("This device is not support this action!");
            }
        }
    }

    public LatLng getLatLng(String strAddress) {
        Geocoder coder = new Geocoder(this);
        List<Address> address;
        LatLng p1 = null;
        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return p1;
    }

    /*------------------------------------------------------call permission-----------------------------------------------------------------*/
    public void phoneCallPermission(View view) {
        if (checkPhoneCallPermission()) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED) {
                onCall();
            }
        }
    }

    public void onCall() {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + customerObject.getPhone()));    //this is the phone number calling

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            //request permission from user if the app hasn't got the required permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},   //request specific permission from user
                    MY_PERMISSIONS_REQUEST_PHONE_CALL);
        } else {     //have got permission
            try {
                startActivity(callIntent);  //call activity and make phone call
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "Invalid Number", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean checkPhoneCallPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CALL_PHONE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_PHONE_CALL);

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        MY_PERMISSIONS_REQUEST_PHONE_CALL);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_PHONE_CALL: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.CALL_PHONE)
                            == PackageManager.PERMISSION_GRANTED) {
                        //Request location updates:
                        onCall();
                    }
                } else {
                    Toast.makeText(this, "Unable to make a phone call with permission!", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void openProductDetail() {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(UPDATE_LIST);
    }

    public void showSnackBar(String message) {
        final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }
}
