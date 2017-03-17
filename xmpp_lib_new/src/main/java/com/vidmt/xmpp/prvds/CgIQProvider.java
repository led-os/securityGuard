package com.vidmt.xmpp.prvds;

import java.io.IOException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.xmpp.enums.XmppEnums.LvlType;
import com.vidmt.xmpp.exts.CgCmdIQ;
import com.vidmt.xmpp.exts.CgUserIQ;
import com.vidmt.xmpp.exts.FenceIQ;
import com.vidmt.xmpp.exts.IQExtConst;
import com.vidmt.xmpp.exts.LvlIQ;
import com.vidmt.xmpp.exts.SportIQ;
import com.vidmt.xmpp.exts.TraceIQ;

public class CgIQProvider extends IQProvider<IQ> {
	@Override
	public IQ parse(XmlPullParser parser, int initialDepth) throws XmlPullParserException, IOException, SmackException {
		try {
			IQ iq = getParsedIQ(parser);
			if (iq == null) {
				return null;
			}
			if (iq instanceof CgCmdIQ) {
				iq.setType(Type.set);
			} else {
				iq.setType(Type.result);
			}
			VLog.i("tmpTest", "CgIQProvider:" + iq.getChildElementXML());
			return iq;
		} catch (Exception e) {
			VLog.e("test", e);
		}
		return null;
	}

	private IQ getParsedIQ(XmlPullParser parser) throws Exception {
		CgUserIQ userIq = null;
		FenceIQ fenceIq = null;
		LvlIQ lvlIq = null;
		SportIQ sportIq = null;
		TraceIQ traceIq = null;
		CgCmdIQ cmdIq = null;
		int event = parser.getEventType();
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_DOCUMENT:
				break;
			case XmlPullParser.START_TAG:
				if ("user".equals(parser.getName()) && fenceIq == null && lvlIq == null && sportIq == null
						&& traceIq == null) {
					userIq = new CgUserIQ();
					userIq.jid = parser.getAttributeValue(null, "jid");
				} else if ("fence".equals(parser.getName()) && userIq == null && lvlIq == null && sportIq == null
						&& traceIq == null) {
					fenceIq = new FenceIQ();
					fenceIq.jid = parser.getAttributeValue(null, "jid");
				} else if ("lvl".equals(parser.getName()) && fenceIq == null && userIq == null && sportIq == null
						&& traceIq == null) {
					lvlIq = new LvlIQ();
				} else if ("sport".equals(parser.getName()) && fenceIq == null && lvlIq == null && userIq == null
						&& traceIq == null) {
					sportIq = new SportIQ();
				} else if ("trace".equals(parser.getName()) && fenceIq == null && lvlIq == null && sportIq == null
						&& userIq == null) {
					traceIq = new TraceIQ();
					traceIq.jid = parser.getAttributeValue(null, "jid");
				} else if ("cmd".equals(parser.getName())) {
					cmdIq = new CgCmdIQ();
				}
				if (userIq != null) {
					packUserIq(parser, userIq);
				} else if (fenceIq != null) {
					packFenceIq(parser, fenceIq);
				} else if (lvlIq != null) {
					packLvlIq(parser, lvlIq);
				} else if (sportIq != null) {
					packSportIq(parser, sportIq);
				} else if (traceIq != null) {
					packTraceIq(parser, traceIq);
				} else if (cmdIq != null) {
					packCmdIq(parser, cmdIq);
				}
				break;
			case XmlPullParser.END_TAG:
				if (IQExtConst.ELEMENT.equals(parser.getName())) {
					if (userIq != null) {
						return userIq;
					} else if (fenceIq != null) {
						return fenceIq;
					} else if (lvlIq != null) {
						return lvlIq;
					} else if (sportIq != null) {
						return sportIq;
					} else if (traceIq != null) {
						return traceIq;
					} else if (cmdIq != null) {
						return cmdIq;
					}
				}
				break;
			}
			event = parser.next();
		}// end while
		return null;
	}

	private void packUserIq(XmlPullParser parser, CgUserIQ userIq) throws Exception {
		if ("code".equals(parser.getName())) {
			userIq.code = LvlType.valueOf(parser.nextText().trim());
		} else if ("ttl".equals(parser.getName())) {
			userIq.ttl = Long.parseLong(parser.nextText().trim());
		} else if ("nick".equals(parser.getName())) {
			userIq.nick = parser.nextText().trim();
		} else if ("avatarUri".equals(parser.getName())) {
			userIq.avatarUri = parser.nextText().trim();
		}
	}

	private void packFenceIq(XmlPullParser parser, FenceIQ fenceIq) throws Exception {
		if ("fence".equals(parser.getName())) {
			fenceIq.jid = parser.getAttributeValue(null, "jid");
		} else if ("item".equals(parser.getName())) {
			fenceIq.nameList.add(parser.getAttributeValue(null, "name"));
			fenceIq.latList.add(Double.valueOf(parser.getAttributeValue(null, "lat")));
			fenceIq.lonList.add(Double.valueOf(parser.getAttributeValue(null, "lon")));
			fenceIq.radiusList.add(Integer.valueOf(parser.getAttributeValue(null, "radius")));
		}
	}

	private void packLvlIq(XmlPullParser parser, LvlIQ lvlIq) throws Exception {
		if ("code".equals(parser.getName())) {
			lvlIq.code = LvlType.valueOf(parser.nextText().trim());
		} else if ("price".equals(parser.getName())) {
			lvlIq.price = Float.parseFloat(parser.nextText().trim());
		} else if ("expire".equals(parser.getName())) {
			lvlIq.expire = Integer.parseInt(parser.nextText().trim());
		} else if ("remoteAudio".equals(parser.getName())) {
			lvlIq.remoteAudio = "T".equals(parser.nextText().trim());
		} else if ("alarm".equals(parser.getName())) {
			lvlIq.alarm = "T".equals(parser.nextText().trim());
		} else if ("nav".equals(parser.getName())) {
			lvlIq.nav = "T".equals(parser.nextText().trim());
		} else if ("fenceNum".equals(parser.getName())) {
			lvlIq.fenceNum = Integer.parseInt(parser.nextText().trim());
		} else if ("traceNum".equals(parser.getName())) {
			lvlIq.traceNum = Integer.parseInt(parser.nextText().trim());
		} else if ("noAd".equals(parser.getName())) {
			lvlIq.noAd = "T".equals(parser.nextText().trim());
		} else if ("hideIcon".equals(parser.getName())) {
			lvlIq.hideIcon = "T".equals(parser.nextText().trim());
		} else if ("sport".equals(parser.getName())) {
			lvlIq.sport = "T".equals(parser.nextText().trim());
		}
	}

	private void packSportIq(XmlPullParser parser, SportIQ sportIq) throws Exception {
		if ("sport".equals(parser.getName())) {
			sportIq.jid = parser.getAttributeValue(null, "jid");
		} else if ("item".equals(parser.getName())) {
			sportIq.dateStrList.add(parser.getAttributeValue(null, "dateStr"));
			sportIq.stepList.add(Integer.parseInt(parser.getAttributeValue(null, "step")));
			sportIq.distanceList.add(Float.parseFloat(parser.getAttributeValue(null, "distance")));
		}
	}

	private void packTraceIq(XmlPullParser parser, TraceIQ traceIq) throws Exception {
		if ("trace".equals(parser.getName())) {
			traceIq.jid = parser.getAttributeValue(null, "jid");
		} else if ("item".equals(parser.getName())) {
			traceIq.dateStr = parser.getAttributeValue(null, "dateStr");
			traceIq.traceJson = parser.getAttributeValue(null, "traceJson");
		}
	}

	private void packCmdIq(XmlPullParser parser, CgCmdIQ cmdIq) throws Exception {
		if ("alarm".equals(parser.getName())) {
			cmdIq.alarm = true;
			cmdIq.from = parser.getAttributeValue(null, "from");
		} else if ("remoteAudio".equals(parser.getName())) {
			cmdIq.remoteAudio = true;
			cmdIq.from = parser.getAttributeValue(null, "from");
		} else if ("hideIcon".equals(parser.getName())) {
			cmdIq.hideIcon = true;
			cmdIq.from = parser.getAttributeValue(null, "from");
		} else if ("showIcon".equals(parser.getName())) {
			cmdIq.showIcon = true;
			cmdIq.from = parser.getAttributeValue(null, "from");
		}
	}

}