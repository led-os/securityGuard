package com.vidmt.telephone.fragments.main;

import java.util.List;

import android.app.Activity;
import android.location.Location;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.baidu.mapapi.map.MapView;
import com.vidmt.acmn.utils.andr.PixUtil;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.telephone.R;
import com.vidmt.telephone.dlgs.CallOnLineDlg;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.managers.HttpManager;
import com.vidmt.telephone.managers.MapManager;
import com.vidmt.telephone.managers.MapManager.CustomView;
import com.vidmt.telephone.utils.VidUtil;
import com.vidmt.telephone.vos.LocVo;
import com.vidmt.xmpp.inner.XmppManager;

public class HomeFragController {
	private static HomeFragController sInstance;
	private Activity mActivity;

	public static HomeFragController get() {
		if (sInstance == null) {
			sInstance = new HomeFragController();
		}
		return sInstance;
	}

	public void init(Activity act) {
		mActivity = act;
	}

	private String mLastClickedUid;
	private boolean mIsMapAvatarIconsShown = true;

	public void initMapAvatarIcons(final LinearLayout avatarsLayout, final List<String> allUids) {
		if (mActivity == null) {// 防止时序问题
			return;
		}
		MainThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				final ScrollView sv = (ScrollView) mActivity.findViewById(R.id.fiends_sv);
				int topHeight = mActivity.findViewById(R.id.top_space).getHeight();
				int bottomHeight = mActivity.findViewById(R.id.bottom_space).getHeight();
				MapView mapView = MapManager.get().getMapView();
				int mapHeight = mapView.getHeight();
				final Button btn = (Button) mActivity.findViewById(R.id.hide_friends);
				int btnHeight = btn.getHeight();
				int height = mapHeight - topHeight - bottomHeight - btnHeight;
				FrameLayout.LayoutParams rp = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, height);
				RelativeLayout rl = (RelativeLayout) mActivity.findViewById(R.id.fiends_rl);
				rl.setLayoutParams(rp);
				if (mIsMapAvatarIconsShown) {
					btn.setBackgroundResource(R.drawable.arrow_up);
					sv.setVisibility(View.VISIBLE);
				} else {
					btn.setBackgroundResource(R.drawable.arrow_down);
					sv.setVisibility(View.GONE);
				}
				btn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (sv.getVisibility() == View.VISIBLE) {
							btn.setBackgroundResource(R.drawable.arrow_down);
							sv.setVisibility(View.GONE);
							mIsMapAvatarIconsShown = false;
						} else {
							btn.setBackgroundResource(R.drawable.arrow_up);
							sv.setVisibility(View.VISIBLE);
							mIsMapAvatarIconsShown = true;
						}
					}
				});
			}
		});
		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				for (final String uid : allUids) {
					try {
						final User user = HttpManager.get().getUserInfo(uid);
						MainThreadHandler.post(new Runnable() {
							@Override
							public void run() {
								final ImageView avatarIv = addMapAvatarIcon(avatarsLayout, uid, user.avatarUri);
								if (avatarIv == null) {
									return;
								}
								if (uid.equals(mLastClickedUid)) {
									avatarIv.setBackgroundResource(R.drawable.shap_circle_blue);
									animateTo(uid);
								}
							}
						});
					} catch (VidException e) {
						VLog.e("test", e);
					}
				}// end for
				MainThreadHandler.post(new Runnable() {
					@Override
					public void run() {
						mActivity.findViewById(R.id.loading_friends).setVisibility(View.GONE);
					}
				});
			}
		});
	}

	public void refreshMapAvatarIcon(LinearLayout avatarsLayout, final String uid, final boolean online) {
		for (int i = 0; i < avatarsLayout.getChildCount(); i++) {
			final View childView = avatarsLayout.getChildAt(i);
			if (childView.getTag().equals(uid)) {
				ThreadPool.execute(new Runnable() {
					@Override
					public void run() {
						try {
							final User user = HttpManager.get().getUserInfo(uid);
							MainThreadHandler.post(new Runnable() {
								@Override
								public void run() {
									ImageView iv = (ImageView) childView;
									VidUtil.asyncCacheAndDisplayAvatar(iv, user.avatarUri, online);
								}
							});
						} catch (VidException e) {
							VLog.e("test", e);
						}
					}
				});
				break;
			}
		}
	}

	public void removeMapAvatarIcon(LinearLayout avatarsLayout, final String uid) {
		Integer willRemoveIndex = null;
		for (int i = 0; i < avatarsLayout.getChildCount(); i++) {
			final View childView = avatarsLayout.getChildAt(i);
			if (childView.getTag().equals(uid)) {
				willRemoveIndex = i;
				break;
			}
		}
		if (willRemoveIndex != null) {
			avatarsLayout.removeViewAt(willRemoveIndex);
		}
	}

	public ImageView addMapAvatarIcon(final LinearLayout avatarsLayout, final String uid, String avatarUri) {
		for (int i = 0; i < avatarsLayout.getChildCount(); i++) {
			ImageView iv = (ImageView) avatarsLayout.getChildAt(i);
			if (iv.getTag().equals(uid)) {// 已有
				return null;
			}
		}
		int size = PixUtil.dp2px(40);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
		final ImageView avatarIv = new ImageView(mActivity);
		int margin = PixUtil.dp2px(2);
		params.setMargins(margin, margin, margin, margin);
		avatarIv.setLayoutParams(params);
		avatarIv.setTag(uid);
		boolean online = XmppManager.get().isUserOnline(uid);
		avatarIv.setImageResource(R.drawable.common_loading);
		VidUtil.asyncCacheAndDisplayAvatar(avatarIv, avatarUri, online);
		int padding = PixUtil.dp2px(3);
		avatarIv.setPadding(padding, padding, padding, padding);
		avatarsLayout.addView(avatarIv);

		avatarIv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				avatarIv.setBackgroundResource(R.drawable.shap_circle_blue);
				animateTo(uid);
				if (mLastClickedUid != null && !uid.equals(mLastClickedUid)) {
					clearFocusedIcon(avatarsLayout);
				}
				mLastClickedUid = uid;
			}
		});
		avatarIv.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				boolean online = XmppManager.get().isUserOnline(uid);
				//仅通知离线好友上线；
				if(online == false){
					new CallOnLineDlg(mActivity, R.string.confirm, R.string.call_friend_online, uid).show();
				}
				return true;
			}
		});
		return avatarIv;
	}

	public void clearFocusedIcon(LinearLayout avatarsLayout) {
		for (int i = 0; i < avatarsLayout.getChildCount(); i++) {
			ImageView iv = (ImageView) avatarsLayout.getChildAt(i);
			if (iv.getTag().equals(mLastClickedUid)) {
				iv.setBackgroundDrawable(null);
				mLastClickedUid = null;
				break;
			}
		}
	}

	private void animateTo(final String uid) {
		CustomView view = MapManager.get().getViewByTag(uid);
		if (view == null || view.loc == null) {
			ThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						final LocVo locVo = HttpManager.get().getLocation(uid);
						if (locVo == null) {
							MainThreadHandler.makeToast(R.string.friend_no_location);
							return;
						}
						MainThreadHandler.post(new Runnable() {
							@Override
							public void run() {
								Location loc = locVo.toLocation();
								if (loc != null) {
									MapManager.get().refreshFriendView(loc, false);
									MapManager.get().animateTo(uid, loc);
								}
							}
						});
					} catch (VidException e) {
						VLog.e("test", e);
					}
				}
			});
		} else {
			MapManager.get().animateTo(uid, view.loc);
		}
	}

}
