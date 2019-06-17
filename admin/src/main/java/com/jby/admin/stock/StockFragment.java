package com.jby.admin.stock;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.jby.admin.MainActivity;
import com.jby.admin.R;
import com.jby.admin.adapter.stock.ProductExpandableAdapter;
import com.jby.admin.object.ProductDetailChildObject;
import com.jby.admin.object.ProductDetailParentObject;
import com.jby.admin.shareObject.AnimationUtility;
import com.jby.admin.shareObject.ApiDataObject;
import com.jby.admin.shareObject.ApiManager;
import com.jby.admin.shareObject.AsyncTaskManager;
import com.jby.admin.sharePreference.SharedPreferenceManager;
import com.jby.admin.stock.dialog.AssignProductDialog;
import com.jby.admin.stock.dialog.DriverDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;
import static android.support.design.widget.BottomSheetBehavior.STATE_EXPANDED;
import static android.support.design.widget.BottomSheetBehavior.STATE_HIDDEN;
import static com.jby.admin.shareObject.CustomToast.CustomToast;


public class StockFragment extends Fragment implements AdapterView.OnItemSelectedListener, SearchView.OnQueryTextListener,
        ProductExpandableAdapter.ProductExpandableAdapterCallBack, ExpandableListView.OnGroupClickListener,
        AssignProductDialog.AssignProductDialogCallBack, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener,
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
    /*
     * delivery purpose
     * */
    private BottomSheetBehavior stockFragmentDeliveryLayout;
    private List<String> deliveryProductIDList;
    private Button stockFragmentAssignButton;
    private int orderPriority = 1000, tempTakenQuantity = 0;
    /*
     * default driver
     * */
    private LinearLayout stockFragmentDefaultDriverLayout;
    private ImageView stockFragmentDefaultDriverLayoutIndicator;
    private TextView stockFragmentDefaultDriver;
    private String defaultDriverId = "", defaultDriver = "";
    private String lastDriverId, lastDriver;
    /*
     * date
     * */
    private LinearLayout stockFragmentDateLayout;
    private TextView stockFragmentDate;
    private String date = "", do_id = "";
    private String lastDate = "";
    /*
     * stock unavailable
     * */
    private ArrayList<ProductDetailParentObject> unavailableStockArrayList;
    private int unavailablePosition = -1;
    /*
     * stock control list
     * */
    private ArrayList<ProductDetailParentObject> stockControlArrayList;

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
         * driver and date layout (bottom sheet) purpose
         * */
        stockFragmentDeliveryLayout = BottomSheetBehavior.from(rootView.findViewById(R.id.fragment_stock_delivery_layout));
        stockFragmentAssignButton = rootView.findViewById(R.id.fragment_stock_assign_button);
        stockFragmentDefaultDriverLayout = rootView.findViewById(R.id.fragment_stock_default_driver_layout);
        stockFragmentDefaultDriver = rootView.findViewById(R.id.fragment_stock_default_driver);
        stockFragmentDefaultDriverLayoutIndicator = rootView.findViewById(R.id.fragment_stock_delivery_layout_indicator);

        stockFragmentDateLayout = rootView.findViewById(R.id.fragment_stock_date_layout);
        stockFragmentDate = rootView.findViewById(R.id.fragment_stock_date);

        deliveryProductIDList = new ArrayList<>();
        unavailableStockArrayList = new ArrayList<>();
        stockControlArrayList = new ArrayList<>();

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

        stockFragmentAssignButton.setOnClickListener(this);
        stockFragmentDefaultDriverLayout.setOnClickListener(this);
        stockFragmentDateLayout.setOnClickListener(this);
        stockFragmentDefaultDriverLayoutIndicator.setOnClickListener(this);

        stockFragmentRefreshLayout.setOnRefreshListener(this);
        //driver and date layout
        setUpBottomSheet();
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
            case R.id.fragment_stock_assign_button:
                showProgressBar(true);
                checkingStockBeforeUpdate();
                break;
            case R.id.fragment_stock_default_driver_layout:
                openDriverDialog();
                break;
            case R.id.fragment_stock_date_layout:
                openDatePicker();
                break;
            case R.id.fragment_stock_delivery_layout_indicator:
                stockFragmentDeliveryLayout.setState(stockFragmentDeliveryLayout.getState() == STATE_COLLAPSED ? STATE_EXPANDED : STATE_COLLAPSED);
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
        //reset order priority
        orderPriority = 1000;
        //delivery list
        deliveryProductIDList.clear();
        //unavailable list
        unavailableStockArrayList.clear();
        productAdapter.clearUnavailableList();
        //stock control list
        stockControlArrayList.clear();
        productAdapter.clearStockControlArrayList();
        //reset date
        stockFragmentDateLayout.setVisibility(View.GONE);

        hideBottomSheetLayout(true);
        showProgressBar(true);
        closeOtherChildView(-1);
        fetchParentItem(query);
    }

    private void fetchParentItem(final String query) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                productAdapter.setUnavailableProduct(unavailableStockArrayList);

                productObjectArrayList.clear();

                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("fetch", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("customer_id", ((MainActivity) getActivity()).getCustomerID()));
                apiDataObjectArrayList.add(new ApiDataObject("do_id", do_id));
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
                                            if (!((MainActivity) getActivity()).getCustomerID().equals("-1")) {
                                                /*
                                                 * default driver detail
                                                 * */
                                                JSONObject object = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONObject("driver_detail");
                                                lastDriverId = defaultDriverId = object.getString("driver_id");
                                                lastDriver = object.getString("name");
                                                stockFragmentDefaultDriver.setText(object.getString("name"));
                                                /*
                                                 * latest DO detail
                                                 * */
                                                lastDate = date = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONObject("do_detail").getString("date");
                                                //show bottom sheet when update
                                                if (!do_id.equals("")) {
                                                    hideBottomSheetLayout(false);
                                                    setDefaultDate();
                                                }
                                            }
                                            /*
                                             * product list
                                             * */
                                            jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("stock");
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

    //--------------------------------------parent----------
    private void setParentValue(int i) {
        try {
            //mean this id is never added yet so create a new group view
            productObjectArrayList.add(new ProductDetailParentObject(
                    jsonArray.getJSONObject(i).getString("id"),
                    jsonArray.getJSONObject(i).getString("name"),
                    jsonArray.getJSONObject(i).getString("picture"),
                    jsonArray.getJSONObject(i).getString("type"),
                    jsonArray.getJSONObject(i).getString("price"),
                    jsonArray.getJSONObject(i).getString("available_quantity"),
                    jsonArray.getJSONObject(i).getString("taken_quantity"),
                    jsonArray.getJSONObject(i).getString("product_code")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onGroupClick(ExpandableListView expandableListView, View view, final int i, long l) {
        groupPosition = i;
        if (!productObjectArrayList.get(i).getAvailable_quantity().equals("0") || !productObjectArrayList.get(i).getTaken_quantity().equals("0")
                && !((MainActivity) getActivity()).getCustomerID().equals("-1")) {

            if (expandableListView.isGroupExpanded(i)) expandableListView.collapseGroup(i);
            else {
                //close view
                closeOtherChildView(i);
                productObjectArrayList.get(i).getProductDetailChildObjectArrayList().clear();
                showProgressBar(true);
                /*
                 * fetch child item
                 * */
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        fetchChildItem(i);
                    }
                }, 200);

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

//    --------------------------------------child-------------

    private void setChildValue(JSONArray jsonArray, int position) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                productObjectArrayList.get(position).setProductDetailChildObjectArrayList(setChildObject(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void fetchChildItem(int position) {
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("product_id", productObjectArrayList.get(position).getId()));
        apiDataObjectArrayList.add(new ApiDataObject("do_id", do_id));
        apiDataObjectArrayList.add(new ApiDataObject("unavailable_list", getUnavailableID()));
        Log.d("haha", "haha: id:: " + getUnavailableID());
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
                    Log.d("jsonObject", "jsonObject: " + jsonObjectLoginResponse);
                    if (jsonObjectLoginResponse.getString("status").equals("1")) {
                        JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("farmer_stock");
                        setChildValue(jsonArray, position);
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
        stockFragmentProductList.expandGroup(position);
        stockFragmentProductList.setSelectedGroup(position);
        showProgressBar(false);
    }

    private ProductDetailChildObject setChildObject(JSONObject jsonObject) {
        ProductDetailChildObject object = null;
        try {
            object = new ProductDetailChildObject(
                    jsonObject.getString("id"),
                    jsonObject.getString("farmer_name"),
                    jsonObject.getString("available_quantity"),
                    jsonObject.getString("taken_quantity"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    /*----------------------------------------------------------taken and available quantity control----------------------------------------------------*/

    public void groupQuantityControl(String status) {
        int availableQuantity = Integer.valueOf(productObjectArrayList.get(groupPosition).getAvailable_quantity());
        int takenQuantity = Integer.valueOf(productObjectArrayList.get(groupPosition).getTaken_quantity());

        if (status.equals("1")) {
            availableQuantity--;
            takenQuantity++;
        } else {
            if (takenQuantity > 0) {
                availableQuantity++;
                takenQuantity--;
            }
        }
        productObjectArrayList.get(groupPosition).setAvailable_quantity(String.valueOf(availableQuantity));
        productObjectArrayList.get(groupPosition).setTaken_quantity(String.valueOf(takenQuantity));
        /*
         * stock into stock control list
         * */
        checkStockControlParentItem(String.valueOf(availableQuantity), String.valueOf(takenQuantity));
        productAdapter.setStockControlArrayList(stockControlArrayList);
        /*
         * for order priority purpose
         * */
        if (!productObjectArrayList.get(groupPosition).getTaken_quantity().equals("0"))
            productObjectArrayList.get(groupPosition).setPriority(orderPriority--);
        else
            productObjectArrayList.get(groupPosition).setPriority(0);
    }

    private void checkStockControlParentItem(String availableQuantity, String takenQuantity) {
        if (stockControlArrayList.size() > 0) {
            boolean isFound = false;
            for (int i = 0; i < stockControlArrayList.size(); i++) {
                if (stockControlArrayList.get(i).getId().equals(productObjectArrayList.get(groupPosition).getId())) {
                    stockControlArrayList.get(i).setAvailable_quantity(availableQuantity);
                    stockControlArrayList.get(i).setTaken_quantity(takenQuantity);
                    isFound = true;
                    break;
                }
            }
            if (!isFound) setStockControlArrayList(availableQuantity, takenQuantity);

        } else {
            setStockControlArrayList(availableQuantity, takenQuantity);
        }
    }

    private void setStockControlArrayList(String availableQuantity, String takenQuantity) {
        stockControlArrayList.add(new ProductDetailParentObject(
                productObjectArrayList.get(groupPosition).getId(),
                availableQuantity,
                takenQuantity));
    }

    private void childQuantityControl(String status) {
        int availableQuantity = Integer.valueOf(productObjectArrayList.get(groupPosition).getProductDetailChildObjectArrayList().get(childPosition).getQuantity());
        int takenQuantity = Integer.valueOf(productObjectArrayList.get(groupPosition).getProductDetailChildObjectArrayList().get(childPosition).getTakenQuantity());

        if (status.equals("1")) {
            availableQuantity--;
            takenQuantity++;
        } else {
            if (takenQuantity > 0) {
                availableQuantity++;
                takenQuantity--;
            }
        }
        productObjectArrayList.get(groupPosition).getProductDetailChildObjectArrayList().get(childPosition).setQuantity(String.valueOf(availableQuantity));
        productObjectArrayList.get(groupPosition).getProductDetailChildObjectArrayList().get(childPosition).setTakenQuantity(String.valueOf(takenQuantity));

        /*
         * stock into stock control list
         * */
        for (int i = 0; i < stockControlArrayList.size(); i++) {
            if (stockControlArrayList.get(i).getId().equals(productObjectArrayList.get(groupPosition).getId())) {
                boolean farmerIsFound = false;

                for (int j = 0; j < stockControlArrayList.get(i).getProductDetailChildObjectArrayList().size(); j++) {
                    String farmerId = stockControlArrayList.get(i).getProductDetailChildObjectArrayList().get(j).getFarmerID();
                    if (farmerId.equals(productObjectArrayList.get(groupPosition).getProductDetailChildObjectArrayList().get(childPosition).getFarmerID())) {
                        farmerIsFound = true;
                        stockControlArrayList.get(i).getProductDetailChildObjectArrayList().get(j).setQuantity(String.valueOf(availableQuantity));
                        stockControlArrayList.get(i).getProductDetailChildObjectArrayList().get(j).setTakenQuantity(String.valueOf(takenQuantity));
                        break;
                    }
                }
                if (!farmerIsFound)
                    stockControlArrayList.get(i).getProductDetailChildObjectArrayList().add(new ProductDetailChildObject(
                            productObjectArrayList.get(groupPosition).getProductDetailChildObjectArrayList().get(childPosition).getFarmerID(),
                            String.valueOf(availableQuantity),
                            String.valueOf(takenQuantity)));

                productAdapter.setStockControlArrayList(stockControlArrayList);
                break;
            }
        }

        productAdapter.setProductDetailParentObjectArrayList(productObjectArrayList);
    }

    public void updateListViewQuantity(String status) {
        groupQuantityControl(status);
        childQuantityControl(status);
    }

    /*
     * update (unavailable id) list every time user's click on the crashed item
     * */
    public void updateUnavailableStockArrayList(final String selectedID, final String farmerID) {
        try {
            List<String> tempList = new ArrayList<>();
            /*
             * for checking unavailable stock's id
             * */
            String[] unavailableIDList = unavailableStockArrayList.get(unavailablePosition).getUnavailableID();
            int initialLength = unavailableIDList.length;

            for (int i = 0; i < unavailableIDList.length; i++) {
                if (!unavailableIDList[i].equals(selectedID)) {
                    tempList.add(unavailableIDList[i]);
                }
            }
            unavailableIDList = tempList.toArray(new String[tempList.size()]);
            unavailableStockArrayList.get(unavailablePosition).setUnavailableID(unavailableIDList);

            if (unavailableIDList.length <= 0) {
                unavailableStockArrayList.remove(unavailablePosition);
                productAdapter.setUnavailableProduct(unavailableStockArrayList);
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    /*--------------------------------------------------------assign delivery order ---------------------------------------------------------------*/
    @Override
    public void childOnClick(final int position, int groupPosition) {
        this.childPosition = position;
        this.groupPosition = groupPosition;
        tempTakenQuantity = Integer.parseInt(productObjectArrayList.get(groupPosition).getTaken_quantity());
        Bundle bundle = new Bundle();
        try {
            bundle.putString("customer_id", ((MainActivity) getActivity()).getCustomerID());
            bundle.putString("do_id", do_id);
            bundle.putString("farmer_id", productObjectArrayList.get(groupPosition).getProductDetailChildObjectArrayList().get(position).getFarmerID());
            bundle.putString("farmer_name", productObjectArrayList.get(groupPosition).getProductDetailChildObjectArrayList().get(position).getFarmerName());
            bundle.putString("product_id", productObjectArrayList.get(groupPosition).getId());
            bundle.putStringArrayList("delivery_product_list_id", (ArrayList<String>) deliveryProductIDList);
            bundle.putStringArray("unavailable_list", unavailableStockArrayList.get(unavailablePosition).getUnavailableID());
        } catch (IndexOutOfBoundsException e) {
            Log.d("StockFragment", "Some list are null!");
        }

        dialogFragment = new AssignProductDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getChildFragmentManager(), "");
    }

    public void setDeliveryProductIDList(String id, String deliveryOrderID) {
        if (deliveryProductIDList.contains(id)) deliveryProductIDList.remove(id);
        else deliveryProductIDList.add(id);

        if (deliveryProductIDList.size() <= 0) {
            hideBottomSheetLayout(true);
//            //clear unavailable list when user deselect all the crash item
//            unavailableStockArrayList.clear();
//            productAdapter.setUnavailableProduct(unavailableStockArrayList);
        } else hideBottomSheetLayout(false);

        setDefaultDate();
    }

    private String getDeliveryProductID() {
        return deliveryProductIDList.toString().substring(1, deliveryProductIDList.toString().length() - 1);
    }

    private String getUnavailableID() {
        try {
            for (int i = 0; i < unavailableStockArrayList.size(); i++) {
                if (unavailableStockArrayList.get(i).getId().equals(productObjectArrayList.get(groupPosition).getId())) {
                    unavailablePosition = i;
                    Log.d("haha", "position:  " + unavailablePosition);
                    String unavailableID = Arrays.toString(unavailableStockArrayList.get(i).getUnavailableID());
                    return unavailableID.substring(1, unavailableID.length() - 1);
                }
            }
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
        return "";
    }

    private void checkingStockBeforeUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("id", getDeliveryProductID()));
                apiDataObjectArrayList.add(new ApiDataObject("admin_id", SharedPreferenceManager.getUserId(getActivity())));

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
                            /*
                             * confict occur
                             * */
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                //close all view
                                closeOtherChildView(-1);
                                //reset
                                unavailableStockArrayList.clear();
                                //toast
                                CustomToast(getActivity(), "Stock not found!");

                                JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("unavailable_stock");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    unavailableStockArrayList.add(new ProductDetailParentObject(
                                            jsonArray.getJSONObject(i).getString("product_id"),
                                            (jsonArray.getJSONObject(i).getString("id").split(",")),
                                            (jsonArray.getJSONObject(i).getString("farmer_id").split(","))));
                                }
                                /*
                                 * show bottom sheet
                                 * */
                                hideBottomSheetLayout(false);
                                productAdapter.setUnavailableProduct(unavailableStockArrayList);
                                notifyDataSetChanged();
                                showProgressBar(false);
                            }
                            /*
                             * continue to assign vege
                             * */
                            else if (jsonObjectLoginResponse.getString("status").equals("2")) {
                                assignVegetable();
                            } else if (jsonObjectLoginResponse.getString("status").equals("3")) {
                                checkingStockBeforeUpdate();
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

    private void assignVegetable() {
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("id", getDeliveryProductID()));
        apiDataObjectArrayList.add(new ApiDataObject("do_id", do_id));
        apiDataObjectArrayList.add(new ApiDataObject("customer_id", ((MainActivity) getActivity()).getCustomerID()));
        apiDataObjectArrayList.add(new ApiDataObject("driver_id", defaultDriverId));
        apiDataObjectArrayList.add(new ApiDataObject("type", productObjectArrayList.get(groupPosition).getType()));
        apiDataObjectArrayList.add(new ApiDataObject("date", date + " " + getCurrentTime()));
        apiDataObjectArrayList.add(new ApiDataObject("admin_id", SharedPreferenceManager.getUserId(getActivity())));
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
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showSnackBar("Delivery order is created!");
                            //fetch item (customer id = -1)
                            resetCustomerDetail();
                            //reset list view
                            reset();
                            //reset date
                            resetDate();
                        }
                    });
                    Log.d("jsonObject", "jsonObject: Assign: " + jsonObjectLoginResponse);
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
            }
        }
        showProgressBar(false);
    }

    private void resetCustomerDetail() {
        ((MainActivity) Objects.requireNonNull(getActivity())).setCustomerID("-1");
        ((MainActivity) getActivity()).actionBarCustomer.setText("Select a Customer");
    }

    public void setDo_id(String do_id) {
        this.do_id = do_id;
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

    public void orderByPriority() {
        try {
            Collections.sort(productObjectArrayList, new Comparator<ProductDetailParentObject>() {
                @Override
                public int compare(ProductDetailParentObject object1, ProductDetailParentObject object2) {
                    return Integer.compare(object2.getPriority(), object1.getPriority());
                }
            });
            productAdapter.notifyDataSetChanged();
        } catch (NullPointerException e) {
            CustomToast(getActivity(), "Failed to sorting!");
        }
    }

    /*-------------------------------------------------------------default driver purpose---------------------------------------------------------------*/
    private void setUpBottomSheet() {
        //set hidden at begin
        stockFragmentDeliveryLayout.setState(STATE_HIDDEN);
        stockFragmentDeliveryLayout.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {
                // Check Logs to see how bottom sheets behaves
                switch (newState) {
                    case STATE_COLLAPSED:
                        setLayoutIndicator(true);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case STATE_EXPANDED:
                        setLayoutIndicator(false);
                        break;
                    case STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    private void setMargin(boolean set) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) stockFragmentProductList.getLayoutParams();
        params.bottomMargin = (set ? ((MainActivity) Objects.requireNonNull(getActivity())).getActionBarHeight() : 0);
    }

    private void hideBottomSheetLayout(final boolean hide) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stockFragmentDeliveryLayout.setHideable(hide);
                stockFragmentDeliveryLayout.setState(hide ? BottomSheetBehavior.STATE_HIDDEN : STATE_COLLAPSED);
                /*
                 * set margin for list view
                 * */
                setMargin(!hide);
            }
        });
    }

    private void setLayoutIndicator(boolean collapsed) {
        stockFragmentDefaultDriverLayoutIndicator.setImageDrawable(collapsed ? Objects.requireNonNull(getActivity()).getResources().getDrawable(R.drawable.arrow_up) : getActivity().getResources().getDrawable(R.drawable.arrow_down));
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
        lastDriverId = id;
        lastDriver = name;
        /*
         * update DO's driver
         * */
        if (!do_id.equals("")) changeDriverConfirmationDialog();
            /*
             * simply choose the driver
             * */
        else {
            defaultDriverId = id;
            defaultDriver = name;
            stockFragmentDefaultDriver.setText(name);
        }
    }

    public void changeDriverConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Warning");
        builder.setMessage("Are you sure that you want to change the driver of #DO" + setDoPlaceHolder(do_id) + "?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "I'm Sure",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        showProgressBar(true);
                        defaultDriver = lastDriver;
                        defaultDriverId = lastDriverId;
                        updateDoDriver();
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

    private void updateDoDriver() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("do_id", do_id));
                apiDataObjectArrayList.add(new ApiDataObject("driver_id", defaultDriverId));
                apiDataObjectArrayList.add(new ApiDataObject("edit_do_driver", "1"));
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
                                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showSnackBar("Update Successfully");
                                        stockFragmentDefaultDriver.setText(defaultDriver);
                                    }
                                });
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
                showProgressBar(false);
            }
        }).start();
    }

    /*--------------------------------------------------------date purpose------------------------------------------------------------------------------*/
    private void setDefaultDate() {
        //this will be only called once when the date layout is in (GONE state)
        if (stockFragmentDateLayout.getVisibility() == View.GONE) {
            new AnimationUtility().fadeInVisible(getActivity(), stockFragmentDateLayout);
            date = date.equals("") ? (String) android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()) : date;
            stockFragmentDate.setText(isToday(date));
        }
    }

    private String isToday(String date) {
        return date.contentEquals(android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date())) ? "Today" : date;
    }

    private String getCurrentTime() {
        return String.valueOf(android.text.format.DateFormat.format("HH:mm:ss", new java.util.Date()));
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
                        lastDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, (monthOfYear + 1), dayOfMonth);
                        /*
                         * if do_id != null mean that the DO is created update date is performed here
                         * */
                        if (!do_id.equals("")) changeDateConfirmationDialog();
                            /*
                             * set date otherwise
                             * */
                        else {
                            date = String.format(Locale.getDefault(), "%d-%02d-%02d", year, (monthOfYear + 1), dayOfMonth);
                            stockFragmentDate.setText(date);
                        }
                    }
                }, mYear, mMonth, mDay);
        datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
        datePickerDialog.show();
    }

    public void resetDate() {
        stockFragmentDateLayout.setVisibility(View.GONE);
        do_id = date = lastDate = "";
    }

    public void changeDateConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Warning");
        builder.setMessage("This will change all the item's date in #DO" + setDoPlaceHolder(do_id) + "\n Do you want to proceed?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Proceed",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        showProgressBar(true);
                        date = lastDate;
                        updateDoDate();
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

    private String setDoPlaceHolder(String do_id) {
        StringBuilder do_idBuilder = new StringBuilder(do_id);
        for (int i = do_idBuilder.length(); i < 5; i++) {
            do_idBuilder.insert(0, "0");
        }
        return do_idBuilder.toString();
    }

    private void updateDoDate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("do_id", do_id));
                apiDataObjectArrayList.add(new ApiDataObject("date", date + " " + getCurrentTime()));
                apiDataObjectArrayList.add(new ApiDataObject("edit_do_date", "1"));

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
                                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showSnackBar("Update Successfully");
                                        stockFragmentDate.setText(date);
                                    }
                                });
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
                showProgressBar(false);
            }
        }).start();
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
        if (deliveryProductIDList.size() > 0 || !do_id.equals("")) {
            refreshConfirmationDialog();
        } else {
            resetCustomerDetail();
            reset();
            resetDate();
        }
        stockFragmentRefreshLayout.setRefreshing(false);
    }

    public void refreshConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Notice");
        builder.setMessage(do_id.equals("") ? "All selected item will be clear once you refresh!" : "Stop doing update?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        do_id = date = lastDate = "";
                        deliveryProductIDList.clear();
                        onRefresh();
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
}
