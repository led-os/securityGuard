package com.vidmt.xmpp.prvds;

import java.io.IOException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.xmpp.exts.MultimediaExt;

public class MultimediaExtProvider extends ExtensionElementProvider<MultimediaExt> {

	@Override
	public MultimediaExt parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException,
			SmackException {
		int event = parser.getEventType();
		MultimediaExt multimedia = new MultimediaExt();
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_DOCUMENT:
				break;
			case XmlPullParser.START_TAG:
				if ("data".equals(parser.getName())) {
					multimedia.type = parser.getAttributeValue(null, "type");
					String durStr = parser.getAttributeValue(null, "during");
					if (durStr != null) {
						multimedia.during = Integer.valueOf(durStr);
					}
					multimedia.data = parser.nextText().trim();
				}
				break;
			case XmlPullParser.END_TAG:
				if (multimedia.getElementName().equals(parser.getName())) {
					return multimedia;
				}
				break;
			default:
				break;
			}
			event = parser.next();
		}
		VLog.e("MultimediaExtProvider", "parse MultimediaExt XML error!!!!!!");
		return multimedia;
	}

}
