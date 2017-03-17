package com.vidmt.child.utils;

import android.graphics.Bitmap;
import android.location.Location;

import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.child.Config;
import com.vidmt.child.exceptions.VidException;
import com.vidmt.child.listeners.AvatarChangedListener;
import com.vidmt.child.managers.HttpManager;
import com.vidmt.child.utils.Enums.PayType;
import com.vidmt.child.vos.LocationVo;
import com.vidmt.child.vos.WxpayInfoVo;
import com.vidmt.xmpp.enums.XmppEnums.LvlType;

import java.io.File;
import java.io.IOException;

public class HttpUtil {
	private static final HttpManager mHttpMgr = HttpManager.get();

	private static final String API_SERVER = Config.WEB_API_SERVER;
	private static final String RES_SERVER = Config.WEB_RES_SERVER;

	private static final String URL_CHANGE_PWD = API_SERVER + "/changepwd";
	private static final String URL_LOCATION = API_SERVER + "/location";
	private static final String URL_UPLOAD = API_SERVER + "/upload";

	private static final String URL_PAY_GET_PAY_INFO = API_SERVER + "/pay/getpayinfo";

	public static void changePwd(String uid, String newpwd) throws VidException {
		String rawJson = mHttpMgr.postResponseSync(URL_CHANGE_PWD, "uid", uid, "newpwd", newpwd);
		JsonUtil.getCorrectJsonResult(rawJson);
	}

	public static void uploadLocation(final String uid, final Location loc) throws VidException {
		String rawJson = mHttpMgr.postResponseSync(URL_LOCATION, "uid", uid, "lat", loc.getLatitude() + "", "lon",
				loc.getLongitude() + "");
		JsonUtil.getCorrectJsonResult(rawJson);
	}

	public static LocationVo getLocation(String uid) throws VidException {
		String rawJson = mHttpMgr.getResponseSync(URL_LOCATION, "uid", uid);
		JsonResult<LocationVo> result = JsonUtil.getCorrectJsonResult(rawJson, LocationVo.class);
		return result.getData();
	}

	public static void setAvatar(Bitmap bm, final boolean forParent) throws VidException, IOException {
		AvatarChangedListener.get().triggerOnAvatarChanged(forParent, bm);// 本地瞬间改变
		final String uid;
		if (forParent) {
			uid = UserUtil.getParentInfo().uid;
		} else {
			uid = UserUtil.getBabyInfo().uid;
		}
		final File avatarFile = VidUtil.genTmpAvatarPath(uid);
		AvatarUtil.saveBitmap2file(bm, avatarFile);
		String url = mHttpMgr.getParamsUrl(URL_UPLOAD, "uid", uid);
		mHttpMgr.uploadFile(url, avatarFile, new RequestCallBack<String>() {
			@Override
			public void onSuccess(ResponseInfo<String> responseInfo) {
				String avatarUri = responseInfo.result;
				VidUtil.clearOldAvatar(uid);
				avatarFile.renameTo(new File(VLib.getSdcardDir(), avatarUri));
				if (forParent) {
					UserUtil.getParentInfo().avatarUri = avatarUri;
				} else {
					UserUtil.getBabyInfo().avatarUri = avatarUri;
				}
				VLog.i("tmpTest", "setAvatar：" + avatarUri);
			}

			@Override
			public void onFailure(HttpException error, String msg) {
				VLog.e("test", error + "," + msg);
				MainThreadHandler.makeToast("upload avatar FAIL:" + msg);
			}
		});
	}

	public static String getAlipayPayInfo(LvlType lvlType, int payNum) throws VidException {
		String uid = UserUtil.getParentInfo().uid;
		String rawJson = HttpManager.get().getResponseSync(URL_PAY_GET_PAY_INFO, "uid", uid, "lvlType", lvlType.name(),
				"payNum", payNum + "", "payType", PayType.ALI.name());
		JsonResult<String> result = JsonUtil.getCorrectJsonResult(rawJson);
		return result.getMsg();
	}

	public static WxpayInfoVo getWxpayPayInfo(LvlType vipType, int payNum) throws VidException {
		String uid = UserUtil.getParentInfo().uid;
		String rawJson = HttpManager.get().getResponseSync(URL_PAY_GET_PAY_INFO, "uid", uid, "lvlType", vipType.name(),
				"payNum", payNum + "", "payType", PayType.WX.name());
		JsonResult<WxpayInfoVo> result = JsonUtil.getCorrectJsonResult(rawJson, WxpayInfoVo.class);
		return result.getData();
	}

}
