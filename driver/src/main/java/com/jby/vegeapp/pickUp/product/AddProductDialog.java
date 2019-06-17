package com.jby.vegeapp.pickUp.product;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.vegeapp.R;
import com.jby.vegeapp.database.CustomSqliteHelper;
import com.jby.vegeapp.database.FrameworkClass;
import com.jby.vegeapp.object.product.ProductChildObject;
import com.jby.vegeapp.object.product.ProductParentObject;
import com.jby.vegeapp.others.KeyboardHelper;
import com.jby.vegeapp.others.SwipeDismissTouchListener;
import com.jby.vegeapp.shareObject.ApiDataObject;
import com.jby.vegeapp.shareObject.ApiManager;
import com.jby.vegeapp.shareObject.AsyncTaskManager;
import com.jby.vegeapp.sharePreference.SharedPreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.vegeapp.database.CustomSqliteHelper.TB_PRODUCT;
import static com.jby.vegeapp.database.CustomSqliteHelper.TB_PRODUCT_PRIORITY;
import static com.jby.vegeapp.database.CustomSqliteHelper.TB_STOCK;
import static com.jby.vegeapp.shareObject.CustomToast.CustomToast;


public class AddProductDialog extends DialogFragment implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    View rootView;

    private TextView addProductDialogProduct;
    private EditText addProductDialogWeight, addProductDialogQuantity;
    private Button addProductDialogCancel, addProductDialogAdd;
    private LinearLayout addProductDialogWeightLayout;
    private RadioGroup addProductDialogGradeLayout;
    private RadioButton addProductDialogGradeA, addProductDialogGradeB, addProductDialogGradeUnknown;
    private RadioButton addProductDialogFarmerA, addProductDialogFarmerB;
    private ProgressBar addProductDialogProgressBar;
    private Handler handler;
    private FrameworkClass frameworkClass, tbPrioritySql;
    //grade
    private String grade = "unknown";
    private String farmerID, productID, price, picture, type, session, name, ro_id;
    private String date, time;
    //update purpose
    private String quantity, weight, oldGrade;
    //keyboard
    private KeyboardHelper keyboardHelper;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    //object
    ProductParentObject productParentObject;
    ProductChildObject productChildObject;
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

    private void objectInitialize() {

        addProductDialogProduct = rootView.findViewById(R.id.add_product_dialog_product);
        addProductDialogWeight = rootView.findViewById(R.id.add_product_dialog_weight);
        addProductDialogQuantity = rootView.findViewById(R.id.add_product_dialog_quantity);

        addProductDialogCancel = rootView.findViewById(R.id.add_product_dialog_cancel);
        addProductDialogAdd = rootView.findViewById(R.id.add_product_dialog_add);

        addProductDialogGradeLayout = rootView.findViewById(R.id.add_product_dialog_grade_layout);
        addProductDialogGradeA = rootView.findViewById(R.id.add_product_dialog_grade_a);
        addProductDialogGradeB = rootView.findViewById(R.id.add_product_dialog_grade_b);
        addProductDialogFarmerA = rootView.findViewById(R.id.add_product_dialog_farmer_a);
        addProductDialogFarmerB = rootView.findViewById(R.id.add_product_dialog_farmer_b);
        addProductDialogGradeUnknown = rootView.findViewById(R.id.add_product_dialog_grade_unknow);
        addProductDialogWeightLayout = rootView.findViewById(R.id.add_product_dialog_weight_layout);

        addProductDialogProgressBar = rootView.findViewById(R.id.add_product_dialog_progress_bar);
        keyboardHelper = new KeyboardHelper();
        frameworkClass = new FrameworkClass(getActivity(), new CustomSqliteHelper(getActivity()), TB_STOCK);
        tbPrioritySql = new FrameworkClass(getActivity(), new CustomSqliteHelper(getActivity()), TB_PRODUCT_PRIORITY);
        handler = new Handler();
        addProductDialogCallBack = (AddProductDialogCallBack) getActivity();
    }

    private void objectSetting() {
        addProductDialogCancel.setOnClickListener(this);
        addProductDialogAdd.setOnClickListener(this);
        addProductDialogGradeLayout.setOnCheckedChangeListener(this);
        /*
         * getting value
         * */
        preSetting();
        /*
         * set weight layout visibility (type == box ) then hide and set (weight fix to 1)
         * */
        setWeightVisibility();
        /*
         * request keyboard focus
         * */
        requestFocus();
    }

    private void preSetting() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            //update setting
            if (!Objects.equals(bundle.getString("weight"), "") || bundle.getSerializable("parent_object") != null) {
                /*
                 * edit from history
                 * */
                productParentObject = (ProductParentObject) bundle.getSerializable("parent_object");
                productChildObject = (ProductChildObject) bundle.getSerializable("child_object");
                //checking value
                quantity = (productParentObject != null ? productChildObject.getQuantity() : bundle.getString("quantity"));
                weight = (productParentObject != null ? productChildObject.getWeight() : bundle.getString("weight"));
                oldGrade = (productParentObject != null ? productChildObject.getGrade() : bundle.getString("grade"));

                addProductDialogQuantity.setText(quantity);
                addProductDialogAdd.setText("Update");

                preCheckRadioButton(oldGrade);
            }
            time = bundle.getString("time");
            date = bundle.getString("date");
            name = bundle.getString("product");
            productID = bundle.getString("product_id");
            farmerID = bundle.getString("farmer_id");
            price = bundle.getString("price");
            picture = bundle.getString("picture");
            type = bundle.getString("type");
            session = bundle.getString("session");
            ro_id = bundle.getString("ro_id");

            addProductDialogProduct.setText(name);
        }
    }

    private void requestFocus() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                KeyboardHelper.openSoftKeyboard(getActivity(), type != null && type.equals("box") ? addProductDialogQuantity : addProductDialogWeight);
                if (type != null && type.equals("box")) {
                    addProductDialogQuantity.setText("");
                    if (quantity != null) addProductDialogQuantity.append(quantity);
                    addProductDialogQuantity.setSelectAllOnFocus(true);
                    addProductDialogQuantity.requestFocus();
                } else {
                    if (weight != null) addProductDialogWeight.append(weight);
                    addProductDialogWeight.setSelectAllOnFocus(true);
                    addProductDialogWeight.requestFocus();
                }
            }
        }, 300);
    }

    /*
     * */
    private void setWeightVisibility() {
        if (type != null && type.equals("box")) addProductDialogWeight.setText("1");
        addProductDialogWeightLayout.setVisibility(type != null && type.equals("box") ? View.GONE : View.VISIBLE);
    }

    private void preCheckRadioButton(String grade) {
        switch (grade) {
            case "A":
                addProductDialogGradeA.setChecked(true);
                break;
            case "B":
                addProductDialogGradeB.setChecked(true);
                break;
            case "FA":
                addProductDialogFarmerA.setChecked(true);
                break;
            case "FB":
                addProductDialogFarmerB.setChecked(true);
                break;
            default:
                addProductDialogGradeUnknown.setChecked(true);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_product_dialog_cancel:
                dismiss();
                break;
            case R.id.add_product_dialog_add:
                checking();
                break;
        }
    }

    private void checking() {
        addProductDialogProgressBar.setVisibility(View.VISIBLE);
        addProductDialogAdd.setEnabled(false);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (addProductDialogQuantity.getText().toString().trim().equals("") || addProductDialogQuantity.getText().toString().trim().equals("0")) {
                    Toast.makeText(getActivity(), "Please key in your quantity!", Toast.LENGTH_SHORT).show();
                    addProductDialogProgressBar.setVisibility(View.GONE);
                    addProductDialogAdd.setEnabled(true);
                } else {
                    if (addProductDialogWeight.getText().toString().trim().equals("") || addProductDialogWeight.getText().toString().trim().equals("0")) {
                        Toast.makeText(getActivity(), "Please key in your weight!", Toast.LENGTH_SHORT).show();
                        addProductDialogProgressBar.setVisibility(View.GONE);
                        addProductDialogAdd.setEnabled(true);
                    } else {
                        /*
                         * update stock from pick up history
                         * */
                        if (productParentObject != null) {
                            updateStock();
                        }
                        /*
                         * update stock from pick up activity
                         * */
                        else if (addProductDialogAdd.getText().toString().equals("Update"))
                            checkingUpdate();
                            /*
                             * add stock from pick up activity
                             * */
                        else add();
                    }
                }
            }
        }, 200);
    }

    private void checkingUpdate() {
        if (!weight.equals(addProductDialogWeight.getText().toString().trim()) || !quantity.equals(addProductDialogQuantity.getText().toString().trim()) || !oldGrade.equals(grade)) {
            //delete previous record
            deletePreviousRecord();
            //add item based on quantity
            int quantity = Integer.valueOf(addProductDialogQuantity.getText().toString());
            for (int i = 0; i < quantity; i++) {
                add(addProductDialogWeight.getText().toString().trim(), "1");
            }
            //fetch child item
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    addProductDialogCallBack.fetchSelectedProductApi();
                    /*
                    * this for controlling on back press in pick activity
                    * */
                    addProductDialogCallBack.setIsChanged();
                }
            }, 200);

            addProductDialogProgressBar.setVisibility(View.GONE);
            Toast.makeText(getActivity(), "Update Successfully!", Toast.LENGTH_SHORT).show();
            dismiss();
        } else {
            dismiss();
        }
        addProductDialogAdd.setEnabled(true);
    }

    private void add() {
        //add item based on quantity
        int quantity = Integer.valueOf(addProductDialogQuantity.getText().toString());
        for (int i = 0; i < quantity; i++) {
            add(addProductDialogWeight.getText().toString().trim(), "1");
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                addProductDialogCallBack.fetchSelectedProductApi();
                /*
                 * this for controlling on back press in pick activity
                 * */
                addProductDialogCallBack.setIsChanged();
            }
        }, 200);
        reset();
        setAsHighPriority();

        addProductDialogProgressBar.setVisibility(View.GONE);
        addProductDialogAdd.setEnabled(true);
        Toast.makeText(getActivity(), "Added Successfully!", Toast.LENGTH_SHORT).show();
    }

    private void reset() {
        addProductDialogWeight.setText("");
        addProductDialogQuantity.setText("1");
        addProductDialogGradeUnknown.setChecked(true);
    }

    private void setAsHighPriority() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String priority = String.valueOf(android.text.format.DateFormat.format("yyyyMMddHHmmss", new java.util.Date()));
                int i = tbPrioritySql.new Read("product_id").where("product_id = " + productID).count();
                if (i == 0)
                    tbPrioritySql.new create("product_id, priority", new String[]{productID, priority}).perform();
                else
                    tbPrioritySql.new Update("priority", priority).where("product_id = ?", productID).perform();
            }
        }).start();
    }

    /*--------------------------------------------------------------------------for pick up activity purpose-------------------------------------------------------*/
    public void add(String weight, String quantity) {
        frameworkClass.
                new create("farmer_id, product_id, name, picture, price, type, quantity, weight, grade, session, created_at"
                , new String[]{farmerID, productID, name, picture, price, type, quantity, weight, grade, session, date})
                .perform();
    }

    public void deletePreviousRecord() {
        frameworkClass.new Delete()
                .where("weight = ? AND session = ? AND product_id = ? AND grade = ?",
                        weight + "," + session + "," + productID + "," + oldGrade)
                .perform();
    }

    /*-----------------------------------------------------------------------------------history purpose--------------------------------------------------------*/
    /*
     * delete old record from database
     * */
    private void updateStock() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("pick_up_driver_id", SharedPreferenceManager.getUserId(getActivity())));
                apiDataObjectArrayList.add(new ApiDataObject("product_id", productParentObject.getId()));
                apiDataObjectArrayList.add(new ApiDataObject("farmer_id", farmerID));
                apiDataObjectArrayList.add(new ApiDataObject("date", date));
                apiDataObjectArrayList.add(new ApiDataObject("weight", weight));
                apiDataObjectArrayList.add(new ApiDataObject("delete", "1"));

                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().pick_up_history,
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
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (int i = 0; i < Integer.valueOf(addProductDialogQuantity.getText().toString()); i++) {
                                            addVegetable(i == (Integer.valueOf(addProductDialogQuantity.getText().toString()) - 1));
                                        }
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
            }
        }).start();
    }

    /*
     * store new stock
     * */
    private void addVegetable(final boolean stop) {
        apiDataObjectArrayList.add(new ApiDataObject("ro_id", ro_id));
        apiDataObjectArrayList.add(new ApiDataObject("driver_id", SharedPreferenceManager.getUserId(getActivity())));
        apiDataObjectArrayList.add(new ApiDataObject("farmer_id", farmerID));
        apiDataObjectArrayList.add(new ApiDataObject("weight", addProductDialogWeight.getText().toString()));
        apiDataObjectArrayList.add(new ApiDataObject("product_id", productParentObject.getId()));
        apiDataObjectArrayList.add(new ApiDataObject("price", productParentObject.getPrice()));
        apiDataObjectArrayList.add(new ApiDataObject("quantity", "1"));
        apiDataObjectArrayList.add(new ApiDataObject("type", productParentObject.getType()));
        apiDataObjectArrayList.add(new ApiDataObject("grade", grade));
        apiDataObjectArrayList.add(new ApiDataObject("date", date + " " + time));

        asyncTaskManager = new AsyncTaskManager(
                getActivity(),
                new ApiManager().vege_manage,
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
                    Log.d("jsonObject", "jsonObject: ee " + jsonObjectLoginResponse);
                    if (jsonObjectLoginResponse.getString("status").equals("1")) {
                        if (stop) {
                            CustomToast(getActivity(), "Update Successfully!");
                            addProductDialogCallBack.reset();
                            dismiss();
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
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (i) {
            case R.id.add_product_dialog_grade_a:
                grade = "A";
                break;
            case R.id.add_product_dialog_grade_b:
                grade = "B";
                break;
            case R.id.add_product_dialog_farmer_a:
                grade = "FA";
                break;
            case R.id.add_product_dialog_farmer_b:
                grade = "FB";
                break;
        }
    }

    @Override
    public void dismiss() {
        KeyboardHelper.hideSoftKeyboard(getActivity(), addProductDialogWeight);
        super.dismiss();
    }

    public interface AddProductDialogCallBack {
        void fetchSelectedProductApi();
        void fetchChildItem();
        void setIsChanged();
        void reset();
    }
}
