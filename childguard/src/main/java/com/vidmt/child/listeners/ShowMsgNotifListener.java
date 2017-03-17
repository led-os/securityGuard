package com.vidmt.child.listeners;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.child.App;
import com.vidmt.child.Const;
import com.vidmt.child.R;
import com.vidmt.child.activities.ChattingActivity;
import com.vidmt.child.entities.ChatRecord;
import com.vidmt.child.utils.VXmppUtil;
import com.vidmt.child.utils.VidUtil;
import com.vidmt.xmpp.enums.XmppEnums.ChatType;
import com.vidmt.xmpp.listeners.OnMsgReceivedListener;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.util.XmppStringUtils;

public class ShowMsgNotifListener implements OnMsgReceivedListener {
	private static ShowMsgNotifListener sInstance;
	private String name;

	public static ShowMsgNotifListener get() {
		if (sInstance == null) {
			sInstance = new ShowMsgNotifListener();
		}
		return sInstance;
	}

	public void setCurChatUser(String name) {
		this.name = name;
	}

	@Override
	public void onMsgReceived(Chat chat, Message msg) {
		if (msg.getType() == Message.Type.error || msg.getExtensions().size() == 0 && TextUtils.isEmpty(msg.getBody())) {
			return;
		}
		if (msg.getExtension("x", "jabber:x:delay") == null) {// 不是离线消息
			VidUtil.playSound(R.raw.mus, false);
		}
		ChatRecord chatRecord = null;
		String uid = XmppStringUtils.parseLocalpart(chat.getParticipant());
		if (uid.equals(this.name)) {// 是当前聊天的人
			chatRecord = VXmppUtil.saveChatRecord(chat.getParticipant(), msg, true);
			if (VidUtil.isTopActivity()) {// 不在后台
				return;
			}
		} else {
			chatRecord = VXmppUtil.saveChatRecord(chat.getParticipant(), msg, false);
		}
		ChatType type = ChatType.valueOf(chatRecord.getType());
		if (type == ChatType.TXT) {
			notifyMsg(chatRecord.getName(), chatRecord.getData());
		} else if (type == ChatType.IMAGE) {
			notifyMsg(chatRecord.getName(), "【图片】");
		} else if (type == ChatType.AUDIO) {
			int totalTime = chatRecord.getDuring();
			boolean isRemoteVoice = false;
			if (totalTime == Const.REMOTE_RECORD_TIME_TAG) {
				isRemoteVoice = true;
				totalTime = Const.REMOTE_RECORD_TIME_LEN;
			}
			notifyMsg(chatRecord.getName(), (isRemoteVoice ? "[远程]" : "") + "【声音" + totalTime + "秒】");
		} else if (type == ChatType.VIDEO) {
			notifyMsg(chatRecord.getName(), "【小视频】");
		}
	}

	public void notifyMsg(final String name, final String content) {
		String nickname = name;
		Bitmap avatar = SysUtil.getBitmap(R.drawable.def_child);
		Notification n = new Notification();
		n.icon = R.drawable.ic_launcher;
		n.tickerText = nickname + " : " + content;
		n.when = System.currentTimeMillis();

		Intent intent = new Intent(App.get(), ChattingActivity.class);
		PendingIntent pi = PendingIntent.getActivity(App.get(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		RemoteViews contentView = new RemoteViews(App.get().getPackageName(), R.layout.notification_msg);
		contentView.setImageViewBitmap(R.id.avatar, avatar);
		contentView.setTextViewText(R.id.nickname, nickname);
		contentView.setTextViewText(R.id.message, content);
		n.contentView = contentView;
		n.contentIntent = pi;
		SysUtil.showNotification(Const.NOTIF_ID_CHAT_RCV, name, n);
	}
}
