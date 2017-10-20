package com.vidmt.telephone.listeners;

public class MsgFriendFragChangedListener {
	private static MsgFriendFragChangedListener sInstance;
	private OnMsgFriendFragChangedListener mListener;

	public static MsgFriendFragChangedListener get() {
		if (sInstance == null) {
			sInstance = new MsgFriendFragChangedListener();
		}
		return sInstance;
	}

	public interface OnMsgFriendFragChangedListener {
		public void onFragChange(int index);
	}

	public void setOnMsgFriendFragChangedListener(OnMsgFriendFragChangedListener listener) {
		mListener = listener;
	}

	public void triggerOnMsgFriendFragChangedListener(int index) {
		if (mListener != null) {
			mListener.onFragChange(index);
		}
	}
}
