package com.vidmt.xmpp.listeners;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;

import android.text.TextUtils;

import com.vidmt.xmpp.listeners.XmppListenerHolder.IXmppListenerExecutor;
import com.vidmt.xmpp.peps.VPEPEvent;

public class VChatCreatedListener implements ChatManagerListener {
	private static VChatCreatedListener sInstance;

	public static VChatCreatedListener get() {
		if (sInstance == null) {
			sInstance = new VChatCreatedListener();
		}
		return sInstance;
	}

	private VChatCreatedListener() {
	}

	@Override
	public void chatCreated(Chat chat, boolean createdLocally) {
		chat.addMessageListener(mMsgListener);
	}

	private ChatMessageListener mMsgListener = new ChatMessageListener() {
		@Override
		public void processMessage(final Chat chat, final Message message) {
			if (message.getType() == Message.Type.error || TextUtils.isEmpty(message.getBody())
					&& message.getExtensions().size() == 0) {// 过滤error和非正常(body==null&&ext==null)消息
				return;
			}
			if (message.getExtension(VPEPEvent.ELEMENT, VPEPEvent.NAMESPACE) != null) {// 过滤PEP消息
				return;
			}
			XmppListenerHolder.callListeners(OnMsgReceivedListener.class,
					new IXmppListenerExecutor<OnMsgReceivedListener>() {
						@Override
						public void execute(OnMsgReceivedListener listener) {
							listener.onMsgReceived(chat, message);
						}
					});
		}
	};

}
