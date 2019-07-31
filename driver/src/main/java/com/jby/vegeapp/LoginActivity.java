package com.jby.vegeapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.jby.vegeapp.database.CustomSqliteHelper;
import com.jby.vegeapp.database.FrameworkClass;
import com.jby.vegeapp.others.NetworkConnection;
import com.jby.vegeapp.shareObject.ApiDataObject;
import com.jby.vegeapp.shareObject.ApiManager;
import com.jby.vegeapp.shareObject.AsyncTaskManager;
import com.jby.vegeapp.shareObject.SystemLanguage;
import com.jby.vegeapp.sharePreference.SharedPreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.vegeapp.database.CustomSqliteHelper.TB_COMPANY;
import static com.jby.vegeapp.database.CustomSqliteHelper.TB_FARMER;
import static com.jby.vegeapp.database.CustomSqliteHelper.TB_PRODUCT;
import static com.jby.vegeapp.shareObject.CustomToast.CustomToast;
import static com.jby.vegeapp.Utils.VariableUtils.MY_PERMISSIONS_REQUEST_READ_PHONE_STATE;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private ProgressBar progressBar;
    private RelativeLayout noInternetConnectionLayout;
    private TextView noInternetConnectionRetryButton;

    private FrameworkClass tbCompany, tbProduct, tbFarmer;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        objectInitialize();
        objectSetting();
    }

    private void isLogin() {
        Stetho.initializeWithDefaults(this);
        if (SharedPreferenceManager.getPhone(this).equals(getImei())) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }

    private void objectInitialize() {
        progressBar = findViewById(R.id.progress_bar);
        noInternetConnectionLayout = findViewById(R.id.no_connection_layout);
        noInternetConnectionRetryButton = findViewById(R.id.retry_button);

        tbCompany = new FrameworkClass(this, new CustomSqliteHelper(this), TB_COMPANY);
        tbFarmer = new FrameworkClass(this, new CustomSqliteHelper(this), TB_FARMER);
        tbProduct = new FrameworkClass(this, new CustomSqliteHelper(this), TB_PRODUCT);

    }

    private void objectSetting() {
        setUserDefaultLanguage("English");
        noInternetConnectionRetryButton.setOnClickListener(this);
        checkPhoneNumber();
    }

    public void showProgressBar(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
    /*-----------------------------------------------------------login purpose-----------------------------------------------------------------------*/

    private void checkPhoneNumber() {
        showProgressBar(true);
        if (new NetworkConnection(this).checkNetworkConnection()) {
            if (checkReadPhonePermission()) login();
            else requestPermission();
        } else {
            isLogin();
            noInternetConnectionLayout.setVisibility(new NetworkConnection(this).checkNetworkConnection() ? View.GONE : View.VISIBLE);
            showProgressBar(false);
        }
    }

    private void login() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("imei", getImei()));
                asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
                        new ApiManager().registration,
                        new ApiManager().getResultParameter(
                                "",
                                new ApiManager().setData(apiDataObjectArrayList),
                                ""
                        )
                );
                Log.d("hahaha", "my Api: " + new ApiManager().registration);
                asyncTaskManager.execute();

                if (!asyncTaskManager.isCancelled()) {

                    try {
                        jsonObjectLoginResponse = asyncTaskManager.get(30000, TimeUnit.MILLISECONDS);

                        if (jsonObjectLoginResponse != null) {
                            Log.d("jsonObject", "jsonObject: 1" + jsonObjectLoginResponse);
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                clear();
                                SharedPreferenceManager.setUserId(LoginActivity.this, jsonObjectLoginResponse.getString("driver_id"));
                                SharedPreferenceManager.setUsername(LoginActivity.this, jsonObjectLoginResponse.getString("username"));
                                SharedPreferenceManager.setUserType(LoginActivity.this, jsonObjectLoginResponse.getString("user_type"));
                                SharedPreferenceManager.setPhone(LoginActivity.this, getImei());
                                //set table value
                                setDefaultTable(jsonObjectLoginResponse);

                                isLogin();
                            } else invalidUser();
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

    private void invalidUser() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("Invalid User");
                builder.setMessage("This device is not able to login.\nPlease kindly contact with your administrator.");
                builder.setCancelable(true);

                builder.setPositiveButton(
                        "I Got IT",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int i) {
                                finish();
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    /*------------------------------------------------------get phone number purpose-----------------------------------------------*/
    private boolean checkReadPhonePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
            return result == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    @SuppressLint("HardwareIds")
    private String getImei() {
        TelephonyManager phoneMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return "";
        }
        assert phoneMgr != null;
        return (phoneMgr.getDeviceId() != null ? phoneMgr.getDeviceId() : "");
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
            Toast.makeText(this, "Phone state permission allows us to get imei number. Please allow it for additional functionality.", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    login();
                } else {
                    requestPermissionDialog();
                }
                break;
        }
    }

    private void requestPermissionDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("Notice");
                builder.setMessage("We can't proceed to the next step without getting your phone number");
                builder.setCancelable(true);

                builder.setPositiveButton(
                        "I Got IT",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int i) {
                                requestPermission();
                                dialog.cancel();
                            }
                        });

                builder.setNegativeButton(
                        "It's Okay",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int i) {
                                finish();
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    /*-----------------------------------------------language purpose--------------------------------------------------------------*/
    public void onStart() {
        super.onStart();
        if (SharedPreferenceManager.getLanguageId(this).equals("default")) {
            String userDefaultLanguage = Locale.getDefault().getDisplayLanguage();
            setUserDefaultLanguage(userDefaultLanguage);
        }
    }

    public void setUserDefaultLanguage(String userDefaultLanguage) {
        switch (userDefaultLanguage) {
            case "English":
                SharedPreferenceManager.setLanguageId(this, "1");
                break;
            case "中文":
                SharedPreferenceManager.setLanguageId(this, "2");
                break;
            case "Bahasa Malaysia":
                SharedPreferenceManager.setLanguageId(this, "3");
                break;
            default:
                SharedPreferenceManager.setLanguageId(this, "1");
                break;
        }
    }

    private String languageSetting(int position) {
        SystemLanguage systemLanguage = new SystemLanguage(this, SharedPreferenceManager.getLanguageId(this));
        return systemLanguage.language(position);
    }

    /*----------------------------------------------------local database ------------------------------------------------------------*/
    private void clear() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                tbCompany.new Delete().perform();
                tbFarmer.new Delete().perform();
                tbProduct.new Delete().perform();
            }
        }).start();
    }

    private void setDefaultTable(final JSONObject jsonObject) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String created_at = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));
                    /*
                     * table farmer
                     * */
                    JSONArray farmer = jsonObject.getJSONArray("farmer");
                    for (int i = 0; i < farmer.length(); i++) {
                        tbFarmer.new create("id, name, phone, address, created_at", new String[]{
                                farmer.getJSONObject(i).getString("id"),
                                farmer.getJSONObject(i).getString("name"),
                                farmer.getJSONObject(i).getString("phone"),
                                farmer.getJSONObject(i).getString("address"),
                                created_at
                        }).perform();
                    }
                    /*
                     * table product
                     * */
                    JSONArray product = jsonObject.getJSONArray("product");
                    for (int i = 0; i < product.length(); i++) {
                        tbProduct.new create("id, product_code, name, picture, type, price, created_at",
                                new String[]{
                                        product.getJSONObject(i).getString("id"),
                                        product.getJSONObject(i).getString("product_code"),
                                        product.getJSONObject(i).getString("name"),
                                        product.getJSONObject(i).getString("picture"),
                                        product.getJSONObject(i).getString("type"),
                                        product.getJSONObject(i).getString("price"),
                                        created_at
                                }).perform();
                    }

                    JSONObject company = jsonObject.getJSONObject("company");
                    tbCompany.new create("id, name, phone, fax, address, created_at", new String[]{
                            company.getString("id"),
                            company.getString("name"),
                            company.getString("phone"),
                            company.getString("fax"),
                            company.getString("address"),
                            created_at
                    }).perform();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.retry_button:
                checkPhoneNumber();
                break;
        }
    }
}
