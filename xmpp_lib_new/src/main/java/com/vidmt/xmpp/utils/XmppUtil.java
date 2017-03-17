package com.vidmt.xmpp.utils;

import org.jxmpp.util.XmppStringUtils;

import com.vidmt.xmpp.inner.XmppManager;

public class XmppUtil {
	public static String buildJid(String uid) {
		return XmppStringUtils.completeJidFrom(uid, XmppManager.get().conn().getServiceName());
	}
	
	public static String buildFullJid(String uid) {
		return XmppStringUtils.completeJidFrom(uid, XmppManager.get().conn().getServiceName(), XmppManager.get().getUserResource(uid));
	}
}
