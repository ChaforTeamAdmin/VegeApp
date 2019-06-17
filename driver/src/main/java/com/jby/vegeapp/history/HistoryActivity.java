package com.jby.vegeapp.history;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.jby.vegeapp.R;
import com.jby.vegeapp.history.basket.BasketHistory;
import com.jby.vegeapp.history.delivery.DeliveryHistory;
import com.jby.vegeapp.history.pick_up.PickUpHistory;
import com.jby.vegeapp.others.NetworkConnection;
import com.jby.vegeapp.others.ViewPagerAdapter;
import com.jby.vegeapp.others.ViewPagerObject;

import java.util.ArrayList;
import java.util.Objects;

import static com.jby.vegeapp.Utils.VariableUtils.REFRESH_AVAILABLE_QUANTITY;
import static com.jby.vegeapp.Utils.VariableUtils.UPDATE_LIST;

public class HistoryActivity extends AppCompatActivity {
    //actionbar
    private Toolbar toolbar;
    //not internet connection
    private RelativeLayout noInternetConnectionLayout;
    //progress bar
    private ProgressBar progressBar;
    //fragment
    private BasketHistory basketHistory;
    private Fragment fragmentControl;
    //tab purpose
    private ViewPagerAdapter adapter;
    private TabLayout tabLayout;
    private ArrayList<ViewPagerObject> viewPagerObjectArrayList;
    private ViewPager viewPager;
    int[] defaultIcon = new int[]{R.drawable.basket_icon, R.drawable.pick_up_icon, R.drawable.delivery_icon};
    int[] selectedIcon = new int[]{R.drawable.selected_basket_icon, R.drawable.selected_pick_up_icon, R.drawable.selected_delivery_icon};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        objectInitialize();
        objectSetting();
    }

    private void objectInitialize() {
        //no internet connection
        noInternetConnectionLayout = findViewById(R.id.no_connection_layout);
        //progress bar
        progressBar = findViewById(R.id.progress_bar);
        tabLayout = findViewById(R.id.activity_history_tab_layout);
        viewPager = findViewById(R.id.activity_history_pager);
    }

    private void objectSetting() {
        tabLayout.addTab(tabLayout.newTab().setText("Basket").setIcon(defaultIcon[0]));
        tabLayout.addTab(tabLayout.newTab().setText("Pick Up").setIcon(defaultIcon[1]));
        tabLayout.addTab(tabLayout.newTab().setText("Delivery").setIcon(defaultIcon[2]));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        adapter = new ViewPagerAdapter(getSupportFragmentManager(), setUpFragment());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabOnClick();
        setTabLayoutIcon(0);
        setupActionBar();
        checkInternetConnection(null);

    }

    @Override
    public void onBackPressed() {
        //for update available basket quantity in home activity
        setResult(REFRESH_AVAILABLE_QUANTITY);
        super.onBackPressed();

    }

    /*-------------------------------------------------------------tab layout view pager setting--------------------------------------------------*/
    private ArrayList<ViewPagerObject> setUpFragment() {
        viewPagerObjectArrayList = new ArrayList<>();
        viewPagerObjectArrayList.add(new ViewPagerObject(new BasketHistory(), "Basket"));
        viewPagerObjectArrayList.add(new ViewPagerObject(new PickUpHistory(), "Pick Up"));
        viewPagerObjectArrayList.add(new ViewPagerObject(new DeliveryHistory(), "Delivery"));
        return viewPagerObjectArrayList;
    }

    private void tabOnClick() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                setTabLayoutIcon(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void setTabLayoutIcon(int position) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            if (i == position) tabLayout.getTabAt(i).setIcon(selectedIcon[i]);
            else tabLayout.getTabAt(i).setIcon(defaultIcon[i]);
        }
    }

    private void fragmentControl() {
        fragmentControl = adapter.getItem(viewPager.getCurrentItem());
        switch (viewPager.getCurrentItem()) {
            case 0:
                basketHistory = (BasketHistory) fragmentControl;
                basketHistory.reset();
                break;
        }
    }
    /*-------------------------------------------------------fragment shareable setting-------------------------------------------------*/
//    public void setupNotFoundLayout(boolean show,int position) {
//        switch(position){
//            case 0:
//                notFoundIcon.setImageDrawable(getResources().getDrawable(R.drawable.no_item_to_deliver_icon));
//                notFoundLabel.setText("No record found!");
//                break;
//        }
//        notFoundLayout.setVisibility(show ? View.VISIBLE : View.GONE);
//    }

    private void setupActionBar() {
        //actionbar
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("History");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    public void checkInternetConnection(View view) {
        boolean networkConnection = new NetworkConnection(this).checkNetworkConnection();
        if (networkConnection) {
        }

        viewPager.setVisibility(networkConnection ? View.VISIBLE : View.GONE);
        tabLayout.setVisibility(networkConnection ? View.VISIBLE : View.GONE);
        noInternetConnectionLayout.setVisibility(networkConnection ? View.GONE : View.VISIBLE);
    }

    public void showProgressBar(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == UPDATE_LIST) {
            fragmentControl();
        }
    }
}
