package com.jby.vegeapp.history.delivery;

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

import com.jby.vegeapp.R;
import com.jby.vegeapp.adapter.DeliveryHistoryExpandableAdapter;
import com.jby.vegeapp.history.HistoryActivity;
import com.jby.vegeapp.object.DeliveryHistoryObject;
import com.jby.vegeapp.object.HistoryParentObject;
import com.jby.vegeapp.others.NetworkConnection;
import com.jby.vegeapp.others.NonScrollExpandableListView;
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

import static com.jby.vegeapp.shareObject.CustomToast.CustomToast;

public class DeliveryHistory extends Fragment implements DeliveryHistoryExpandableAdapter.DeliveryHistoryExpandableAdapterCallBack, ExpandableListView.OnGroupClickListener {
    View rootView;
    //not found layout
    private RelativeLayout notFoundLayout;
    private ImageView notFoundIcon;
    private TextView notFoundLabel;

    NonScrollExpandableListView pickUpHistoryExpandableAdapterListView;
    ArrayList<HistoryParentObject> historyParentObjectArrayList;
    DeliveryHistoryExpandableAdapter deliveryHistoryExpandableAdapter;
    private int groupPosition = 0;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private Handler handler;
    public DeliveryHistory() {
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
        rootView = inflater.inflate(R.layout.fragment_delivery_history, container, false);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        //not found layout
        notFoundLayout = rootView.findViewById(R.id.not_found_layout);
        notFoundIcon = rootView.findViewById(R.id.not_found_layout_icon);
        notFoundLabel = rootView.findViewById(R.id.not_found_layout_label);

        pickUpHistoryExpandableAdapterListView = rootView.findViewById(R.id.fragment_delivery_history_list_view);
        historyParentObjectArrayList = new ArrayList<>();
        deliveryHistoryExpandableAdapter = new DeliveryHistoryExpandableAdapter(getActivity(), historyParentObjectArrayList, this);
        handler = new Handler();
    }

    private void objectSetting() {
        pickUpHistoryExpandableAdapterListView.setAdapter(deliveryHistoryExpandableAdapter);
        pickUpHistoryExpandableAdapterListView.setOnGroupClickListener(this);
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
                apiDataObjectArrayList.add(new ApiDataObject("delivery_driver_id", SharedPreferenceManager.getUserId(getActivity())));

                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().history,
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
                                JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("delivery_history_date");
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
                apiDataObjectArrayList.add(new ApiDataObject("delivery_driver_id", SharedPreferenceManager.getUserId(getActivity())));
                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().history,
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
                                JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("delivery_history");
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
                historyParentObjectArrayList.get(position).setDeliveryHistoryObjectArrayList(setChildObject(jsonArray.getJSONObject(i)));
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

    private DeliveryHistoryObject setChildObject(JSONObject jsonObject) {
        DeliveryHistoryObject object = null;
        try {
            object = new DeliveryHistoryObject(
                    jsonObject.getString("id"),
                    jsonObject.getString("do_id"),
                    jsonObject.getString("customer_id"),
                    jsonObject.getString("customer"),
                    jsonObject.getString("quantity"),
                    jsonObject.getString("created_time"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    /*------------------------------------------------------list view onclick-------------------------------------------------------------*/
    @Override
    public void edit(int position) {

    }

    @Override
    public void delete(int position) {

    }

    /*---------------------------------------------------------list view other setting---------------------------------------------------*/
    private void preOpenChild(final int groupPosition) {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (historyParentObjectArrayList.size() > 0) fetchChildItem(groupPosition >0 ? groupPosition : 0);
            }
        });
    }

    private void setVisibility() {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pickUpHistoryExpandableAdapterListView.setVisibility(historyParentObjectArrayList.size() > 0 ? View.VISIBLE : View.GONE);
                showProgressBar(false);
                showFoundLayout();
                notifyDataSetChanged();
            }
        });
    }

    private void notifyDataSetChanged() {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deliveryHistoryExpandableAdapter.notifyDataSetChanged();
            }
        });
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

    public void reset(){
        historyParentObjectArrayList.clear();
        fetchParentItem();
    }

    /*----------------------------------------------------------other--------------------------------------------------------------------*/
    private void showSnackBar(String message){
        ((HistoryActivity) Objects.requireNonNull(getActivity())).showSnackBar(message);
    }
}
