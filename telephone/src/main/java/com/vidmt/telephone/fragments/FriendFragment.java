package com.vidmt.telephone.fragments;

import java.util.List;

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
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.DefaultThreadHandler;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.activities.ChattingActivity;
import com.vidmt.telephone.activities.MainActivity;
import com.vidmt.telephone.cache.MemCacheHolder;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.listeners.TabChangedListener.OnTabChangedListener;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.managers.HttpManager;
import com.vidmt.telephone.ui.adapters.FriendListAdapter;
import com.vidmt.telephone.ui.adapters.FriendListAdapter.ViewHolder;
import com.vidmt.telephone.ui.views.FriendPopView;
import com.vidmt.telephone.utils.VidUtil;
import com.vidmt.xmpp.enums.XmppEnums.Relationship;
import com.vidmt.xmpp.inner.XmppManager;
import com.vidmt.xmpp.listeners.OnRosterListener.AbsOnRosterListener;

/**
 * lihuichao
 * 主界面，消息，好友选项
 */
public class FriendFragment extends Fragment implements OnTabChangedListener {
	@ViewInject(R.id.frinds_list)
	private ListView mListView;
	@ViewInject(R.id.empty_notify)
	private TextView mEmptyNotifyTv;
	@ViewInject(R.id.frifrag_loading)
	private View mLoadingView;
	private FriendListAdapter mAdapter;
	private boolean mIsFirstTime = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.frag_friends, container, false);
		ViewUtils.inject(this, view);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initData();

		XmppManager.get().addXmppListener(mAbsOnRosterListener);
	}

	private void initData() {
		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				List<String> uids = VidUtil.getAllFriendUids(false);
				if (uids.size() == 0) {
					handleEmptyNotify(true);
					hideLoadingView();
					return;
				}
				try {
					List<User> allUsers = HttpManager.get().getMultUser(uids);
					if (allUsers == null || allUsers.size() == 0) {
						handleEmptyNotify(true);
						hideLoadingView();
						return;
					}
					handleEmptyNotify(false);
					if (mIsFirstTime) {
						MemCacheHolder.setUserCache(allUsers);// 缓存所有好友信息
						mIsFirstTime = false;
					}
					mAdapter = new FriendListAdapter(getActivity(), allUsers);
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

	private AbsOnRosterListener mAbsOnRosterListener = new AbsOnRosterListener() {
		@Override
		public void entriesAdded(final String uid) {
			ThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						final boolean isSelfVip = AccManager.get().getCurUser().isVip();
						final User user = HttpManager.get().getUserInfo(uid);
						final boolean isSideVip = user == null ? false : user.isVip();
						Relationship relation = XmppManager.get().getRelationship(uid);
						if (relation == Relationship.FRIEND || relation == Relationship.WAIT_BE_AGREE && isSelfVip
								&& !isSideVip) {
							DefaultThreadHandler.post(new Runnable() {
								@Override
								public void run() {
									initData();
								}
							}, 1 * 1000);
						}
					} catch (VidException e) {
						VLog.e("test", e);
					}
				}
			});
		}

		@Override
		public void entriesDeleted(String uid) {
			initData();
		}
	};

	@OnItemClick(R.id.frinds_list)
	private void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(getActivity(), ChattingActivity.class);
		ViewHolder holder = (ViewHolder) view.getTag();
		String friendUid = holder.user.uid;
		intent.putExtra(ExtraConst.EXTRA_UID, friendUid);
		startActivity(intent);
	}

	@OnItemLongClick(R.id.frinds_list)
	private boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		ViewHolder holder = (ViewHolder) view.getTag();
		String friendUid = holder.user.uid;
		new FriendPopView(getActivity(), view, friendUid, this).show();
		return true;
	}

	private void hideLoadingView() {
		MainThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				mLoadingView.setVisibility(View.GONE);
			}
		});
	}

	private void handleEmptyNotify(final boolean show) {
		MainThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				if (show) {
					mListView.setAdapter(null);
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
		super.onDestroy();
	}

}
