package com.jby.vegeapp.history.pick_up;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.vegeapp.R;
import com.jby.vegeapp.adapter.history.PickUpHistoryExpandableAdapter;
import com.jby.vegeapp.history.HistoryActivity;
import com.jby.vegeapp.history.HistoryDetailActivity;
import com.jby.vegeapp.object.history.HistoryParentObject;
import com.jby.vegeapp.object.history.PickUpHistoryObject;
import com.jby.vegeapp.others.NetworkConnection;
import com.jby.vegeapp.others.NonScrollExpandableListView;
import com.jby.vegeapp.pickUp.PickUpActivity;
import com.jby.vegeapp.printer.Manager.PrintObject;
import com.jby.vegeapp.printer.Manager.PrintfManager;
import com.jby.vegeapp.printer.PrintfBlueListActivity;
import com.jby.vegeapp.shareObject.ApiDataObject;
import com.jby.vegeapp.shareObject.ApiManager;
import com.jby.vegeapp.shareObject.AsyncTaskManager;
import com.jby.vegeapp.sharePreference.SharedPreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.vegeapp.Utils.VariableUtils.PROCEED_TO_PRINT;
import static com.jby.vegeapp.shareObject.CustomToast.CustomToast;
import static com.jby.vegeapp.Utils.VariableUtils.UPDATE_LIST;


public class PickUpHistory extends Fragment implements ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener {
    View rootView;
    //not found layout
    private RelativeLayout notFoundLayout;
    private ImageView notFoundIcon;
    private TextView notFoundLabel;

    NonScrollExpandableListView pickUpHistoryExpandableAdapterListView;
    ArrayList<HistoryParentObject> historyParentObjectArrayList;
    PickUpHistoryExpandableAdapter pickUpHistoryExpandableAdapterExpandableAdapter;
    private int groupPosition = 0;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private Handler handler;

    public PickUpHistory() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_pick_up_history, container, false);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        //not found layout
        notFoundLayout = rootView.findViewById(R.id.not_found_layout);
        notFoundIcon = rootView.findViewById(R.id.not_found_layout_icon);
        notFoundLabel = rootView.findViewById(R.id.not_found_layout_label);

        pickUpHistoryExpandableAdapterListView = rootView.findViewById(R.id.fragment_pick_up_history_list_view);
        historyParentObjectArrayList = new ArrayList<>();
        pickUpHistoryExpandableAdapterExpandableAdapter = new PickUpHistoryExpandableAdapter(getActivity(), historyParentObjectArrayList);
        handler = new Handler();
    }

    private void objectSetting() {
        pickUpHistoryExpandableAdapterListView.setAdapter(pickUpHistoryExpandableAdapterExpandableAdapter);
        pickUpHistoryExpandableAdapterListView.setOnGroupClickListener(this);
        pickUpHistoryExpandableAdapterListView.setOnChildClickListener(this);
        setupNotFoundLayout();
        checkInternetConnection(null);
    }

    private void showFoundLayout() {
        notFoundLayout.setVisibility(historyParentObjectArrayList.size() <= 0 ? View.VISIBLE : View.GONE);
    }

    private void setupNotFoundLayout() {
        notFoundIcon.setImageDrawable(getResources().getDrawable(R.drawable.no_item_to_deliver_icon));
        notFoundLabel.setText("No record is found!");
    }

    public void checkInternetConnection(View view) {
        boolean networkConnection = new NetworkConnection(getActivity()).checkNetworkConnection();
        showProgressBar(networkConnection);
        pickUpHistoryExpandableAdapterListView.setVisibility(networkConnection ? View.VISIBLE : View.GONE);

        if (networkConnection) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fetchParentItem();
                }
            }, 400);
        }
    }

    /*----------------------------------------------------------list view parent--------------------------------------------------------*/
    private void fetchParentItem() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("pick_up_driver_id", SharedPreferenceManager.getUserId(getActivity())));

                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().pick_up_history,
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
                                JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("pick_up_history_date");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    historyParentObjectArrayList.add(new HistoryParentObject(jsonArray.getJSONObject(i).getString("created_date")));
                                }
                            }
                        } else {
                            CustomToast(getActivity(), "Network Error!");
                        }
                    } catch (InterruptedException e) {
                        CustomToast(getActivity(), "Interrupted Exception!");
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        CustomToast(getActivity(), "Execution Exception!");
                        e.printStackTrace();
                    } catch (JSONException e) {
                        CustomToast(getActivity(), "JSON Exception!");
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        CustomToast(getActivity(), "Connection Time Out!");
                        e.printStackTrace();
                    }
                }
                setVisibility();
                preOpenChild(groupPosition);

            }
        }).start();
    }

    @Override
    public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
        groupPosition = i;
        if (expandableListView.isGroupExpanded(i)) expandableListView.collapseGroup(i);
        else {
            //close view
            closeOtherChildView(i);
            historyParentObjectArrayList.get(i).getPickUpHistoryObjectArrayList().clear();
            fetchChildItem(i);
        }
        return true;
    }

    /*--------------------------------------------------------------list view child----------------------------------------------------------*/
    private void fetchChildItem(final int position) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("date", historyParentObjectArrayList.get(position).getDate()));
                apiDataObjectArrayList.add(new ApiDataObject("pick_up_driver_id", SharedPreferenceManager.getUserId(getActivity())));
                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().pick_up_history,
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
                                JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("pick_up_history");
                                setChildValue(jsonArray, position);
                            }
                        } else {
                            CustomToast(getActivity(), "Network Error!");
                        }
                    } catch (InterruptedException e) {
                        CustomToast(getActivity(), "Interrupted Exception!");
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        CustomToast(getActivity(), "Execution Exception!");
                        e.printStackTrace();
                    } catch (JSONException e) {
                        CustomToast(getActivity(), "JSON Exception!");
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        CustomToast(getActivity(), "Connection Time Out!");
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void setChildValue(JSONArray jsonArray, final int position) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                historyParentObjectArrayList.get(position).setPickUpHistoryObjectArrayList(setChildObject(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pickUpHistoryExpandableAdapterListView.expandGroup(position);
                pickUpHistoryExpandableAdapterListView.setSelectedGroup(position);
                notifyDataSetChanged();
            }
        });
    }

    private PickUpHistoryObject setChildObject(JSONObject jsonObject) {
        PickUpHistoryObject object = null;
        try {
            object = new PickUpHistoryObject(
                    jsonObject.getString("ro_id"),
                    jsonObject.getString("id"),
                    jsonObject.getString("farmer_id"),
                    jsonObject.getString("farmer"),
                    jsonObject.getString("quantity"),
                    jsonObject.getString("created_time"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    /*------------------------------------------------------list view onclick-------------------------------------------------------------*/
    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
        Bundle bundle = new Bundle();
        bundle.putString("ro_id", historyParentObjectArrayList.get(i).getPickUpHistoryObjectArrayList().get(i1).getRo_id());
        bundle.putString("farmer_id", historyParentObjectArrayList.get(i).getPickUpHistoryObjectArrayList().get(i1).getFarmer_id());
        bundle.putString("date", historyParentObjectArrayList.get(i).getDate());
        bundle.putString("time", historyParentObjectArrayList.get(i).getPickUpHistoryObjectArrayList().get(i1).getCreated_time());
        startActivityForResult(new Intent(getActivity(), HistoryDetailActivity.class).putExtras(bundle), UPDATE_LIST);
        return true;
    }

    /*---------------------------------------------------------list view other setting---------------------------------------------------*/
    private void preOpenChild(final int groupPosition) {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (historyParentObjectArrayList.size() > 0)
                    fetchChildItem(groupPosition > 0 ? groupPosition : 0);
            }
        });
    }

    private void setVisibility() {
        try {
            Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pickUpHistoryExpandableAdapterListView.setVisibility(historyParentObjectArrayList.size() > 0 ? View.VISIBLE : View.GONE);
                    showProgressBar(false);
                    showFoundLayout();
                    notifyDataSetChanged();
                }
            });
        } catch (NullPointerException e) {
            CustomToast(getActivity(), "Loading...");
        }
    }

    private void notifyDataSetChanged() {
        try {
            Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pickUpHistoryExpandableAdapterExpandableAdapter.notifyDataSetChanged();
                }
            });
        } catch (NullPointerException e) {
            CustomToast(getActivity(), "Loading...");
        }
    }

    private void closeOtherChildView(int position) {
        for (int i = 0; i < historyParentObjectArrayList.size(); i++) {
            if (i != position) pickUpHistoryExpandableAdapterListView.collapseGroup(i);
        }
    }

    private void showProgressBar(final boolean show) {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((HistoryActivity) Objects.requireNonNull(getActivity())).showProgressBar(show);
            }
        });
    }

    public void reset() {
        historyParentObjectArrayList.clear();
        fetchParentItem();
    }

    /*----------------------------------------------------------other--------------------------------------------------------------------*/
    private void showSnackBar(String message) {
        ((HistoryActivity) Objects.requireNonNull(getActivity())).showSnackBar(message);
    }

    /*-------------------------------------------------------------refresh list request from history detail activity---------------------------*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == UPDATE_LIST)
            reset();
    }
}
