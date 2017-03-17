package com.vidmt.child.managers.inner;

import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.child.R;
import com.vidmt.child.exceptions.VidException;
import com.vidmt.child.managers.AccManager.IAccManager;
import com.vidmt.child.managers.HttpManager;
import com.vidmt.child.utils.Enums.CmdType;
import com.vidmt.child.utils.VidUtil;
import com.vidmt.child.vos.FenceVo;
import com.vidmt.xmpp.IXmppManager;
import com.vidmt.xmpp.enums.XmppEnums.LvlType;
import com.vidmt.xmpp.exts.CgCmdIQ;
import com.vidmt.xmpp.exts.CgUserIQ;
import com.vidmt.xmpp.exts.FenceIQ;
import com.vidmt.xmpp.exts.LvlIQ;
import com.vidmt.xmpp.exts.TraceIQ;
import com.vidmt.xmpp.inner.XmppManager;
import com.vidmt.xmpp.utils.XmppUtil;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Stanza;

import java.io.IOException;
import java.util.Map;

public class InnerAccManagerImpl implements IAccManager {
	private IXmppManager mXmppMgr;
	private HttpManager mHttpMgr;

	public InnerAccManagerImpl() {
		mXmppMgr = XmppManager.get();
		mHttpMgr = HttpManager.get();
	}

	@Override
	public void login(String account, String pwd) throws VidException {
		try {
			mXmppMgr.login(account, pwd);
		} catch (SmackException | IOException | XMPPException e) {
			throw new VidException(e);
		}
	}

	@Override
	public void register(String account, String pwd, Map<String, String> attributes) throws VidException {
		try {
			mXmppMgr.register(account, pwd, attributes);
		} catch (SmackException | IOException | XMPPException e) {
			throw new VidException(e);
		}
	}

	@Override
	public void logout() {
		try {
			mXmppMgr.logout();
		} catch (Exception e) {
		}
	}

	/**
	 * uid==null:获取自己的info
	 */
	@Override
	public CgUserIQ getUserInfo(String uid) {
		CgUserIQ cgUserIq = new CgUserIQ();
		if (uid != null) {
			cgUserIq.jid = XmppUtil.buildJid(uid);
		}
		cgUserIq.setType(Type.get);
		Stanza pkt = sendIQ(cgUserIq, true);
		return (CgUserIQ) pkt;
	}

	@Override
	public LvlIQ getLvlInfo(LvlType lvlType) {
		LvlIQ lvlIq = new LvlIQ();
		lvlIq.code = lvlType;
		lvlIq.setType(Type.get);
		Stanza pkt = sendIQ(lvlIq, true);
		return (LvlIQ) pkt;
	}

	@Override
	public FenceIQ getFence() {
		FenceIQ fenceIq = new FenceIQ();
		fenceIq.jid = XmppUtil.buildJid(VidUtil.getSideName());
		fenceIq.setType(Type.get);
		Stanza pkt = sendIQ(fenceIq, true);
		return (FenceIQ) pkt;
	}

	@Override
	public void uploadFence(FenceVo fence) {
		FenceIQ fenceIq = new FenceIQ();
		fenceIq.jid = fence.jid;
		fenceIq.latList.add(fence.lat);
		fenceIq.lonList.add(fence.lon);
		fenceIq.nameList.add(fence.name);
		fenceIq.radiusList.add(fence.radius);
		fenceIq.setType(Type.set);
		sendIQ(fenceIq, false);
	}

	@Override
	public void deleteFence(String jid, String name) {
		FenceIQ fenceIq = new FenceIQ();
		fenceIq.jid = jid;
		fenceIq.nameList.add(name);
		fenceIq.setType(Type.set);
		sendIQ(fenceIq, false);
	}

	@Override
	public TraceIQ getTraceData(String dateStr) {
		TraceIQ traceIq = new TraceIQ();
		traceIq.jid = XmppUtil.buildJid(VidUtil.getSideName());
		traceIq.dateStr = dateStr;
		traceIq.setType(Type.get);
		Stanza pkt = sendIQ(traceIq, true);
		return (TraceIQ) pkt;
	}

	@Override
	public void sendCommand(final CmdType cmd) {
		CgCmdIQ cgCmdIq = new CgCmdIQ();
		if (cmd == CmdType.REMOTE_AUDIO) {
			cgCmdIq.remoteAudio = true;
		} else if (cmd == CmdType.HIDE_ICON) {
			cgCmdIq.hideIcon = true;
		} else if (cmd == CmdType.SHOW_ICON) {
			cgCmdIq.showIcon = true;
		} else if (cmd == CmdType.ALARM) {
			cgCmdIq.alarm = true;
		}
		cgCmdIq.setTo(XmppUtil.buildJid(VidUtil.getSideName()));
		cgCmdIq.setType(Type.set);
		sendIQ(cgCmdIq, false);
	}

	private Stanza sendIQ(IQ iq, boolean synchronous) {
		Stanza stanza = null;
		try {
			if (synchronous) {
				stanza = mXmppMgr.syncSendIQ(iq);
				return stanza;
			} else {
				mXmppMgr.sendStanza(iq);
			}
		} catch (Exception e) {
			VLog.e("test", e);
			MainThreadHandler.makeToast(R.string.error_occur_please_try_again);
		}
		return null;
	}

	@Override
	public boolean isSideOnline() {
		return mXmppMgr.isUserOnline(VidUtil.getSideName());
	}

}
