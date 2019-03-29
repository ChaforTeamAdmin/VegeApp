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
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.vegeapp.R;
import com.jby.vegeapp.database.CustomSqliteHelper;
import com.jby.vegeapp.database.FrameworkClass;
import com.jby.vegeapp.others.KeyboardHelper;

import java.util.Objects;

import static com.jby.vegeapp.database.CustomSqliteHelper.TB_STOCK;


public class AddProductDialog extends DialogFragment implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    View rootView;

    private TextView addProductDialogProduct;
    private EditText addProductDialogWeight, addProductDialogQuantity;
    private Button addProductDialogCancel, addProductDialogAdd;
    private RadioGroup addProductDialogGradeLayout;
    private RadioButton addProductDialogGradeA, addProductDialogGradeB, addProductDialogGradeUnknown;
    private ProgressBar addProductDialogProgressBar;
    private Handler handler;
    private FrameworkClass frameworkClass;
    //grade
    private String grade = "unknown";
    private String farmerID, productID, price, picture, type, session, name;
    //update purpose
    private String quantity, weight, oldGrade;
    //keyboard
    private KeyboardHelper keyboardHelper;
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

    private void objectInitialize() {

        addProductDialogProduct = rootView.findViewById(R.id.add_product_dialog_product);
        addProductDialogWeight = rootView.findViewById(R.id.add_product_dialog_weight);
        addProductDialogQuantity = rootView.findViewById(R.id.add_product_dialog_quantity);

        addProductDialogCancel = rootView.findViewById(R.id.add_product_dialog_cancel);
        addProductDialogAdd = rootView.findViewById(R.id.add_product_dialog_add);

        addProductDialogGradeLayout = rootView.findViewById(R.id.add_product_dialog_grade_layout);
        addProductDialogGradeA = rootView.findViewById(R.id.add_product_dialog_grade_a);
        addProductDialogGradeB = rootView.findViewById(R.id.add_product_dialog_grade_b);
        addProductDialogGradeUnknown = rootView.findViewById(R.id.add_product_dialog_grade_unknow);

        addProductDialogProgressBar = rootView.findViewById(R.id.add_product_dialog_progress_bar);
        keyboardHelper = new KeyboardHelper();
        frameworkClass = new FrameworkClass(getActivity(), new CustomSqliteHelper(getActivity()), TB_STOCK);
        handler = new Handler();
        addProductDialogCallBack = (AddProductDialogCallBack) getActivity();
    }

    private void objectSetting() {
        preSetting();
        addProductDialogCancel.setOnClickListener(this);
        addProductDialogAdd.setOnClickListener(this);
        addProductDialogGradeLayout.setOnCheckedChangeListener(this);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
               KeyboardHelper.openSoftKeyboard(getActivity(), addProductDialogWeight);
            }
        },300);
    }

    private void preSetting(){
        Bundle bundle = getArguments();
        if(bundle != null){
            //update setting
            if(!Objects.requireNonNull(bundle.getString("weight")).equals("")){
                quantity = bundle.getString("quantity");
                weight = bundle.getString("weight");
                oldGrade = bundle.getString("grade");

                addProductDialogWeight.append(weight);
                addProductDialogWeight.setSelectAllOnFocus(true);

                addProductDialogQuantity.setText(quantity);
                addProductDialogAdd.setText("Update");

                preCheckRadioButton(oldGrade);
            }

            name = bundle.getString("product");
            productID = bundle.getString("product_id");
            farmerID = bundle.getString("farmer_id");
            price = bundle.getString("price");
            picture = bundle.getString("picture");
            type = bundle.getString("type");
            session = bundle.getString("session");

            addProductDialogProduct.setText(name);
        }
    }

    private void preCheckRadioButton(String grade){
        switch (grade){
            case "A":
                addProductDialogGradeA.setChecked(true);
                break;
            case "B":
                addProductDialogGradeB.setChecked(true);
                break;
                default: addProductDialogGradeUnknown.setChecked(true);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.add_product_dialog_cancel:
                dismiss();
                break;
            case R.id.add_product_dialog_add:
                checking();
                break;
        }
    }

    private void checking(){
        addProductDialogProgressBar.setVisibility(View.VISIBLE);
        addProductDialogAdd.setEnabled(false);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(addProductDialogQuantity.getText().toString().trim().equals("") || addProductDialogQuantity.getText().toString().trim().equals("0")){
                    Toast.makeText(getActivity(), "Please key in your quantity!", Toast.LENGTH_SHORT).show();
                    addProductDialogProgressBar.setVisibility(View.GONE);
                    addProductDialogAdd.setEnabled(true);
                }
                else{
                    if(addProductDialogWeight.getText().toString().trim().equals("") || addProductDialogWeight.getText().toString().trim().equals("0")){
                        Toast.makeText(getActivity(), "Please key in your weight!", Toast.LENGTH_SHORT).show();
                        addProductDialogProgressBar.setVisibility(View.GONE);
                        addProductDialogAdd.setEnabled(true);
                    }


                    else{
                        if(addProductDialogAdd.getText().toString().equals("Update"))checkingUpdate();
                        else add();
                    }
                }
            }
        },200);

    }

    private void checkingUpdate(){
        if(!weight.equals(addProductDialogWeight.getText().toString().trim())|| !quantity.equals(addProductDialogQuantity.getText().toString().trim())|| !oldGrade.equals(grade)) {
            //delete previous record
            deletePreviousRecord();
            //add item based on quantity
            int quantity = Integer.valueOf(addProductDialogQuantity.getText().toString());
            for (int i = 0; i < quantity; i++){
                add(addProductDialogWeight.getText().toString().trim(), "1");
            }
            //fetch child item
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    addProductDialogCallBack.fetchChildItem();
                }
            },200);

            addProductDialogProgressBar.setVisibility(View.GONE);
            Toast.makeText(getActivity(), "Update Successfully!", Toast.LENGTH_SHORT).show();
            dismiss();
        }
        else{
            dismiss();
        }
        addProductDialogAdd.setEnabled(true);
    }

    private void add(){
        //add item based on quantity
        int quantity = Integer.valueOf(addProductDialogQuantity.getText().toString());
        for (int i = 0; i < quantity; i++){
            add(addProductDialogWeight.getText().toString().trim(), "1");
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                addProductDialogCallBack.fetchSelectedProductApi();
            }
        },200);
        reset();
        addProductDialogProgressBar.setVisibility(View.GONE);
        addProductDialogAdd.setEnabled(true);
        Toast.makeText(getActivity(), "Added Successfully!", Toast.LENGTH_SHORT).show();
    }

    private void reset(){
        addProductDialogWeight.setText("");
        addProductDialogQuantity.setText("1");
        addProductDialogGradeUnknown.setChecked(true);
    }

    public void add(String weight, String quantity) {
        String date = String.valueOf(android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()));
            frameworkClass.
                    new create("farmer_id, product_id, name, picture, price, type, quantity, weight, grade, session, created_at"
                    , farmerID+ ", "+ productID + ", "+ name + ", "+ picture + ", "+
                            price + ", "+ type + ", " + quantity + ", "+ weight + ", "+ grade + ", "+session + ", " + date)
                    .perform();

    }

    public void deletePreviousRecord() {
        frameworkClass.new Delete()
                .where("weight = ? AND session = ? AND product_id = ? AND grade = ?",
                        weight + "," + session + "," + productID + "," + oldGrade)
                .perform();
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (i){
            case R.id.add_product_dialog_grade_a:
                grade = "A";
                break;
            case R.id.add_product_dialog_grade_b:
                grade = "B";
                break;
        }
    }

    @Override
    public void dismiss() {
        KeyboardHelper.hideSoftKeyboard(getContext(), addProductDialogWeight);
        super.dismiss();
    }

    public interface AddProductDialogCallBack{
        void fetchSelectedProductApi();
        void fetchChildItem();
    }
}
