package com.jby.admin.basket;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;


import com.jby.admin.R;
import com.jby.admin.adapter.BasketAdapter;
import com.jby.admin.object.BasketObject;
import com.jby.admin.others.SwipeDismissTouchListener;
import com.jby.admin.shareObject.ApiDataObject;
import com.jby.admin.shareObject.ApiManager;
import com.jby.admin.shareObject.AsyncTaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.admin.shareObject.CustomToast.CustomToast;


public class BasketDetailDialog extends DialogFragment implements SearchView.OnQueryTextListener, AdapterView.OnItemSelectedListener {
    View rootView;

    private ProgressBar progressBar;
    private SearchView basketDetailDialogSearchView;
    private Spinner basketDetailDialogSorting;
    private TextView basketDetailDialogNotFound;
    /*
     * list view
     * */
    private RecyclerView basketDetailDialogListView;
    private ArrayList<BasketObject> basketObjectArrayList;
    private BasketAdapter basketAdapter;
    private String type, sort = "All";
    /*
     * Async task
     * */
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    Handler handler;

    public BasketDetailDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.basket_detail_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        progressBar = rootView.findViewById(R.id.progress_bar);
        basketDetailDialogSearchView = rootView.findViewById(R.id.basket_detail_dialog_search);
        basketDetailDialogSorting = rootView.findViewById(R.id.basket_detail_dialog_sort);
        basketDetailDialogNotFound = rootView.findViewById(R.id.basket_detail_dialog_not_found);

        basketDetailDialogListView = rootView.findViewById(R.id.basket_detail_dialog_product_list);
        basketObjectArrayList = new ArrayList<>();
        basketAdapter = new BasketAdapter(getActivity(), basketObjectArrayList);
        handler = new Handler();
    }

    private void objectSetting() {
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        basketDetailDialogListView.setLayoutManager(mLayoutManager);
        basketDetailDialogListView.setItemAnimator(new DefaultItemAnimator());
        basketDetailDialogListView.setAdapter(basketAdapter);

        basketDetailDialogSearchView.setOnQueryTextListener(this);
        basketDetailDialogSorting.setOnItemSelectedListener(this);

        if (getArguments() != null) {
            showProgressBar(true);
            setupSpinner();
            type = getArguments().getString("type");
            getBasketDetail();

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

    private void setupSpinner() {
        List<String> categories = new ArrayList<>();
        categories.add("All");
        categories.add("Outstanding");
        categories.add("Owe");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(Objects.requireNonNull(getActivity()), R.layout.custom_spinner_layout, categories) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                CheckedTextView textView = view.findViewById(android.R.id.text1);
                textView.setTextColor(getResources().getColor(R.color.grey));
                return view;
            }
        };
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        basketDetailDialogSorting.setAdapter(dataAdapter);
    }

    /*-------------------------------------------------------------------------list view purpose-------------------------------------------------------------*/
    private void getBasketDetail() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("type", type));
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
                        Log.d("json", "jsonObject: " + jsonObjectLoginResponse);
                        if (jsonObjectLoginResponse != null) {
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {

                                JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray(type);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    basketObjectArrayList.add(new BasketObject(
                                            jsonArray.getJSONObject(i).getString("id"),
                                            jsonArray.getJSONObject(i).getString("name"),
                                            jsonArray.getJSONObject(i).getString("quantity")
                                    ));
                                }
                                notifyDataSetChanged();
                                showProgressBar(false);
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

    private void notifyDataSetChanged() {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                basketAdapter.notifyDataSetChanged();
            }
        });
    }

    /*-------------------------------------------------------------------------search setting------------------------------------------------------------*/
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        showProgressBar(true);
        searchFromArrayList(query);
        return false;
    }

    private void searchFromArrayList(final String query) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<BasketObject> searchList = new ArrayList<>();
                for (int i = 0; i < basketObjectArrayList.size(); i++) {
                    if (sort.equals("All")) {
                        if (basketObjectArrayList.get(i).getName().contains(query)) {
                            searchList.add(basketObjectArrayList.get(i));
                        }
                    }
                    //if sort = outstanding or owe
                    else if (sort.equals("Outstanding") ? Integer.valueOf(basketObjectArrayList.get(i).getQuantity()) > 0 : Integer.valueOf(basketObjectArrayList.get(i).getQuantity()) < 0)
                        searchList.add(basketObjectArrayList.get(i));
                }

                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        basketAdapter = new BasketAdapter(getActivity(), searchList);
                        basketDetailDialogListView.setAdapter(basketAdapter);
                        notifyDataSetChanged();
                        showProgressBar(false);
                        setVisibility(searchList);
                    }
                });
            }
        }).start();
    }

    /*--------------------------------------------------------------------------sorting purpose--------------------------------------------------------------*/
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        sort = adapterView.getSelectedItem().toString();
        if (basketObjectArrayList.size() > 0) searchFromArrayList("");
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    /*------------------------------------------------------------------other ----------------------------------------------------------------------------------*/

    private void showProgressBar(final boolean show) {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void setVisibility(ArrayList<BasketObject> arrayList) {
        basketDetailDialogListView.setVisibility(arrayList.size() > 0 ? View.VISIBLE : View.GONE);
        basketDetailDialogNotFound.setVisibility(arrayList.size() > 0 ? View.GONE : View.VISIBLE);
        basketDetailDialogNotFound.setText(basketDetailDialogNotFound.getVisibility() == View.VISIBLE ? "Not any " + sort + " record found" : "");
    }
}