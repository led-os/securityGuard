package com.vidmt.telephone.listeners;

import java.util.ArrayList;
import java.util.List;

public class TabChangedListener {
	private static TabChangedListener sInstance;
	private List<OnTabChangedListener> mOnTabChangedListeners = new ArrayList<OnTabChangedListener>();

	public static TabChangedListener get() {
		if (sInstance == null) {
			sInstance = new TabChangedListener();
		}
		return sInstance;
	}

	public interface OnTabChangedListener {
		public void onTabChange(int index);
	}

	public void setOnTabChangedListener(OnTabChangedListener listener) {
		mOnTabChangedListeners.add(listener);
	}
	
	public void removeOnTabChangedListener(OnTabChangedListener listener) {
		mOnTabChangedListeners.remove(listener);
	}

	public void triggerOnTabChangedListener(int index) {
		for (OnTabChangedListener listener : mOnTabChangedListeners) {
			listener.onTabChange(index);
		}
	}

}
