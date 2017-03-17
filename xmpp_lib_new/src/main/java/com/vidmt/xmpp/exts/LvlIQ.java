package com.vidmt.xmpp.exts;

import org.jivesoftware.smack.packet.IQ;

import com.vidmt.xmpp.enums.XmppEnums.LvlType;

public class LvlIQ extends IQ {
	public LvlType code;
	public float price;
	public int expire, fenceNum, traceNum;
	public boolean remoteAudio, alarm, nav, noAd, hideIcon, sport;
	
	public LvlIQ() {
		super(IQExtConst.ELEMENT, IQExtConst.NAMESPACE);
	}
	
	@Override
	protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
		xml.rightAngleBracket();
		xml.openElement("lvl");
		if (code != null) {
			xml.openElement("code").append(code.name()).closeElement("code");
		}
		xml.closeElement("lvl");
		return xml;
	}

}
