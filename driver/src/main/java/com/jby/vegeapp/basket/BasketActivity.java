package com.jby.vegeapp.basket;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jby.vegeapp.R;
import com.jby.vegeapp.basket.customer.CustomerDialog;
import com.jby.vegeapp.basket.dialog.TypeDialog;
import com.jby.vegeapp.database.CustomSqliteHelper;
import com.jby.vegeapp.database.FrameworkClass;
import com.jby.vegeapp.database.ResultCallBack;
import com.jby.vegeapp.network.BasketNetworkMonitor;
import com.jby.vegeapp.object.history.BasketHistoryObject;
import com.jby.vegeapp.pickUp.OffLineModeDialog;
import com.jby.vegeapp.pickUp.farmer.FarmerDialog;
import com.jby.vegeapp.shareObject.AnimationUtility;
import com.jby.vegeapp.shareObject.ApiDataObject;
import com.jby.vegeapp.shareObject.ApiManager;
import com.jby.vegeapp.shareObject.AsyncTaskManager;
import com.jby.vegeapp.sharePreference.SharedPreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.vegeapp.database.CustomSqliteHelper.TB_BASKET;
import static com.jby.vegeapp.database.CustomSqliteHelper.TB_BASKET_FAVOURITE_CUSTOMER;
import static com.jby.vegeapp.database.CustomSqliteHelper.TB_BASKET_FAVOURITE_FARMER;
import static com.jby.vegeapp.shareObject.CustomToast.CustomToast;
import static com.jby.vegeapp.Utils.VariableUtils.REFRESH_AVAILABLE_QUANTITY;

public class BasketActivity extends AppCompatActivity implements View.OnClickListener, TypeDialog.TypeDialogCallBack,
        FarmerDialog.FarmerDialogCallBack, CustomerDialog.CustomerDialogCallBack, ResultCallBack,
        OffLineModeDialog.OffLineModeDialogCallBack {
    //actionbar
    private Toolbar toolbar;

    private TextView basketActivityType, basketActivityTo, basketActivityAvailableQuantity;
    private TextView basketActivityLabelType, basketActivityLabelTo;
    private LinearLayout basketActivityTypeLayout, basketActivityToLayout, basketActivityParentLayout;

    private DialogFragment dialogFragment;
    private FragmentManager fm;
    //either customer or farmer
    private String type = null;
    private String farmerID = "0", customerID = "0";
    //bsaket purpose
    private ImageView basketActivityMinus, basketActivityPlus;
    private Button basketActivityReturn, basketActivitySend, basketActivityUpdate;
    private EditText basketActivityQuantity;
    private LinearLayout basketActivityBasketLayout;
    //update purpose
    private BasketHistoryObject basketHistoryObject;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basket);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        //actionbar
        toolbar = findViewById(R.id.toolbar);
        basketActivityAvailableQuantity = findViewById(R.id.activity_basket_available_quantity);

        basketActivityType = findViewById(R.id.activity_basket_select_type);
        basketActivityTo = findViewById(R.id.activity_basket_to);

        basketActivityLabelType = findViewById(R.id.activity_basket_label_select_type);
        basketActivityLabelTo = findViewById(R.id.activity_basket_label_to_layout);

        basketActivityTypeLayout = findViewById(R.id.activity_basket_select_type_layout);
        basketActivityToLayout = findViewById(R.id.activity_basket_to_layout);
        basketActivityParentLayout = findViewById(R.id.activity_basket_parent_layout);

        basketActivityMinus = findViewById(R.id.activity_basket_minus_button);
        basketActivityPlus = findViewById(R.id.activity_basket_plus_button);
        basketActivityQuantity = findViewById(R.id.activity_basket_quantity);

        basketActivityReturn = findViewById(R.id.activity_basket_return_button);
        basketActivitySend = findViewById(R.id.activity_basket_send_button);
        basketActivityUpdate = findViewById(R.id.activity_basket_update);

        basketActivityBasketLayout = findViewById(R.id.activity_basket_basket_layout);
        fm = getSupportFragmentManager();
        handler = new Handler();
    }

    private void objectSetting() {
        basketActivityTypeLayout.setOnClickListener(this);
        basketActivityToLayout.setOnClickListener(this);
        basketActivitySend.setOnClickListener(this);
        basketActivityReturn.setOnClickListener(this);
        basketActivityUpdate.setOnClickListener(this);
        basketActivityPlus.setOnClickListener(this);
        basketActivityMinus.setOnClickListener(this);
        //check whether is update or edit
        checkingStatus();
    }

    private void checkingStatus() {
        //edit
        if (getIntent().getExtras() != null) {
            basketHistoryObject = (BasketHistoryObject) getIntent().getExtras().getSerializable("object");
            assert basketHistoryObject != null;

            customerID = basketHistoryObject.getCustomer_id();
            farmerID = basketHistoryObject.getFarmer_id();
            String quantity = basketHistoryObject.getQuantity();

            basketActivityQuantity.setText(Integer.valueOf(quantity) < 0 ? quantity.substring(1) : quantity);
            type(!farmerID.equals("0") ? "Farmer" : "Customer");
        }
        setupActionBar();
        getAvailableBasket();

        basketActivityReturn.setVisibility(getIntent().getExtras() == null ? View.VISIBLE : View.GONE);
        basketActivitySend.setVisibility(getIntent().getExtras() == null ? View.VISIBLE : View.GONE);
        basketActivityTypeLayout.setVisibility(getIntent().getExtras() == null ? View.VISIBLE : View.GONE);
        basketActivityLabelType.setVisibility(getIntent().getExtras() == null ? View.VISIBLE : View.GONE);
        basketActivityUpdate.setVisibility(getIntent().getExtras() == null ? View.GONE : View.VISIBLE);
    }

    private void setupActionBar() {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Basket");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_basket_select_type_layout:
                openTypeDialog();
                break;
            case R.id.activity_basket_to_layout:
                openDialog();
                break;
            case R.id.activity_basket_send_button:
                save(!customerID.equals("0") ? "5" : "3");
                break;
            case R.id.activity_basket_return_button:
                save(!customerID.equals("0") ? "6" : "4");
                break;
            case R.id.activity_basket_update:
                updateBasket();
                break;
            case R.id.activity_basket_plus_button:
                setQuantity(true);
                break;
            case R.id.activity_basket_minus_button:
                setQuantity(false);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        //for update available basket quantity in home activity
        setResult(REFRESH_AVAILABLE_QUANTITY);
        super.onBackPressed();

    }

    /*------------------------------------------------------------------get driver available basket------------------------------------------------------*/
    private void getAvailableBasket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("driver_id", SharedPreferenceManager.getUserId(BasketActivity.this)));

                asyncTaskManager = new AsyncTaskManager(
                        BasketActivity.this,
                        new ApiManager().basket,
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
                                final String quantity = jsonObjectLoginResponse.getString("total_basket");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        basketActivityAvailableQuantity.setText(quantity);
                                    }
                                });
                            }
                        } else {
                            CustomToast(BasketActivity.this, "Network Error!");
                        }
                    } catch (InterruptedException e) {
                        CustomToast(BasketActivity.this, "Interrupted Exception!");
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        CustomToast(BasketActivity.this, "Execution Exception!");
                        e.printStackTrace();
                    } catch (JSONException e) {
                        CustomToast(BasketActivity.this, "JSON Exception!");
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        CustomToast(BasketActivity.this, "Connection Time Out!");
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /*--------------------------------------------------------------select type purpose-------------------------------------------------------------------------*/
    private void openTypeDialog() {
        dialogFragment = new TypeDialog();
        dialogFragment.show(fm, "");
    }

    private void showToLayout(boolean show) {
        basketActivityLabelTo.setVisibility(show ? View.VISIBLE : View.GONE);
        basketActivityToLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void type(final String type) {
        this.type = type;
        basketActivityType.setText(type);
        //setup initialize value
        basketActivityLabelTo.setText(type);
        //show default
        showDefault();
        showToLayout(true);
    }

    private void openDialog() {
        if (type.equals("Farmer")) openFarmerDialog();
        else openCustomerDialog();
    }

    private void showDefault() {
        String target;
        if (type.equals("Farmer")) {
            target = SharedPreferenceManager.getBasketDefaultFarmer(this);
            if(!target.equals("default")) farmerID = splitString(target, 1);
            customerID = "0";
        } else {
            target = SharedPreferenceManager.getBasketDefaultCustomer(this);
            if(!target.equals("default")) customerID = splitString(target, 1);
            farmerID = "0";
        }
        basketActivityTo.setText(!target.equals("default") ? splitString(target, 0) : "Click here to select");
        hideBasketLayout(false);
    }

    private String splitString(String string, int position) {
        String[] details = string.split("%");
        return details[position];
    }

    /*-------------------------------------------------------farmer purpose-----------------------------------------------------------------------*/
    private void openFarmerDialog() {
        dialogFragment = new FarmerDialog();

        Bundle bundle = new Bundle();
        bundle.putString("BasketActivity", "From");

        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
    }

    /*-------------------------------------------------------customer purpose-----------------------------------------------------------------------*/

    private void openCustomerDialog() {
        dialogFragment = new CustomerDialog();
        dialogFragment.show(fm, "");
    }

    @Override
    public void selectedItem(String name, String id, String address, String phone) {
        //this id can be customer or farmer id
        if (type.equals("Farmer")) {
            customerID = "0";
            farmerID = id;
            new FrameworkClass(this, new CustomSqliteHelper(this), TB_BASKET_FAVOURITE_FARMER)
                    .new create("farmer_id, name", id + "," + name)
                    .perform();
            SharedPreferenceManager.setBasketDefaultFarmer(this, name + "%" + id + "%" + address);
        } else {
            farmerID = "0";
            customerID = id;
            new FrameworkClass(this, new CustomSqliteHelper(this), TB_BASKET_FAVOURITE_CUSTOMER)
                    .new create("customer_id, name", id + "," + name)
                    .perform();
            SharedPreferenceManager.setBasketDefaultCustomer(this, name + "%" + id + "%" + address);
        }
        basketActivityTo.setText(name);
        hideBasketLayout(false);

    }

    //    ------------------------------------------------------------basket purpose--------------------------------------------------------------------
    private void hideBasketLayout(boolean hide) {
        //reset
        if (hide) {
            new AnimationUtility().fadeOutGone(this, basketActivityBasketLayout);
            basketActivityType.setText("Please select one");
            type = null;
            farmerID = "0";
            customerID = "0";
            //to layout
            basketActivityLabelTo.setVisibility(View.GONE);
            basketActivityToLayout.setVisibility(View.GONE);
        } else {
            basketActivitySend.setVisibility(View.VISIBLE);
            new AnimationUtility().fadeInVisible(this, basketActivityBasketLayout);
        }
        if (basketHistoryObject == null) basketActivityQuantity.setText("0");
    }

    private void setQuantity(boolean plus) {
        if (basketActivityQuantity.getText().toString().trim().equals(""))
            basketActivityQuantity.setText("0");

        int quantity = Integer.valueOf(basketActivityQuantity.getText().toString().trim());
        if (plus) quantity++;
        else {
            if (quantity > 0) quantity--;
        }
        basketActivityQuantity.setText(String.valueOf(quantity));
    }

    /*----------------------------------------------------------------upload basket to server------------------------------------------------------*/

    private void save(String type) {
        if(customerID.equals("0") && farmerID.equals("0")){
            showSnackBar("Please select a target");
            return;
        }
        try {
            if (!basketActivityQuantity.getText().toString().equals("0")) {
                if (checkNetworkConnection()) basketControl(type);
                else {
                    storeToLocal(type);
                    scheduleJob();
                }
            } else showSnackBar("Please enter a valid input");
        } catch (NumberFormatException e) {
            showSnackBar("Please enter a valid input");
        }
    }

    public boolean checkNetworkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return (networkInfo != null && networkInfo.isConnected());
    }

    public void scheduleJob() {
        ComponentName componentName = new ComponentName(this, BasketNetworkMonitor.class);
        JobInfo info = new JobInfo.Builder(2, componentName)
                .setRequiresCharging(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setPeriodic(15 * 60 * 1000)
                .build();

        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = 0;
        if (scheduler != null) {
            resultCode = scheduler.schedule(info);
        }
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d("PickUpActivity", "Job scheduled");
        } else {
            Log.d("PickUpActivity", "Job scheduling failed");
        }
    }

    private void basketControl(String type) {
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("driver_id", SharedPreferenceManager.getUserId(this)));
        apiDataObjectArrayList.add(new ApiDataObject("farmer_id", farmerID));
        apiDataObjectArrayList.add(new ApiDataObject("customer_id", customerID));
        apiDataObjectArrayList.add(new ApiDataObject("quantity", basketActivityQuantity.getText().toString().trim()));
        apiDataObjectArrayList.add(new ApiDataObject("type", type));

        asyncTaskManager = new AsyncTaskManager(
                this,
                new ApiManager().basket,
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
                        showSnackBar("Save Successfully!");
                        hideBasketLayout(true);
                    }
                } else {
                    CustomToast(this, "Network Error!");
                }

            } catch (InterruptedException e) {
                CustomToast(this, "Interrupted Exception!");
                e.printStackTrace();
            } catch (ExecutionException e) {
                CustomToast(this, "Execution Exception!");
                e.printStackTrace();
            } catch (JSONException e) {
                CustomToast(this, "JSON Exception!");
                e.printStackTrace();
            } catch (TimeoutException e) {
                CustomToast(this, "Connection Time Out!");
                e.printStackTrace();
            }
        }
    }

    private void openOffLineModeDialog() {
        dialogFragment = new OffLineModeDialog();
        dialogFragment.show(fm, "");
    }

    private void storeToLocal(String type) {
        String created_at = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));
        FrameworkClass frameworkClass = new FrameworkClass(this, this, new CustomSqliteHelper(this), TB_BASKET);
        frameworkClass.new create("farmer_id, customer_id, quantity, type, created_at", farmerID + "," + customerID + "," + basketActivityQuantity.getText().toString().trim() + "," + type + "," + created_at).perform();
    }

    /*-------------------------------------------------------------------------update purpose---------------------------------------------------------*/
    private void updateBasket() {
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("driver_id", SharedPreferenceManager.getUserId(this)));
        apiDataObjectArrayList.add(new ApiDataObject("farmer_id", farmerID));
        apiDataObjectArrayList.add(new ApiDataObject("customer_id", customerID));
        apiDataObjectArrayList.add(new ApiDataObject("quantity", basketActivityQuantity.getText().toString().trim()));
        apiDataObjectArrayList.add(new ApiDataObject("type", basketHistoryObject.getType()));
        apiDataObjectArrayList.add(new ApiDataObject("id", basketHistoryObject.getId()));

        asyncTaskManager = new AsyncTaskManager(
                this,
                new ApiManager().basket,
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
                        CustomToast(this, "Update Successfully!");
                        finish();
                    }
                } else {
                    CustomToast(this, "Network Error!");
                }

            } catch (InterruptedException e) {
                CustomToast(this, "Interrupted Exception!");
                e.printStackTrace();
            } catch (ExecutionException e) {
                CustomToast(this, "Execution Exception!");
                e.printStackTrace();
            } catch (JSONException e) {
                CustomToast(this, "JSON Exception!");
                e.printStackTrace();
            } catch (TimeoutException e) {
                CustomToast(this, "Connection Time Out!");
                e.printStackTrace();
            }
        }
    }

    //    -----------------------------------------------------------other-----------------------------------------------------------------------
//    snackBar setting
    public void showSnackBar(String message) {
        final Snackbar snackbar = Snackbar.make(basketActivityParentLayout, message, Snackbar.LENGTH_SHORT);
        snackbar.setActionTextColor(getResources().getColor(R.color.blue));
        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    @Override
    public void createResult(String status) {
        openOffLineModeDialog();
    }

    @Override
    public void readResult(String result) {

    }

    @Override
    public void updateResult(String status) {

    }

    @Override
    public void deleteResult(String status) {

    }

    @Override
    public void clear() {
        showSnackBar("Save Successfully!");
        hideBasketLayout(true);
    }
}
