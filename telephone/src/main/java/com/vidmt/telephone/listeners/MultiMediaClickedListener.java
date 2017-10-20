package com.vidmt.telephone.listeners;

import com.vidmt.xmpp.enums.XmppEnums.ChatType;

public class MultiMediaClickedListener {
	private static MultiMediaClickedListener sInstance;
	private OnMultiMediaClickedListener mOnMultiMediaClickedListener;

	public static MultiMediaClickedListener get() {
		if (sInstance == null) {
			sInstance = new MultiMediaClickedListener();
		}
		return sInstance;
	}

	private MultiMediaClickedListener() {
	}

	public static interface OnMultiMediaClickedListener {
		public void onAudioClick(String mediaUri);

		public void onImageClick(String mediaUri);

		public void onVideoClick(String mediaUri);
	}

	public void setOnMultiMediaClickedListener(OnMultiMediaClickedListener listener) {
		mOnMultiMediaClickedListener = listener;
	}

	public void triggerOnMultiMediaClickedListener(ChatType type, String data) {
		if (type == ChatType.AUDIO) {
			mOnMultiMediaClickedListener.onAudioClick(data);
		} else if (type == ChatType.IMAGE) {
			mOnMultiMediaClickedListener.onImageClick(data);
		} else if (type == ChatType.VIDEO) {
			mOnMultiMediaClickedListener.onVideoClick(data);
		}
	}

}
