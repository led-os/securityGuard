package com.vidmt.child.managers;

import com.vidmt.child.exceptions.VidException;
import com.vidmt.child.managers.inner.InnerAccManagerImpl;
import com.vidmt.child.utils.Enums.CmdType;
import com.vidmt.child.vos.FenceVo;
import com.vidmt.xmpp.enums.XmppEnums.LvlType;
import com.vidmt.xmpp.exts.CgUserIQ;
import com.vidmt.xmpp.exts.FenceIQ;
import com.vidmt.xmpp.exts.LvlIQ;
import com.vidmt.xmpp.exts.TraceIQ;

import java.util.Map;

public class AccManager {
	private static IAccManager sInstance;

	public static IAccManager get() {
		if (sInstance == null) {
			sInstance = new InnerAccManagerImpl();
		}
		return sInstance;
	}

	public interface IAccManager {
		void login(String account, String pwd) throws VidException;

		void register(String account, String pwd, Map<String, String> attributes) throws VidException;

		void logout();

		boolean isSideOnline();

		CgUserIQ getUserInfo(String uid);

		void sendCommand(CmdType command);

		LvlIQ getLvlInfo(LvlType vip);

		void uploadFence(FenceVo fence);

		TraceIQ getTraceData(String dateStr);

		FenceIQ getFence();

		void deleteFence(String jid, String name);
	}
}
