package com.vidmt.xmpp.exts;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.packet.IQ;

public class FenceIQ extends IQ {
	public String jid;
	public List<String> nameList = new ArrayList<String>();
	public List<Double> latList = new ArrayList<Double>(), lonList = new ArrayList<Double>();
	public List<Integer> radiusList = new ArrayList<Integer>();

	public FenceIQ() {
		super(IQExtConst.ELEMENT, IQExtConst.NAMESPACE);
	}

	@Override
	protected IQChildElementXmlStringBuilder getIQChildElementBuilder(IQChildElementXmlStringBuilder xml) {
		xml.rightAngleBracket();
		xml.halfOpenElement("fence");
		if (jid != null) {
			xml.attribute("jid", jid);
		}
		xml.rightAngleBracket();
		if (nameList.size() > 0) {
			if (latList.size() == 0) {
				xml.openElement("del");
			}
			xml.halfOpenElement("item").attribute("name", nameList.get(0));
			if (latList.size() > 0) {
				xml.attribute("lat", latList.get(0).toString()).attribute("lon", lonList.get(0).toString())
						.attribute("radius", radiusList.get(0).toString());
			}
			xml.closeEmptyElement();
			if (latList.size() == 0) {
				xml.closeElement("del");
			}
		}
		xml.closeElement("fence");
		return xml;
	}

}
