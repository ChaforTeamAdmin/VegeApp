package com.jby.admin;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.jby.admin.basket.BasketFragment;
import com.jby.admin.delivery.DeliveryFragment;
import com.jby.admin.others.NetworkConnection;
import com.jby.admin.remark.RemarkFragment;
import com.jby.admin.setting.SettingFragment;
import com.jby.admin.shareObject.ApiDataObject;
import com.jby.admin.shareObject.ApiManager;
import com.jby.admin.shareObject.AsyncTaskManager;
import com.jby.admin.sharePreference.SharedPreferenceManager;
import com.jby.admin.stock.StockFragment;
import com.jby.admin.stock.dialog.CustomerDialog;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.jby.admin.shareObject.CustomToast.CustomToast;
import static com.jby.admin.shareObject.VariableUtils.REFRESH_DELIVERY_ORDER_LIST;
import static com.jby.admin.shareObject.VariableUtils.REFRESH_STOCK_LIST;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        StockFragment.OnFragmentInteractionListener, RemarkFragment.OnFragmentInteractionListener,
        View.OnClickListener, CustomerDialog.CustomerDialogCallBack {
    //new version
    //navigation drawer
    private DrawerLayout mainActivityDrawerLayout;
    private ActionBarDrawerToggle mainActivityActionBarDrawerToggle;
    private NavigationView mainActivityNavigationView;
    private FrameLayout mainActivityFrameLayout;
    /*
     * nav header
     * */
    private TextView mainActivityUsername;
    //actionbar
    private Toolbar actionBar;
    //no connection purpose
    private RelativeLayout noInternetConnectionLayout;
    //progress bar
    public ProgressBar mainActivityProgressBar;
    //customer purpose
    private LinearLayout actionBarCustomerLayout;
    public TextView actionBarCustomer;
    private String customerID = "-1";
    Class fragmentClass;
    public static Fragment fragment;
    //prevent double reload;
    private int lastFragment = -1;
    //fragment
    private StockFragment stockFragment;
    private DeliveryFragment deliveryFragment;
    private Handler handler;
    //exit purpose
    private boolean exit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        Stetho.initializeWithDefaults(this);
        //connection purpose
        noInternetConnectionLayout = findViewById(R.id.no_connection_layout);

        mainActivityDrawerLayout = findViewById(R.id.activity_main_drawer_layout);
        mainActivityNavigationView = findViewById(R.id.activity_main_navigation_view);
        mainActivityFrameLayout = findViewById(R.id.frameLayout);

        View headerView = mainActivityNavigationView.getHeaderView(0);
        mainActivityUsername = headerView.findViewById(R.id.activity_main_username);

        actionBar = findViewById(R.id.toolbar);
        actionBarCustomerLayout = findViewById(R.id.actionbar_customer_layout);
        actionBarCustomer = findViewById(R.id.actionbar_customer);

        mainActivityProgressBar = findViewById(R.id.activity_main_progress_bar);
        handler = new Handler();
    }

    private void objectSetting() {
        mainActivityActionBarDrawerToggle = new ActionBarDrawerToggle(this, mainActivityDrawerLayout, actionBar, R.string.activity_main_open, R.string.activity_main_close);
        mainActivityDrawerLayout.addDrawerListener(mainActivityActionBarDrawerToggle);
        mainActivityActionBarDrawerToggle.syncState();
        mainActivityNavigationView.setNavigationItemSelectedListener(this);
        mainActivityUsername.setText(SharedPreferenceManager.getUsername(this));

        actionBarCustomerLayout.setOnClickListener(this);
        actionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mainActivityDrawerLayout.isDrawerOpen(Gravity.START)) {
                    mainActivityDrawerLayout.openDrawer(Gravity.START);
                } else {
                    mainActivityDrawerLayout.openDrawer(Gravity.START);
                }
            }
        });

        /*
         * get value from notification
         * */
        String channel_id = (getIntent().getStringExtra("channel_id") != null ? getIntent().getStringExtra("channel_id") : "-1");

        registerToken();
        setDefaultFragment(channel_id);
    }

    private void setDefaultFragment(String channel_id) {
        switch (channel_id) {
            case "1":
                displaySelectedScreen(R.id.navigation_remark);
                break;
            case "2":
                displaySelectedScreen(R.id.navigation_stock);
                break;
            case "3":
                displaySelectedScreen(R.id.navigation_delivery);
                break;
            default:
                displaySelectedScreen(R.id.navigation_stock);
        }
    }

    //<-----------------------------------Navigation Drawer-------------------------------------------------------->
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        return mainActivityActionBarDrawerToggle.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
        //calling the method displayselectedscreen and passing the id of selected menu
        displaySelectedScreen(item.getItemId());
        //make this method blank
        return true;
    }

    public void displaySelectedScreen(int itemId) {
        //initializing the fragment object which is selected
        switch (itemId) {
            case R.id.navigation_stock:
                fragmentClass = StockFragment.class;
                break;
            case R.id.navigation_basket:
                fragmentClass = BasketFragment.class;
                setActionBarTitle("Basket");
                break;
            case R.id.navigation_delivery:
                fragmentClass = DeliveryFragment.class;
                setActionBarTitle("Delivery Order");
                break;
            case R.id.navigation_remark:
                fragmentClass = RemarkFragment.class;
                setActionBarTitle("Remark");
                break;
            case R.id.navigation_setting:
                fragmentClass = SettingFragment.class;
                setActionBarTitle("Setting");
                break;
        }
        if (lastFragment != itemId) {
            checkInternetConnection(null);
            hideCustomerLayout(itemId);
        }
        lastFragment = itemId;
        mainActivityDrawerLayout.closeDrawer(Gravity.START);
    }

    public void checkInternetConnection(View view) {
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        renderFragment();
    }

    private void renderFragment() {
        boolean connection = new NetworkConnection(this).checkNetworkConnection();
        mainActivityFrameLayout.setVisibility(connection ? View.VISIBLE : View.GONE);
        noInternetConnectionLayout.setVisibility(connection ? View.GONE : View.VISIBLE);
        if (connection) {
            if (fragment != null) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).replace(R.id.frameLayout, fragment).commit();
            }
            /*
             * register interface
             * */
            if (fragmentClass == StockFragment.class) stockFragment = (StockFragment) fragment;
            else if (fragmentClass == DeliveryFragment.class)
                deliveryFragment = (DeliveryFragment) fragment;

        } else showSnackBar("No Internet Connection!");
    }

    /*------------------------------------------end of navigation-------------------------------------------------------*/
    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.actionbar_customer_layout:
                openCustomerDialog();
                break;
        }
    }

    /*-------------------------------------------------------customer dialog purpose---------------------------------------------*/
    private void openCustomerDialog() {
        DialogFragment dialogFragment = new CustomerDialog();
        FragmentManager fragmentManager = getSupportFragmentManager();
        dialogFragment.show(fragmentManager, "");
    }

    @Override
    public void selectedItem(String name, String id, String address) {
        customerID = id;
        actionBarCustomer.setText(name);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stockFragment.reset();
            }
        }, 200);
    }

    private void hideCustomerLayout(int id) {
        if (id != R.id.navigation_stock) actionBarCustomerLayout.setVisibility(View.GONE);
        else actionBarCustomerLayout.setVisibility(View.VISIBLE);
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    /*---------------------------------------------------------progress bar-----------------------------------------*/
    public void showProgressBar(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainActivityProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    /*----------------------------------------------on back press------------------------------------------------------------------*/
    @Override
    public void onBackPressed() {
        if (lastFragment == R.id.navigation_stock) exit();
        else displaySelectedScreen(R.id.navigation_stock);
    }

    public void exit() {
        if (exit) {
            moveTaskToBack(true);
            finish();
        } else {
            Toast.makeText(this, "Press Back again to Exit.", Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);
        }
    }

    /*------------------------------------------------register token-------------------------------------------------------------------*/
    private void registerToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("FireBase", "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = Objects.requireNonNull(task.getResult()).getToken();
                        Log.d("FireBase", "token: " + token);
                        updateToken(token);
                    }
                });
    }

    private void updateToken(final String token) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<ApiDataObject> apiDataObjectArrayList = new ArrayList<>();
                apiDataObjectArrayList.add(new ApiDataObject("user_id", SharedPreferenceManager.getUserId(getApplicationContext())));
                apiDataObjectArrayList.add(new ApiDataObject("user_type", "admin"));
                apiDataObjectArrayList.add(new ApiDataObject("token", token));
                AsyncTaskManager asyncTaskManager = new AsyncTaskManager(
                        getApplicationContext(),
                        new ApiManager().notification,
                        new ApiManager().getResultParameter(
                                "",
                                new ApiManager().setData(apiDataObjectArrayList),
                                ""
                        )
                );
                asyncTaskManager.execute();

                if (!asyncTaskManager.isCancelled()) {

                    try {
                        JSONObject jsonObjectLoginResponse = asyncTaskManager.get(30000, TimeUnit.MILLISECONDS);
                        if (jsonObjectLoginResponse != null) {
                            Log.d("jsonObject", "jsonObject: " + jsonObjectLoginResponse);
                        } else {
                            CustomToast(getApplicationContext(), "No Network Connection");
                        }
                    } catch (InterruptedException e) {
                        CustomToast(getApplicationContext(), "Interrupted Exception!");
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        CustomToast(getApplicationContext(), "Execution Exception!");
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        CustomToast(getApplicationContext(), "Connection Time Out!");
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /*---------------------------------------------------------------------------other----------------------------------------------------------------------*/

    public void showSnackBar(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT);
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

    public int getActionBarHeight() {
        return actionBar.getLayoutParams().height;
    }

    public void setActionBarTitle(String title) {
        setSupportActionBar(actionBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*
         * when click add more from delivery order detail
         * */
        if (resultCode == REFRESH_STOCK_LIST) {
            setDefaultFragment("2");
            /*
             * set do_id
             * */
            stockFragment.setDo_id(data.getStringExtra("do_id"));
            /*
             * get customer detail
             * */
            selectedItem(data.getStringExtra("name"), data.getStringExtra("id"), data.getStringExtra("address"));
        }
        /*
        * refresh do list when delete*/
        else if (resultCode == REFRESH_DELIVERY_ORDER_LIST) {
            deliveryFragment.reset();
        }
    }
}
