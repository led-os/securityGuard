package com.vidmt.telephone.listeners;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class UserInfoChangedListener {
	private static UserInfoChangedListener sInstance;
	private List<OnUserInfoChangedListener> mListeners = new CopyOnWriteArrayList<OnUserInfoChangedListener>();

	public static UserInfoChangedListener get() {
		if (sInstance == null) {
			sInstance = new UserInfoChangedListener();
		}
		return sInstance;
	}

	private UserInfoChangedListener() {
	}

	public interface OnUserInfoChangedListener {
		public void onInfoChanged(String uid, Map<String, String> map);
	}

	public void addOnUserInfoChangedListener(OnUserInfoChangedListener listener) {
		mListeners.add(listener);
	}

	public void removeOnUserInfoChangedListener(OnUserInfoChangedListener listener) {
		mListeners.remove(listener);
	}

	public void triggerOnUserInfoChangedListener(String uid, Map<String, String> map) {
		if (mListeners == null) {
			return;
		}
		for (OnUserInfoChangedListener listener : mListeners) {
			listener.onInfoChanged(uid, map);
		}
	}
}
