package com.vidmt.telephone.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import android.annotation.SuppressLint;
import android.widget.TextView;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.java.CommUtil;
import com.vidmt.telephone.App;
import com.vidmt.telephone.R;
import com.vidmt.telephone.entities.ChatRecord;

@SuppressLint("SimpleDateFormat")
public class DateUtil {
	public static final int TODAY = 0;// 今天
	public static final int YESTERDAY = -1;// 昨天
	public static final int BEFORE_Y = -2;// 前天

	public static String getDeltaTime(long end, long begin) {
		long delta = (end - begin) / 1000;// 秒

		int hour = (int) (delta / 3600);
		int minute = (int) ((delta - hour * 3600) / 60);
		int second = (int) (delta - hour * 3600 - minute * 60);

		return formatTime(hour) + ":" + formatTime(minute) + ":" + formatTime(second);
	}

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

	public static String formatTime(int t) {// 如：7 -> 07,针对小时、分钟
		String time = String.valueOf(t);
		if (time.length() == 1) {
			time = "0" + time;
		}
		return time;
	}

	public static String formatNearbyTime(long time) {
		long delta = System.currentTimeMillis() - time;
		if (delta <= 1000) {
			return "现在";
		}
		long seconds = delta / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		long days = hours / 24;
		if (days != 0) {
			return days + "天前";
		} else if (hours != 0) {
			return hours + "小时前";
		} else if (minutes != 0) {
			if(minutes <=10){
				return "现在";
			}else {
				return minutes + "分钟前";
			}
		} else {
			//return seconds + "秒前";
			return "现在";
		}
	}

	/**
	 * 根据用户生日计算年龄
	 */
	public static int getAgeByBirthDate(Date birthday) {
		int age = 0;
		Calendar cal = Calendar.getInstance();
		if (cal.before(birthday)) {
			throw new IllegalArgumentException("The birthDay is before Now.It's unbelievable!");
		}
		int yearNow = cal.get(Calendar.YEAR);
		int monthNow = cal.get(Calendar.MONTH) + 1;
		int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);

		cal.setTime(birthday);
		int yearBirth = cal.get(Calendar.YEAR);
		int monthBirth = cal.get(Calendar.MONTH) + 1;
		int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);

		age = yearNow - yearBirth;

		if (monthNow <= monthBirth) {
			if (monthNow == monthBirth) {
				if (dayOfMonthNow < dayOfMonthBirth) {
					age--;
				}
			} else {
				age--;
			}
		}
		return age;
	}

	/**
	 * 根据用户年龄生成大致生日
	 */
	public static long getBirthTimeByAge(int age) {
		Calendar cal = Calendar.getInstance();
		int yearNow = cal.get(Calendar.YEAR);
//		int monthNow = cal.get(Calendar.MONTH) + 1;
//		int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-M-d");
		String birthStr = (yearNow - age) + "-" + 1 + "-" + 1;
		try {
			return dateFormat.parse(birthStr).getTime();
		} catch (ParseException e) {
			VLog.e("test", e);
		}
		return 0;
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
	
	public static String getMsgListTimeStr(long sayTime) {
		String timeStr = null;
		DateFormat df = new SimpleDateFormat("HH:mm");
		long formattedDay = DateUtil.formatDay(sayTime);
		Date sayTimeDate = new Date(sayTime);
		if (formattedDay == TODAY) {
			timeStr = df.format(sayTimeDate);
		} else if (formattedDay == YESTERDAY) {
			timeStr = App.get().getString(R.string.yesterday) + df.format(sayTimeDate);
		} else if (formattedDay == BEFORE_Y) {
			timeStr = App.get().getString(R.string.before_y) + df.format(sayTimeDate);
		} else {
			df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			timeStr = df.format(new Date(sayTime));
		}
		return timeStr;
	}

	private static final long TIME_TAG_INTERVAL = 1 * 60 * 1000;

	public static void setChatTime(TextView timeTv, List<ChatRecord> chatContents, int position) {
		// [stackFromBottom="true"]position:顶->底 0->19,加载19->0
		ChatRecord chatContent = chatContents.get(position);
		long time = chatContent.getSayTime();
		String timeStr = getChatTimeStr(time);
		if (position == 0) {// 最顶部的一条
			timeTv.setText(timeStr);
		} else {
			ChatRecord preChatContent = chatContents.get(position - 1);// 上一条记录
			long diff = Math.abs(time - preChatContent.getSayTime());
			if (diff >= TIME_TAG_INTERVAL) {
				timeTv.setText(timeStr);
			} else {
				timeTv.setText("");
			}
		}
	}

}
