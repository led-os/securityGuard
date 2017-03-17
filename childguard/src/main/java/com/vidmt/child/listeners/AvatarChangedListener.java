package com.vidmt.child.listeners;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

public class AvatarChangedListener {
	private static AvatarChangedListener sInstance;
	private List<OnAvatarChangedListener> mOnAvatarChangedListeners = new ArrayList<OnAvatarChangedListener>();

	private AvatarChangedListener() {
	}

	public static AvatarChangedListener get() {
		if (sInstance == null) {
			sInstance = new AvatarChangedListener();
		}
		return sInstance;
	}

	public void addOnAvatarChangedListener(OnAvatarChangedListener listener) {
		mOnAvatarChangedListeners.add(listener);
	}

	public void removeOnAvatarChangedListener(OnAvatarChangedListener listener) {
		mOnAvatarChangedListeners.remove(listener);
	}

	public void triggerOnAvatarChanged(boolean forParent, Bitmap bm) {
		for (OnAvatarChangedListener listener : mOnAvatarChangedListeners) {
			listener.onAvatarChanged(forParent, bm);
		}
	}

	public interface OnAvatarChangedListener {
		void onAvatarChanged(boolean forParent, Bitmap bm);
	}
}
