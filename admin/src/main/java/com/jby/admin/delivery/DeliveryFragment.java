package com.jby.admin.delivery;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jby.admin.MainActivity;
import com.jby.admin.R;
import com.jby.admin.adapter.DeliveryAdapter;
import com.jby.admin.object.DeliveryOrderObject;
import com.jby.admin.object.ExpandableParentObject;
import com.jby.admin.others.NetworkConnection;
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

import static com.jby.admin.shareObject.CustomToast.CustomToast;
import static com.jby.admin.shareObject.VariableUtils.REFRESH_STOCK_LIST;


public class DeliveryFragment extends Fragment implements ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener,
        SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener {
    View rootView;
    //not found layout
    private RelativeLayout notFoundLayout;
    private ImageView notFoundIcon;
    private TextView notFoundLabel;

    private SwipeRefreshLayout deliveryFragmentSwipeRefreshLayout;
    private ExpandableListView deliveryFragmentListView;
    private DeliveryAdapter deliveryAdapter;
    private ArrayList<ExpandableParentObject> expandableParentObjectArrayList;
    private int groupPosition;

    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private Handler handler;

    public DeliveryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_delivery, container, false);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        //not found layout
        notFoundLayout = rootView.findViewById(R.id.not_found_layout);
        notFoundIcon = rootView.findViewById(R.id.not_found_layout_icon);
        notFoundLabel = rootView.findViewById(R.id.not_found_layout_label);

        deliveryFragmentSwipeRefreshLayout = rootView.findViewById(R.id.delivery_fragment_refresh_layout);
        deliveryFragmentListView = rootView.findViewById(R.id.delivery_fragment_list_view);
        expandableParentObjectArrayList = new ArrayList<>();
        deliveryAdapter = new DeliveryAdapter(getActivity(), expandableParentObjectArrayList);
        handler = new Handler();
    }

    private void objectSetting() {
        deliveryFragmentListView.setAdapter(deliveryAdapter);
        deliveryFragmentListView.setOnGroupClickListener(this);
        deliveryFragmentListView.setOnChildClickListener(this);
        deliveryFragmentListView.setOnScrollListener(this);

        deliveryFragmentSwipeRefreshLayout.setOnRefreshListener(this);
        setupNotFoundLayout();
        checkInternetConnection(null);
    }

    private void setupNotFoundLayout() {
        notFoundIcon.setImageDrawable(getResources().getDrawable(R.drawable.not_found));
        notFoundLabel.setText("No Delivery Order Is Found");
    }

    public void checkInternetConnection(View view) {
        boolean networkConnection = new NetworkConnection(getActivity()).checkNetworkConnection();
        if (networkConnection) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fetchParentItem();
                }
            }, 400);
        }
        deliveryFragmentListView.setVisibility(networkConnection ? View.VISIBLE : View.GONE);
        showProgressBar(networkConnection);
    }

    private void showProgressBar(final boolean show) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((MainActivity) Objects.requireNonNull(getActivity())).showProgressBar(show);
            }
        });
    }


    /*-----------------------------------------------------------------list view purpose---------------------------------------------------------*/
    /*
     * parent setup
     * */
    private void fetchParentItem() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("date", "-1"));

                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
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
                            Log.d("jsonObject", "jsonObject: " + jsonObjectLoginResponse);
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("delivery_order");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    expandableParentObjectArrayList.add(new ExpandableParentObject(jsonArray.getJSONObject(i).getString("date")));
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
                preOpenChild();

            }
        }).start();
    }

    @Override
    public boolean onGroupClick(ExpandableListView expandableListView, View view, final int i, long l) {
        groupPosition = i;
        if (expandableListView.isGroupExpanded(i)) expandableListView.collapseGroup(i);
        else {
            //close view
            closeOtherChildView(i);
            expandableParentObjectArrayList.get(i).getDeliveryOrderObjectArrayList().clear();
            fetchChildItem(i);
        }
        return true;
    }

    /*
     * child setup
     * */
    private void fetchChildItem(final int position) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("date", expandableParentObjectArrayList.get(position).getDate()));
                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
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
                            Log.d("jsonObject", "jsonObject: " + jsonObjectLoginResponse);
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("delivery_order");
                                setChildValue(jsonArray, position);
                            } else {
                                expandableParentObjectArrayList.remove(groupPosition);
                                setVisibility();
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
                expandableParentObjectArrayList.get(position).setDeliveryOrderObjectArrayList(setChildObject(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deliveryFragmentListView.expandGroup(position);
                deliveryFragmentListView.setSelectedGroup(position);
                notifyDataSetChanged();
            }
        });
    }

    private DeliveryOrderObject setChildObject(JSONObject jsonObject) {
        DeliveryOrderObject object = null;
        try {
            object = new DeliveryOrderObject(
                    jsonObject.getString("id"),
                    jsonObject.getString("prefix"),
                    jsonObject.getString("status"),
                    jsonObject.getString("print_status"),
                    jsonObject.getString("created_time"),
                    jsonObject.getString("driver"),
                    jsonObject.getString("customer"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
        Bundle bundle = new Bundle();
        bundle.putString("do_id", expandableParentObjectArrayList.get(i).getDeliveryOrderObjectArrayList().get(i1).getId());
        bundle.putString("date", expandableParentObjectArrayList.get(i).getDate());
        startActivityForResult(new Intent(getActivity(), DeliveryOrderDetail.class).putExtras(bundle), REFRESH_STOCK_LIST);
        return true;
    }

    /*
     * list View other setting
     * */
    private void preOpenChild() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (expandableParentObjectArrayList.size() > 0) fetchChildItem(0);
            }
        });
    }

    private void closeOtherChildView(int position) {
        for (int i = 0; i < expandableParentObjectArrayList.size(); i++) {
            if (i != position) deliveryFragmentListView.collapseGroup(i);
        }
    }

    private void notifyDataSetChanged() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deliveryAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setVisibility() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                deliveryFragmentListView.setVisibility(expandableParentObjectArrayList.size() > 0 ? View.VISIBLE : View.GONE);
                notFoundLayout.setVisibility(expandableParentObjectArrayList.size() > 0 ? View.GONE : View.VISIBLE);
                showProgressBar(false);
                notifyDataSetChanged();
            }
        });
    }

    public void reset() {
        closeOtherChildView(-1);
        expandableParentObjectArrayList.clear();
        fetchParentItem();
    }

    @Override
    public void onRefresh() {
        reset();
        deliveryFragmentSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {
        if (deliveryFragmentListView.getChildAt(0) != null) {
            deliveryFragmentSwipeRefreshLayout.setEnabled(deliveryFragmentListView.getFirstVisiblePosition() == 0 && deliveryFragmentListView.getChildAt(0).getTop() == 0);
        }
    }
}
