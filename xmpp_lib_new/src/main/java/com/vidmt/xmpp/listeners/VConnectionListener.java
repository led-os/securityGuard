package com.vidmt.xmpp.listeners;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;

import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.xmpp.listeners.XmppListenerHolder.IXmppListenerExecutor;

public class VConnectionListener implements ConnectionListener {
	private static VConnectionListener sInstance;

	public static VConnectionListener get() {
		if (sInstance == null) {
			sInstance = new VConnectionListener();
		}
		return sInstance;
	}

	private VConnectionListener() {
	}

	@Override
	public void connected(final XMPPConnection connection) {
		VLog.i("test", "connected");
		XmppListenerHolder.callListeners(OnConnectionListener.class,
				new IXmppListenerExecutor<OnConnectionListener>() {
					@Override
					public void execute(OnConnectionListener listener) {
						listener.connected(connection);
					}
				});
	}

	@Override
	public void authenticated(final XMPPConnection connection, final boolean resumed) {
		VLog.i("test", "authenticated");
		XmppListenerHolder.callListeners(OnConnectionListener.class,
				new IXmppListenerExecutor<OnConnectionListener>() {
					@Override
					public void execute(OnConnectionListener listener) {
						listener.authenticated(connection, resumed);
					}
				});
	}

	@Override
	public void connectionClosed() {
		VLog.i("test", "connectionClosed");
		XmppListenerHolder.callListeners(OnConnectionListener.class,
				new IXmppListenerExecutor<OnConnectionListener>() {
					@Override
					public void execute(OnConnectionListener listener) {
						listener.connectionClosed();
					}
				});
	}

	@Override
	public void connectionClosedOnError(final Exception e) {
		VLog.i("test", "connectionClosedOnError");
		XmppListenerHolder.callListeners(OnConnectionListener.class,
				new IXmppListenerExecutor<OnConnectionListener>() {
					@Override
					public void execute(OnConnectionListener listener) {
						listener.connectionClosedOnError(e);
					}
				});
	}

	@Override
	public void reconnectionSuccessful() {
		VLog.i("test", "reconnectionSuccessful");
		XmppListenerHolder.callListeners(OnConnectionListener.class,
				new IXmppListenerExecutor<OnConnectionListener>() {
					@Override
					public void execute(OnConnectionListener listener) {
						listener.reconnectionSuccessful();
					}
				});
	}

	@Override
	public void reconnectingIn(final int seconds) {
		VLog.i("test", "reconnectingIn");
		XmppListenerHolder.callListeners(OnConnectionListener.class,
				new IXmppListenerExecutor<OnConnectionListener>() {
					@Override
					public void execute(OnConnectionListener listener) {
						listener.reconnectingIn(seconds);
					}
				});
	}

	@Override
	public void reconnectionFailed(final Exception e) {
		VLog.i("test", "reconnectionFailed");
		XmppListenerHolder.callListeners(OnConnectionListener.class,
				new IXmppListenerExecutor<OnConnectionListener>() {
					@Override
					public void execute(OnConnectionListener listener) {
						listener.reconnectionFailed(e);
					}
				});
	}

}
