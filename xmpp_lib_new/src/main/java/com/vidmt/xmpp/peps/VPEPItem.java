package com.vidmt.xmpp.peps;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.pep.packet.PEPItem;

public class VPEPItem extends PEPItem {
	private ExtensionElement payload;

	public VPEPItem(ExtensionElement payload) {
		super(StringUtils.randomString(7));
		this.payload = payload;
	}
	
	public ExtensionElement getPayLoad(){
		return payload;
	}

	@Override
	public String getNode() {
		return payload.getNamespace() ;
	}

	@Override
	public String getItemDetailsXML() {
		return payload.toXML().toString();
	}
	
}