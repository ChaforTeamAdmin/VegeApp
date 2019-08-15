package com.jby.admin.registration;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.admin.MainActivity;
import com.jby.admin.R;
import com.jby.admin.database.CustomSqliteHelper;
import com.jby.admin.database.FrameworkClass;
import com.jby.admin.others.NetworkConnection;
import com.jby.admin.shareObject.ApiDataObject;
import com.jby.admin.shareObject.ApiManager;
import com.jby.admin.shareObject.AsyncTaskManager;
import com.jby.admin.sharePreference.SharedPreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.admin.database.CustomSqliteHelper.TB_CUSTOMER;
import static com.jby.admin.database.CustomSqliteHelper.TB_DRIVER;
import static com.jby.admin.database.CustomSqliteHelper.TB_FARMER;
import static com.jby.admin.database.CustomSqliteHelper.TB_GRADE;
import static com.jby.admin.database.CustomSqliteHelper.TB_LOCATION;
import static com.jby.admin.database.CustomSqliteHelper.TB_PRODUCT;
import static com.jby.admin.shareObject.CustomToast.CustomToast;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText loginActivityUsername, loginActivityPassword;
    private TextView loginActivityForgotPassword, loginActivityVersion;
    private ImageView loginActivityShowPassword, loginActivityCancelUsername;
    private LinearLayout loginActivityMainLayout;
    private ProgressBar loginActivityProgressBar;

    private FrameworkClass tbDriver, tbFarmer, tbProduct, tbCustomer, tbLocation, tbGrade;

    private boolean show = true;
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        isLogin();
        objectInitialize();
        objectSetting();

    }

    private void objectInitialize() {
        loginActivityUsername = findViewById(R.id.activity_login_username);
        loginActivityPassword = findViewById(R.id.activity_login_password);

        loginActivityForgotPassword = findViewById(R.id.activity_login_forgot_password);
        loginActivityVersion = findViewById(R.id.activity_login_version_name);

        loginActivityShowPassword = findViewById(R.id.activity_login_show_password);
        loginActivityCancelUsername = findViewById(R.id.activity_login_cancel_username);

        loginActivityMainLayout = findViewById(R.id.activity_login_parent_layout);
        loginActivityProgressBar = findViewById(R.id.login_activity_progress_bar);

        handler = new Handler();

        tbDriver = new FrameworkClass(getApplicationContext(), new CustomSqliteHelper(getApplicationContext()), TB_DRIVER);
        tbFarmer = new FrameworkClass(getApplicationContext(), new CustomSqliteHelper(getApplicationContext()), TB_FARMER);
        tbProduct = new FrameworkClass(getApplicationContext(), new CustomSqliteHelper(getApplicationContext()), TB_PRODUCT);
        tbCustomer = new FrameworkClass(getApplicationContext(), new CustomSqliteHelper(getApplicationContext()), TB_CUSTOMER);
        tbLocation = new FrameworkClass(getApplicationContext(), new CustomSqliteHelper(getApplicationContext()), TB_LOCATION);
        tbGrade = new FrameworkClass(getApplicationContext(), new CustomSqliteHelper(getApplicationContext()), TB_GRADE);
    }

    private void objectSetting() {
        loginActivityShowPassword.setOnClickListener(this);
        loginActivityCancelUsername.setOnClickListener(this);
        displayVersion();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.activity_login_show_password:
                showPasswordSetting();
                break;
            case R.id.activity_login_cancel_username:
                loginActivityUsername.setText("");
                break;
        }
    }

    //    show/ hide password setting
    private void showPasswordSetting() {
        if (show) {
            loginActivityShowPassword.setImageDrawable(getResources().getDrawable(R.drawable.activity_login_hide_icon));
            loginActivityPassword.setTransformationMethod(null);
            show = false;
        } else {
            loginActivityShowPassword.setImageDrawable(getResources().getDrawable(R.drawable.activity_login_show_icon));
            loginActivityPassword.setTransformationMethod(new PasswordTransformationMethod());
            show = true;
        }
    }

    //    sign in setting
    public void checking(View v) {
        loginActivityProgressBar.setVisibility(View.VISIBLE);
        final String username = loginActivityUsername.getText().toString().trim();
        final String password = loginActivityPassword.getText().toString().trim();
        closeKeyBoard();

        if (new NetworkConnection(this).checkNetworkConnection()) {
            if (!username.equals("") && !password.equals("")) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        signIn(username, password);
                    }
                }, 200);
            } else {
                showSnackBar("Invalid username or password!");
                loginActivityProgressBar.setVisibility(View.GONE);
            }

        } else {
            showSnackBar("No Internet connection!");
            loginActivityProgressBar.setVisibility(View.GONE);
        }
    }

    public void closeKeyBoard() {
        View view = getCurrentFocus();
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view != null)
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void signIn(String username, String password) {
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("password", password));
        apiDataObjectArrayList.add(new ApiDataObject("username", username));

        asyncTaskManager = new AsyncTaskManager(
                this,
                new ApiManager().registration,
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
//                        setup user detail
                        whenLoginSuccessful(jsonObjectLoginResponse);

                    } else if (jsonObjectLoginResponse.getString("status").equals("2")) {
                        showSnackBar("Invalid email or password");
                        loginActivityProgressBar.setVisibility(View.GONE);
                    }
                } else {
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
        loginActivityProgressBar.setVisibility(View.GONE);
    }

    //    snackBar setting
    private void showSnackBar(String message) {
        final Snackbar snackbar = Snackbar.make(loginActivityMainLayout, message, Snackbar.LENGTH_SHORT);
        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    public void whenLoginSuccessful(JSONObject jsonObject) {
        try {
            syncDataFromCloud();

            SharedPreferenceManager.setUserId(this, jsonObject.getString("admin_id"));
            SharedPreferenceManager.setUsername(this, jsonObject.getString("username"));
            //intent
            startActivity(new Intent(this, MainActivity.class));
            finish();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void isLogin() {
        if (!SharedPreferenceManager.getUserId(this).equals("default")) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    /*--------------------------------------------------------------local database purpose--------------------------------------------------------------------------------*/
    private void syncDataFromCloud() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                fetchAllDriver();
                fetchAllFarmer();
                fetchAllCustomer();
                fetchAllProduct();
                fetchLocation();
                fetchGrade();


            }
        }).start();
    }

    /*
     * driver
     * */
    private void fetchAllDriver() {

        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("read", "1"));

        asyncTaskManager = new AsyncTaskManager(
                getApplicationContext(),
                new ApiManager().driver,
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
                    Log.d("jsonObject", "product List: " + jsonObjectLoginResponse);

                    try {
                        if (jsonObjectLoginResponse.getString("status").equals("1")) {
                            tbDriver.new Delete().perform();
                            JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("driver");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                tbDriver.new create("driver_id, name, nickname, phone, created_at",
                                        new String[]{
                                                jsonArray.getJSONObject(i).getString("id"),
                                                jsonArray.getJSONObject(i).getString("username"),
                                                jsonArray.getJSONObject(i).getString("nickname"),
                                                jsonArray.getJSONObject(i).getString("phone"),
                                                (String) android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date())
                                        }).perform();
                            }
                        }
                    } catch (JSONException e) {
                        CustomToast(getApplicationContext(), "JSON Exception!");
                        e.printStackTrace();
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
            } catch (TimeoutException e) {
                CustomToast(getApplicationContext(), "Connection Time Out!");
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * farmer
     * */
    private void fetchAllFarmer() {
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("read", "1"));

        asyncTaskManager = new AsyncTaskManager(
                getApplicationContext(),
                new ApiManager().farmer,
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
                    Log.d("jsonObject", "product List: " + jsonObjectLoginResponse);

                    try {
                        if (jsonObjectLoginResponse.getString("status").equals("1")) {
                            tbFarmer.new Delete().perform();
                            JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("farmer");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                tbFarmer.new create("farmer_id, name, phone, address, created_at",
                                        new String[]{
                                                jsonArray.getJSONObject(i).getString("id"),
                                                jsonArray.getJSONObject(i).getString("name"),
                                                jsonArray.getJSONObject(i).getString("phone"),
                                                jsonArray.getJSONObject(i).getString("address"),
                                                (String) android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date())
                                        }).perform();
                            }
                        }
                    } catch (JSONException e) {
                        CustomToast(getApplicationContext(), "JSON Exception!");
                        e.printStackTrace();
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
            } catch (TimeoutException e) {
                CustomToast(getApplicationContext(), "Connection Time Out!");
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * customer
     * */
    private void fetchAllCustomer() {

        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("read", "1"));

        asyncTaskManager = new AsyncTaskManager(
                getApplicationContext(),
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
                    Log.d("jsonObject", "product List: " + jsonObjectLoginResponse);

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
                        }
                    } catch (JSONException e) {
                        CustomToast(getApplicationContext(), "JSON Exception!");
                        e.printStackTrace();
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
            } catch (TimeoutException e) {
                CustomToast(getApplicationContext(), "Connection Time Out!");
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * product
     * */
    private void fetchAllProduct() {
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("read", "1"));

        asyncTaskManager = new AsyncTaskManager(
                getApplicationContext(),
                new ApiManager().product,
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
                    Log.d("jsonObject", "product List: " + jsonObjectLoginResponse);
                    try {
                        if (jsonObjectLoginResponse.getString("status").equals("1")) {
                            tbProduct.new Delete().perform();
                            JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("product");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                tbProduct.new create("product_id, product_code, name, picture, type, created_at", new String[]{
                                        jsonArray.getJSONObject(i).getString("id"),
                                        jsonArray.getJSONObject(i).getString("product_code"),
                                        jsonArray.getJSONObject(i).getString("name"),
                                        jsonArray.getJSONObject(i).getString("picture"),
                                        jsonArray.getJSONObject(i).getString("type"),
                                        (String) android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date())
                                }).perform();
                            }
                        }
                    } catch (JSONException e) {
                        CustomToast(getApplicationContext(), "JSON Exception!");
                        e.printStackTrace();
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
            } catch (TimeoutException e) {
                CustomToast(getApplicationContext(), "Connection Time Out!");
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * location
     * */
    private void fetchLocation() {
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("read", "1"));

        asyncTaskManager = new AsyncTaskManager(
                getApplicationContext(),
                new ApiManager().location,
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
                    Log.d("jsonObject", "product List: " + jsonObjectLoginResponse);
                    try {
                        if (jsonObjectLoginResponse.getString("status").equals("1")) {
                            tbLocation.new Delete().perform();
                            JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("location");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                tbLocation.new create("location_id, name, created_at",
                                        new String[]{
                                                jsonArray.getJSONObject(i).getString("id"),
                                                jsonArray.getJSONObject(i).getString("name"),
                                                (String) android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date())
                                        }).perform();
                            }
                        }
                    } catch (JSONException e) {
                        CustomToast(getApplicationContext(), "JSON Exception!");
                        e.printStackTrace();
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
            } catch (TimeoutException e) {
                CustomToast(getApplicationContext(), "Connection Time Out!");
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * grade
     * */
    private void fetchGrade() {
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("read", "1"));

        asyncTaskManager = new AsyncTaskManager(
                getApplicationContext(),
                new ApiManager().grade,
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
                    Log.d("jsonObject", "product List: " + jsonObjectLoginResponse);
                    try {
                        if (jsonObjectLoginResponse.getString("status").equals("1")) {
                            tbGrade.new Delete().perform();

                            JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("grade");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                tbGrade.new create("grade_id, name, created_at",
                                        new String[]{
                                                jsonArray.getJSONObject(i).getString("id"),
                                                jsonArray.getJSONObject(i).getString("name"),
                                                (String) android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date())}).perform();
                            }
                        }
                    } catch (JSONException e) {
                        CustomToast(getApplicationContext(), "JSON Exception!");
                        e.printStackTrace();
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
            } catch (TimeoutException e) {
                CustomToast(getApplicationContext(), "Connection Time Out!");
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    private void displayVersion() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = "Version " + pInfo.versionName;
            loginActivityVersion.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

}
