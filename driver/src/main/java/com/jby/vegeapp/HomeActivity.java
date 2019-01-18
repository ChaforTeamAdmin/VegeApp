package com.jby.vegeapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import com.jby.vegeapp.basket.BasketActivity;
import com.jby.vegeapp.pickUp.PickUpActivity;
import com.jby.vegeapp.shareObject.SystemLanguage;
import com.jby.vegeapp.sharePreference.SharedPreferenceManager;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private CardView homeActivityPickUp, homeActivityDeliver, homeActivityBasket, homeActivityHistory, homeActivitySignOut;
    private TextView homeActivityLabelPickUp, homeActivityLabelDeliver, homeActivityLabelBasket, homeActivityLabelHistory,
            homeActivityLabelSignOut;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        objectInitialize();
        objectSetting();

    }

    private void objectInitialize() {
        homeActivityPickUp = findViewById(R.id.activity_home_pick_up_vege);
        homeActivityDeliver = findViewById(R.id.activity_home_deliver);
        homeActivityBasket = findViewById(R.id.activity_home_basket);
        homeActivityHistory = findViewById(R.id.activity_home_history);
        homeActivitySignOut = findViewById(R.id.activity_home_sign_out);

        homeActivityLabelPickUp = findViewById(R.id.activity_home_label_pick_up_vege);
        homeActivityLabelDeliver = findViewById(R.id.activity_home_label_deliver);
        homeActivityLabelBasket = findViewById(R.id.activity_home_label_basket);
        homeActivityLabelHistory = findViewById(R.id.activity_home_label_history);
        homeActivityLabelSignOut = findViewById(R.id.activity_home_label_sign_out);
    }

    private void objectSetting() {
        homeActivityPickUp.setOnClickListener(this);
        homeActivityDeliver.setOnClickListener(this);
        homeActivityBasket.setOnClickListener(this);
        homeActivityHistory.setOnClickListener(this);
        homeActivitySignOut.setOnClickListener(this);

        setUpLanguage();
    }

    private void setUpLanguage(){
        homeActivityLabelPickUp.setText(languageSetting(4));
        homeActivityLabelDeliver.setText(languageSetting(5));
        homeActivityLabelBasket.setText(languageSetting(6));
        homeActivityLabelHistory.setText(languageSetting(7));
        homeActivityLabelSignOut.setText(languageSetting(8));
    }

    private String languageSetting(int position){
        SystemLanguage systemLanguage = new SystemLanguage(this, SharedPreferenceManager.getLanguageId(this));
        return systemLanguage.language(position);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.activity_home_pick_up_vege:
                startActivity(new Intent(this, PickUpActivity.class));
                break;
            case R.id.activity_home_deliver:
                break;
            case R.id.activity_home_basket:
                startActivity(new Intent(this, BasketActivity.class));
                break;
            case R.id.activity_home_history:
                break;
            case R.id.activity_home_sign_out:
                alertMessage(languageSetting(9), languageSetting(10));
                break;
        }
    }

    public void alertMessage(String title,String content){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(content);
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        logout();
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

    private void logout(){
        SharedPreferenceManager.setUserId(this, "default");
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
