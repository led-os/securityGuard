package com.vidmt.xmpp.listeners;

import org.jivesoftware.smack.XMPPConnection;

public interface OnConnectionListener extends IBaseXmppListener {
	public void onConnected();
	
	public void connected(XMPPConnection connection);

	public void authenticated(XMPPConnection connection, boolean resumed);

	public void connectionClosed();

	public void connectionClosedOnError(Exception e);

	public void reconnectionSuccessful();

	public void reconnectingIn(int seconds);

	public void reconnectionFailed(Exception e);

	public static class AbsOnConnectionListener implements OnConnectionListener {
		@Override
		public void onConnected() {
			// TODO Auto-generated method stub
		}

		@Override
		public void connected(XMPPConnection connection) {
			// TODO Auto-generated method stub
		}

		@Override
		public void authenticated(XMPPConnection connection, boolean resumed) {
			// TODO Auto-generated method stub
		}

		@Override
		public void connectionClosed() {
			// TODO Auto-generated method stub
		}

		@Override
		public void connectionClosedOnError(Exception e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void reconnectionSuccessful() {
			// TODO Auto-generated method stub
		}

		@Override
		public void reconnectingIn(int seconds) {
			// TODO Auto-generated method stub
		}

		@Override
		public void reconnectionFailed(Exception e) {
			// TODO Auto-generated method stub
		}
	}
}
