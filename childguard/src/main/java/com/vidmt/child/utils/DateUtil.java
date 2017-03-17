package com.vidmt.child.utils;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.TextView;

import com.vidmt.acmn.utils.java.CommUtil;
import com.vidmt.child.App;
import com.vidmt.child.R;
import com.vidmt.child.entities.ChatRecord;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@SuppressLint("SimpleDateFormat")
public class DateUtil {
	public static final int TODAY = 0;// 今天
	public static final int YESTERDAY = -1;// 昨天
	public static final int BEFORE_Y = -2;// 前天
	private static final long TIME_TAG_INTERVAL = 1 * 60 * 1000;

	@SuppressWarnings("deprecation")
	public static long formatDay(long dayTimeInMillis) {
		Date now = new Date();
		Date minToday = new Date(now.getYear(), now.getMonth(), now.getDate(), 0, 0, 0);
		Date maxToday = new Date(now.getYear(), now.getMonth(), now.getDate(), 23, 59, 59);
		long minTodayInMillis = minToday.getTime();
		long maxTodayInMillis = maxToday.getTime();
		long minYesterdayInMillis = minTodayInMillis - 24 * 60 * 60 * 1000;
		long maxYesterdayInMillis = maxTodayInMillis - 24 * 60 * 60 * 1000;
		long minBeforeInmillis = minYesterdayInMillis - 24 * 60 * 60 * 1000;
		long maxBeforeInmillis = maxYesterdayInMillis - 24 * 60 * 60 * 1000;
		if (dayTimeInMillis >= minTodayInMillis && dayTimeInMillis <= maxTodayInMillis) {
			return TODAY;
		} else if (dayTimeInMillis >= minYesterdayInMillis && dayTimeInMillis <= maxYesterdayInMillis) {
			return YESTERDAY;
		} else if (dayTimeInMillis >= minBeforeInmillis && dayTimeInMillis <= maxBeforeInmillis) {
			return BEFORE_Y;
		} else {
			return dayTimeInMillis;// 更早
		}
	}

	/**
	 *
	 * @param dayOffset
	 *            :+1后一天，-1前一天
	 * @return yyyy-MM-dd
	 */
	public static String getDateStr(int dayOffset) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		long curTime = new Date().getTime();
		String dateStr = df.format(curTime + dayOffset * 24 * 60 * 60 * 1000);
		return dateStr;
	}

	private static String getChatTimeStr(long sayTime) {
		String timeStr = null;
		DateFormat df = new SimpleDateFormat("HH:mm:ss");
		long formattedDay = DateUtil.formatDay(sayTime);
		Date sayTimeDate = new Date(sayTime);
		if (formattedDay == TODAY) {
			timeStr = App.get().getString(R.string.today) + df.format(sayTimeDate);
		} else if (formattedDay == YESTERDAY) {
			timeStr = App.get().getString(R.string.yesterday) + df.format(sayTimeDate);
		} else if (formattedDay == BEFORE_Y) {
			timeStr = App.get().getString(R.string.before_y) + df.format(sayTimeDate);
		} else {
			timeStr = CommUtil.formatDate(sayTimeDate);
		}
		return timeStr;
	}

	/**
	 * @param timeTv
	 * @param chatContents
	 * @param position
	 */
	public static void setChatTime(TextView timeTv, List<ChatRecord> chatContents, int position) {
		// [stackFromBottom="true"]position:顶->底 0->19,加载19->0
		ChatRecord chatContent = chatContents.get(position);
		long time = chatContent.getSayTime();
		String timeStr = getChatTimeStr(time);
		if (position == 0) {// 最顶部的一条
			timeTv.setVisibility(View.VISIBLE);
			timeTv.setText(timeStr);
		} else {
			ChatRecord preChatContent = chatContents.get(position - 1);// 上一条记录
			long diff = Math.abs(time - preChatContent.getSayTime());
			if (diff >= TIME_TAG_INTERVAL) {
				timeTv.setVisibility(View.VISIBLE);
				timeTv.setText(timeStr);
			} else {
				timeTv.setVisibility(View.INVISIBLE);
			}
		}
	}

}
