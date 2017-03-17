package com.vidmt.xmpp.listeners;

import org.jivesoftware.smack.packet.IQ;

public interface OnIQExtReceivedListener extends IBaseXmppListener {
	public void processIQExt(IQ iqExt);
}
