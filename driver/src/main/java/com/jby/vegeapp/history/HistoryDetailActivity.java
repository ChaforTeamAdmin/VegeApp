package com.jby.vegeapp.history;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.vegeapp.R;
import com.jby.vegeapp.adapter.history.HistoryProductExpandableAdapter;
import com.jby.vegeapp.history.dialog.HistoryProductListDialog;
import com.jby.vegeapp.object.product.ProductChildObject;
import com.jby.vegeapp.object.product.ProductParentObject;
import com.jby.vegeapp.others.NetworkConnection;
import com.jby.vegeapp.others.NonScrollExpandableListView;
import com.jby.vegeapp.pickUp.PickUpActivity;
import com.jby.vegeapp.pickUp.farmer.FarmerDialog;
import com.jby.vegeapp.pickUp.product.AddProductDialog;
import com.jby.vegeapp.printer.Manager.PrintfManager;
import com.jby.vegeapp.printer.PrintfBlueListActivity;
import com.jby.vegeapp.shareObject.ApiDataObject;
import com.jby.vegeapp.shareObject.ApiManager;
import com.jby.vegeapp.shareObject.AsyncTaskManager;
import com.jby.vegeapp.sharePreference.SharedPreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.vegeapp.Utils.VariableUtils.DELETE_CONFIRMATION;
import static com.jby.vegeapp.Utils.VariableUtils.PROCEED_TO_PRINT;
import static com.jby.vegeapp.Utils.VariableUtils.UPDATE_PICK_UP_HISTORY;
import static com.jby.vegeapp.shareObject.CustomDialog.CustomDialog;
import static com.jby.vegeapp.shareObject.CustomToast.CustomToast;
import static com.jby.vegeapp.Utils.VariableUtils.UPDATE_LIST;

public class HistoryDetailActivity extends AppCompatActivity implements HistoryProductExpandableAdapter.ProductExpandableAdapterCallBack,
        View.OnClickListener, FarmerDialog.FarmerDialogCallBack, AddProductDialog.AddProductDialogCallBack {

    private NonScrollExpandableListView historyDetailActivityListView;
    private HistoryProductExpandableAdapter historyProductExpandableAdapter;
    private ArrayList<ProductParentObject> productObjectArrayList;
    private Toolbar toolbar;
    private TextView edit;
    //farmer detail
    private CardView historyDetailActivityTargetLayout, historyDetailActivityProductLayout;
    private TextView historyDetailActivityTarget, historyDetailActivityTargetAddress, historyDetailActivityTargetPhone;
    private TextView historyDetailActivityLabel, historyDetailActivityLabelItem;
    private ImageView historyDetailActivityEditIcon;
    private Button historyDetailActivityPrint, historyDetailActivityEdit;
    //not internet connection
    private RelativeLayout noInternetConnectionLayout;
    //not found layout
    private RelativeLayout notFoundLayout;
    private ImageView notFoundIcon;
    private TextView notFoundLabel;
    //progress bar
    private ProgressBar progressBar;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    JSONArray jsonArray;
    //customer or farmer
    private boolean isPickUp = true;
    private String do_id = "0", customer_id = "0", farmer_id = "0", date = "0", time = "0", target_id = "", target = "", ro_id = "0";
    //update delivery or pick up history purpose
    private boolean requestUpdate = false;
    /*
     * print purpose
     * */
    private ArrayList<ProductParentObject> printList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        toolbar = findViewById(R.id.toolbar);
        edit = findViewById(R.id.actionbar_save);
        //not found layout
        notFoundLayout = findViewById(R.id.not_found_layout);
        notFoundIcon = findViewById(R.id.not_found_layout_icon);
        notFoundLabel = findViewById(R.id.not_found_layout_label);
        //no internet connection
        noInternetConnectionLayout = findViewById(R.id.no_connection_layout);
        //progress bar
        progressBar = findViewById(R.id.progress_bar);
        noInternetConnectionLayout = findViewById(R.id.no_connection_layout);

        historyDetailActivityTargetLayout = findViewById(R.id.activity_history_detail_customer_detail_layout);
        historyDetailActivityProductLayout = findViewById(R.id.activity_history_detail_item_detail_layout);

        historyDetailActivityTarget = findViewById(R.id.activity_history_detail_target);
        historyDetailActivityTargetAddress = findViewById(R.id.activity_history_detail_target_address);
        historyDetailActivityTargetPhone = findViewById(R.id.activity_history_detail_target_phone);

        historyDetailActivityLabel = findViewById(R.id.activity_history_detail_label_action);
        historyDetailActivityLabelItem = findViewById(R.id.activity_history_detail_label_item);

        historyDetailActivityEditIcon = findViewById(R.id.activity_history_detail_edit_icon);

        historyDetailActivityPrint = findViewById(R.id.activity_history_detail_reprint);

        historyDetailActivityEdit = findViewById(R.id.activity_history_detail_edit);

        historyDetailActivityListView = findViewById(R.id.activity_history_detail_list_view);
        productObjectArrayList = new ArrayList<>();
        printList = new ArrayList<>();
    }

    private void objectSetting() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            farmer_id = (bundle.getString("farmer_id") != null ? bundle.getString("farmer_id") : "0");
            customer_id = (bundle.getString("customer_id") != null ? bundle.getString("customer_id") : "0");
            date = (bundle.getString("date") != null ? bundle.getString("date") : "0");
            time = (bundle.getString("time") != null ? bundle.getString("time") : "0");
            do_id = (bundle.getString("do_id") != null ? bundle.getString("do_id") : "0");
            ro_id = (bundle.getString("ro_id") != null ? bundle.getString("ro_id") : "0");

            historyDetailActivityPrint.setVisibility(!ro_id.equals("0") ? View.VISIBLE : View.GONE);
            historyDetailActivityEdit.setVisibility(!ro_id.equals("0") ? View.VISIBLE : View.GONE);

            isPickUp = (do_id.equals("0"));
        }

        historyProductExpandableAdapter = new HistoryProductExpandableAdapter(this, productObjectArrayList, this, date);
        historyDetailActivityListView.setAdapter(historyProductExpandableAdapter);
        historyDetailActivityEditIcon.setVisibility(isPickUp ? View.VISIBLE : View.GONE);

        historyDetailActivityTargetLayout.setOnClickListener(this);
        historyDetailActivityPrint.setOnClickListener(this);
        edit.setOnClickListener(this);

        setupActionBar();
        setupNotFoundLayout();
        checkInternetConnection(null);
    }

    /*----------------------------------------------------------------------share layout setting-----------------------------------------------------*/
    private void setupActionBar() {
        edit.setText("Edit");
        edit.setVisibility(!ro_id.equals("") ? View.VISIBLE : View.GONE);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(isPickUp ? "#PO" + ro_id : "#DO" + setPlaceHolder(do_id));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    private String setPlaceHolder(String do_id) {
        StringBuilder do_idBuilder = new StringBuilder(do_id);
        for (int i = do_idBuilder.length(); i < 5; i++) {
            do_idBuilder.insert(0, "0");
        }
        return do_idBuilder.toString();
    }

    private void setupNotFoundLayout() {
        notFoundIcon.setImageDrawable(getResources().getDrawable(R.drawable.no_item_to_deliver_icon));
        notFoundLabel.setText("No item to delivey!");
    }

    public void checkInternetConnection(View view) {
        boolean networkConnection = new NetworkConnection(this).checkNetworkConnection();
        if (networkConnection) fetchParentData();

        noInternetConnectionLayout.setVisibility(networkConnection ? View.GONE : View.VISIBLE);
        historyDetailActivityTargetLayout.setVisibility(networkConnection ? View.VISIBLE : View.GONE);
        historyDetailActivityProductLayout.setVisibility(networkConnection ? View.VISIBLE : View.GONE);
        showProgressBar(networkConnection);
    }

    /*----------------------------------------------------------------------------click event-------------------------------------------------------------*/
    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.activity_history_detail_customer_detail_layout:
                if (isPickUp) {
                    DialogFragment dialogFragment = new FarmerDialog();
                    dialogFragment.show(getSupportFragmentManager(), "");
                }
                break;
            case R.id.activity_history_detail_reprint:
                if (print()) CustomToast(getApplicationContext(), "Printing..");
                view.setEnabled(false);
                //set enable
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.setEnabled(true);
                    }
                }, 500);
                break;
            case R.id.actionbar_save:
                editReceiveOrder(null);
                break;

        }
    }

    /*---------------------------------------------------------------------fetch parent item--------------------------------------------------------------*/
    private void fetchParentData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
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

                        if (jsonObjectLoginResponse != null) {
                            Log.d("jsonObject", "jsonObject: data: " + jsonObjectLoginResponse);
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                setUpTargetInformation(jsonObjectLoginResponse.getJSONArray("value"));
                                setUpDeliveryListValue(jsonObjectLoginResponse);
                            }
                            /*
                             * when data not found
                             * */
                            else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        onBackPressed();
                                    }
                                });
                            }
                        } else {
                            CustomToast(getApplicationContext(), "Network Error!");
                        }

                    } catch (InterruptedException e) {
                        CustomToast(getApplicationContext(), "Interrupted Exception!");
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        CustomToast(getApplicationContext(), "Execution Exception!");
                        e.printStackTrace();
                    } catch (JSONException e) {
                        CustomToast(getApplicationContext(), "JSON Exception!");
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        CustomToast(getApplicationContext(), "Connection Time Out!");
                        e.printStackTrace();
                    }
                }
                setVisibility();
            }
        }).start();
    }

    private ArrayList<ApiDataObject> getApiDataObjectArrayList() {
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject((isPickUp ? "ro_id" : "do_id"), (isPickUp ? ro_id : do_id)));
        apiDataObjectArrayList.add(new ApiDataObject((isPickUp ? "farmer_id" : "customer_id"), (isPickUp ? farmer_id : customer_id)));
        return apiDataObjectArrayList;
    }

    private void setUpDeliveryListValue(JSONObject jsonObject) {
        //close all group
//        closeOtherChildView(-1);
        if (productObjectArrayList.size() > 0) productObjectArrayList.clear();
        try {
            jsonArray = jsonObject.getJSONArray("value").getJSONObject(0).getJSONArray(isPickUp ? "pick_up_history_item" : "delivery_history_item");

            for (int i = 0; i < jsonArray.length(); i++) {
                int position = -1;
                String productName = jsonArray.getJSONObject(i).getString("name");

                for (int j = 0; j < productObjectArrayList.size(); j++) {
                    //if found mean that same child record under the same date
                    if (productObjectArrayList.get(j).getName().equals(productName)) {
                        position = j;
                        //stop looping
                        break;
                    }
                }
                //mean this date is never added yet so create a new group view
                if (position == -1) {
                    productObjectArrayList.add(new ProductParentObject(
                            jsonArray.getJSONObject(i).getString("id"),
                            jsonArray.getJSONObject(i).getString("picture"),
                            jsonArray.getJSONObject(i).getString("name"),
                            jsonArray.getJSONObject(i).getString("total_quantity"),
                            jsonArray.getJSONObject(i).getString("price"),
                            jsonArray.getJSONObject(i).getString("type"),
                            setChildArray(jsonArray.getJSONObject(i))));
                }
                // if the same date(group view) is added then add the child item
                else {
                    /*
                     * check same farmer is existed within one product or not
                     * */
                    if (!isPickUp) {
                        int childPosition = -1;
                        String farmer = jsonArray.getJSONObject(i).getString("farmer");
                        for (int j = 0; j < productObjectArrayList.get(position).getProductChildObjectArrayList().size(); j++) {
                            if (productObjectArrayList.get(position).getProductChildObjectArrayList().get(j).getFarmer().equals(farmer)) {
                                childPosition = j;
                                //stop looping
                                break;
                            }
                        }
                        //create new child
                        if (childPosition == -1)
                            productObjectArrayList.get(position).getProductChildObjectArrayList().add(setChildObject(jsonArray.getJSONObject(i)));
                            //if exist then add weight into the same farmer within one product,
                        else {
                            productObjectArrayList.get(position).getProductChildObjectArrayList().get(childPosition).joinAllWeight(jsonArray.getJSONObject(i).getString("weight"));
                            productObjectArrayList.get(position).getProductChildObjectArrayList().get(childPosition).totalQuantity(jsonArray.getJSONObject(i).getString("total_quantity"));
                        }
                    }
                    /*
                     * pick up
                     * */
                    else {
                        productObjectArrayList.get(position).getProductChildObjectArrayList().add(setChildObject(jsonArray.getJSONObject(i)));
                    }
                    /*
                     * count parent total item (delivery or pick up both using this method)
                     * */
                    productObjectArrayList.get(position).setQuantity(countParentTotalQuantity(position));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
    }

    private void setVisibility() {
        final boolean show = productObjectArrayList.size() > 0;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                historyDetailActivityListView.setVisibility(show ? View.VISIBLE : View.GONE);
                historyDetailActivityTarget.setVisibility(show ? View.VISIBLE : View.GONE);
                historyDetailActivityProductLayout.setVisibility(show ? View.VISIBLE : View.GONE);
                notFoundLayout.setVisibility(show ? View.GONE : View.VISIBLE);
                showProgressBar(false);

                requestUpdate = !show;
                if (requestUpdate) onBackPressed();

                notifyDataSetChanged();
            }
        });
    }

    private String countParentTotalQuantity(int position) {
        int totalQuantity = 0;
        for (int k = 0; k < productObjectArrayList.get(position).getProductChildObjectArrayList().size(); k++) {
            totalQuantity = totalQuantity + Integer.valueOf(productObjectArrayList.get(position).getProductChildObjectArrayList().get(k).getQuantity());
        }
        return String.valueOf(totalQuantity);
    }

    private void notifyDataSetChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                historyProductExpandableAdapter.notifyDataSetChanged();
            }
        });
    }

    /*----------------------------------------------------------------------fetch child item---------------------------------------------------------------*/

    private ArrayList<ProductChildObject> setChildArray(JSONObject jsonObject) {
        ArrayList<ProductChildObject> transactionChildObjectArrayList = new ArrayList<>();
        transactionChildObjectArrayList.add(setChildObject(jsonObject));
        return transactionChildObjectArrayList;
    }

    private ProductChildObject setChildObject(JSONObject jsonObject) {
        ProductChildObject object = new ProductChildObject();
        try {
            object.setId(jsonObject.getString("id"));
            object.setWeight(jsonObject.getString("weight"));
            object.setQuantity(jsonObject.getString("total_quantity"));
            if (isPickUp) object.setGrade(jsonObject.getString("grade"));
            if (!isPickUp) object.setFarmer(jsonObject.getString("farmer"));
            if (!isPickUp) object.setFarmer_id(jsonObject.getString("farmer_id"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    /*------------------------------------------------------------------target information---------------------------------------------------------------*/
    private void setUpTargetInformation(final JSONArray jsonArray) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = jsonArray.getJSONObject(0).getJSONArray(isPickUp ? "farmer_detail" : "customer_detail").getJSONObject(0);
                    target = jsonObject.getString("name");
                    if (isPickUp) target_id = jsonObject.getString("id");

                    historyDetailActivityTarget.setText(target);
                    historyDetailActivityTargetPhone.setText(jsonObject.getString("phone"));
                    historyDetailActivityTargetAddress.setText(jsonObject.getString("address"));

                    historyDetailActivityLabelItem.setText(isPickUp ? "Pick Up Items" : "Deliver Items");
                    historyDetailActivityLabel.setText(isPickUp ? "Farmer Details" : "Customer Details");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /*------------------------------------------------------------------delete and edit stock purpose---------------------------------------------------------*/
    @Override
    public void edit(int groupPosition, int childPosition) {
        if (isPickUp) checkingUpdatePermission(groupPosition, childPosition, true);
        else editDeliveryItem(groupPosition, childPosition);
    }

    /*
     * open grid view for edit delivery item
     * */
    private void editDeliveryItem(int groupPosition, int childPosition) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("parent_object", productObjectArrayList.get(groupPosition));
        bundle.putSerializable("child_object", productObjectArrayList.get(groupPosition).getProductChildObjectArrayList().get(childPosition));
        bundle.putString("target_id", target_id);
        bundle.putString("target", target);
        bundle.putString("do_id", do_id);
        bundle.putString("farmer_id", productObjectArrayList.get(groupPosition).getProductChildObjectArrayList().get(childPosition).getFarmer_id());

        DialogFragment dialogFragment = new HistoryProductListDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getSupportFragmentManager(), "");
    }

    /*
     * open add product dialog for edit pick up item
     * */
    private void editPickUpItem(int groupPosition, int childPosition) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("parent_object", productObjectArrayList.get(groupPosition));
        bundle.putSerializable("child_object", productObjectArrayList.get(groupPosition).getProductChildObjectArrayList().get(childPosition));
        bundle.putString("farmer_id", target_id);
        bundle.putString("date", date);
        bundle.putString("time", time);
        bundle.putString("ro_id", ro_id);

        DialogFragment dialogFragment = new AddProductDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getSupportFragmentManager(), "");
    }

    /*
     * check whether it is enable to edit or not
     * */
    private void checkingUpdatePermission(final int groupPosition, final int childPosition, final boolean isEdit) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("pick_up_driver_id", SharedPreferenceManager.getUserId(getApplicationContext())));
                apiDataObjectArrayList.add(new ApiDataObject("product_id", productObjectArrayList.get(groupPosition).getId()));
                apiDataObjectArrayList.add(new ApiDataObject("farmer_id", farmer_id));
                apiDataObjectArrayList.add(new ApiDataObject("date", date));
                apiDataObjectArrayList.add(new ApiDataObject("weight", productObjectArrayList.get(groupPosition).getProductChildObjectArrayList().get(childPosition).getWeight()));
                apiDataObjectArrayList.add(new ApiDataObject("status", "0"));

                asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
                        new ApiManager().pick_up_history,
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
                            Log.d("jsonObject", "jsonObject: 1" + jsonObjectLoginResponse);
                            if (!jsonObjectLoginResponse.getString("status").equals("1")) {
                                if (isEdit) editPickUpItem(groupPosition, childPosition);
                            } else CustomDialog(HistoryDetailActivity.this, "Item Is Being Process",
                                    "You are not allowed to perform this action right now. If any changes please inform with admin right now.",
                                    "I Got It");
                        } else {
                            CustomToast(getApplicationContext(), "Network Error!");
                        }

                    } catch (InterruptedException e) {
                        CustomToast(getApplicationContext(), "Interrupted Exception!");
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        CustomToast(getApplicationContext(), "Execution Exception!");
                        e.printStackTrace();
                    } catch (JSONException e) {
                        CustomToast(getApplicationContext(), "JSON Exception!");
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        CustomToast(getApplicationContext(), "Connection Time Out!");
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /*------------------------------------------------------------------------update farmer purpose (for pick up only)--------------------------------------------------------*/
    @Override
    public void selectedItem(final String name, final String id, final String address, final String phone) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("Are you sure that you want to change the farmer?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        updateFarmer(name, id, address, phone);
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

    private void updateFarmer(final String name, final String new_farmer_id, final String address, final String phone) {
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("new_farmer_id", new_farmer_id));
        apiDataObjectArrayList.add(new ApiDataObject("ro_id", ro_id));
        new Thread(new Runnable() {
            @Override
            public void run() {
                asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
                        new ApiManager().pick_up_history,
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
                            Log.d("jsonObject", "jsonObject: 1" + jsonObjectLoginResponse);
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        requestUpdate = true;
                                        farmer_id = target_id = new_farmer_id;
                                        historyDetailActivityTarget.setText(name);
                                        historyDetailActivityTargetAddress.setText(address);
                                        historyDetailActivityTargetPhone.setText(phone);
                                        showSnackBar("Update Successfully!");
                                    }
                                });
                            }
                        } else {
                            CustomToast(getApplicationContext(), "Network Error!");
                        }

                    } catch (InterruptedException e) {
                        CustomToast(getApplicationContext(), "Interrupted Exception!");
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        CustomToast(getApplicationContext(), "Execution Exception!");
                        e.printStackTrace();
                    } catch (JSONException e) {
                        CustomToast(getApplicationContext(), "JSON Exception!");
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        CustomToast(getApplicationContext(), "Connection Time Out!");
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /*----------------------------------------------------------print purpose------------------------------------------------------------*/
    private boolean print() {
        PrintfManager printfManager = PrintfManager.getInstance(this);
        if (printfManager.isConnect()) {
            printfManager.startPrint(
                    historyDetailActivityTarget.getText().toString(),
                    historyDetailActivityTargetAddress.getText().toString(),
                    ro_id,
                    setupPrintingData());
            return true;
        } else {
            PrintfBlueListActivity.startActivity(this);
            return false;
        }
    }

    public ArrayList<ProductParentObject> setupPrintingData() {
        for (int i = 0; i < productObjectArrayList.size(); i++) {
            StringBuilder weight = new StringBuilder();
            int quantity = Integer.valueOf(productObjectArrayList.get(i).getQuantity());
            /*
             * weight
             * */
            for (int k = 0; k < productObjectArrayList.get(i).getProductChildObjectArrayList().size(); k++) {
                for (int j = 0; j < Integer.valueOf(productObjectArrayList.get(i).getProductChildObjectArrayList().get(k).getQuantity()); j++)
                    weight.append(",").append(productObjectArrayList.get(i).getProductChildObjectArrayList().get(k).getWeight()).append("KG");
            }

            printList.add(new ProductParentObject(
                    productObjectArrayList.get(i).getName(),
                    String.valueOf(quantity),
                    weight.toString().substring(1)));
        }

        return printList;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == PROCEED_TO_PRINT) print();
        else if (resultCode == UPDATE_PICK_UP_HISTORY) {
            fetchParentData();
        }
    }

    /*--------------------------------------------------------------------delete or edit receive order---------------------------------------------------------------*/
    public void editReceiveOrder(View view) {
        if (new NetworkConnection(this).checkNetworkConnection()) {
            Bundle bundle = new Bundle();
            bundle.putString("ro_id", ro_id);
            startActivityForResult(new Intent(this, PickUpActivity.class).putExtras(bundle), UPDATE_PICK_UP_HISTORY);
        } else showSnackBar("No internet connection!");
    }

    public void deleteReceiveOrderConfirmation(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("Are you sure that you want to this purchase order? \nOnce delete can't be undone!");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, int i) {
                        showProgressBar(true);
                        deleteReceiveOrder();
                        dialog.cancel();
                    }
                });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void deleteReceiveOrder() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("ro_id", ro_id));
                apiDataObjectArrayList.add(new ApiDataObject("delete_whole_ro", "1"));

                asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
                        new ApiManager().vege_manage,
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
                            Log.d("jsonObject", "jsonObject: delete: " + jsonObjectLoginResponse);
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        onBackPressed();
                                    }
                                });
                            }
                        } else {
                            CustomToast(getApplicationContext(), "Network Error!");
                        }

                    } catch (InterruptedException e) {
                        CustomToast(getApplicationContext(), "Interrupted Exception!");
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        CustomToast(getApplicationContext(), "Execution Exception!");
                        e.printStackTrace();
                    } catch (JSONException e) {
                        CustomToast(getApplicationContext(), "JSON Exception!");
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        CustomToast(getApplicationContext(), "Connection Time Out!");
                        e.printStackTrace();
                    }
                }
                showProgressBar(false);
            }
        }).start();
    }

    /*-------------------------------------------------------------------other ----------------------------------------------------------------------------------*/
    @Override
    public void onBackPressed() {
        setResult(UPDATE_LIST);
        super.onBackPressed();
    }

    public void showSnackBar(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
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

    private void showProgressBar(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    public void reset() {
        productObjectArrayList.clear();
        notifyDataSetChanged();
        fetchParentData();
    }

    @Override
    public void fetchSelectedProductApi() {

    }

    @Override
    public void fetchChildItem() {

    }

    @Override
    public void setIsChanged() {

    }

}