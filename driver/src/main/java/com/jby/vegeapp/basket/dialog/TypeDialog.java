package com.jby.vegeapp.basket.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jby.vegeapp.R;
import java.util.Objects;


public class TypeDialog extends DialogFragment implements View.OnClickListener {
    View rootView;
    private TextView typeDialogFarmer, typeDialogCustomer;
    public TypeDialogCallBack typeDialogCallBack;

    public TypeDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.type_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            Objects.requireNonNull(d.getWindow()).setLayout(width, height);
            d.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            d.getWindow().setWindowAnimations(R.style.dialog_up_down);
        }
    }

    private void objectInitialize() {
        typeDialogFarmer = rootView.findViewById(R.id.type_dialog_farmer);
        typeDialogCustomer = rootView.findViewById(R.id.type_dialog_customer);

        typeDialogCallBack = (TypeDialogCallBack)getActivity();
    }

    private void objectSetting() {
        typeDialogFarmer.setOnClickListener(this);
        typeDialogCustomer.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.type_dialog_customer:
                typeDialogCallBack.type("Customer");
                break;
            case R.id.type_dialog_farmer:
                typeDialogCallBack.type("Farmer");
                break;
        }
        dismiss();
    }


    public interface TypeDialogCallBack{
        void type(String type);
    }
}
