package com.plugable.mcommerceapp.cpmvp1.utils.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;

import androidx.viewpager.widget.ViewPager;

public class WrappingViewPager extends ViewPager {
    private int mCurrentPagePosition = 0;

    private int[] heightArr = new int[3];  // allocating memory to array
    public WrappingViewPager(Context context) {
        super(context);
    }

    public WrappingViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int mode = MeasureSpec.getMode(heightMeasureSpec);
        if (mode == MeasureSpec.UNSPECIFIED || mode == MeasureSpec.AT_MOST) {
            // super has to be called in the beginning so the child views can be initialized.
            // super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int height = 0;
            int numOfElements = getChildCount();
            for (int i = 0; i < numOfElements; i++) {
                View child = getChildAt(i);
                child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                int h = child.getMeasuredHeight();
                height = h;
                //if (h > height) height = h;
                heightArr[i]=height;
            }
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightArr[mCurrentPagePosition], MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    public void reMeasureCurrentPage(int position) {
        mCurrentPagePosition = position;
        requestLayout();
    }

}