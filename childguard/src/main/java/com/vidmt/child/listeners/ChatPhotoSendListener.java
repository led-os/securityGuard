package com.vidmt.child.listeners;

import com.vidmt.child.entities.ChatRecord;

public class ChatPhotoSendListener {
	private static ChatPhotoSendListener sInstance;
	private OnChatPhotoSendListener mOnChatPhotoSendListener;

	private ChatPhotoSendListener() {
	}

	public static ChatPhotoSendListener get() {
		if (sInstance == null) {
			sInstance = new ChatPhotoSendListener();
		}
		return sInstance;
	}

	public void setOnChatPhotoSendListener(OnChatPhotoSendListener listener) {
		mOnChatPhotoSendListener = listener;
	}

	public void triggerOnChatPhotoSendListener(ChatRecord chatRecord) {
		if (mOnChatPhotoSendListener != null) {
			mOnChatPhotoSendListener.onChatPhotoSend(chatRecord);
		}
	}

	public interface OnChatPhotoSendListener {
		void onChatPhotoSend(ChatRecord chatRecord);
	}
}
