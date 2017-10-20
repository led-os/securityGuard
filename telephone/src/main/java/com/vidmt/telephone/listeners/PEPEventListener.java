package com.vidmt.telephone.listeners;

import org.jivesoftware.smack.packet.ExtensionElement;

import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.telephone.cache.MemCacheHolder;
import com.vidmt.telephone.entities.User;
import com.vidmt.xmpp.exts.UserExt;
import com.vidmt.xmpp.listeners.OnPEPEventListener;

public class PEPEventListener implements OnPEPEventListener {
	private static PEPEventListener sInstance;

	public static PEPEventListener get() {
		if (sInstance == null) {
			sInstance = new PEPEventListener();
		}
		return sInstance;
	}

	private PEPEventListener() {
	}

	@Override
	public void eventReceived(final String fromUid, ExtensionElement extElement) {
		if (extElement instanceof UserExt) {
			final UserExt userExt = (UserExt) extElement;
			User user = MemCacheHolder.getUser(fromUid);
			if (user == null) {
				user = new User();
			}
			for (String key : userExt.keySet()) {
				String value = userExt.get(key);
				if ("nick".equals(key)) {
					user.nick = value;
				} else if ("age".equals(key)) {
					user.age = Integer.parseInt(value);
				} else if ("gender".equals(key)) {
					user.gender = value.charAt(0);
				} else if ("address".equals(key)) {
					user.address = value;
				} else if ("signature".equals(key)) {
					user.signature = value;
				} else if ("avatarUri".equals(key)) {
					user.avatarUri = value;
				} else if ("locSecret".equals(key)) {
					user.locSecret = value.charAt(0);
				} else if ("avoidDisturb".equals(key)) {
					user.avoidDisturb = value.charAt(0);
				}
			}
			MainThreadHandler.post(new Runnable() {
				@Override
				public void run() {
					UserInfoChangedListener.get().triggerOnUserInfoChangedListener(fromUid, userExt);
				}
			});
			MemCacheHolder.updateUser(user);
		}
	}

}
