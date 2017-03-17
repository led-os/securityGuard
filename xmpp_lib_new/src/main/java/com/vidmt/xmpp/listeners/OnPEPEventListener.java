package com.vidmt.xmpp.listeners;

import org.jivesoftware.smack.packet.ExtensionElement;

public interface OnPEPEventListener extends IBaseXmppListener {
	public void eventReceived(String fromUid, ExtensionElement extElement);
}
