package com.vidmt.child.utils;

import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.java.Base64Coder;
import com.vidmt.acmn.utils.java.FileUtil;
import com.vidmt.child.FileStorage;
import com.vidmt.child.entities.ChatRecord;
import com.vidmt.child.managers.DBManager;
import com.vidmt.xmpp.enums.XmppEnums.ChatType;
import com.vidmt.xmpp.exts.MultimediaExt;

import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.xhtmlim.XHTMLManager;
import org.jxmpp.util.XmppStringUtils;

import java.io.File;
import java.util.Date;
import java.util.List;

public class VXmppUtil {
	public static ChatRecord parseChatMessage(Message msg) {
		MultimediaExt multimedia = msg.getExtension(MultimediaExt.ELEMENT, MultimediaExt.NAMESPACE);
		ChatRecord record = new ChatRecord();
		String name = XmppStringUtils.parseLocalpart(msg.getFrom());
		record.setName(name);
		record.setSelf(false);
		record.setSayTime(new Date().getTime());
		if (multimedia != null) {// 多媒体
			if (multimedia.type == null) {
				record.setType(ChatType.TXT.name());
			} else {
				record.setType(multimedia.type);
			}
			ChatType recordType = ChatType.valueOf(record.getType());
			if (recordType == ChatType.AUDIO) {
				String fName = MD5.getMD5(multimedia.data) + ".amr";
				String filePath = FileStorage.buildChatAudioPath(fName);
				record.setData(filePath);
				record.setDuring(multimedia.during == null ? 0 : multimedia.during);
			} else if (recordType == ChatType.IMAGE) {
				String fName = MD5.getMD5(multimedia.data) + ".jpg";
				String filePath = FileStorage.buildChatImgPath(fName);
				record.setData(filePath);
			} else if(recordType == ChatType.VIDEO) {
				String fName = MD5.getMD5(multimedia.data) + ".mp4";
				String filePath = FileStorage.buildChatVideoPath(fName);
				record.setData(filePath);
			}
			return record;
		} else {
			List<CharSequence> list = XHTMLManager.getBodies(msg);
			if (list != null && list.size() > 0) {// HTML
				record.setData(list.get(0).toString());
				record.setType(ChatType.HTML.name());
				return record;
			} else if (msg.getBody() != null) {// TXT
				record.setData(msg.getBody());
				record.setType(ChatType.TXT.name());
				return record;
			}
		}
		return null;
	}

	/**
	 *    if type=txt then data=plaintext; or data = base64data;
	 */
	public static ChatRecord saveChatRecord(String paticipantJid, Message msg, boolean isRead) {
		MultimediaExt multimedia = msg.getExtension(MultimediaExt.ELEMENT, MultimediaExt.NAMESPACE);
		ChatRecord record = new ChatRecord();
		String name = XmppStringUtils.parseLocalpart(paticipantJid);
		record.setName(name);
		record.setSelf(paticipantJid.equals(msg.getTo()));
		record.setRead(isRead);
		record.setSayTime(new Date().getTime());
		if (multimedia != null) {// 多媒体
			if (multimedia.type == null) {
				record.setType(ChatType.TXT.name());
			} else {
				record.setType(multimedia.type);
			}
			ChatType recordType = ChatType.valueOf(record.getType());
			if (recordType == ChatType.AUDIO) {
				String newFName = MD5.getMD5(multimedia.data) + ".amr";
				String fPath = FileStorage.buildChatAudioPath(newFName);
				File audioFile = new File(VLib.getSdcardDir(), fPath);
				if (!audioFile.exists()) {
					byte[] bytes = Base64Coder.decode(multimedia.data);
					FileUtil.saveToFile(bytes, audioFile);
				}
				record.setData(fPath);
				record.setDuring(multimedia.during == null ? 0 : multimedia.during);
			} else if (recordType == ChatType.IMAGE) {
				String newFName = MD5.getMD5(multimedia.data) + ".jpg";
				String fPath = FileStorage.buildChatImgPath(newFName);
				File imgFile = new File(VLib.getSdcardDir(), fPath);
				if (!imgFile.exists()) {
					byte[] bytes = Base64Coder.decode(multimedia.data);
					FileUtil.saveToFile(bytes, imgFile);
				}
				record.setData(fPath);
			} else if (recordType == ChatType.VIDEO) {
				String newFName = MD5.getMD5(multimedia.data) + ".mp4";
				String fPath = FileStorage.buildChatVideoPath(newFName);
				File imgFile = new File(VLib.getSdcardDir(), fPath);
				if (!imgFile.exists()) {
					byte[] bytes = Base64Coder.decode(multimedia.data);
					FileUtil.saveToFile(bytes, imgFile);
				}
				record.setData(fPath);
			}
		} else {
			List<CharSequence> list = XHTMLManager.getBodies(msg);
			if (list != null && list.size() > 0) {// HTML
				record.setData(list.get(0).toString());
				record.setType(ChatType.HTML.name());
			} else if (msg.getBody() != null) {// TXT
				record.setData(msg.getBody());
				record.setType(ChatType.TXT.name());
			}
		}
		DBManager.get().saveEntity(record);
		return record;
	}
	
}
