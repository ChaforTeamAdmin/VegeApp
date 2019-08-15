package com.jby.admin.setting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jby.admin.MainActivity;
import com.jby.admin.R;
import com.jby.admin.registration.LoginActivity;
import com.jby.admin.setting.dialog.StockLimitDialog;
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

import static com.jby.admin.shareObject.CustomToast.CustomToast;


public class SettingFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, StockLimitDialog.StockLimitDialogCallBack {
    View rootView;
    RelativeLayout settingFragmentLogOut, settingFragmentContactUs, settingFragmentNotification, settingFragmentStockLimitLayout;
    SwitchCompat settingFragmentRemarkNotificationSwitch, settingFragmentPickUpNotificationSwitch, settingFragmentDeliverNotificationSwitch;
    SwitchCompat settingFragmentLocationSwitch, settingFragmentGradeSwitch, settingFragmentPriceSwitch;
    TextView settingFragmentVersion, settingFragmentStockLimit;
    /*
     * Async task
     * */
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private Handler handler;

    public SettingFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_setting, container, false);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        settingFragmentLogOut = rootView.findViewById(R.id.fragment_setting_log_out_button);
        settingFragmentContactUs = rootView.findViewById(R.id.fragment_setting_contact_us);

        settingFragmentRemarkNotificationSwitch = rootView.findViewById(R.id.fragment_setting_remark_notification_button);
        settingFragmentPickUpNotificationSwitch = rootView.findViewById(R.id.fragment_setting_pick_up_notification_button);
        settingFragmentDeliverNotificationSwitch = rootView.findViewById(R.id.fragment_setting_delivery_notification_button);
        settingFragmentStockLimitLayout = rootView.findViewById(R.id.fragment_setting_stock_limit_layout);

        settingFragmentLocationSwitch = rootView.findViewById(R.id.fragment_setting_location_button);
        settingFragmentGradeSwitch = rootView.findViewById(R.id.fragment_setting_grade_button);
        settingFragmentPriceSwitch = rootView.findViewById(R.id.fragment_setting_price_button);
        settingFragmentStockLimit = rootView.findViewById(R.id.fragment_setting_stock_limit);

        settingFragmentVersion = rootView.findViewById(R.id.fragment_setting_version_name);
    }

    private void objectSetting() {
        settingFragmentLogOut.setOnClickListener(this);
        settingFragmentContactUs.setOnClickListener(this);
        settingFragmentStockLimitLayout.setOnClickListener(this);

        settingFragmentRemarkNotificationSwitch.setOnCheckedChangeListener(this);
        settingFragmentPickUpNotificationSwitch.setOnCheckedChangeListener(this);
        settingFragmentDeliverNotificationSwitch.setOnCheckedChangeListener(this);

        settingFragmentLocationSwitch.setOnCheckedChangeListener(this);
        settingFragmentGradeSwitch.setOnCheckedChangeListener(this);
        settingFragmentPriceSwitch.setOnCheckedChangeListener(this);

        settingFragmentRemarkNotificationSwitch.setChecked(SharedPreferenceManager.getShowNotification(getActivity(), "remark_notification"));
        settingFragmentPickUpNotificationSwitch.setChecked(SharedPreferenceManager.getShowNotification(getActivity(), "pick_up_notification"));
        settingFragmentDeliverNotificationSwitch.setChecked(SharedPreferenceManager.getShowNotification(getActivity(), "delivery_notification"));

        settingFragmentLocationSwitch.setChecked(SharedPreferenceManager.getLocation(getActivity()));
        settingFragmentGradeSwitch.setChecked(SharedPreferenceManager.getGrade(getActivity()));
        settingFragmentPriceSwitch.setChecked(SharedPreferenceManager.getPrice(getActivity()));
        setDayLimit();
        displayVersion();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_setting_log_out_button:
                logOutConfirmation();
                break;
            case R.id.fragment_setting_contact_us:
                openWhatApp();
                break;
            case R.id.fragment_setting_clear_cache:
                showProgressBar(true);
                clearCache();
                break;
            case R.id.fragment_setting_stock_limit_layout:
                openStockLimitDialog();
                break;
        }
    }

    private void openWhatApp() {
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(
                        "https://api.whatsapp.com/send?phone=60143157329&text=I'm%20interested%20in%20your%20car%20for%20sale"
                )));
    }

    private void logOutConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Warning");
        builder.setMessage("Are you sure that you want to log out?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        logOut();
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

    private void logOut() {
        SharedPreferenceManager.setUserId(getActivity(), "default");
        startActivity(new Intent(getActivity(), LoginActivity.class));
        Objects.requireNonNull(getActivity()).finish();
    }

    private void displayVersion() {
        try {
            PackageInfo pInfo = Objects.requireNonNull(getActivity()).getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            String version = "Version " + pInfo.versionName;
            settingFragmentVersion.setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.fragment_setting_remark_notification_button:
                SharedPreferenceManager.setShowNotification(getActivity(), "remark_notification", b);
                break;
            case R.id.fragment_setting_pick_up_notification_button:
                SharedPreferenceManager.setShowNotification(getActivity(), "pick_up_notification", b);
                break;
            case R.id.fragment_setting_delivery_notification_button:
                SharedPreferenceManager.setShowNotification(getActivity(), "delivery_notification", b);
                break;

            case R.id.fragment_setting_location_button:
                SharedPreferenceManager.setLocation(getActivity(), b);
                updateUserSetting();
                break;
            case R.id.fragment_setting_grade_button:
                SharedPreferenceManager.setGrade(getActivity(), b);
                updateUserSetting();
                break;
            case R.id.fragment_setting_price_button:
                SharedPreferenceManager.setPrice(getActivity(), b);
                updateUserSetting();
                break;

        }
    }

    /*
     * clear queue control
     * */
    private void clearCache() {
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("clear_cache", ""));

        asyncTaskManager = new AsyncTaskManager(
                getActivity(),
                new ApiManager().cache,
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
                    try {
                        if (jsonObjectLoginResponse.getString("status").equals("1")) {
                            CustomToast(getActivity(), "Cache cleared Successfully!");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    CustomToast(getActivity(), "Network Error!");
                }
            } catch (InterruptedException e) {
                CustomToast(getActivity(), "Interrupted Exception!");
                e.printStackTrace();
            } catch (ExecutionException e) {
                CustomToast(getActivity(), "Execution Exception!");
                e.printStackTrace();
            } catch (TimeoutException e) {
                CustomToast(getActivity(), "Connection Time Out!");
                e.printStackTrace();
            }
        }
        showProgressBar(false);
    }

    /*
     * grade, location, price control
     * */
    public void updateUserSetting() {
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("update", "1"));
        apiDataObjectArrayList.add(new ApiDataObject("stocked_day_limit", SharedPreferenceManager.getDayLimit(getActivity())));
        apiDataObjectArrayList.add(new ApiDataObject("setting_price", SharedPreferenceManager.getPrice(getActivity()) ? "1" : "0"));
        apiDataObjectArrayList.add(new ApiDataObject("setting_location", SharedPreferenceManager.getLocation(getActivity()) ? "1" : "0"));
        apiDataObjectArrayList.add(new ApiDataObject("setting_grade", SharedPreferenceManager.getGrade(getActivity()) ? "1" : "0"));

        asyncTaskManager = new AsyncTaskManager(
                getActivity(),
                new ApiManager().company,
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
                    try {
                        if (jsonObjectLoginResponse.getString("status").equals("1")) {
                            Log.d("Setting Fragment", "user setting: " + jsonObjectLoginResponse);
                            setDayLimit();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    CustomToast(getActivity(), "Network Error!");
                }
            } catch (InterruptedException e) {
                CustomToast(getActivity(), "Interrupted Exception!");
                e.printStackTrace();
            } catch (ExecutionException e) {
                CustomToast(getActivity(), "Execution Exception!");
                e.printStackTrace();
            } catch (TimeoutException e) {
                CustomToast(getActivity(), "Connection Time Out!");
                e.printStackTrace();
            }
        }
        showProgressBar(false);
    }

    private void setDayLimit() {
        settingFragmentStockLimit.setText(String.format("%s Days", SharedPreferenceManager.getDayLimit(getActivity())));
    }

    private void openStockLimitDialog() {
        DialogFragment stockLimitDialog = new StockLimitDialog();
        stockLimitDialog.show(getChildFragmentManager(), "");
    }

    private void showProgressBar(boolean show) {
        ((MainActivity) Objects.requireNonNull(getActivity())).showProgressBar(show);
    }
}
