package com.vidmt.xmpp.peps;

import org.jivesoftware.smackx.pep.packet.PEPEvent;
import org.jivesoftware.smackx.pep.packet.PEPItem;

public class VPEPEvent extends PEPEvent {
	public static final String ELEMENT = "event";
	public static final String NAMESPACE = "http://jabber.org/protocol/pubsub#event";
	private PEPItem item;

	public VPEPEvent(PEPItem item) {
		super(item);
		this.item = item;
	}

	public PEPItem getItem() {
		return item;
	}

	@Override
	public String getElementName() {
		return ELEMENT;
	}

	@Override
	public String getNamespace() {
		return NAMESPACE;
	}

	@Override
	public String toXML() {
		StringBuilder buf = new StringBuilder();
		buf.append("<").append(getElementName()).append(" xmlns=\"").append(getNamespace()).append("\">");
		buf.append("<items node='" + item.getNode() + "'>");
		buf.append(item.toXML());
		buf.append("</items>");
		buf.append("</").append(getElementName()).append(">");
		return buf.toString();
	}
}
