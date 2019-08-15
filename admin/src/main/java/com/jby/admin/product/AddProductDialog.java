package com.jby.admin.product;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.jby.admin.R;
import com.jby.admin.database.CustomSqliteHelper;
import com.jby.admin.database.FrameworkClass;
import com.jby.admin.database.ResultCallBack;
import com.jby.admin.object.StockObject;
import com.jby.admin.object.entity.GradeObject;
import com.jby.admin.object.entity.LocationObject;
import com.jby.admin.object.product.ProductObject;
import com.jby.admin.others.KeyboardHelper;
import com.jby.admin.others.NetworkConnection;
import com.jby.admin.others.SwipeDismissTouchListener;
import com.jby.admin.shareObject.ApiDataObject;
import com.jby.admin.shareObject.ApiManager;
import com.jby.admin.shareObject.AsyncTaskManager;
import com.jby.admin.sharePreference.SharedPreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.admin.database.CustomSqliteHelper.TB_GRADE;
import static com.jby.admin.database.CustomSqliteHelper.TB_LOCATION;
import static com.jby.admin.shareObject.CustomToast.CustomToast;


public class AddProductDialog extends DialogFragment implements View.OnClickListener, RadioGroup.OnCheckedChangeListener, AdapterView.OnItemSelectedListener,
        ResultCallBack, TextView.OnEditorActionListener {
    View rootView;

    private TextView addProductDialogProduct;

    private LinearLayout addProductDialogAvailableStockLayout, addProductDialogLocationLayout, addProductDialogGradeLayout, addProductDialogPriceLayout;
    private Spinner addProductDialogAvailableStock, addProductDialogLocation, addProductDialogGrade;

    private EditText addProductDialogWeight, addProductDialogQuantity, addProductDialogPrice;

    private Button addProductDialogCancel, addProductDialogAdd;

    private LinearLayout addProductDialogWeightLayout;
    private ProgressBar addProductDialogProgressBar;
    private Handler handler;

    private String fragment = "";
    private boolean isUpdate = false;

    private ArrayList<StockObject> stockObjectArrayList = new ArrayList<>();
    private ArrayList<GradeObject> gradeObjectArrayList = new ArrayList<>();
    private ArrayList<LocationObject> locationObjectArrayList = new ArrayList<>();
    /*
     * local database
     * */
    FrameworkClass tbLocation, tbGrade;
    private boolean isLocation = true;
    //keyboard
    private KeyboardHelper keyboardHelper;
    private ProductObject productObject;

    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;

    AddProductDialogCallBack addProductDialogCallBack;

    public AddProductDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.add_product_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
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

    @Override
    public void dismiss() {
        addProductDialogCallBack.dismiss();
        KeyboardHelper.hideSoftKeyboard(getActivity(), addProductDialogWeight);
        super.dismiss();
    }

    private void objectInitialize() {
        addProductDialogCallBack = (AddProductDialogCallBack) getParentFragment();

        addProductDialogProduct = rootView.findViewById(R.id.add_product_dialog_product);

        addProductDialogAvailableStockLayout = rootView.findViewById(R.id.add_product_dialog_available_stock_layout);
        addProductDialogLocationLayout = rootView.findViewById(R.id.add_product_dialog_location_layout);
        addProductDialogGradeLayout = rootView.findViewById(R.id.add_product_dialog_grade_layout);
        addProductDialogPriceLayout = rootView.findViewById(R.id.add_product_dialog_price_layout);

        addProductDialogAvailableStock = rootView.findViewById(R.id.add_product_dialog_available_stock);
        addProductDialogLocation = rootView.findViewById(R.id.add_product_dialog_location);
        addProductDialogGrade = rootView.findViewById(R.id.add_product_dialog_grade);

        addProductDialogWeight = rootView.findViewById(R.id.add_product_dialog_weight);
        addProductDialogQuantity = rootView.findViewById(R.id.add_product_dialog_quantity);
        addProductDialogPrice = rootView.findViewById(R.id.add_product_dialog_price);

        addProductDialogCancel = rootView.findViewById(R.id.add_product_dialog_cancel);
        addProductDialogAdd = rootView.findViewById(R.id.add_product_dialog_add);

        addProductDialogGradeLayout = rootView.findViewById(R.id.add_product_dialog_grade_layout);

        addProductDialogWeightLayout = rootView.findViewById(R.id.add_product_dialog_weight_layout);

        addProductDialogProgressBar = rootView.findViewById(R.id.add_product_dialog_progress_bar);
        keyboardHelper = new KeyboardHelper();

        tbLocation = new FrameworkClass(getActivity(), this, new CustomSqliteHelper(getActivity()), TB_LOCATION);
        tbGrade = new FrameworkClass(getActivity(), this, new CustomSqliteHelper(getActivity()), TB_GRADE);

        handler = new Handler();
    }

    private void objectSetting() {
        addProductDialogPrice.setOnEditorActionListener(this);

        addProductDialogCancel.setOnClickListener(this);
        addProductDialogAdd.setOnClickListener(this);

        addProductDialogAvailableStock.setOnItemSelectedListener(this);
        addProductDialogLocation.setOnItemSelectedListener(this);
        addProductDialogGrade.setOnItemSelectedListener(this);
        /*
         * getting value
         * */
        preSetting();
        /*
         * checking network
         * */
        checkingNetwork();
        /*
         * request keyboard focus
         * */
        requestFocus();
    }

    private void preSetting() {
        Bundle bundle = getArguments();
        if (getArguments() != null) {
            fragment = getArguments().getString("fragment");
            isUpdate = getArguments().getBoolean("isUpdate");
            productObject = (ProductObject) bundle.getSerializable("product");

            addProductDialogProduct.setText(productObject.getName());
            if (isUpdate) {
                addProductDialogQuantity.setText(productObject.getQuantity());
                addProductDialogPrice.setText(productObject.getPrice());
            }
            /*
             * set price visibility
             * */
            addProductDialogPriceLayout.setVisibility(SharedPreferenceManager.getPrice(getActivity()) ? View.VISIBLE : View.GONE);
            /*
             * set quantity enter action
             */
            if (SharedPreferenceManager.getPrice(getActivity())) {
                //jump to price field
                addProductDialogQuantity.setNextFocusDownId(R.id.add_product_dialog_price);
            } else {
                //enter to submit
                addProductDialogQuantity.setOnEditorActionListener(this);
            }


            /*
             * set weight layout visibility (type == box ) then hide and set (weight fix to 1)
             * */
            setWeightVisibility();
        }
    }

    private void requestFocus() {
        if (productObject != null && productObject.getType().equals("box")) {
            addProductDialogQuantity.setText("");
            if (productObject.getQuantity() != null)
                addProductDialogQuantity.append(productObject.getQuantity());
            addProductDialogQuantity.setSelectAllOnFocus(true);
            addProductDialogQuantity.requestFocus();
        } else {
            if (productObject.getWeight() != null)
                addProductDialogWeight.append(productObject.getWeight());
            addProductDialogWeight.setSelectAllOnFocus(true);
            addProductDialogWeight.requestFocus();
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                KeyboardHelper.openSoftKeyboard(getActivity(), productObject != null && productObject.getType().equals("box") ? addProductDialogQuantity : addProductDialogWeight);
            }
        }, 300);
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        switch (textView.getId()) {
            case R.id.add_product_dialog_price:
                addProduct();
                return true;
            case R.id.add_product_dialog_quantity:
                /*
                 * if price is close then enter = add product
                 * */
                if (!SharedPreferenceManager.getPrice(getActivity())) addProduct();
                return true;
        }
        return false;
    }

    private void setWeightVisibility() {
        if (productObject.getType().equals("box")) {
            addProductDialogWeightLayout.setVisibility(View.GONE);
            addProductDialogWeight.setText("1");
        }
    }

    private void checkingNetwork() {
        if (new NetworkConnection(getActivity()).checkNetworkConnection()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    /*
                     * if creating PO then need this
                     * */
                    if (fragment.equals("delivery_fragment")) {
                        fetchAvailableStock();
                    } else showSpinner("available_stock", false);
                    /*
                     * location spinner
                     * */
                    if (SharedPreferenceManager.getLocation(getActivity())) fetchLocation();
                    else showSpinner("location", false);
                    /*
                     * grade spinner
                     * */
                    if (SharedPreferenceManager.getGrade(getActivity())) fetchGrade();
                    else showSpinner("grade", false);
                }
            }).start();
        } else {
            addProductDialogAvailableStockLayout.setVisibility(View.GONE);
            /*
             * off line location
             * */
            if (SharedPreferenceManager.getLocation(getActivity())) readLocationFromLocal();
            /*
             * off line grade
             * */
            if (SharedPreferenceManager.getGrade(getActivity())) readGradeFromLocal();
        }
    }

    private void showSpinner(final String type, final boolean show) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (type) {
                    case "available_stock":
                        addProductDialogAvailableStockLayout.setVisibility(show ? View.VISIBLE : View.GONE);
                        break;
                    case "location":
                        addProductDialogLocationLayout.setVisibility(show ? View.VISIBLE : View.GONE);
                        break;
                    case "grade":
                        addProductDialogGradeLayout.setVisibility(show ? View.VISIBLE : View.GONE);
                        break;
                }
            }
        });
    }

    private void reset() {
        addProductDialogWeight.setText(productObject.getType().equals("box") ? "1" : "");
        addProductDialogQuantity.setText("1");
        addProductDialogPrice.setText("");
        requestFocus();

    }

    /*------------------------------------------------------------------checking input-------------------------------------------------------------------------*/
    private void addProduct() {
        try {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    String weight = addProductDialogWeight.getText().toString();
                    String quantity = addProductDialogQuantity.getText().toString();

                    if (!weight.equals("") && !weight.equals("0") && !quantity.equals("") && !quantity.equals("0")) {
                        if (isUpdate) addProductDialogCallBack.removeItemFromAddedProduct();
                        /*
                         * bind value
                         * */
                        addProductDialogCallBack.addItemIntoAddedProduct(
                                new ProductObject(productObject.getId(),
                                        productObject.getName(),
                                        productObject.getPicture(),
                                        productObject.getType(),
                                        weight,
                                        quantity,
                                        addProductDialogPrice.getText().toString(),
                                        addProductDialogAvailableStockLayout.getVisibility() == View.VISIBLE ? stockObjectArrayList.get(addProductDialogAvailableStock.getSelectedItemPosition()).getDate() : (String) android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date()),
                                        SharedPreferenceManager.getLocation(getActivity()) ? locationObjectArrayList.get(addProductDialogLocation.getSelectedItemPosition()).getLocationId() : "",
                                        SharedPreferenceManager.getLocation(getActivity()) ? locationObjectArrayList.get(addProductDialogLocation.getSelectedItemPosition()).getLocation() : "",
                                        SharedPreferenceManager.getGrade(getActivity()) ? gradeObjectArrayList.get(addProductDialogGrade.getSelectedItemPosition()).getGradeId() : "",
                                        SharedPreferenceManager.getLocation(getActivity()) ? gradeObjectArrayList.get(addProductDialogGrade.getSelectedItemPosition()).getGrade() : ""));

                        Toast.makeText(getActivity(), isUpdate ? "Update Successfully!" : "Add Successfully!", Toast.LENGTH_SHORT).show();
                        addProductDialogCallBack.notifyDataSetChanged();
                        /*
                         * if update then close the dialog once update completed
                         * */
                        if (isUpdate) dismiss();
                        else reset();


                    } else {
                        Toast.makeText(getActivity(), "All the fields above are required!", Toast.LENGTH_SHORT).show();
                    }
                }
            }, 50);
        } catch (NullPointerException e) {
            Toast.makeText(getActivity(), "Something Went Wrong!", Toast.LENGTH_SHORT).show();
        } catch (IndexOutOfBoundsException e) {
            Toast.makeText(getActivity(), "Something Went Wrong!", Toast.LENGTH_SHORT).show();
        }

    }

    /*------------------------------------------------------------------listener-------------------------------------------------------------------------*/

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_product_dialog_cancel:
                dismiss();
                break;
            case R.id.add_product_dialog_add:
                addProduct();
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {

    }

    /*------------------------------------------------------------------spinner purpose-------------------------------------------------------------------------*/
    /*
     * available stock
     * */
    private void fetchAvailableStock() {
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("read", "1"));
        apiDataObjectArrayList.add(new ApiDataObject("product_id", productObject.getId()));
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
                    try {
                        if (jsonObjectLoginResponse.getString("status").equals("1")) {
                            JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("stock_detail");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                stockObjectArrayList.add(new StockObject(
                                        jsonArray.getJSONObject(i).getString("created_at"),
                                        jsonArray.getJSONObject(i).getString("total_in"),
                                        jsonArray.getJSONObject(i).getString("total_out"),
                                        jsonArray.getJSONObject(i).getString("total_in_quantity"),
                                        jsonArray.getJSONObject(i).getString("total_out_quantity")
                                ));
                            }
                            setUpAvailableStock();
                        } else {
                            showSpinner("available_stock", false);
                        }
                    } catch (JSONException e) {
                        CustomToast(getActivity(), "JSON Exception!");
                        e.printStackTrace();
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
            } catch (TimeoutException e) {
                CustomToast(getActivity(), "Connection Time Out!");
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    private void setUpAvailableStock() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayAdapter<StockObject> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, stockObjectArrayList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                addProductDialogAvailableStock.setAdapter(adapter);
                /*
                 * set selected item if isUpdate = true
                 * */
                if (isUpdate) {
                    for (int i = 0; i < stockObjectArrayList.size(); i++) {
                        if (stockObjectArrayList.get(i).getDate().equals(productObject.getAvailable_stock())) {
                            addProductDialogAvailableStock.setSelection(i);
                            return;
                        }
                    }
                }
            }
        });
    }

    /*
     * location
     * */
    private void fetchLocation() {
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("read", "1"));
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
                    try {
                        if (jsonObjectLoginResponse.getString("status").equals("1")) {

                            isLocation = true;
                            tbLocation.new Delete().perform();

                            JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("location");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                tbLocation.new create("location_id, name, created_at",
                                        new String[]{
                                                jsonArray.getJSONObject(i).getString("id"),
                                                jsonArray.getJSONObject(i).getString("name"),
                                                (String) android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date())
                                        }).perform();
                            }
                            readLocationFromLocal();
                        } else {
                            showSpinner("location", false);
                        }
                    } catch (JSONException e) {
                        CustomToast(getActivity(), "JSON Exception!");
                        e.printStackTrace();
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
            } catch (TimeoutException e) {
                CustomToast(getActivity(), "Connection Time Out!");
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    private void readLocationFromLocal() {
        tbLocation
                .new Read("*")
                .orderByDesc("location_id").perform();
    }

    private void setUpLocationSpinner(final String result) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONArray jsonArray = new JSONObject(result).getJSONArray("result");
                    if (jsonArray.length() > 0)
                        for (int i = 0; i < jsonArray.length(); i++) {
                            locationObjectArrayList.add(new LocationObject(
                                    jsonArray.getJSONObject(i).getString("location_id"),
                                    jsonArray.getJSONObject(i).getString("name")
                            ));
                        }
                    ArrayAdapter<LocationObject> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, locationObjectArrayList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    addProductDialogLocation.setAdapter(adapter);
                    /*
                     * set selected item if isUpdate = true
                     * */
                    if (isUpdate) {
                        for (int i = 0; i < locationObjectArrayList.size(); i++) {
                            if (locationObjectArrayList.get(i).getLocationId().equals(productObject.getLocation_id())) {
                                addProductDialogLocation.setSelection(i);
                                return;
                            }
                        }
                    }
                } catch (
                        JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /*
     * grade
     * */
    private void fetchGrade() {
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("read", "1"));
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
                    try {
                        if (jsonObjectLoginResponse.getString("status").equals("1")) {
                            isLocation = false;
                            tbGrade.new Delete().perform();

                            JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("grade");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                tbGrade.new create("grade_id, name, created_at",
                                        new String[]{
                                                jsonArray.getJSONObject(i).getString("id"),
                                                jsonArray.getJSONObject(i).getString("name"),
                                                (String) android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date())}).perform();
                            }
                            readGradeFromLocal();
                        } else {
                            showSpinner("grade", false);
                        }
                    } catch (JSONException e) {
                        CustomToast(getActivity(), "JSON Exception!");
                        e.printStackTrace();
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
            } catch (TimeoutException e) {
                CustomToast(getActivity(), "Connection Time Out!");
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    private void readGradeFromLocal() {
        isLocation = false;
        tbGrade
                .new Read("*")
                .orderByDesc("grade_id").perform();
    }

    private void setUpGradeSpinner(final String result) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONArray jsonArray = new JSONObject(result).getJSONArray("result");
                    if (jsonArray.length() > 0)
                        for (int i = 0; i < jsonArray.length(); i++) {
                            gradeObjectArrayList.add(new GradeObject(
                                    jsonArray.getJSONObject(i).getString("grade_id"),
                                    jsonArray.getJSONObject(i).getString("name")
                            ));
                        }
                    ArrayAdapter<GradeObject> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, gradeObjectArrayList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    addProductDialogGrade.setAdapter(adapter);
                    /*
                     * set selected item if isUpdate = true
                     * */
                    if (isUpdate) {
                        for (int i = 0; i < gradeObjectArrayList.size(); i++) {
                            if (gradeObjectArrayList.get(i).getGradeId().equals(productObject.getGrade_id())) {
                                addProductDialogGrade.setSelection(i);
                                return;
                            }
                        }
                    }
                } catch (
                        JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /*------------------------------------------------------------------local database purpose-------------------------------------------------------------------------*/
    @Override
    public void createResult(String status) {

    }

    @Override
    public void readResult(String result) {
        if (isLocation) {
            setUpLocationSpinner(result);
        } else {
            setUpGradeSpinner(result);
        }
    }

    @Override
    public void updateResult(String status) {

    }

    @Override
    public void deleteResult(String status) {

    }


    public interface AddProductDialogCallBack {
        void dismiss();

        void addItemIntoAddedProduct(ProductObject productObject);

        void removeItemFromAddedProduct();

        void notifyDataSetChanged();
    }
}
