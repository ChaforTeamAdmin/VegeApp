package com.jby.admin.remark;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.jby.admin.R;
import com.jby.admin.object.RemarkChildObject;
import com.jby.admin.others.SwipeDismissTouchListener;
import com.jby.admin.shareObject.ApiDataObject;
import com.jby.admin.shareObject.ApiManager;
import com.jby.admin.shareObject.AsyncTaskManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.admin.shareObject.CustomToast.CustomToast;

public class EditRemarkDialog extends DialogFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    View rootView;
    private ProgressBar progressBar;
    private TextView remarkDialogTitle;
    private TextView remarkDialogRemarkedLabel;
    private Button remarkDialogRemarkButton;
    private Spinner remarkDialogSpinner;
    private EditText remarkDialogInput;
    private String remark_status = "", remark = "", id = "";
    private List<String> categories = new ArrayList<>();
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    RemarkDialogCallBack remarkDialogCallBack;
    RemarkChildObject object;

    public EditRemarkDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.edit_remark_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        progressBar = rootView.findViewById(R.id.progress_bar);
        remarkDialogTitle = rootView.findViewById(R.id.remark_dialog_title);

        remarkDialogRemarkButton = rootView.findViewById(R.id.remark_dialog_remark_button);
        remarkDialogSpinner = rootView.findViewById(R.id.remark_dialog_spinner);
        remarkDialogInput = rootView.findViewById(R.id.remark_dialog_input);
    }

    private void objectSetting() {
        remarkDialogCallBack = (RemarkDialogCallBack) getParentFragment();
        remarkDialogRemarkButton.setOnClickListener(this);
        remarkDialogSpinner.setOnItemSelectedListener(this);
        setupSpinner();

        if (getArguments() != null) {
            object = (RemarkChildObject) getArguments().getSerializable("object");
            remark_status = (object.getRemark_status());
            remark = (object.getRemark());
            id = (object.getId());

            remarkDialogInput.append(remark);
            remarkDialogSpinner.setSelection(remark_status.equals("2") ? 0 : 1);
        }
    }

    private void setupSpinner() {
        categories.add("Less");
        categories.add("More");
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(Objects.requireNonNull(getActivity()), R.layout.spinner_layout, categories);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        remarkDialogSpinner.setAdapter(dataAdapter);
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.remark_dialog_remark_button:
                checkingInput();
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        remark_status = (i == 0 ? "2" : "3");
    }

    private void checkingInput() {
        try {
            remark = (remarkDialogInput.getText().toString().trim());
            updateRemarkItem();

        } catch (NumberFormatException e) {
            showSnackBar("Please enter the correct weight");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void updateRemarkItem() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("id", object.getId()));
                apiDataObjectArrayList.add(new ApiDataObject("product_id", object.getProduct_id()));
                apiDataObjectArrayList.add(new ApiDataObject("remark_weight", remark));
                apiDataObjectArrayList.add(new ApiDataObject("remark_status", remark_status));
                apiDataObjectArrayList.add(new ApiDataObject("type", object.getRemark_type()));
                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().remark,
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
                        Log.d("json", "jsonObject: " + jsonObjectLoginResponse);
                        if (jsonObjectLoginResponse != null) {
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                remarkDialogCallBack.reset();
                                dismiss();
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
                    } catch (JSONException e) {
                        CustomToast(getActivity(), "JSON Exception!");
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        CustomToast(getActivity(), "Connection Time Out!");
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    //    snackBar setting
    public void showSnackBar(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT);
                snackbar.setActionTextColor(getResources().getColor(R.color.blue));
                snackbar.setAction("Dismiss", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                    }
                });
                snackbar.show();
            }
        });
    }

    public interface RemarkDialogCallBack {
        void reset();
    }
}