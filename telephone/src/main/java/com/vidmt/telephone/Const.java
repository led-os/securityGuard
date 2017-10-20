package com.vidmt.telephone;

import java.util.List;

import com.vidmt.telephone.utils.Enums.VipType;

public class Const {
	//1分钟定位一次，1分钟获取一次位置；
	public static final int DEFAUL_LOC_FREQUENCY = 2 * 60 * 1000;// 默认定位频率
	public static final int GET_FRIENDS_LOC_FREQUENCY = 2 * 60 * 1000;// 默认定位频率
	public static final int BG_LOC_FREQUENCY = 60 * 1000;// 后台定位频率
	public static final int FACE_COLUMNS = 6;
	public static final int FACE_ROWS = 3;
	public static final int NOTIF_ID_CHAT_RCV = 1;
	public static final int REMOTE_RECORD_TIME_LEN = 10;
	public static final int REMOTE_RECORD_TIME_TAG = -1;

	public static final String QQ_GROUP_KEY = "3S5OP9XyBThrjnayMxDEHgWOk0DYngUq";
	public static final String QQ_GROUP_NO = "337903107";
	public static final String QQ_VIP_GROUP_KEY = "p2C7TrfNIikbwNp0kWzYYRKLaILa6k65";
	public static final String QQ_VIP_GROUP_NO = "258574767";

	public static final String DEF_KEFU_UID = "2";

	// public static final int VIP_YEAR_MONEY = 150;
	// public static final int VIP_TRY_MONEY = 10;
	// public static final int VIP_TRY_DAYS = 7;//天

	public static List<VipType> LVL_LIST;

	// wdj-ad-key
	public static final String AD_APP_ID = "100013819";
	public static final String AD_SECRET_KEY = "dfd9c879073f34188034957a8d036729";
	public static final String AD_APP_WALL = "43999227bf8a0d6d7d9d2210247a4cce";
	public static final String AD_INTERSTITIAL = "6b639b2ae6be50ce944bb9080ad84b74";
	public static final String AD_BANNER = "14db9eae6732e25a6f0f7f9c6d09e397";

	public static final int[] VIP_INFO_TITLE_ID_ARR = new int[] { R.string.vip_fast_add_friend,
			R.string.vip_remote_record, R.string.vip_history_trace, R.string.vip_loc_secret,
			R.string.vip_avoid_disturb, R.string.vip_no_ad };
	public static final int[] VIP_INFO_MSG_ID_ARR = new int[] { R.string.vip_fast_add_friend_detail,
			R.string.vip_remote_record_detail, R.string.vip_history_trace_detail, R.string.vip_loc_secret_detail,
			R.string.vip_avoid_disturb_detail, R.string.vip_no_ad_detail };
	public static final int[] VIP_INFO_ICON_ID_ARR = new int[] { R.drawable.vip_fast_add_friend,
			R.drawable.vip_remote_record, R.drawable.vip_history_trace, R.drawable.vip_loc_secret,
			R.drawable.vip_avoid_disturb, R.drawable.vip_no_ad };
}
