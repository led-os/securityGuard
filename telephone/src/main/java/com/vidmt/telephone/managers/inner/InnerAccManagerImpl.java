package com.vidmt.telephone.managers.inner;

import java.io.IOException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.IQ.Type;

import android.util.Log;

import com.vidmt.acmn.utils.andr.FLog;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.telephone.PrefKeyConst;
import com.vidmt.telephone.cache.MemCacheHolder;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.exceptions.VHttpException;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.listeners.UserInfoChangedListener;
import com.vidmt.telephone.managers.AccManager.IAccManager;
import com.vidmt.telephone.managers.HttpManager;
import com.vidmt.telephone.tasks.ServerConfInfoTask;
import com.vidmt.telephone.utils.EncryptUtil;
import com.vidmt.telephone.utils.Enums;
import com.vidmt.telephone.utils.VidUtil;
import com.vidmt.xmpp.IXmppManager;
import com.vidmt.xmpp.exts.CgCmdIQ;
import com.vidmt.xmpp.exts.UserExt;
import com.vidmt.xmpp.inner.XmppManager;
import com.vidmt.xmpp.listeners.OnConnectionListener;
import com.vidmt.xmpp.listeners.OnRosterListener;
import com.vidmt.xmpp.listeners.XmppListenerHolder;
import com.vidmt.xmpp.listeners.XmppListenerHolder.IXmppListenerExecutor;
import com.vidmt.xmpp.utils.XmppUtil;

public class InnerAccManagerImpl implements IAccManager {
	private IXmppManager mXmppMgr;
	private User mCurUser;
	private static final long lefttime=1000000000;

	public InnerAccManagerImpl() {
		mXmppMgr = XmppManager.get();
	}

	@Override
	public void setCurUser(User user){
		mCurUser = user;
	}
	@Override
	public User getCurUser() {
		if(mCurUser == null){
			VidUtil.fLog("InnerAccManagerImpl getCurUser is null; maybe login exception need care.");
		}
		if(ServerConfInfoTask.hideVip()){
			mCurUser.vipType= Enums.VipType.TRY.name();
			mCurUser.timeLeft=lefttime;
		}
		return mCurUser;
	}

	@Override
	public void login(String account, String pwd) throws VidException {
		try {
			long start = System.currentTimeMillis();
			VidUtil.fLog("LOGIN_TIME", "=========start=========");
			mCurUser = HttpManager.get().login(account, pwd);
			VidUtil.fLog("HttpManager http logined;");
			long httpEnd = System.currentTimeMillis();
			VidUtil.fLog("LOGIN_TIME", "http login time lengh:" + (httpEnd - start) / 1000 + "秒");
			mXmppMgr.login(mCurUser.uid, pwd);
			long xmppEnd = System.currentTimeMillis();
			VidUtil.fLog("LOGIN_TIME", "xmpp login time lengh:" + (xmppEnd - httpEnd) / 1000 + "秒");
			VidUtil.fLog("LOGIN_TIME", "=========end(total time:" + (xmppEnd - start) / 1000 + "秒)=========");
			XmppListenerHolder.callListeners(OnConnectionListener.class,
					new IXmppListenerExecutor<OnConnectionListener>() {
						@Override
						public void execute(OnConnectionListener listener) {
							listener.onConnected();
						}
					});
		} catch (SmackException | XMPPException | IOException e) {
			throw new VidException(e);
		}
	}

	@Override
	public boolean addFriend(final String uid) {
		boolean success = mXmppMgr.addFriend(uid);
		XmppListenerHolder.callListeners(OnRosterListener.class, new IXmppListenerExecutor<OnRosterListener>() {
			@Override
			public void execute(OnRosterListener listener) {
				listener.entriesAdded(uid);
			}
		});
		return success;
	}

	@Override
	public boolean deleteFriend(String uid) {
		boolean success = mXmppMgr.deleteFriend(uid);
		MemCacheHolder.removeUser(uid);// don't forget!
		return success;
	}

	@Override
	public void logout() {
		try {
			mXmppMgr.logout();
			ThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						HttpManager.get().logout();
						mCurUser = null;
					} catch (Exception e) {
					}
				}
			});
		} catch (Exception e) {
		}
	}

	@Override
	public void setUserInfo(String... params) throws VidException {
		UserExt userExt = new UserExt();
		User user = new User();
		for (int i = 0; i < params.length; i += 2) {
			userExt.put(params[i], params[i + 1]);
			if ("nick".equals(params[i])) {
				user.nick = params[i + 1];
				mCurUser.nick = params[i + 1];
			} else if ("age".equals(params[i])) {
				user.age = Integer.parseInt(params[i + 1]);
				mCurUser.age = Integer.parseInt(params[i + 1]);
			} else if ("gender".equals(params[i])) {
				user.gender = params[i + 1].charAt(0);
				mCurUser.gender = params[i + 1].charAt(0);
			} else if ("address".equals(params[i])) {
				user.address = params[i + 1];
				mCurUser.address = params[i + 1];
			} else if ("signature".equals(params[i])) {
				user.signature = params[i + 1];
				mCurUser.signature = params[i + 1];
			} else if ("avatarUri".equals(params[i])) {
				user.avatarUri = params[i + 1];
				mCurUser.avatarUri = params[i + 1];
			} else if ("locSecret".equals(params[i])) {
				user.locSecret = params[i + 1].charAt(0);
				mCurUser.locSecret = params[i + 1].charAt(0);
			} else if ("avoidDisturb".equals(params[i])) {
				user.avoidDisturb = params[i + 1].charAt(0);
				mCurUser.avoidDisturb = params[i + 1].charAt(0);
			}
		}
		HttpManager.get().updateUser(user);
		try {
			mXmppMgr.pepMessage(userExt);
		} catch (NotConnectedException e) {
			throw new VidException(e);
		}
		UserInfoChangedListener.get().triggerOnUserInfoChangedListener(mCurUser.uid, userExt);
	}

	@Override
	public void launchRemoteRecord(String uid) throws NotConnectedException {
		CgCmdIQ cgCmdIq = new CgCmdIQ();
		cgCmdIq.remoteAudio = true;
		cgCmdIq.setTo(XmppUtil.buildFullJid(uid));
		cgCmdIq.setType(Type.set);
		mXmppMgr.sendStanza(cgCmdIq);
	}

}
