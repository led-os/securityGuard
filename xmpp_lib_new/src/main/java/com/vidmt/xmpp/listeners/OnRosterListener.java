package com.vidmt.xmpp.listeners;

import org.jivesoftware.smack.packet.Presence;

public interface OnRosterListener extends IBaseXmppListener {
	public void entriesAdded(String uid);

	public void entriesUpdated(String uid);

	public void entriesDeleted(String uid);

	public void presenceChanged(Presence presence);

	public static abstract class AbsOnRosterListener implements OnRosterListener {
		@Override
		public void entriesAdded(String uid) {
		}

		@Override
		public void entriesUpdated(String uid) {
		}

		@Override
		public void entriesDeleted(String uid) {
		}

		@Override
		public void presenceChanged(Presence presence) {
		}
	}
}
