package com.vidmt.xmpp.exts;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.packet.IQ;

public class SportIQ extends IQ {
	public String jid;
	public List<String> dateStrList = new ArrayList<String>();
	public List<Float> caloryList = new ArrayList<Float>(), distanceList = new ArrayList<Float>();
	public List<Integer> stepList = new ArrayList<Integer>();

	public SportIQ() {
		super(IQExtConst.ELEMENT, IQExtConst.NAMESPACE);
	}

	@Override
	protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
		xml.rightAngleBracket();
		xml.halfOpenElement("sport");
		if (jid != null) {
			xml.attribute("jid", jid).closeEmptyElement();
		}
		return xml;
	}

}
