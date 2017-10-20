package com.vidmt.telephone.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.vidmt.telephone.entities.User;
import com.vidmt.xmpp.enums.XmppEnums.Relationship;
import com.vidmt.xmpp.inner.XmppManager;

public class MemCacheHolder {
	private static Map<String, User> sUserCache;

	public static void setUserCache(Collection<User> users) {
		if (users == null) {
			return;
		}
		sUserCache = Collections.synchronizedMap(new HashMap<String, User>(users.size()));
		for (User user : users) {
			// 只缓存“friend关系”的朋友，否则导致资料不更新
			if (XmppManager.get().getRelationship(user.uid) == Relationship.FRIEND) {
				sUserCache.put(user.uid, user);
			}
		}
	}

	public static Collection<User> getAllUser() {
		if (sUserCache == null || sUserCache.size() == 0) {
			return null;
		}
		return Collections.unmodifiableCollection(sUserCache.values());
	}

	public static void updateUser(User user) {
		if (sUserCache == null) {
			sUserCache = Collections.synchronizedMap(new HashMap<String, User>());
		}
		if (XmppManager.get().getRelationship(user.uid) == Relationship.FRIEND) {
			sUserCache.put(user.uid, user);
		}
	}

	public static void removeUser(String uid) {
		sUserCache.remove(uid);
	}

	public static User getUser(String uid) {
		if (sUserCache == null) {
			return null;
		}
		return sUserCache.get(uid);
	}

	public static void destroy() {
		sUserCache.clear();
		sUserCache = null;
	}
}
