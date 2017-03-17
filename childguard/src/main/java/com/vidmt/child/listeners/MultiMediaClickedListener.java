package com.vidmt.child.listeners;

import com.vidmt.xmpp.enums.XmppEnums.ChatType;

public class MultiMediaClickedListener {
	private static MultiMediaClickedListener sInstance;
	private OnMultiMediaClickedListener mOnMultiMediaClickedListener;

	private MultiMediaClickedListener() {
	}

	public static MultiMediaClickedListener get() {
		if (sInstance == null) {
			sInstance = new MultiMediaClickedListener();
		}
		return sInstance;
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

	public interface OnMultiMediaClickedListener {
		void onAudioClick(String mediaUri);

		void onImageClick(String mediaUri);

		void onVideoClick(String mediaUri);
	}

}
