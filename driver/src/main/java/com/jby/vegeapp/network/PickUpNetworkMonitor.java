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

import static com.jby.vegeapp.database.CustomSqliteHelper.TB_STOCK;
import static com.jby.vegeapp.shareObject.CustomToast.CustomToast;


public class PickUpNetworkMonitor extends JobService implements ResultCallBack {

    public static final String TAG = "PickUpActivity";
    private boolean jobCancelled = false;
    private JobParameters params;
    private FrameworkClass frameworkClass = new FrameworkClass(this, this, new CustomSqliteHelper(this), TB_STOCK);
    private JSONArray jsonArray;
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
                frameworkClass.new Read("*").where("status = 2").orderByDesc("id").perform();
            }
        }).start();
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled before completion");
        jobCancelled = true;
        return true;
    }

    @Override
    public void createResult(String status) {

    }

    @Override
    public void readResult(String result) {
        Log.d("haha", "result:  " + result);
        try {
            JSONObject jsonObject = new JSONObject(result);
            jsonArray = jsonObject.getJSONArray("result");
            for(int i = 0 ; i < jsonArray.length(); i++){
                storeToCloud(jsonArray.getJSONObject(i), i);
                jobFinished(params, false);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateResult(String status) {

    }

    private void sendNotification(final String farmer_id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("driver_id", SharedPreferenceManager.getUserId(getApplicationContext())));
                apiDataObjectArrayList.add(new ApiDataObject("notification", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("farmer_id", farmer_id));
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
                                Log.d("jsonObject", "jsonObject: 1" + jsonObjectLoginResponse);
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

    @Override
    public void deleteResult(String status) {

    }

    private void storeToCloud(JSONObject jsonObject, int position){
        apiDataObjectArrayList = new ArrayList<>();

        try {
            apiDataObjectArrayList.add(new ApiDataObject("ro_id", jsonObject.getString("ro_id")));
            apiDataObjectArrayList.add(new ApiDataObject("driver_id", SharedPreferenceManager.getUserId(this)));
            apiDataObjectArrayList.add(new ApiDataObject("farmer_id", jsonObject.getString("farmer_id")));
            apiDataObjectArrayList.add(new ApiDataObject("weight", jsonObject.getString("weight")));
            apiDataObjectArrayList.add(new ApiDataObject("product_id", jsonObject.getString("product_id")));
            apiDataObjectArrayList.add(new ApiDataObject("price", jsonObject.getString("price")));
            apiDataObjectArrayList.add(new ApiDataObject("quantity", jsonObject.getString("quantity")));
            apiDataObjectArrayList.add(new ApiDataObject("type", jsonObject.getString("type")));
            apiDataObjectArrayList.add(new ApiDataObject("grade", jsonObject.getString("grade")));
            apiDataObjectArrayList.add(new ApiDataObject("date", jsonObject.getString("created_at")));

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
                        deleteRecordAfterUpload(jsonObject.getString("id"));
                        if(position == jsonArray.length()-1) sendNotification(jsonObject.getString("farmer_id"));
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

    private void deleteRecordAfterUpload(String id){
        frameworkClass.new Delete().where("id = ?", id).perform();
    }
}
