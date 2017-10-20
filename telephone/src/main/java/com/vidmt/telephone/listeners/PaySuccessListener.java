package com.vidmt.telephone.listeners;

import java.util.ArrayList;
import java.util.List;

import com.vidmt.telephone.utils.Enums.PayType;

public class PaySuccessListener {
	private static PaySuccessListener sInstance;
	private List<OnPaySuccessListener> mListeners = new ArrayList<OnPaySuccessListener>();

	public static PaySuccessListener get() {
		if (sInstance == null) {
			sInstance = new PaySuccessListener();
		}
		return sInstance;
	}

	private PaySuccessListener() {
	}

	public interface OnPaySuccessListener {
		public void onSuccess(PayType type);
	}

	public void addOnPaySuccessListener(OnPaySuccessListener listener) {
		mListeners.add(listener);
	}

	public void removeOnPaySuccessListener(OnPaySuccessListener listener) {
		mListeners.remove(listener);
	}

	public void triggerOnPaySuccessListener(PayType type) {
		for (OnPaySuccessListener listener : mListeners) {
			listener.onSuccess(type);
		}
	}

}
