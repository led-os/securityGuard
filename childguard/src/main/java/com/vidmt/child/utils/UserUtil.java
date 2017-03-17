package com.vidmt.child.utils;

import android.app.Activity;
import android.os.Bundle;

import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.child.App;
import com.vidmt.child.Const;
import com.vidmt.child.PrefKeyConst;
import com.vidmt.child.R;
import com.vidmt.child.dlgs.BeVipDlg;
import com.vidmt.child.utils.Enums.VipFuncType;
import com.vidmt.child.vos.LvlVo;
import com.vidmt.child.vos.UserVo;
import com.vidmt.xmpp.enums.XmppEnums.LvlType;
import com.vidmt.xmpp.exts.CgUserIQ;
import com.vidmt.xmpp.exts.LvlIQ;

public class UserUtil {
	private static final UserVo PARENT = new UserVo();
	private static final UserVo BABY = new UserVo();
	private static final LvlVo LVL = new LvlVo();
	private static long START_TIME;

	public static void initParentInfo(CgUserIQ cgUser) {// 注：数据可能为空
		START_TIME = System.nanoTime();

		PARENT.uid = SysUtil.getPref(PrefKeyConst.PREF_ACCOUNT);
		PARENT.code = cgUser.code;
		PARENT.ttl = cgUser.ttl;
		PARENT.nick = cgUser.nick;
		PARENT.avatarUri = cgUser.avatarUri;
	}

	public static void initBabyInfo(CgUserIQ cgUser) {
		BABY.uid = Const.BABY_ACCOUNT_PREFIX + SysUtil.getPref(PrefKeyConst.PREF_ACCOUNT);
		BABY.nick = cgUser.nick;
		BABY.avatarUri = cgUser.avatarUri;
	}

	public static void upgradeLvl(CgUserIQ userIq, LvlIQ lvlIq) {
		PARENT.ttl = userIq.ttl;
		if (lvlIq != null) {
			PARENT.code = userIq.code;
			initLvl(lvlIq);
		}
	}

	public static void initLvl(LvlIQ lvl) {
		LVL.code = lvl.code == null ? LvlType.NONE : lvl.code;
		LVL.price = lvl.price;
		LVL.expire = lvl.expire;
		LVL.remoteAudio = lvl.remoteAudio;
		LVL.alarm = lvl.alarm;
		LVL.nav = lvl.nav;
		LVL.traceNum = lvl.traceNum;
		LVL.fenceNum = lvl.fenceNum;
		LVL.noAd = lvl.noAd;
		LVL.hideIcon = lvl.hideIcon;
		LVL.sport = lvl.sport;
	}

//	public static UserVo getSelfInfo() {
//		boolean isBabyClient = SysUtil.getBooleanPref(PrefKeyConst.PREF_IS_BABY_CLIENT);
//		if (isBabyClient) {
//			return BABY;
//		}
//		return PARENT;
//	}
//
//	public static UserVo getSideInfo() {
//		boolean isBabyClient = SysUtil.getBooleanPref(PrefKeyConst.PREF_IS_BABY_CLIENT);
//		if (isBabyClient) {
//			return PARENT;
//		}
//		return BABY;
//	}

	public static UserVo getBabyInfo() {
		return BABY;
	}
	
	public static UserVo getParentInfo() {
		return PARENT;
	}

	public static LvlVo getLvl() {
		return LVL;
	}
	
	public static boolean isAuthorized(Activity act, VipFuncType type) {
		if (LVL.code == LvlType.NONE) {// 非会员
			new BeVipDlg(act, R.string.recharge, R.string.not_gold_vip).show();
			return false;
		}
//		if (!isPermitted(type)) {// 低级会员
//			new BeVipDlg(act, R.string.vip_upgrade, R.string.no_permission).show();
//			return false;
//		}
		if (isExpired()) {// 会员已过期
			new BeVipDlg(act, R.string.renewal, R.string.expired).show();
			return false;
		}
		return true;
	}

	private static boolean isPermitted(VipFuncType type) {
		if (type == VipFuncType.REMOTE_AUDIO) {
			return LVL.remoteAudio;
		} else if (type == VipFuncType.ALARM) {
			return LVL.alarm;
		} else if (type == VipFuncType.NAV) {
			return LVL.nav;
		} else if (type == VipFuncType.TRACE) {
			return LVL.traceNum != 0;
		} else if (type == VipFuncType.NO_AD) {
			return LVL.noAd;
		} else if (type == VipFuncType.HIDE_ICON) {
			return LVL.hideIcon;
		} else if (type == VipFuncType.SPORT) {
			return LVL.sport;
		}
		return false;
	}

	public static boolean isExpired() {
		return System.nanoTime() - START_TIME > PARENT.ttl * 1E9;// 纳秒
	}

	/**
	 * price：价格,单位:元，wxpay:是否为微信支付
	 */
	public static String getPriceStr(float price, boolean isWxpay) {
		if (isWxpay) {// 支付宝精确两位小数点单位:元；微信-单位:分
			price = price * 100;
		}
		String priceStr = price + "";
		if (priceStr.endsWith(".0")) {
			return priceStr.substring(0, priceStr.length() - 2);
		}
		return priceStr;
	}

	public static String getExpireUnit(int expire) {
		int year = expire / 365;
		if (year == 0) {
			int month = expire / 30;
			if (month == 1) {
				return VLib.app().getString(R.string.month);
			} else {
				return VLib.app().getString(R.string.half_year);
			}
		} else {
			return VLib.app().getString(R.string.year);
		}
	}

	public static Bundle lvlIq2Bundle(LvlIQ lvlIq) {
		Bundle bundle = new Bundle();
		bundle.putString("code", lvlIq.code.name());
		bundle.putFloat("price", lvlIq.price);
		bundle.putInt("expire", lvlIq.expire);
		bundle.putInt("traceNum", lvlIq.traceNum);
		bundle.putInt("fenceNum", lvlIq.fenceNum);
		bundle.putBoolean("remoteAudio", lvlIq.remoteAudio);
		bundle.putBoolean("alarm", lvlIq.alarm);
		bundle.putBoolean("nav", lvlIq.nav);
		bundle.putBoolean("noAd", lvlIq.noAd);
		bundle.putBoolean("hideIcon", lvlIq.hideIcon);
		bundle.putBoolean("sport", lvlIq.sport);
		return bundle;
	}

	public static LvlVo bundle2Lvl(Bundle bundle) {
		LvlVo lvl = new LvlVo();
		lvl.code = LvlType.valueOf(bundle.getString("code"));
		lvl.price = bundle.getFloat("price");
		lvl.expire = bundle.getInt("expire");
		lvl.traceNum = bundle.getInt("traceNum");
		lvl.fenceNum = bundle.getInt("fenceNum");
		lvl.remoteAudio = bundle.getBoolean("remoteAudio");
		lvl.alarm = bundle.getBoolean("alarm");
		lvl.nav = bundle.getBoolean("nav");
		lvl.noAd = bundle.getBoolean("noAd");
		lvl.hideIcon = bundle.getBoolean("hideIcon");
		lvl.sport = bundle.getBoolean("sport");
		return lvl;
	}

	public static String lvlCode2PaySubject(LvlType lvlType, int payNum) {
		String prefix = App.get().getString(R.string.app_name) + ":";
		String postfix = "×" + payNum;
		if (lvlType == LvlType.Au) {
			return prefix + App.get().getString(R.string.vip_gold) + postfix;
		} else if (lvlType == LvlType.Ag) {
			return prefix + App.get().getString(R.string.vip_silver) + postfix;
		} else if (lvlType == LvlType.Cu) {
			return prefix + App.get().getString(R.string.vip_bronze) + postfix;
		}
		return null;
	}

	public static int getLeftDays() {
		return (int) (Math.ceil(PARENT.ttl / 24D / 60 / 60));
	}

}