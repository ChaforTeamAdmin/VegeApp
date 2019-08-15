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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.jby.admin.R;
import com.jby.admin.adapter.stock.StockDetailAdapter;
import com.jby.admin.object.StockObject;
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

import static com.jby.admin.shareObject.CustomToast.CustomToast;

public class StockDetailDialog extends DialogFragment implements AdapterView.OnItemClickListener, SearchView.OnQueryTextListener {
    View rootView;
    private ListView stockDetailListView;
    private SearchView stockDetailSearch;
    private StockDetailAdapter stockDetailAdapter;
    private ArrayList<StockObject> stockObjectArrayList;
    private ProgressBar progressBar;
    private TextView stockDetailDate, stockDetailBalance;
    private String productID, productType, date, balance;

    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private Handler handler;

    public StockDetailDialogCallBack stockDetailDialogCallBack;

    public StockDetailDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.stock_detail_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        if (getArguments() != null) {
            productID = getArguments().getString("product_id");
            productType = getArguments().getString("product_type");
            balance = getArguments().getString("balance");
            date = getArguments().getString("date");
        }

        progressBar = rootView.findViewById(R.id.progress_bar);
        stockDetailListView = rootView.findViewById(R.id.stock_detail_list);
        stockDetailSearch = rootView.findViewById(R.id.stock_detail_search);
        stockDetailDate = rootView.findViewById(R.id.stock_detail_date);
        stockDetailBalance = rootView.findViewById(R.id.stock_detail_balance);

        stockObjectArrayList = new ArrayList<>();
        stockDetailAdapter = new StockDetailAdapter(getContext(), stockObjectArrayList, productType);
        stockDetailDialogCallBack = (StockDetailDialogCallBack) getParentFragment();
        handler = new Handler();
    }

    private void objectSetting() {
        stockDetailListView.setOnItemClickListener(this);
        stockDetailListView.setAdapter(stockDetailAdapter);
        stockDetailSearch.setOnQueryTextListener(this);
        stockDetailDate.setText(date);
        stockDetailBalance.setText(String.format("Balance: %s%s", balance, productType.equals("box") ? " Box" : " KG"));
        fetchStockDetail("");
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

    private void fetchStockDetail(final String query) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("product_id", productID));
                apiDataObjectArrayList.add(new ApiDataObject("date", date));
                apiDataObjectArrayList.add(new ApiDataObject("query", query));

                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().stock,
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
                            Log.d("jsonObject", "stock detail List: " + jsonObjectLoginResponse);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                            JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("stock_card");
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                stockObjectArrayList.add(new StockObject(
                                                        jsonArray.getJSONObject(i).getString("id"),
                                                        jsonArray.getJSONObject(i).getString("name"),
                                                        jsonArray.getJSONObject(i).getString("total_in"),
                                                        jsonArray.getJSONObject(i).getString("total_out")
                                                ));
                                            }
                                            Log.d("jsonObject", "stock detail List: " + stockObjectArrayList.size());
                                            notifyDataSetChanged();
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
                showProgressBar(false);
                notifyDataSetChanged();
            }
        }).start();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if(!stockObjectArrayList.get(i).getTotalIn().equals("null")){
            stockDetailDialogCallBack.selectedItemForDeduction(stockObjectArrayList.get(i), date);
            dismiss();
        }
    }

    /*
     * search
     * */
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(final String query) {
        showProgressBar(true);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                reset();
                fetchStockDetail(query);
            }
        }, 150);
        return false;
    }


    private void showProgressBar(final boolean show) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void notifyDataSetChanged() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stockDetailAdapter.notifyDataSetChanged();
            }
        });
    }

    private void reset() {
        stockObjectArrayList.clear();
        notifyDataSetChanged();
    }


    public interface StockDetailDialogCallBack {
        void selectedItemForDeduction(StockObject stockObject, String date);
    }
}
