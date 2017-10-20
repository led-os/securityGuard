package com.vidmt.telephone.listeners;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatStatusListener {
	private static ChatStatusListener sInstance;
	private List<OnChatStatusListener> mListeners = new CopyOnWriteArrayList<OnChatStatusListener>();
	private String mCurChatUser;

	public static ChatStatusListener get() {
		if (sInstance == null) {
			sInstance = new ChatStatusListener();
		}
		return sInstance;
	}

	private ChatStatusListener() {
	}

	public interface OnChatStatusListener {
		public void onChat(String withWhom, boolean isStartNotEnd);
	}

	public void addOnChatStatusListener(OnChatStatusListener listener) {
		mListeners.add(listener);
	}

	public void removeOnChatStatusListener(OnChatStatusListener listener) {
		mListeners.remove(listener);
	}

	public void triggerOnChatStatusListener(String withWhom, boolean isStartNotEnd) {
		for (OnChatStatusListener listener : mListeners) {
			if (listener != null) {
				listener.onChat(withWhom, isStartNotEnd);
			}
		}
		if (isStartNotEnd) {
			mCurChatUser = withWhom;
		} else {
			mCurChatUser = null;
		}
	}

	public String getCurChatUser() {
		return mCurChatUser;
	}
}
