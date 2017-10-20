package com.vidmt.telephone.fragments;

import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.packet.Presence;
import org.jxmpp.util.XmppStringUtils;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.vidmt.acmn.utils.andr.FLog;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.DefaultThreadHandler;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.telephone.Const;
import com.vidmt.telephone.R;
import com.vidmt.telephone.activities.MainActivity;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.fragments.main.HomeFragController;
import com.vidmt.telephone.listeners.TabChangedListener;
import com.vidmt.telephone.listeners.TabChangedListener.OnTabChangedListener;
import com.vidmt.telephone.listeners.UserInfoChangedListener;
import com.vidmt.telephone.listeners.UserInfoChangedListener.OnUserInfoChangedListener;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.managers.HttpManager;
import com.vidmt.telephone.managers.LocationManager;
import com.vidmt.telephone.managers.LocationManager.AbsLocationListener;
import com.vidmt.telephone.managers.MapManager;
import com.vidmt.telephone.managers.MapManager.CustomView;
import com.vidmt.telephone.tasks.ServerConfInfoTask;
import com.vidmt.telephone.utils.GeoUtil;
import com.vidmt.telephone.utils.VidUtil;
import com.vidmt.telephone.vos.LocVo;
import com.vidmt.xmpp.enums.XmppEnums.Relationship;
import com.vidmt.xmpp.inner.XmppManager;
import com.vidmt.xmpp.listeners.OnRosterListener.AbsOnRosterListener;

/**
 * lihuichao
 * 主界面，主页
 */
public class HomeFragment extends Fragment implements OnClickListener, OnTabChangedListener {
	private LinearLayout mAvatarsLayout;
	private LocationManager mLocManager;
	private HomeFragController mController;
	private boolean mIsFirstLocate = true;
	private boolean mIsFirstGetFriendLocs = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.i("homeFrag", "onCreateView");
		View view = inflater.inflate(R.layout.frag_home, container, false);
		MapView mapView = (MapView) view.findViewById(R.id.mapview);
		MapManager.get().init(getActivity(), mapView);
		mAvatarsLayout = (LinearLayout) view.findViewById(R.id.avatars_layout);
		view.findViewById(R.id.locate_myself).setOnClickListener(this);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mLocManager = LocationManager.get();
		mLocManager.start();
		mController = HomeFragController.get();
		mController.init(getActivity());
		mLocManager.addListener(mLocationListener);
		TabChangedListener.get().setOnTabChangedListener(this);
		XmppManager.get().addXmppListener(mAbsOnRosterListener);
		UserInfoChangedListener.get().addOnUserInfoChangedListener(mOnUserInfoChangedListener);
		DefaultThreadHandler.post(mRunnable);
		locateSelfFirstTime();
	}

	private void locateSelfFirstTime() {
		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					if (mIsFirstLocate) {
						User curUser = AccManager.get().getCurUser();
						if (curUser != null) {// 可能service里注册位置监听先于登录成功
							final LocVo loc = HttpManager.get().getLocation(curUser.uid);
							VidUtil.fLog("locateSelfFirstTime", "loc : " + loc);
							if (loc != null) {
								VidUtil.fLog("locateSelfFirstTime", "loc is not null : " + loc);
								mLocManager.setCurLocation(loc.toLocation());
								mIsFirstLocate = false;
								MainThreadHandler.post(new Runnable() {
									@Override
									public void run() {
										if(AccManager.get().getCurUser() == null){
											VidUtil.fLog("locateSelfFirstTime", "AccManager.get().getCurUser() == null");
											return;
										}
										String myUid = AccManager.get().getCurUser().uid;
										MapManager.get().animateTo(myUid, loc.toLocation());
									}
								});
							}
						}
					}
				} catch (VidException e) {
					VLog.e("test", e);
				}
			}
		});
	}

	private AbsLocationListener mLocationListener = new AbsLocationListener() {
		@Override
		public void onLocationChanged(final Location loc) {
			if (mIsFirstLocate) {
				VidUtil.fLog("HomeFragment  onLocationChanged mIsFirstLocate : " + mIsFirstLocate);
				if(AccManager.get().getCurUser() == null){
					VidUtil.fLog("HomeFragment  onLocationChanged mIsFirstLocate :AccManager.get().getCurUser() == null ");
					return;
				}
				String myUid = AccManager.get().getCurUser().uid;
				MapManager.get().animateTo(myUid, loc);
				mIsFirstLocate = false;
			} else {
				VidUtil.fLog("HomeFragment  onLocationChanged mIsFirstLocate : " + mIsFirstLocate);
				MapManager.get().refreshMyView(loc, false);
			}
		}
	};

	private Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				if (!XmppManager.get().isAuthenticated()) {// 退出瞬间异常
					DefaultThreadHandler.post(this, Const.GET_FRIENDS_LOC_FREQUENCY);
					return;
				}
				List<LocVo> locs = null;
				if (mIsFirstGetFriendLocs) {// 首次下载所有好友位置
					List<String> friendUids = VidUtil.getAllFriendUids(false);
					VidUtil.removeLocSecretUids(friendUids);
					if (friendUids.size() > 0) {
						locs = HttpManager.get().getFriendLocs(friendUids);
					}
					mController.initMapAvatarIcons(mAvatarsLayout, friendUids);
					mIsFirstGetFriendLocs = false;
				} else {// 只下载在线好友位置
					//huawei change----from true to fase; iphone 后台后也可以定位；
					List<String> onlineUids = VidUtil.getAllFriendUids(false);
					VidUtil.removeLocSecretUids(onlineUids);
					if (onlineUids.size() > 0) {
						locs = HttpManager.get().getFriendLocs(onlineUids);
					}
				}
				if (locs != null) {
					for (LocVo loc : locs) {
						MapManager.get().refreshFriendView(loc.toLocation(), false);
					}
				}
			} catch (VidException e) {
				VLog.e("test", e);
			}
			DefaultThreadHandler.post(this, Const.GET_FRIENDS_LOC_FREQUENCY);
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// case R.id.efence:
		// Intent ei = new Intent(getActivity(), MapActivity.class);
		// ei.putExtra(ExtraConst.EXTRA_MAP_ADD_EFENCE, true);
		// // i.putExtra(ExtraConst.EXTRA_UID, value);
		// startActivity(ei);
		// break;
		case R.id.locate_myself:
			LatLng ll = mLocManager.getCurLocation();
			if (ll == null) {
				MainThreadHandler.makeToast(R.string.locating);
				return;
			}
			if(AccManager.get().getCurUser() == null){
				return;
			}
			String myUid = AccManager.get().getCurUser().uid;
			MapManager.get().animateTo(myUid, GeoUtil.latlng2Location(ll));
			mController.clearFocusedIcon(mAvatarsLayout);
			break;
		}
	}

	private OnUserInfoChangedListener mOnUserInfoChangedListener = new OnUserInfoChangedListener() {
		@Override
		public void onInfoChanged(final String uid, Map<String, String> map) {
			if (uid.equals(AccManager.get().getCurUser().uid)) {// 自己信息改变
				LatLng ll = mLocManager.getCurLocation();
				if (ll != null) {
					MapManager.get().refreshMyView(GeoUtil.latlng2Location(ll), true);
				}
			} else {// 好友信息改变
				if (map.get("locSecret") != null) {
					boolean locSecret = 'T' == map.get("locSecret").charAt(0);
					if (locSecret) {// "位置保密"
						MapManager.get().removePinView(uid);
						mController.removeMapAvatarIcon(mAvatarsLayout, uid);
					} else {
						ThreadPool.execute(new Runnable() {
							@Override
							public void run() {
								try {
									LocVo locVo = HttpManager.get().getLocation(uid);
									if (locVo == null) {
										MainThreadHandler.makeToast(R.string.friend_no_location);
										return;
									}
									final Location loc = locVo.toLocation();
									MainThreadHandler.post(new Runnable() {
										@Override
										public void run() {
											MapManager.get().refreshFriendView(loc, false);
										}
									});
								} catch (VidException e) {
									VLog.e("test", e);
								}
								try {
									final User user = HttpManager.get().getUserInfo(uid);
									MainThreadHandler.post(new Runnable() {
										@Override
										public void run() {
											mController.addMapAvatarIcon(mAvatarsLayout, uid, user.avatarUri);
										}
									});
								} catch (VidException e) {
									VLog.e("test", e);
								}
							}
						});
					}
				}
				CustomView view = MapManager.get().getViewByTag(uid);
				if (view == null) {
					return;
				}
				MapManager.get().refreshFriendView(view.loc, true);
				boolean isOnline = XmppManager.get().isUserOnline(uid);
				mController.refreshMapAvatarIcon(mAvatarsLayout, uid, isOnline);
			}
		}
	};

	private AbsOnRosterListener mAbsOnRosterListener = new AbsOnRosterListener() {
		@Override
		public void presenceChanged(Presence presence) {
			final String uid = XmppStringUtils.parseLocalpart(presence.getFrom());
			boolean online = presence.getType() == Presence.Type.available;
			if (!online) {// 下线
				CustomView view = MapManager.get().getViewByTag(uid);
				if(view != null){
					MapManager.get().refreshFriendView(view.loc, true);
				}
			} else {// 上线
				Relationship relation = XmppManager.get().getRelationship(uid);
				if (relation != Relationship.FRIEND) {
					return;
				}
				ThreadPool.execute(new Runnable() {
					@Override
					public void run() {
						try {
							User user = HttpManager.get().getUserInfo(uid);
							if (user.isLocSecret()) {
								return;
							}
							LocVo loc = HttpManager.get().getLocation(uid);
							if (loc != null) {
								MapManager.get().refreshFriendView(loc.toLocation(), true);
							}
						} catch (VidException e) {
							VLog.e("test", e);
						}
					}
				});
			}
			mController.refreshMapAvatarIcon(mAvatarsLayout, uid, online);
		}

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
						if (relation == Relationship.FRIEND || (ServerConfInfoTask.canDirectAddFriend()&&isSelfVip && !isSideVip)) {
							LocVo loc = HttpManager.get().getLocation(uid);
							if (!user.isLocSecret() && loc != null) {
								MapManager.get().refreshFriendView(loc.toLocation(), true);
							}
							MainThreadHandler.post(new Runnable() {
								@Override
								public void run() {
									mController.addMapAvatarIcon(mAvatarsLayout, uid, user.avatarUri);
								}
							});
						}
					} catch (VidException e) {
						VLog.e("test", e);
					}
				}
			});
		}

		@Override
		public void entriesDeleted(final String uid) {
			MainThreadHandler.post(new Runnable() {
				@Override
				public void run() {
					MapManager.get().removePinView(uid);
					mController.removeMapAvatarIcon(mAvatarsLayout, uid);
				}
			});
		};
	};

	@Override
	public void onTabChange(int index) {
		if (index == MainActivity.TAB_HOME_INDEX) {
		}
	}

	@Override
	public void onResume() {
		MapManager.get().onResume();
		super.onResume();
	}

	@Override
	public void onPause() {
		MapManager.get().onPause();
		super.onPause();
	}

	@Override
	public void onDestroyView() {
		Log.i("homeFrag", "onDestroyView");
		MapManager.get().onDestroy();
		mLocManager.removeListener(mLocationListener);
		mLocManager = null;
		TabChangedListener.get().removeOnTabChangedListener(this);
		XmppManager.get().removeXmppListener(mAbsOnRosterListener);
		DefaultThreadHandler.remove(mRunnable);
		UserInfoChangedListener.get().removeOnUserInfoChangedListener(mOnUserInfoChangedListener);
		mIsFirstLocate = true;
		mIsFirstGetFriendLocs = true;
		super.onDestroy();
	}

}
