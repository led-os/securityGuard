package com.vidmt.xmpp.exts;

import org.jivesoftware.smack.packet.IQ;

import com.vidmt.xmpp.enums.XmppEnums.LvlType;

public class CgUserIQ extends IQ {
	public String jid, nick, avatarUri;
	public LvlType code;
	public long ttl;

	public CgUserIQ() {
		super(IQExtConst.ELEMENT, IQExtConst.NAMESPACE);
	}

	@Override
	protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
		xml.rightAngleBracket();
		xml.halfOpenElement("user");
		if (jid != null) {
			xml.attribute("jid", jid);
		}
		xml.rightAngleBracket();
		if (code != null) {
			xml.emptyElement("code");
		}
		if (nick != null) {
			xml.openElement("nick").append(nick).closeElement("nick");
		}
		if (avatarUri != null) {
			xml.openElement("avatarUri").append(avatarUri).closeElement("avatarUri");
		}
		xml.closeElement("user");
		return xml;
	}

}
