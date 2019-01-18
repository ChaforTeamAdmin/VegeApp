package com.jby.vegeapp.others;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by wypan on 1/6/2017.
 */

public class SquareHeightLinearLayout extends LinearLayout {
    public SquareHeightLinearLayout(Context context) {
        super(context);
    }

    public SquareHeightLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareHeightLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SquareHeightLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size = getMeasuredHeight();
        setMeasuredDimension(size, size);
        super.onMeasure(MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY));
    }
}
