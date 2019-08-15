package com.jby.admin.delivery;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.jby.admin.MainActivity;
import com.jby.admin.R;
import com.jby.admin.adapter.product.AddedProductExpandableAdapter;
import com.jby.admin.database.CustomSqliteHelper;
import com.jby.admin.database.FrameworkClass;
import com.jby.admin.database.ResultCallBack;
import com.jby.admin.network.DONetworkMonitor;
import com.jby.admin.object.entity.DriverObject;
import com.jby.admin.object.entity.CustomerObject;
import com.jby.admin.object.product.ProductObject;
import com.jby.admin.others.NetworkConnection;
import com.jby.admin.others.NonScrollExpandableListView;
import com.jby.admin.product.AddProductDialog;
import com.jby.admin.product.ProductDialog;
import com.jby.admin.shareObject.AnimationUtility;
import com.jby.admin.shareObject.ApiDataObject;
import com.jby.admin.shareObject.ApiManager;
import com.jby.admin.shareObject.AsyncTaskManager;
import com.jby.admin.driver.DriverDialog;
import com.jby.admin.shareObject.CustomScheduleJob;
import com.jby.admin.sharePreference.SharedPreferenceManager;
import com.jby.admin.customer.CustomerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.admin.database.CustomSqliteHelper.TB_DELIVERY_ORDER;
import static com.jby.admin.database.CustomSqliteHelper.TB_STOCK_OUT;
import static com.jby.admin.shareObject.CustomToast.CustomToast;


public class DeliveryFragment extends Fragment implements View.OnClickListener, CustomerDialog.CustomerDialogCallBack, DriverDialog.DriverDialogCallBack,
        ProductDialog.ProductDialogCallBack, AddProductDialog.AddProductDialogCallBack, AddedProductExpandableAdapter.ProductExpandableAdapterCallBack,
        ResultCallBack {
    View rootView;
    /*
     * page one
     * */

    /*
     * page two
     * */
    private ScrollView page2ParentLayout;
    private LinearLayout page2DeliveryDetailLayout;
    private ImageView page2DeliveryDetailLayoutButton;
    private LinearLayout page2DateLayout, page2CustomerLayout, page2DriverLayout;
    private TextView page2Date, page2Customer, page2Driver;
    private FloatingActionButton page2AddProductButton;

    private TextView page2LabelSelectedItem;

    private NonScrollExpandableListView page2SelectedProductList;
    private AddedProductExpandableAdapter addedProductExpandableAdapter;
    private ArrayList<ProductObject> addedProductArrayList;
    private int groupPosition, childPosition;
    private String doId = "";

    private CustomerObject customerObject;
    private DriverObject driverObject;

    private boolean refreshProductDialog = true;
    private boolean reopenProductDialog = true;

    //not found layout
    private RelativeLayout notFoundLayout;
    private ImageView notFoundIcon;
    private TextView notFoundLabel;

    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private Handler handler;
    /*
     * local database
     * */
    private String localDoId;
    private FrameworkClass tbDeliveryOrder, tbStockOut;
    private boolean creatingLocalDo = true;

    public DeliveryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_delivery, container, false);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        //not found layout
        notFoundLayout = rootView.findViewById(R.id.not_found_layout);
        notFoundIcon = rootView.findViewById(R.id.not_found_layout_icon);
        notFoundLabel = rootView.findViewById(R.id.not_found_layout_label);
        /*
         * page 2
         * */
        page2ParentLayout = rootView.findViewById(R.id.fragment_delivery_order_page2_parent_layout);

        page2AddProductButton = rootView.findViewById(R.id.fragment_delivery_order_page2_add_product_button);

        page2DeliveryDetailLayout = rootView.findViewById(R.id.fragment_delivery_order_page2_deliver_detail_layout);
        page2DeliveryDetailLayoutButton = rootView.findViewById(R.id.fragment_delivery_order_page2_deliver_detail_layout_button);

        page2DateLayout = rootView.findViewById(R.id.fragment_delivery_order_page2_date_layout);
        page2CustomerLayout = rootView.findViewById(R.id.fragment_delivery_order_page2_customer_layout);
        page2DriverLayout = rootView.findViewById(R.id.fragment_delivery_order_page2_driver_layout);

        page2Date = rootView.findViewById(R.id.fragment_delivery_order_page2_date);
        page2Customer = rootView.findViewById(R.id.fragment_delivery_order_page2_customer);
        page2Driver = rootView.findViewById(R.id.fragment_delivery_order_page2_driver);

        page2LabelSelectedItem = rootView.findViewById(R.id.fragment_delivery_order_page2_label_selected_item);
        page2SelectedProductList = rootView.findViewById(R.id.fragment_delivery_order_page2_added_product_list);
        addedProductArrayList = new ArrayList<>();
        addedProductExpandableAdapter = new AddedProductExpandableAdapter(getActivity(), addedProductArrayList, this, "delivery_fragment");


        handler = new Handler();
    }

    private void objectSetting() {
        page2DateLayout.setOnClickListener(this);
        page2CustomerLayout.setOnClickListener(this);
        page2DriverLayout.setOnClickListener(this);
        page2AddProductButton.setOnClickListener(this);
        page2DeliveryDetailLayoutButton.setOnClickListener(this);

        /*
         * page2 setting
         * */
        page2SelectedProductList.setAdapter(addedProductExpandableAdapter);
        setDefaultDate();
        /*
         * page3 setting
         * */

        showPage(2);
        setupNotFoundLayout();
        checkInternetConnection();
    }

    private void setupNotFoundLayout() {
        notFoundIcon.setImageDrawable(getResources().getDrawable(R.drawable.not_found));
        notFoundLabel.setText("No Item is Found");
    }

    public void checkInternetConnection() {
        boolean networkConnection = new NetworkConnection(getActivity()).checkNetworkConnection();
        if (networkConnection) {
            refreshProductDialog = true;
        } else refreshProductDialog = false;
    }

    private void showProgressBar(final boolean show) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((MainActivity) Objects.requireNonNull(getActivity())).showProgressBar(show);
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_delivery_order_page2_date_layout:
                openDatePicker();
                break;
            case R.id.fragment_delivery_order_page2_customer_layout:
                view.setEnabled(false);
                openCustomerDialog();
                break;
            case R.id.fragment_delivery_order_page2_driver_layout:
                view.setEnabled(false);
                openDriverDialog();
                break;
            case R.id.fragment_delivery_order_page2_deliver_detail_layout_button:
                showDoDetail();
                break;
            case R.id.fragment_delivery_order_page2_add_product_button:
                view.setEnabled(false);
                openProductDialog();
                break;
        }
    }

    private void showPage(int page) {
        page2ParentLayout.setVisibility(page == 2 ? View.VISIBLE : View.GONE);
    }

    /*--------------------------------------------------------------------------page two ----------------------------------------------------------------------------*/
    /*
     * do detail visibility control
     * */
    private void showDoDetail() {
        if (page2DeliveryDetailLayout.getVisibility() == View.VISIBLE) {
            new AnimationUtility().fastFadeOutGone(getContext(), page2DeliveryDetailLayout);
        } else {
            new AnimationUtility().layoutSwipeDownIn(getContext(), page2DeliveryDetailLayout);
        }
        page2DeliveryDetailLayoutButton.setImageDrawable(page2DeliveryDetailLayout.getVisibility() == View.VISIBLE ? getActivity().getResources().getDrawable(R.drawable.arrow_up) : getActivity().getResources().getDrawable(R.drawable.arrow_down));
    }

    /*
     * date layout
     * */
    private String setDefaultDate() {
        return (String) android.text.format.DateFormat.format("yyyy-MM-dd", new java.util.Date());
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
                        page2Date.setText(String.format("%s", String.format(Locale.getDefault(), "%d-%02d-%02d", year, (monthOfYear + 1), dayOfMonth)));
                    }
                }, mYear, mMonth, mDay);

        datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
        datePickerDialog.show();
    }

    /*
     * customer layout
     * */
    private void openCustomerDialog() {
        DialogFragment customerDialog = new CustomerDialog();
        customerDialog.show(getChildFragmentManager(), "");
        /*
         * enable button
         * */
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                page2CustomerLayout.setEnabled(true);
            }
        }, 50);
    }

    @Override
    public void selectedCustomer(CustomerObject customerObject) {
        this.customerObject = customerObject;
        page2Customer.setText(customerObject.getName());
    }

    /*
     * driver layout
     * */
    private void openDriverDialog() {
        DialogFragment driverDialog = new DriverDialog();
        driverDialog.show(getChildFragmentManager(), "");
        /*
         * enable button
         * */
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                page2DriverLayout.setEnabled(true);
            }
        }, 50);
    }

    @Override
    public void selectedDriver(DriverObject driverObject) {
        this.driverObject = driverObject;
        page2Driver.setText(driverObject.getName());
    }

    /*--------------------------------------------------------------------------add product purpose ----------------------------------------------------------------------------*/
    /*
     * product layout
     * */
    private void openProductDialog() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("refresh", refreshProductDialog);
        DialogFragment productDialog = new ProductDialog();
        productDialog.setArguments(bundle);
        productDialog.show(getChildFragmentManager(), "");
        /*
         * avoid to refresh product dialog in second time onward
         * */
        refreshProductDialog = false;
        /*
         * enable button
         * */
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                page2AddProductButton.setEnabled(true);
            }
        }, 50);
    }

    @Override
    public void selectedProduct(ProductObject productObject) {
        openAddProductDialog(productObject, false, true);
    }

    /*
     * driver layout
     * */
    private void openAddProductDialog(ProductObject productObject, boolean isUpdate, boolean reopenProductDialog) {
        this.reopenProductDialog = reopenProductDialog;

        Bundle bundle = new Bundle();
        bundle.putSerializable("product", productObject);
        bundle.putString("fragment", "delivery_fragment");
        bundle.putBoolean("isUpdate", isUpdate);

        DialogFragment addProductDialog = new AddProductDialog();
        addProductDialog.setArguments(bundle);
        addProductDialog.show(getChildFragmentManager(), "");
    }

    /*--------------------------------------------------------------------------add product dialog call back----------------------------------------------------------------------------*/
    @Override
    public void dismiss() {
        if (reopenProductDialog) openProductDialog();
    }

    @Override
    public void addItemIntoAddedProduct(ProductObject productObject) {
        Log.d("haha", "added product: " + productObject.toString());
        for (int i = 0; i < addedProductArrayList.size(); i++) {
            /*
             * if parent id existed
             * */
            if (addedProductArrayList.get(i).getId().equals(productObject.getId())) {

                for (int j = 0; j < addedProductArrayList.get(i).getAddedProductChildArrayList().size(); j++) {

                    ProductObject childObject = addedProductArrayList.get(i).getAddedProductChildArrayList().get(j);

                    boolean locationRequest = SharedPreferenceManager.getLocation(getActivity());
                    boolean gradeRequest = SharedPreferenceManager.getGrade(getActivity());
                    /*
                     * if same child existed
                     * */
                    if ((locationRequest ? childObject.getLocation().equals(productObject.getLocation()) : childObject.getLocation().equals("")) && (gradeRequest ? childObject.getGrade().equals(productObject.getGrade()) : childObject.getGrade().equals(""))
                            && childObject.getAvailable_stock().equals(productObject.getAvailable_stock()) && childObject.getWeight().equals(productObject.getWeight())) {

                        childObject.addOnQuantity(productObject.getQuantity());
                        childObject.setAvailable_stock(productObject.getAvailable_stock());
                        return;
                    }
                }
                /*
                 * if not then create new child
                 * */
                addedProductArrayList.get(i).setAddedProductChildArrayList(addAddedProductChildObject(productObject));
                return;
            }
        }
        /*
         * add parent item
         * */
        addedProductArrayList.add(addAddedProductParentObject(productObject));
        /*
         * add child
         * */
        addedProductArrayList.get(addedProductArrayList.size() - 1).setAddedProductChildArrayList(addAddedProductChildObject(productObject));
    }

    @Override
    public void removeItemFromAddedProduct() {
        delete(groupPosition, childPosition);
    }

    @Override
    public void notifyDataSetChanged() {
        addedProductExpandableAdapter.notifyDataSetChanged();
        /*
         * show label
         * */
        showLabel();
    }

    private ProductObject addAddedProductParentObject(ProductObject productObject) {
        return new ProductObject(
                productObject.getId(),
                productObject.getName(),
                productObject.getPicture(),
                productObject.getType());
    }

    private ProductObject addAddedProductChildObject(ProductObject productObject) {
        return new ProductObject(
                productObject.getWeight(),
                productObject.getQuantity(),
                productObject.getPrice(),
                productObject.getAvailable_stock(),
                productObject.getLocation_id(),
                productObject.getLocation(),
                productObject.getGrade_id(),
                productObject.getGrade());
    }


    @Override
    public void deleteConfirmation(final int groupPosition, final int childPosition) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Warning");
        builder.setMessage("Are you sure that you want to delete this item?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        delete(groupPosition, childPosition);
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

    /*
     * delete added product
     * */
    private void delete(int groupPosition, int childPosition) {
        addedProductArrayList.get(groupPosition).getAddedProductChildArrayList().remove(childPosition);
        /*
         * delete parent when child is empty
         * */
        if (addedProductArrayList.get(groupPosition).getAddedProductChildArrayList().size() == 0)
            addedProductArrayList.remove(groupPosition);
        notifyDataSetChanged();
    }

    @Override
    public void update(int groupPosition, int childPosition) {
        this.groupPosition = groupPosition;
        this.childPosition = childPosition;

        ProductObject parentObject = addedProductArrayList.get(groupPosition);
        ProductObject childObject = addedProductArrayList.get(groupPosition).getAddedProductChildArrayList().get(childPosition);
        openAddProductDialog(
                new ProductObject(
                        parentObject.getId(),
                        parentObject.getName(),
                        parentObject.getPicture(),
                        parentObject.getType(),
                        childObject.getWeight(),
                        childObject.getQuantity(),
                        childObject.getPrice(),
                        childObject.getAvailable_stock(),
                        childObject.getLocation_id(),
                        childObject.getLocation(),
                        childObject.getGrade_id(),
                        childObject.getGrade()), true, false);
    }

    private void showLabel() {
        page2LabelSelectedItem.setVisibility(addedProductArrayList.size() > 0 ? View.VISIBLE : View.GONE);
    }

    /*--------------------------------------------------------------------------store to cloud ----------------------------------------------------------------------------*/

    public void uploadConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Create Delivery Order Request");
        builder.setMessage("Are you sure that you want to proceed?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        showProgressBar(true);
                        if (new NetworkConnection(getActivity()).checkNetworkConnection()) {
                            createDo();
                        } else {
                            createLocalDo();
                        }
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

    public void checkingBeforeUpload() {
        if (customerObject == null) {
            showSnackBar("Please Select a customer!");
            return;
        }

        if (addedProductArrayList.size() <= 0) {
            showSnackBar("Nothing to Upload!");
            return;
        }
        uploadConfirmation();
    }

    private void createDo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("create", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("deliver_driver_id", driverObject != null ? driverObject.getId() : ""));
                apiDataObjectArrayList.add(new ApiDataObject("customer_id", customerObject.getId()));
                apiDataObjectArrayList.add(new ApiDataObject("date", page2Date.getText().toString().equals("Today") ? setDefaultDate() : page2Date.getText().toString()));


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
                            Log.d("jsonObject", "do_id: " + jsonObjectLoginResponse);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                            doId = jsonObjectLoginResponse.getString("do_id");
                                            for (int i = 0; i < addedProductArrayList.size(); i++) {
                                                for (int j = 0; j < addedProductArrayList.get(i).getAddedProductChildArrayList().size(); j++) {
                                                    storeDoItem(i, j, (i == addedProductArrayList.size() - 1 && j == addedProductArrayList.get(i).getAddedProductChildArrayList().size() - 1));
                                                }
                                            }
                                        } else {
                                            CustomToast(getActivity(), "Something Went Wrong!");
                                        }

                                    } catch (JSONException e) {
                                        CustomToast(getActivity(), "JSON Exception!");
                                        e.printStackTrace();
                                    } catch (IndexOutOfBoundsException e) {
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

    private void storeDoItem(final int groupPosition, final int childPosition, final boolean finish) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ProductObject childObject = addedProductArrayList.get(groupPosition).getAddedProductChildArrayList().get(childPosition);
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("create", "1"));
                apiDataObjectArrayList.add(new ApiDataObject("stock_date", addedProductArrayList.get(groupPosition).getAddedProductChildArrayList().get(childPosition).getAvailable_stock()));
                apiDataObjectArrayList.add(new ApiDataObject("do_id", doId));
                apiDataObjectArrayList.add(new ApiDataObject("product_id", addedProductArrayList.get(groupPosition).getId()));
                apiDataObjectArrayList.add(new ApiDataObject("grade", childObject.getGrade().equals("") ? "unknown" : childObject.getGrade()));
                apiDataObjectArrayList.add(new ApiDataObject("location", childObject.getLocation().equals("") ? "unknown" : childObject.getLocation()));
                apiDataObjectArrayList.add(new ApiDataObject("weight", childObject.getWeight()));
                apiDataObjectArrayList.add(new ApiDataObject("quantity", childObject.getQuantity()));
                apiDataObjectArrayList.add(new ApiDataObject("price", childObject.getPrice().equals("") ? "0" : childObject.getPrice()));
                apiDataObjectArrayList.add(new ApiDataObject("customer_id", customerObject.getId()));

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
                            Log.d("jsonObject", "do_id: " + jsonObjectLoginResponse);

                            try {
                                if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                    if (finish) {
                                        showSnackBar("Store Successfully!");
                                        page2Reset();
                                    }
                                } else {
                                    CustomToast(getActivity(), "Something Went Wrong!");
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
        }).start();
    }

    private void page2Reset() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addedProductArrayList.clear();

                page2Customer.setText("Please Select A Customer Here");
                page2Driver.setText("Please Select A Driver Here");

                customerObject = new CustomerObject();
                driverObject = new DriverObject();
                showProgressBar(false);
                showDoDetail();
                notifyDataSetChanged();
            }
        });
    }

    public void showSnackBar(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
                snackbar.setAction("Dismiss", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                    }
                });
                snackbar.show();
            }
        });
    }

    /*--------------------------------------------------------------------------store to local ----------------------------------------------------------------------------*/
    private void createLocalDo() {
        creatingLocalDo = true;
        tbDeliveryOrder = new FrameworkClass(getActivity(), this, new CustomSqliteHelper(getActivity()), TB_DELIVERY_ORDER);
        tbStockOut = new FrameworkClass(getActivity(), this, new CustomSqliteHelper(getActivity()), TB_STOCK_OUT);
        localDoId = (String) android.text.format.DateFormat.format("yyyyMMddHHmmss", new java.util.Date());

        new Thread(new Runnable() {
            @Override
            public void run() {
                tbDeliveryOrder.new create("do_id, deliver_driver_id, customer_id, date", new String[]{
                        localDoId,
                        (driverObject != null ? driverObject.getId() : ""),
                        customerObject.getId(),
                        page2Date.getText().toString().equals("Today") ? setDefaultDate() : page2Date.getText().toString()
                }).perform();

            }
        }).start();
    }

    private void storeLocalDoItem(final int groupPosition, final int childPosition, final boolean finish) {
        creatingLocalDo = false;
        tbStockOut.new create("local_do_id, customer_id, product_id, stock_date, grade, location, weight, quantity, price", new String[]{
                localDoId,
                customerObject.getId(),
                addedProductArrayList.get(groupPosition).getId(),
                addedProductArrayList.get(groupPosition).getAddedProductChildArrayList().get(childPosition).getAvailable_stock(),
                addedProductArrayList.get(groupPosition).getAddedProductChildArrayList().get(childPosition).getGrade(),
                addedProductArrayList.get(groupPosition).getAddedProductChildArrayList().get(childPosition).getLocation(),
                addedProductArrayList.get(groupPosition).getAddedProductChildArrayList().get(childPosition).getWeight(),
                addedProductArrayList.get(groupPosition).getAddedProductChildArrayList().get(childPosition).getQuantity(),
                addedProductArrayList.get(groupPosition).getAddedProductChildArrayList().get(childPosition).getPrice()
        }).perform();

        if (finish) {
            CustomScheduleJob.scheduleJob(getActivity(), new ComponentName(getActivity(), DONetworkMonitor.class), 2);
            showSnackBar("Store Successfully!");
            page2Reset();
        }
    }


    @Override
    public void createResult(String status) {
        if (creatingLocalDo) {
            for (int i = 0; i < addedProductArrayList.size(); i++) {
                for (int j = 0; j < addedProductArrayList.get(i).getAddedProductChildArrayList().size(); j++) {
                    storeLocalDoItem(i, j, (i == addedProductArrayList.size() - 1 && j == addedProductArrayList.get(i).getAddedProductChildArrayList().size() - 1));
                }
            }
        }
    }

    @Override
    public void readResult(String result) {

    }

    @Override
    public void updateResult(String status) {

    }

    @Override
    public void deleteResult(String status) {

    }

}
