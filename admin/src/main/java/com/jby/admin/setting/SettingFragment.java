package com.jby.admin.setting;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jby.admin.R;
import com.jby.admin.registration.LoginActivity;
import com.jby.admin.sharePreference.SharedPreferenceManager;

import java.util.Objects;


public class SettingFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    View rootView;
    RelativeLayout settingFragmentLogOut, settingFragmentContactUs, settingFragmentNotification;
    SwitchCompat settingFragmentRemarkNotificationSwitch, settingFragmentPickUpNotificationSwitch, settingFragmentDeliverNotificationSwitch;
    TextView settingFragmentVersion;

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

        settingFragmentVersion = rootView.findViewById(R.id.fragment_setting_version_name);
    }

    private void objectSetting() {
        settingFragmentLogOut.setOnClickListener(this);
        settingFragmentContactUs.setOnClickListener(this);

        settingFragmentRemarkNotificationSwitch.setOnCheckedChangeListener(this);
        settingFragmentPickUpNotificationSwitch.setOnCheckedChangeListener(this);
        settingFragmentDeliverNotificationSwitch.setOnCheckedChangeListener(this);

        settingFragmentRemarkNotificationSwitch.setChecked(SharedPreferenceManager.getShowNotification(getActivity(), "remark_notification"));
        settingFragmentPickUpNotificationSwitch.setChecked(SharedPreferenceManager.getShowNotification(getActivity(), "pick_up_notification"));
        settingFragmentDeliverNotificationSwitch.setChecked(SharedPreferenceManager.getShowNotification(getActivity(), "delivery_notification"));
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
        }
    }

    private void openWhatApp(){
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
        }
    }
}
