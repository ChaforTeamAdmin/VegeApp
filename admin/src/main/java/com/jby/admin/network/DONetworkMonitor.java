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

import static com.jby.admin.database.CustomSqliteHelper.TB_DELIVERY_ORDER;
import static com.jby.admin.database.CustomSqliteHelper.TB_STOCK_OUT;
import static com.jby.admin.shareObject.CustomToast.CustomToast;


public class DONetworkMonitor extends JobService implements ResultCallBack {

    public static final String TAG = "DeliveryFragment";
    private boolean jobCancelled = false;
    private JobParameters params;

    private String cloudDoId;
    private FrameworkClass tbDeliveryOrder, tbStockOut;
    private boolean readingLocalDo = true;

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
        tbDeliveryOrder = new FrameworkClass(getApplicationContext(), this, new CustomSqliteHelper(getApplicationContext()), TB_DELIVERY_ORDER);
        tbStockOut = new FrameworkClass(getApplicationContext(), this, new CustomSqliteHelper(getApplicationContext()), TB_STOCK_OUT);

        new Thread(new Runnable() {
            @Override
            public void run() {
                tbDeliveryOrder.new Read("do_id, deliver_driver_id, customer_id, date").perform();
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
                    uploadDo(jsonArray.getJSONObject(i));
                }
            /*
             * read po item
             * */
            readingLocalDo = false;
            tbStockOut.new Read("stock_out_id, do_id, customer_id, local_do_id, product_id, stock_date, grade, location, weight, quantity, price").perform();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void uploadDo(final JSONObject jsonObject) {
        try {
            apiDataObjectArrayList = new ArrayList<>();
            apiDataObjectArrayList.add(new ApiDataObject("create", "1"));
            apiDataObjectArrayList.add(new ApiDataObject("deliver_driver_id", jsonObject.getString("deliver_driver_id")));
            apiDataObjectArrayList.add(new ApiDataObject("customer_id", jsonObject.getString("customer_id")));
            apiDataObjectArrayList.add(new ApiDataObject("date", jsonObject.getString("date")));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        asyncTaskManager = new AsyncTaskManager(
                getApplicationContext(),
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
                    Log.d("jsonObject", "do_id here: " + jsonObjectLoginResponse);
                    try {
                        if (jsonObjectLoginResponse.getString("status").equals("1")) {
                            cloudDoId = jsonObjectLoginResponse.getString("do_id");
                            /*
                             * update do_id
                             * */
                            tbStockOut.new Update("do_id", cloudDoId).where("local_do_id = ?", jsonObject.getString("do_id")).perform();
                            /*
                             * delete local do record
                             * */
                            tbDeliveryOrder.new Delete().where("do_id = ?", jsonObject.getString("do_id")).perform();
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
                    uploadDoItem(jsonArray.getJSONObject(i));
                }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void uploadDoItem(final JSONObject jsonObject) {
        try {
            apiDataObjectArrayList = new ArrayList<>();
            apiDataObjectArrayList.add(new ApiDataObject("create", "1"));
            apiDataObjectArrayList.add(new ApiDataObject("do_id", jsonObject.getString("do_id")));
            apiDataObjectArrayList.add(new ApiDataObject("product_id", jsonObject.getString("product_id")));
            apiDataObjectArrayList.add(new ApiDataObject("stock_date", jsonObject.getString("stock_date")));
            apiDataObjectArrayList.add(new ApiDataObject("grade", jsonObject.getString("grade").equals("") ? "unknown" : jsonObject.getString("grade")));
            apiDataObjectArrayList.add(new ApiDataObject("location", jsonObject.getString("location").equals("") ? "unknown" : jsonObject.getString("location")));
            apiDataObjectArrayList.add(new ApiDataObject("weight", jsonObject.getString("weight")));
            apiDataObjectArrayList.add(new ApiDataObject("quantity", jsonObject.getString("quantity")));
            apiDataObjectArrayList.add(new ApiDataObject("price", jsonObject.getString("price").equals("") ? "0" : jsonObject.getString("price")));
            apiDataObjectArrayList.add(new ApiDataObject("customer_id", jsonObject.getString("customer_id")));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        asyncTaskManager = new AsyncTaskManager(
                getApplicationContext(),
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
                    Log.d("jsonObject", "po item here: " + jsonObjectLoginResponse);
                    try {
                        if (jsonObjectLoginResponse.getString("status").equals("1")) {
                            /*
                             * delete po item after upload
                             * */
                            tbStockOut.new Delete().where("stock_out_id = ?", jsonObject.getString("stock_out_id")).perform();
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
        if (readingLocalDo) {
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
