package com.vidmt.xmpp.inner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.AlreadyLoggedInException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.android.AndroidSmackInitializer;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.filter.IQTypeFilter;
import org.jivesoftware.smack.filter.OrFilter;
import org.jivesoftware.smack.filter.PresenceTypeFilter;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.packet.RosterPacket.ItemStatus;
import org.jivesoftware.smack.roster.packet.RosterPacket.ItemType;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.debugger.android.AndroidDebugger;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.pep.PEPManager;
import org.jivesoftware.smackx.pep.provider.PEPProvider;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;
import org.jxmpp.util.XmppStringUtils;

import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.java.Base64Coder;
import com.vidmt.acmn.utils.java.FileUtil;
import com.vidmt.xmpp.IXmppManager;
import com.vidmt.xmpp.XmppConfig;
import com.vidmt.xmpp.enums.XmppEnums.ChatType;
import com.vidmt.xmpp.enums.XmppEnums.Relationship;
import com.vidmt.xmpp.exts.IQExtConst;
import com.vidmt.xmpp.exts.MultimediaExt;
import com.vidmt.xmpp.exts.UserExt;
import com.vidmt.xmpp.listeners.IBaseXmppListener;
import com.vidmt.xmpp.listeners.VChatCreatedListener;
import com.vidmt.xmpp.listeners.VConnectionListener;
import com.vidmt.xmpp.listeners.VPEPEventListener;
import com.vidmt.xmpp.listeners.VPingFailedListener;
import com.vidmt.xmpp.listeners.VRosterListener;
import com.vidmt.xmpp.listeners.VStanzaListener;
import com.vidmt.xmpp.listeners.XmppListenerHolder;
import com.vidmt.xmpp.peps.VPEPEvent;
import com.vidmt.xmpp.peps.VPEPItem;
import com.vidmt.xmpp.peps.VPEPProvider;
import com.vidmt.xmpp.prvds.CgIQProvider;
import com.vidmt.xmpp.prvds.MultimediaExtProvider;
import com.vidmt.xmpp.prvds.UserExtProvider;
import com.vidmt.xmpp.utils.XmppUtil;

public class XmppManager implements IXmppManager {
	private static IXmppManager sInstance;
	private AbstractXMPPConnection mConn;
	private PEPManager mPepManager;
	private PingManager pingManager;

	//huawei test
	private String mUid;
	private String mPwd;

	static {
		SmackConfiguration.DEBUG = VLib.DEBUG;
		AndroidDebugger.printInterpreted = true;
		List<Exception> es = new AndroidSmackInitializer().initialize();
		if (es != null) {
			for (Exception e : es) {
				VLog.e("test", "android smack initial error:" + e.getMessage());
			}
		}
		try {
			Class.forName("org.jivesoftware.smack.ReconnectionManager");
		} catch (ClassNotFoundException e) {
			VLog.e("test", e);
		}
	}

	public static IXmppManager get() {
		if (sInstance == null) {
			sInstance = new XmppManager();
		}
		return sInstance;
	}

	private XmppManager() {
	}

	/*
	 * 注:每次重置密码都要init
	 */
	private void initConn() {
		XMPPTCPConnectionConfiguration xmppTcpConf = (XMPPTCPConnectionConfiguration) XmppConfig.get().getBuilder()
				.build();
		mConn = new XMPPTCPConnection(xmppTcpConf);
		mConn.setPacketReplyTimeout(XmppConfig.PACKET_REPLY_TIMEOUT);
	}

	@Override
	public void init(boolean debug, String host, int port, String serviceName, String resourceName)
			throws SmackException, IOException, XMPPException {
		XmppConfig.init(debug, host, port, serviceName, resourceName);
		initConn();
	}

	@Override
	public String getmUid(){
		return mUid;
	}

	@Override
	public String getmPwd(){
		return mPwd;
	}

	@Override
	public void login(String uid, String pwd) throws SmackException, IOException, XMPPException {
		//huawei test
		mUid = uid;
		mPwd = pwd;

		if (mConn == null) {
			initConn();
		}
		if (!mConn.isConnected()) {
			mConn.connect();
			mConn.addConnectionListener(VConnectionListener.get());
		}
		try {
			mConn.login(uid, pwd);
			ChatManager chatMgr = ChatManager.getInstanceFor(mConn);
			chatMgr.addChatListener(VChatCreatedListener.get());

			ProviderManager.addExtensionProvider(MultimediaExt.ELEMENT, MultimediaExt.NAMESPACE,
					new MultimediaExtProvider());
			ProviderManager.addIQProvider(IQExtConst.ELEMENT, IQExtConst.NAMESPACE, new CgIQProvider());

			Roster.getInstanceFor(mConn).addRosterListener(VRosterListener.get());

			OrFilter orFilter = new OrFilter(PresenceTypeFilter.SUBSCRIBE, PresenceTypeFilter.SUBSCRIBED,
					PresenceTypeFilter.UNSUBSCRIBE, PresenceTypeFilter.UNSUBSCRIBED, IQTypeFilter.GET_OR_SET,
					IQTypeFilter.RESULT);
			mConn.addAsyncStanzaListener(VStanzaListener.get(), orFilter);

			// care : below is a must for PEP
			ServiceDiscoveryManager sdm = ServiceDiscoveryManager.getInstanceFor(mConn);
			sdm.addFeature(UserExt.NAMESPACE + "+notify");

			PEPProvider.registerPEPParserExtension(UserExt.NAMESPACE, new UserExtProvider());
			ProviderManager.addExtensionProvider(VPEPEvent.ELEMENT, VPEPEvent.NAMESPACE, new VPEPProvider());

			mPepManager = new PEPManager(mConn);
			mPepManager.addPEPListener(VPEPEventListener.get());
			ReconnectionManager reconnMgr = ReconnectionManager.getInstanceFor(mConn);
			reconnMgr.enableAutomaticReconnection();
			reconnMgr.setReconnectionPolicy(ReconnectionManager.ReconnectionPolicy.RANDOM_INCREASING_DELAY);
			//reconnMgr.setFixedDelay(0);

			//change for huawei store;
			pingManager = PingManager.getInstanceFor(mConn);
			pingManager.registerPingFailedListener(VPingFailedListener.get());
			//changed for reconnect time by huawei;

		} catch (AlreadyLoggedInException e) {
			return;
		} catch (SmackException e) {
			logout();
			throw e;
		} catch (IOException e) {
			logout();
			throw e;
		} catch (XMPPException e) {
			logout();
			throw e;
		}
	}

	public AbstractXMPPConnection getConnection(){
		return mConn;
	}

	@Override
	public void register(String uid, String pwd, Map<String, String> attributes) throws SmackException, IOException,
			XMPPException {
		if (mConn == null) {
			initConn();
		}
		if (!mConn.isConnected()) {
			mConn.connect();
		}
		AccountManager accMgr = AccountManager.getInstance(mConn);
		if (attributes == null) {
			attributes = new HashMap<String, String>();
		}
		accMgr.createAccount(uid, pwd, attributes);
	}

	@Override
	public boolean isAuthenticated() {
		return mConn != null && mConn.isAuthenticated() && mConn.isConnected();
	}

	@Override
	public AbstractXMPPConnection conn() {
		return mConn;
	}

	@Override
	public Chat createChat(String uid) {
		ChatManager cm = ChatManager.getInstanceFor(mConn);
		Chat chat = cm.createChat(XmppUtil.buildJid(uid));
		return chat;
	}

	@Override
	public void logout() {
		if (mConn != null) {
			//huawei add start;
			if(pingManager != null) {
				pingManager.unregisterPingFailedListener(VPingFailedListener.get());
			}
			//huawei add end;
			mConn.disconnect();
			mConn = null;
		}
	}

	@Override
	public void pepMessage(ExtensionElement payload) throws NotConnectedException {
		mPepManager.publish(new VPEPItem(payload));
	}

	@Override
	public void sendStanza(Stanza stanza) throws NotConnectedException {
		if (!isAuthenticated()) {
			return;
		}
		mConn.sendStanza(stanza);
	}

	@Override
	public Stanza syncSendIQ(IQ iq) throws NotConnectedException {
		if (!isAuthenticated()) {
			return null;
		}
		PacketCollector pktCollector = mConn.createPacketCollectorAndSend(iq);
		Stanza stanza = pktCollector.nextResult();
		return stanza;
	}

	@Override
	public boolean isUserOnline(String uid) {
		if (!isAuthenticated()) {
			return false;
		}
		Roster roster = Roster.getInstanceFor(mConn);
		Presence presence = roster.getPresence(XmppUtil.buildJid(uid));
		return presence != null && presence.isAvailable();
	}

	@Override
	public String getUserResource(String uid) {
		if (!isAuthenticated()) {
			return null;
		}
		Roster roster = Roster.getInstanceFor(mConn);
		Presence presence = roster.getPresence(XmppUtil.buildJid(uid));
		if (presence == null) {
			return null;
		}
		String fullJid = presence.getFrom();
		String resourcePart = XmppStringUtils.parseResource(fullJid);
		if (resourcePart == null || resourcePart.equals("")) {// when offline
			return null;
		}
		return resourcePart;
	}

	/**
	 * 根据itemType返回花名册，为null时返回所有
	 */
	@Override
	public List<String> getRosterUids(ItemType itemType) {
		List<String> uids = new ArrayList<String>();
		if (!isAuthenticated()) {
			return uids;
		}
		Roster roster = Roster.getInstanceFor(mConn);
		for (RosterEntry entry : roster.getEntries()) {
			if (itemType == null || itemType == entry.getType()) {
				uids.add(XmppStringUtils.parseLocalpart(entry.getUser()));
			}
		}
		return uids;
	}

	@Override
	public Relationship getRelationship(String uid) {
		if (!isAuthenticated()) {
			return Relationship.NONE;
		}
		Roster roster = Roster.getInstanceFor(mConn);
		RosterEntry entry = roster.getEntry(XmppUtil.buildJid(uid));
		if (entry == null) {
			return Relationship.NONE;
		}
		ItemType type = entry.getType();
		ItemStatus status = entry.getStatus();
		if (type == ItemType.none && status == null) {
			return Relationship.NONE;
		} else if (type == ItemType.none && status == ItemStatus.subscribe || type == ItemType.to && status == null) {
			return Relationship.WAIT_BE_AGREE;
		} else if (type == ItemType.from && status == null) {
			return Relationship.WAIT_TO_REPLY;
		} else if (type == ItemType.both || type == ItemType.from && status == ItemStatus.subscribe) {
			return Relationship.FRIEND;
		}
		return Relationship.NONE;
	}

	/**
	 * 添加操作成功,返回true. The server will asynchronously update the roster
	 * 注：同意对方的添加好友请求也用此方法
	 */
	@Override
	public boolean addFriend(String friendUid) {
		if (!isAuthenticated()) {
			return false;
		}
		Roster roster = Roster.getInstanceFor(mConn);
		try {
			roster.createEntry(XmppUtil.buildJid(friendUid), null, null);
			return true;
		} catch (NotLoggedInException e) {
			VLog.e("test", e);
		} catch (NoResponseException e) {
			VLog.e("test", e);
		} catch (XMPPErrorException e) {
			VLog.e("test", e);
		} catch (NotConnectedException e) {
			VLog.e("test", e);
		}
		return false;
	}

	/**
	 * 删除成功,返回true.This is a synchronous call 注：拒绝对方的添加好友请求也用此方法
	 */
	@Override
	public boolean deleteFriend(String friendUid) {
		if (!isAuthenticated()) {
			return false;
		}
		Roster roster = Roster.getInstanceFor(mConn);
		RosterEntry entry = roster.getEntry(XmppUtil.buildJid(friendUid));
		if (entry != null) {
			try {
				roster.removeEntry(entry);
				return true;
			} catch (NotLoggedInException e) {
				VLog.e("test", e);
			} catch (NoResponseException e) {
				VLog.e("test", e);
			} catch (XMPPErrorException e) {
				VLog.e("test", e);
			} catch (NotConnectedException e) {
				VLog.e("test", e);
			}
		}
		return false;
	}

	@Override
	public Message sendMessage(Chat chat, String msg) throws NotConnectedException {
		Message message = new Message();
		message.setBody(msg);
		chat.sendMessage(message);
		return message;
	}

	@Override
	public Message sendMessage(Chat chat, ChatType type, File file, Integer during) throws NotConnectedException {
		Message message = new Message();
		MultimediaExt multimedia = new MultimediaExt();
		multimedia.type = type.toString();
		byte[] bytes = FileUtil.readFile(file);
		String base64Data = Base64Coder.encode(bytes);
		multimedia.data = base64Data;
		multimedia.during = during;
		message.addExtension(multimedia);
		chat.sendMessage(message);
		return message;
	}

	@Override
	public void addXmppListener(IBaseXmppListener listener) {
		XmppListenerHolder.addListener(listener);
	}

	@Override
	public boolean containsXmppListener(IBaseXmppListener listener){
		return XmppListenerHolder.containsListener(listener);
	}
	@Override
	public void removeXmppListener(IBaseXmppListener listener) {
		XmppListenerHolder.removeListener(listener);
	}

}
