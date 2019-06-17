package com.jby.admin.others;

import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

public abstract class DoubleClickListener implements AdapterView.OnItemClickListener {

    // The time in which the second tap should be done in order to qualify as
    // a double click
    private static final long DEFAULT_QUALIFICATION_SPAN = 200;
    private long doubleClickQualificationSpanInMillis;
    private long timestampLastClick;

    public DoubleClickListener() {
        doubleClickQualificationSpanInMillis = DEFAULT_QUALIFICATION_SPAN;
        timestampLastClick = 0;
    }

    public DoubleClickListener(long doubleClickQualificationSpanInMillis) {
        this.doubleClickQualificationSpanInMillis = doubleClickQualificationSpanInMillis;
        timestampLastClick = 0;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d("double click", "SystemClock.elapsedRealtime() - timestampLastClick: " + (SystemClock.elapsedRealtime() - timestampLastClick));
        Log.d("double click", "doubleClickQualificationSpanInMillis: " + (doubleClickQualificationSpanInMillis));
        if ((SystemClock.elapsedRealtime() - timestampLastClick) < doubleClickQualificationSpanInMillis) {
            onDoubleClick(i, l);
        } else {
            onItemClick(view, i, l);
            timestampLastClick = SystemClock.elapsedRealtime();
        }
    }

    public abstract void onDoubleClick(int i, long l);

    protected abstract void onItemClick(View view, int i, long l);
}