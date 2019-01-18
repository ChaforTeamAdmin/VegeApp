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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.vegeapp.R;
import com.jby.vegeapp.basket.customer.CustomerDialog;
import com.jby.vegeapp.basket.dialog.TypeDialog;
import com.jby.vegeapp.database.CustomSqliteHelper;
import com.jby.vegeapp.database.FrameworkClass;
import com.jby.vegeapp.database.ResultCallBack;
import com.jby.vegeapp.network.BasketNetworkMonitor;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.vegeapp.database.CustomSqliteHelper.TB_BASKET;
import static com.jby.vegeapp.database.CustomSqliteHelper.TB_BASKET_FAVOURITE_CUSTOMER;
import static com.jby.vegeapp.database.CustomSqliteHelper.TB_BASKET_FAVOURITE_FARMER;

public class BasketActivity extends AppCompatActivity implements View.OnClickListener, TypeDialog.TypeDialogCallBack ,
        FarmerDialog.FarmerDialogCallBack, CustomerDialog.CustomerDialogCallBack, ResultCallBack,
        OffLineModeDialog.OffLineModeDialogCallBack {

    private TextView basketActivityType, basketActivityTo;
    private TextView basketActivityLabelType, basketActivityLabelTo;
    private LinearLayout basketActivityTypeLayout, basketActivityToLayout, basketActivityParentLayout;

    private DialogFragment dialogFragment;
    private FragmentManager fm;
    //either customer or farmer
    private String type = null;

    private String farmerID = "0", customerID = "0";
    //bsaket purpose
    private ImageView basketActivityMinus, basketActivityPlus;
    private Button basketActivityReturn, basketActivitySend;
    private TextView basketActivityLabelBasket;
    private EditText basketActivityQuantity;
    private LinearLayout basketActivityBasketLayout;
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

        basketActivityLabelBasket = findViewById(R.id.activity_basket_label_basket);
        basketActivityBasketLayout = findViewById(R.id.activity_basket_basket_layout);
        fm = getSupportFragmentManager();
        handler = new Handler();
    }

    private void objectSetting() {
        basketActivityTypeLayout.setOnClickListener(this);
        basketActivityToLayout.setOnClickListener(this);
        basketActivitySend.setOnClickListener(this);
        basketActivityReturn.setOnClickListener(this);
        basketActivityPlus.setOnClickListener(this);
        basketActivityMinus.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.activity_basket_select_type_layout:
                openTypeDialog();
                break;
            case R.id.activity_basket_to_layout:
                openDialog();
                break;
            case R.id.activity_basket_send_button:
                if(!basketActivityQuantity.getText().toString().trim().equals("") && !basketActivityQuantity.getText().toString().trim().equals("0")) save(false);
                else showSnackBar("Please enter basket quantity!");
                break;
            case R.id.activity_basket_return_button:
                if(!basketActivityQuantity.getText().toString().trim().equals("") && !basketActivityQuantity.getText().toString().trim().equals("0")) save(true);
                else showSnackBar("Please enter basket quantity!");
                break;
            case R.id.activity_basket_plus_button:
                setQuantity(true);
                break;
            case R.id.activity_basket_minus_button:
                setQuantity(false);
                break;
        }
    }
/*--------------------------------------------------------------select type purpose-------------------------------------------------------------------------*/
    private void openTypeDialog(){
        dialogFragment = new TypeDialog();
        dialogFragment.show(fm, "");
    }

    private void showToLayout(boolean show){
        basketActivityLabelTo.setVisibility(show ? View.VISIBLE:View.GONE);
        basketActivityToLayout.setVisibility(show ? View.VISIBLE:View.GONE);
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

    private void openDialog(){
        if(type.equals("Farmer")) openFarmerDialog();
        else openCustomerDialog();
    }

    private void showDefault(){
        if(type.equals("Farmer")) {
            String defaultFarmer = SharedPreferenceManager.getBasketDefaultFarmer(this);
            if(!defaultFarmer.equals("default")) {
                basketActivityTo.setText(splitString(defaultFarmer, 0));
                farmerID = splitString(defaultFarmer, 1);
                showBasketLayout(true);
            }
            else basketActivityTo.setText("Click here to select farmer");
        }
        else{
            String defaultCustomer = SharedPreferenceManager.getBasketDefaultCustomer(this);
            if(!defaultCustomer.equals("default")){
                basketActivityTo.setText(splitString(defaultCustomer, 0));
                customerID = splitString(defaultCustomer, 1);
                //show basket layout
                showBasketLayout(true);
            }
            else basketActivityTo.setText("Click here to select customer");
        }
    }

    private String splitString(String string, int position) {
        String[] details = string.split(",");
        return details[position];
    }

    /*-------------------------------------------------------farmer purpose-----------------------------------------------------------------------*/
    private void openFarmerDialog(){
        dialogFragment  = new FarmerDialog();

        Bundle bundle = new Bundle();
        bundle.putString("BasketActivity", "From");

        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
    }

    /*-------------------------------------------------------customer purpose-----------------------------------------------------------------------*/

    private void openCustomerDialog(){
        dialogFragment  = new CustomerDialog();
        dialogFragment.show(fm, "");
    }
    @Override
    public void selectedItem(String name, String id, String address) {
        //this id can be customer or farmer id
        if(type.equals("Farmer")){
            customerID = "0";
            farmerID = id;
            new FrameworkClass(this, new CustomSqliteHelper(this), TB_BASKET_FAVOURITE_FARMER)
                    .new create("farmer_id, name",id + "," + name)
                    .perform();
            SharedPreferenceManager.setBasketDefaultFarmer(this, name + "," + id);
        }

        else{
            farmerID = "0";
            customerID = id;
            new FrameworkClass(this, new CustomSqliteHelper(this), TB_BASKET_FAVOURITE_CUSTOMER)
                    .new create("customer_id, name",id + "," + name)
                    .perform();
            SharedPreferenceManager.setBasketDefaultCustomer(this, name + "," + id);
        }
        basketActivityTo.setText(name);
        showBasketLayout(true);

    }

//    ------------------------------------------------------------basket purpose--------------------------------------------------------------------
    private void showBasketLayout(boolean show){
        //reset
        if(!show){
            new AnimationUtility().fadeOutGone(this, basketActivityBasketLayout);
            basketActivityType.setText("Please select one");
            type = null;
            farmerID = "0";
            customerID = "0";
            //to layout
            basketActivityLabelTo.setVisibility(View.GONE);
            basketActivityToLayout.setVisibility(View.GONE);
        }
        else{
            if(type.equals("Customer")) basketActivitySend.setVisibility(View.GONE);
            else basketActivitySend.setVisibility(View.VISIBLE);
            new AnimationUtility().fadeInVisible(this, basketActivityBasketLayout);
        }
        basketActivityQuantity.setText("0");
    }

    private void setQuantity(boolean plus){
        if(basketActivityQuantity.getText().toString().trim().equals("")) basketActivityQuantity.setText("0");

        int quantity = Integer.valueOf(basketActivityQuantity.getText().toString().trim());
        if(plus) quantity ++;
        else {if(quantity > 0) quantity --;}
        basketActivityQuantity.setText(String.valueOf(quantity));
    }

    /*----------------------------------------------------------------upload basket to server------------------------------------------------------*/

    private void save(boolean isReturn){
        if(checkNetworkConnection()){
            if(type.equals("Customer")) returnBasketFromFarmerOrCustomer("6");
            else{
                if(isReturn) returnBasketFromFarmerOrCustomer("4");
                else sendBasketToFarmer();
            }
        }
        else{
            storeToLocal(isReturn);
            scheduleJob();
        }
    }

    public boolean checkNetworkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return (networkInfo !=  null && networkInfo.isConnected());
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

    private void returnBasketFromFarmerOrCustomer(String type){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("driver_id", SharedPreferenceManager.getUserId(this)));
        apiDataObjectArrayList.add(new ApiDataObject("farmer_id", farmerID));
        apiDataObjectArrayList.add(new ApiDataObject("customer_id", customerID));
        apiDataObjectArrayList.add(new ApiDataObject("quantity", basketActivityQuantity.getText().toString().trim()));
        apiDataObjectArrayList.add(new ApiDataObject("type", type));

        asyncTaskManager = new AsyncTaskManager(
                this,
                new ApiManager().vege_manage,
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
                        showSnackBar("Save Successfully!");
                        showBasketLayout(false);
                    }
                }
                else {
                    Toast.makeText(this, "Network Error!", Toast.LENGTH_SHORT).show();
                }

            } catch (InterruptedException e) {
                Toast.makeText(this, "Interrupted Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (ExecutionException e) {
                Toast.makeText(this, "Execution Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (JSONException e) {
                Toast.makeText(this, "JSON Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (TimeoutException e) {
                Toast.makeText(this, "Connection Time Out!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void sendBasketToFarmer(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("driver_id", SharedPreferenceManager.getUserId(this)));
        apiDataObjectArrayList.add(new ApiDataObject("farmer_id", farmerID));
        apiDataObjectArrayList.add(new ApiDataObject("quantity", basketActivityQuantity.getText().toString().trim()));

        asyncTaskManager = new AsyncTaskManager(
                this,
                new ApiManager().vege_manage,
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
                        showSnackBar("Save Successfully!");
                        showBasketLayout(false);
                    }
                }
                else {
                    Toast.makeText(this, "Network Error!", Toast.LENGTH_SHORT).show();
                }

            } catch (InterruptedException e) {
                Toast.makeText(this, "Interrupted Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (ExecutionException e) {
                Toast.makeText(this, "Execution Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (JSONException e) {
                Toast.makeText(this, "JSON Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (TimeoutException e) {
                Toast.makeText(this, "Connection Time Out!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void openOffLineModeDialog(){
        dialogFragment = new OffLineModeDialog();
        dialogFragment.show(fm, "");
    }

    private void storeToLocal(boolean isReturn){
        String type;
        if(this.type.equals("Customer")) type = "6";
        else {
            if(isReturn) type = "4";
            else type = "3";
        }
        String created_at = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));

        FrameworkClass frameworkClass = new FrameworkClass(this, this, new CustomSqliteHelper(this), TB_BASKET);
        frameworkClass.new create("farmer_id, customer_id, quantity, type, created_at",
                farmerID + "," +customerID + "," + basketActivityQuantity.getText().toString().trim() + "," + type + "," +created_at)
                .perform();

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
        showBasketLayout(false);
    }
}
