package com.vidmt.xmpp;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.packet.RosterPacket.ItemType;

import com.vidmt.xmpp.enums.XmppEnums.ChatType;
import com.vidmt.xmpp.enums.XmppEnums.Relationship;
import com.vidmt.xmpp.listeners.IBaseXmppListener;

public interface IXmppManager {
	public void init(boolean debug, String host, int port, String serviceName, String resourceName)
			throws SmackException, IOException, XMPPException;

	public void login(String uid, String pwd) throws SmackException, IOException, XMPPException;

	//huawei test
	public String getmUid();

	public String getmPwd();

	public void register(String uid, String pwd, Map<String, String> attributes) throws SmackException, IOException,
			XMPPException;

	public void sendStanza(Stanza stanza) throws NotConnectedException;
	
	public Stanza syncSendIQ(IQ iq) throws NotConnectedException;

	public AbstractXMPPConnection getConnection();

	public void logout();

	public void pepMessage(ExtensionElement payload) throws NotConnectedException;

	public void addXmppListener(IBaseXmppListener listener);

	public boolean containsXmppListener(IBaseXmppListener listener);

	public void removeXmppListener(IBaseXmppListener listener);

	public AbstractXMPPConnection conn();

	public Chat createChat(String uid);

	public boolean isAuthenticated();

	public boolean isUserOnline(String uid);
	
	public String getUserResource(String uid);

	public Relationship getRelationship(String uid);

	public List<String> getRosterUids(ItemType itemType);

	public boolean addFriend(String friendUid);

	public boolean deleteFriend(String friendUid);

	public Message sendMessage(Chat chat, String msg) throws NotConnectedException;

	public Message sendMessage(Chat chat, ChatType type, File File, Integer during) throws NotConnectedException;

}
