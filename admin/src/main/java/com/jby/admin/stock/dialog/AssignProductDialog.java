package com.jby.admin.stock.dialog;

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
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jby.admin.R;
import com.jby.admin.adapter.ProductGridAdapter;
import com.jby.admin.object.ProductDetailChildObject;
import com.jby.admin.others.SwipeDismissTouchListener;
import com.jby.admin.shareObject.ApiDataObject;
import com.jby.admin.shareObject.ApiManager;
import com.jby.admin.shareObject.AsyncTaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.admin.shareObject.CustomToast.CustomToast;


public class AssignProductDialog extends DialogFragment implements View.OnClickListener, ProductGridAdapter.ProductGridAdapterCallBack {
    View rootView;

    private ProgressBar assignProductDialogProgressBar;
    private GridView assignProductDialogGridView;
    private Button assignProductDialogCancelButton;
    private ProductGridAdapter productGridAdapter;
    private TextView assignProductDialogTitle;
    private ArrayList<ProductDetailChildObject> productDetailChildObjectArrayList;
    AssignProductDialogCallBack assignProductDialogCallBack;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private Handler handler;
    private String customerID, farmerID, productID, farmer_name;
    //selected item id
    private List<String> deliveryProductIDList;

    public AssignProductDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.assign_product_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        assignProductDialogProgressBar = rootView.findViewById(R.id.assign_product_dialog_progress_bar);
        assignProductDialogGridView = rootView.findViewById(R.id.assign_product_dialog_grid_view);
        assignProductDialogCancelButton = rootView.findViewById(R.id.assign_product_dialog_cancel_button);
        assignProductDialogTitle = rootView.findViewById(R.id.assign_product_dialog_title);


        productDetailChildObjectArrayList = new ArrayList<>();
        productGridAdapter = new ProductGridAdapter(getActivity(), productDetailChildObjectArrayList, this);
        handler = new Handler();
    }

    private void objectSetting() {
        assignProductDialogGridView.setAdapter(productGridAdapter);
        assignProductDialogCancelButton.setOnClickListener(this);
        assignProductDialogCallBack = (AssignProductDialogCallBack) getParentFragment();

        Bundle bundle = getArguments();
        if (bundle != null) {
            farmerID = bundle.getString("farmer_id");
            farmer_name = bundle.getString("farmer_name");
            customerID = bundle.getString("customer_id");
            productID = bundle.getString("product_id");
            productID = bundle.getString("product_id");
            deliveryProductIDList = bundle.getStringArrayList("delivery_product_list_id");
            assignProductDialogTitle.setText(farmer_name);
            showProgressBar(true);
            fetchAllStockByFarmer();
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
            case R.id.assign_product_dialog_cancel_button:
                dismiss();
                break;
        }
    }

    /*---------------------------------------------------------------------------fetch stock and list view purpose-----------------------------------------------*/
    private void fetchAllStockByFarmer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("product_id", productID));
                apiDataObjectArrayList.add(new ApiDataObject("customer_id", customerID));
                apiDataObjectArrayList.add(new ApiDataObject("farmer_id", farmerID));
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
                        Log.d("haha", "jsonObject: " + jsonObjectLoginResponse);
                        if (jsonObjectLoginResponse != null) {
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                String status;
                                JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("stock_detail");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    if (deliveryProductIDList.contains(jsonArray.getJSONObject(i).getString("id"))){
                                        status = "1";
//                                        getActivity().runOnUiThread(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                assignProductDialogCallBack.updateListViewQuantity("1");
//                                            }
//                                        });
                                    }

                                    else status = jsonArray.getJSONObject(i).getString("status");

                                    productDetailChildObjectArrayList.add(new ProductDetailChildObject(
                                            jsonArray.getJSONObject(i).getString("id"),
                                            jsonArray.getJSONObject(i).getString("price"),
                                            jsonArray.getJSONObject(i).getString("grade"),
                                            jsonArray.getJSONObject(i).getString("date"),
                                            status,
                                            jsonArray.getJSONObject(i).getString("weight"),
                                            jsonArray.getJSONObject(i).getString("do_id"),
                                            "0",
                                            "0"));
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

    private void notifyDataSetChanged() {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                productGridAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showProgressBar(final boolean show) {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                assignProductDialogProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void assignItem(final int position, final String status, final String do_id) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                assignProductDialogCallBack.setDeliveryProductIDList(productDetailChildObjectArrayList.get(position).getId(), do_id);
                assignProductDialogCallBack.updateListViewQuantity(status);
            }
        }, 200);
    }

    @Override
    public void removeItem(final int position) {
        //update local information
        assignProductDialogCallBack.updateListViewQuantity("0");
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("id", productDetailChildObjectArrayList.get(position).getId()));
                apiDataObjectArrayList.add(new ApiDataObject("do_id", productDetailChildObjectArrayList.get(position).getDo_id()));
                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().delivery,
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
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    CustomToast(getActivity(), "Remove Successfully!");
                                    productDetailChildObjectArrayList.clear();
                                    fetchAllStockByFarmer();
                                }
                            });
                            Log.d("jsonObject", "jsonObject: " + jsonObjectLoginResponse);
                        } else {
                            CustomToast(getActivity(), "Network Error!");
                        }
                    } catch (InterruptedException e) {
                        CustomToast(getActivity(), "Interrupted Exception!");
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        CustomToast(getActivity(), "Execution Exception!");
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        CustomToast(getActivity(), "Connection Time Out!");
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public interface AssignProductDialogCallBack {
        void updateListViewQuantity(String status);
        void setDeliveryProductIDList(String id, String deliveryOrderID);

    }

}