package com.jby.vegeapp.pickUp.farmer;

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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.vegeapp.R;
import com.jby.vegeapp.adapter.FarmerAdapter;
import com.jby.vegeapp.database.CustomSqliteHelper;
import com.jby.vegeapp.database.FrameworkClass;
import com.jby.vegeapp.database.ResultCallBack;
import com.jby.vegeapp.object.FarmerObject;
import com.jby.vegeapp.object.product.ProductObject;
import com.jby.vegeapp.others.ExpandableHeightListView;
import com.jby.vegeapp.others.NetworkConnection;
import com.jby.vegeapp.others.SwipeDismissTouchListener;
import com.jby.vegeapp.shareObject.ApiDataObject;
import com.jby.vegeapp.shareObject.ApiManager;
import com.jby.vegeapp.shareObject.AsyncTaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.vegeapp.database.CustomSqliteHelper.TB_BASKET_FAVOURITE_FARMER;
import static com.jby.vegeapp.database.CustomSqliteHelper.TB_FARMER;
import static com.jby.vegeapp.database.CustomSqliteHelper.TB_PICK_UP_FAVOURITE_FARMER;


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

    private FrameworkClass frameworkClass, tbFarmerSql;

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

        farmDialogLabelFavouriteFarmer = rootView.findViewById(R.id.farmer_dialog_label_recent_choose);
        favouriteFarmerList = rootView.findViewById(R.id.farmer_dialog_favourite_farmer_list);

        favouriteFarmerArrayList = new ArrayList<>();
        farmerObjectArrayList = new ArrayList<>();

        farmerAdapter = new FarmerAdapter(getActivity(), farmerObjectArrayList);
        favouriteFarmerAdapter = new FarmerAdapter(getActivity(), favouriteFarmerArrayList);

        handler = new Handler();
        farmerDialogCallBack = (FarmerDialogCallBack) (getActivity() != null ? getActivity() : getTargetFragment());
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

        Bundle bundle = getArguments();
        //check whether open from pick up activity or basket activity
        if (bundle != null)
            frameworkClass = new FrameworkClass(getActivity(), this, new CustomSqliteHelper(getActivity()), TB_BASKET_FAVOURITE_FARMER);
        else
            frameworkClass = new FrameworkClass(getActivity(), this, new CustomSqliteHelper(getActivity()), TB_PICK_UP_FAVOURITE_FARMER);

        tbFarmerSql = new FrameworkClass(getActivity(), this, new CustomSqliteHelper(getActivity()), TB_FARMER);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (new NetworkConnection(getActivity()).checkNetworkConnection()) fetchAllFarmer();
                else offlineMode();

                isFarmerList = false;
                frameworkClass.new Read("*").orderByDesc("id").perform();
            }
        }, 200);
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
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("fetch", "1"));

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
                    Log.d("jsonObject", "jsonObject: " + jsonObjectLoginResponse);
                    if (jsonObjectLoginResponse.getString("status").equals("1")) {
                        JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("farmer");
                        //date
                        String created_at = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));
                        //delete all product before store new product
                        tbFarmerSql.new Delete().perform();
                        //store farmer into product table
                        for (int i = 0; i < jsonArray.length(); i++) {
//                            tbFarmerSql.new create("id, name, phone, address, created_at",
//                                    jsonArray.getJSONObject(i).getString("id") + "," +
//                                            jsonArray.getJSONObject(i).getString("name") + "," +
//                                            jsonArray.getJSONObject(i).getString("phone") + "," +
//                                            "address" + "," +
//                                            created_at
//                            ).perform();
                            tbFarmerSql.new create("id, name, phone, address, created_at",
                                    new String[]{
                                    jsonArray.getJSONObject(i).getString("id"),
                                    jsonArray.getJSONObject(i).getString("name"),
                                    jsonArray.getJSONObject(i).getString("phone"),
                                    jsonArray.getJSONObject(i).getString("address"),
                                    created_at
                            }).perform();
                        }
                        //read farmer list after stored
                        offlineMode();
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
        farmerAdapter.notifyDataSetChanged();
    }

    private void offlineMode() {
        //read product list after stored
        tbFarmerSql.new Read("*").perform();
    }

    private void setUpFarmerList(String result) {
        try {
            JSONArray jsonArray = new JSONObject(result).getJSONArray("result");
            if (jsonArray.length() > 0)
                for (int i = 0; i < jsonArray.length(); i++) {
                    farmerObjectArrayList.add(new FarmerObject(
                            jsonArray.getJSONObject(i).getString("id"),
                            jsonArray.getJSONObject(i).getString("name"),
                            jsonArray.getJSONObject(i).getString("phone"),
                            jsonArray.getJSONObject(i).getString("address")));
                }
            farmerAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
            notFoundLayout.setVisibility(View.VISIBLE);
        }
    }

    private void hide(boolean hide) {
        if (hide) {
            favouriteFarmerList.setVisibility(View.GONE);
            farmDialogLabelFavouriteFarmer.setVisibility(View.GONE);
        } else {
            favouriteFarmerList.setVisibility(View.VISIBLE);
            farmDialogLabelFavouriteFarmer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (adapterView.getId()) {
            case R.id.farmer_dialog_favourite_farmer_list:
                Log.d("haha","farmer detail :id: " + favouriteFarmerArrayList.get(i).getId());
                farmerDialogCallBack.selectedItem(favouriteFarmerArrayList.get(i).getName(), favouriteFarmerArrayList.get(i).getId(), favouriteFarmerArrayList.get(i).getAddress(), favouriteFarmerArrayList.get(i).getPhone());

                break;
            case R.id.farmer_dialog_farmer_list:
                Log.d("haha","farmer detail :id: " + farmerObjectArrayList.get(i).getId());
                farmerDialogCallBack.selectedItem(farmerObjectArrayList.get(i).getName(), farmerObjectArrayList.get(i).getId(), farmerObjectArrayList.get(i).getAddress(), farmerObjectArrayList.get(i).getPhone());
                break;
        }
        dismiss();
    }

    private void setupNotFoundLayout() {
        notFoundIcon.setImageDrawable(getResources().getDrawable(R.drawable.no_item_to_deliver_icon));
        notFoundLabel.setText("No farmer is found from local!");
    }

    /*--------------------------------------------------------------------search purpose-------------------------------------------------------------------*/
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
        ArrayList<FarmerObject> searchList = new ArrayList<>();
        for (int i = 0; i < farmerObjectArrayList.size(); i++) {
            if (farmerObjectArrayList.get(i).getName().contains(query)) {
                searchList.add(farmerObjectArrayList.get(i));
            }
        }
        farmerAdapter = new FarmerAdapter(getActivity(), searchList);
        farmerList.setAdapter(farmerAdapter);
        farmerAdapter.notifyDataSetChanged();
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
            Log.d("haha","haha: favourite: " + result);
            JSONObject jsonObject;
            int count = 0;
            try {
                jsonObject = new JSONObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray("result");

                for (int i = 0; i < jsonArray.length(); i++) {
                    if (favouriteFarmerArrayList.size() < 3) {
                        Log.d("haha", "haha: big loop");
                        //add item into favouriteFarmerArrayList when size = 0
                        if (favouriteFarmerArrayList.size() <= 0) {
                            favouriteFarmerArrayList.add(new FarmerObject(
                                    jsonArray.getJSONObject(i).getString("farmer_id"),
                                    jsonArray.getJSONObject(i).getString("name"),
                                    "",
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
                                        "",
                                        jsonArray.getJSONObject(i).getString("address")
                                ));
                            count = 0;
                        }
                    } else break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            favouriteFarmerAdapter.notifyDataSetChanged();
        }
        /*
         * for offline farmer list purpose
         * */
        else setUpFarmerList(result);
    }

    @Override
    public void updateResult(String status) {

    }

    @Override
    public void deleteResult(String status) {

    }

    public interface FarmerDialogCallBack {
        void selectedItem(String name, String id, String address, String phone);
    }
}
