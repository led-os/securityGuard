package com.vidmt.xmpp.listeners;

import java.util.Collection;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterListener;
import org.jxmpp.util.XmppStringUtils;

import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.xmpp.enums.XmppEnums.Relationship;
import com.vidmt.xmpp.inner.XmppManager;
import com.vidmt.xmpp.listeners.XmppListenerHolder.IXmppListenerExecutor;

public class VRosterListener implements RosterListener {
	private static VRosterListener sInstance;

	public static VRosterListener get() {
		if (sInstance == null) {
			sInstance = new VRosterListener();
		}
		return sInstance;
	}

	private VRosterListener() {
	}

	/**
	 * 注：成为both关系好友后不会被调用
	 */
	@Override
	public void entriesAdded(final Collection<String> addresses) {// 即便未成为both好友也会执行
		final String uid = XmppStringUtils.parseLocalpart(addresses.iterator().next());
		VLog.i("lk", "entriesAdded:" + uid + ">>>" + XmppManager.get().getRelationship(uid));
		XmppListenerHolder.callListeners(OnRosterListener.class, new IXmppListenerExecutor<OnRosterListener>() {
			@Override
			public void execute(OnRosterListener listener) {
				listener.entriesAdded(uid);
			}
		});
	}

	@Override
	public void entriesUpdated(final Collection<String> addresses) {// add、remove都会执行
		final String uid = XmppStringUtils.parseLocalpart(addresses.iterator().next());
		VLog.i("lk", "entriesUpdated:" + uid + ">>>" + XmppManager.get().getRelationship(uid));
		XmppListenerHolder.callListeners(OnRosterListener.class, new IXmppListenerExecutor<OnRosterListener>() {
			@Override
			public void execute(OnRosterListener listener) {
				listener.entriesUpdated(uid);
			}
		});
		if (XmppManager.get().getRelationship(uid) == Relationship.FRIEND) {
			XmppListenerHolder.callListeners(OnRosterListener.class, new IXmppListenerExecutor<OnRosterListener>() {
				@Override
				public void execute(OnRosterListener listener) {
					listener.entriesAdded(uid);
				}
			});
		}
	}

	@Override
	public void entriesDeleted(final Collection<String> addresses) {// 正确执行
		final String uid = XmppStringUtils.parseLocalpart(addresses.iterator().next());
		VLog.i("lk", "entriesDeleted:" + uid + ">>>" + XmppManager.get().getRelationship(uid));
		XmppListenerHolder.callListeners(OnRosterListener.class, new IXmppListenerExecutor<OnRosterListener>() {
			@Override
			public void execute(OnRosterListener listener) {
				listener.entriesDeleted(uid);
			}
		});
	}

	@Override
	public void presenceChanged(final Presence presence) {// 正确执行
		XmppListenerHolder.callListeners(OnRosterListener.class, new IXmppListenerExecutor<OnRosterListener>() {
			@Override
			public void execute(OnRosterListener listener) {
				listener.presenceChanged(presence);
			}
		});
	}

}
