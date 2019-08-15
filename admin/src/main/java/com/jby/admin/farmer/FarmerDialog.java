package com.jby.admin.farmer;

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
import com.jby.admin.adapter.FarmerAdapter;
import com.jby.admin.database.CustomSqliteHelper;
import com.jby.admin.database.FrameworkClass;
import com.jby.admin.database.ResultCallBack;
import com.jby.admin.object.entity.FarmerObject;
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

import static com.jby.admin.database.CustomSqliteHelper.TB_FARMER;
import static com.jby.admin.database.CustomSqliteHelper.TB_FAVOURITE_FARMER;
import static com.jby.admin.shareObject.CustomToast.CustomToast;


public class FarmerDialog extends DialogFragment implements SearchView.OnQueryTextListener, AdapterView.OnItemClickListener, ResultCallBack {
    View rootView;
    private SearchView farmerDialogSearch;
    private ExpandableHeightListView farmerList;
    private ArrayList<FarmerObject> farmerObjectArrayList;
    private FarmerAdapter farmerAdapter;
    private boolean isFarmerList = true;
    //favourite list
    private TextView farmDialogLabelFavouriteFarmer;
    private ExpandableHeightListView favouriteFarmerList;
    private ArrayList<FarmerObject> favouriteFarmerArrayList;
    private FarmerAdapter favouriteFarmerAdapter;
    //not found layout
    private RelativeLayout notFoundLayout;
    private ImageView notFoundIcon;
    private TextView notFoundLabel;
    //progress bar
    private ProgressBar progressBar;

    private FrameworkClass tbFavouriteFarmer, tbFarmer;

    private Handler handler;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;

    public FarmerDialogCallBack farmerDialogCallBack;

    public FarmerDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.farmer_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        //not found layout
        notFoundLayout = rootView.findViewById(R.id.not_found_layout);
        notFoundIcon = rootView.findViewById(R.id.not_found_layout_icon);
        notFoundLabel = rootView.findViewById(R.id.not_found_layout_label);

        farmerDialogSearch = rootView.findViewById(R.id.farmer_dialog_search);
        farmerList = rootView.findViewById(R.id.farmer_dialog_farmer_list);

        progressBar = rootView.findViewById(R.id.progress_bar);

        farmDialogLabelFavouriteFarmer = rootView.findViewById(R.id.farmer_dialog_label_recent_choose);
        favouriteFarmerList = rootView.findViewById(R.id.farmer_dialog_favourite_farmer_list);

        favouriteFarmerArrayList = new ArrayList<>();
        farmerObjectArrayList = new ArrayList<>();

        farmerAdapter = new FarmerAdapter(getActivity(), farmerObjectArrayList);
        favouriteFarmerAdapter = new FarmerAdapter(getActivity(), favouriteFarmerArrayList);

        handler = new Handler();
        farmerDialogCallBack = (FarmerDialogCallBack) getParentFragment();
    }

    private void objectSetting() {
        farmerList.setAdapter(farmerAdapter);
        farmerList.setExpanded(true);

        favouriteFarmerList.setAdapter(favouriteFarmerAdapter);
        favouriteFarmerList.setExpanded(true);

        farmerDialogSearch.setOnQueryTextListener(this);

        farmerList.setOnItemClickListener(this);
        favouriteFarmerList.setOnItemClickListener(this);
        setupNotFoundLayout();

        tbFavouriteFarmer = new FrameworkClass(getActivity(), this, new CustomSqliteHelper(getActivity()), TB_FAVOURITE_FARMER);
        tbFarmer = new FrameworkClass(getActivity(), this, new CustomSqliteHelper(getActivity()), TB_FARMER);

        checkingNetwork();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                readFavouriteFarmerFromLocal();

            }
        }, 50);
    }

    private void checkingNetwork(){
        if (new NetworkConnection(getActivity()).checkNetworkConnection()) {
            fetchAllFarmer();
        }
        else{
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

    /*------------------------------------------------------------farmer list purpose---------------------------------------------------------------------------*/
    private void fetchAllFarmer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("read", "1"));

                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().farmer,
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
                                            tbFarmer.new Delete().perform();
                                            JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("farmer");
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                tbFarmer.new create("farmer_id, name, phone, address, created_at",
                                                        new String[]{
                                                                jsonArray.getJSONObject(i).getString("id"),
                                                                jsonArray.getJSONObject(i).getString("name"),
                                                                jsonArray.getJSONObject(i).getString("phone"),
                                                                jsonArray.getJSONObject(i).getString("address"),
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
        tbFarmer.new Read("*")
                .where("name LIKE '%" + query + "%' OR address LIKE '%" + query + "%'")
                .perform();
    }

    private void setUpFarmerList(String result) {
        try {
            JSONArray jsonArray = new JSONObject(result).getJSONArray("result");
            if (jsonArray.length() > 0)
                for (int i = 0; i < jsonArray.length(); i++) {
                    farmerObjectArrayList.add(new FarmerObject(
                            jsonArray.getJSONObject(i).getString("farmer_id"),
                            jsonArray.getJSONObject(i).getString("name"),
                            jsonArray.getJSONObject(i).getString("phone"),
                            jsonArray.getJSONObject(i).getString("address")));
                }
            showProgressBar(false);
            farmerAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        showNotFoundLayout();
    }

    /*----------------------------------------------------------------------favourite setting----------------------------------------------------------------------*/
    private void readFavouriteFarmerFromLocal() {
        //read product list after stored
        isFarmerList = false;
        tbFavouriteFarmer.new Read("*").orderByDesc("farmer_id").perform();
    }

    private void storeFavouriteList(FarmerObject farmerObject) {
        tbFavouriteFarmer.new create("farmer_id, name, phone, address, created_at", new String[]{
                farmerObject.getId(),
                farmerObject.getName(),
                farmerObject.getPhone(),
                farmerObject.getAddress(),
                (String) android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date())
        }).perform();
    }

    private void setUpFavouriteList(String result) {
        int count = 0;
        try {
            JSONArray jsonArray = (new JSONObject(result).getJSONArray("result"));
            for (int i = 0; i < jsonArray.length(); i++) {
                if (favouriteFarmerArrayList.size() < 3) {
                    //add item into favouriteFarmerArrayList when size = 0
                    if (favouriteFarmerArrayList.size() <= 0) {
                        favouriteFarmerArrayList.add(new FarmerObject(
                                jsonArray.getJSONObject(i).getString("farmer_id"),
                                jsonArray.getJSONObject(i).getString("name"),
                                jsonArray.getJSONObject(i).getString("phone"),
                                jsonArray.getJSONObject(i).getString("address")
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
                            favouriteFarmerArrayList.add(new FarmerObject(
                                    jsonArray.getJSONObject(i).getString("farmer_id"),
                                    jsonArray.getJSONObject(i).getString("name"),
                                    jsonArray.getJSONObject(i).getString("phone"),
                                    jsonArray.getJSONObject(i).getString("address")
                            ));
                        count = 0;
                    }
                } else break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
        isFarmerList = true;
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
        farmerObjectArrayList.clear();
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
        notFoundLabel.setText("No farmer is found from local!");
    }

    private void showNotFoundLayout() {
        notFoundLayout.setVisibility(farmerObjectArrayList.size() > 0 ? View.GONE : View.VISIBLE);
    }

    private void notifyDataSetChanged() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                favouriteFarmerAdapter.notifyDataSetChanged();
                farmerAdapter.notifyDataSetChanged();
            }
        });
    }

    private void hide(boolean hide) {
        favouriteFarmerList.setVisibility(hide ? View.GONE : View.VISIBLE);
        farmDialogLabelFavouriteFarmer.setVisibility(hide ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.farmer_dialog_favourite_farmer_list:
                farmerDialogCallBack.selectedFarmer(favouriteFarmerArrayList.get(i));
                storeFavouriteList(favouriteFarmerArrayList.get(i));

                break;
            case R.id.farmer_dialog_farmer_list:
                farmerDialogCallBack.selectedFarmer(farmerObjectArrayList.get(i));
                storeFavouriteList(farmerObjectArrayList.get(i));
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
         * for farmer favourite list purpose
         * */
        if (!isFarmerList) {
            setUpFavouriteList(result);
        }
        /*
         * for offline farmer list purpose
         * */
        else {
            setUpFarmerList(result);
        }
    }

    @Override
    public void updateResult(String status) {

    }

    @Override
    public void deleteResult(String status) {

    }

    public interface FarmerDialogCallBack {
        void selectedFarmer(FarmerObject farmerObject);
    }
}
