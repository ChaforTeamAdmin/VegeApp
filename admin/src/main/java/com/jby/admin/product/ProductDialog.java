package com.jby.admin.product;

import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.jby.admin.R;
import com.jby.admin.adapter.product.ProductAdapter;
import com.jby.admin.database.CustomSqliteHelper;
import com.jby.admin.database.FrameworkClass;
import com.jby.admin.database.ResultCallBack;
import com.jby.admin.object.product.ProductObject;
import com.jby.admin.others.NetworkConnection;
import com.jby.admin.others.SwipeDismissTouchListener;
import com.jby.admin.others.recycleview.GridSpacingItemDecoration;
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

import static com.jby.admin.database.CustomSqliteHelper.TB_PRODUCT;
import static com.jby.admin.shareObject.CustomToast.CustomToast;

public class ProductDialog extends DialogFragment implements SearchView.OnQueryTextListener, ProductAdapter.ProductAdapterCallBack, ResultCallBack {
    View rootView;

    private RecyclerView productDialogProductList;
    private SearchView productDialogSearch;
    private ProgressBar progressBar;
    private ArrayList<ProductObject> productObjectArrayList;
    private ProductAdapter productAdapter;
    private Handler handler;
    /*
     * local database
     * */
    private boolean refresh = true;
    private FrameworkClass tbProduct;
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
        productDialogCallBack = (ProductDialogCallBack) getParentFragment();

        tbProduct = new FrameworkClass(getActivity(), this, new CustomSqliteHelper(getActivity()), TB_PRODUCT);

        productDialogProductList = rootView.findViewById(R.id.product_dialog_product_list);
        productDialogSearch = rootView.findViewById(R.id.product_dialog_search);
        progressBar = rootView.findViewById(R.id.progress_bar);

        productObjectArrayList = new ArrayList<>();
        productAdapter = new ProductAdapter(getActivity(), productObjectArrayList, this);

        handler = new Handler();
    }

    private void objectSetting() {
        if (getArguments() != null) {
            refresh = getArguments().getBoolean("refresh");
        }
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 2);
        productDialogProductList.setLayoutManager(mLayoutManager);
        productDialogProductList.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(5), true));
        productDialogProductList.setItemAnimator(new DefaultItemAnimator());
        productDialogProductList.setAdapter(productAdapter);

        productDialogSearch.setOnQueryTextListener(this);
        showProgressBar(true);
        checkingNetwork();
    }

    private void checkingNetwork(){
        if (new NetworkConnection(getActivity()).checkNetworkConnection()) {
            /*
             * if refresh then reload data from cloud
             * */
            if (refresh) fetchAllProduct();
                /*
                 * directly load data from local
                 * */
            else readDataFromLocal("");
        }
        else{
            readDataFromLocal("");
        }
    }

    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    private void fetchAllProduct() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("read", "1"));

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
                            Log.d("jsonObject", "product List: " + jsonObjectLoginResponse);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                            tbProduct.new Delete().perform();
                                            JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("product");
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                tbProduct.new create("product_id, product_code, name, picture, type, created_at", new String[]{
                                                        jsonArray.getJSONObject(i).getString("id"),
                                                        jsonArray.getJSONObject(i).getString("product_code"),
                                                        jsonArray.getJSONObject(i).getString("name"),
                                                        jsonArray.getJSONObject(i).getString("picture"),
                                                        jsonArray.getJSONObject(i).getString("type"),
                                                        (String) android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date())
                                                }).perform();
                                            }
                                            readDataFromLocal("");
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

    private void readDataFromLocal(String query) {
        tbProduct.new Read("*")
                .where("name LIKE '%" + query + "%' OR product_code LIKE '%" + query + "%'")
                .perform();
    }


    private void setUpList(JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                productObjectArrayList.add(new ProductObject(
                        jsonArray.getJSONObject(i).getString("product_id"),
                        jsonArray.getJSONObject(i).getString("product_code"),
                        jsonArray.getJSONObject(i).getString("name"),
                        jsonArray.getJSONObject(i).getString("picture"),
                        jsonArray.getJSONObject(i).getString("type")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        showProgressBar(false);
        notifyDataSetChanged();
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
                reset();
                readDataFromLocal(query);
            }
        }, 150);
        readDataFromLocal(query);
        return false;
    }

    private void reset() {
        productObjectArrayList.clear();
        notifyDataSetChanged();
    }

    private void showProgressBar(final boolean show) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
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
    public void createResult(String status) {
    }

    @Override
    public void readResult(String result) {
        try {
            setUpList(new JSONObject(result).getJSONArray("result"));

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

    @Override
    public void openAddProductDialog(ProductObject productObject) {
        productDialogCallBack.selectedProduct(productObject);
        dismiss();
    }

    public interface ProductDialogCallBack {
        void selectedProduct(ProductObject productObject);
    }
}
