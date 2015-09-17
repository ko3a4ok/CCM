package com.mesosphere.ccm;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

/**
 * Created by ko3a4ok on 17.09.15.
 */
public class CustomSwipeRefreshLayout extends SwipeRefreshLayout{
    private OnChildScrollUpListener mScrollListenerNeeded;

    public interface OnChildScrollUpListener {
        boolean canChildScrollUp();
    }

    public CustomSwipeRefreshLayout(Context context) {
        super(context);
    }
    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Listener that controls if scrolling up is allowed to child views or not
     */
    public void setOnChildScrollUpListener(OnChildScrollUpListener listener) {
        mScrollListenerNeeded = listener;
    }

    @Override
    public boolean canChildScrollUp() {
        if (mScrollListenerNeeded == null) {
            System.err.println("listener is not defined!");
        }
        return mScrollListenerNeeded != null && mScrollListenerNeeded.canChildScrollUp();
    }}
