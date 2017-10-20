package com.vidmt.telephone.listeners;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.util.XmppStringUtils;

import com.vidmt.telephone.R;
import com.vidmt.telephone.entities.ChatRecord;
import com.vidmt.telephone.utils.VXmppUtil;
import com.vidmt.telephone.utils.VidUtil;
import com.vidmt.xmpp.listeners.OnMsgReceivedListener;

public class ShowMsgNotifListener implements OnMsgReceivedListener {
	private static ShowMsgNotifListener sInstance;

	public static ShowMsgNotifListener get() {
		if (sInstance == null) {
			sInstance = new ShowMsgNotifListener();
		}
		return sInstance;
	}

	@Override
	public void onMsgReceived(Chat chat, Message msg) {
		if (msg.getExtension("x", "jabber:x:delay") == null) {// 不是离线消息
			VidUtil.playSound(R.raw.mus, false);
		}
		ChatRecord chatRecord = null;
		String uid = XmppStringUtils.parseLocalpart(chat.getParticipant());
		if (uid.equals(ChatStatusListener.get().getCurChatUser())) {// 是当前聊天的人
			chatRecord = VXmppUtil.saveChatRecord(chat.getParticipant(), msg, true);
			if (VidUtil.isTopActivity()) {// 不在后台
				return;
			}
		} else {
			chatRecord = VXmppUtil.saveChatRecord(chat.getParticipant(), msg, false);
		}
		String msgTxt = VidUtil.getNotifyTxt(chatRecord);
		VidUtil.notifyChatMsg(chatRecord.getFuid(), msgTxt);
		VidUtil.addToMsgList(uid);
	}

}
