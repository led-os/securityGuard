package com.vidmt.xmpp.exts;

import org.jivesoftware.smack.packet.IQ;

public class CgCmdIQ extends IQ {
	public boolean alarm, remoteAudio, hideIcon, showIcon;
	public String from;

	public CgCmdIQ() {
		super(IQExtConst.ELEMENT, IQExtConst.NAMESPACE);
	}

	@Override
	protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
		xml.rightAngleBracket();
		xml.openElement("cmd");
		if (alarm) {
			xml.emptyElement("alarm");
		} else if (remoteAudio) {
			xml.emptyElement("remoteAudio");
		} else if (hideIcon) {
			xml.emptyElement("hideIcon");
		} else if (showIcon) {
			xml.emptyElement("showIcon");
		}
		xml.closeElement("cmd");
		return xml;
	}

}
