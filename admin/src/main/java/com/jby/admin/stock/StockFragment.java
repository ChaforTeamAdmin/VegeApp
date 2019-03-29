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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.admin.MainActivity;
import com.jby.admin.R;
import com.jby.admin.adapter.ProductExpandableAdapter;
import com.jby.admin.object.ProductDetailChildObject;
import com.jby.admin.object.ProductDetailParentObject;
import com.jby.admin.others.SwipeDismissTouchListener;
import com.jby.admin.shareObject.ApiDataObject;
import com.jby.admin.shareObject.ApiManager;
import com.jby.admin.shareObject.AsyncTaskManager;
import com.jby.admin.stock.dialog.AssignProductDialog;

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
        AssignProductDialog.AssignProductDialogCallBack, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;

    private View rootView;
    private Spinner stockFragmentSpinner;
    private SearchView stockFragmentSearch;
    //list
    private SwipeRefreshLayout stockFragmentRefreshLayout;
    private ExpandableListView stockFragmentProductList;
    private ProductExpandableAdapter productAdapter;
    private ArrayList<ProductDetailParentObject> productObjectArrayList;
    private JSONArray jsonArray;
    private int groupPosition = -1, childPosition = -1;
    //sorting and search purpose
    private String query = "";
    //dialog
    private DialogFragment dialogFragment;
    private FragmentManager fm;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private Handler handler;
    //delivery order purpose
    private List<String> deliveryProductIDList;
    private Button stockFragmentAssignButton;
    private String deliveryOrderID = "0";

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
        stockFragmentAssignButton = rootView.findViewById(R.id.fragment_stock_assign_button);
        deliveryProductIDList = new ArrayList<>();

        productObjectArrayList = new ArrayList<>();
        productAdapter = new ProductExpandableAdapter(getActivity(), productObjectArrayList, this);
        handler = new Handler();
        fm = getChildFragmentManager();

    }

    private void objectSetting() {
        stockFragmentProductList.setAdapter(productAdapter);
        stockFragmentProductList.setOnGroupClickListener(this);

        stockFragmentSpinner.setOnItemSelectedListener(this);
        stockFragmentSearch.setOnQueryTextListener(this);

        stockFragmentAssignButton.setOnClickListener(this);

        stockFragmentRefreshLayout.setOnRefreshListener(this);

        searchViewSetting();
        setupSpinner();
        showProgressBar(true);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchParentItem();
            }
        }, 200);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fragment_stock_assign_button:
                assignVegetable();
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
        showProgressBar(true);
        closeOtherChildView(-1);
        productObjectArrayList.clear();
        fetchParentItem();
        //delivery list
        deliveryProductIDList.clear();
        stockFragmentAssignButton.setVisibility(View.GONE);
    }

    private void fetchParentItem() {
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("fetch", "1"));
        apiDataObjectArrayList.add(new ApiDataObject("customer_id", ((MainActivity) getActivity()).getCustomerID()));

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
                        jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("stock");
                        sorting();
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
    }

    //--------------------------------------parent----------
    private void sorting() {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                String name = jsonArray.getJSONObject(i).getString("name");
                //search value
                if (name.contains(query)) {
                    //sorting by spinner
                    switch (stockFragmentSpinner.getSelectedItemPosition()) {
                        case 0:
                            setParentValue(i);
                            break;
                        case 1:
                            if (!jsonArray.getJSONObject(i).getString("current_quantity").equals("0"))
                                setParentValue(i);
                            break;
                        case 2:
                            if (jsonArray.getJSONObject(i).getString("current_quantity").equals("0"))
                                setParentValue(i);
                            break;
                    }
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
                    jsonArray.getJSONObject(i).getString("taken_quantity")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void parentOnCluck(int position) {

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

    private void closeOtherChildView(int position) {
        for (int i = 0; i < productObjectArrayList.size(); i++) {
            if (i != position) stockFragmentProductList.collapseGroup(i);
        }
    }

    public void groupQuantityControl(String status) {
        View v = stockFragmentProductList.getChildAt(groupPosition - stockFragmentProductList.getFirstVisiblePosition());
        if (v == null) return;
        TextView tvQuantity = v.findViewById(R.id.product_parent_list_view_item_quantity);
        TextView tvTakenQuantity = v.findViewById(R.id.product_parent_list_view_item_taken_quantity);
        LinearLayout takenLayout = v.findViewById(R.id.product_parent_list_view_item_taken_layout);

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
        if (takenQuantity <= 0) takenLayout.setVisibility(View.GONE);
        else takenLayout.setVisibility(View.VISIBLE);

        productObjectArrayList.get(groupPosition).setAvailable_quantity(String.valueOf(availableQuantity));
        productObjectArrayList.get(groupPosition).setTaken_quantity(String.valueOf(takenQuantity));


        tvQuantity.setText(" x " + String.valueOf(availableQuantity));
        tvTakenQuantity.setText(" x " + String.valueOf(takenQuantity));

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
        apiDataObjectArrayList.add(new ApiDataObject("customer_id", ((MainActivity) getActivity()).getCustomerID()));
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
        productAdapter.notifyDataSetChanged();
    }

    //--------------------------------------stock detail-------------------------
    @Override
    public void childOnClick(final int position, int groupPosition) {
        this.childPosition = position;
        Bundle bundle = new Bundle();
        bundle.putString("customer_id", ((MainActivity) getActivity()).getCustomerID());
        bundle.putString("farmer_id", productObjectArrayList.get(groupPosition).getProductDetailChildObjectArrayList().get(position).getFarmerID());
        bundle.putString("farmer_name", productObjectArrayList.get(groupPosition).getProductDetailChildObjectArrayList().get(position).getFarmerName());
        bundle.putString("product_id", productObjectArrayList.get(groupPosition).getId());
        bundle.putStringArrayList("delivery_product_list_id", (ArrayList<String>) deliveryProductIDList);

        dialogFragment = new AssignProductDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getChildFragmentManager(), "");

    }

    public void updateListViewQuantity(String status) {
        groupQuantityControl(status);
        childQuantityControl(status);
    }

    /*--------------------------------------------------------assign delivery order ---------------------------------------------------------------*/
    public void setDeliveryProductIDList(String id, String deliveryOrderID) {
        if (deliveryProductIDList.contains(id)) deliveryProductIDList.remove(id);
        else deliveryProductIDList.add(id);

        stockFragmentAssignButton.setVisibility(deliveryProductIDList.size() > 0 ? View.VISIBLE : View.GONE);
        this.deliveryOrderID = deliveryOrderID;
    }

    private String getDeliveryProductID() {
        return deliveryProductIDList.toString().substring(1, deliveryProductIDList.toString().length() - 1);
    }

    private void assignVegetable() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("id", getDeliveryProductID()));
                apiDataObjectArrayList.add(new ApiDataObject("do_id", deliveryOrderID));
                apiDataObjectArrayList.add(new ApiDataObject("customer_id", ((MainActivity) getActivity()).getCustomerID()));
                apiDataObjectArrayList.add(new ApiDataObject("type", productObjectArrayList.get(groupPosition).getType()));
                Log.d("haha" ,"order: " + deliveryOrderID);
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
                                    stockFragmentAssignButton.setVisibility(View.GONE);
                                    deliveryProductIDList.clear();
                                    showSnackBar("Delivery order is created!");
                                }
                            });
                            Log.d("jsonObject", "jsonObject: " + jsonObjectLoginResponse);
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
            }
        }).start();
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
    public boolean onQueryTextChange(String newText) {
        query = newText;
        if ((((MainActivity) Objects.requireNonNull(getActivity())).mainActivityProgressBar.getVisibility() != View.VISIBLE))
            showProgressBar(true);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                productObjectArrayList.clear();
                sorting();
            }
        }, 700);
        return false;
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

    @Override
    public void onRefresh() {
        reset();
        stockFragmentRefreshLayout.setRefreshing(false);
    }

}
