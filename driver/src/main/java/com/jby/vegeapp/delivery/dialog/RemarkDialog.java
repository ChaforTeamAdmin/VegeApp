package com.jby.vegeapp.delivery.dialog;

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

import com.jby.vegeapp.R;
import com.jby.vegeapp.others.SwipeDismissTouchListener;
import com.jby.vegeapp.shareObject.ApiDataObject;
import com.jby.vegeapp.shareObject.ApiManager;
import com.jby.vegeapp.shareObject.AsyncTaskManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.vegeapp.shareObject.CustomToast.CustomToast;


public class RemarkDialog extends DialogFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    View rootView;
    private ProgressBar progressBar;
    private TextView remarkDialogTitle, remarkDialogContent;
    private TextView remarkDialogRemarkedLabel;
    private Button remarkDialogRemarkButton;
    private Spinner remarkDialogSpinner;
    private EditText remarkDialogInput;
    private String deliver_remark = "", deliver_remark_status = "", stockID = "";
    private List<String> categories = new ArrayList<>();
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    RemarkDialogCallBack remarkDialogCallBack;

    public RemarkDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.remark_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        progressBar = rootView.findViewById(R.id.progress_bar);
        remarkDialogTitle = rootView.findViewById(R.id.remark_dialog_title);
        remarkDialogContent = rootView.findViewById(R.id.remark_dialog_label);
        remarkDialogRemarkedLabel = rootView.findViewById(R.id.remark_dialog_remarked_label);

        remarkDialogRemarkButton = rootView.findViewById(R.id.remark_dialog_remark_button);
        remarkDialogSpinner = rootView.findViewById(R.id.remark_dialog_spinner);
        remarkDialogInput = rootView.findViewById(R.id.remark_dialog_input);
    }

    private void objectSetting() {
        remarkDialogCallBack = (RemarkDialogCallBack) getParentFragment();
        remarkDialogRemarkButton.setOnClickListener(this);
        remarkDialogSpinner.setOnItemSelectedListener(this);
        setupSpinner();

        stockID = (getArguments() != null ? getArguments().getString("stock_id") : "");
        deliver_remark_status = (getArguments() != null ? getArguments().getString("remark") : "0");
        if (!deliver_remark_status.equals("0")) {
            remarkDialogRemarkedLabel.setVisibility(View.VISIBLE);
            remarkDialogRemarkButton.setVisibility(View.GONE);
            getRemarkDetail();
            setSpinnerValue();
        }
    }

    private void setupSpinner() {
        categories.add("Missing");
        categories.add("Less");
        categories.add("More");
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(Objects.requireNonNull(getActivity()), R.layout.spinner_layout, categories);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        remarkDialogSpinner.setAdapter(dataAdapter);
    }

    private void setSpinnerValue() {
        switch (deliver_remark_status) {
            case "1":
                //missing
                remarkDialogSpinner.setSelection(0);
                break;
            case "2":
                //less
                remarkDialogSpinner.setSelection(1);
                break;
            case "3":
                //more
                remarkDialogSpinner.setSelection(2);
                break;
        }
        remarkDialogSpinner.setEnabled(false);
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
        remarkDialogInput.setVisibility(i == 0 ? View.GONE : View.VISIBLE);
        //missing = 1/less = 2/more = 3 (so position + 1 = status)
        deliver_remark_status = String.valueOf(i+1);
    }

    private void checkingInput() {
        if (!deliver_remark_status.equals("1")) {
            if (remarkDialogInput.getText().toString().equals("")) {
                showSnackBar("Please enter the correct weight");
            } else {
                showProgressBar(true);
                deliver_remark = (remarkDialogInput.getText().toString().trim());
                remarkItem();
            }
        } else {
            showProgressBar(true);
            remarkItem();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void getRemarkDetail() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("stock_id", stockID));
                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().delivery_remark,
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
                                //get weight
                                final String weight = jsonObjectLoginResponse.getJSONObject("remark").getString("delivery_remark");
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //if status less then set the weight
                                        remarkDialogInput.setText(!deliver_remark_status.equals("1") ? weight : "");
                                        remarkDialogInput.setEnabled(false);
                                    }
                                });
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

    private void remarkItem() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("stock_id", stockID));
                apiDataObjectArrayList.add(new ApiDataObject("remark", deliver_remark));
                apiDataObjectArrayList.add(new ApiDataObject("remark_status", deliver_remark_status));
                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().delivery_remark,
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
                                CustomToast(getActivity(), deliver_remark_status.equals("0") ? "Remove Successfully!" : "Remark Successfully!");
                                showProgressBar(false);
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

    public void showProgressBar(final boolean show){
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        remarkDialogCallBack.refresh();
    }

    public interface RemarkDialogCallBack {
        void refresh();
    }
}