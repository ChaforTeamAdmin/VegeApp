package com.jby.admin.registration;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.jby.admin.MainActivity;
import com.jby.admin.R;
import com.jby.admin.others.NetworkConnection;
import com.jby.admin.shareObject.ApiDataObject;
import com.jby.admin.shareObject.ApiManager;
import com.jby.admin.shareObject.AsyncTaskManager;
import com.jby.admin.sharePreference.SharedPreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText loginActivityUsername, loginActivityPassword;
    private TextView loginActivityForgotPassword, loginActivityVersion;
    private ImageView loginActivityShowPassword, loginActivityCancelUsername;
    private LinearLayout loginActivityMainLayout;
    private ProgressBar loginActivityProgressBar;

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
        loginActivityUsername = (EditText) findViewById(R.id.activity_login_username);
        loginActivityPassword = (EditText) findViewById(R.id.activity_login_password);

        loginActivityForgotPassword = (TextView) findViewById(R.id.activity_login_forgot_password);
        loginActivityVersion = findViewById(R.id.activity_login_version_name);

        loginActivityShowPassword = (ImageView) findViewById(R.id.activity_login_show_password);
        loginActivityCancelUsername = (ImageView) findViewById(R.id.activity_login_cancel_username);

        loginActivityMainLayout = (LinearLayout) findViewById(R.id.activity_login_parent_layout);
        loginActivityProgressBar = (ProgressBar) findViewById(R.id.login_activity_progress_bar);

        handler = new Handler();

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
            String userID = jsonObject.getString("admin_id");
            String userName = jsonObject.getString("username");
            SharedPreferenceManager.setUserId(this, userID);
            SharedPreferenceManager.setUsername(this, userName);
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
