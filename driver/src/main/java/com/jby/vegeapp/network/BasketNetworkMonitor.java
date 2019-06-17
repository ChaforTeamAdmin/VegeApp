package com.jby.vegeapp.network;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;
import android.widget.Toast;

import com.jby.vegeapp.database.CustomSqliteHelper;
import com.jby.vegeapp.database.FrameworkClass;
import com.jby.vegeapp.database.ResultCallBack;
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

import static com.jby.vegeapp.database.CustomSqliteHelper.TB_BASKET;
import static com.jby.vegeapp.shareObject.CustomToast.CustomToast;

public class BasketNetworkMonitor extends JobService implements ResultCallBack {

    public static final String TAG = "PickUpActivity";
    private boolean jobCancelled = false;
    private JobParameters params;
    private FrameworkClass frameworkClass = new FrameworkClass(this, this, new CustomSqliteHelper(this), TB_BASKET);

    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Job started");
        doBackgroundWork(params);
        return true;
    }

    private void doBackgroundWork(final JobParameters params) {
        this.params = params;
        new Thread(new Runnable() {
            @Override
            public void run() {
                frameworkClass.new Read("id, farmer_id, customer_id, quantity, type, created_at")
                        .where("status = 0")
                        .orderByDesc("id").perform();
            }
        }).start();
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled before completion");
        jobCancelled = true;
        return true;
    }

    private void basketControl(String id, String farmerID, String customerID, String quantity, String type, String date){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("driver_id", SharedPreferenceManager.getUserId(this)));
        apiDataObjectArrayList.add(new ApiDataObject("farmer_id", farmerID));
        apiDataObjectArrayList.add(new ApiDataObject("customer_id", customerID));
        apiDataObjectArrayList.add(new ApiDataObject("quantity", quantity));
        apiDataObjectArrayList.add(new ApiDataObject("type", type));
        apiDataObjectArrayList.add(new ApiDataObject("date", date));

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
                        updateStatus(id);
                    }
                }
                else {
                    CustomToast(this, "Network Error!");
                }

            } catch (InterruptedException e) {
                CustomToast(this, "Interrupted Exception!");
                e.printStackTrace();
            } catch (ExecutionException e) {
                CustomToast(this, "Execution Exception!");
                e.printStackTrace();
            } catch (JSONException e) {
                CustomToast(this, "JSON Exception!");
                e.printStackTrace();
            } catch (TimeoutException e) {
                CustomToast(this, "Connection Time Out!");
                e.printStackTrace();
            }
        }
    }

    private void updateStatus(String id){
        frameworkClass.new Delete().where("id = ?" , id).perform();
    }
/*-----------------------------------------------------------------framework call back--------------------------------------------------------------*/
    @Override
    public void createResult(String status) {

    }

    @Override
    public void readResult(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("result");
            for(int i = 0 ; i < jsonArray.length(); i++){
                    basketControl(
                            jsonArray.getJSONObject(i).getString("id"),
                            jsonArray.getJSONObject(i).getString("farmer_id"),
                            jsonArray.getJSONObject(i).getString("customer_id"),
                            jsonArray.getJSONObject(i).getString("quantity"),
                            jsonArray.getJSONObject(i).getString("type"),
                            jsonArray.getJSONObject(i).getString("created_at")
                    );
                jobFinished(params, true);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateResult(String status) {

    }

    @Override
    public void deleteResult(String status) {

    }
}
