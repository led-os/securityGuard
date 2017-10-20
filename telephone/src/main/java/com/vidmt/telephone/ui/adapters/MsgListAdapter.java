package com.vidmt.telephone.ui.adapters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jxmpp.util.XmppStringUtils;

import android.app.Activity;
import android.database.DataSetObserver;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vidmt.acmn.utils.andr.FLog;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.telephone.Const;
import com.vidmt.telephone.PrefKeyConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.entities.ChatRecord;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.listeners.ChatStatusListener;
import com.vidmt.telephone.managers.HttpManager;
import com.vidmt.telephone.utils.DBUtil;
import com.vidmt.telephone.utils.DateUtil;
import com.vidmt.telephone.utils.VXmppUtil;
import com.vidmt.telephone.utils.VidUtil;
import com.vidmt.telephone.vos.MsgVo;
import com.vidmt.xmpp.inner.XmppManager;
import com.vidmt.xmpp.listeners.OnMsgReceivedListener;

public class MsgListAdapter extends BaseAdapter {
	private Activity mCtx;
	private List<User> mMsgUsers;
	private Map<Integer, ViewHolder> mHolderMap = new HashMap<Integer, ViewHolder>();

	public MsgListAdapter(Activity act, List<User> msgUsers) {
		mCtx = act;
		mMsgUsers = getOrderedUserList(msgUsers);
		XmppManager.get().addXmppListener(mOnMsgReceivedListener);
	}

	@Override
	public int getCount() {
		return mMsgUsers.size();
	}

	@Override
	public Object getItem(int position) {
		return mMsgUsers.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		super.registerDataSetObserver(observer);
	}
	
	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		super.unregisterDataSetObserver(observer);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = SysUtil.inflate(R.layout.msg_list_item);
			holder.avatarLayout = (LinearLayout) convertView.findViewById(R.id.avatar_layout);
			holder.nickTv = (TextView) convertView.findViewById(R.id.nick);
			holder.MsgTv = (TextView) convertView.findViewById(R.id.msg);
			holder.timtTv = (TextView) convertView.findViewById(R.id.time);
			holder.unReadNumTv = (TextView) convertView.findViewById(R.id.unread_num);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		ImageView avatarIv = new ImageView(mCtx);
		LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		avatarIv.setLayoutParams(params);
		if (holder.avatarLayout.getChildCount() > 0) {// 防止异步加载导致图片错乱
			holder.avatarLayout.removeAllViews();
		}
		holder.avatarLayout.addView(avatarIv);
		User user = (User) getItem(position);
		holder.user = user;
		mHolderMap.put(position, holder);
		holder.nickTv.setText(user.getNick());
		//huawei add;
		String kefu_uid =  SysUtil.getPref(PrefKeyConst.PREF_KEFU_ACCOUNT, Const.DEF_KEFU_UID);
		if(user.uid.equals(kefu_uid)){
			avatarIv.setImageResource(R.drawable.default_kefu_avatar);
		}else{
			VidUtil.asyncCacheAndDisplayAvatar(avatarIv, user.avatarUri, true);
		}
		MsgVo msgVo = user.msgVo;
		holder.MsgTv.setText(msgVo.msgTxt);
		holder.timtTv.setText(DateUtil.getMsgListTimeStr(msgVo.time));
		int unReadNum = DBUtil.getUnReadNum(user.uid);
		if (unReadNum == 0) {
			holder.unReadNumTv.setVisibility(View.INVISIBLE);
		} else {
			holder.unReadNumTv.setText(unReadNum + "");
			holder.unReadNumTv.setVisibility(View.VISIBLE);
		}
		return convertView;
	}

	public class ViewHolder {
		public User user;
		public LinearLayout avatarLayout;
		public TextView nickTv;
		public TextView MsgTv;
		public TextView timtTv;
		public TextView unReadNumTv;
	}

	public void clearUnReadNum(final String uid) {
		for (int p : mHolderMap.keySet()) {
			ViewHolder holder = mHolderMap.get(p);
			if (uid.equals(holder.user.uid)) {
				holder.unReadNumTv.setVisibility(View.INVISIBLE);
				holder.unReadNumTv.setText("0");
				ThreadPool.execute(new Runnable() {
					@Override
					public void run() {
						DBUtil.setAllChatRead(uid);
					}
				});
				break;
			}
		}
	}

	private List<User> getOrderedUserList(List<User> msgUsers) {
		List<User> users = new ArrayList<User>();
		if (msgUsers == null) {
			return users;
		}
		for (User user : msgUsers) {
			String uid = user.uid;
			ChatRecord chatRecord = DBUtil.getLastChatRecord(uid);
			if (chatRecord == null) {
				continue;
			}
			if (chatRecord.getType() == null) {
				VidUtil.fLog("MsgListAdapter", chatRecord.toString());
//				ChatRecord [suid=13466609943, fuid=null, isSelf=false, isRead=false, type=null, data=null, during=0, sayTime=0]
				continue;
			}
			long time = chatRecord.getSayTime();
			String msgTxt = VidUtil.getNotifyTxt(chatRecord);
			int unReadNum = DBUtil.getUnReadNum(uid);
			MsgVo msgVo = new MsgVo(time, msgTxt, unReadNum);
			user.msgVo = msgVo;
			users.add(user);
		}
		User[] userArr = new User[users.size()];
		users.toArray(userArr);
		Arrays.sort(userArr, new Comparator<User>() {// 时间先后顺序
			@Override
			public int compare(User u1, User u2) {
				long t1 = u1.msgVo.time;
				long t2 = u2.msgVo.time;
				if (t1 > t2) {
					return -1;
				} else if (t1 < t2) {
					return 1;
				}
				return 0;
			}
		});
		return Arrays.asList(userArr);
	}

	/**
	 * 如果列表中没有消息条，则添加一条
	 */
	private boolean checkIfUserIsShownOnMsgList(String uid) {
		Iterator<ViewHolder> it = mHolderMap.values().iterator();
		while (it.hasNext()) {
			ViewHolder holder = it.next();
			if (holder.user.uid.equals(uid)) {// 列表中有
				return true;
			}
		}
		final List<String> msgUids = VidUtil.getMsgUidList();
		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					List<User> users = HttpManager.get().getMultUser(msgUids);
					if (users != null) {
						mMsgUsers = getOrderedUserList(users);
						MainThreadHandler.post(new Runnable() {
							@Override
							public void run() {
								notifyDataSetChanged();
							}
						});
					}
				} catch (VidException e) {
					VLog.e("test", e);
				}
			}
		});
		return false;
	}

	private OnMsgReceivedListener mOnMsgReceivedListener = new OnMsgReceivedListener() {
		@Override
		public void onMsgReceived(Chat chat, final Message msg) {
			if (msg.getType() == Message.Type.error || msg.getExtensions().size() == 0
					&& TextUtils.isEmpty(msg.getBody())) {
				return;
			}
			String uid = XmppStringUtils.parseLocalpart(chat.getParticipant());
			boolean isShown = checkIfUserIsShownOnMsgList(uid);
			if (!isShown) {
				return;
			}
			if (uid.equals(ChatStatusListener.get().getCurChatUser())) {// 正在聊天中
				return;
			}
			for (int p : mHolderMap.keySet()) {
				final ViewHolder holder = mHolderMap.get(p);
				if (uid.equals(holder.user.uid)) {
					int unReadNum = 0;
					String unReadTxt = holder.unReadNumTv.getText().toString();
					if (!TextUtils.isEmpty(unReadTxt)) {
						unReadNum = Integer.parseInt(holder.unReadNumTv.getText().toString());
					}
					final String unReadNumTxt = ++unReadNum + "";
					MainThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							holder.unReadNumTv.setVisibility(View.VISIBLE);
							holder.unReadNumTv.setText(unReadNumTxt);
							long time = System.currentTimeMillis();
							String msgTxt = VidUtil.getNotifyTxt(VXmppUtil.parseChatMessage(msg));
							MsgVo msgVo = new MsgVo(time, msgTxt, Integer.parseInt(unReadNumTxt));
							holder.user.msgVo = msgVo;
							holder.timtTv.setText(DateUtil.getMsgListTimeStr(time));
							holder.MsgTv.setText(msgTxt);
							holder.unReadNumTv.setText(unReadNumTxt);
						}
					});
					break;
				}
			}
		}
	};

	public void removeXmppListeners() {
		XmppManager.get().removeXmppListener(mOnMsgReceivedListener);
	}
}
