package com.jby.admin.basket;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.admin.MainActivity;
import com.jby.admin.R;
import com.jby.admin.shareObject.ApiDataObject;
import com.jby.admin.shareObject.ApiManager;
import com.jby.admin.shareObject.AsyncTaskManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class BasketFragment extends Fragment {
    private View rootView;

    private TextView basketFragmentDriverQuantity, basketFragmentFarmerQuantity, basketFragmentCustomerQuantity;
    private TextView basketFragmentTotalOutStandingBasket, basketFragmentTotalBasket, basketFragmentAvailableQuantity;

    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private Handler handler;

    public BasketFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_basket, container, false);
        objectInitialize();
        objectSetting();
        return  rootView;
    }

    private void objectInitialize() {
        basketFragmentDriverQuantity = rootView.findViewById(R.id.fragment_basket_driver_quantity);
        basketFragmentFarmerQuantity = rootView.findViewById(R.id.fragment_basket_farmer_quantity);
        basketFragmentCustomerQuantity = rootView.findViewById(R.id.fragment_basket_customer_quantity);
        basketFragmentTotalOutStandingBasket = rootView.findViewById(R.id.fragment_basket_total_outstanding_quantity);

        basketFragmentTotalBasket = rootView.findViewById(R.id.fragment_basket_total_basket);
        basketFragmentAvailableQuantity = rootView.findViewById(R.id.fragment_basket_available_basket);

        handler = new Handler();
    }

    private void objectSetting() {
        showProgressBar(true);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchBasketInformation();
            }
        },200);
    }

    private void fetchBasketInformation(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("fetch", "1"));

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

                if (jsonObjectLoginResponse != null) {
                    Log.d("jsonObject", "jsonObject: " +  jsonObjectLoginResponse);
                    if(jsonObjectLoginResponse.getString("status").equals("1")){
                        setupValue(
                                jsonObjectLoginResponse.getString("total"),
                                jsonObjectLoginResponse.getString("driver"),
                                jsonObjectLoginResponse.getString("farmer"),
                                jsonObjectLoginResponse.getString("customer")
                        );
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
        showProgressBar(false);
    }

    private void setupValue(String total, String driver, String farmer, String customer){
        basketFragmentTotalBasket.setText(total);
        basketFragmentDriverQuantity.setText(driver);
        basketFragmentFarmerQuantity.setText(farmer);
        basketFragmentCustomerQuantity.setText(customer);
        basketFragmentTotalOutStandingBasket.setText(getTotalOutStanding(driver, farmer, customer));
        basketFragmentAvailableQuantity.setText(getTotalAvailableQuantity(total, getTotalOutStanding(driver, farmer, customer)));
    }

    private String getTotalOutStanding(String driver, String farmer, String customer){
        try{
            return String.valueOf(Integer.valueOf(driver) + Integer.valueOf(farmer) + Integer.valueOf(customer));
        }catch (NumberFormatException e){
            return "-";
        }
    }

    private String getTotalAvailableQuantity(String total, String outstanding){
        try{
            return String.valueOf(Integer.valueOf(total) - Integer.valueOf(outstanding));
        }catch (NumberFormatException e){
            return "-";
        }
    }

    private void showProgressBar(boolean show){
        ((MainActivity) Objects.requireNonNull(getActivity())).showProgressBar(show);
    }
}
