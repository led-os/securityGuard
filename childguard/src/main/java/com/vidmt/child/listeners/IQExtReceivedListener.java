package com.vidmt.child.listeners;

import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.DefaultThreadHandler;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.child.Const;
import com.vidmt.child.R;
import com.vidmt.child.activities.AlarmActivity;
import com.vidmt.child.managers.AccManager;
import com.vidmt.child.utils.UserUtil;
import com.vidmt.child.utils.VidUtil;
import com.vidmt.xmpp.enums.XmppEnums.ChatType;
import com.vidmt.xmpp.exts.CgCmdIQ;
import com.vidmt.xmpp.exts.CgUserIQ;
import com.vidmt.xmpp.exts.LvlIQ;
import com.vidmt.xmpp.inner.XmppManager;
import com.vidmt.xmpp.listeners.OnIQExtReceivedListener;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.IQ;

import java.io.File;

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
			CgCmdIQ cgCmdExt = (CgCmdIQ) iqExt;
			if (cgCmdExt.alarm) {
				VidUtil.startNewTaskActivity(AlarmActivity.class);
			} else if (cgCmdExt.remoteAudio) {// 远程录音
				VidUtil.startRecord();
				DefaultThreadHandler.post(new Runnable() {
					@Override
					public void run() {
						File recordFile = VidUtil.getRecordFile();
						if (recordFile != null) {
							try {
								Chat chat = XmppManager.get().createChat(VidUtil.getSideName());
								XmppManager.get().sendMessage(chat, ChatType.AUDIO, recordFile,
										Const.REMOTE_RECORD_TIME_TAG);
							} catch (NotConnectedException e) {
								VLog.e("test", e);
							}
						}
					}
				}, Const.REMOTE_RECORD_TIME_LEN * 1000);
			} else if (cgCmdExt.hideIcon) {// 隐藏图标
				MainThreadHandler.makeToast(R.string.icon_hide_after_10_seconds);
				VidUtil.removeShortCut();
			} else if (cgCmdExt.showIcon) {// 显示图标
				MainThreadHandler.makeToast(R.string.icon_show_after_10_seconds);
				VidUtil.addShortCut();
			}
		} else if (iqExt instanceof CgUserIQ) {
			CgUserIQ userIq = (CgUserIQ) iqExt;
			if (userIq.jid == null && userIq.nick == null && userIq.avatarUri == null) {// 用户缴费
				LvlIQ lvlIq = UserUtil.getParentInfo().code == userIq.code ? null : AccManager.get().getLvlInfo(
						userIq.code);
				UserUtil.upgradeLvl(userIq, lvlIq);
				MainThreadHandler.makeToast(R.string.vip_charge_success);
			} else if (userIq.jid != null && userIq.avatarUri != null) {// 修改头像
				String jidPrefix = userIq.jid.substring(0, 1);
				if (jidPrefix.equals(Const.BABY_ACCOUNT_PREFIX)) {
					AvatarChangedListener.get().triggerOnAvatarChanged(false, null);
					UserUtil.getBabyInfo().avatarUri = userIq.avatarUri;
				} else {
					AvatarChangedListener.get().triggerOnAvatarChanged(true, null);
					UserUtil.getParentInfo().avatarUri = userIq.avatarUri;
				}
			}
		}
	}

}
