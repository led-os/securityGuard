package com.vidmt.telephone.ui.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CustomViewPager extends ViewPager {
	private boolean isCanScroll = false;

	public CustomViewPager(Context context) {
		super(context);
	}

	public CustomViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setScrollable(boolean isCanScroll) {
		this.isCanScroll = isCanScroll;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (isCanScroll) {
			return super.onTouchEvent(event);
		} else {
			return false;
		}

	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (isCanScroll) {
			return super.onInterceptTouchEvent(event);
		} else {
			return false;
		}

	}
}