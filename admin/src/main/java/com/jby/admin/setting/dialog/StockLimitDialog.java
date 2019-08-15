package com.jby.admin.setting.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.jby.admin.R;
import com.jby.admin.others.KeyboardHelper;
import com.jby.admin.others.SwipeDismissTouchListener;
import com.jby.admin.sharePreference.SharedPreferenceManager;

import java.util.Objects;


import static com.jby.admin.shareObject.CustomToast.CustomToast;

public class StockLimitDialog extends DialogFragment implements View.OnClickListener {
    View rootView;
    private Button stockLimitDialogUpdate;
    private EditText stockLimitDialogDay;
    private Handler handler;
    public StockLimitDialogCallBack stockLimitDialogCallBack;

    public StockLimitDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.stock_limit_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        try {
            stockLimitDialogCallBack = (StockLimitDialogCallBack) getParentFragment();
        } catch (ClassCastException e) {
            Log.d("StockLimitDialog", "Unable to register call back");
        }

        stockLimitDialogUpdate = rootView.findViewById(R.id.stock_limit_dialog_update_button);
        stockLimitDialogDay = rootView.findViewById(R.id.stock_limit_dialog_input);
        handler = new Handler();
    }

    private void objectSetting() {
        stockLimitDialogUpdate.setOnClickListener(this);

        stockLimitDialogDay.append(SharedPreferenceManager.getDayLimit(getContext()));
        stockLimitDialogDay.setSelectAllOnFocus(true);
        stockLimitDialogDay.requestFocus();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                KeyboardHelper.openSoftKeyboard(getActivity(), stockLimitDialogDay);
            }
        }, 300);
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

    @Override
    public void onResume() {
        super.onResume();
        Dialog d = getDialog();
        Objects.requireNonNull(d.getWindow()).getDecorView().setOnTouchListener(new SwipeDismissTouchListener(d.getWindow().getDecorView(), null,
                new SwipeDismissTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(Object token) {
                        return true;
                    }

                    @Override
                    public void onDismiss(View view, Object token) {
                        dismiss();
                    }
                }));
    }


    @Override
    public void dismiss() {
        KeyboardHelper.hideSoftKeyboard(getActivity(), stockLimitDialogDay);
        super.dismiss();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.stock_limit_dialog_update_button:
                checkingInput();
                break;
        }
    }

    private void checkingInput() {
        try {
            if (stockLimitDialogDay.getText().toString().trim().equals("") && stockLimitDialogDay.getText().toString().trim().equals("0")) {
                CustomToast(getActivity(), "Minimum day is 1!");
            } else {
                SharedPreferenceManager.setDayLimit(getContext(), stockLimitDialogDay.getText().toString().trim());
                stockLimitDialogCallBack.updateUserSetting();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dismiss();
                    }
                }, 150);
            }

        } catch (NumberFormatException e) {
            CustomToast(getActivity(), "Invalid Input!");
        }
    }

    public interface StockLimitDialogCallBack {
        void updateUserSetting();
    }
}
