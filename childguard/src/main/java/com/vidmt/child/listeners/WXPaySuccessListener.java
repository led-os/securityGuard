package com.vidmt.child.listeners;

public class WXPaySuccessListener {
	private static WXPaySuccessListener sInstance;
	private OnWXPaySuccessListener mListener;

	private WXPaySuccessListener() {
	}

	public static WXPaySuccessListener get() {
		if (sInstance == null) {
			sInstance = new WXPaySuccessListener();
		}
		return sInstance;
	}

	public void setOnWXPaySuccessListener(OnWXPaySuccessListener listener) {
		mListener = listener;
	}

	public void triggerOnWXPaySuccessListener() {
		if (mListener != null) {
			mListener.onSuccess();
		}
	}

	public interface OnWXPaySuccessListener {
		void onSuccess();
	}

}
