package com.jby.admin.stock.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jby.admin.R;
import com.jby.admin.object.ProductDetailChildObject;

import com.jby.admin.others.SwipeDismissTouchListener;
import com.jby.admin.shareObject.ApiDataObject;
import com.jby.admin.shareObject.ApiManager;
import com.jby.admin.shareObject.AsyncTaskManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.admin.shareObject.CustomToast.CustomToast;

public class SpoilDialog extends DialogFragment implements View.OnClickListener {
    View rootView;
    private Button spoilDialogCount, spoilDialogNotCount;
    private EditText spoilDialogWeight;

    private String product_id, product_weight;
    ArrayList<ProductDetailChildObject> spoiledWeightList = new ArrayList<ProductDetailChildObject>();
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;

    public SpoilDialogCallBack spoilDialogCallBack;

    public SpoilDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.spoil_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        try {
            spoilDialogCallBack = (SpoilDialogCallBack) getParentFragment();
        } catch (ClassCastException e) {
            Log.d("SpoilDialog", "Unable to register call back");
        }

        spoilDialogWeight = rootView.findViewById(R.id.spoil_dialog_input);
        spoilDialogCount = rootView.findViewById(R.id.spoil_dialog_count_button);
        spoilDialogNotCount = rootView.findViewById(R.id.spoil_dialog_not_count_button);
    }

    private void objectSetting() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            spoiledWeightList = (ArrayList<ProductDetailChildObject>) bundle.getSerializable("spoil_weight_list");
        }
        spoilDialogCount.setOnClickListener(this);
        spoilDialogNotCount.setOnClickListener(this);
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
            case R.id.spoil_dialog_count_button:
                checkingInput("count");
                break;
            case R.id.spoil_dialog_not_count_button:
                checkingInput("not_count");
                break;
        }
    }

    private void checkingInput(String type) {
        try {
            double spoilWeight = Double.valueOf(spoilDialogWeight.getText().toString().trim());
            if (spoilWeight > 0) {
                /*
                 * make sure all fo the weight is more then spoil weight
                 * */
                for (int i = 0; i < spoiledWeightList.size(); i++) {
                    if (spoilWeight >= Double.valueOf(spoiledWeightList.get(i).getWeight())) {
                        Toast.makeText(getActivity(), "Spoil weight can't exceed the total weight!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                /*
                 * if every time okay then upload it one by one
                 * */
                for (int i = 0; i < spoiledWeightList.size(); i++) {
                    remarkSpoil(
                            spoiledWeightList.get(i).getId(),
                            spoiledWeightList.get(i).getWeight(),
                            String.valueOf(spoilWeight),
                            type,
                            i == spoiledWeightList.size() - 1);
                }

            } else CustomToast(getActivity(), "Invalid Input!");

        } catch (NumberFormatException e) {
            CustomToast(getActivity(), "Invalid Input!");
        }
    }

    private void remarkSpoil(final String product_id, final String product_weight, final String spoilWeight, final String type, final boolean stop) {
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("id", product_id));
        apiDataObjectArrayList.add(new ApiDataObject("weight", product_weight));
        apiDataObjectArrayList.add(new ApiDataObject("spoil_weight", spoilWeight));
        apiDataObjectArrayList.add(new ApiDataObject("type", type));

        asyncTaskManager = new AsyncTaskManager(
                getActivity(),
                new ApiManager().stock,
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
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (stop) {
                                    CustomToast(getActivity(), "Update Successfully!");
                                    spoilDialogCallBack.reset();
                                    dismiss();
                                }
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

    public interface SpoilDialogCallBack {
        void reset();
    }
}
