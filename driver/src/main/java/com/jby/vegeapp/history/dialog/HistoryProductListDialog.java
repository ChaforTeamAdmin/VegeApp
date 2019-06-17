package com.jby.vegeapp.history.dialog;

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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jby.vegeapp.R;
import com.jby.vegeapp.adapter.delivery.DeliverProductGridAdapter;
import com.jby.vegeapp.delivery.dialog.RemarkDialog;
import com.jby.vegeapp.object.product.ProductChildObject;
import com.jby.vegeapp.object.product.ProductObject;
import com.jby.vegeapp.object.product.ProductParentObject;
import com.jby.vegeapp.others.SwipeDismissTouchListener;
import com.jby.vegeapp.shareObject.ApiDataObject;
import com.jby.vegeapp.shareObject.ApiManager;
import com.jby.vegeapp.shareObject.AsyncTaskManager;
import com.jby.vegeapp.sharePreference.SharedPreferenceManager;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.vegeapp.shareObject.CustomToast.CustomToast;


public class HistoryProductListDialog extends DialogFragment implements View.OnClickListener, DeliverProductGridAdapter.ProductGridAdapterCallBack,
        RemarkDialog.RemarkDialogCallBack, ProductDialog.ProductDialogCallBack {
    View rootView;

    private ProgressBar historyProductDialogProgressBar;
    private Button historyProductDialogCancelButton;
    private TextView historyProductDialogTitle, historyProductDialogFarmer, historyProductDialogQuantity;
    private ImageView historyProductDialogProductImage, historyProductDialogEdit;

    private GridView historyProductDialogGridView;
    private ArrayList<ProductChildObject> productChildObjectArrayList;
    private DeliverProductGridAdapter historyProductGridAdapter;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;

    private ProductParentObject productParentObject;
    private ProductChildObject productChildObject;
    private String target, targetID, doID, date, farmer_id;
    private boolean isPickUp = false;

    public HistoryProductListDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.deliver_product_list_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        historyProductDialogProgressBar = rootView.findViewById(R.id.progress_bar);
        historyProductDialogGridView = rootView.findViewById(R.id.deliver_product_list_dialog_list);
        historyProductDialogCancelButton = rootView.findViewById(R.id.deliver_product_list_dialog_cancel);
        historyProductDialogTitle = rootView.findViewById(R.id.deliver_product_list_dialog_product);
        historyProductDialogFarmer = rootView.findViewById(R.id.deliver_product_list_dialog_farmer);
        historyProductDialogProductImage = rootView.findViewById(R.id.deliver_product_list_dialog_product_image);
        historyProductDialogEdit = rootView.findViewById(R.id.deliver_product_list_dialog_edit);
        historyProductDialogQuantity = rootView.findViewById(R.id.deliver_product_list_dialog_quantity);

        productChildObjectArrayList = new ArrayList<>();
        historyProductGridAdapter = new DeliverProductGridAdapter(getActivity(), productChildObjectArrayList, this);
    }

    private void objectSetting() {
        historyProductDialogGridView.setAdapter(historyProductGridAdapter);
        historyProductDialogCancelButton.setOnClickListener(this);
        historyProductDialogEdit.setOnClickListener(this);

        Bundle bundle = getArguments();
        if (bundle != null) {
            productParentObject = (ProductParentObject) bundle.getSerializable("parent_object");
            productChildObject = (ProductChildObject) bundle.getSerializable("child_object");

            target = bundle.getString("target");
            doID = bundle.getString("do_id");
            date = bundle.getString("date");
            targetID = bundle.getString("target_id");
            farmer_id = bundle.getString("farmer_id");
            isPickUp = (doID.equals("0"));

            historyProductDialogEdit.setVisibility(isPickUp ? View.VISIBLE : View.GONE);
            historyProductDialogFarmer.setText(target);
            historyProductDialogTitle.setText(productParentObject.getName());
            Picasso.get().load(new ApiManager().img_product + productParentObject.getPicture()).error(R.drawable.image_error).resize(100, 100).into(historyProductDialogProductImage);

            showProgressBar(true);
            fetchAllItem();
        }
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
            case R.id.deliver_product_list_dialog_cancel:
                dismiss();
                break;
        }
    }

    /*---------------------------------------------------------------------------fetch stock and list view purpose-----------------------------------------------*/
    private ArrayList<ApiDataObject> getApiDataObjectArrayList() {
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("product_id", productParentObject.getId()));
        apiDataObjectArrayList.add(new ApiDataObject(isPickUp ? "farmer_id" : "do_id", isPickUp ? targetID : doID));
        if (isPickUp) {
            apiDataObjectArrayList.add(new ApiDataObject("pick_up_driver_id", SharedPreferenceManager.getUserId(getActivity())));
            apiDataObjectArrayList.add(new ApiDataObject("date", date));
            apiDataObjectArrayList.add(new ApiDataObject("weight", productChildObject.getWeight()));
        } else {
            apiDataObjectArrayList.add(new ApiDataObject("farmer_id", farmer_id));
        }
        return apiDataObjectArrayList;
    }

    private void fetchAllItem() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        (isPickUp ? new ApiManager().pick_up_history : new ApiManager().deliver_history),
                        new ApiManager().getResultParameter(
                                "",
                                new ApiManager().setData(getApiDataObjectArrayList()),
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
                                JSONArray jsonArray = null;
                                try {
                                    jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray(isPickUp ? "pick_up_history_item_detail" : "delivery_history_item_detail");
                                    setGridViewData(jsonArray);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
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
                    notifyDataSetChanged();
                    showProgressBar(false);
                }
            }
        }).start();
    }

    private void setGridViewData(JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                ProductChildObject object = new ProductChildObject();
                object.setWeight(jsonArray.getJSONObject(i).getString("weight"));
                object.setId(jsonArray.getJSONObject(i).getString("id"));
                object.setRemark(jsonArray.getJSONObject(i).getString("remark"));
                object.setStatus(jsonArray.getJSONObject(i).getString("status"));
                productChildObjectArrayList.add(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void notifyDataSetChanged() {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                historyProductGridAdapter.notifyDataSetChanged();
            }
        });
    }

    /*------------------------------------------------------------------------------product dialog call back-----------------------------------------------------*/

    @Override
    public void selectProduct(String id, String name, String image_pic) {
        historyProductDialogTitle.setText(name);

    }

    private void showProgressBar(final boolean show) {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                historyProductDialogProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onClick(final int position) {
        Bundle bundle = new Bundle();
        bundle.putString("remark", productChildObjectArrayList.get(position).getRemark());
        bundle.putString("stock_id", productChildObjectArrayList.get(position).getId());

        DialogFragment dialogFragment = new RemarkDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getChildFragmentManager(), "");
    }

    @Override
    public void refresh() {
        productChildObjectArrayList.clear();
        fetchAllItem();
    }

}