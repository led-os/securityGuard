package com.vidmt.xmpp.exts;

import org.jivesoftware.smack.packet.IQ;

public class TraceIQ extends IQ {
	public String dateStr, traceJson;
	public String jid;
	
	public TraceIQ() {
		super(IQExtConst.ELEMENT, IQExtConst.NAMESPACE);
	}
	
	@Override
	protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
		xml.rightAngleBracket();
		xml.halfOpenElement("trace");
		if (jid != null && dateStr != null) {
			xml.attribute("jid", jid).attribute("dateStr", dateStr).closeEmptyElement();
		}
		return xml;
	}

}
