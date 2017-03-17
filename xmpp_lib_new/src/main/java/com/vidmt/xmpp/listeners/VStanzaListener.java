package com.vidmt.xmpp.listeners;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.packet.RosterPacket.ItemType;
import org.jxmpp.util.XmppStringUtils;

import android.util.Log;

import com.vidmt.xmpp.inner.XmppManager;
import com.vidmt.xmpp.listeners.XmppListenerHolder.IXmppListenerExecutor;

public class VStanzaListener implements StanzaListener {
	private static VStanzaListener sInstance;

	public static VStanzaListener get() {
		if (sInstance == null) {
			sInstance = new VStanzaListener();
		}
		return sInstance;
	}

	private VStanzaListener() {
	}

	private boolean isFriendDeleteMe;

	@Override
	public void processPacket(Stanza packet) throws NotConnectedException {
		if (packet instanceof IQ) {
			final IQ iqExt = (IQ) packet;
			XmppListenerHolder.callListeners(OnIQExtReceivedListener.class,
					new IXmppListenerExecutor<OnIQExtReceivedListener>() {
						@Override
						public void execute(OnIQExtReceivedListener listener) {
							listener.processIQExt(iqExt);
						}
					});
			return;
		}
		String from = packet.getFrom();
		if (from == null || !(packet instanceof Presence)) {
			return;
		}
		final String fromUid = XmppStringUtils.parseLocalpart(from);
		Presence presence = (Presence) packet;
		RosterEntry fentry = Roster.getInstanceFor(XmppManager.get().conn()).getEntry(presence.getFrom());
		String entryDetail = "fentry:" + (fentry == null ? "NULL" : ("entryType=" + fentry.getType() + ";entryStatus=" + fentry.getStatus()) + ";presenceType=" + presence.getType()) + "{" + presence.toXML() + "}";
		Log.i("packet", entryDetail);
		if (presence.getType() == Presence.Type.subscribe) {
			if (fentry == null) {
				// 对方请求添加我为好友
				Log.i("friend", "friendRequestMe>>>" + entryDetail);
				XmppListenerHolder.callListeners(OnRelChangedListener.class,
						new IXmppListenerExecutor<OnRelChangedListener>() {
							@Override
							public void execute(OnRelChangedListener listener) {
								listener.beRequested(fromUid);
							}
						});
			} else if (fentry.getType() == ItemType.to) {
				// 对方同意我的好友请求
				Log.i("friend", "friendAgreeMe>>>" + entryDetail);
				XmppListenerHolder.callListeners(OnRelChangedListener.class,
						new IXmppListenerExecutor<OnRelChangedListener>() {
							@Override
							public void execute(OnRelChangedListener listener) {
								listener.beAgreed(fromUid);
							}
						});
			}
		} else if (presence.getType() == Presence.Type.subscribed) {
		} else if (presence.getType() == Presence.Type.unsubscribe) {
			if (fentry.getType() == ItemType.to) {
				Log.i("friend", "friendWillDeleteMe>>>" + entryDetail);
				isFriendDeleteMe = true;
			}
		} else if (presence.getType() == Presence.Type.unsubscribed) {
			if (fentry != null && fentry.getType() == ItemType.none) {
				if (isFriendDeleteMe) {
					// 好友删除了我
					isFriendDeleteMe = false;
					Log.i("friend", "friendHaveDeleteMe>>>" + entryDetail);
					XmppListenerHolder.callListeners(OnRelChangedListener.class,
							new IXmppListenerExecutor<OnRelChangedListener>() {
								@Override
								public void execute(OnRelChangedListener listener) {
									listener.beDeleted(fromUid);
								}
							});
				} else {
					// 好友拒绝了我的好友请求
					Log.i("friend", "friendRejectMe>>>" + entryDetail);
					XmppListenerHolder.callListeners(OnRelChangedListener.class,
							new IXmppListenerExecutor<OnRelChangedListener>() {
								@Override
								public void execute(OnRelChangedListener listener) {
									listener.beRefused(fromUid);
								}
							});
				}
			}
			XmppManager.get().deleteFriend(fromUid);
		}
	}

}
