package com.jby.vegeapp.history.basket;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jby.vegeapp.R;
import com.jby.vegeapp.adapter.BasketHistoryExpandableAdapter;
import com.jby.vegeapp.basket.BasketActivity;
import com.jby.vegeapp.history.HistoryActivity;
import com.jby.vegeapp.object.BasketHistoryObject;
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
import static com.jby.vegeapp.shareObject.VariableUtils.UPDATE_LIST;


public class BasketHistory extends Fragment implements BasketHistoryExpandableAdapter.BasketHistoryExpandableAdapterCallBack,
        ExpandableListView.OnGroupClickListener {
    View rootView;
    //not found layout
    private RelativeLayout notFoundLayout;
    private ImageView notFoundIcon;
    private TextView notFoundLabel;

    NonScrollExpandableListView basketHistoryListView;
    ArrayList<HistoryParentObject> historyParentObjectArrayList;
    BasketHistoryExpandableAdapter basketHistoryExpandableAdapter;
    private int groupPosition = 0;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_basket_history, container, false);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        //not found layout
        notFoundLayout = rootView.findViewById(R.id.not_found_layout);
        notFoundIcon = rootView.findViewById(R.id.not_found_layout_icon);
        notFoundLabel = rootView.findViewById(R.id.not_found_layout_label);

        basketHistoryListView = rootView.findViewById(R.id.fragment_basket_history_list_view);
        historyParentObjectArrayList = new ArrayList<>();
        basketHistoryExpandableAdapter = new BasketHistoryExpandableAdapter(getActivity(), historyParentObjectArrayList, this);
        handler = new Handler();
    }

    private void objectSetting() {
        basketHistoryListView.setAdapter(basketHistoryExpandableAdapter);
        basketHistoryListView.setOnGroupClickListener(this);
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
        basketHistoryListView.setVisibility(networkConnection ? View.VISIBLE : View.GONE);

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
                apiDataObjectArrayList.add(new ApiDataObject("driver_id", SharedPreferenceManager.getUserId(getActivity())));

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
                                JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("basket_history_date");
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
            historyParentObjectArrayList.get(i).getBasketHistoryObjectArrayList().clear();
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
                apiDataObjectArrayList.add(new ApiDataObject("driver_id", SharedPreferenceManager.getUserId(getActivity())));
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
                                JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("basket_history");
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
                historyParentObjectArrayList.get(position).setBasketHistoryObjectArrayList(setChildObject(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                basketHistoryListView.expandGroup(position);
                basketHistoryListView.setSelectedGroup(position);
                notifyDataSetChanged();
            }
        });
    }

    private BasketHistoryObject setChildObject(JSONObject jsonObject) {
        BasketHistoryObject object = null;
        try {
            object = new BasketHistoryObject(
                    jsonObject.getString("id"),
                    jsonObject.getString("quantity"),
                    jsonObject.getString("type"),
                    jsonObject.getString("farmer_id"),
                    jsonObject.getString("customer_id"),
                    jsonObject.getString("stock_id"),
                    jsonObject.getString("created_time"),
                    jsonObject.getString("created_date"),
                    jsonObject.getString("farmer"),
                    jsonObject.getString("customer"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    /*------------------------------------------------------list view onclick-------------------------------------------------------------*/
    @Override
    public void edit(int position) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("object", historyParentObjectArrayList.get(groupPosition).getBasketHistoryObjectArrayList().get(position));
        startActivityForResult(new Intent(getActivity(), BasketActivity.class).putExtras(bundle), UPDATE_LIST);
    }

    @Override
    public void delete(int position) {
        deleteConfirmation(position);
    }

    public void deleteConfirmation(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Warning");
        builder.setMessage("Are you sure that you want to delete this record? \nIt may affect overall result");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        deleteBasket(position);
                        dialog.cancel();
                    }
                });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    private void deleteBasket(final int position) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("id", historyParentObjectArrayList.get(groupPosition).getBasketHistoryObjectArrayList().get(position).getId()));
                apiDataObjectArrayList.add(new ApiDataObject("type", historyParentObjectArrayList.get(groupPosition).getBasketHistoryObjectArrayList().get(position).getType()));
                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().basket,
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
                                showSnackBar("Delete Successfully!");
                                reset();
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
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                basketHistoryListView.setVisibility(historyParentObjectArrayList.size() > 0 ? View.VISIBLE : View.GONE);
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
                basketHistoryExpandableAdapter.notifyDataSetChanged();
            }
        });
    }

    private void closeOtherChildView(int position) {
        for (int i = 0; i < historyParentObjectArrayList.size(); i++) {
            if (i != position) basketHistoryListView.collapseGroup(i);
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

}
