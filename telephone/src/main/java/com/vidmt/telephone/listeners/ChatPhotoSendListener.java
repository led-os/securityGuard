package com.vidmt.telephone.listeners;

import com.vidmt.telephone.entities.ChatRecord;

public class ChatPhotoSendListener {
	private static ChatPhotoSendListener sInstance;
	private OnChatPhotoSendListener mOnChatPhotoSendListener;

	public static ChatPhotoSendListener get() {
		if (sInstance == null) {
			sInstance = new ChatPhotoSendListener();
		}
		return sInstance;
	}

	private ChatPhotoSendListener() {
	}

	public interface OnChatPhotoSendListener {
		public void onChatPhotoSend(ChatRecord chatRecord);
	}

	public void setOnChatPhotoSendListener(OnChatPhotoSendListener listener) {
		mOnChatPhotoSendListener = listener;
	}

	public void triggerOnChatPhotoSendListener(ChatRecord chatRecord) {
		if (mOnChatPhotoSendListener != null) {
			mOnChatPhotoSendListener.onChatPhotoSend(chatRecord);
		}
	}
}
