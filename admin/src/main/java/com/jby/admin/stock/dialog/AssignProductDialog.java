package com.jby.admin.stock.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.admin.R;
import com.jby.admin.database.CustomSqliteHelper;
import com.jby.admin.database.FrameworkClass;
import com.jby.admin.database.ResultCallBack;
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

import static com.jby.admin.database.CustomSqliteHelper.TB_DEFAULT_DRIVER;

public class AssignProductDialog extends DialogFragment implements View.OnClickListener, DriverDialog.DriverDialogCallBack,
        ResultCallBack {
    View rootView;
    private TextView assignProductDialogProduct, assignProductDialogDriver;
    private EditText assignProductDialogQuantity;
    private ImageView assignProductDialogPlusButton, assignProductDialogMinusButton;
    private Button assignProductDialogCancelButton, assignProductDialogSendButton;
    private ProgressBar assignProductDialogProgressBar;
    private LinearLayout assignProductDialogDriverLayout;

    private int availableQuantity;
    String farmerId, productId, customerId, driverId = "", type, stock_in_id;

    AssignProductDialogCallBack assignProductDialogCallBack;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private Handler handler;

    public AssignProductDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.assign_product_dialog, container);
        objectInitialize();
        objectSetting();

        return rootView;
    }

    private void objectInitialize() {
        assignProductDialogProduct = rootView.findViewById(R.id.assign_product_dialog_product);

        assignProductDialogDriver = rootView.findViewById(R.id.assign_product_dialog_driver);
        assignProductDialogDriverLayout = rootView.findViewById(R.id.assign_product_dialog_driver_layout);

        assignProductDialogPlusButton = rootView.findViewById(R.id.assign_product_dialog_plus);
        assignProductDialogMinusButton = rootView.findViewById(R.id.assign_product_dialog_minus);

        assignProductDialogQuantity = rootView.findViewById(R.id.assign_product_dialog_quantity);

        assignProductDialogProgressBar = rootView.findViewById(R.id.assign_product_dialog_progress_bar);

        assignProductDialogCancelButton = rootView.findViewById(R.id.assign_product_cancel_button);
        assignProductDialogSendButton = rootView.findViewById(R.id.assign_product_send_button);
        handler = new Handler();
    }

    private void objectSetting() {
        assignProductDialogCallBack = (AssignProductDialogCallBack) getParentFragment();
        assignProductDialogCancelButton.setOnClickListener(this);
        assignProductDialogSendButton.setOnClickListener(this);

        assignProductDialogPlusButton.setOnClickListener(this);
        assignProductDialogMinusButton.setOnClickListener(this);

        assignProductDialogDriverLayout.setOnClickListener(this);

        Bundle bundle = getArguments();
        if(bundle != null){
            availableQuantity = Integer.valueOf(bundle.getString("quantity"));
            farmerId = bundle.getString("farmer_id");
            productId = bundle.getString("product_id");
            customerId = bundle.getString("customer_id");
            stock_in_id = bundle.getString("stock_in_id");
            type = bundle.getString("type");
            String product = bundle.getString("product") + " (Grade:" + bundle.getString("grade") + ")";

            assignProductDialogProduct.setText(product);
            assignProductDialogQuantity.append(bundle.getString("quantity"));
            assignProductDialogQuantity.setSelectAllOnFocus(true);
            new FrameworkClass(getActivity(), this, new CustomSqliteHelper(getActivity()), TB_DEFAULT_DRIVER)
            .new Read("name, driver_id")
                    .where("farmer_id =" +  farmerId +" AND product_id = " + productId + " AND customer_id =" + customerId )
                    .orderByDesc("id").perform();
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
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.assign_product_cancel_button:
                dismiss();
                break;
            case R.id.assign_product_send_button:
                checking();
                break;
            case R.id.assign_product_dialog_plus:
                if(assignProductDialogQuantity.getText().toString().trim().equals(""))assignProductDialogQuantity.setText("0");
                addQuantity();
                break;
            case R.id.assign_product_dialog_minus:
                if(assignProductDialogQuantity.getText().toString().trim().equals(""))assignProductDialogQuantity.setText("0");
                minusQuantity();
                break;
            case R.id.assign_product_dialog_driver_layout:
                openDriverDialog();
                break;
        }
    }

    private void openDriverDialog(){
        DialogFragment dialogFragment = new DriverDialog();
        FragmentManager fm = getChildFragmentManager();

        Bundle bundle = new Bundle();
        bundle.putString("farmer_id", farmerId);
        bundle.putString("product_id", productId);

        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, "");
    }

    private void addQuantity(){
        int currentQuantity = Integer.valueOf(assignProductDialogQuantity.getText().toString().trim());
        if(currentQuantity < availableQuantity){
            currentQuantity++;
            assignProductDialogQuantity.setText("");
            assignProductDialogQuantity.append(String.valueOf(currentQuantity));
        }
    }

    private void minusQuantity(){
        int currentQuantity = Integer.valueOf(assignProductDialogQuantity.getText().toString().trim());
        if(currentQuantity > 0){
            currentQuantity--;
            assignProductDialogQuantity.setText("");
            assignProductDialogQuantity.append(String.valueOf(currentQuantity));
        }
    }
/*-----------------------------------------------------driver dialog call back---------------------------------------------------*/
    @Override
    public void selectedItem(String name, String driver_id) {
        new FrameworkClass(getActivity(), new CustomSqliteHelper(getActivity()), TB_DEFAULT_DRIVER)
                .new create("driver_id, name, farmer_id, product_id, customer_id",driver_id + "," + name + "," + farmerId + "," + productId + "," + customerId)
                .perform();

        assignProductDialogDriver.setText(name);
        driverId = driver_id;
    }

/*-----------------------------------------------------store to cloud------------------------------------------------------------*/
    private void checking(){
        String quantity = assignProductDialogQuantity.getText().toString().trim();
        if(quantity.equals("") || quantity.equals("0") || driverId.equals("")) Toast.makeText(getActivity(), "Please fill in all the fields", Toast.LENGTH_SHORT).show();
        else{
            assignProductDialogProgressBar.setVisibility(View.VISIBLE);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    assignVegetable();
                }
            },200);
        }
    }
    private void assignVegetable(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("driver_id",driverId ));
        apiDataObjectArrayList.add(new ApiDataObject("customer_id", customerId));
        apiDataObjectArrayList.add(new ApiDataObject("stock_in_id", stock_in_id));
        apiDataObjectArrayList.add(new ApiDataObject("product_id", productId));
        apiDataObjectArrayList.add(new ApiDataObject("quantity", assignProductDialogQuantity.getText().toString().trim()));
        apiDataObjectArrayList.add(new ApiDataObject("type", type));

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
                    Log.d("jsonObject", "jsonObject: " +  jsonObjectLoginResponse);
                    if(jsonObjectLoginResponse.getString("status").equals("1")){

                        assignProductDialogProgressBar.setVisibility(View.GONE);
                        assignProductDialogCallBack.showSnackBar("Uploaded Successfully!");
                        dismiss();
                    }
                }
                else {
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

    @Override
    public void createResult(String status) {

    }

    @Override
    public void readResult(String result) {
        Log.d("haha", "haha: " + result);
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("result");
            if(jsonArray.length() > 0){
                assignProductDialogDriver.setText(jsonArray.getJSONObject(0).getString("name"));
                driverId = jsonArray.getJSONObject(0).getString("driver_id");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateResult(String status) {

    }

    @Override
    public void deleteResult(String status) {

    }

    public interface AssignProductDialogCallBack {
        void showSnackBar(String message);
    }

}