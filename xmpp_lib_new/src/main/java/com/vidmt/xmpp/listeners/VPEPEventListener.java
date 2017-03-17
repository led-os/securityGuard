package com.vidmt.xmpp.listeners;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smackx.pep.PEPListener;
import org.jivesoftware.smackx.pep.packet.PEPEvent;
import org.jxmpp.util.XmppStringUtils;

import com.vidmt.xmpp.listeners.XmppListenerHolder.IXmppListenerExecutor;
import com.vidmt.xmpp.peps.VPEPEvent;
import com.vidmt.xmpp.peps.VPEPItem;

public class VPEPEventListener implements PEPListener {
	private static VPEPEventListener sInstance;

	public static VPEPEventListener get() {
		if (sInstance == null) {
			sInstance = new VPEPEventListener();
		}
		return sInstance;
	}
	
	private VPEPEventListener() {
	}

	@Override
	public void eventReceived(final String from, final PEPEvent event) {
		final String fromUid = XmppStringUtils.parseLocalpart(from);
		VPEPEvent pepEvent = (VPEPEvent) event;
		VPEPItem pepItem = (VPEPItem) pepEvent.getItem();
		final ExtensionElement extElement = pepItem.getPayLoad();
		XmppListenerHolder.callListeners(OnPEPEventListener.class, new IXmppListenerExecutor<OnPEPEventListener>() {
			@Override
			public void execute(OnPEPEventListener listener) {
				listener.eventReceived(fromUid, extElement);
			}
		});
	}
	
}
