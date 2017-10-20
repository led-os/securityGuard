package com.vidmt.telephone.managers.inner;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Bitmap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.ResponseStream;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.andr.FLog;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.telephone.Config;
import com.vidmt.telephone.cache.MemCacheHolder;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.exceptions.VHttpException;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.managers.HttpManager.IHttpManager;
import com.vidmt.telephone.utils.AvatarUtil;
import com.vidmt.telephone.utils.EncryptUtil;
import com.vidmt.telephone.utils.Enums.ResType;
import com.vidmt.telephone.utils.Enums.VipType;
import com.vidmt.telephone.utils.HttpUtil;
import com.vidmt.telephone.utils.JsonResult;
import com.vidmt.telephone.utils.JsonUtil;
import com.vidmt.telephone.utils.VidUtil;
import com.vidmt.telephone.vos.LocVo;
import com.vidmt.telephone.vos.LvlVo;
import com.vidmt.telephone.vos.TraceVo;
import com.vidmt.telephone.vos.WxpayInfoVo;

public class InnerHttpManagerImpl implements IHttpManager {
	private static String token;
	private static final String API_SERVER = Config.WEB_API_SERVER;
	private static final String UTIL_SERVER = Config.WEB_UTIL_SERVER;

	private static final String URL_USER_LOGIN = API_SERVER + "/api/1/user/login.api";
	private static final String URL_USER_GET_UID_BY_ACCOUNT = API_SERVER + "/api/1/user/getUidByAccount.api";
	private static final String URL_USER_RECONNECT = API_SERVER + "/api/1/user/reconnect.api";
	private static final String URL_LOGOUT = API_SERVER + "/api/1/user/logout.api";//退出登录

	private static final String URL_USER_REGISTER = API_SERVER + "/api/1/user/register.api";
	private static final String URL_USER_CHANGE_PWD = API_SERVER + "/api/1/user/changePwd.api";
	private static final String URL_USER_GET = API_SERVER + "/api/1/user/get.api";
	private static final String URL_USER_GET_BY_ACCOUNT = API_SERVER + "/api/1/user/getByAcc.api";
	private static final String URL_USER_GET_MULT = API_SERVER + "/api/1/user/getMult.api";
	private static final String URL_USER_GET_REQUEST_USERS = API_SERVER + "/api/1/user/getReqUsers.api";
	private static final String URL_USER_UPDATE = API_SERVER + "/api/1/user/update.api";//更新用户信息
	private static final String URL_USER_FILE_UPLOAD = API_SERVER + "/api/1/user/upload.api";
	private static final String URL_USER_MATCH_PHONES = API_SERVER + "/api/1/user/matchPhones.api";


	private static final String URL_LOCATION_UPLOAD = API_SERVER + "/api/1/location/upload.api";
	private static final String URL_LOCATION_GET_NEARBY = API_SERVER + "/api/1/location/getNearby.api";
	private static final String URL_LOCATION_GET_FRIENDS = API_SERVER + "/api/1/location/getFriends.api";

	private static final String URL_TRACE_GET = API_SERVER + "/api/1/trace/get.api";

	private static final String URL_PAY_ALIPAY_GET_PAY_INFO = API_SERVER + "/api/1/pay/alipay/getPayInfo.api";
	private static final String URL_PAY_WXPAY_GET_PAY_INFO = API_SERVER + "/api/1/pay/wxpay/getPayInfo.api";
	
	private static final String URL_LVL_GET = API_SERVER + "/api/1/lvl/get.api";

	private static final String URL_WEB_GET_PHONE_ADDR = UTIL_SERVER +"/getPhoneCity.json?phone=";

	@Override
	public void register(String account, String pwd) throws VidException {
		String rawJson = HttpUtil.postResponseSync(URL_USER_REGISTER, "account", account, "pwd", pwd);
		FLog.d("register: ", "" + rawJson);
		JsonUtil.getCorrectJsonResult(rawJson);
	}

	@Override
	public String getUidByAccount(String account, String pwd) throws VidException {
		String rawJson = HttpUtil.getResponseSync(URL_USER_GET_UID_BY_ACCOUNT, "account", account, "pwd", pwd);
		JsonResult<String> res = JsonUtil.getCorrectJsonResult(rawJson);
		String uid = res.getMsg();
		return uid;
	}

	@Override
	public User login(String account, String pwd) throws VidException {
		String rawJson = HttpUtil.getResponseSync(URL_USER_LOGIN, "account", account, "pwd", pwd);
		FLog.d("login: ", "" + rawJson);
		JsonResult<User> result = JsonUtil.getCorrectJsonResult(rawJson, User.class);
		User user = result.getData();
		token = user.token;
		return user;
	}

	@Override
	public void reconnect(String account, String pwd) throws VidException {
		String rawJson = HttpUtil.getResponseSync(URL_USER_RECONNECT, "account", account, "pwd", pwd);
		FLog.d("reconnect: ", "" + rawJson);
		JsonResult<String> result = JsonUtil.getCorrectJsonResult(rawJson);
		token = result.getMsg();
	}
	
	@Override
	public void logout() throws VidException {
		String rawJson = HttpUtil.postResponseSync(URL_LOGOUT, "token", token);
		JsonUtil.getCorrectJsonResult(rawJson);
	}

	@Override
	public User getUserInfo(String uid) throws VidException {
		User user = MemCacheHolder.getUser(uid);
		if (user == null) {
			String rawJson = HttpUtil.getResponseSync(URL_USER_GET, "uid", uid, "token", token);
			JsonResult<User> result = JsonUtil.getCorrectJsonResult(rawJson, User.class);
			user = result.getData();
			if (user.uid == null) {
				return null;
			}
			MemCacheHolder.updateUser(user);
		}
		return user;
	}

	/**
	 * 通过手机号查找用户，搜索页用
	 * @param account
	 * @return
	 * @throws VidException
	 */
	@Override
	public User getUserByAccount(String account) throws VidException {
		String rawJson = HttpUtil.getResponseSync(URL_USER_GET_BY_ACCOUNT, "account", account, "token", token);
		JsonResult<User> result = JsonUtil.getCorrectJsonResult(rawJson, User.class);
		User user = result.getData();
		if(user == null){
			return null;
		}
		if (user.uid == null) {
			return null;
		}
		MemCacheHolder.updateUser(user);
		return user;
	}

	/**
	 * 获取手机联系人使用软件的用户
	 * @param phones
	 * @return
	 * @throws VidException
	 */
	@Override
	public List<User> getUserMatchPhones(List<String> phones) throws VidException {
		List<User> users = null;
		String phonesStr = VidUtil.getArrayString(phones);
		String rawJson = HttpUtil.getResponseSync(URL_USER_MATCH_PHONES, "phones",
					phonesStr, "token", token);
		JsonResult<User> result = JsonUtil.getCorrectJsonResult(rawJson, User.class);
		users = result.getArrayData();
		return users;
	}

	/**
	 * 注：不是按uids顺序返回
	 * 根据uid获取用户
	 */
	@Override
	public List<User> getMultUser(List<String> uids) throws VidException {
		List<User> cachedUsers = new ArrayList<User>();
		List<String> noCachedUids = new ArrayList<String>();
		for (String uid : uids) {
			User user = MemCacheHolder.getUser(uid);
			if (user != null) {
				cachedUsers.add(user);
			} else {
				noCachedUids.add(uid);
			}
		}
		List<User> users = null;
		if (noCachedUids.size() > 0) {
			String jsonUidsStr = JSON.toJSONString(noCachedUids);
			String rawJson = HttpUtil.getResponseSync(URL_USER_GET_MULT, "uids",
					EncryptUtil.encryptParam(jsonUidsStr), "token", token);
			JsonResult<User> result = JsonUtil.getCorrectJsonResult(rawJson, User.class);
			users = result.getArrayData();
		}
		if (users != null) {
			users.addAll(cachedUsers);
		} else {
			users = cachedUsers;
		}
		return users;
	}

	/**
	 * 获得请求加好友的人
	 * @param justLatestOne
	 * @return
	 * @throws VidException
	 */
	@Override
	public List<User> getRequestUsers(boolean justLatestOne) throws VidException {
		String rawJson = HttpUtil.getResponseSync(URL_USER_GET_REQUEST_USERS, "justLatestOne", justLatestOne ? "T"
				: "F", "token", token);
		JsonResult<User> result = JsonUtil.getCorrectJsonResult(rawJson, User.class);
		return result.getArrayData();
	}

	@Override
	public void updateUser(User user) throws VidException {
		Map<String, String> paramMap = new HashMap<String, String>();
		if (user.nick != null) {
			paramMap.put("nick", EncryptUtil.encryptParam(user.nick));
		}
		if (user.age != null) {
			paramMap.put("age", user.age + "");
		}
		if (user.gender != null) {
			paramMap.put("gender", user.gender.toString());
		}
		if (user.address != null) {
			paramMap.put("address", EncryptUtil.encryptParam(user.address));
		}
		if (user.signature != null) {
			paramMap.put("signature", EncryptUtil.encryptParam(user.signature));
		}
		if (user.locSecret != null) {
			paramMap.put("locSecret", user.locSecret + "");
		}
		if (user.avoidDisturb != null) {
			paramMap.put("avoidDisturb", user.avoidDisturb + "");
		}
		//huawei change;
		if(paramMap.size() <= 0){
			return;
		}
		paramMap.put("token", token);
		String url = HttpUtil.getParamsUrl(URL_USER_UPDATE, paramMap);
		String rawJson = HttpUtil.postResponseSync(url);
		JsonUtil.getCorrectJsonResult(rawJson);
	}

	/**
	 * @param type 头像或相册
	 * @param bm
	 */
	@Override
	public void uploadFile(ResType type, Bitmap bm) {
		final String curUid = AccManager.get().getCurUser().uid;
		final File avatarFile = VidUtil.genTmpAvatarPath(curUid);
		AvatarUtil.saveBitmap2file(bm, avatarFile);
		String url = HttpUtil.getParamsUrl(URL_USER_FILE_UPLOAD, "type", type.name(), "token", token);


//		HttpUtil.uploadFile(url, avatarFile, new RequestCallBack<String>() {
//			@Override
//			public void onSuccess(ResponseInfo<String> responseInfo) {
//				String jsonResult = responseInfo.result;
//				try {
//					JsonResult<String> result = JsonUtil.getCorrectJsonResult(jsonResult);
//					final String avatarUri = result.getMsg();
//					VidUtil.clearOldAvatar(curUid);
//					avatarFile.renameTo(new File(VLib.getSdcardDir(), avatarUri));
//					VLog.i("test", "upload file success:" + avatarUri);
//					ThreadPool.execute(new Runnable() {
//						@Override
//						public void run() {
//							try {
//								AccManager.get().setUserInfo("avatarUri", avatarUri);
//							} catch (VidException e) {
//								VLog.e("test", e);
//							}
//						}
//					});
//				} catch (VidException e) {
//					VLog.e("test", e);
//				}
//			}
//
//			@Override
//			public void onFailure(HttpException error, String msg) {
//				VLog.e("test", error + "," + msg);
//			}
//		});
		try {
			ResponseStream responseStream = HttpUtil.uploadFile(url, avatarFile);
			String s = responseStream.readString();
			JsonResult<String> result = JsonUtil.getCorrectJsonResult(s);
			final String avatarUri = result.getMsg();
					VidUtil.clearOldAvatar(curUid);
					avatarFile.renameTo(new File(VLib.getSdcardDir(), avatarUri));
					VLog.i("test", "upload file success:" + avatarUri);
					ThreadPool.execute(new Runnable() {
						@Override
						public void run() {
							try {
								AccManager.get().setUserInfo("avatarUri", avatarUri);
							} catch (VidException e) {
								VLog.e("test", e);
							}
						}
					});
			responseStream.close();
		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (VidException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void uploadLocation(double lat, double lon) throws VidException {
		VidUtil.fLog("uploadLocation  : token :" + token);
		String rawJson = HttpUtil
				.getResponseSync(URL_LOCATION_UPLOAD, "lat", lat + "", "lon", lon + "", "token", token);
		VidUtil.fLog("uploadLocation  : rawJson :" + rawJson);
		JsonUtil.getCorrectJsonResult(rawJson);
	}

	@Override
	public List<LocVo> getFriendLocs(List<String> uids) throws VidException {
		String jsonUidsStr = JSON.toJSONString(uids);
		String rawJson = HttpUtil.getResponseSync(URL_LOCATION_GET_FRIENDS, "uids",
				EncryptUtil.encryptParam(jsonUidsStr), "token", token);
		JsonResult<LocVo> result = JsonUtil.getCorrectJsonResult(rawJson, LocVo.class);
		return result.getArrayData();
	}

	@Override
	public LocVo getLocation(String uid) throws VidException {
		List<String> uids = new ArrayList<String>();
		uids.add(uid);
		List<LocVo> locs = getFriendLocs(uids);
		if (locs != null && locs.size() > 0) {
			return locs.get(0);
		}
		return null;
	}

	@Override
	public List<LocVo> getNearbyLocs(String gender, Integer time, Integer ageStart, Integer ageEnd, int currentPage,
			int pageSize) throws VidException {
		String timeStr = time == null ? null : time + "";
		String ageStartStr = ageStart == null ? null : ageStart + "";
		String ageEndStr = ageEnd == null ? null : ageEnd + "";
		String rawJson = HttpUtil.getResponseSync(URL_LOCATION_GET_NEARBY, "gender", gender, "time", timeStr,
				"ageStart", ageStartStr, "ageEnd", ageEndStr, "currentPage", currentPage + "", "pageSize", pageSize
						+ "", "token", token);
		JsonResult<LocVo> result = JsonUtil.getCorrectJsonResult(rawJson, LocVo.class);
		return result.getArrayData();
	}

	@Override
	public TraceVo getTrace(String uid, String dateStr) throws VidException {
		String rawJson = HttpUtil.getResponseSync(URL_TRACE_GET, "uid", uid, "dateStr", dateStr, "token", token);
		JsonResult<TraceVo> result = JsonUtil.getCorrectJsonResult(rawJson, TraceVo.class);
		return result.getData();
	}

	@Override
	public void changePwd(String account, String newpwd) throws VidException {
		String rawJson = HttpUtil.postResponseSync(URL_USER_CHANGE_PWD, "account", account, "newpwd", newpwd);
		JsonUtil.getCorrectJsonResult(rawJson);
	}

	@Override
	public String getPhoneAddr(String phoneNO) throws VidException {
		String province = null;
		String cityName = null;
		String result = null;
		//新的格式：{"c":0,"d":{"city":"北京","prefix":"1861028","province":"北京","suit":"联通","supplier":"联通"}}
		try {
			String rawJson = HttpUtil.getPhoneAddr(URL_WEB_GET_PHONE_ADDR + phoneNO);
			JSONObject json = JsonUtil.getCorrectRawjson(rawJson);
			province = json.getString("province");
			cityName = json.getString("city");
			result = province + "-" + cityName;
		}catch (VHttpException e){
			VidUtil.fLog("phone number is wrong, cannot search it");
			result = null;
		}
		return result;
	}

	@Override
	public String getAlipayPayInfo(VipType vipType) throws VidException {
		String rawJson = HttpUtil.getResponseSync(URL_PAY_ALIPAY_GET_PAY_INFO, "vipType", vipType.name(), "token", token);
		JsonResult<String> result = JsonUtil.getCorrectJsonResult(rawJson);
		return result.getMsg();
	}
	
	@Override
	public WxpayInfoVo getWxpayPayInfo(VipType vipType) throws VidException {
		String rawJson = HttpUtil.getResponseSync(URL_PAY_WXPAY_GET_PAY_INFO, "vipType", vipType.name(), "token", token);
		JsonResult<WxpayInfoVo> result = JsonUtil.getCorrectJsonResult(rawJson, WxpayInfoVo.class);
		return result.getData();
	}
	
	@Override
	public List<LvlVo> getLvl() throws VidException {
		String rawJson = HttpUtil.getResponseSync(URL_LVL_GET, "token", token);
		JsonResult<LvlVo> result = JsonUtil.getCorrectJsonResult(rawJson, LvlVo.class);
		return result.getArrayData();
	}
}
