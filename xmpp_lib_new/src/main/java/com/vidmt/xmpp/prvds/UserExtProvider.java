package com.vidmt.xmpp.prvds;

import java.io.IOException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.provider.ExtensionElementProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.xmpp.exts.UserExt;

public class UserExtProvider extends ExtensionElementProvider<UserExt> {

	@Override
	public UserExt parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException,
			SmackException {
		int event = parser.getEventType();
		UserExt user = null;
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_DOCUMENT:
				break;
			case XmlPullParser.START_TAG:
				if (UserExt.ELEMENT.equals(parser.getName())) {
					user = new UserExt();
				} else if (user != null) {
					user.put(parser.getName(), parser.nextText());
				}
				break;
			case XmlPullParser.END_TAG:
				if (UserExt.ELEMENT.equals(parser.getName())) {
					return user;
				}
				break;
			default:
				break;
			}
			event = parser.next();
		}
		VLog.e("UserExtProvider", "parse UserExt XML error!!!!!!");
		return user;
	}

}
