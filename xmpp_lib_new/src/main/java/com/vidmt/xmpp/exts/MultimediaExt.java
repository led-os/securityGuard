package com.vidmt.xmpp.exts;

import org.jivesoftware.smack.packet.ExtensionElement;

public class MultimediaExt implements ExtensionElement {
	public static final String NAMESPACE = "http://www.vidmt.com/multimedia";
	public static final String ELEMENT = "multimedia";

	public String data;
	public String type;
	public Integer during;

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
		sb.append("<multimedia xmlns='"+NAMESPACE+"'>");
		if(data!=null&&data.length()>0){
			sb.append("<data");
			if(type!=null&&type.length()>0){
				sb.append(" type='"+type+"'");
			}
			if(during!=null){
				sb.append(" during='"+during+"'");
			}
			sb.append(">").append(data).append("</data>");
		}
		sb.append("</multimedia>");
		return sb.toString();
	}
}
