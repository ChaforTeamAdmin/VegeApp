package com.jby.vegeapp;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.jby.vegeapp.basket.BasketActivity;
import com.jby.vegeapp.delivery.DeliverActivity;
import com.jby.vegeapp.history.HistoryActivity;
import com.jby.vegeapp.others.NetworkConnection;
import com.jby.vegeapp.pickUp.PickUpActivity;
import com.jby.vegeapp.shareObject.ApiDataObject;
import com.jby.vegeapp.shareObject.ApiManager;
import com.jby.vegeapp.shareObject.AsyncTaskManager;
import com.jby.vegeapp.shareObject.SystemLanguage;
import com.jby.vegeapp.sharePreference.SharedPreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.vegeapp.shareObject.CustomToast.CustomToast;
import static com.jby.vegeapp.Utils.VariableUtils.REFRESH_AVAILABLE_QUANTITY;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private CardView homeActivityPickUp, homeActivityDeliver, homeActivityBasket, homeActivityHistory;
    private TextView homeActivityLabelPickUp, homeActivityLabelDeliver, homeActivityLabelBasket, homeActivityLabelHistory;

    private TextView homeActivityNumberBasket, homeActivityDriverName;
    private ProgressBar progressBar;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        objectInitialize();
        objectSetting();

    }

    private void objectInitialize() {
        homeActivityNumberBasket = findViewById(R.id.activity_home_num_basket);
        homeActivityDriverName = findViewById(R.id.activity_home_driver_name);
        progressBar = findViewById(R.id.progress_bar);


        homeActivityPickUp = findViewById(R.id.activity_home_pick_up_vege);
        homeActivityDeliver = findViewById(R.id.activity_home_deliver);
        homeActivityBasket = findViewById(R.id.activity_home_basket);
        homeActivityHistory = findViewById(R.id.activity_home_history);

        homeActivityLabelPickUp = findViewById(R.id.activity_home_label_pick_up_vege);
        homeActivityLabelDeliver = findViewById(R.id.activity_home_label_deliver);
        homeActivityLabelBasket = findViewById(R.id.activity_home_label_basket);
        homeActivityLabelHistory = findViewById(R.id.activity_home_label_history);
    }

    private void objectSetting() {
        homeActivityPickUp.setOnClickListener(this);
        homeActivityDeliver.setOnClickListener(this);
        homeActivityBasket.setOnClickListener(this);
        homeActivityHistory.setOnClickListener(this);

        showProgressBar(true);
        homeActivityDriverName.setText(String.format("Hi, %s", SharedPreferenceManager.getUsername(this)));

        setUpLanguage();
        checkNetworkConnection();
    }

    private void checkNetworkConnection(){
        if(new NetworkConnection(getApplicationContext()).checkNetworkConnection())  getAvailableBasket();
        else {
            showSnackBar("You're in offline mode");
            showProgressBar(false);
        }
    }

    /*
     * language setting
     * */
    private void setUpLanguage() {
        homeActivityLabelPickUp.setText(languageSetting(4));
        homeActivityLabelDeliver.setText(languageSetting(5));
        homeActivityLabelBasket.setText(languageSetting(6));
        homeActivityLabelHistory.setText(languageSetting(7));
    }

    private String languageSetting(int position) {
        SystemLanguage systemLanguage = new SystemLanguage(this, SharedPreferenceManager.getLanguageId(this));
        return systemLanguage.language(position);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_home_pick_up_vege:
                startActivityForResult(new Intent(this, PickUpActivity.class), REFRESH_AVAILABLE_QUANTITY);
                break;
            case R.id.activity_home_deliver:
                if (new NetworkConnection(getApplicationContext()).checkNetworkConnection())
                    startActivityForResult(new Intent(this, DeliverActivity.class), REFRESH_AVAILABLE_QUANTITY);
                else showSnackBar("You're in offline mode");
                break;
            case R.id.activity_home_basket:
                if (new NetworkConnection(getApplicationContext()).checkNetworkConnection())
                    startActivityForResult(new Intent(this, BasketActivity.class), REFRESH_AVAILABLE_QUANTITY);
                else showSnackBar("You're in offline mode");
                break;
            case R.id.activity_home_history:
                if (new NetworkConnection(getApplicationContext()).checkNetworkConnection())
                    startActivityForResult(new Intent(this, HistoryActivity.class), REFRESH_AVAILABLE_QUANTITY);
                else showSnackBar("You're in offline mode");
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == REFRESH_AVAILABLE_QUANTITY){
            checkNetworkConnection();
        }
    }

    private void getAvailableBasket() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("driver_id", SharedPreferenceManager.getUserId(HomeActivity.this)));

                asyncTaskManager = new AsyncTaskManager(
                        HomeActivity.this,
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
                                        homeActivityNumberBasket.setText(quantity);
                                    }
                                });
                            }
                        } else {
                            CustomToast(HomeActivity.this, "Network Error!");
                        }
                    } catch (InterruptedException e) {
                        CustomToast(HomeActivity.this, "Interrupted Exception!");
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        CustomToast(HomeActivity.this, "Execution Exception!");
                        e.printStackTrace();
                    } catch (JSONException e) {
                        CustomToast(HomeActivity.this, "JSON Exception!");
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        CustomToast(HomeActivity.this, "Connection Time Out!");
                        e.printStackTrace();
                    }
                }
                showProgressBar(false);
            }
        }).start();
    }

    public void showProgressBar(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    public void showSnackBar(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
                snackbar.setAction("Dismiss", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                    }
                });
                snackbar.show();
            }
        });
    }
}
