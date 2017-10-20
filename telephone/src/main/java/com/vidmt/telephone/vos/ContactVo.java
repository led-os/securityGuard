package com.vidmt.telephone.vos;

import android.graphics.Bitmap;

public class ContactVo {
	/**
	 * 添加、邀请、已是好友
	 */
	public static enum RecommendType {
		ADD, INVITE, FRIEND
	}

	public String uid;
	public String name;// 联系人名称
	public String phone;// 联系人号码
	public Bitmap avatar;// 联系人头像
	public String avatarUri;// 联系人头像uri
	public RecommendType recommendType = RecommendType.INVITE;
}
