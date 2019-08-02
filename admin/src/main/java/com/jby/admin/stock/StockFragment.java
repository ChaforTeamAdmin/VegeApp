package com.jby.admin.stock;

import android.content.Context;;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Spinner;

import com.jby.admin.MainActivity;
import com.jby.admin.R;
import com.jby.admin.adapter.stock.ProductExpandableAdapter;
import com.jby.admin.object.StockObject;
import com.jby.admin.object.ProductDetailParentObject;
import com.jby.admin.shareObject.ApiDataObject;
import com.jby.admin.shareObject.ApiManager;
import com.jby.admin.shareObject.AsyncTaskManager;
import com.jby.admin.sharePreference.SharedPreferenceManager;
import com.jby.admin.stock.dialog.DriverDialog;

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


public class StockFragment extends Fragment implements AdapterView.OnItemSelectedListener, SearchView.OnQueryTextListener,
        ProductExpandableAdapter.ProductExpandableAdapterCallBack, ExpandableListView.OnGroupClickListener,
        View.OnClickListener, SwipeRefreshLayout.OnRefreshListener,
        DriverDialog.DriverDialogCallBack, AbsListView.OnScrollListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;

    private View rootView;
    private Spinner stockFragmentSpinner;
    private SearchView stockFragmentSearch;
    /*
     * list view
     * */
    private SwipeRefreshLayout stockFragmentRefreshLayout;
    private ExpandableListView stockFragmentProductList;
    private ProductExpandableAdapter productAdapter;
    private ArrayList<ProductDetailParentObject> productObjectArrayList;
    private JSONArray jsonArray;
    private int groupPosition = -1, childPosition = -1;
    /*
     * sorting purpose
     * */
    private String query = "";
    /*
     * dialog
     * */
    private DialogFragment dialogFragment;
    private FragmentManager fm;
    /*
     * Async task
     * */
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private Handler handler;

    public StockFragment() {
    }

    // TODO: Rename and change types and number of parameters
    public static StockFragment newInstance(String param1, String param2) {
        StockFragment fragment = new StockFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_stock, container, false);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        stockFragmentRefreshLayout = rootView.findViewById(R.id.fragment_stock_refresh_layout);

        stockFragmentProductList = rootView.findViewById(R.id.fragment_stock_product_list);
        stockFragmentSearch = rootView.findViewById(R.id.fragment_stock_search_view);
        stockFragmentSpinner = rootView.findViewById(R.id.fragment_stock_sort);

        productObjectArrayList = new ArrayList<>();
        productAdapter = new ProductExpandableAdapter(getActivity(), productObjectArrayList, this);
        handler = new Handler();
        fm = getChildFragmentManager();

    }

    private void objectSetting() {
        stockFragmentProductList.setAdapter(productAdapter);
        stockFragmentProductList.setOnGroupClickListener(this);
        stockFragmentProductList.setOnScrollListener(this);

        stockFragmentSpinner.setOnItemSelectedListener(this);
        stockFragmentSearch.setOnQueryTextListener(this);

        stockFragmentRefreshLayout.setOnRefreshListener(this);
        //search view
        searchViewSetting();
        //spinner and progress bar
        setupSpinner();
        //progress bar
        showProgressBar(true);
        //fetch data
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchParentItem(query);
            }
        }, 200);
    }

    @Override
    public void onClick(View view) {
    }

    private void searchViewSetting() {
        //close icon
        ImageView iconClose = stockFragmentSearch.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        iconClose.setColorFilter(getResources().getColor(R.color.white));
        //change search icon color
        ImageView iconSearch = stockFragmentSearch.findViewById(android.support.v7.appcompat.R.id.search_button);
        iconSearch.setColorFilter(getResources().getColor(R.color.white));
        //change text color
        EditText text = stockFragmentSearch.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        text.setTextColor(getResources().getColor(R.color.white));

    }

    private void setupSpinner() {
        List<String> categories = new ArrayList<>();
        categories.add("All");
        categories.add("Available");
        categories.add("Unavailable");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(Objects.requireNonNull(getActivity()), R.layout.custom_spinner_layout, categories);
        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        stockFragmentSpinner.setAdapter(dataAdapter);
    }

    /*-------------------------------------------------------------------expandable list purpose----------------------------------------------*/

    public void reset() {
        hideBottomSheetLayout(true);
        showProgressBar(true);
        closeOtherChildView(-1);
        fetchParentItem(query);
    }

    private void fetchParentItem(final String query) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                productObjectArrayList.clear();

                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("read", "1"));
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
                            /*
                             * cleat array list for search purpose
                             * */
                            productObjectArrayList.clear();
                            Log.d("jsonObject", "Stock List: " + jsonObjectLoginResponse);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                            /*
                                             * product list
                                             * */
                                            jsonArray = jsonObjectLoginResponse.getJSONArray("stock");
                                            sorting();
                                        }
                                        /*
                                         * status != 1 mean not found or something else
                                         * */
                                        else {
                                            productAdapter.notifyDataSetChanged();
                                            showProgressBar(false);
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

    //----------------------------------------------------------------------------------parent-----------------------------------------------------
    private void setParentValue(int i) {
        try {
            //mean this id is never added yet so create a new group view
            productObjectArrayList.add(new ProductDetailParentObject(
                    jsonArray.getJSONObject(i).getString("id"),
                    jsonArray.getJSONObject(i).getString("name"),
                    jsonArray.getJSONObject(i).getString("picture"),
                    jsonArray.getJSONObject(i).getString("type"),
                    jsonArray.getJSONObject(i).getString("price"),
                    jsonArray.getJSONObject(i).getString("product_code"),
                    jsonArray.getJSONObject(i).getString("available_quantity")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onGroupClick(ExpandableListView expandableListView, View view, final int i, long l) {
        groupPosition = i;
        if (!productObjectArrayList.get(i).getAvailable_quantity().equals("0")) {

            if (expandableListView.isGroupExpanded(i)) expandableListView.collapseGroup(i);
            else {
                //close view
                closeOtherChildView(i);
                productObjectArrayList.get(i).getStockObjectArrayList().clear();
                showProgressBar(true);
                fetchChildItem();

            }
        } else showSnackBar("Not Available!");
        return true;
    }

    private void closeOtherChildView(final int position) {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < productObjectArrayList.size(); i++) {
                    if (i != position) stockFragmentProductList.collapseGroup(i);
                }
            }
        });
    }

//    ------------------------------------------------------------------------------child---------------------------------------------------------------------

    private void fetchChildItem() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("read", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("product_id", productObjectArrayList.get(groupPosition).getId()));
                apiDataObjectArrayList.add(new ApiDataObject("day_limit", SharedPreferenceManager.getDayLimit(getActivity())));
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
                            Log.d("jsonObject", "child stock List: " + jsonObjectLoginResponse);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                            setChildValue(jsonObjectLoginResponse.getJSONArray("stock_detail"), groupPosition);
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
            }
        }).start();
    }

    private void setChildValue(JSONArray jsonArray, final int position) {
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    productObjectArrayList.get(position).setStockObjectArrayList(setChildObject(jsonArray.getJSONObject(i)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stockFragmentProductList.expandGroup(position);
                        stockFragmentProductList.setSelectedGroup(position);
                        notifyDataSetChanged();
                    }
                });
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private StockObject setChildObject(JSONObject jsonObject) {
        StockObject object = null;
        try {
            object = new StockObject(
                    jsonObject.getString("created_at"),
                    jsonObject.getString("total_weight"),
                    jsonObject.getString("total_quantity"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    /*--------------------------------------------------------assign delivery order ---------------------------------------------------------------*/
    @Override
    public void childOnClick(final int position, int groupPosition) {

    }

    // ------------------------------------------------------sorting purpose------------------------------------------------------------------------
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.getId() == R.id.fragment_stock_sort) {
            if (jsonArray != null) {
                productObjectArrayList.clear();
                sorting();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(final String newText) {
        query = newText;
        if ((((MainActivity) Objects.requireNonNull(getActivity())).mainActivityProgressBar.getVisibility() != View.VISIBLE))
            showProgressBar(true);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchParentItem(query);
            }
        }, 500);
        return false;
    }

    private void sorting() {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                //sorting by spinner
                switch (stockFragmentSpinner.getSelectedItemPosition()) {
                    case 0:
                        setParentValue(i);
                        break;
                    case 1:
                        if (!jsonArray.getJSONObject(i).getString("available_quantity").equals("0"))
                            setParentValue(i);
                        break;
                    case 2:
                        if (jsonArray.getJSONObject(i).getString("available_quantity").equals("0"))
                            setParentValue(i);
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                productAdapter.notifyDataSetChanged();
                showProgressBar(false);
            }
        }, 200);

    }

    /*-------------------------------------------------------------default driver purpose---------------------------------------------------------------*/

    private void setMargin(boolean set) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) stockFragmentProductList.getLayoutParams();
        params.bottomMargin = (set ? ((MainActivity) Objects.requireNonNull(getActivity())).getActionBarHeight() : 0);
    }

    private void hideBottomSheetLayout(final boolean hide) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /*
                 * set margin for list view
                 * */
                setMargin(!hide);
            }
        });
    }

    public void openDriverDialog() {
        Bundle bundle = new Bundle();
        bundle.putString("customer_id", ((MainActivity) getActivity()).getCustomerID());

        dialogFragment = new DriverDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getChildFragmentManager(), "");
    }

    @Override
    public void selectedItem(String name, String id) {

    }


    /*------------------------------------------------------------fragment default setting-------------------------------------------------------------*/
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {
        if (stockFragmentProductList.getChildAt(0) != null) {
            stockFragmentRefreshLayout.setEnabled(stockFragmentProductList.getFirstVisiblePosition() == 0 && stockFragmentProductList.getChildAt(0).getTop() == 0);
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

    }

    /*--------------------------------------------------------------other------------------------------------------------------------------------*/
    public void showSnackBar(String message) {
        final Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    private void showProgressBar(boolean show) {
        ((MainActivity) Objects.requireNonNull(getActivity())).showProgressBar(show);
    }

    private void notifyDataSetChanged() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                productAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onRefresh() {
        stockFragmentRefreshLayout.setRefreshing(false);
    }

}
