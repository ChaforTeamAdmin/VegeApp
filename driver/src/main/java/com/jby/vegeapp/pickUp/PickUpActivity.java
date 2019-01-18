package com.jby.vegeapp.pickUp;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.vegeapp.R;
import com.jby.vegeapp.adapter.AddedProductAdapter;
import com.jby.vegeapp.database.CustomSqliteHelper;
import com.jby.vegeapp.database.FrameworkClass;
import com.jby.vegeapp.database.ResultCallBack;
import com.jby.vegeapp.network.PickUpNetworkMonitor;
import com.jby.vegeapp.object.ProductObject;
import com.jby.vegeapp.others.ExpandableHeightListView;
import com.jby.vegeapp.pickUp.farmer.FarmerDialog;
import com.jby.vegeapp.pickUp.product.ProductDialog;
import com.jby.vegeapp.shareObject.ApiDataObject;
import com.jby.vegeapp.shareObject.ApiManager;
import com.jby.vegeapp.shareObject.AsyncTaskManager;
import com.jby.vegeapp.sharePreference.SharedPreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.vegeapp.database.CustomSqliteHelper.TB_PICK_UP_FAVOURITE_FARMER;
import static com.jby.vegeapp.database.CustomSqliteHelper.TB_STOCK;

public class PickUpActivity extends AppCompatActivity implements View.OnClickListener, FarmerDialog.FarmerDialogCallBack,
        ProductDialog.ProductDialogCallBack, ResultCallBack, AddedProductAdapter.AddedProductAdapterCallBack,
        OffLineModeDialog.OffLineModeDialogCallBack {
    //actionbar
    private TextView actionbarSave;

    private LinearLayout pickUpActivityFarmerLayout;
    private TextView pickUpActivityFarmer;
    private Button pickUpActivitySelectItem;
    private DialogFragment dialogFragment, productDialog;
    private FragmentManager fm;
    //farmer
    private String farmerId, farmerName;
    private FrameworkClass frameworkClass;
    //added product list
    private TextView pickUpActivityLabelSelectedItem;
    private ExpandableHeightListView addedProductList;
    private ArrayList<ProductObject> productObjectArrayList;
    private AddedProductAdapter addedProductAdapter;

    private boolean onBackPressed = false;
    private boolean insertFromProductDialog = false;
    private Handler handler;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;

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
        actionbarSave = findViewById(R.id.actionbar_save);
        pickUpActivityFarmerLayout = findViewById(R.id.activity_pick_up_farmer_layout);
        pickUpActivityFarmer = findViewById(R.id.activity_pick_up_farmer);
        pickUpActivitySelectItem = findViewById(R.id.activity_pick_up_select_item);
        pickUpActivityLabelSelectedItem = findViewById(R.id.activity_pick_up_label_selected_item);

        addedProductList = findViewById(R.id.activity_pick_up_add_product_list);
        productObjectArrayList = new ArrayList<>();
        addedProductAdapter = new AddedProductAdapter(this, productObjectArrayList, this);

        frameworkClass = new FrameworkClass(this, this, new CustomSqliteHelper(this), TB_STOCK);
        fm = getSupportFragmentManager();
        handler = new Handler();
    }

    private void objectSetting() {
        actionbarSave.setOnClickListener(this);
        pickUpActivityFarmerLayout.setOnClickListener(this);
        pickUpActivitySelectItem.setOnClickListener(this);

        addedProductList.setAdapter(addedProductAdapter);
        addedProductList.setExpanded(true);
        setDefaultFarmer();
        setVisibility();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.actionbar_save:
                save();
                break;
            case R.id.activity_pick_up_farmer_layout:
                openFarmDialog();
                break;
            case R.id.activity_pick_up_select_item:
                openProductDialog();
                break;
        }
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
        setUpButton();
    }

    private void setDefaultFarmer() {
        String farmerDetail = SharedPreferenceManager.getPickUpDefaultFarmer(this);

        if(!farmerDetail.equals("default")){
            String[] farmerDetails = farmerDetail.split(",");
            farmerName = farmerDetails[0];
            farmerId = farmerDetails[1];
            pickUpActivityFarmer.setText(farmerName);
            setUpButton();
        }
    }

    private void setUpButton(){
        pickUpActivitySelectItem.setBackground(getDrawable(R.drawable.custom_button));
        pickUpActivitySelectItem.setEnabled(true);
    }

//    -----------------------------------------------------------------product----------------------------------------------------------------

    private void openProductDialog(){
        productDialog = new ProductDialog();
        productDialog.show(fm, "");
    }

    @Override
    public void add(ProductObject productObject, String quantity) {
        String date = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));
        Log.d("haha", "number of record: " + isExistedRecord(productObject.getId()));
        Log.d("haha", "number of record: " + accessSession);
        if(isExistedRecord(productObject.getId()) > 0){
            int newQuantity = Integer.valueOf(quantity) + currentQuantity(productObject.getId());
            //to prevent the list view keep refresh
            insertFromProductDialog = true;
            frameworkClass.
                    new Update("quantity, updated_at", String.valueOf(newQuantity) + " , " + date)
                    .where("session = ? AND product_id = ?", accessSession + " , " + productObject.getId())
                    .perform();
        }
        else{
            frameworkClass.
                    new create("farmer_id, product_id, name, picture, price, type, quantity, session, created_at",
                    farmerId+ ", "+ productObject.getId() + ", "+ productObject.getName() + ", "+ productObject.getPicture()
                            + ", "+ productObject.getPrice() + ", "+productObject.getType()+ ", " + quantity + ", "+ accessSession + ", " + date)
                    .perform();
        }
    }

    private int isExistedRecord(String productId){
       return frameworkClass.
                new Read("id")
                .where("session = " + accessSession + " AND " + "product_id = " + productId)
                .count();
    }

    private int currentQuantity(String productId){
        int position = 0;
        for(int i =  0 ; i < productObjectArrayList.size(); i++){
            if(productObjectArrayList.get(i).getId().equals(productId)){
                position = i;
                break;
            }
        }
        return Integer.valueOf(productObjectArrayList.get(position).getQuantity());
    }
//    ------------------------------------------------------------------added product list call back--------------------------------------
    private void fetchSelectedProductApi(){
        frameworkClass.new Read("product_id, name, picture, type, price, quantity")
                .where("session = " + accessSession)
                .orderByDesc("product_id")
                .perform();
    }

    @Override
    public void update(ProductObject productObject, String quantity) {
        String updated_at = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));
        //update quantity in current array
        updateCurrentQuantityInList(productObject, quantity);
        frameworkClass.new Update("quantity, updated_at", quantity + "," + updated_at)
                .where("product_id =? AND session =?", productObject.getId() + "," + accessSession)
                .perform();
    }

    private void updateCurrentQuantityInList(ProductObject productObject, String quantity) {
        int position = 0;
        for (int i = 0; i < productObjectArrayList.size(); i++) {
            if (productObjectArrayList.get(i).getId().equals(productObject.getId())) {
                position = i;
                break;
            }
        }
        productObject.setQuantity(quantity);
        productObjectArrayList.set(position, productObject);
    }

    @Override
    public void delete(String product_id) {
        if(!onBackPressed)
            frameworkClass.new Delete().where("product_id = ? AND session = ?", product_id + "," + accessSession).perform();
    }

    private void getAllAddedProduct(String json){
        if(productObjectArrayList.size() > 0) productObjectArrayList.clear();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("result");
            for(int i = 0 ; i < jsonArray.length(); i++){
                productObjectArrayList.add(new ProductObject(
                        jsonArray.getJSONObject(i).getString("product_id"),
                        jsonArray.getJSONObject(i).getString("name"),
                        jsonArray.getJSONObject(i).getString("picture"),
                        jsonArray.getJSONObject(i).getString("type"),
                        jsonArray.getJSONObject(i).getString("price"),
                        jsonArray.getJSONObject(i).getString("quantity")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        addedProductAdapter.notifyDataSetChanged();
        setVisibility();
    }

    public void setVisibility(){
        if(productObjectArrayList.size() > 0) {
            pickUpActivityLabelSelectedItem.setVisibility(View.VISIBLE);
            actionbarSave.setVisibility(View.VISIBLE);
        }
        else {
            pickUpActivityLabelSelectedItem.setVisibility(View.GONE);
            actionbarSave.setVisibility(View.GONE);
        }
    }

    /*----------------------------------------------------------------upload stock to server------------------------------------------------------*/
    private void save(){
        if(checkNetworkConnection()){
            for(int i = 0; i < productObjectArrayList.size(); i++){
                storeToCloud(i);
            }
        }
        else{
            updateStatusWhenNoConnection();
            scheduleJob();
            openOffLineModeDialog();
        }
    }

    private void storeToCloud(int position){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("driver_id", SharedPreferenceManager.getUserId(this)));
        apiDataObjectArrayList.add(new ApiDataObject("farmer_id", farmerId));
        apiDataObjectArrayList.add(new ApiDataObject("product_id", productObjectArrayList.get(position).getId()));
        apiDataObjectArrayList.add(new ApiDataObject("price", productObjectArrayList.get(position).getPrice()));
        apiDataObjectArrayList.add(new ApiDataObject("quantity", productObjectArrayList.get(position).getQuantity()));
        apiDataObjectArrayList.add(new ApiDataObject("type", productObjectArrayList.get(position).getType()));

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
                        updateStatus(productObjectArrayList.get(position).getId());

                        if(position == productObjectArrayList.size()-1){
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

    private void updateStatus(String productId){
        String status = "1";
        String updated_at = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));
        frameworkClass.new Update("status, updated_at",  status + "," + updated_at)
                .where("product_id =? AND session =?", productId + "," + accessSession)
                .perform();
    }

    private void updateStatusWhenNoConnection(){
        String status = "2";
        String updated_at = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));
        frameworkClass.new Update("status, updated_at", status + "," + updated_at)
                .where("session =?", accessSession)
                .perform();
    }

    public boolean checkNetworkConnection(){
        ConnectivityManager connectivityManager = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return (networkInfo !=  null && networkInfo.isConnected());
    }

    public void scheduleJob() {
        ComponentName componentName = new ComponentName(this, PickUpNetworkMonitor.class);
        JobInfo info = new JobInfo.Builder(1, componentName)
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

    //call back from off line mode dialog
    @Override
    public void clear() {
        productObjectArrayList.clear();
        addedProductAdapter.notifyDataSetChanged();

        setVisibility();
        accessSession = String.valueOf(android.text.format.DateFormat.format("yyyyMMddHHmmss", new java.util.Date()));
        showSnackBar("Save Successfully!");
    }

    /*----------------------------------------------------------frame work call back-----------------------------------------------------------*/
    @Override
    public void createResult(String status) {
        if(status.equals("Success")) fetchSelectedProductApi();
    }

    @Override
    public void readResult(String result) {
        getAllAddedProduct(result);
    }

    @Override
    public void updateResult(String status) {
        //only refresh the list view when it is inserted from product dialog
        if(insertFromProductDialog)
            if(status.equals("Success")) fetchSelectedProductApi();
        insertFromProductDialog = false;
    }

    @Override
    public void deleteResult(String status) {
        if(status.equals("Success")) {
            showSnackBar("Delete Successfully");
            fetchSelectedProductApi();
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
