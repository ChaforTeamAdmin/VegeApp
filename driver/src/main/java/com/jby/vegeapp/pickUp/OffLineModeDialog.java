package com.jby.vegeapp.pickUp;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


import com.jby.vegeapp.R;

import java.util.Objects;

public class OffLineModeDialog extends DialogFragment {

    View rootView;
    private Button confirmButton;
    private OffLineModeDialogCallBack offLineModeDialogCallBack;

    public OffLineModeDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.off_line_mode_dialog, container);
        objectInitialize();
        objectSetting();

        return rootView;
    }

    private void objectInitialize() {
        confirmButton = rootView.findViewById(R.id.off_line_mode_dialog_button);

    }

    private void objectSetting() {
        offLineModeDialogCallBack = (OffLineModeDialogCallBack) getActivity();
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                offLineModeDialogCallBack.clear();
                dismiss();
            }
        });
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

    public interface OffLineModeDialogCallBack {
        void clear();
    }

}