package com.vidmt.telephone.fragments;

import java.util.Date;
import java.util.List;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnItemClick;
import com.lidroid.xutils.view.annotation.event.OnItemLongClick;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.activities.ChattingActivity;
import com.vidmt.telephone.activities.FriendsRequestActivity;
import com.vidmt.telephone.activities.MainActivity;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.listeners.ChatStatusListener;
import com.vidmt.telephone.listeners.ChatStatusListener.OnChatStatusListener;
import com.vidmt.telephone.listeners.TabChangedListener.OnTabChangedListener;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.managers.HttpManager;
import com.vidmt.telephone.ui.adapters.MsgListAdapter;
import com.vidmt.telephone.ui.adapters.MsgListAdapter.ViewHolder;
import com.vidmt.telephone.ui.views.MsgPopView;
import com.vidmt.telephone.utils.DateUtil;
import com.vidmt.telephone.utils.VidUtil;
import com.vidmt.xmpp.inner.XmppManager;
import com.vidmt.xmpp.listeners.OnMsgReceivedListener;
import com.vidmt.xmpp.listeners.OnRelChangedListener.AbsOnRelChangedListener;
import com.vidmt.xmpp.listeners.OnRosterListener.AbsOnRosterListener;

/**
 * lihuichao
 * 主界面，消息，消息选项
 */
public class MsgFragment extends Fragment implements OnTabChangedListener {
	@ViewInject(R.id.msg_list)
	private ListView mListView;
	@ViewInject(R.id.empty_notify)
	private TextView mEmptyNotifyTv;
	@ViewInject(R.id.msg_loading)
	private View mLoadingView;
	private TextView mNewFriendsMsgTv;
	private TextView mNewFriendsTimeTv;
	private User mHeaderReqUser;
	private MsgListAdapter mAdapter;
	private boolean mIsPaused;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.frag_msgs, container, false);
		ViewUtils.inject(this, view);
		initHeaderView();
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		ChatStatusListener.get().addOnChatStatusListener(mOnChatStatusListener);

		XmppManager.get().addXmppListener(mAbsOnRosterListener);
		XmppManager.get().addXmppListener(mOnMsgReceivedListener);
		XmppManager.get().addXmppListener(mAbsOnRelChangedListener);
		refreshData();
	}

	@Override
	public void onResume() {
		super.onResume();
		mIsPaused = false;
		refreshData();
	}

	@Override
	public void onPause() {
		super.onPause();
		mIsPaused = true;
	}

	private void initHeaderView() {
		View header = SysUtil.inflate(R.layout.msg_header_view);
		mNewFriendsMsgTv = (TextView) header.findViewById(R.id.msg);
		mNewFriendsTimeTv = (TextView) header.findViewById(R.id.time);
		mListView.addHeaderView(header);
		header.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), FriendsRequestActivity.class));
			}
		});
		reloadHeaderViewData();
	}

	private void reloadHeaderViewData() {
		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					List<User> users = HttpManager.get().getRequestUsers(true);
					if (users != null && users.size() > 0) {
						mHeaderReqUser = users.get(0);
						MainThreadHandler.post(new Runnable() {
							@Override
							public void run() {
								mNewFriendsMsgTv.setText(getString(R.string.request_to_be_friends,
										mHeaderReqUser.getNick()));
								mNewFriendsTimeTv.setText("");
							}
						});
					} else {
						MainThreadHandler.post(new Runnable() {
							@Override
							public void run() {
								mNewFriendsMsgTv.setText(R.string.no_friends_request);
								mNewFriendsTimeTv.setText("");
							}
						});
					}
				} catch (VidException e) {
					VLog.e("test", e);
				}
			}
		});
	}

	private AbsOnRelChangedListener mAbsOnRelChangedListener = new AbsOnRelChangedListener() {
		@Override
		public void beRequested(String uid) {
			try {
				mHeaderReqUser = HttpManager.get().getUserInfo(uid);
				if (mHeaderReqUser.isVip() && !AccManager.get().getCurUser().isVip()) {
					return;
				}
				MainThreadHandler.post(new Runnable() {
					@Override
					public void run() {
						mNewFriendsMsgTv.setText(getString(R.string.new_friends, mHeaderReqUser.getNick()));
						mNewFriendsTimeTv.setText(DateUtil.getMsgListTimeStr(new Date().getTime()));
					}
				});
			} catch (VidException e) {
				VLog.e("test", e);
			}
		}
	};

	private OnChatStatusListener mOnChatStatusListener = new OnChatStatusListener() {
		@Override
		public void onChat(String withWhom, boolean isStartNotEnd) {
			if (isStartNotEnd && mAdapter != null) {
				mAdapter.clearUnReadNum(withWhom);
			}
		}
	};

	private AbsOnRosterListener mAbsOnRosterListener = new AbsOnRosterListener() {
		@Override
		public void entriesAdded(String uid) {
			reloadHeaderViewData();
		}

		@Override
		public void entriesDeleted(String uid) {
			VidUtil.removeFromMsgList(uid);
			refreshData();
			ChatStatusListener.get().triggerOnChatStatusListener(uid, false);
			reloadHeaderViewData();
		}
	};

	private OnMsgReceivedListener mOnMsgReceivedListener = new OnMsgReceivedListener() {
		@Override
		public void onMsgReceived(Chat chat, final Message msg) {
			if (!mIsPaused) {// 在当前页面活动中
				refreshData();
			}
		}
	};

	public void refreshData() {
		final List<String> msgUids = VidUtil.getMsgUidList();
		if (msgUids.size() == 0) {
			handleEmptyNotify(true);
			MainThreadHandler.post(new Runnable() {
				@Override
				public void run() {
					hideLoadingView();
				}
			});
			return;
		}
		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					List<User> msgUsers = HttpManager.get().getMultUser(msgUids);
					if (msgUsers == null || msgUsers.size() == 0) {
						handleEmptyNotify(true);
					} else {
						handleEmptyNotify(false);
					}
					mAdapter = new MsgListAdapter(getActivity(), msgUsers);
					MainThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							mListView.setAdapter(mAdapter);
							hideLoadingView();
						}
					});
				} catch (VidException e) {
					VLog.e("test", e);
					hideLoadingView();
				}
			}
		});
	}

	private void hideLoadingView() {
		MainThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				mLoadingView.setVisibility(View.GONE);
			}
		});
	}

	@OnItemClick(R.id.msg_list)
	private void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (view.getTag() == null) {
			return;
		}
		Intent intent = new Intent(getActivity(), ChattingActivity.class);
		ViewHolder holder = (ViewHolder) view.getTag();
		String friendUid = holder.user.uid;
		intent.putExtra(ExtraConst.EXTRA_UID, friendUid);
		startActivity(intent);
	}

	@OnItemLongClick(R.id.msg_list)
	private boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (view.getTag() == null) {
			return true;
		}
		ViewHolder holder = (ViewHolder) view.getTag();
		String friendUid = holder.user.uid;
		new MsgPopView(getActivity(), view, friendUid, this).show();
		return true;
	}

	private void handleEmptyNotify(final boolean show) {
		MainThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				if (show) {
					mAdapter = new MsgListAdapter(getActivity(), null);
					mListView.setAdapter(mAdapter);
					mEmptyNotifyTv.setVisibility(View.VISIBLE);
				} else {
					mEmptyNotifyTv.setVisibility(View.GONE);
				}
			}
		});
	}

	@Override
	public void onTabChange(int index) {
		if (index == MainActivity.TAB_MSG_FRIEND_INDEX) {
		}
	}

	@Override
	public void onDestroy() {
		XmppManager.get().removeXmppListener(mAbsOnRosterListener);
		XmppManager.get().removeXmppListener(mOnMsgReceivedListener);
		XmppManager.get().removeXmppListener(mAbsOnRelChangedListener);
		ChatStatusListener.get().removeOnChatStatusListener(mOnChatStatusListener);
		if (mAdapter != null) {// 可能还未跳转过此页面
			mAdapter.removeXmppListeners();
		}
		super.onDestroy();
	}

}
