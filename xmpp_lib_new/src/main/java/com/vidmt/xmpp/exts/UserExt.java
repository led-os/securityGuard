package com.vidmt.xmpp.exts;

import java.util.HashMap;

import org.jivesoftware.smack.packet.ExtensionElement;

public class UserExt extends HashMap<String, String> implements ExtensionElement {
	private static final long serialVersionUID = 1L;
	public static final String NAMESPACE = "http://www.vidmt.com/user";
	public static final String ELEMENT = "user";

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
		StringBuilder sb = new StringBuilder();
		sb.append("<user xmlns='" + NAMESPACE + "'>");
		for (Entry<String, String> entry : this.entrySet()) {
			if (entry.getValue() != null) {
				sb.append("<" + entry.getKey() + ">" + entry.getValue() + "</" + entry.getKey() + ">");
			}
		}
		sb.append("</user>");
		return sb.toString();
	}

}
