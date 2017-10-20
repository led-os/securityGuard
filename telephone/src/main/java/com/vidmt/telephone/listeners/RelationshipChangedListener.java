package com.vidmt.telephone.listeners;

import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.managers.HttpManager;
import com.vidmt.telephone.tasks.ServerConfInfoTask;
import com.vidmt.telephone.utils.VidUtil;
import com.vidmt.xmpp.listeners.OnRelChangedListener;

public class RelationshipChangedListener implements OnRelChangedListener {
	private static RelationshipChangedListener sInstance;

	public static RelationshipChangedListener get() {
		if (sInstance == null) {
			sInstance = new RelationshipChangedListener();
		}
		return sInstance;
	}

	private RelationshipChangedListener() {
	}

	@Override
	public void beRequested(final String uid) {
		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				User curUser = AccManager.get().getCurUser();
				if (curUser.isAvoidDisturb()) {// 防骚扰
					AccManager.get().deleteFriend(uid);
					return;
				}
				try {
					if (ServerConfInfoTask.canDirectAddFriend()) {
						User user = HttpManager.get().getUserInfo(uid);
						boolean isSelfVip = curUser.isVip();
						boolean isSideVip = user != null && user.isVip();
						if (isSideVip && !isSelfVip) {// 对方是vip，我不是vip(直接同意)
							AccManager.get().addFriend(uid);
							return;
						}
					}
					VidUtil.notifyFriendRequest(uid);
				} catch (VidException e) {
					VLog.e("test", e);
				}
			}
		});
	}

	@Override
	public void beAgreed(String uid) {
	}

	@Override
	public void beRefused(String uid) {
	}

	@Override
	public void beDeleted(String uid) {
	}

}
