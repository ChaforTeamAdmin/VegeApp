package com.jby.admin.driver;

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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jby.admin.R;
import com.jby.admin.adapter.DriverAdapter;
import com.jby.admin.database.CustomSqliteHelper;
import com.jby.admin.database.FrameworkClass;
import com.jby.admin.database.ResultCallBack;
import com.jby.admin.object.entity.DriverObject;
import com.jby.admin.others.ExpandableHeightListView;
import com.jby.admin.others.NetworkConnection;
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

import static com.jby.admin.database.CustomSqliteHelper.TB_DRIVER;
import static com.jby.admin.database.CustomSqliteHelper.TB_FAVOURITE_DRIVER;
import static com.jby.admin.shareObject.CustomToast.CustomToast;


public class DriverDialog extends DialogFragment implements SearchView.OnQueryTextListener, AdapterView.OnItemClickListener,
        ResultCallBack {
    View rootView;
    private SearchView driverDialogSearch;
    private ExpandableHeightListView driverList;
    private ArrayList<DriverObject> driverObjectArrayList;
    private DriverAdapter driverAdapter;
    private boolean isDriverList = true;
    //favourite list
    private TextView farmDialogLabelFavouriteDriver;
    private ExpandableHeightListView favouriteDriverList;
    private ArrayList<DriverObject> favouriteDriverArrayList;
    private DriverAdapter favouriteDriverAdapter;
    //not found layout
    private RelativeLayout notFoundLayout;
    private ImageView notFoundIcon;
    private TextView notFoundLabel;
    //progress bar
    private ProgressBar progressBar;

    private FrameworkClass tbFavouriteDriver, tbDriver;

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
        //not found layout
        notFoundLayout = rootView.findViewById(R.id.not_found_layout);
        notFoundIcon = rootView.findViewById(R.id.not_found_layout_icon);
        notFoundLabel = rootView.findViewById(R.id.not_found_layout_label);

        driverDialogSearch = rootView.findViewById(R.id.driver_dialog_search);
        driverList = rootView.findViewById(R.id.driver_dialog_driver_list);

        progressBar = rootView.findViewById(R.id.progress_bar);

        farmDialogLabelFavouriteDriver = rootView.findViewById(R.id.driver_dialog_label_recent_choose);
        favouriteDriverList = rootView.findViewById(R.id.driver_dialog_favourite_driver_list);

        favouriteDriverArrayList = new ArrayList<>();
        driverObjectArrayList = new ArrayList<>();

        driverAdapter = new DriverAdapter(getActivity(), driverObjectArrayList);
        favouriteDriverAdapter = new DriverAdapter(getActivity(), favouriteDriverArrayList);

        handler = new Handler();
        driverDialogCallBack = (DriverDialogCallBack) getParentFragment();
    }

    private void objectSetting() {
        driverList.setAdapter(driverAdapter);
        driverList.setExpanded(true);

        favouriteDriverList.setAdapter(favouriteDriverAdapter);
        favouriteDriverList.setExpanded(true);

        driverDialogSearch.setOnQueryTextListener(this);

        driverList.setOnItemClickListener(this);
        favouriteDriverList.setOnItemClickListener(this);
        setupNotFoundLayout();

        tbFavouriteDriver = new FrameworkClass(getActivity(), this, new CustomSqliteHelper(getActivity()), TB_FAVOURITE_DRIVER);
        tbDriver = new FrameworkClass(getActivity(), this, new CustomSqliteHelper(getActivity()), TB_DRIVER);

        checkingNetwork();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                readFavouriteDriverFromLocal();

            }
        }, 50);
    }

    private void checkingNetwork() {
        if (new NetworkConnection(getActivity()).checkNetworkConnection()) {
            fetchAllDriver();
        } else {
            readDataFromLocal("");
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

    /*------------------------------------------------------------driver list purpose---------------------------------------------------------------------------*/
    private void fetchAllDriver() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("read", "1"));

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
                            Log.d("jsonObject", "product List: " + jsonObjectLoginResponse);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                            tbDriver.new Delete().perform();
                                            JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("driver");
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                tbDriver.new create("driver_id, name, nickname, phone, created_at",
                                                        new String[]{
                                                                jsonArray.getJSONObject(i).getString("id"),
                                                                jsonArray.getJSONObject(i).getString("username"),
                                                                jsonArray.getJSONObject(i).getString("nickname"),
                                                                jsonArray.getJSONObject(i).getString("phone"),
                                                                (String) android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date())
                                                        }).perform();
                                            }
                                            readDataFromLocal("");
                                        }
                                    } catch (JSONException e) {
                                        CustomToast(getActivity(), "JSON Exception!");
                                        e.printStackTrace();
                                    }
                                }
                            });
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
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void readDataFromLocal(String query) {
        //read product list after stored
        tbDriver.new Read("*")
                .where("name LIKE '%" + query + "%'")
                .perform();
    }

    private void setUpDriverList(String result) {
        try {
            JSONArray jsonArray = new JSONObject(result).getJSONArray("result");
            if (jsonArray.length() > 0)
                for (int i = 0; i < jsonArray.length(); i++) {
                    driverObjectArrayList.add(new DriverObject(
                            jsonArray.getJSONObject(i).getString("driver_id"),
                            jsonArray.getJSONObject(i).getString("name"),
                            jsonArray.getJSONObject(i).getString("nickname"),
                            jsonArray.getJSONObject(i).getString("phone")));
                }
            showProgressBar(false);
            driverAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        showNotFoundLayout();
    }

    /*----------------------------------------------------------------------favourite setting----------------------------------------------------------------------*/
    private void readFavouriteDriverFromLocal() {
        //read product list after stored
        isDriverList = false;
        tbFavouriteDriver.new Read("*").orderByDesc("driver_id").perform();
    }

    private void storeFavouriteList(DriverObject driverObject) {
        tbFavouriteDriver.new create("driver_id, name, nickname, phone, created_at", new String[]{
                driverObject.getId(),
                driverObject.getName(),
                driverObject.getNickname(),
                driverObject.getPhone(),
                (String) android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date())
        }).perform();
    }

    private void setUpFavouriteList(String result) {
        int count = 0;
        try {
            JSONArray jsonArray = (new JSONObject(result).getJSONArray("result"));
            for (int i = 0; i < jsonArray.length(); i++) {
                if (favouriteDriverArrayList.size() < 3) {
                    //add item into favouriteDriverArrayList when size = 0
                    if (favouriteDriverArrayList.size() <= 0) {
                        favouriteDriverArrayList.add(new DriverObject(
                                jsonArray.getJSONObject(i).getString("driver_id"),
                                jsonArray.getJSONObject(i).getString("name"),
                                jsonArray.getJSONObject(i).getString("nickname"),
                                jsonArray.getJSONObject(i).getString("phone")
                        ));
                    }
                    //favouriteDriverArrayList.size > 0
                    else {
                        //check repeat values
                        for (int j = 0; j < favouriteDriverArrayList.size(); j++) {
                            if (!favouriteDriverArrayList.get(j).getName().equals(jsonArray.getJSONObject(i).getString("name")))
                                count++;
                        }
                        //if count == favourite.size() mean that one is new item
                        if (count == favouriteDriverArrayList.size())
                            favouriteDriverArrayList.add(new DriverObject(
                                    jsonArray.getJSONObject(i).getString("driver_id"),
                                    jsonArray.getJSONObject(i).getString("name"),
                                    jsonArray.getJSONObject(i).getString("nickname"),
                                    jsonArray.getJSONObject(i).getString("phone")
                            ));
                        count = 0;
                    }
                } else break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
        isDriverList = true;
    }

    /*--------------------------------------------------------------------search purpose-------------------------------------------------------------------*/
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(final String query) {
        showProgressBar(true);
        //hide favourite list
        hide(query.length() > 0);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                reset();
                readDataFromLocal(query);
            }
        }, 150);
        return false;
    }

    /*----------------------------------------------------------------------share setting----------------------------------------------------------------------*/
    private void reset() {
        driverObjectArrayList.clear();
        notifyDataSetChanged();
    }

    private void showProgressBar(final boolean show) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void setupNotFoundLayout() {
        notFoundIcon.setImageDrawable(getResources().getDrawable(R.drawable.not_found));
        notFoundLabel.setText("No driver is found from local!");
    }

    private void showNotFoundLayout() {
        notFoundLayout.setVisibility(driverObjectArrayList.size() > 0 ? View.GONE : View.VISIBLE);
    }

    private void notifyDataSetChanged() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                favouriteDriverAdapter.notifyDataSetChanged();
                driverAdapter.notifyDataSetChanged();
            }
        });
    }

    private void hide(boolean hide) {
        favouriteDriverList.setVisibility(hide ? View.GONE : View.VISIBLE);
        farmDialogLabelFavouriteDriver.setVisibility(hide ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.driver_dialog_favourite_driver_list:
                driverDialogCallBack.selectedDriver(favouriteDriverArrayList.get(i));
                storeFavouriteList(favouriteDriverArrayList.get(i));

                break;
            case R.id.driver_dialog_driver_list:
                driverDialogCallBack.selectedDriver(driverObjectArrayList.get(i));
                storeFavouriteList(driverObjectArrayList.get(i));
                break;
        }
        dismiss();
    }


    /*-----------------------------------------------------------------------------sqlite purpose--------------------------------------------------------------*/
    @Override
    public void createResult(String status) {
    }

    @Override
    public void readResult(String result) {
        /*
         * for driver favourite list purpose
         * */
        if (!isDriverList) {
            setUpFavouriteList(result);
        }
        /*
         * for offline driver list purpose
         * */
        else {
            setUpDriverList(result);
        }
    }

    @Override
    public void updateResult(String status) {

    }

    @Override
    public void deleteResult(String status) {

    }

    public interface DriverDialogCallBack {
        void selectedDriver(DriverObject driverObject);
    }
}
