package com.vidmt.xmpp.peps;

import java.io.IOException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smackx.pep.provider.PEPProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class VPEPProvider extends PEPProvider {
	@Override
	public ExtensionElement parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException,
			SmackException {
		ExtensionElement payload = super.parse(parser, initialDepth);
		return new VPEPEvent(new VPEPItem(payload));
	}
}
