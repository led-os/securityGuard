package com.vidmt.telephone.entities;

import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.telephone.PrefKeyConst;
import com.vidmt.telephone.vos.MsgVo;

public class User {
	public String uid;
	public String accType;
	public String account;
	public String nick;
	public Character gender;
	public Integer age;
	public String signature;
	public String address;
	public String avatarUri;
	public String albumUri;

	public String vipType;
	public Character locSecret;
	public Character avoidDisturb;
	public long timeLeft;// 单位:mm
	public long startCountTime = System.nanoTime();// 用于判断是否过期

	public static long NANOTIME_TO_SENCONDS = 1000 * 1000;

	public MsgVo msgVo;

	public String token;

	private boolean isVip;
	public User() {
	}

	@Override
	public String toString() {
		return "User [uid=" + uid + ", accType=" + accType + ", account=" + account + ", nick=" + nick + ", gender="
				+ gender + ", age=" + age + ", signature=" + signature + ", address=" + address + ", avatarUri="
				+ avatarUri + ", albumUri=" + albumUri + ", vipType=" + vipType + ", locSecret=" + locSecret
				+ ", avoidDisturb=" + avoidDisturb + ", timeLeft=" + timeLeft + ", startCountTime=" + startCountTime
				+ ", msgVo=" + msgVo + ", token=" + token + "]";
	}

	public boolean isVip() {
		if (isExpired()) {
			return false;
		}
		return vipType != null;
	}

	public void setVip(boolean vip) {
		isVip = vip;
	}
	public int getLeftDays() {
		if (isExpired()) {
			return -1;
		}
		//huawei change;
		//return (int) (Math.ceil(timeLeft / 24D / 60 / 60 / 1000));
		long relativeTimeLeft = (timeLeft - (long)((System.nanoTime() - startCountTime)/NANOTIME_TO_SENCONDS));
		return (int) (Math.ceil(relativeTimeLeft / 24D / 60 / 60 / 1000));
	}

	private boolean isExpired() {
		return System.nanoTime() - startCountTime > timeLeft * NANOTIME_TO_SENCONDS;// 纳秒
	}

	public boolean isLocSecret() {
		if (locSecret != null && 'T' == locSecret) {
			return true;
		}
		return false;
	}

	public boolean isAvoidDisturb() {
		if (avoidDisturb != null && 'T' == avoidDisturb) {
			return true;
		}
		return false;
	}

	public String getNick() {
		return nick == null ? SysUtil.getPref(PrefKeyConst.PREF_ACCOUNT) : nick;
	}

}
