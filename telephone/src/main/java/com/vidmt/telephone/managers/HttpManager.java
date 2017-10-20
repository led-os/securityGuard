package com.vidmt.telephone.managers;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.graphics.Bitmap;

import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.optional.java.DynProxyUtil;
import com.vidmt.telephone.PrefKeyConst;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.exceptions.VHttpException;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.managers.inner.InnerHttpManagerImpl;
import com.vidmt.telephone.utils.EncryptUtil;
import com.vidmt.telephone.utils.Enums.ResType;
import com.vidmt.telephone.utils.Enums.VipType;
import com.vidmt.telephone.vos.LocVo;
import com.vidmt.telephone.vos.LvlVo;
import com.vidmt.telephone.vos.TraceVo;
import com.vidmt.telephone.vos.WxpayInfoVo;

public class HttpManager {
	private static IHttpManager sInstance;

	private HttpManager() {
	}

	public static IHttpManager get() {
		if (sInstance == null) {
			sInstance = new InnerHttpManagerImpl();
			sInstance = (IHttpManager) DynProxyUtil.newProxy(sInstance, new InvocationHandler() {
				@Override
				public Object invoke(Object tgtObj, Method method, Object[] args) throws Throwable {
					try {
						Object obj = method.invoke(tgtObj, args);
						return obj;
					} catch (Throwable e) {
						if (e instanceof InvocationTargetException) {
							e = e.getCause();
						}
						if (e instanceof VHttpException) {
							VHttpException httpE = (VHttpException) e;
							if (VHttpException.ERR_CODE_USER_NOT_LOGIN == httpE.getCode()) {// 未登录
								User curUser = AccManager.get().getCurUser();
								if (curUser == null) {// 从未登录过
									throw e;
								}
								String encodedPwd = SysUtil.getPref(PrefKeyConst.PREF_PASSWORD);
								if (encodedPwd == null) {// 注销账号瞬间可能导致的NullPointerException
									return null;
								}
								String decodedPwd = EncryptUtil.decryptLocalPwd(encodedPwd);
								IHttpManager httpMgr = (IHttpManager) tgtObj;
								httpMgr.reconnect(curUser.account, decodedPwd);
								try {
									return method.invoke(tgtObj, args);
									//huawei change here fot catch exception outside;
								} catch (InvocationTargetException t) {
									throw t.getCause();
							    }
//								} catch (Throwable t) {
//									throw t;
//								}
							}
						}
						throw e;
					}
				}
			});
		}
		return sInstance;
	}

	public static interface IHttpManager {
		public void register(String account, String pwd) throws VidException;

		public User login(String account, String pwd) throws VidException;

		public void reconnect(String account, String pwd) throws VidException;

		public void logout() throws VidException;

		public String getUidByAccount(String account, String pwd) throws VidException;

		public User getUserInfo(String uid) throws VidException;

		public User getUserByAccount(String account) throws VidException;

		public List<User> getUserMatchPhones(List<String> phones) throws VidException;

		public List<User> getMultUser(List<String> uids) throws VidException;

		public List<User> getRequestUsers(boolean justLatestOne) throws VidException;

		public void updateUser(User user) throws VidException;

		public void uploadFile(ResType type, Bitmap bm);

		public void uploadLocation(double lat, double lon) throws VidException;

		public List<LocVo> getFriendLocs(List<String> uids) throws VidException;

		public LocVo getLocation(String uid) throws VidException;

		public List<LocVo> getNearbyLocs(String gender, Integer time, Integer ageStart, Integer ageEnd,
				int currentPage, int pageSize) throws VidException;

		public TraceVo getTrace(String uid, String dateStr) throws VidException;

		public void changePwd(String account, String newpwd) throws VidException;

		public String getPhoneAddr(String phoneNO) throws VidException;

		public String getAlipayPayInfo(VipType vipType) throws VidException;

		public WxpayInfoVo getWxpayPayInfo(VipType vipType) throws VidException;

		public List<LvlVo> getLvl() throws VidException;

	}
}
