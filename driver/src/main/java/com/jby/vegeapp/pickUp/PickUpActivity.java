package com.jby.vegeapp.pickUp;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.jby.vegeapp.R;
import com.jby.vegeapp.adapter.pick_up.AddedProductExpandableAdapter;
import com.jby.vegeapp.adapter.pick_up.ProductAdapter;
import com.jby.vegeapp.database.CustomSqliteHelper;
import com.jby.vegeapp.database.FrameworkClass;
import com.jby.vegeapp.database.ResultCallBack;
import com.jby.vegeapp.network.BasketNetworkMonitor;
import com.jby.vegeapp.network.PickUpNetworkMonitor;
import com.jby.vegeapp.object.product.ProductChildObject;
import com.jby.vegeapp.object.product.ProductParentObject;
import com.jby.vegeapp.object.product.ProductObject;
import com.jby.vegeapp.others.NetworkConnection;
import com.jby.vegeapp.others.NonScrollExpandableListView;
import com.jby.vegeapp.others.recycleview.GridSpacingItemDecoration;
import com.jby.vegeapp.pickUp.farmer.FarmerDialog;
import com.jby.vegeapp.pickUp.product.AddProductDialog;
import com.jby.vegeapp.printer.Manager.PrintfManager;
import com.jby.vegeapp.printer.PrintfBlueListActivity;
import com.jby.vegeapp.shareObject.AnimationUtility;
import com.jby.vegeapp.shareObject.ApiDataObject;
import com.jby.vegeapp.shareObject.ApiManager;
import com.jby.vegeapp.shareObject.AsyncTaskManager;
import com.jby.vegeapp.shareObject.CustomScheduleJob;
import com.jby.vegeapp.sharePreference.SharedPreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;
import static android.support.design.widget.BottomSheetBehavior.STATE_EXPANDED;
import static com.jby.vegeapp.Utils.VariableUtils.DELETE_CONFIRMATION;
import static com.jby.vegeapp.Utils.VariableUtils.DELETE_SELECTED_ITEM;
import static com.jby.vegeapp.Utils.VariableUtils.GET_CHILD_DATA;
import static com.jby.vegeapp.Utils.VariableUtils.GET_PARENT_DATA;
import static com.jby.vegeapp.Utils.VariableUtils.GET_PRODUCT_LIST;
import static com.jby.vegeapp.Utils.VariableUtils.GET_RECEIVE_ORDER_ID;
import static com.jby.vegeapp.Utils.VariableUtils.PRINT_CONFIRMATION;
import static com.jby.vegeapp.Utils.VariableUtils.PRINT_REQUEST;
import static com.jby.vegeapp.Utils.VariableUtils.PROCEED_TO_PRINT;
import static com.jby.vegeapp.Utils.VariableUtils.UPDATE_PICK_UP_HISTORY;
import static com.jby.vegeapp.Utils.VariableUtils.UPDATE_RECEIVE_ORDER_ID;
import static com.jby.vegeapp.Utils.VariableUtils.UPLOAD_STOCK;
import static com.jby.vegeapp.database.CustomSqliteHelper.TB_BASKET;
import static com.jby.vegeapp.database.CustomSqliteHelper.TB_PICK_UP_FAVOURITE_FARMER;
import static com.jby.vegeapp.database.CustomSqliteHelper.TB_PRODUCT;
import static com.jby.vegeapp.database.CustomSqliteHelper.TB_PRODUCT_PRIORITY;
import static com.jby.vegeapp.database.CustomSqliteHelper.TB_RECEIVED_ORDER;
import static com.jby.vegeapp.database.CustomSqliteHelper.TB_STOCK;
import static com.jby.vegeapp.shareObject.CustomToast.CustomToast;
import static com.jby.vegeapp.Utils.VariableUtils.REFRESH_AVAILABLE_QUANTITY;

public class PickUpActivity extends AppCompatActivity implements View.OnClickListener, FarmerDialog.FarmerDialogCallBack,
        ResultCallBack, OffLineModeDialog.OffLineModeDialogCallBack, TextWatcher,
        ProductAdapter.ProductAdapterCallBack, AddProductDialog.AddProductDialogCallBack,
        ExpandableListView.OnGroupClickListener, AddedProductExpandableAdapter.ProductExpandableAdapterCallBack,
        ExpandableListView.OnChildClickListener {
    //actionbar
    private TextView actionbarSave;
    private Toolbar toolbar;

    private LinearLayout pickUpActivityFarmerLayout;
    private TextView pickUpActivityFarmer;
    //select product button
    private TextView pickUpActivityLabelSelectItem;
    private LinearLayout pickUpActivitySelectItemLayout;
    private ImageView pickUpActivityArrowIcon;
    private DialogFragment dialogFragment;
    private FragmentManager fm;
    //database
    private FrameworkClass tbStockSql, tbProductSql, tbReceiveOrderSql;
    private int requestCode = 0;
    //product list;
    private RecyclerView productDialogProductList;
    private ArrayList<ProductObject> productObjectArrayList;
    private ProductAdapter productAdapter;
    private BottomSheetBehavior pickUpActivityProductListLayout;
    private EditText pickUpActivityProductSearch;
    private ProgressBar progressBar;
    private String query = "";
    //added product list
    private LinearLayout pickUpActivityAddItemLayout;
    private TextView pickUpActivityLabelSelectedItem;
    private NonScrollExpandableListView addedProductList;
    private ArrayList<ProductParentObject> addedProductArrayList;
    private AddedProductExpandableAdapter addedProductAdapter;
    private int position;
    private boolean preventDoubleClick = true;
    //basket
    private LinearLayout pickUpActivityBasketLayout;
    private Spinner pickUpActivityBasketStatus;
    private EditText pickUpActivityBasketQuantity;
    private ImageView pickUpActivityPlusBasket;
    //not found layout
    private RelativeLayout notFoundLayout;
    private ImageView notFoundIcon;
    private TextView notFoundLabel;

    private Handler handler;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    /*
     * print purpose
     * */
    private ArrayList<ProductParentObject> printList;
    /*
     * generating received order purpose
     * */
    private String ro_ID = "";
    private boolean isOffLine = false;
    /*
     * date purpose
     * */
    private LinearLayout pickUpActivityDateParentLayout, pickUpActivityDateLayout;
    private TextView pickUpActivityDate;
    private String date;
    private String lastDate = "";
    /*
     * farmer
     * */
    private String farmerId, farmerName, farmerAddress;
    private String lastFarmer, lastFarmerId;
    /*
     * update purpose
     * */
    private boolean isUpdate = false;
    private boolean isChanged = false;
    /*
     * progress dialog
     * */
    private ProgressBar progressDialogProgress;
    private LinearLayout progressDialogProgressLayout;
    private TextView progressDialogDownloadMaxNumber, progressDialogCurrentNumber, progressDialogLabel;

    private String accessSession = String.valueOf(android.text.format.DateFormat.format("yyyyMMddHHmmss", new java.util.Date()));
    private Bundle bundle;
    //status
    // status 0 : haven't upload yet
    // status 1 : uploaded successfully
    // status 2 : uploaded but no network connection

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_up);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        //action bar
        toolbar = findViewById(R.id.toolbar);
        actionbarSave = findViewById(R.id.actionbar_save);
        pickUpActivityFarmerLayout = findViewById(R.id.activity_pick_up_farmer_layout);
        pickUpActivityFarmer = findViewById(R.id.activity_pick_up_farmer);
        //product list
        productDialogProductList = findViewById(R.id.activity_pick_up_product_list);
        pickUpActivityProductListLayout = BottomSheetBehavior.from(findViewById(R.id.activity_pick_up_product_list_layout));

        progressBar = findViewById(R.id.progress_bar);

        pickUpActivityProductSearch = findViewById(R.id.activity_pick_up_product_search);
        //add item
        pickUpActivityAddItemLayout = findViewById(R.id.activity_pick_up_add_item_layout);
        pickUpActivityLabelSelectItem = findViewById(R.id.activity_pick_up_label_select_item);
        pickUpActivitySelectItemLayout = findViewById(R.id.activity_pick_up_select_item_layout);
        pickUpActivityArrowIcon = findViewById(R.id.activity_pick_up_arrow);
        pickUpActivityLabelSelectedItem = findViewById(R.id.activity_pick_up_label_selected_item);
        //basket
        pickUpActivityBasketLayout = findViewById(R.id.activity_pick_up_basket_layout);
        pickUpActivityBasketQuantity = findViewById(R.id.activity_pick_up_basket_quantity);
        pickUpActivityPlusBasket = findViewById(R.id.activity_pick_up_plus_basket_button);
        pickUpActivityBasketStatus = findViewById(R.id.activity_pick_up_basket_status);
        //not found layout
        notFoundLayout = findViewById(R.id.not_found_layout);
        notFoundIcon = findViewById(R.id.not_found_layout_icon);
        notFoundLabel = findViewById(R.id.not_found_layout_label);
        //date
        pickUpActivityDateLayout = findViewById(R.id.activity_pick_up_date_layout);
        pickUpActivityDateParentLayout = findViewById(R.id.activity_pick_up_date_parent_layout);
        pickUpActivityDate = findViewById(R.id.activity_pick_up_date);
        //progress dialog
        progressDialogProgressLayout = findViewById(R.id.progress_dialog_layout);
        progressDialogProgress = findViewById(R.id.progress_dialog_progress_bar);
        progressDialogDownloadMaxNumber = findViewById(R.id.progress_dialog__max_num);
        progressDialogCurrentNumber = findViewById(R.id.progress_dialog_current_num);
        progressDialogLabel = findViewById(R.id.progress_dialog_label);

        addedProductList = findViewById(R.id.activity_pick_up_add_product_list);
        addedProductArrayList = new ArrayList<>();
        addedProductAdapter = new AddedProductExpandableAdapter(this, addedProductArrayList, this);

        productObjectArrayList = new ArrayList<>();
        productAdapter = new ProductAdapter(this, productObjectArrayList, this);

        printList = new ArrayList<>();

        tbStockSql = new FrameworkClass(this, this, new CustomSqliteHelper(this), TB_STOCK);
        tbProductSql = new FrameworkClass(this, this, new CustomSqliteHelper(this), TB_PRODUCT);
        tbReceiveOrderSql = new FrameworkClass(this, new CustomSqliteHelper(this), TB_RECEIVED_ORDER);
        fm = getSupportFragmentManager();
        handler = new Handler();
    }

    private void objectSetting() {
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 3);
        productDialogProductList.setLayoutManager(mLayoutManager);
        productDialogProductList.addItemDecoration(new GridSpacingItemDecoration(3, dpToPx(2), true));
        productDialogProductList.setItemAnimator(new DefaultItemAnimator());
        productDialogProductList.setAdapter(productAdapter);

        actionbarSave.setOnClickListener(this);
        actionbarSave.setVisibility(View.VISIBLE);

        pickUpActivityFarmerLayout.setOnClickListener(this);
        pickUpActivitySelectItemLayout.setOnClickListener(this);
        //product
        pickUpActivityProductSearch.addTextChangedListener(this);
        //basket
        pickUpActivityPlusBasket.setOnClickListener(this);
        //added product list
        addedProductList.setAdapter(addedProductAdapter);
        addedProductList.setOnChildClickListener(this);
        addedProductList.setOnGroupClickListener(this);
        //date
        pickUpActivityDateLayout.setOnClickListener(this);

        setupNotFoundLayout();
        setVisibility();
        setupSpinner();
        setUpBottomSheet();
        
        /*
        open from pick up history for update purpose
        * */
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            showProgressBar(true);
            isUpdate = true;
            ro_ID = bundle.getString("ro_id");
            getReceiveOrderDetail();
            setUpView();
        } else {
            setDefaultFarmer();
        }
        /*
         * set action bar
         * */
        setupActionBar();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.actionbar_save:
                checkingBeforeSave();
                break;
            case R.id.activity_pick_up_farmer_layout:
                openFarmDialog();
                break;
            case R.id.activity_pick_up_select_item_layout:
                showProductLayout(pickUpActivityProductListLayout.getState() == STATE_EXPANDED ? STATE_COLLAPSED : STATE_EXPANDED);
                break;
            case R.id.activity_pick_up_plus_basket_button:
                setQuantity(true);
                break;
            case R.id.activity_pick_up_date_layout:
                openDatePicker();
                break;
        }
    }

    private void setupActionBar() {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        getSupportActionBar().setTitle(ro_ID.equals("") ? "Pick Up" : "#PO" + ro_ID);
        actionbarSave.setText(ro_ID.equals("") ? "Save" : "Update");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void setupNotFoundLayout() {
        notFoundIcon.setImageDrawable(getResources().getDrawable(R.drawable.no_item_to_deliver_icon));
        notFoundLabel.setText("No Item is found from local!");
    }

    private void getLatestReceivedId() {
        requestCode = GET_RECEIVE_ORDER_ID;
        //get latest ro id
        new FrameworkClass(this, this, new CustomSqliteHelper(this), TB_RECEIVED_ORDER)
                .new Read("id")
                .orderByDesc("id")
                .limitBy("1")
                .perform();
    }

//    -------------------------------------------------------------------farmer---------------------------------------------------------------

    private void openFarmDialog() {
        dialogFragment = new FarmerDialog();
        dialogFragment.show(fm, "");
    }

    @Override
    public void selectedItem(String name, String farmerId, String address, String phone) {
        this.farmerId = lastFarmerId = farmerId;
        this.farmerName = lastFarmer = name;
        this.farmerAddress = address;

        pickUpActivityFarmer.setText(name);
        /*
         * update RO's farmer
         * */
        if (!ro_ID.equals("")) {
            changeFarmerConfirmationDialog();
        }
        /*
         * simply choose the farmer
         * */
        else {
            new FrameworkClass(this, new CustomSqliteHelper(this), TB_PICK_UP_FAVOURITE_FARMER)
                    .new create("farmer_id, name", farmerId + "," + name)
                    .perform();

            SharedPreferenceManager.setPickUpDefaultFarmer(this, name + "%" + farmerId + "%" + address);
            setUpView();
        }
    }

    private void setDefaultFarmer() {
        String farmerDetail = SharedPreferenceManager.getPickUpDefaultFarmer(this);
        try {
            if (!farmerDetail.equals("default")) {
                String[] farmerDetails = farmerDetail.split("%");
                farmerName = farmerDetails[0];
                farmerId = farmerDetails[1];
                farmerAddress = farmerDetails[2];
                pickUpActivityFarmer.setText(farmerName);
                setUpView();
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    private void setUpView() {
        if (!isUpdate) new AnimationUtility().fadeInVisible(this, pickUpActivityBasketLayout);
        new AnimationUtility().fadeInVisible(this, pickUpActivityAddItemLayout);

        if (pickUpActivityBasketQuantity.getText().toString().trim().equals(""))
            pickUpActivityBasketQuantity.append("0");
        pickUpActivitySelectItemLayout.setVisibility(View.VISIBLE);
        //show date layout only the driver is high priority
        setDefaultDate();
    }

    public void changeFarmerConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("Are you sure that you want to change the farmer of #RO" + ro_ID + "?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "I'm Sure",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        showProgressBar(true);
                        farmerName = lastFarmer;
                        farmerId = lastFarmerId;
                        /*
                         * update local database farmer
                         * */
                        updateLocalFarmer();
                        /*
                         * update cloud farmer
                         * */
                        updateReceiveOrderFarmer();
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

    /*
     * update (created_at) in table stock when changed the date from date picker
     * */
    private void updateLocalFarmer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                new FrameworkClass(getApplicationContext(), new CustomSqliteHelper(getApplicationContext()), TB_STOCK)
                        .new Update("farmer_id", farmerId)
                        .where("session = ? AND status = ?", accessSession + ", 0")
                        .perform();
            }
        }).start();
    }

    private void updateReceiveOrderFarmer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("ro_id", ro_ID));
                apiDataObjectArrayList.add(new ApiDataObject("farmer_id", farmerId));
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
                            Log.d("jsonObject", "jsonObject: " + jsonObjectLoginResponse);
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showSnackBar("Update Successfully");
                                        pickUpActivityFarmer.setText(farmerName);
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

    //    -----------------------------------------------------------------basket----------------------------------------------------------------
    private void setQuantity(boolean plus) {
        if (pickUpActivityBasketQuantity.getText().toString().trim().equals(""))
            pickUpActivityBasketQuantity.setText("0");

        int quantity = Integer.valueOf(pickUpActivityBasketQuantity.getText().toString().trim());
        if (plus) quantity++;
        else {
            if (quantity > 0) quantity--;
        }
        pickUpActivityBasketQuantity.setText("");
        pickUpActivityBasketQuantity.append(String.valueOf(quantity));
    }

    private String basketStatus() {
        return basketStatus(pickUpActivityBasketStatus.getSelectedItem().toString());
    }

    private String basketStatus(String checkPosition) {
        String status = "";
        switch (checkPosition) {
            case "Send":
                status = "3";
                break;
            case "Borrow":
                status = "4";
                break;
        }
        return status;
    }

    private void setupSpinner() {
        List<String> categories = new ArrayList<>();
        categories.add("Send");
        categories.add("Borrow");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.spinner_layout, categories);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        pickUpActivityBasketStatus.setAdapter(dataAdapter);
    }

    //    -----------------------------------------------------------------product list---------------------------------------------------------
    private void setUpBottomSheet() {
        pickUpActivityProductListLayout.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                showProductLayout(newState);
                if (newState == STATE_EXPANDED) showProductList();
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    //layout setting
    private void showProductList() {
        showProgressBar(true);
        if (productObjectArrayList.size() <= 0) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (new NetworkConnection(getApplicationContext()).checkNetworkConnection())
                        fetchAllProduct();
                    else fetchDataFromLocal(query);
                }
            }, 300);
        } else {
            showProgressBar(false);
        }
    }

    private void showProductLayout(int state) {
        pickUpActivityLabelSelectItem.setText(state == STATE_COLLAPSED ? "Swipe up to select item" : "Swipe up to hide");
        pickUpActivityArrowIcon.setImageDrawable(state == STATE_COLLAPSED ? getDrawable(R.drawable.arrow_up) : getDrawable(R.drawable.activity_pick_up_arrow_down));
        pickUpActivityProductListLayout.setState(state);
    }

    private void fetchAllProduct() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("fetch", "1"));

                asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
                        new ApiManager().product,
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
                                JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("product");
                                //date
                                String created_at = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));
                                //delete all product before store new product
                                tbProductSql.new Delete().perform();
                                //store product into product table
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    tbProductSql.new create("id, product_code, name, picture, type, price, created_at", new String[]{
                                            jsonArray.getJSONObject(i).getString("id"),
                                            jsonArray.getJSONObject(i).getString("product_code"),
                                            jsonArray.getJSONObject(i).getString("name"),
                                            jsonArray.getJSONObject(i).getString("picture"),
                                            jsonArray.getJSONObject(i).getString("type"),
                                            jsonArray.getJSONObject(i).getString("price"),
                                            created_at
                                    }).perform();
                                }
                                showNotFoundLayout(false);
                                //read product list after stored
                                fetchDataFromLocal(query);
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
        });
    }

    private void fetchDataFromLocal(String query) {
        requestCode = GET_PRODUCT_LIST;
        //read product list after stored
        tbProductSql.new Read("*")
                .leftJoinTable(TB_PRODUCT_PRIORITY)
                .leftJoinTableCondition("tb_product.id = tb_product_priority.product_id")
                .where("tb_product.name LIKE '%" + query + "%' OR tb_product.product_code LIKE '%" + query + "%'")
                .orderByDesc("tb_product_priority.priority")
                .perform();
    }

    private void setProductList(String result) {
        try {
            JSONArray jsonArray = new JSONObject(result).getJSONArray("result");
            ProductObject productObject;
            for (int i = 0; i < jsonArray.length(); i++) {
                productObject = new ProductObject();

                productObject.setId(jsonArray.getJSONObject(i).getString("id"));
                productObject.setName(jsonArray.getJSONObject(i).getString("name"));
                productObject.setPicture(jsonArray.getJSONObject(i).getString("picture"));
                productObject.setType(jsonArray.getJSONObject(i).getString("type"));
                productObject.setPrice(jsonArray.getJSONObject(i).getString("price"));
                productObject.setProduct_code(jsonArray.getJSONObject(i).getString("product_code"));

                productObjectArrayList.add(productObject);
            }
            notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();

            showNotFoundLayout(true);
            showProgressBar(false);
            notifyDataSetChanged();
        }
    }

    private void notifyDataSetChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                productAdapter.notifyDataSetChanged();
                pickUpActivityProductListLayout.setState(STATE_EXPANDED);
                showProgressBar(false);
            }
        });
    }

    //     Converting dp to pixel
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    /*----------------------------------------------------------------------product list search---------------------------------------------------------------*/

    private void searchFromArrayList(final String query) {
        productObjectArrayList.clear();
        fetchDataFromLocal(query);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(final Editable editable) {
        showNotFoundLayout(false);
        showProgressBar(true);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                searchFromArrayList(editable.toString());
            }
        }, 200);
    }

    @Override
    public void openAddProductDialog(String productID, String product, String price, String picture, String type, String quantity, String weight, String grade) {
        dialogFragment = new AddProductDialog();
        Bundle bundle = new Bundle();

        bundle.putString("product_id", productID);
        bundle.putString("product", product);
        bundle.putString("ro_id", ro_ID);
        bundle.putString("farmer_id", farmerId);
        bundle.putString("price", price);
        bundle.putString("picture", picture);
        bundle.putString("type", type);
        bundle.putString("session", accessSession);

        bundle.putString("quantity", quantity);
        bundle.putString("weight", weight);
        bundle.putString("grade", grade);
        bundle.putString("date", getCurrentDateAndTime());

        dialogFragment.setArguments(bundle);
        if (preventDoubleClick) {
            dialogFragment.show(fm, "");
            preventDoubleClick = false;
        }
        //reset the prevent double to to true
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                preventDoubleClick = true;
            }
        }, 200);
    }

    //    ------------------------------------------------------------------added product list call back--------------------------------------
    //------------------------------------------------------group item----------------------------------------------------------------------------
    public void fetchSelectedProductApi() {
        requestCode = GET_PARENT_DATA;
        tbStockSql.new Read("product_id, name, picture, SUM(quantity), price, type")
                .where("session = " + accessSession + " GROUP BY product_id ")
                .orderByDesc("id")
                .perform();
    }

    private void getAllParentAddedProduct(final String json) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //close all group
                closeOtherChildView(-1);

                if (addedProductArrayList.size() > 0) addedProductArrayList.clear();
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    JSONArray jsonArray = jsonObject.getJSONArray("result");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        addedProductArrayList.add(new ProductParentObject(
                                jsonArray.getJSONObject(i).getString("product_id"),
                                jsonArray.getJSONObject(i).getString("picture"),
                                jsonArray.getJSONObject(i).getString("name"),
                                jsonArray.getJSONObject(i).getString("SUM(quantity)"),
                                jsonArray.getJSONObject(i).getString("price"),
                                jsonArray.getJSONObject(i).getString("type")));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                addedProductAdapter.notifyDataSetChanged();
                setVisibility();
            }
        });
    }

    @Override
    public boolean onGroupClick(ExpandableListView expandableListView, View view, final int i, long l) {
        //set position
        this.position = i;

        //close the group if opened
        if (expandableListView.isGroupExpanded(i)) {
            expandableListView.collapseGroup(i);
        } else {
            //close other view
            closeOtherChildView(i);
            addedProductArrayList.get(i).getProductChildObjectArrayList().clear();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fetchChildItem();

                }
            }, 200);
        }
        return true;
    }

    private void closeOtherChildView(int position) {
        for (int i = 0; i < productObjectArrayList.size(); i++) {
            if (i != position) addedProductList.collapseGroup(i);
        }
    }

    /*--------------------------------------------------------child item------------------------------------------------------------------------------*/

    public void fetchChildItem() {
        requestCode = GET_CHILD_DATA;
        //clear
        addedProductArrayList.get(position).getProductChildObjectArrayList().clear();

        tbStockSql.new Read("id, weight, COUNT(quantity), grade")
                .where("session = " + accessSession + " AND product_id =" + addedProductArrayList.get(position).getId() + " GROUP BY weight, grade ")
                .orderByDesc("id")
                .perform();
    }

    @Override
    public void reset() {

    }

    private void setChildValue(String result) {
        try {
            JSONArray jsonArray = new JSONObject(result).getJSONArray("result");
            for (int i = 0; i < jsonArray.length(); i++) {
                addedProductArrayList.get(position).setAddedProductChildObjectArrayList(setChildObject(jsonArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        addedProductList.expandGroup(position);
        addedProductList.setSelectedGroup(position);
        addedProductAdapter.notifyDataSetChanged();
    }

    private ProductChildObject setChildObject(JSONObject jsonObject) {
        ProductChildObject object = null;
        try {
            object = new ProductChildObject(
                    jsonObject.getString("id"),
                    jsonObject.getString("weight"),
                    jsonObject.getString("COUNT(quantity)"),
                    jsonObject.getString("grade"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    public void setVisibility() {
        if (addedProductArrayList.size() > 0) {
            pickUpActivityLabelSelectedItem.setVisibility(View.VISIBLE);
        } else {
            pickUpActivityLabelSelectedItem.setVisibility(View.GONE);
            //product layout
            showProductLayout(STATE_COLLAPSED);
        }
    }

    public void delete(int childPosition) {
        String weight = addedProductArrayList.get(position).getProductChildObjectArrayList().get(childPosition).getWeight();
        String grade = addedProductArrayList.get(position).getProductChildObjectArrayList().get(childPosition).getGrade();
        String productID = addedProductArrayList.get(position).getId();

        requestCode = DELETE_SELECTED_ITEM;
        tbStockSql.new Delete()
                .where("weight = ? AND session = ? AND product_id = ? AND grade = ?",
                        weight + "," + accessSession + "," + productID + "," + grade)
                .perform();
    }

    private void deleteChecking() {
        //number child < 1
        if (addedProductArrayList.get(position).getProductChildObjectArrayList().size() > 1) {
            fetchChildItem();
        } else {
            //if this position of parent contain only one child then redraw the list when delete to avoid error
            addedProductArrayList.clear();
            fetchSelectedProductApi();
            closeOtherChildView(-1);
        }
    }

    public void confirmationDialog(final String title, final String content, final int position, final int action) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(PickUpActivity.this);
                builder.setTitle(title);
                builder.setMessage(content);
                builder.setCancelable(true);

                builder.setPositiveButton(
                        "Confirm",
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, int i) {
                                if (action == DELETE_CONFIRMATION) {
                                    delete(position);
                                    dialog.cancel();
                                } else {
                                    if (print()) {
                                        dialog.cancel();
                                    }
                                }
                            }
                        });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        if(action == PRINT_CONFIRMATION && isUpdate) onBackPressed();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    //update purpose
    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
        String productID = addedProductArrayList.get(i).getId();
        String product = addedProductArrayList.get(i).getName();
        String price = addedProductArrayList.get(i).getPrice();
        String picture = addedProductArrayList.get(i).getPicture();
        String type = addedProductArrayList.get(i).getType();
        String weight = addedProductArrayList.get(i).getProductChildObjectArrayList().get(i1).getWeight();
        String grade = addedProductArrayList.get(i).getProductChildObjectArrayList().get(i1).getGrade();
        String quantity = addedProductArrayList.get(i).getProductChildObjectArrayList().get(i1).getQuantity();

        openAddProductDialog(productID, product, price, picture, type, quantity, weight, grade);
        return true;
    }

    /*---------------------------------------------------------------checking before upload to clouse-------------------------------------------*/
    private void checkingBeforeSave() {
        if (!pickUpActivityBasketQuantity.getText().toString().equals("0") && !pickUpActivityBasketQuantity.getText().toString().equals("") || addedProductArrayList.size() > 0)
            save();
        else {
            /*
             * delete receive order
             * */
            if (isUpdate) deleteConfirmationDialog();
            else showSnackBar("Nothing to upload!");
        }
    }

    private void save() {
        CustomToast(getApplicationContext(), "Uploading...");
        showProgressBar(true);
        actionbarSave.setEnabled(false);
        //clear and get printing data
        printList.clear();
        //print
        getDataForPrinting();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                /*
                 * online
                 * */
                if (new NetworkConnection(PickUpActivity.this).checkNetworkConnection()) {
                    isOffLine = false;
                    requestCode = UPLOAD_STOCK;
                    //if basket quantity != 0
                    if (!pickUpActivityBasketQuantity.getText().toString().equals("") && !pickUpActivityBasketQuantity.getText().toString().equals("0")) {
                        basketControl();
                    } else {
                        if (addedProductArrayList.size() > 0) {
                            performUpload();
                        }
                    }
                }
                /*
                 * offline
                 * */
                else {
                    isOffLine = true;
                    //basket
                    if (!pickUpActivityBasketQuantity.getText().toString().equals("") && !pickUpActivityBasketQuantity.getText().toString().equals("0")) {
                        storeToLocal();
                        CustomScheduleJob.scheduleJob(getApplicationContext(), new ComponentName(getApplicationContext(), BasketNetworkMonitor.class), 2);
                    }
                    //product
                    if (addedProductArrayList.size() > 0) {
                        /*
                         * proceed to upload without creating the ro_id because this is update
                         * */
                        if (!isUpdate) getLatestReceivedId();
                            /*
                             * update required network
                             * */
                        else showSnackBar("Network is required for doing update!");

                    }
                    clear();
                    //view setting
                    showProgressBar(false);
                    actionbarSave.setEnabled(true);
                }
            }
        }, 500);
    }

    /*----------------------------------------------------------------upload stock to server------------------------------------------------------*/
    //step 1
    private void performUpload() {
        /*
         * proceed to upload without creating the ro_id because this is update
         * */
        if (isUpdate) clearRoItemBeforeUpload();
            /*
             * create receive order id then only proceed to upload
             * */
        else getLatestReceivedId();
    }

    //step 2
    private void gettingUploadData() {
        requestCode = UPLOAD_STOCK;
        tbStockSql.new Read("*").where("session = " + accessSession).perform();
    }

    //step 3
    private void settingUploadData(final String result) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONArray jsonArray = new JSONObject(result).getJSONArray("result");
                    /*
                     * show progress dialog
                     * */
                    showProgressDialog(true, "Uploading data...", jsonArray.length());
                    /*
                     * start upload
                     * */
                    for (int i = 0; i < jsonArray.length(); i++) {
                        storeToCloud(jsonArray.getJSONObject(i), i == jsonArray.length() - 1, i);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                showProgressBar(false);
            }
        }).start();
    }

    //step 4
    private void storeToCloud(final JSONObject jsonObject, boolean stop, int position) {
        apiDataObjectArrayList = new ArrayList<>();
        try {
            apiDataObjectArrayList.add(new ApiDataObject("ro_id", ro_ID));
            apiDataObjectArrayList.add(new ApiDataObject("driver_id", SharedPreferenceManager.getUserId(getApplicationContext())));
            apiDataObjectArrayList.add(new ApiDataObject("farmer_id", farmerId));
            apiDataObjectArrayList.add(new ApiDataObject("weight", jsonObject.getString("weight")));
            apiDataObjectArrayList.add(new ApiDataObject("product_id", jsonObject.getString("product_id")));
            apiDataObjectArrayList.add(new ApiDataObject("quantity", jsonObject.getString("quantity")));
            apiDataObjectArrayList.add(new ApiDataObject("type", jsonObject.getString("type")));
            apiDataObjectArrayList.add(new ApiDataObject("grade", jsonObject.getString("grade")));
            apiDataObjectArrayList.add(new ApiDataObject("date", jsonObject.getString("created_at")));
        } catch (JSONException e) {
            e.printStackTrace();
        }

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
                    Log.d("jsonObject", "jsonObject: stock: " + jsonObjectLoginResponse);
                    if (jsonObjectLoginResponse.getString("status").equals("1")) {
                        /*
                         * set progress
                         * */
                        setCurrentProgress(position++);
                        /*
                         * upload item's status set to 1
                         * */
                        updateStatus(jsonObject.getString("id"));
                        if (stop) {
                            //close progress dialog
                            showProgressDialog(false, "", 0);
                            //sent notiication
                            sendNotification();
                            //reset
                            clear();
                            //print purpose
                            confirmationDialog("Print Request", "Do you want to print the receipt?", 0, PRINT_CONFIRMATION);
                        }
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

    //update added item's status to 1 after upload
    private void updateStatus(String id) {
        String status = "1";
        String updated_at = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));
        tbStockSql.new Update("status, updated_at", status + "," + updated_at)
                .where("id =?", id)
                .perform();
    }

    //stock basket record to local when no connection
    private void storeToLocal() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FrameworkClass tbBasketSql = new FrameworkClass(getApplicationContext(), PickUpActivity.this, new CustomSqliteHelper(getApplicationContext()), TB_BASKET);
                tbBasketSql.new create("farmer_id, customer_id, quantity, type, created_at",
                        farmerId + ", 0 ," + pickUpActivityBasketQuantity.getText().toString().trim() + "," + basketStatus() + "," + getCurrentDateAndTime())
                        .perform();
            }
        }).start();
    }

    /*--------------------------------------------------------------upload basket purpose---------------------------------------------------------*/

    //update added item's status to 2 when no connection
    private void updateStatusWhenNoConnection() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String status = "2";
                String updated_at = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));
                tbStockSql.new Update("status, updated_at", status + "," + updated_at)
                        .where("session =? AND status != ?", accessSession + "," + "1")
                        .perform();
                //print purpose
                confirmationDialog("Print Request", "Do you want to print the receipt?", 0, PRINT_CONFIRMATION);
            }
        }).start();
    }

    private void basketControl() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("driver_id", SharedPreferenceManager.getUserId(getApplicationContext())));
                apiDataObjectArrayList.add(new ApiDataObject("farmer_id", farmerId));
                apiDataObjectArrayList.add(new ApiDataObject("customer_id", "0"));
                apiDataObjectArrayList.add(new ApiDataObject("quantity", pickUpActivityBasketQuantity.getText().toString().trim()));
                apiDataObjectArrayList.add(new ApiDataObject("type", basketStatus()));
                apiDataObjectArrayList.add(new ApiDataObject("date", getCurrentDateAndTime()));

                asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
                        new ApiManager().basket,
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
                                if (addedProductArrayList.size() > 0)
                                    performUpload();
                                else {
                                    clear();
                                    showProgressBar(false);
                                }
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

    /*----------------------------------------------------------create receive order id-----------------------------------------------------------*/
    /*
     * create receive order
     * */
    private void createReceiveOrderId(String result) {
        String id;
        try {
            id = new JSONObject(result).getJSONArray("result").getJSONObject(0).getString("id");
        } catch (JSONException e) {
            e.printStackTrace();
            //create first receive order id
            tbReceiveOrderSql.new create("created_at", accessSession).perform();
            id = "1";
        }
        tbReceiveOrderSql.new create("created_at", accessSession).perform();
        ro_ID = SharedPreferenceManager.getUserId(this) + android.text.format.DateFormat.format("yyMMdd", new java.util.Date()) + Integer.valueOf(id);
        //after (receive order id is create) then (insert) into all product that going to upload
        setReceiveOrderIntoStock();
    }

    private void setReceiveOrderIntoStock() {
        requestCode = UPDATE_RECEIVE_ORDER_ID;
        tbStockSql.new Update("ro_id", ro_ID).where("session = ?", accessSession).perform();
    }

    private void checkConnectionBeforeUpload() {
        if (!isOffLine)
            gettingUploadData();
        else {
            updateStatusWhenNoConnection();
            CustomScheduleJob.scheduleJob(this, new ComponentName(getApplicationContext(), PickUpNetworkMonitor.class), 1);
        }
    }

    /*----------------------------------------------------------send notification------------------------------------------------------------*/
    private void sendNotification() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("driver_id", SharedPreferenceManager.getUserId(getApplicationContext())));
                apiDataObjectArrayList.add(new ApiDataObject("notification", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("farmer_id", farmerId));
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
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                Log.d("jsonObject", "jsonObject: notification: " + jsonObjectLoginResponse);
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

    /*-------------------------------------------------------------print receipt--------------------------------------------------------------*/
    private void getDataForPrinting() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < addedProductArrayList.size(); i++) {
                    requestCode = PRINT_REQUEST;
                    tbStockSql.new Read("id, name, weight, COUNT(quantity)")
                            .where("session = " + accessSession + " AND product_id =" + addedProductArrayList.get(i).getId() + " GROUP BY weight, grade ")
                            .orderByAsc("weight")
                            .perform();
                }
            }
        }).start();
    }

    private void setupPrintingData(String json) {
        try {
            int totalQuantity = 0;
            StringBuilder weight = new StringBuilder();
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("result");
            for (int i = 0; i < jsonArray.length(); i++) {
                /*
                 * quantity
                 * */
                int quantity = Integer.valueOf(jsonArray.getJSONObject(i).getString("COUNT(quantity)"));
                totalQuantity = totalQuantity + quantity;
                /*
                 * weight
                 * */
                for (int j = 0; j < quantity; j++)
                    weight.append(",").append(jsonArray.getJSONObject(i).getString("weight")).append("KG");
            }
            printList.add(new ProductParentObject(
                    jsonArray.getJSONObject(0).getString("name"),
                    String.valueOf(totalQuantity),
                    weight.toString().substring(1)));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean print() {
        PrintfManager printfManager = PrintfManager.getInstance(getApplicationContext());
        if (printfManager.isConnect()) {
            printfManager.startPrint(farmerName, farmerAddress, ro_ID, printList);
            return true;
        } else {
            PrintfBlueListActivity.startActivity(PickUpActivity.this);
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == PROCEED_TO_PRINT) print();
    }

    /*------------------------------------------------------------date purpose-----------------------------------------------------------------*/
    private void setDefaultDate() {
        if (SharedPreferenceManager.getUserType(this).equals("1"))
            new AnimationUtility().fadeInVisible(this, pickUpActivityDateParentLayout);
        date = (date != null ? date : (String) android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()));
    }

    private String getCurrentDateAndTime() {
        return date + " " + android.text.format.DateFormat.format("HH:mm:ss", new java.util.Date());
    }

    private String getCurrentTime() {
        return (String) android.text.format.DateFormat.format("HH:mm:ss", new java.util.Date());
    }

    private void openDatePicker() {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        lastDate = String.format("%s", String.format(Locale.getDefault(), "%d-%02d-%02d", year, (monthOfYear + 1), dayOfMonth));
                        /*
                         * if ro_id != null then update ro_id's date
                         * */
                        if (!ro_ID.equals("")) changeDateConfirmationDialog();
                            /*
                             * select date as usual
                             * */
                        else {
                            date = String.format("%s", String.format(Locale.getDefault(), "%d-%02d-%02d", year, (monthOfYear + 1), dayOfMonth));
                            pickUpActivityDate.setText(date);
                            updateLocalRoDate();
                        }
                    }
                }, mYear, mMonth, mDay);

        datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
        datePickerDialog.show();
    }

    /*
     * update (created_at) in table stock when changed the date from date picker
     * */
    private void updateLocalRoDate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                new FrameworkClass(getApplicationContext(), new CustomSqliteHelper(getApplicationContext()), TB_STOCK)
                        .new Update("created_at", date + getCurrentTime())
                        .where("session = ? AND status = ?", accessSession + ", 0")
                        .perform();
            }
        }).start();
    }

    /*---------------------------------------------------------------------update receive order detail---------------------------------------------*/

    /*
     * get Receive Order detail
     * */
    private void getReceiveOrderDetail() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("ro_id", ro_ID));

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
                            Log.d("jsonObject", "ro_id detail: " + jsonObjectLoginResponse);
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            JSONObject jsonObject = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONObject("ro_detail");
                                            /*
                                             *set up RO  date detail
                                             * */
                                            date = jsonObject.getString("date");
                                            pickUpActivityDate.setText(date.substring(0, 10));
                                            /*
                                             * set up RO farmer detail
                                             * */
                                            farmerName = jsonObject.getString("farmer");
                                            farmerId = jsonObject.getString("farmer_id");
                                            pickUpActivityFarmer.setText(farmerName);
                                            /*
                                             * set up RO item
                                             * */
                                            storeRoItem(jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("ro_item"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
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

    /*
     * update Receive Order date
     * */
    public void changeDateConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("This will change all the item's date in #PO" + ro_ID + "\n Do you want to proceed?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Proceed",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        showProgressBar(true);
                        date = lastDate + " " + getCurrentTime();
                        /*
                         * update local database RO's date*/
                        updateLocalRoDate();
                        /*
                         * update cloud Ro's date
                         * */
                        updateDoDate();
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

    private void updateDoDate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("ro_id", ro_ID));
                apiDataObjectArrayList.add(new ApiDataObject("date", date));

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
                            Log.d("jsonObject", "jsonObject: " + jsonObjectLoginResponse);
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showSnackBar("Update Successfully");
                                        pickUpActivityDate.setText(date.substring(0, 10));
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

    /*
     * store ro's item into local database
     * */
    private void storeRoItem(final JSONArray jsonArray) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                /*
                 * show progress dialog
                 * */
                showProgressDialog(true, "Getting data...", jsonArray.length());
                FrameworkClass tbStockSql = new FrameworkClass(getApplicationContext(), new CustomSqliteHelper(getApplicationContext()), TB_STOCK);
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        tbStockSql.
                                new create("farmer_id, product_id, name, picture, price, type, quantity, weight, grade, session, created_at"
                                , new String[]{
                                jsonArray.getJSONObject(i).getString("farmer_id"),
                                jsonArray.getJSONObject(i).getString("product_id"),
                                jsonArray.getJSONObject(i).getString("product"),
                                jsonArray.getJSONObject(i).getString("picture"),
                                jsonArray.getJSONObject(i).getString("price"),
                                jsonArray.getJSONObject(i).getString("type"),
                                jsonArray.getJSONObject(i).getString("quantity"),
                                jsonArray.getJSONObject(i).getString("farmer_weight"),
                                jsonArray.getJSONObject(i).getString("grade")
                                , accessSession,
                                date}).perform();

                        //set progress
                        setCurrentProgress(i);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                showProgressDialog(false, "", 0);

                fetchSelectedProductApi();
            }
        }).start();
    }

    private void clearRoItemBeforeUpload() {
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("ro_id", ro_ID));
        apiDataObjectArrayList.add(new ApiDataObject("delete", "1"));

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
                    Log.d("jsonObject", "jsonObject: stock: " + jsonObjectLoginResponse);
                    if (jsonObjectLoginResponse.getString("status").equals("1")) {
                        checkConnectionBeforeUpload();
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

    public void setIsChanged() {
        isChanged = true;
    }

    /*
     * delete purchase order when no item is added when user click the update button
     * */
    public void deleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("Are you sure that you want to delete this purchase order?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Proceed",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        deleteReceiveOrder();
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

    /*
     * delete receive order when the array list size <= 0 while uploading
     * */
    private void deleteReceiveOrder() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("ro_id", ro_ID));
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

    /*-----------------------------------------------------------------------progress dialog-----------------------------------------------------*/
    private void showProgressDialog(final boolean show, final String label, final int maxProgress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialogProgressLayout.setVisibility(show ? View.VISIBLE : View.GONE);
                progressDialogLabel.setText(label);
                progressDialogDownloadMaxNumber.setText(String.format("/%d", maxProgress));
                progressDialogCurrentNumber.setText("0");
                progressDialogProgress.setMax(maxProgress);
            }
        });
    }

    private void setCurrentProgress(final int current) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialogProgress.setProgress(current);
                progressDialogCurrentNumber.setText(String.valueOf(current));
            }
        });
    }

    /*----------------------------------------------------------frame work call back-----------------------------------------------------------*/
    @Override
    public void createResult(String status) {
        requestCode = -1;
    }

    @Override
    public void readResult(final String result) {
        switch (requestCode) {
            case GET_PRODUCT_LIST:
                setProductList(result);
                break;
            case UPLOAD_STOCK:
                settingUploadData(result);
                break;
            case GET_RECEIVE_ORDER_ID:
                createReceiveOrderId(result);
                break;
            case PRINT_REQUEST:
                setupPrintingData(result);
                break;
            case GET_PARENT_DATA:
                getAllParentAddedProduct(result);
                break;
            case GET_CHILD_DATA:
                setChildValue(result);
                break;
        }
        requestCode = -1;
    }

    @Override
    public void updateResult(String status) {
        if (requestCode == UPDATE_RECEIVE_ORDER_ID) {
            checkConnectionBeforeUpload();
        }
        requestCode = -1;
    }

    @Override
    public void deleteResult(String status) {
        if (requestCode == DELETE_SELECTED_ITEM) {
            if (status.equals("Success")) {
                showSnackBar("Delete Successfully");
                deleteChecking();
            }
        }
        requestCode = -1;
    }

    //    -----------------------------------------------------------other-----------------------------------------------------------------------
//    snackBar setting
    public void showSnackBar(String message) {
        final Snackbar snackbar = Snackbar.make(addedProductList, message, Snackbar.LENGTH_SHORT);
        snackbar.setActionTextColor(getResources().getColor(R.color.blue));
        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    private void showNotFoundLayout(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notFoundLayout.setVisibility(show ? View.VISIBLE : View.GONE);
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

    //call back from off line mode dialog
    @Override
    public void clear() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addedProductArrayList.clear();
                addedProductAdapter.notifyDataSetChanged();

                actionbarSave.setEnabled(true);
                pickUpActivityBasketQuantity.setText("0");
                accessSession = String.valueOf(android.text.format.DateFormat.format("yyyyMMddHHmmss", new java.util.Date()));
                setVisibility();

                ro_ID = "";
                showSnackBar("Save Successfully!");
            }
        });
    }

    @Override
    public void onBackPressed() {
        /*
         * uploading can't on back press
         * */
        if (progressDialogProgressLayout.getVisibility() == View.VISIBLE) {
            return;
        }
        /*
         * if product layout is show then close
         * */
        if (pickUpActivityProductListLayout.getState() == STATE_EXPANDED) {
            pickUpActivityProductListLayout.setState(STATE_COLLAPSED);
            return;
        }
        /*
         * is user forgot to save
         * */
        if (!isUpdate || isChanged) {
            if (addedProductArrayList.size() > 0) {
                onBackPressConfirmation();
                return;
            }
        }
        //for update available basket quantity in home activity
        setResult(isUpdate ? UPDATE_PICK_UP_HISTORY : REFRESH_AVAILABLE_QUANTITY);

        //delete record when on back pressed
        new FrameworkClass(getApplicationContext(), new CustomSqliteHelper(getApplicationContext()), TB_STOCK)
                .new Delete()
                .where("status != ?", "2")
                .perform();

        super.onBackPressed();
    }

    /*
     * update Receive Order date
     * */
    public void onBackPressConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warning");
        builder.setMessage("Are you sure you want to leave without save?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Save Now",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        save();
                    }
                });

        builder.setNegativeButton("Leave", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
                addedProductArrayList.clear();
                onBackPressed();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
