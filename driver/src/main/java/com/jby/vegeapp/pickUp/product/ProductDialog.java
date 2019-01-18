package com.jby.vegeapp.pickUp.product;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.jby.vegeapp.R;
import com.jby.vegeapp.adapter.ProductAdapter;
import com.jby.vegeapp.object.ProductObject;
import com.jby.vegeapp.others.ExpandableHeightListView;
import com.jby.vegeapp.shareObject.ApiDataObject;
import com.jby.vegeapp.shareObject.ApiManager;
import com.jby.vegeapp.shareObject.AsyncTaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ProductDialog extends DialogFragment implements SearchView.OnQueryTextListener, ProductAdapter.ProductAdapterCallBack {
    View rootView;

    private ExpandableHeightListView productDialogProductList;
    private SearchView productDialogSearch;
    private RelativeLayout productDialogProgressBar;
    private ArrayList<ProductObject> productObjectArrayList;
    private ProductAdapter productAdapter;
    private Handler handler;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;

    ProductDialogCallBack productDialogCallBack;

    public ProductDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.product_dialog, container);
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
        productDialogCallBack = (ProductDialogCallBack) getActivity();

        productDialogProductList = rootView.findViewById(R.id.product_dialog_product_list);
        productDialogSearch = rootView.findViewById(R.id.product_dialog_search);
        productDialogProgressBar = rootView.findViewById(R.id.product_dialog_progerss_bar);

        productObjectArrayList = new ArrayList<>();
        productAdapter = new ProductAdapter(getActivity(), productObjectArrayList, this);

        handler = new Handler();
    }

    private void objectSetting() {
        productDialogProductList.setAdapter(productAdapter);
        productDialogProductList.setExpanded(true);

        productDialogSearch.setOnQueryTextListener(this);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchAllProduct();
            }
        },200);
    }

    private void fetchAllProduct(){
        apiDataObjectArrayList = new ArrayList<>();
        apiDataObjectArrayList.add(new ApiDataObject("fetch", "1"));

        asyncTaskManager = new AsyncTaskManager(
                getActivity(),
                new ApiManager().product,
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
                        JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("product");
                        for(int i = 0; i < jsonArray.length(); i++)
                        {
                            productObjectArrayList.add(new ProductObject(
                                    jsonArray.getJSONObject(i).getString("id"),
                                    jsonArray.getJSONObject(i).getString("name"),
                                    jsonArray.getJSONObject(i).getString("picture"),
                                    jsonArray.getJSONObject(i).getString("type"),
                                    jsonArray.getJSONObject(i).getString("price")));
                        }
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
        productAdapter.notifyDataSetChanged();
        showProgressBar(false);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(final String query) {
        showProgressBar(true);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                searchFromArrayList(query);
            }
        },700);

        return false;
    }

    private void searchFromArrayList(String query){
        ArrayList<ProductObject> searchList = new ArrayList<>();
        for(int i = 0 ; i < productObjectArrayList.size(); i++){
            if(productObjectArrayList.get(i).getName().contains(query)) {
                searchList.add(productObjectArrayList.get(i));
            }
        }
        productAdapter = new ProductAdapter(getActivity(), searchList, this);
        productDialogProductList.setAdapter(productAdapter);
        productAdapter.notifyDataSetChanged();
        showProgressBar(false);
    }

    private void showProgressBar(boolean show){
        if(!show) productDialogProgressBar.setVisibility(View.GONE);
        else productDialogProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void add(final ProductObject productObject, final String quantity) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                productDialogCallBack.add(productObject, quantity);
                showSnackBar("Added Successfully!");
            }
        },200);
    }

    //    snackBar setting
    public void showSnackBar(String message) {
        final Snackbar snackbar = Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT);
        snackbar.setActionTextColor(getResources().getColor(R.color.blue));
        snackbar.setAction("Dismiss", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    public interface ProductDialogCallBack{
        void add(ProductObject productObject, String quantity);
    }
}
