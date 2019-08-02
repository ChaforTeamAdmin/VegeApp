package com.jby.admin.stock.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jby.admin.MainActivity;
import com.jby.admin.R;
import com.jby.admin.adapter.stock.ProductGridAdapter;
import com.jby.admin.object.ProductDetailChildObject;
import com.jby.admin.others.SwipeDismissTouchListener;
import com.jby.admin.shareObject.ApiDataObject;
import com.jby.admin.shareObject.ApiManager;
import com.jby.admin.shareObject.AsyncTaskManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.admin.shareObject.CustomToast.CustomToast;


public class AssignProductDialog extends DialogFragment implements View.OnClickListener, ProductGridAdapter.ProductGridAdapterCallBack,
        AbsListView.MultiChoiceModeListener, SpoilDialog.SpoilDialogCallBack, AdapterView.OnItemClickListener {
    View rootView;

    private ProgressBar assignProductDialogProgressBar;
    private GridView assignProductDialogGridView;
    private Button assignProductDialogCancelButton;
    private ProductGridAdapter productGridAdapter;
    private TextView assignProductDialogTitle;
    private ArrayList<ProductDetailChildObject> productDetailChildObjectArrayList;
    AssignProductDialogCallBack assignProductDialogCallBack;
    //not found
    LinearLayout assignProductDialogNotFound;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private Handler handler;
    private String customerID = "-1", farmerID, productID, farmer_name;
    //selected item id
    private List<String> deliveryProductIDList;
    /*
     * delete purpose
     * */
    SparseBooleanArray checkDeleteItem;
    List<String> list = new ArrayList<String>();
    ActionMode actionMode;
    private String type = "";
    /*
     * edit (remark) when customer contact with admin
     * */
    private String fromWhere = "stock_fragment";
    private String weight, do_id;
    private boolean isUpdated = false;
    /*
     * change grade purpose
     * */
    private String grade;
    /*
     * auto select quantitty
     * */
    private EditText assignProductDialogQuantity;
    private Button assignProductDialogQuantityButton;
    private LinearLayout assignProductDialogQuantityLayout;
    /*
     * unavailable id
     * */
    private String[] unavailableIDList;
    /*
     * for multiple spoiled by weight purpose
     * */
    ArrayList<ProductDetailChildObject> spoilWeightlist = new ArrayList<ProductDetailChildObject>();

    public AssignProductDialog() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.assign_product_dialog, container);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        assignProductDialogProgressBar = rootView.findViewById(R.id.assign_product_dialog_progress_bar);
        assignProductDialogGridView = rootView.findViewById(R.id.assign_product_dialog_grid_view);
        assignProductDialogCancelButton = rootView.findViewById(R.id.assign_product_dialog_cancel_button);
        assignProductDialogTitle = rootView.findViewById(R.id.assign_product_dialog_title);
        assignProductDialogNotFound = rootView.findViewById(R.id.assign_product_dialog_not_found);

        assignProductDialogQuantity = rootView.findViewById(R.id.assign_product_dialog_quantity);
        assignProductDialogQuantityButton = rootView.findViewById(R.id.assign_product_dialog_quantity_button);
        assignProductDialogQuantityLayout = rootView.findViewById(R.id.assign_product_dialog_quantity_layout);

        productDetailChildObjectArrayList = new ArrayList<>();
        handler = new Handler();
    }

    private void objectSetting() {
        assignProductDialogCancelButton.setOnClickListener(this);
        assignProductDialogQuantityButton.setOnClickListener(this);
        assignProductDialogCallBack = (AssignProductDialogCallBack) (getParentFragment() != null ? getParentFragment() : getActivity());

        productGridAdapter = new ProductGridAdapter(getActivity(), productDetailChildObjectArrayList, this, fromWhere);

        assignProductDialogGridView.setAdapter(productGridAdapter);
        assignProductDialogGridView.setMultiChoiceModeListener(this);
        assignProductDialogGridView.setOnItemClickListener(this);
        assignProductDialogGridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        Bundle bundle = getArguments();
        if (bundle != null) {
            farmerID = bundle.getString("farmer_id");
            farmer_name = bundle.getString("farmer_name");
            customerID = bundle.getString("customer_id");
            productID = bundle.getString("product_id");

            weight = bundle.getString("weight");
            do_id = bundle.getString("do_id");
            /*
             * check whether is open from stock fragment or delivery order activity
             * */
            fromWhere = (bundle.getString("from_where") != null ? bundle.getString("from_where") : fromWhere);
            /*
             * selected item list
             * */
            deliveryProductIDList = bundle.getStringArrayList("delivery_product_list_id");
            /*
             * crash item while uploading to server
             * */
            if (bundle.getStringArray("unavailable_list") != null) {
                unavailableIDList = bundle.getStringArray("unavailable_list");
                productGridAdapter.setUnavailableIDList(unavailableIDList);
            }
            assignProductDialogTitle.setText(farmer_name);
        }
        showProgressBar(true);
        if (fromWhere.equals("stock_fragment")) {
            //if customer id = -1 then hide
            preSetMultipleSelectLayout();
            //fetch stock
            fetchAllStockByFarmer();
        } else fetchDeliveryOrderItem();
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

    private String getUnavailableID() {
        if (unavailableIDList != null && unavailableIDList.length > 0) {
            String unavailableID = Arrays.toString(unavailableIDList);
            return unavailableID.substring(1, unavailableID.length() - 1);
        } else return "";
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.assign_product_dialog_cancel_button:
                dismiss();
                break;
            case R.id.assign_product_dialog_quantity_button:
                multipleSelectItem();
                break;
        }
    }

    /*---------------------------------------------------------------------------fetch stock (assign purpose)-----------------------------------------------*/
    private void fetchAllStockByFarmer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("product_id", productID));
                apiDataObjectArrayList.add(new ApiDataObject("do_id", do_id));
                apiDataObjectArrayList.add(new ApiDataObject("farmer_id", farmerID));
                apiDataObjectArrayList.add(new ApiDataObject("unavailable_list", getUnavailableID()));
                Log.d("haha", "id:: " + getUnavailableID());
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
                        Log.d("haha", "jsonObject: item: " + jsonObjectLoginResponse);
                        if (jsonObjectLoginResponse != null) {
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                String status = "0";
                                JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("stock_detail");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    //if local array contain same id then set it into status 1 (assigned)
                                    if (deliveryProductIDList != null) {
                                        status = (deliveryProductIDList.contains(jsonArray.getJSONObject(i).getString("id")) ? "1" : jsonArray.getJSONObject(i).getString("status"));
                                    }
                                    productDetailChildObjectArrayList.add(new ProductDetailChildObject(
                                            jsonArray.getJSONObject(i).getString("id"),
                                            jsonArray.getJSONObject(i).getString("price"),
                                            jsonArray.getJSONObject(i).getString("grade"),
                                            jsonArray.getJSONObject(i).getString("date"),
                                            status,
                                            jsonArray.getJSONObject(i).getString("weight"),
                                            jsonArray.getJSONObject(i).getString("self_absorb_weight"),
                                            jsonArray.getJSONObject(i).getString("do_id"),
                                            "0",
                                            "0"));
                                }
                                /*
                                 * preset auto select quantity
                                 * */
                                presetQuantity();
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
                    notifyDataSetChanged();
                    showProgressBar(false);
                }
            }
        }).start();
    }

    private void notifyDataSetChanged() {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                productGridAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showProgressBar(final boolean show) {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                assignProductDialogProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void showNotFound(final boolean show) {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                assignProductDialogNotFound.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    /*
     * assigned item to customer
     * */
    @Override
    public void assignItem(final int position, final String status, final String do_id) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                assignProductDialogCallBack.setDeliveryProductIDList(productDetailChildObjectArrayList.get(position).getId(), do_id);
                assignProductDialogCallBack.updateUnavailableStockArrayList(productDetailChildObjectArrayList.get(position).getId(), farmerID);
                assignProductDialogCallBack.updateListViewQuantity(status);
            }
        }, 200);
    }

    /*
     * remove assigned item
     * */
    @Override
    public void removeItem(final int position) {
        //update local information
        assignProductDialogCallBack.updateListViewQuantity("0");
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("id", productDetailChildObjectArrayList.get(position).getId()));
                apiDataObjectArrayList.add(new ApiDataObject("do_id", productDetailChildObjectArrayList.get(position).getDo_id()));
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
                                    CustomToast(getActivity(), "Remove Successfully!");
                                    productDetailChildObjectArrayList.clear();
                                    fetchAllStockByFarmer();
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

    /*----------------------------------------------------------------------------multiple select (assign purpose)-----------------------------------------*/
    private void multipleSelectItem() {
        try {
            int quantity = (Integer.parseInt(assignProductDialogQuantity.getText().toString()) > productDetailChildObjectArrayList.size() ? productDetailChildObjectArrayList.size() : Integer.parseInt(assignProductDialogQuantity.getText().toString()));
            checkDeleteItem = new SparseBooleanArray();
            for (int i = 0; i < quantity; i++) {
                productGridAdapter.toggleSelection(i);
                if (productDetailChildObjectArrayList.get(i).getDo_id().equals("0") && productDetailChildObjectArrayList.get(i).getStatus().equals("0")) {
                    /*
                     *check delivery order is created or not;
                     */
                    String deliveryOrderId = (!productDetailChildObjectArrayList.get(i).getDo_id().equals("0") ? productDetailChildObjectArrayList.get(i).getDo_id() : "0");
                    /*
                     *initialize status
                     */
                    String status = (productDetailChildObjectArrayList.get(i).getStatus().equals("0") ? "1" : "0");
                    assignItem(i, status, deliveryOrderId);
                    productDetailChildObjectArrayList.get(i).setStatus(status);
                }
            }
        } catch (NumberFormatException e) {
            CustomToast(getActivity(), "Please enter a valid quantity");
        }
    }

    private void preSetMultipleSelectLayout() {
        assignProductDialogQuantityLayout.setVisibility(customerID.equals("-1") ? View.GONE : View.VISIBLE);
    }

    private void presetQuantity() {
        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                assignProductDialogQuantity.append(String.valueOf(productDetailChildObjectArrayList.size()));
            }
        });

    }

    /*-----------------------------------------------------------------multiple delete--------------------------------------------------------------------------*/
    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int position, long l, boolean b) {
        final int checkDeleteItemCount = assignProductDialogGridView.getCheckedItemCount();
        // Set the  CAB title according to total checkDeleteItem items
        actionMode.setTitle(checkDeleteItemCount + "  Selected");

        // Calls  toggleSelection method from ListViewAdapter Class
        productGridAdapter.toggleSelection(position);
        checkDeleteItem = assignProductDialogGridView.getCheckedItemPositions();
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        actionMode.getMenuInflater().inflate(R.menu.delete_action_bar, menu);
        setMenuItemVisibility(menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        this.actionMode = actionMode;
        switch (menuItem.getItemId()) {
            case R.id.select_all:
                final int checkDeleteItemCount = productDetailChildObjectArrayList.size();
                productGridAdapter.removeSelection();
                for (int i = 0; i < checkDeleteItemCount; i++) {
                    assignProductDialogGridView.setItemChecked(i, true);
                }
                actionMode.setTitle(checkDeleteItemCount + "  Selected");
                return true;
            case R.id.spoiled:
                type = "spoiled";
                deleteConfirmationDialog();
                return true;
            case R.id.spoiled_by_weight:
                openSpoilDialog();
                return true;
            case R.id.delete:
                type = "delete";
                deleteConfirmationDialog();
                return true;
            case R.id.grade_a:
                grade = "A";
                changeGradeConfirmationDialog();
                return true;
            case R.id.grade_b:
                grade = "B";
                changeGradeConfirmationDialog();
                return true;
            case R.id.farmer_a:
                grade = "FA";
                changeGradeConfirmationDialog();
                return true;
            case R.id.farmer_b:
                grade = "FB";
                changeGradeConfirmationDialog();
                return true;
            case R.id.unknown:
                grade = "unknown";
                changeGradeConfirmationDialog();
                return true;
            default:
                return false;
        }
    }

    private void setMenuItemVisibility(Menu menu) {
        menu.findItem(R.id.spoiled).setVisible(fromWhere.equals("stock_fragment"));
        menu.findItem(R.id.spoiled_by_weight).setVisible(fromWhere.equals("stock_fragment"));
        menu.findItem(R.id.grade_a).setVisible(fromWhere.equals("stock_fragment"));
        menu.findItem(R.id.grade_b).setVisible(fromWhere.equals("stock_fragment"));
        menu.findItem(R.id.farmer_a).setVisible(fromWhere.equals("stock_fragment"));
        menu.findItem(R.id.farmer_b).setVisible(fromWhere.equals("stock_fragment"));
        menu.findItem(R.id.unknown).setVisible(fromWhere.equals("stock_fragment"));
        menu.findItem(R.id.delete).setTitle(fromWhere.equals("stock_fragment") ? "Delete" : "Remove");
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        productGridAdapter.removeSelection();
        list.clear();
    }

    public void deleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Warning");
        builder.setMessage(fromWhere.equals("stock_fragment") ? "Are you sure you want to remove these item?" : "Are you sure you want to remove these item from this DO?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "I'm Sure",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (fromWhere.equals("stock_fragment")) deleteStock();
                        else removeItemFromDO();
                    }
                });

        builder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public ActionMode getActionMode() {
        return actionMode;
    }

    public ArrayList<String> getSelectedItem() {
        for (int i = 0; i < assignProductDialogGridView.getCount(); i++) {
            if (checkDeleteItem.get(i)) {
                list.add(productDetailChildObjectArrayList.get(i).getId());
            }
        }
        return (ArrayList<String>) list;
    }

    /*
     * delete stock
     * */
    public void deleteStock() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("id", TextUtils.join(", ", getSelectedItem())));
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
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    CustomToast(getActivity(), "Remove Successfully!");
                                    updateListAfterDelete();
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

    /*
     * remove item from delivery order
     * */
    public void removeItemFromDO() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("stock_id", TextUtils.join(", ", getSelectedItem())));
                apiDataObjectArrayList.add(new ApiDataObject("do_id", do_id));
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
                                    CustomToast(getActivity(), "Remove Successfully!");
                                    reset();
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
                    }
                }
            }
        }).start();
    }

    /*
     * update list after delete successfully
     */
    private void updateListAfterDelete() {
        for (int i = assignProductDialogGridView.getCount() - 1; i >= 0; i--) {
            if (productGridAdapter.getSelectedIds().get(i)) {
                productDetailChildObjectArrayList.remove(i);
            }
        }
        getActionMode().finish();
        productGridAdapter.notifyDataSetChanged();
    }

    private void editDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Note");
        builder.setMessage("Remove this item may brings effect to the delivery order. Are you sure that you want to do so?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        removeItem(position);
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

    /*------------------------------------------------------------------spoil by weight---------------------------------------------------------------*/
    /*
     * open spoil dialog
     * */
    private void openSpoilDialog() {
        Bundle bundle = new Bundle();
        bundle.putSerializable("spoil_weight_list", getSpoilWeightList());

        DialogFragment dialogFragment = new SpoilDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getChildFragmentManager(), "");
    }

    public ArrayList<ProductDetailChildObject> getSpoilWeightList() {
        for (int i = 0; i < assignProductDialogGridView.getCount(); i++) {
            if (checkDeleteItem.get(i)) {
                spoilWeightlist.add(new ProductDetailChildObject(
                        productDetailChildObjectArrayList.get(i).getId(),
                        productDetailChildObjectArrayList.get(i).getWeight()
                ));
            }
        }
        return spoilWeightlist;
    }

    /*---------------------------------------------------------------delivery order purpose-----------------------------------------------------------*/
    public void changeGradeConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Warning");
        builder.setMessage("Are you sure you want to change the grade of these item?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "I'm Sure",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        changeGrade();
                    }
                });

        builder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void changeGrade() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("id", TextUtils.join(", ", getSelectedItem())));
                apiDataObjectArrayList.add(new ApiDataObject("grade", grade));
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
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    CustomToast(getActivity(), "Update Successfully!");
                                    reset();
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

    /*---------------------------------------------------------------delivery order purpose-----------------------------------------------------------*/
    private void fetchDeliveryOrderItem() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("do_id", do_id));
                apiDataObjectArrayList.add(new ApiDataObject("product_id", productID));
                apiDataObjectArrayList.add(new ApiDataObject("farmer_id", farmerID));
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
                        Log.d("haha", "jsonObject: do item: " + jsonObjectLoginResponse);
                        Log.d("haha", "jsonObject: do id: " + do_id);
                        Log.d("haha", "jsonObject: do product: " + productID);
                        Log.d("haha", "jsonObject: do farmer: " + farmerID);
                        if (jsonObjectLoginResponse != null) {
                            if (jsonObjectLoginResponse.getString("status").equals("1")) {
                                JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("item");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    productDetailChildObjectArrayList.add(new ProductDetailChildObject(
                                            jsonArray.getJSONObject(i).getString("id"),
                                            jsonArray.getJSONObject(i).getString("weight"),
                                            jsonArray.getJSONObject(i).getString("grade"),
                                            jsonArray.getJSONObject(i).getString("date"),
                                            jsonArray.getJSONObject(i).getString("do_id")));
                                }
                            } else showNotFound(true);
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
                    notifyDataSetChanged();
                    showProgressBar(false);
                }
            }
        }).start();
    }

    @Override
    public void reset() {
        //for on back press purpose
        isUpdated = true;

        productDetailChildObjectArrayList.clear();
        spoilWeightlist.clear();

        showProgressBar(true);
        if (fromWhere.equals("stock_fragment")) fetchAllStockByFarmer();
        else fetchDeliveryOrderItem();
        /*
         * close action bar
         * */
        if (getActionMode() != null) getActionMode().finish();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (isUpdated) {
            assignProductDialogCallBack.reset();
        }
        assignProductDialogCallBack.orderByPriority();
        super.onDismiss(dialog);

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (fromWhere.equals("stock_fragment")) {
            if (((MainActivity) Objects.requireNonNull(getActivity())).getCustomerID().equals("-1"))
                Toast.makeText(getActivity(), "Please select a customer!", Toast.LENGTH_SHORT).show();
            else {
                //new item
                if (productDetailChildObjectArrayList.get(i).getDo_id().equals("0") || unavailableIDList != null) {
                    productGridAdapter.tick(view.findViewById(R.id.product_detail_child_list_view_ticked_layout));
                    /*
                     *check delivery order is created or not;
                     */
                    String deliveryOrderId = (!productDetailChildObjectArrayList.get(i).getDo_id().equals("0") ? productDetailChildObjectArrayList.get(i).getDo_id() : "0");
                    /*
                     *initialize status
                     */
                    String status = (productDetailChildObjectArrayList.get(i).getStatus().equals("0") ? "1" : "0");
                    assignItem(i, status, deliveryOrderId);
                    productDetailChildObjectArrayList.get(i).setStatus(status);
                }
                //edit item
                else editDialog(i);
            }
        }
    }

    /*------------------------------------------------------------call back--------------------------------------------------------------------------*/

    public interface AssignProductDialogCallBack {
        void updateListViewQuantity(String status);

        void setDeliveryProductIDList(String id, String deliveryOrderID);

        void reset();

        void orderByPriority();

        void updateUnavailableStockArrayList(String selectedID, String farmerID);
    }

}