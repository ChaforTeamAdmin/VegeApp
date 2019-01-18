package com.jby.vegeapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chaos.view.PinView;
import com.facebook.stetho.Stetho;
import com.jby.vegeapp.shareObject.ApiDataObject;
import com.jby.vegeapp.shareObject.ApiManager;
import com.jby.vegeapp.shareObject.AsyncTaskManager;
import com.jby.vegeapp.shareObject.SystemLanguage;
import com.jby.vegeapp.sharePreference.SharedPreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LoginActivity extends AppCompatActivity implements TextWatcher {
    private PinView loginActivityPassword;
    private TextView loginActivityLabel, loginActivityStatus, loginActivityForgotPassword;
    private RelativeLayout loginActivityProgressBarLayout;

    private Handler handler;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        isLogin();
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        loginActivityPassword = findViewById(R.id.activity_login_password);
        loginActivityLabel = findViewById(R.id.activity_login_label_password);
        loginActivityStatus = findViewById(R.id.activity_login_status);
        loginActivityForgotPassword = findViewById(R.id.activity_login_forgot_password);

        loginActivityProgressBarLayout = findViewById(R.id.activity_login_progress_bar_layout);
        handler = new Handler();
        Stetho.initializeWithDefaults(this);

    }

    private void objectSetting() {
        loginActivityPassword.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        if(editable.length() == 6){

//            loginActivityPassword.setEnabled(false);
            if(checkNetworkConnection()){
                loginActivityProgressBarLayout.setVisibility(View.VISIBLE);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        login();
                    }
                },200);
            }
            else{
                Toast.makeText(this, "No Network Connection!", Toast.LENGTH_SHORT).show();
                editable.clear();
            }

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
    
    private void login(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("password", loginActivityPassword.getText().toString()));
        
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
                    Log.d("jsonObject", "jsonObject: " +  jsonObjectLoginResponse);

                    showStatus(jsonObjectLoginResponse.getString("status"));
                    if(jsonObjectLoginResponse.getString("status").equals("1")){
                        SharedPreferenceManager.setUserId(this, jsonObjectLoginResponse.getString("driver_id"));
                        SharedPreferenceManager.setUsername(this, jsonObjectLoginResponse.getString("username"));
                        isLogin();
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

    private void showStatus(String status){
        loginActivityStatus.setVisibility(View.VISIBLE);
        if(!status.equals("2")){
            loginActivityStatus.setText(languageSetting(3));
        }
        else{
            loginActivityStatus.setText(languageSetting(2));
        }
        loginActivityPassword.setText("");
        loginActivityProgressBarLayout.setVisibility(View.INVISIBLE);
    }

    private String languageSetting(int position){
        SystemLanguage systemLanguage = new SystemLanguage(this, SharedPreferenceManager.getLanguageId(this));
        return systemLanguage.language(position);
    }

    private void isLogin(){
        if(!SharedPreferenceManager.getUserId(this).equals("default")){
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }

    //    snackBar setting
    public void showSnackBar(String message) {
        final Snackbar snackbar = Snackbar.make(loginActivityProgressBarLayout, message, Snackbar.LENGTH_SHORT);
        snackbar.setActionTextColor(getResources().getColor(R.color.blue));
        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    /*-----------------------------------------------language purpose--------------------------------------------------------------*/
    public void onStart(){
        super.onStart();
        if(SharedPreferenceManager.getLanguageId(this).equals("default")){
            String userDefaultLanguage = Locale.getDefault().getDisplayLanguage();
            setUserDefaultLanguage(userDefaultLanguage);
        }
    }

    public void setUserDefaultLanguage(String userDefaultLanguage){
        switch(userDefaultLanguage){
            case "English":
                SharedPreferenceManager.setLanguageId(this, "1");
                break;
            case "中文":
                SharedPreferenceManager.setLanguageId(this, "2");
                break;
            case "Bahasa Malaysia":
                SharedPreferenceManager.setLanguageId(this, "3");
                break;
            case "Bahasa Melayu":
                SharedPreferenceManager.setLanguageId(this, "3");
                break;
            default:
                SharedPreferenceManager.setLanguageId(this, "1");
                break;
        }
    }
}
