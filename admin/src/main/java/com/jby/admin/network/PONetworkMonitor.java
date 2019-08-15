package com.jby.admin.network;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import com.jby.admin.database.CustomSqliteHelper;
import com.jby.admin.database.FrameworkClass;
import com.jby.admin.database.ResultCallBack;
import com.jby.admin.shareObject.ApiDataObject;
import com.jby.admin.shareObject.ApiManager;
import com.jby.admin.shareObject.AsyncTaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.admin.database.CustomSqliteHelper.TB_PURCHASE_ORDER;
import static com.jby.admin.database.CustomSqliteHelper.TB_STOCK_IN;
import static com.jby.admin.shareObject.CustomToast.CustomToast;


public class PONetworkMonitor extends JobService implements ResultCallBack {

    public static final String TAG = "PurchaseFragment";
    private boolean jobCancelled = false;
    private JobParameters params;

    private String cloudPoId;
    private FrameworkClass tbPurchaseOrder, tbStockIn;
    private boolean readingLocalPo = true;

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
        tbPurchaseOrder = new FrameworkClass(getApplicationContext(), this, new CustomSqliteHelper(getApplicationContext()), TB_PURCHASE_ORDER);
        tbStockIn = new FrameworkClass(getApplicationContext(), this, new CustomSqliteHelper(getApplicationContext()), TB_STOCK_IN);

        new Thread(new Runnable() {
            @Override
            public void run() {
                tbPurchaseOrder.new Read("po_id, driver_id, farmer_id, date").perform();
            }
        }).start();
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job cancelled before completion");
        jobCancelled = true;
        return true;
    }

    /*----------------------------------------------------------------setup data-------------------------------------------------------------------------*/
    private void gettingLocalDo(String result) {
        try {
            JSONArray jsonArray = new JSONObject(result).getJSONArray("result");
            if (jsonArray.length() > 0)
                for (int i = 0; i < jsonArray.length(); i++) {
                    uploadPo(jsonArray.getJSONObject(i));
                }
            /*
             * read po item
             * */
            readingLocalPo = false;
            tbStockIn.new Read("stock_in_id, po_id, local_po_id, farmer_id, product_id, grade, location, weight, quantity, price, created_at").perform();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void uploadPo(final JSONObject jsonObject) {
        try {
            apiDataObjectArrayList = new ArrayList<>();
            apiDataObjectArrayList.add(new ApiDataObject("create", "1"));
            apiDataObjectArrayList.add(new ApiDataObject("driver_id", jsonObject.getString("driver_id")));
            apiDataObjectArrayList.add(new ApiDataObject("farmer_id", jsonObject.getString("farmer_id")));
            apiDataObjectArrayList.add(new ApiDataObject("date", jsonObject.getString("date")));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        asyncTaskManager = new AsyncTaskManager(
                getApplicationContext(),
                new ApiManager().purchase,
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
                    Log.d("jsonObject", "po_id here: " + jsonObjectLoginResponse);
                    try {
                        if (jsonObjectLoginResponse.getString("status").equals("1")) {
                            cloudPoId = jsonObjectLoginResponse.getString("po_id");
                            /*
                             * update po_id
                             * */
                            tbStockIn.new Update("po_id", cloudPoId).where("local_po_id = ?", jsonObject.getString("po_id")).perform();
                            /*
                             * delete local do record
                             * */
                            tbPurchaseOrder.new Delete().where("po_id = ?", jsonObject.getString("po_id")).perform();
                        } else {
                            CustomToast(getApplicationContext(), "Something Went Wrong!");
                        }

                    } catch (JSONException e) {
                        CustomToast(getApplicationContext(), "JSON Exception!");
                        e.printStackTrace();
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
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
            } catch (TimeoutException e) {
                CustomToast(getApplicationContext(), "Connection Time Out!");
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    private void gettingLocalDoItem(String result) {
        try {
            JSONArray jsonArray = new JSONObject(result).getJSONArray("result");
            Log.d("haha", "po child item: " + jsonArray);
            if (jsonArray.length() > 0)
                for (int i = 0; i < jsonArray.length(); i++) {
                    uploadPoItem(jsonArray.getJSONObject(i));
                }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void uploadPoItem(final JSONObject jsonObject) {
        try {
            apiDataObjectArrayList = new ArrayList<>();
            apiDataObjectArrayList.add(new ApiDataObject("create", "1"));
            apiDataObjectArrayList.add(new ApiDataObject("po_id", jsonObject.getString("po_id")));
            apiDataObjectArrayList.add(new ApiDataObject("product_id", jsonObject.getString("product_id")));
            apiDataObjectArrayList.add(new ApiDataObject("grade", jsonObject.getString("grade").equals("") ? "unknown" : jsonObject.getString("grade")));
            apiDataObjectArrayList.add(new ApiDataObject("location", jsonObject.getString("location").equals("") ? "unknown" : jsonObject.getString("location")));
            apiDataObjectArrayList.add(new ApiDataObject("weight", jsonObject.getString("weight")));
            apiDataObjectArrayList.add(new ApiDataObject("quantity", jsonObject.getString("quantity")));
            apiDataObjectArrayList.add(new ApiDataObject("price", jsonObject.getString("price").equals("") ? "0" : jsonObject.getString("price")));
            apiDataObjectArrayList.add(new ApiDataObject("farmer_id", jsonObject.getString("farmer_id")));
            apiDataObjectArrayList.add(new ApiDataObject("created_at", jsonObject.getString("created_at")));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        asyncTaskManager = new AsyncTaskManager(
                getApplicationContext(),
                new ApiManager().purchase,
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
                    Log.d("jsonObject", "po item here: " + jsonObjectLoginResponse);
                    try {
                        if (jsonObjectLoginResponse.getString("status").equals("1")) {
                            /*
                             * delete po item after upload
                             * */
                            tbStockIn.new Delete().where("stock_in_id = ?", jsonObject.getString("stock_in_id")).perform();
                        } else {
                            CustomToast(getApplicationContext(), "Something Went Wrong!");
                        }

                    } catch (JSONException e) {
                        CustomToast(getApplicationContext(), "JSON Exception!");
                        e.printStackTrace();
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
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
            } catch (TimeoutException e) {
                CustomToast(getApplicationContext(), "Connection Time Out!");
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }


    /*-----------------------------------------------------------------framework call back--------------------------------------------------------------*/
    @Override
    public void createResult(String status) {

    }

    @Override
    public void readResult(String result) {
        if (readingLocalPo) {
            gettingLocalDo(result);
        } else {
            gettingLocalDoItem(result);
        }
    }

    @Override
    public void updateResult(String status) {

    }

    @Override
    public void deleteResult(String status) {

    }
}
