package com.vidmt.xmpp;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

public class XmppConfig extends ConnectionConfiguration {
	private static final int CONNECTION_TIMEOUT = 60 * 1000;// default:30s
	public static final int PACKET_REPLY_TIMEOUT = 60 * 1000;// default:5s
	private static XmppConfig sInstance;
	private Builder<?, ?> mBuilder;

	public static XmppConfig get() {
		return sInstance;
	}

	protected XmppConfig(Builder<?, ?> builder) {
		super(builder);
	}

	public static void init(boolean debug, String host, int port, String serviceName, String resourceName) {
		Builder<?, ?> builder = XMPPTCPConnectionConfiguration.builder()
				.setServiceName(serviceName)
				.setHost(host)
				.setPort(port)
				.setResource(resourceName)
				.setConnectTimeout(CONNECTION_TIMEOUT)
				.setDebuggerEnabled(debug)
				.setCompressionEnabled(!debug);
//		if (debug) {
			builder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
//		} else {
//			builder.setSecurityMode(ConnectionConfiguration.SecurityMode.required);
//		}
		sInstance = new XmppConfig(builder);
		sInstance.mBuilder = builder;
	}

	public Builder<?, ?> getBuilder() {
		return sInstance.mBuilder;
	}

}
