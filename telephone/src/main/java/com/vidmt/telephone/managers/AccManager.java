package com.vidmt.telephone.managers;

import java.util.Map;

import org.jivesoftware.smack.SmackException.NotConnectedException;

import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.managers.inner.InnerAccManagerImpl;

public class AccManager {
	private static IAccManager sInstance;

	public static IAccManager get() {
		if (sInstance == null) {
			sInstance = new InnerAccManagerImpl();
		}
		return sInstance;
	}

	public static interface IAccManager {
		public void setCurUser(User user);
		public User getCurUser();
		public void login(String account, String pwd) throws VidException;
		public void logout();
		public void setUserInfo(String... params) throws VidException;
		public void launchRemoteRecord(String uid) throws NotConnectedException;
		public boolean addFriend(String uid);
		public boolean deleteFriend(String uid);
	}

}
