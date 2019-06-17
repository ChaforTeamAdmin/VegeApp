package com.jby.admin.remark;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jby.admin.MainActivity;
import com.jby.admin.R;
import com.jby.admin.adapter.RemarkAdapter;
import com.jby.admin.object.RemarkChildObject;
import com.jby.admin.object.ExpandableParentObject;
import com.jby.admin.others.NetworkConnection;
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

import static com.jby.admin.shareObject.CustomToast.CustomToast;

public class RemarkFragment extends Fragment implements ExpandableListView.OnGroupClickListener, RemarkAdapter.RemarkAdapterCallBack,
        EditRemarkDialog.RemarkDialogCallBack {
    private View rootView;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;

    private ExpandableListView remarkFragmentListView;
    private RemarkAdapter remarkAdapter;
    private ArrayList<ExpandableParentObject> remarkParentObjectArrayList;
    private int groupPosition;
    //not found layout
    private RelativeLayout notFoundLayout;
    private ImageView notFoundIcon;
    private TextView notFoundLabel;
    //    asyncTask
    AsyncTaskManager asyncTaskManager;
    JSONObject jsonObjectLoginResponse;
    ArrayList<ApiDataObject> apiDataObjectArrayList;
    private Handler handler;

    public RemarkFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static RemarkFragment newInstance(String param1, String param2) {
        RemarkFragment fragment = new RemarkFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_remark, container, false);
        objectInitialize();
        objectSetting();
        return rootView;
    }

    private void objectInitialize() {
        //not found layout
        notFoundLayout = rootView.findViewById(R.id.not_found_layout);
        notFoundIcon = rootView.findViewById(R.id.not_found_layout_icon);
        notFoundLabel = rootView.findViewById(R.id.not_found_layout_label);

        remarkFragmentListView = rootView.findViewById(R.id.remark_fragment_list_view);
        remarkParentObjectArrayList = new ArrayList<>();
        remarkAdapter = new RemarkAdapter(getActivity(), remarkParentObjectArrayList, this);
        handler = new Handler();
    }

    private void objectSetting() {
        remarkFragmentListView.setAdapter(remarkAdapter);
        remarkFragmentListView.setOnGroupClickListener(this);
        setupNotFoundLayout();
        checkInternetConnection(null);
    }

    private void setupNotFoundLayout() {
        notFoundIcon.setImageDrawable(getResources().getDrawable(R.drawable.not_found));
        notFoundLabel.setText("No Remark Is Found");
    }

    public void checkInternetConnection(View view) {
        boolean networkConnection = new NetworkConnection(getActivity()).checkNetworkConnection();
        if (networkConnection) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fetchParentItem();
                }
            }, 400);
        }
        remarkFragmentListView.setVisibility(networkConnection ? View.VISIBLE : View.GONE);
        showProgressBar(networkConnection);
    }

    private void showProgressBar(final boolean show) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((MainActivity) Objects.requireNonNull(getActivity())).showProgressBar(show);
            }
        });
    }

    /*-----------------------------------------------------------------list view purpose---------------------------------------------------------*/
    private void fetchParentItem() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("fetch", "1"));

                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().remark,
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
                                JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("remark");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    remarkParentObjectArrayList.add(new ExpandableParentObject(jsonArray.getJSONObject(i).getString("created_date")));
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
                setVisibility();
                preOpenChild();

            }
        }).start();
    }

    @Override
    public boolean onGroupClick(ExpandableListView expandableListView, View view, final int i, long l) {
        groupPosition = i;
        if (expandableListView.isGroupExpanded(i)) expandableListView.collapseGroup(i);
        else {
            //close view
            closeOtherChildView(i);
            remarkParentObjectArrayList.get(i).getRemarkChildObjectArrayList().clear();
            fetchChildItem(i);
        }
        return true;
    }

    private void closeOtherChildView(int position) {
        for (int i = 0; i < remarkParentObjectArrayList.size(); i++) {
            if (i != position) remarkFragmentListView.collapseGroup(i);
        }
    }

    /*------------------------------------child-------------------------------*/
    private void fetchChildItem(final int position) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("date", remarkParentObjectArrayList.get(position).getDate()));
                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().remark,
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
                                JSONArray jsonArray = jsonObjectLoginResponse.getJSONArray("value").getJSONObject(0).getJSONArray("remark");
                                setChildValue(jsonArray, position);
                            } else {
                                remarkParentObjectArrayList.remove(groupPosition);
                                setVisibility();
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

    private void setChildValue(JSONArray jsonArray, final int position) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                remarkParentObjectArrayList.get(position).setRemarkChildObjectArrayList(setChildObject(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                remarkFragmentListView.expandGroup(position);
                remarkFragmentListView.setSelectedGroup(position);
                notifyDataSetChanged();
            }
        });
    }

    private RemarkChildObject setChildObject(JSONObject jsonObject) {
        RemarkChildObject object = null;
        try {
            object = new RemarkChildObject(
                    jsonObject.getString("id"),
                    jsonObject.getString("product_id"),
                    jsonObject.getString("product"),
                    jsonObject.getString("farmer"),
                    jsonObject.getString("customer"),
                    jsonObject.getString("pick_up_driver"),
                    jsonObject.getString("deliver_driver"),
                    jsonObject.getString("farmer_weight"),
                    jsonObject.getString("customer_weight"),
                    jsonObject.getString("remark_type"),
                    jsonObject.getString("remark"),
                    jsonObject.getString("remark_status"),
                    jsonObject.getString("status"),
                    jsonObject.getString("created_time"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    private void preOpenChild() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (remarkParentObjectArrayList.size() > 0) fetchChildItem(0);
            }
        });
    }

    private void notifyDataSetChanged() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                remarkAdapter.notifyDataSetChanged();
            }
        });
    }

    private void setVisibility() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                remarkFragmentListView.setVisibility(remarkParentObjectArrayList.size() > 0 ? View.VISIBLE : View.GONE);
                notFoundLayout.setVisibility(remarkParentObjectArrayList.size() > 0 ? View.GONE : View.VISIBLE);
                showProgressBar(false);
                notifyDataSetChanged();
            }
        });
    }

    /*----------------------------------------------------------adapter call back-----------------------------------------------------------------*/
    @Override
    public void approved(int position) {
        confirmationDialog("approve", position);
    }

    @Override
    public void undo(int position) {
        confirmationDialog("undo", position);
    }

    @Override
    public void edit(int position) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("remark_child_object", remarkParentObjectArrayList.get(groupPosition).getRemarkChildObjectArrayList().get(position));
        bundle.putBoolean("from_where" , true);

        DialogFragment dialogFragment = new EditRemarkDialog();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getChildFragmentManager(), "");
    }

    private void selectAction(String type, int position) {
        if (type.equals("approve")) {
            RemarkChildObject object = remarkParentObjectArrayList.get(groupPosition).getRemarkChildObjectArrayList().get(position);
            if (!object.getRemark_status().equals("1")) updateWeightRemark(object);
            else updateMissingRemark(object, "missing");
        } else {
            RemarkChildObject object = remarkParentObjectArrayList.get(groupPosition).getRemarkChildObjectArrayList().get(position);
            updateMissingRemark(object, "find_back");
        }
    }

    public void confirmationDialog(final String type, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Warning");
        builder.setMessage("Are you sure that you perform this action? \nIt may affect overall result");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Confirm",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                        selectAction(type, position);
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

    /*--------------------------------------------------------------approved remark---------------------------------------------------------------*/
    private void updateWeightRemark(final RemarkChildObject object) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("id", object.getId()));
                apiDataObjectArrayList.add(new ApiDataObject("product_id", object.getProduct_id()));
                apiDataObjectArrayList.add(new ApiDataObject("remark_weight", object.getRemark()));
                apiDataObjectArrayList.add(new ApiDataObject("type", object.getRemark_type()));
                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().remark,
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
                                reset();
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

    private void updateMissingRemark(final RemarkChildObject object, final String type) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("id", object.getId()));
                apiDataObjectArrayList.add(new ApiDataObject("type", type));
                asyncTaskManager = new AsyncTaskManager(
                        getActivity(),
                        new ApiManager().remark,
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
                                reset();
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

    public void reset() {
        remarkParentObjectArrayList.get(groupPosition).getRemarkChildObjectArrayList().clear();
        fetchChildItem(groupPosition);
        ((MainActivity) Objects.requireNonNull(getActivity())).showSnackBar("Remark Updated!");
        setVisibility();
    }

    /*---------------------------------------------------------------fragment setting------------------------------------------------------------*/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
