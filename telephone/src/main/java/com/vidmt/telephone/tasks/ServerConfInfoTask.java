package com.vidmt.telephone.tasks;

import java.util.HashMap;
import java.util.Map;

import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.telephone.PrefKeyConst;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.listeners.ServerConfDownloadFinishedListener;
import com.vidmt.telephone.utils.HttpUtil;
import com.vidmt.telephone.utils.JsonResult;
import com.vidmt.telephone.utils.JsonUtil;

public class ServerConfInfoTask {
	public static final String SHOW_AD_WALL = "show_ad_wall";
	public static final String SHOW_AD_WIDGET = "show_ad_widget";
	public static final String HIDE_VIP_OPTION = "hide_vip_option";
	public static final String SHOW_AD_BANNER = "show_ad_banner";
	public static final String SHOW_AD_CUSTOM = "show_ad_custom";
	private static final String ALLOW_DIRECT_ADD_FRIEND = "allow_direct_add_friend";
	private static final String ALLOW_SELF_START = "allow_self_start";
	private static final String WEIXINPAY_ANDROID_CONFIG = "weixinpay_android_config";
	private static final String ALIPAY_ANDROID_CONFIG = "alipay_android_config";
	private static final String KEFU_UID= "kefu_uid";
	private static boolean canDirectAddFriend = false;
	private static boolean canSelfStart = false;
	private static boolean mAli_pay = true;
	private static boolean mWeixin_pay = true;
	private static boolean mHideVip = false;

	public static Map<String, Boolean> getAdInfo() {
		final Map<String, Boolean> adInfoMap = new HashMap<String, Boolean>();
		adInfoMap.put(SHOW_AD_WALL, false);
		adInfoMap.put(SHOW_AD_WIDGET, false);
		adInfoMap.put(SHOW_AD_BANNER, false);
		adInfoMap.put(SHOW_AD_CUSTOM, false);
		HttpUtil.getConf(new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				String response = responseInfo.result;
				try {
					JsonResult<String> result = JsonUtil.getCorrectJsonResult(response);
					Map<String, String> map = result.getDataAsMap();
					String wall = map.get(SHOW_AD_WALL);
					String widget = map.get(SHOW_AD_WIDGET);
					String banner = map.get(SHOW_AD_BANNER);
					String custom = map.get(SHOW_AD_CUSTOM);
					String direct_add = map.get(ALLOW_DIRECT_ADD_FRIEND);
					String self_start = map.get(ALLOW_SELF_START);
					String kefu_uid = map.get(KEFU_UID);
					String weixin_pay = map.get(WEIXINPAY_ANDROID_CONFIG);
					String ali_pay = map.get(ALIPAY_ANDROID_CONFIG);
					String hide_vip = map.get(HIDE_VIP_OPTION);
					adInfoMap.put(SHOW_AD_WALL, "true".equals(wall));
					adInfoMap.put(SHOW_AD_WIDGET, "true".equals(widget));
					adInfoMap.put(SHOW_AD_BANNER, "true".equals(banner));
					adInfoMap.put(SHOW_AD_CUSTOM, "true".equals(custom));
					canDirectAddFriend = "true".equals(direct_add);
					canSelfStart = "true".equals(self_start);
					mAli_pay = "true".equals(ali_pay);
					mWeixin_pay = "true".equals(weixin_pay);
					mHideVip = "true".equals(hide_vip);
//					mHideVip =true;
					SysUtil.savePref(PrefKeyConst.PREF_SELF_START, canSelfStart);
					//默认客服UID为2；
					if(kefu_uid == null){
						kefu_uid = "2";
					}
					SysUtil.savePref(PrefKeyConst.PREF_KEFU_ACCOUNT, kefu_uid);
					ServerConfDownloadFinishedListener.get().triggerOnServerConfDownloadFinishedListener();
				} catch (VidException e) {
					VLog.e("test", e);
				}
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				VLog.e("test", error);
				ServerConfDownloadFinishedListener.get().triggerOnServerConfDownloadFinishedListener();
			}
		});
		return adInfoMap;
	}

	public static boolean canDirectAddFriend() {
		return canDirectAddFriend;
	}
	public static boolean canAlipay() {
		return mAli_pay;
	}
	public static boolean hideVip() {
		return mHideVip;
	}
	public static boolean canWeixinPay() {
		return mWeixin_pay;
	}

}
