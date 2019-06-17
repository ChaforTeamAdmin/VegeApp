package com.jby.vegeapp.history.dialog;

import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import android.widget.Toast;

import com.jby.vegeapp.R;
import com.jby.vegeapp.adapter.pick_up.ProductAdapter;
import com.jby.vegeapp.object.product.ProductObject;
import com.jby.vegeapp.others.SwipeDismissTouchListener;
import com.jby.vegeapp.others.recycleview.GridSpacingItemDecoration;
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

public class ProductDialog extends DialogFragment implements SearchView.OnQueryTextListener {
    View rootView;

    private ProgressBar progressBar;
    private SearchView searchView;
    private RecyclerView productList;
    private ProductAdapter productAdapter;
    private ArrayList<ProductObject> productObjectArrayList;
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

    private void objectInitialize() {
        progressBar = rootView.findViewById(R.id.progress_bar);
        searchView = rootView.findViewById(R.id.product_dialog_search);
        productList = rootView.findViewById(R.id.product_dialog_product_list);
        productObjectArrayList = new ArrayList<>();
        setUpList(productObjectArrayList);
    }

    private void objectSetting() {
        productDialogCallBack = (ProductDialogCallBack) (getActivity() != null ? getActivity() : getTargetFragment());
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 3);
        productList.setLayoutManager(mLayoutManager);
        productList.addItemDecoration(new GridSpacingItemDecoration(3, dpToPx(5), true));
        productList.setItemAnimator(new DefaultItemAnimator());
        productList.setAdapter(productAdapter);
        searchView.setOnQueryTextListener(this);
        showProgressBar(true);
        fetchAllProduct();
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

    private void fetchAllProduct() {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
                            Log.d("jsonObject", "jsonObject: " + jsonObjectLoginResponse);
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("product");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    productObjectArrayList.add(new ProductObject(
                                            jsonArray.getJSONObject(i).getString("id"),
                                            jsonArray.getJSONObject(i).getString("name"),
                                            jsonArray.getJSONObject(i).getString("picture"),
                                            jsonArray.getJSONObject(i).getString("type"),
                                            jsonArray.getJSONObject(i).getString("price")));
                                }
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
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        productAdapter.notifyDataSetChanged();
                        showProgressBar(false);
                    }
                });
            }
        }).start();
    }

    //     Converting dp to pixel
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    private void showProgressBar(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(final String s) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<ProductObject> searchList = new ArrayList<>();
                for (int i = 0; i < productObjectArrayList.size(); i++) {
                    if (productObjectArrayList.get(i).getName().contains(s)) {
                        searchList.add(productObjectArrayList.get(i));
                    }
                }
                setUpList(searchList);
            }
        }).start();
        return true;
    }

    private void setUpList(final ArrayList<ProductObject> arrayList) {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                productAdapter = new ProductAdapter(getActivity(), arrayList, new ProductAdapter.ProductAdapterCallBack() {
                    @Override
                    public void openAddProductDialog(String productID, String product, String price, String picture, String type, String weight, String quantity, String grade) {
                        productDialogCallBack.selectProduct(productID, product, picture);
                        dismiss();
                    }
                });
                productList.setAdapter(productAdapter);
                productAdapter.notifyDataSetChanged();
            }
        });
    }
    public interface ProductDialogCallBack{
        void selectProduct(String id, String name, String image_pic);
    }
}