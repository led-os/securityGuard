package com.vidmt.telephone.listeners;

public class ServerConfDownloadFinishedListener {
	private static ServerConfDownloadFinishedListener sInstance;
	private OnServerConfDownloadFinishedListener mListener;

	public static ServerConfDownloadFinishedListener get() {
		if (sInstance == null) {
			sInstance = new ServerConfDownloadFinishedListener();
		}
		return sInstance;
	}

	public interface OnServerConfDownloadFinishedListener {
		public void onDownloadFinished();
	}

	public void setOnServerConfDownloadFinishedListener(OnServerConfDownloadFinishedListener listener) {
		mListener = listener;
	}

	public void triggerOnServerConfDownloadFinishedListener() {
		if (mListener != null) {
			mListener.onDownloadFinished();
		}
	}
}
