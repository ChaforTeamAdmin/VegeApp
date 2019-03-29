package com.jby.vegeapp.delivery;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jby.vegeapp.R;
import com.jby.vegeapp.adapter.DeliveryCustomerAdapter;
import com.jby.vegeapp.object.CustomerObject;
import com.jby.vegeapp.others.ExpandableHeightListView;
import com.jby.vegeapp.others.NetworkConnection;
import com.jby.vegeapp.shareObject.ApiDataObject;
import com.jby.vegeapp.shareObject.ApiManager;
import com.jby.vegeapp.shareObject.AsyncTaskManager;
import com.jby.vegeapp.sharePreference.SharedPreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.vegeapp.shareObject.CustomToast.CustomToast;
import static com.jby.vegeapp.shareObject.VariableUtils.UPDATE_LIST;

public class DeliverActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private ExpandableHeightListView activityDeliverListView;
    private DeliveryCustomerAdapter deliveryCustomerAdapter;
    private ArrayList<CustomerObject> customerObjectArrayList;
    //actionbar
    private Toolbar toolbar;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliver);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        //actionbar
        toolbar = findViewById(R.id.toolbar);
        //not found layout
        notFoundLayout = findViewById(R.id.not_found_layout);
        notFoundIcon = findViewById(R.id.not_found_layout_icon);
        notFoundLabel = findViewById(R.id.not_found_layout_label);
        //no internet connection
        noInternetConnectionLayout = findViewById(R.id.no_connection_layout);
        //progress bar
        progressBar = findViewById(R.id.progress_bar);
        activityDeliverListView = findViewById(R.id.activity_deliver_list_view);

        customerObjectArrayList = new ArrayList<>();
        deliveryCustomerAdapter = new DeliveryCustomerAdapter(this, customerObjectArrayList);
    }

    private void objectSetting() {
        activityDeliverListView.setAdapter(deliveryCustomerAdapter);
        activityDeliverListView.setOnItemClickListener(this);
        activityDeliverListView.setExpanded(true);
        setupActionBar();
        setupNotFoundLayout();
        checkInternetConnection(null);
    }

    private void setupActionBar() {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Delivery List");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void setupNotFoundLayout() {
        notFoundIcon.setImageDrawable(getResources().getDrawable(R.drawable.no_item_to_deliver_icon));
        notFoundLabel.setText("No item to deliver");
    }

    public void checkInternetConnection(View view) {
        boolean networkConnection = new NetworkConnection(this).checkNetworkConnection();
        if (networkConnection) {
            fetchAllCustomerOrder();
        }

        noInternetConnectionLayout.setVisibility(networkConnection ? View.GONE : View.VISIBLE);
        activityDeliverListView.setVisibility(networkConnection ? View.VISIBLE : View.GONE);
        progressBar.setVisibility(networkConnection ? View.VISIBLE : View.GONE);
    }

    private void fetchAllCustomerOrder() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("driver_id", SharedPreferenceManager.getUserId(getApplicationContext())));

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
                                try {
                                    JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("all_customer_item");
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        customerObjectArrayList.add(new CustomerObject(
                                                jsonArray.getJSONObject(i).getString("do_id"),
                                                jsonArray.getJSONObject(i).getString("do_prefix"),
                                                jsonArray.getJSONObject(i).getString("id"),
                                                jsonArray.getJSONObject(i).getString("name"),
                                                jsonArray.getJSONObject(i).getString("nickname"),
                                                jsonArray.getJSONObject(i).getString("phone"),
                                                jsonArray.getJSONObject(i).getString("address"),
                                                jsonArray.getJSONObject(i).getString("total_quantity")));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
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

    private void notifyDataSetChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deliveryCustomerAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setVisibility() {
        final boolean show = customerObjectArrayList.size() > 0;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activityDeliverListView.setVisibility(show ? View.VISIBLE : View.GONE);
                notFoundLayout.setVisibility(show ? View.GONE : View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("customer", customerObjectArrayList.get(i));
        startActivityForResult(new Intent(this, DeliveryDetailActivity.class).putExtras(bundle), UPDATE_LIST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == UPDATE_LIST) refresh();
    }

    private void refresh(){
        customerObjectArrayList.clear();
        fetchAllCustomerOrder();
    }
}
