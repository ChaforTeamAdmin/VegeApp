package com.jby.admin;

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
import com.jby.admin.basket.BasketFragment;
import com.jby.admin.others.NetworkConnection;
import com.jby.admin.remark.RemarkFragment;
import com.jby.admin.stock.StockFragment;
import com.jby.admin.stock.dialog.CustomerDialog;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
        StockFragment.OnFragmentInteractionListener, RemarkFragment.OnFragmentInteractionListener,
        View.OnClickListener, CustomerDialog.CustomerDialogCallBack {

    //navigation drawer
    private DrawerLayout mainActivityDrawerLayout;
    private ActionBarDrawerToggle mainActivityActionBarDrawerToggle;
    private NavigationView mainActivityNavigationView;
    private FrameLayout mainActivityFrameLayout;
    //actionbar
    private Toolbar actionBar;
    //no connection purpose
    private RelativeLayout noInternetConnectionLayout;
    private TextView reconnectButton;
    //progress bar
    public ProgressBar mainActivityProgressBar;
    //customer purpose
    private LinearLayout actionBarCustomerLayout;
    private TextView actionBarCustomer;
    private String customerID = "-1";
    Class fragmentClass;
    public static Fragment fragment;
    //prevent double reload;
    private int lastFragment = R.id.navigation_stock;
    //fragment
    private StockFragment stockFragment;
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
        fragmentClass = StockFragment.class;
        checkInternetConnection(null);
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
                break;
            case R.id.navigation_remark:
                fragmentClass = RemarkFragment.class;
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
            if (fragmentClass == StockFragment.class) stockFragment = (StockFragment) fragment;
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

    /*---------------------------------------------------------progress bar-----------------------------------------*/
    public void showProgressBar(boolean show) {
        mainActivityProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
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
}
