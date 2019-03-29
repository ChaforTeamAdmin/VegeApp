package com.jby.vegeapp.pickUp;

import android.app.AlertDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.NestedScrollView;
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
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.vegeapp.R;
import com.jby.vegeapp.adapter.AddedProductExpandableAdapter;
import com.jby.vegeapp.adapter.ProductAdapter;
import com.jby.vegeapp.database.CustomSqliteHelper;
import com.jby.vegeapp.database.FrameworkClass;
import com.jby.vegeapp.database.ResultCallBack;
import com.jby.vegeapp.network.BasketNetworkMonitor;
import com.jby.vegeapp.network.PickUpNetworkMonitor;
import com.jby.vegeapp.object.ProductChildObject;
import com.jby.vegeapp.object.ProductParentObject;
import com.jby.vegeapp.object.ProductObject;
import com.jby.vegeapp.others.NetworkConnection;
import com.jby.vegeapp.others.NonScrollExpandableListView;
import com.jby.vegeapp.others.recycleview.GridSpacingItemDecoration;
import com.jby.vegeapp.pickUp.farmer.FarmerDialog;
import com.jby.vegeapp.pickUp.product.AddProductDialog;
import com.jby.vegeapp.shareObject.AnimationUtility;
import com.jby.vegeapp.shareObject.ApiDataObject;
import com.jby.vegeapp.shareObject.ApiManager;
import com.jby.vegeapp.shareObject.AsyncTaskManager;
import com.jby.vegeapp.sharePreference.SharedPreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.vegeapp.database.CustomSqliteHelper.TB_BASKET;
import static com.jby.vegeapp.database.CustomSqliteHelper.TB_PICK_UP_FAVOURITE_FARMER;
import static com.jby.vegeapp.database.CustomSqliteHelper.TB_STOCK;
import static com.jby.vegeapp.shareObject.CustomToast.CustomToast;

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
    //farmer
    private String farmerId, farmerName;
    private FrameworkClass frameworkClass;
    //product list
    private RecyclerView productDialogProductList;
    private ArrayList<ProductObject> productObjectArrayList;
    private ProductAdapter productAdapter;
    private RelativeLayout pickUpActivityProductListLayout;
    private EditText pickUpActivityProductSearch;
    private ProgressBar progressBar;
    //added product list
    private LinearLayout pickUpActivityAddItemLayout;
    private TextView pickUpActivityLabelSelectedItem;
    private NonScrollExpandableListView addedProductList;
    private ArrayList<ProductParentObject> addedProductArrayList;
    private AddedProductExpandableAdapter addedProductAdapter;
    private boolean isParent = true;
    private int position;
    private boolean preventDoubleClick = true;
    //basket
    private LinearLayout pickUpActivityBasketLayout;
    private Spinner pickUpActivityBasketStatus;
    private EditText pickUpActivityBasketQuantity;
    private ImageView pickUpActivityPlusBasket;

    private boolean onBackPressed = false;
    private Handler handler;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private boolean isUpload = false;

    private String accessSession = String.valueOf(android.text.format.DateFormat.format("yyyyMMddHHmmss", new java.util.Date()));
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
        pickUpActivityProductListLayout = findViewById(R.id.activity_pick_up_product_list_layout);
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
        
        addedProductList = findViewById(R.id.activity_pick_up_add_product_list);
        addedProductArrayList = new ArrayList<>();
        addedProductAdapter = new AddedProductExpandableAdapter(this, addedProductArrayList, this);

        productObjectArrayList = new ArrayList<>();
        productAdapter = new ProductAdapter(this, productObjectArrayList, this);

        frameworkClass = new FrameworkClass(this, this, new CustomSqliteHelper(this), TB_STOCK);
        fm = getSupportFragmentManager();
        handler = new Handler();
    }

    private void objectSetting() {
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 3);
        productDialogProductList.setLayoutManager(mLayoutManager);
        productDialogProductList.addItemDecoration(new GridSpacingItemDecoration(3, dpToPx(5), true));
        productDialogProductList.setItemAnimator(new DefaultItemAnimator());
        productDialogProductList.setAdapter(productAdapter);

        actionbarSave.setOnClickListener(this);
        actionbarSave.setVisibility(View.VISIBLE);

        pickUpActivityFarmerLayout.setOnClickListener(this);
        pickUpActivitySelectItemLayout.setOnClickListener(this);
        //product
        pickUpActivityProductSearch.setOnClickListener(this);
        pickUpActivityProductSearch.addTextChangedListener(this);
        //basket
        pickUpActivityPlusBasket.setOnClickListener(this);
        //added product list
        addedProductList.setAdapter(addedProductAdapter);
        addedProductList.setOnChildClickListener(this);
        addedProductList.setOnGroupClickListener(this);
        setupActionBar();
        setDefaultFarmer();
        setVisibility();
        setupSpinner();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.actionbar_save:
                checkingBeforeSave();
                break;
            case R.id.activity_pick_up_farmer_layout:
                openFarmDialog();
                break;
            case R.id.activity_pick_up_select_item_layout:
                showProductLayout();
                break;
            case R.id.activity_pick_up_plus_basket_button:
                setQuantity(true);
                break;
        }
    }

    private void setupActionBar() {
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Pick Up");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

//    -------------------------------------------------------------------farmer---------------------------------------------------------------

    private void openFarmDialog(){
        dialogFragment = new FarmerDialog();
        dialogFragment.show(fm, "");
    }

    @Override
    public void selectedItem(String name, String farmerId, String address) {
        this.farmerId = farmerId;
        new FrameworkClass(this, new CustomSqliteHelper(this), TB_PICK_UP_FAVOURITE_FARMER)
                .new create("farmer_id, name",farmerId + "," + name)
                .perform();

        pickUpActivityFarmer.setText(name);
        SharedPreferenceManager.setPickUpDefaultFarmer(this, name + "," + farmerId);
        setUpView();
    }

    private void setDefaultFarmer() {
        String farmerDetail = SharedPreferenceManager.getPickUpDefaultFarmer(this);

        if(!farmerDetail.equals("default")){
            String[] farmerDetails = farmerDetail.split(",");
            farmerName = farmerDetails[0];
            farmerId = farmerDetails[1];
            pickUpActivityFarmer.setText(farmerName);
            setUpView();
        }
    }

    private void setUpView(){
        new AnimationUtility().fadeInVisible(this, pickUpActivityBasketLayout);
        new AnimationUtility().fadeInVisible(this, pickUpActivityAddItemLayout);

        if(pickUpActivityBasketQuantity.getText().toString().trim().equals(""))pickUpActivityBasketQuantity.append("0");
        pickUpActivitySelectItemLayout.setVisibility(View.VISIBLE);
    }

    //    -----------------------------------------------------------------basket----------------------------------------------------------------
    private void setQuantity(boolean plus){
        if(pickUpActivityBasketQuantity.getText().toString().trim().equals("")) pickUpActivityBasketQuantity.setText("0");

        int quantity = Integer.valueOf(pickUpActivityBasketQuantity.getText().toString().trim());
        if(plus) quantity ++;
        else {if(quantity > 0) quantity --;}
        pickUpActivityBasketQuantity.setText("");
        pickUpActivityBasketQuantity.append(String.valueOf(quantity));
    }

    private String basketStatus(){
        return basketStatus(pickUpActivityBasketStatus.getSelectedItem().toString());
    }

    private String basketStatus(String checkPosition){
        String status = "";
        switch (checkPosition){
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
    //layout setting
    private void showProductLayout(){
        if(pickUpActivityProductListLayout.getVisibility() == View.VISIBLE){
            showProductLayout(false);

        }
        else{
            progressBar.setVisibility(View.VISIBLE);
            showProductLayout(true);

            if(productObjectArrayList.size() <= 0)
                handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fetchAllProduct();
                }
            },300);
            else{
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    private void showProductLayout(boolean show){
        if(!show){
            pickUpActivityLabelSelectItem.setText("Click here to show item");
            pickUpActivityArrowIcon.setImageDrawable(getDrawable(R.drawable.activity_pick_up_arrow_down));
            new AnimationUtility().fadeOutGone(this, pickUpActivityProductListLayout);
        }
        else{
            pickUpActivityLabelSelectItem.setText("Click here to hide item");
            pickUpActivityArrowIcon.setImageDrawable(getDrawable(R.drawable.arrow_up));
            new AnimationUtility().fadeInVisible(this, pickUpActivityProductListLayout);
        }
    }

    private void fetchAllProduct(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("fetch", "1"));

        asyncTaskManager = new AsyncTaskManager(
                this,
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
                    Log.d("jsonObject", "jsonObject: " +  jsonObjectLoginResponse);
                    if(jsonObjectLoginResponse.getString("status").equals("1")){
                        JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("product");
                        for(int i = 0; i < jsonArray.length(); i++)
                        {
                            productObjectArrayList.add(new ProductObject(
                                    jsonArray.getJSONObject(i).getString("id"),
                                    jsonArray.getJSONObject(i).getString("name"),
                                    jsonArray.getJSONObject(i).getString("picture"),
                                    jsonArray.getJSONObject(i).getString("type"),
                                    jsonArray.getJSONObject(i).getString("price")));
                        }
                    }
                }
                else {
                    Toast.makeText(this, "Network Error!", Toast.LENGTH_SHORT).show();
                }

            } catch (InterruptedException e) {
                Toast.makeText(this, "Interrupted Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (ExecutionException e) {
                Toast.makeText(this, "Execution Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (JSONException e) {
                Toast.makeText(this, "JSON Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (TimeoutException e) {
                Toast.makeText(this, "Connection Time Out!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                productAdapter.notifyDataSetChanged();

                pickUpActivityProductListLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        },200);

    }
//     Converting dp to pixel
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    private void searchFromArrayList(String query){
        ArrayList<ProductObject> searchList = new ArrayList<>();
        for(int i = 0 ; i < productObjectArrayList.size(); i++){
            if(productObjectArrayList.get(i).getName().contains(query)) {
                searchList.add(productObjectArrayList.get(i));
            }
        }
        productAdapter = new ProductAdapter(this, searchList, this);
        productDialogProductList.setAdapter(productAdapter);
        productAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(final Editable editable) {
        progressBar.setVisibility(View.VISIBLE);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                searchFromArrayList(editable.toString());
            }
        },700);
    }

    @Override
    public void openAddProductDialog(String productID, String product, String price, String picture, String type, String quantity, String weight, String grade) {
        dialogFragment = new AddProductDialog();
        Bundle bundle = new Bundle();

        bundle.putString("product_id", productID);
        bundle.putString("product", product);
        bundle.putString("farmer_id", farmerId);
        bundle.putString("price", price);
        bundle.putString("picture", picture);
        bundle.putString("type", type);
        bundle.putString("session", accessSession);

        bundle.putString("quantity", quantity);
        bundle.putString("weight", weight);
        bundle.putString("grade", grade);

        dialogFragment.setArguments(bundle);
        if(preventDoubleClick) {
            dialogFragment.show(fm, "");
            preventDoubleClick = false;
        }
        //reset the prevent double to to true
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                preventDoubleClick = true;
            }
        },200);
    }

//    ------------------------------------------------------------------added product list call back--------------------------------------
    //------------------------------------------------------group item----------------------------------------------------------------------------
    public void fetchSelectedProductApi(){
        isUpload = false;
        // getting child item
        isParent = true;
        frameworkClass.new Read("product_id, name, picture, SUM(quantity), price, type")
                .where("session = " + accessSession + " GROUP BY product_id ")
                .orderByDesc("id")
                .perform();
    }

    private void getAllParentAddedProduct(String json){
        //close all group
        closeOtherChildView(-1);

        if(addedProductArrayList.size() > 0) addedProductArrayList.clear();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("result");
            for(int i = 0 ; i < jsonArray.length(); i++){
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

    @Override
    public boolean onGroupClick(ExpandableListView expandableListView, View view, final int i, long l) {
        // getting child item
        isParent = false;
        //set position
        this.position = i;

        //close the group if opened
        if (expandableListView.isGroupExpanded(i)) {
            expandableListView.collapseGroup(i);
        }
        else{
            //close other view
            closeOtherChildView(i);
            addedProductArrayList.get(i).getProductChildObjectArrayList().clear();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fetchChildItem();

                }
            },200);
        }
        return true;
    }

    private void closeOtherChildView(int position){
        for(int i = 0 ; i < productObjectArrayList.size(); i ++){
            if(i != position) addedProductList.collapseGroup(i);
        }
    }

/*--------------------------------------------------------child item------------------------------------------------------------------------------*/

    public void fetchChildItem(){
        //clear
        addedProductArrayList.get(position).getProductChildObjectArrayList().clear();

        frameworkClass.new Read("id, weight, COUNT(quantity), grade")
                .where("session = " + accessSession + " AND product_id =" + addedProductArrayList.get(position).getId() + " GROUP BY weight, grade ")
                .orderByDesc("id")
                .perform();
    }

    private void setChildValue(String result){
        try {
            JSONArray jsonArray = new JSONObject(result).getJSONArray("result");
            for(int i = 0; i < jsonArray.length(); i++){
                addedProductArrayList.get(position).setAddedProductChildObjectArrayList(setChildObject(jsonArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        addedProductList.expandGroup(position);
        addedProductList.setSelectedGroup(position);
        addedProductAdapter.notifyDataSetChanged();
    }

    private ProductChildObject setChildObject(JSONObject jsonObject){
        ProductChildObject object = null;
        try {
            object = new ProductChildObject(
                    jsonObject.getString("id"),
                    jsonObject.getString("weight"),
                    jsonObject.getString("grade"),
                    jsonObject.getString("COUNT(quantity)"),
                    "pick_up");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    public void setVisibility(){
        if(addedProductArrayList.size() > 0) {
            pickUpActivityLabelSelectedItem.setVisibility(View.VISIBLE);
        }
        else {
            pickUpActivityLabelSelectedItem.setVisibility(View.GONE);
            //product layout
            showProductLayout(false);
        }
    }

    public void delete(int childPosition) {
        String weight = addedProductArrayList.get(position).getProductChildObjectArrayList().get(childPosition).getWeight();
        String grade = addedProductArrayList.get(position).getProductChildObjectArrayList().get(childPosition).getGrade();
        String productID = addedProductArrayList.get(position).getId();

        frameworkClass.new Delete()
                .where("weight = ? AND session = ? AND product_id = ? AND grade = ?",
                        weight + "," + accessSession + "," + productID + "," + grade)
                .perform();
    }

    private void deleteChecking(){
        //number child < 1
        if(addedProductArrayList.get(position).getProductChildObjectArrayList().size() > 1){
            fetchChildItem();
        }
        else{
            //if this position of parent contain only one child then redraw the list when delete to avoid error
            addedProductArrayList.clear();
            fetchSelectedProductApi();
            closeOtherChildView(-1);
        }
    }

    public void deleteMessage(String title,String content, final int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(content);
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        delete(position);
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

    /*----------------------------------------------------------------upload stock to server------------------------------------------------------*/
    private void checkingBeforeSave(){
        if(!pickUpActivityBasketQuantity.getText().toString().equals("0") && !pickUpActivityBasketQuantity.getText().toString().equals("") || addedProductArrayList.size() > 0) save();
        else showSnackBar("Nothing to upload!");

    }

    private void save(){
        progressBar.setVisibility(View.VISIBLE);
        actionbarSave.setEnabled(false);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(new NetworkConnection(PickUpActivity.this).checkNetworkConnection()){
                    isUpload = true;
                    //if basket quantity != 0
                    if(!pickUpActivityBasketQuantity.getText().toString().equals("") && !pickUpActivityBasketQuantity.getText().toString().equals("0")){
                        basketControl();
                    }
                    else{
                        if(addedProductArrayList.size() > 0) frameworkClass.new Read("*").where("session = " + accessSession).perform();
                    }
                }
                else{
                    //basket
                    if(!pickUpActivityBasketQuantity.getText().toString().equals("") || !pickUpActivityBasketQuantity.getText().toString().equals("0")){
                        storeToLocal();
                        scheduleJob(false);
                    }
                    //product
                    if(addedProductArrayList.size() > 0){
                        updateStatusWhenNoConnection();
                        scheduleJob(true);
                        openOffLineModeDialog();
                    }
                    //view setting
                    progressBar.setVisibility(View.GONE);
                    actionbarSave.setEnabled(true);
                }
            }
        },200);
    }
    //product
    private void gettingUploadData(String result){
        try {
            JSONArray jsonArray = new JSONObject(result).getJSONArray("result");
            for(int i = 0 ; i < jsonArray.length(); i++){
                storeToCloud(jsonArray.getJSONObject(i), i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        progressBar.setVisibility(View.GONE);
    }

    private void storeToCloud(JSONObject jsonObject, int position){
        apiDataObjectArrayList = new ArrayList<>();
        try {
            apiDataObjectArrayList.add(new ApiDataObject("driver_id", SharedPreferenceManager.getUserId(this)));
            apiDataObjectArrayList.add(new ApiDataObject("farmer_id", farmerId));
            apiDataObjectArrayList.add(new ApiDataObject("weight", jsonObject.getString("weight")));
            apiDataObjectArrayList.add(new ApiDataObject("product_id", jsonObject.getString("product_id")));
            apiDataObjectArrayList.add(new ApiDataObject("price", jsonObject.getString("price")));
            apiDataObjectArrayList.add(new ApiDataObject("quantity", jsonObject.getString("quantity")));
            apiDataObjectArrayList.add(new ApiDataObject("type", jsonObject.getString("type")));
            apiDataObjectArrayList.add(new ApiDataObject("grade", jsonObject.getString("grade")));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        asyncTaskManager = new AsyncTaskManager(
                this,
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
                    Log.d("jsonObject", "jsonObject: " +  jsonObjectLoginResponse);
                    if(jsonObjectLoginResponse.getString("status").equals("1")){
                        updateStatus(jsonObject.getString("id"));

                        if(position == addedProductArrayList.size()-1){
                            //reset
                            clear();
                        }
                    }
                }
                else {
                    Toast.makeText(this, "Network Error!", Toast.LENGTH_SHORT).show();
                }

            } catch (InterruptedException e) {
                Toast.makeText(this, "Interrupted Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (ExecutionException e) {
                Toast.makeText(this, "Execution Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (JSONException e) {
                Toast.makeText(this, "JSON Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (TimeoutException e) {
                Toast.makeText(this, "Connection Time Out!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void updateStatus(String id){
        String status = "1";
        String updated_at = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));
        frameworkClass.new Update("status, updated_at",  status + "," + updated_at)
                .where("id =?", id)
                .perform();
    }

    private void updateStatusWhenNoConnection(){
        String status = "2";
        String updated_at = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));
        frameworkClass.new Update("status, updated_at", status + "," + updated_at)
                .where("session =?", accessSession)
                .perform();
    }

    public void scheduleJob(boolean product) {
        ComponentName componentName;
        int jobId;
        if(product){
            componentName = new ComponentName(this, PickUpNetworkMonitor.class);
            jobId = 1;
        }
        else{
            componentName = new ComponentName(this, BasketNetworkMonitor.class);
            jobId = 2;
        }

        JobInfo info = new JobInfo.Builder(jobId, componentName)
                .setRequiresCharging(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setPeriodic(15 * 60 * 1000)
                .build();

        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = 0;
        if (scheduler != null) {
            resultCode = scheduler.schedule(info);
        }
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d("PickUpActivity", "Job scheduled");
        } else {
            Log.d("PickUpActivity", "Job scheduling failed");
        }
    }

    private void openOffLineModeDialog(){
        dialogFragment = new OffLineModeDialog();
        dialogFragment.show(fm, "");
    }

    private void basketControl(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("driver_id", SharedPreferenceManager.getUserId(this)));
        apiDataObjectArrayList.add(new ApiDataObject("farmer_id", farmerId));
        apiDataObjectArrayList.add(new ApiDataObject("customer_id", "0"));
        apiDataObjectArrayList.add(new ApiDataObject("quantity", pickUpActivityBasketQuantity.getText().toString().trim()));
        apiDataObjectArrayList.add(new ApiDataObject("type", basketStatus()));

        asyncTaskManager = new AsyncTaskManager(
                this,
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
                    Log.d("jsonObject", "jsonObject: " +  jsonObjectLoginResponse);
                    if(jsonObjectLoginResponse.getString("status").equals("1")){
                        if(addedProductArrayList.size() > 0) frameworkClass.new Read("*").where("session = " + accessSession).perform();
                        else {
                            clear();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                }
                else {
                    Toast.makeText(this, "Network Error!", Toast.LENGTH_SHORT).show();
                }

            } catch (InterruptedException e) {
                Toast.makeText(this, "Interrupted Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (ExecutionException e) {
                Toast.makeText(this, "Execution Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (JSONException e) {
                Toast.makeText(this, "JSON Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (TimeoutException e) {
                Toast.makeText(this, "Connection Time Out!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void storeToLocal(){
        String created_at = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));
        FrameworkClass frameworkClass = new FrameworkClass(this, this, new CustomSqliteHelper(this), TB_BASKET);
        frameworkClass.new create("farmer_id, customer_id, quantity, type, created_at",
                farmerId + ", 0 ," + pickUpActivityBasketQuantity.getText().toString().trim() + "," + basketStatus() + "," +created_at)
                .perform();

    }
    //call back from off line mode dialog
    @Override
    public void clear() {
        addedProductArrayList.clear();
        addedProductAdapter.notifyDataSetChanged();

        actionbarSave.setEnabled(true);
        pickUpActivityBasketQuantity.setText("0");
        accessSession = String.valueOf(android.text.format.DateFormat.format("yyyyMMddHHmmss", new java.util.Date()));

        setVisibility();
        showSnackBar("Save Successfully!");
    }

    /*----------------------------------------------------------frame work call back-----------------------------------------------------------*/
    @Override
    public void createResult(String status) {
        if(status.equals("Success")) fetchSelectedProductApi();
    }

    @Override
    public void readResult(final String result) {
        //is Upload == true mean user uploading data
        if(isUpload) handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                gettingUploadData(result);
            };
        },200);
        else{
            if(isParent) getAllParentAddedProduct(result);
            else setChildValue(result);
        }

    }

    @Override
    public void updateResult(String status) {

    }

    @Override
    public void deleteResult(String status) {
        if(status.equals("Success") && !onBackPressed) {
            showSnackBar("Delete Successfully");
            deleteChecking();
        }
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

    @Override
    public void onBackPressed() {
        onBackPressed = true;
        //delete record when on back pressed
        frameworkClass.new Delete()
                .where("status = ?", "0")
                .perform();
        super.onBackPressed();
    }

}
