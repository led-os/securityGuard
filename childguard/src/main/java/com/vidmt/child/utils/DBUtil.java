package com.vidmt.child.utils;

import com.vidmt.child.entities.ChatRecord;
import com.vidmt.child.managers.DBManager;

import java.util.ArrayList;
import java.util.List;

public class DBUtil {
	private static final DBManager mDbMgr = DBManager.get();

	public static void setAllChatRead() {
		List<ChatRecord> chatRecords = DBManager.get().getAllEntity(ChatRecord.class);
		if (chatRecords == null) {
			return;
		}
		for (ChatRecord chatRecord : chatRecords) {
			if (chatRecord.isRead()) {
				continue;
			}
			chatRecord.setRead(true);
			mDbMgr.update(chatRecord);
		}
	}

	public static int getUnReadNum() {
		List<ChatRecord> recordList = mDbMgr.getWhereEntity(ChatRecord.class, "isread", false);
		if (recordList == null) {
			return 0;
		}
		return recordList.size();
	}

	public static int getChatRecordCount() {
		int count = mDbMgr.getWhereCount(ChatRecord.class, "name", VidUtil.getSideName());
		return count;
	}

	public static List<ChatRecord> getRangeChatRecords(int start, int len) {
		List<ChatRecord> recordList = mDbMgr.getRangeWhereEntity(ChatRecord.class, "name", VidUtil.getSideName(),
				start, len);
		if(recordList == null){
			recordList = new ArrayList<ChatRecord>();
		}
		return recordList;
	}

}
