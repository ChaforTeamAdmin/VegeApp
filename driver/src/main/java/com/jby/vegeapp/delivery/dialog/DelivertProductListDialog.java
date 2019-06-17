package com.jby.vegeapp.delivery.dialog;

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
import com.jby.vegeapp.object.product.ProductChildObject;
import com.jby.vegeapp.others.SwipeDismissTouchListener;
import com.jby.vegeapp.shareObject.ApiDataObject;
import com.jby.vegeapp.shareObject.ApiManager;
import com.jby.vegeapp.shareObject.AsyncTaskManager;
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


public class DelivertProductListDialog extends DialogFragment implements View.OnClickListener, DeliverProductGridAdapter.ProductGridAdapterCallBack,
        RemarkDialog.RemarkDialogCallBack {
    View rootView;

    private ProgressBar deliverProductDialogProgressBar;
    private GridView deliverProductDialogGridView;
    private Button deliverProductDialogCancelButton;
    private DeliverProductGridAdapter deliverProductGridAdapter;
    private TextView deliverProductDialogTitle, deliverProductDialogFarmer;
    private ImageView deliverProductDialogProductImage;
    private ArrayList<ProductChildObject> productChildObjectArrayList;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private String customerID, productID, productPic, product, farmer, farmer_id;

    public DelivertProductListDialog() {
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
        deliverProductDialogProgressBar = rootView.findViewById(R.id.progress_bar);
        deliverProductDialogGridView = rootView.findViewById(R.id.deliver_product_list_dialog_list);
        deliverProductDialogCancelButton = rootView.findViewById(R.id.deliver_product_list_dialog_cancel);
        deliverProductDialogTitle = rootView.findViewById(R.id.deliver_product_list_dialog_product);
        deliverProductDialogFarmer = rootView.findViewById(R.id.deliver_product_list_dialog_farmer);
        deliverProductDialogProductImage = rootView.findViewById(R.id.deliver_product_list_dialog_product_image);

        productChildObjectArrayList = new ArrayList<>();
        deliverProductGridAdapter = new DeliverProductGridAdapter(getActivity(), productChildObjectArrayList, this);
    }

    private void objectSetting() {
        deliverProductDialogGridView.setAdapter(deliverProductGridAdapter);
        deliverProductDialogCancelButton.setOnClickListener(this);

        Bundle bundle = getArguments();
        if (bundle != null) {
            farmer_id = bundle.getString("farmer_id");
            farmer = bundle.getString("farmer");
            customerID = bundle.getString("customer_id");
            productID = bundle.getString("product_id");
            product = bundle.getString("product");
            productPic = bundle.getString("product_pic");

            deliverProductDialogFarmer.setText(farmer);
            deliverProductDialogTitle.setText(product);
            Picasso.get().load(new ApiManager().img_product + productPic).error(R.drawable.image_error).resize(100, 100).into(deliverProductDialogProductImage);

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
    private void fetchAllItem() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("product_id", productID));
                apiDataObjectArrayList.add(new ApiDataObject("customer_id", customerID));
                apiDataObjectArrayList.add(new ApiDataObject("farmer_id", farmer_id));
                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().deliver,
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
                                JSONArray jsonArray = null;
                                try {
                                    jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("item");
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
                deliverProductGridAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showProgressBar(final boolean show) {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deliverProductDialogProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
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