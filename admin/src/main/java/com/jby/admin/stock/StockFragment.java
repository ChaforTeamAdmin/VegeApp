package com.jby.admin.stock;

import android.app.DatePickerDialog;
import android.content.Context;;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.jby.admin.MainActivity;
import com.jby.admin.R;
import com.jby.admin.adapter.stock.StockExpandableAdapter;
import com.jby.admin.farmer.FarmerDialog;
import com.jby.admin.object.entity.FarmerObject;
import com.jby.admin.object.StockObject;
import com.jby.admin.object.ProductDetailParentObject;
import com.jby.admin.object.entity.GradeObject;
import com.jby.admin.object.entity.LocationObject;
import com.jby.admin.object.product.ProductObject;
import com.jby.admin.product.ProductDialog;
import com.jby.admin.shareObject.ApiDataObject;
import com.jby.admin.shareObject.ApiManager;
import com.jby.admin.shareObject.AsyncTaskManager;
import com.jby.admin.sharePreference.SharedPreferenceManager;
import com.jby.admin.stock.dialog.StockDetailDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.admin.shareObject.CustomToast.CustomToast;


public class StockFragment extends Fragment implements AdapterView.OnItemSelectedListener, SearchView.OnQueryTextListener,
        ExpandableListView.OnGroupClickListener, View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener,
        ProductDialog.ProductDialogCallBack, FarmerDialog.FarmerDialogCallBack, StockDetailDialog.StockDetailDialogCallBack,
        StockExpandableAdapter.ProductExpandableAdapterCallBack {

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
    private StockExpandableAdapter productAdapter;
    private ArrayList<ProductDetailParentObject> productObjectArrayList;
    private JSONArray jsonArray;
    private int groupPosition = -1;
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

    /*------------------------------------------------------deduction layout purpose--------------------------------------------------------------*/
    private BottomSheetBehavior deductionLayout;
    private LinearLayout stockFragmentDateLayout, stockFragmentFarmerLayout, stockFragmentProductLayout;
    private EditText stockFragmentDeduceWeight;
    private TextView stockFragmentDate, stockFragmentFarmer, stockFragmentProduct;

    private Spinner stockFragmentGrade, stockFragmentLocation;
    private LinearLayout stockFragmentGradeLayout, stockFragmentLocationLayout, stockFragmentWeightLayout;

    private Button stockFragmentDeduceButton;

    private ProductObject productObject;
    private FarmerObject farmerObject;
    private ArrayList<GradeObject> gradeObjectArrayList = new ArrayList<>();
    private ArrayList<LocationObject> locationObjectArrayList = new ArrayList<>();

    private String poId = "";

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

        /*
         * deduction layout
         * */
        View bottomSheet = rootView.findViewById(R.id.fragment_stock_bottom_sheet);
        deductionLayout = BottomSheetBehavior.from(bottomSheet);

        stockFragmentDateLayout = rootView.findViewById(R.id.fragment_stock_date_layout);
        stockFragmentFarmerLayout = rootView.findViewById(R.id.fragment_stock_farmer_layout);
        stockFragmentProductLayout = rootView.findViewById(R.id.fragment_stock_product_layout);

        stockFragmentDeduceWeight = rootView.findViewById(R.id.fragment_stock_deduce_weight);

        stockFragmentLocation = rootView.findViewById(R.id.fragment_stock_location);
        stockFragmentGrade = rootView.findViewById(R.id.fragment_stock_grade);

        stockFragmentLocationLayout = rootView.findViewById(R.id.fragment_stock_location_layout);
        stockFragmentGradeLayout = rootView.findViewById(R.id.fragment_stock_grade_layout);
        stockFragmentWeightLayout = rootView.findViewById(R.id.fragment_stock_deduce_weight_layout);

        stockFragmentDate = rootView.findViewById(R.id.fragment_stock_date);
        stockFragmentFarmer = rootView.findViewById(R.id.fragment_stock_farmer);
        stockFragmentProduct = rootView.findViewById(R.id.fragment_stock_product);

        stockFragmentDeduceButton = rootView.findViewById(R.id.fragment_stock_deduce_button);

        productObjectArrayList = new ArrayList<>();
        productAdapter = new StockExpandableAdapter(getActivity(), productObjectArrayList, this);
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
        /*
         * deduction layout
         * */
        stockFragmentDateLayout.setOnClickListener(this);
        stockFragmentFarmerLayout.setOnClickListener(this);
        stockFragmentProductLayout.setOnClickListener(this);
        stockFragmentDeduceButton.setOnClickListener(this);

        stockFragmentLocation.setOnItemSelectedListener(this);
        stockFragmentGrade.setOnItemSelectedListener(this);

        stockFragmentDate.setText(setDefaultDate());

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
        switch (view.getId()) {
            case R.id.fragment_stock_date_layout:
                openDatePicker();
                break;
            case R.id.fragment_stock_farmer_layout:
                openFarmerDialog();
                break;
            case R.id.fragment_stock_product_layout:
                openProductDialog();
                break;
            case R.id.fragment_stock_deduce_button:
                checkingBeforeDeduction();
                break;

        }
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
                    } catch (NullPointerException e) {
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
        if (expandableListView.isGroupExpanded(i)) expandableListView.collapseGroup(i);
        else {
            //close view
            closeOtherChildView(i);
            showProgressBar(true);
            fetchChildItem(i);

        }
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

    private void fetchChildItem(final int position) {
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
                                            productObjectArrayList.get(position).getStockObjectArrayList().clear();
                                            setChildValue(jsonObjectLoginResponse.getJSONArray("stock_detail"), groupPosition);
                                        } else {
                                            showSnackBar("No Record is Found!");
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
                    jsonObject.getString("total_in"),
                    jsonObject.getString("total_out"),
                    jsonObject.getString("total_in_quantity"),
                    jsonObject.getString("total_out_quantity"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    @Override
    public void childOnClick(int position, int childPosition) {
        openStockDetailDialog(position, childPosition);
    }

    private void openStockDetailDialog(int position, int childPosition) {
        Bundle bundle = new Bundle();
        bundle.putString("date", productObjectArrayList.get(position).getStockObjectArrayList().get(childPosition).getDate());
        bundle.putString("product_id", productObjectArrayList.get(position).getId());
        bundle.putString("product_type", productObjectArrayList.get(position).getType());
        bundle.putString("balance", productObjectArrayList.get(position).getStockObjectArrayList().get(childPosition).calculateTotalWeight());

        DialogFragment stockDetailDialog = new StockDetailDialog();
        stockDetailDialog.setArguments(bundle);
        stockDetailDialog.show(getChildFragmentManager(), "");
    }

    //    ------------------------------------------------------------------------------deduction purpose------------------------------------------------------------
    /*
     * date layout
     * */
    private String setDefaultDate() {
        return (String) android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date());
    }

    private void openDatePicker() {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        stockFragmentDate.setText(String.format("%s", String.format(Locale.getDefault(), "%d-%02d-%02d", year, (monthOfYear + 1), dayOfMonth)));
                        checkLocationAndGradeSetting();
                    }
                }, mYear, mMonth, mDay);

        datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
        datePickerDialog.show();
    }

    /*
     * product layout
     * */

    private void openProductDialog() {
        DialogFragment productDialog = new ProductDialog();
        productDialog.show(getChildFragmentManager(), "");
    }

    @Override
    public void selectedProduct(ProductObject productObject) {
        this.productObject = productObject;
        stockFragmentProduct.setText(productObject.getName());
        checkLocationAndGradeSetting();
    }

    /*
     * farmer layout
     * */
    private void openFarmerDialog() {
        DialogFragment farmerDialog = new FarmerDialog();
        farmerDialog.show(getChildFragmentManager(), "");
    }

    @Override
    public void selectedFarmer(FarmerObject farmerObject) {
        this.farmerObject = farmerObject;
        stockFragmentFarmer.setText(farmerObject.getName());
        checkLocationAndGradeSetting();
    }


    private void checkLocationAndGradeSetting() {
        if (productObject != null && farmerObject != null && !stockFragmentDate.getText().toString().equals("")) {
            showProgressBar(true);

            if (SharedPreferenceManager.getGrade(getActivity()) || SharedPreferenceManager.getLocation(getActivity())) {
                /*
                 * clear spinner item
                 * */
                clearSpinnerItem();

                /*
                 * fetch location and grade
                 * */
                if (SharedPreferenceManager.getLocation(getActivity())) fetchLocation();
                else fetchGrade();
            }
            /*
             * fetch po_id if location and grade is close
             * */
            else {
                showWeightLayout(true);
                fetchPoID();
            }
        }
        /*
         * hide before all the required fields is filled up
         * */
        else {
            setLocationAndGradeVisibility(false);
        }

    }

    /*
     * set location and grade visibility
     * */
    private void setLocationAndGradeVisibility(boolean show) {
        if (show) {
            stockFragmentLocationLayout.setVisibility(SharedPreferenceManager.getLocation(getActivity()) ? View.VISIBLE : View.GONE);
            stockFragmentGradeLayout.setVisibility(SharedPreferenceManager.getGrade(getActivity()) ? View.VISIBLE : View.GONE);
            stockFragmentWeightLayout.setVisibility(View.VISIBLE);
        } else {
            stockFragmentLocationLayout.setVisibility(View.GONE);
            stockFragmentGradeLayout.setVisibility(View.GONE);
            stockFragmentWeightLayout.setVisibility(View.GONE);
            clearSpinnerItem();
        }
        showWeightLayout(show);
    }

    private void showWeightLayout(boolean show) {
        stockFragmentWeightLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /*
     * location setting
     * */
    private void fetchLocation() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("date", stockFragmentDate.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("farmer_id", farmerObject.getId()));
                apiDataObjectArrayList.add(new ApiDataObject("product_id", productObject.getId()));
                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().location,
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
                            Log.d("jsonObject", "location value: " + jsonObjectLoginResponse);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                            setLocationAndGradeVisibility(true);
                                            JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("location");
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                locationObjectArrayList.add(new LocationObject(
                                                        jsonArray.getJSONObject(i).getString("id"),
                                                        "",
                                                        jsonArray.getJSONObject(i).getString("location")
                                                ));
                                            }
                                            setUpLocationSpinner();
                                        } else {
                                            setLocationAndGradeVisibility(false);
                                            showSnackBar("No Record is found!");
                                        }

                                    } catch (
                                            JSONException e) {
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

    private void setUpLocationSpinner() {
        ArrayAdapter<LocationObject> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, locationObjectArrayList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stockFragmentLocation.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    /*
     * grade setting
     * */
    private void fetchGrade() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("date", stockFragmentDate.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("farmer_id", farmerObject.getId()));
                apiDataObjectArrayList.add(new ApiDataObject("product_id", productObject.getId()));
                apiDataObjectArrayList.add(new ApiDataObject("location", SharedPreferenceManager.getLocation(getActivity()) ? locationObjectArrayList.get(stockFragmentLocation.getSelectedItemPosition()).getLocation() : ""));
                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().grade,
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
                            Log.d("jsonObject", "grade value: " + jsonObjectLoginResponse);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                            setLocationAndGradeVisibility(true);
                                            JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("grade");
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                gradeObjectArrayList.add(new GradeObject(
                                                        jsonArray.getJSONObject(i).getString("id"),
                                                        "",
                                                        jsonArray.getJSONObject(i).getString("grade")
                                                ));
                                            }
                                            setUpGradeSpinner();
                                        } else {
                                            setLocationAndGradeVisibility(false);
                                            showSnackBar("No Record is found!");
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

    private void setUpGradeSpinner() {
        ArrayAdapter<GradeObject> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, gradeObjectArrayList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stockFragmentGrade.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void checkingBeforeDeduction() {
        if (productObject != null && farmerObject != null && !stockFragmentDate.getText().toString().equals("") && !stockFragmentDeduceWeight.getText().toString().equals("") && !poId.equals("")) {
            if (SharedPreferenceManager.getGrade(getActivity())) {
                if (gradeObjectArrayList.size() <= 0) {
                    showSnackBar("Please select a grade!");
                    return;
                }
            }

            if (SharedPreferenceManager.getLocation(getActivity())) {
                if (locationObjectArrayList.size() <= 0) {
                    showSnackBar("Please select a location!");
                    return;
                }
            }
            /*
             * everything okay then perform deduction
             * */
            showProgressBar(true);
            performDeduction();
        } else {
            showSnackBar("Please select all the fields above!");
        }
    }

    /*
     * fetch po_id is grade and location setting is close
     * */
    private void fetchPoID() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("read", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("date", stockFragmentDate.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("farmer_id", farmerObject.getId()));
                apiDataObjectArrayList.add(new ApiDataObject("product_id", productObject.getId()));
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
                            Log.d("jsonObject", "location value: " + jsonObjectLoginResponse);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                            poId = jsonObjectLoginResponse.getJSONArray("po_id").getJSONObject(0).getString("id");
                                        } else {
                                            setLocationAndGradeVisibility(false);
                                            showSnackBar("No Record is found!");
                                        }

                                    } catch (
                                            JSONException e) {
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

    /*
     *perform deduction
     * */
    private void performDeduction() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("create", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("po_id", poId));
                apiDataObjectArrayList.add(new ApiDataObject("product_id", productObject.getId()));
                apiDataObjectArrayList.add(new ApiDataObject("weight", stockFragmentDeduceWeight.getText().toString()));
                apiDataObjectArrayList.add(new ApiDataObject("location", SharedPreferenceManager.getLocation(getActivity()) ? locationObjectArrayList.get(stockFragmentLocation.getSelectedItemPosition()).getLocationId() : ""));
                apiDataObjectArrayList.add(new ApiDataObject("grade", SharedPreferenceManager.getGrade(getActivity()) ? gradeObjectArrayList.get(stockFragmentGrade.getSelectedItemPosition()).getGradeId() : ""));

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
                            Log.d("jsonObject", "deduction: " + jsonObjectLoginResponse);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                            setLocationAndGradeVisibility(false);
                                            deductionReset();
                                            showSnackBar("Deduce Successfully!");
                                        } else {
                                            CustomToast(getActivity(), "Something Went Wrong!");
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

    private void deductionReset() {
        productObject = null;
        farmerObject = null;

        stockFragmentProduct.setText("Please Select A Item Here");
        stockFragmentFarmer.setText("Please Select A Farmer Here");
    }

    private void clearSpinnerItem() {
        locationObjectArrayList.clear();
        gradeObjectArrayList.clear();
    }

    /*
     * call back from stock detail dialog
     * */
    @Override
    public void selectedItemForDeduction(StockObject stockObject, String date) {
        deductionLayout.setState(BottomSheetBehavior.STATE_EXPANDED);
        farmerObject = new FarmerObject(stockObject.getTarget_id(), stockObject.getTarget());
        productObject = new ProductObject(productObjectArrayList.get(groupPosition).getId(), productObjectArrayList.get(groupPosition).getName());

        stockFragmentDate.setText(date);
        stockFragmentFarmer.setText(farmerObject.getName());
        stockFragmentProduct.setText(productObject.getName());
        checkLocationAndGradeSetting();
    }


    // ------------------------------------------------------sorting purpose------------------------------------------------------------------------
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.getId() == R.id.fragment_stock_sort) {
            if (jsonArray != null) {
                productObjectArrayList.clear();
                sorting();
            }
        } else if (adapterView.getId() == R.id.fragment_stock_location) {
            if (SharedPreferenceManager.getGrade(getActivity())) {
                poId = locationObjectArrayList.get(i).getPoId();
                showProgressBar(true);
                gradeObjectArrayList.clear();
                fetchGrade();
            }
        } else if (adapterView.getId() == R.id.fragment_stock_grade) {
            if (SharedPreferenceManager.getGrade(getActivity())) {
                poId = gradeObjectArrayList.get(i).getPoId();
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
    public void showSnackBar(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
                snackbar.setAction("Dismiss", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                    }
                });
                snackbar.show();
            }
        });
    }

    private void showProgressBar(boolean show) {
        try {
            ((MainActivity) Objects.requireNonNull(getActivity())).showProgressBar(show);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    private void notifyDataSetChanged() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                productAdapter.notifyDataSetChanged();
            }
        });
    }

    public boolean isBottomSheetOpen() {
        if (deductionLayout.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            deductionLayout.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return true;
        }
        return false;
    }

    @Override
    public void onRefresh() {
        stockFragmentRefreshLayout.setRefreshing(false);
        reset();
    }

}
