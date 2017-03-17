package com.vidmt.xmpp.listeners;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;

public interface OnMsgReceivedListener extends IBaseXmppListener {
	public void onMsgReceived(Chat chat,Message msg);
}
