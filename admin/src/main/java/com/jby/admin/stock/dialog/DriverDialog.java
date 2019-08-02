package com.jby.admin.stock.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.admin.R;
import com.jby.admin.adapter.DriverAdapter;
import com.jby.admin.database.CustomSqliteHelper;
import com.jby.admin.database.FrameworkClass;
import com.jby.admin.database.ResultCallBack;
import com.jby.admin.object.DriverObject;
import com.jby.admin.others.ExpandableHeightListView;
import com.jby.admin.others.SwipeDismissTouchListener;
import com.jby.admin.shareObject.ApiDataObject;
import com.jby.admin.shareObject.ApiManager;
import com.jby.admin.shareObject.AsyncTaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.admin.database.CustomSqliteHelper.TB_FAVOURITE_DRIVER;


public class DriverDialog extends DialogFragment implements SearchView.OnQueryTextListener, AdapterView.OnItemClickListener,
        ResultCallBack {
    View rootView;
    private SearchView driverDialogSearch;
    private ExpandableHeightListView driverList;
    private ArrayList<DriverObject> driverObjectArrayList;
    private DriverAdapter driverAdapter;
    private String customerID = "";
    //favourite list
    private TextView drivDialogLabelFavouriteFarmer;
    private ExpandableHeightListView favouriteFarmerList;
    private ArrayList<DriverObject> favouriteFarmerArrayList;
    private DriverAdapter favouriteFarmerAdapter;

    private FrameworkClass frameworkClass;

    private Handler handler;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;

    public DriverDialogCallBack driverDialogCallBack;

    public DriverDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.driver_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        driverDialogSearch = rootView.findViewById(R.id.driver_dialog_search);
        driverList = rootView.findViewById(R.id.driver_dialog_driver_list);

        drivDialogLabelFavouriteFarmer = rootView.findViewById(R.id.driver_dialog_label_recent_choose);
        favouriteFarmerList = rootView.findViewById(R.id.driver_dialog_favourite_driver_list);

        favouriteFarmerArrayList = new ArrayList<>();
        driverObjectArrayList = new ArrayList<>();

        driverAdapter = new DriverAdapter(getActivity(), driverObjectArrayList);
        favouriteFarmerAdapter = new DriverAdapter(getActivity(), favouriteFarmerArrayList);

        handler = new Handler();
        driverDialogCallBack = (DriverDialogCallBack) getParentFragment();
    }

    private void objectSetting() {
        driverList.setAdapter(driverAdapter);
        driverList.setExpanded(true);

        favouriteFarmerList.setAdapter(favouriteFarmerAdapter);
        favouriteFarmerList.setExpanded(true);

        driverDialogSearch.setOnQueryTextListener(this);

        driverList.setOnItemClickListener(this);
        favouriteFarmerList.setOnItemClickListener(this);

        frameworkClass = new FrameworkClass(getActivity(), this, new CustomSqliteHelper(getActivity()), TB_FAVOURITE_DRIVER);
        final Bundle bundle = getArguments();
        if (bundle != null) {
            customerID = bundle.getString("customer_id");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    frameworkClass.new Read("*")
                            .where("customer_id = " + customerID)
                            .orderByDesc("id")
                            .perform();
                }
            }, 200);
        }
        fetchDriver();
    }

    private void fetchDriver() {
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("fetch", "1"));

        asyncTaskManager = new AsyncTaskManager(
                getActivity(),
                new ApiManager().driver,
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
                    Log.d("jsonObject", "jsonObject: haha " + jsonObjectLoginResponse);
                    if (jsonObjectLoginResponse.getString("status").equals("1")) {
                        JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("driver");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            driverObjectArrayList.add(new DriverObject(
                                    jsonArray.getJSONObject(i).getString("id"),
                                    jsonArray.getJSONObject(i).getString("name"),
                                    jsonArray.getJSONObject(i).getString("phone")));
                        }
                    }
                } else {
                    Toast.makeText(getActivity(), "Network Error!", Toast.LENGTH_SHORT).show();
                }

            } catch (InterruptedException e) {
                Toast.makeText(getActivity(), "Interrupted Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (ExecutionException e) {
                Toast.makeText(getActivity(), "Execution Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (JSONException e) {
                Toast.makeText(getActivity(), "JSON Exception!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (TimeoutException e) {
                Toast.makeText(getActivity(), "Connection Time Out!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        driverAdapter.notifyDataSetChanged();
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
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        //hide favourite list
        if (query.length() > 0) hide(true);
        else hide(false);

        searchFromArrayList(query);
        return false;
    }

    private void searchFromArrayList(String query) {
        ArrayList<DriverObject> searchList = new ArrayList<>();
        for (int i = 0; i < driverObjectArrayList.size(); i++) {
            if (driverObjectArrayList.get(i).getName().contains(query)) {
                searchList.add(driverObjectArrayList.get(i));
            }
        }
        driverAdapter = new DriverAdapter(getActivity(), searchList);
        driverList.setAdapter(driverAdapter);
        driverAdapter.notifyDataSetChanged();
    }

    private void hide(boolean hide) {
        if (hide) {
            favouriteFarmerList.setVisibility(View.GONE);
            drivDialogLabelFavouriteFarmer.setVisibility(View.GONE);
        } else {
            favouriteFarmerList.setVisibility(View.VISIBLE);
            drivDialogLabelFavouriteFarmer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.driver_dialog_favourite_driver_list:
                driverDialogCallBack.selectedItem(favouriteFarmerArrayList.get(i).getName(), favouriteFarmerArrayList.get(i).getId());
                /*
                 * store value into favourite list
                 * */
                frameworkClass.new create("driver_id, name, customer_id",
                        favouriteFarmerArrayList.get(i).getId() + "," +
                                favouriteFarmerArrayList.get(i).getName() + "," +
                                customerID).perform();
                break;
            case R.id.driver_dialog_driver_list:
                driverDialogCallBack.selectedItem(driverObjectArrayList.get(i).getName(), driverObjectArrayList.get(i).getId());
                /*
                 * store value into favourite list
                 * */
                frameworkClass.new create("driver_id, name, customer_id",
                        driverObjectArrayList.get(i).getId() + "," +
                                driverObjectArrayList.get(i).getName() + "," +
                                customerID).perform();
                break;
        }
        dismiss();
    }

    @Override
    public void createResult(String status) {

    }

    @Override
    public void readResult(String result) {
        Log.d("haha", "haha: " + result);
        JSONObject jsonObject = null;
        int count = 0;
        try {
            jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("result");

            for (int i = 0; i < jsonArray.length(); i++) {
                if (favouriteFarmerArrayList.size() < 3) {
                    Log.d("haha", "haha: big loop");
                    //add item into favouriteFarmerArrayList when size = 0
                    if (favouriteFarmerArrayList.size() <= 0) {
                        favouriteFarmerArrayList.add(new DriverObject(
                                jsonArray.getJSONObject(i).getString("driver_id"),
                                jsonArray.getJSONObject(i).getString("name"),
                                ""
                        ));
                    }
                    //favouriteFarmerArrayList.size > 0
                    else {
                        //check repeat values
                        for (int j = 0; j < favouriteFarmerArrayList.size(); j++) {
                            if (!favouriteFarmerArrayList.get(j).getName().equals(jsonArray.getJSONObject(i).getString("name")))
                                count++;
                        }
                        //if count == favourite.size() mean that one is new item
                        if (count == favouriteFarmerArrayList.size())
                            favouriteFarmerArrayList.add(new DriverObject(
                                    jsonArray.getJSONObject(i).getString("driver_id"),
                                    jsonArray.getJSONObject(i).getString("name"),
                                    ""
                            ));
                        count = 0;
                    }
                } else break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        favouriteFarmerAdapter.notifyDataSetChanged();
        if (favouriteFarmerArrayList.size() <= 0)
            drivDialogLabelFavouriteFarmer.setVisibility(View.GONE);
    }

    @Override
    public void updateResult(String status) {

    }

    @Override
    public void deleteResult(String status) {

    }

    public interface DriverDialogCallBack {
        void selectedItem(String name, String id);
    }
}
