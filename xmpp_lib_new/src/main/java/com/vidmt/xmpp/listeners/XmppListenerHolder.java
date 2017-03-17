package com.vidmt.xmpp.listeners;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class XmppListenerHolder {
	private static Set<OnConnectionListener> sConnectionListeners = new CopyOnWriteArraySet<OnConnectionListener>();
	private static Set<OnMsgReceivedListener> sMsgReceivedListeners = new CopyOnWriteArraySet<OnMsgReceivedListener>();
	private static Set<OnPEPEventListener> sPEPListeners = new CopyOnWriteArraySet<OnPEPEventListener>();
	private static Set<OnRosterListener> sRosterListeners = new CopyOnWriteArraySet<OnRosterListener>();
	private static Set<OnRelChangedListener> sRelChangedListeners = new CopyOnWriteArraySet<OnRelChangedListener>();
	private static Set<OnIQExtReceivedListener> mIQExtReceivedListeners = new CopyOnWriteArraySet<OnIQExtReceivedListener>();

	@SuppressWarnings({ "unchecked" })
	public static <T extends IBaseXmppListener> void callListeners(Class<T> clz, IXmppListenerExecutor<T> executor) {
		if (OnConnectionListener.class.isAssignableFrom(clz)) {
			for (OnConnectionListener listener : sConnectionListeners) {
				executor.execute((T) listener);
			}
		} else if (OnMsgReceivedListener.class.isAssignableFrom(clz)) {
			for (OnMsgReceivedListener listener : sMsgReceivedListeners) {
				executor.execute((T) listener);
			}
		} else if (OnPEPEventListener.class.isAssignableFrom(clz)) {
			for (OnPEPEventListener listener : sPEPListeners) {
				executor.execute((T) listener);
			}
		} else if (OnRosterListener.class.isAssignableFrom(clz)) {
			for (OnRosterListener listener : sRosterListeners) {
				executor.execute((T) listener);
			}
		} else if (OnRelChangedListener.class.isAssignableFrom(clz)) {
			for (OnRelChangedListener listener : sRelChangedListeners) {
				executor.execute((T) listener);
			}
		} else if (OnIQExtReceivedListener.class.isAssignableFrom(clz)) {
			for (OnIQExtReceivedListener listener : mIQExtReceivedListeners) {
				executor.execute((T) listener);
			}
		}
	}

	public static void addListener(IBaseXmppListener listener) {
		if (listener instanceof OnConnectionListener) {
			sConnectionListeners.add((OnConnectionListener) listener);
		} else if (listener instanceof OnMsgReceivedListener) {
			sMsgReceivedListeners.add((OnMsgReceivedListener) listener);
		} else if (listener instanceof OnPEPEventListener) {
			sPEPListeners.add((OnPEPEventListener) listener);
		} else if (listener instanceof OnRosterListener) {
			sRosterListeners.add((OnRosterListener) listener);
		} else if (listener instanceof OnRelChangedListener) {
			sRelChangedListeners.add((OnRelChangedListener) listener);
		} else if (listener instanceof OnIQExtReceivedListener) {
			mIQExtReceivedListeners.add((OnIQExtReceivedListener) listener);
		}
	}


	public static boolean containsListener(IBaseXmppListener listener) {
		if (listener instanceof OnConnectionListener) {
			return sConnectionListeners.contains((OnConnectionListener) listener);
		} else if (listener instanceof OnMsgReceivedListener) {
			return sMsgReceivedListeners.contains((OnMsgReceivedListener) listener);
		} else if (listener instanceof OnPEPEventListener) {
			return sPEPListeners.contains((OnPEPEventListener) listener);
		} else if (listener instanceof OnRosterListener) {
			return sRosterListeners.contains((OnRosterListener) listener);
		} else if (listener instanceof OnRelChangedListener) {
			return sRelChangedListeners.contains((OnRelChangedListener) listener);
		} else if (listener instanceof OnIQExtReceivedListener) {
			return mIQExtReceivedListeners.contains((OnIQExtReceivedListener) listener);
		}
		return false;
	}

	public static void removeListener(IBaseXmppListener listener) {
		if (listener instanceof OnConnectionListener) {
			sConnectionListeners.remove(listener);
		} else if (listener instanceof OnMsgReceivedListener) {
			sMsgReceivedListeners.remove(listener);
		} else if (listener instanceof OnPEPEventListener) {
			sPEPListeners.remove(listener);
		} else if (listener instanceof OnRosterListener) {
			sRosterListeners.remove(listener);
		} else if (listener instanceof OnRelChangedListener) {
			sRelChangedListeners.remove(listener);
		} else if (listener instanceof OnIQExtReceivedListener) {
			mIQExtReceivedListeners.remove(listener);
		}
	}

	public static void clear() {
		sConnectionListeners.clear();
		sMsgReceivedListeners.clear();
		sPEPListeners.clear();
		sRosterListeners.clear();
		sRelChangedListeners.clear();
		mIQExtReceivedListeners.clear();
	}

	public static interface IXmppListenerExecutor<T extends IBaseXmppListener> {
		public void execute(T listener);
	}

}
