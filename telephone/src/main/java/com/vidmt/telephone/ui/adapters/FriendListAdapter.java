package com.vidmt.telephone.ui.adapters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.packet.Presence;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.telephone.R;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.utils.VidUtil;
import com.vidmt.xmpp.inner.XmppManager;
import com.vidmt.xmpp.listeners.OnRosterListener.AbsOnRosterListener;

public class FriendListAdapter extends BaseAdapter {
	private Activity mCtx;
	private List<User> mAllUser;
	private Map<Integer, ViewHolder> mHolderMap = new HashMap<Integer, ViewHolder>();

	public FriendListAdapter(Activity act, Collection<User> friends) {
		mCtx = act;
		mAllUser = getOrderedUserList(friends);
		XmppManager.get().addXmppListener(mAbsOnRosterListener);
	}

	@Override
	public int getCount() {
		return mAllUser.size();
	}

	@Override
	public Object getItem(int position) {
		return mAllUser.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = SysUtil.inflate(R.layout.friend_list_item);
			holder.avatarLayout = (LinearLayout) convertView.findViewById(R.id.avatar_layout);
			holder.nickTv = (TextView) convertView.findViewById(R.id.nick);
			holder.statusTv = (TextView) convertView.findViewById(R.id.status);
			holder.signatureTv = (TextView) convertView.findViewById(R.id.signature);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.user = (User) getItem(position);
		ImageView avatarIv = new ImageView(mCtx);
		LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		avatarIv.setLayoutParams(params);
		if (holder.avatarLayout.getChildCount() > 0) {// 防止异步加载导致图片错乱
			holder.avatarLayout.removeAllViews();
		}
		holder.avatarLayout.addView(avatarIv);
		mHolderMap.put(position, holder);
		holder.nickTv.setText(holder.user.getNick());
		boolean online = XmppManager.get().isUserOnline(holder.user.uid);
		if (online) {
			holder.statusTv.setText(R.string.online);
		} else {
			holder.statusTv.setText(R.string.offline);
		}
		VidUtil.asyncCacheAndDisplayAvatar(avatarIv, holder.user.avatarUri, online);
		String signature = holder.user.signature;
		holder.signatureTv.setText(signature == null ? "" : signature);
		return convertView;
	}

	public class ViewHolder {
		public User user;
		public LinearLayout avatarLayout;
		public TextView nickTv;
		public TextView statusTv;
		public TextView signatureTv;
	}

	private List<User> getOrderedUserList(Collection<User> friends) {
		List<User> list = new ArrayList<User>();
		Iterator<User> it = friends.iterator();
		while (it.hasNext()) {
			User user = it.next();
			boolean online = XmppManager.get().isUserOnline(user.uid);
			if (online) {
				list.add(0, user);// 在线的排在前面
			} else {
				list.add(user);
			}
		}
		return list;
	}

	private AbsOnRosterListener mAbsOnRosterListener = new AbsOnRosterListener() {
		@Override
		public void presenceChanged(Presence presence) {
			mAllUser = getOrderedUserList(mAllUser);
			MainThreadHandler.post(new Runnable() {
				@Override
				public void run() {
					notifyDataSetChanged();
				}
			});
		}
	};

	public void removeXmppListeners() {
		XmppManager.get().removeXmppListener(mAbsOnRosterListener);
	}
}
