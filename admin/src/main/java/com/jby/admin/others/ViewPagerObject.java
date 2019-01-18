package com.jby.admin.others;

import android.support.v4.app.Fragment;

/**
 * Created by wypan on 12/22/2016.
 */

public class ViewPagerObject {

    private Fragment fragment;
    private String title;

    public ViewPagerObject(Fragment fragment, String title) {
        this.fragment = fragment;
        this.title = title;
    }

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
