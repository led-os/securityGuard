package com.vidmt.telephone.utils;

import java.util.ArrayList;
import java.util.List;

import com.vidmt.telephone.entities.ChatRecord;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.managers.DBManager;

public class DBUtil {
	private static final DBManager mDbMgr = DBManager.get();

	public static void setAllChatRead(String friendUid) {
		List<ChatRecord> chatRecords = DBManager.get().getWhereEntity(ChatRecord.class, "suid", getCurUid(), "fuid",
				friendUid);
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

	//huawei add to delete chat records;
	public static void deleteAllChatRecod(String friendUid) {
		List<ChatRecord> chatRecords = DBManager.get().getWhereEntity(ChatRecord.class, "suid", getCurUid(), "fuid",
				friendUid);
		if (chatRecords == null) {
			return;
		}
		for (ChatRecord chatRecord : chatRecords) {
			mDbMgr.delete(chatRecord);
		}
	}

	public static int getUnReadNum(String friendUid) {
		List<ChatRecord> recordList = mDbMgr.getWhereEntity(ChatRecord.class, "suid", getCurUid(), "fuid", friendUid,
				"isread", false);
		if (recordList == null) {
			return 0;
		}
		return recordList.size();
	}

	public static int getUnReadNums(String[] uidArr) {
		return mDbMgr.getInEntityCount(ChatRecord.class, "fuid", uidArr, "suid", getCurUid(), "isread", false);
	}

	public static int getAllUnReadNum() {
		List<ChatRecord> recordList = mDbMgr.getWhereEntity(ChatRecord.class, "isread", false);
		if (recordList == null) {
			return 0;
		}
		return recordList.size();
	}

	public static int getChatRecordCount(String friendUid) {
		int count = mDbMgr.getWhereCount(ChatRecord.class, "suid", getCurUid(), "fuid", friendUid);
		return count;
	}

	public static List<ChatRecord> getRangeChatRecords(String friendUid, int start, int len) {
		List<ChatRecord> recordList = mDbMgr.getRangeWhereEntity(ChatRecord.class, "suid", getCurUid(), "fuid",
				friendUid, start, len);
		if (recordList == null) {
			recordList = new ArrayList<ChatRecord>();
		}
		return recordList;
	}

	public static ChatRecord getLastChatRecord(String friendUid) {
		return mDbMgr.getWhereEndEntity(ChatRecord.class, "suid", getCurUid(), "fuid", friendUid, "saytime", true);
	}

	public static void saveChatRecord(ChatRecord chatRecord) {
		chatRecord.setSuid(getCurUid());
		mDbMgr.saveEntity(chatRecord);
	}

	private static String getCurUid() {
		User curUser = AccManager.get().getCurUser();
		if (curUser != null) {
			return curUser.uid;
		}
		return null;
	}

}
