package com.vidmt.child.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.acmn.utils.andr.AndrUtil;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.DefaultThreadHandler;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.child.Const;
import com.vidmt.child.ExtraConst;
import com.vidmt.child.R;
import com.vidmt.child.activities.main.MainController;
import com.vidmt.child.dlgs.ExitDlg;
import com.vidmt.child.dlgs.OpenGPSDlg;
import com.vidmt.child.exceptions.VidException;
import com.vidmt.child.listeners.AvatarChangedListener;
import com.vidmt.child.listeners.AvatarChangedListener.OnAvatarChangedListener;
import com.vidmt.child.managers.AccManager;
import com.vidmt.child.managers.AdManager;
import com.vidmt.child.managers.LocationManager;
import com.vidmt.child.managers.LocationManager.AbsLocationListener;
import com.vidmt.child.managers.MapManager;
import com.vidmt.child.tasks.UpdateTask;
import com.vidmt.child.ui.views.BadgeView;
import com.vidmt.child.utils.DBUtil;
import com.vidmt.child.utils.Enums.VipFuncType;
import com.vidmt.child.utils.GeoUtil;
import com.vidmt.child.utils.HttpUtil;
import com.vidmt.child.utils.UserUtil;
import com.vidmt.child.utils.VidUtil;
import com.vidmt.child.vos.LocationVo;
import com.vidmt.xmpp.exts.FenceIQ;
import com.vidmt.xmpp.inner.XmppManager;
import com.vidmt.xmpp.listeners.OnMsgReceivedListener;
import com.vidmt.xmpp.listeners.OnRosterListener.AbsOnRosterListener;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

public class MainActivity extends AbsVidActivity {
	private MapManager mMapManager;
	// private MainController mMainCtrl;
	private LocationManager mLocManager;
	@ViewInject(R.id.title)
	private TextView mTitleTv;
	@ViewInject(R.id.right)
	private Button mTitleRightBtn;
	@ViewInject(R.id.locate_child)
	private ImageView mLocateChildIv;
	@ViewInject(R.id.locate_parent)
	private ImageView mLocateParentIv;
	@ViewInject(R.id.reconnect_layout)
	private View mReconnectLayout;
	@ViewInject(R.id.baby_img)
	private ImageView mBabyIv;
	private BadgeView mUnreadView;
	private boolean mIsSelfFirstLocate = true;
	private boolean mIsChildFirstLocate = true;
	private MainController mMainCtrl;
	private boolean mIsDownloadLocRemoved;
	private boolean mIsDestroyed;
	private Runnable mLocationRunnable = new Runnable() {
		boolean animatedToChild;

		@Override
		public void run() {
			if (mIsDestroyed) {// 防止时序异常
				return;
			}
			try {
				// 得到孩子最新位置
				LocationVo loc = HttpUtil.getLocation(UserUtil.getBabyInfo().uid);
				if (mIsSelfFirstLocate && !animatedToChild) {// 还未定位过parent,animateTo-child
					mMapManager.animateTo(new LatLng(loc.lat, loc.lon));
					animatedToChild = true;
				}
				VLog.i("test", "get new child location：" + loc.lat + "," + loc.lon);
				if (loc != null) {
					mLocManager.setChildLocation(loc.toLocation());
					if (mIsChildFirstLocate) {
						mMapManager.updateChildMarker(true, true);
						mIsChildFirstLocate = false;
					} else {
						mMapManager.updateChildMarker(false, false);
					}
				}
			} catch (VidException e) {
				VLog.e("test", e);
			}
			DefaultThreadHandler.post(this, Const.DEFAUL_LOC_FREQUENCY);
		}
	};
	private AbsLocationListener mLocationListener = new AbsLocationListener() {
		@Override
		public void onLocationChanged(final Location loc) {
			// VLog.d("test", "onLocationChanged:" + loc.getLatitude() + "," +
			// loc.getLongitude());
			if (mIsSelfFirstLocate) {
				mMapManager.animateTo(new LatLng(loc.getLatitude(), loc.getLongitude()));
				mIsSelfFirstLocate = false;
				mMapManager.updateParentMarker();
			} else {// 至少已被定位了一次
				mMapManager.updateParentMarker();
			}
		}
	};
	private AbsOnRosterListener mAbsOnRosterListener = new AbsOnRosterListener() {
		@Override
		public void presenceChanged(Presence presence) {
			if (presence.getType() == Presence.Type.available) {// 上线
				if (mIsDownloadLocRemoved) {
					DefaultThreadHandler.post(mLocationRunnable);
					mIsDownloadLocRemoved = false;
				}
			} else {
				DefaultThreadHandler.remove(mLocationRunnable);
				mIsDownloadLocRemoved = true;
			}
			if (mLocManager.getChildLoc() != null) {// 上/下线后，都重新开始渲染
				mMapManager.updateChildMarker(true, false);
			}
			MainThreadHandler.post(new Runnable() {
				@Override
				public void run() {
					initIcons(false, true);
				}
			});
		}
	};
	private OnMsgReceivedListener mOnMsgReceivedListener = new OnMsgReceivedListener() {
		@Override
		public void onMsgReceived(Chat chat, Message msg) {
			DefaultThreadHandler.post(new Runnable() {
				@Override
				public void run() {
					final int unReadNum = DBUtil.getUnReadNum();
					MainThreadHandler.post(new Runnable() {
						@Override
						public void run() {
							if (unReadNum != 0) {
								mUnreadView.setText(unReadNum + "");
								mUnreadView.show();
							}
						}
					});
				}
			}, 2 * 1000);// Depressed
		}
	};
	private OnAvatarChangedListener mOnAvatarChangedListener = new OnAvatarChangedListener() {
		@Override
		public void onAvatarChanged(final boolean forParent, Bitmap bm) {
			Log.i("lk", "onAvatarChanged:" + forParent + "," + bm);
			if (bm != null) {
				if (forParent) {
					VidUtil.setAvatar(mLocateParentIv, bm, true);
				} else {
					VidUtil.setAvatar(mBabyIv, bm, false);
					VidUtil.setAvatar(mLocateChildIv, bm, false);
					MapManager.get(null).updateChildMarker(bm);
				}
				return;
			}
			if (forParent) {
				initIcons(true, false);
			} else {
				initIcons(false, true);
				MapManager.get(null).updateChildMarker(true, false);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mMapManager = MapManager.get(this);
		setContentView(mMapManager.getMapView());
		boolean isRestartFromCrash = getIntent().getBooleanExtra(ExtraConst.EXTRA_RESTART_FROM_CRASH, false);
		if (isRestartFromCrash) {// 崩溃后重启
			moveTaskToBack(false);
		}

		// mMainCtrl = MainController.get();
		mLocManager = LocationManager.get();
		mLocManager.start();

		View mapWidgetsView = LayoutInflater.from(this).inflate(R.layout.activity_main, null);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		addContentView(mapWidgetsView, params);
		ViewUtils.inject(this, mapWidgetsView);

		initTitle();
		mLocManager.addListener(mLocationListener);

		XmppManager.get().addXmppListener(mAbsOnRosterListener);
		XmppManager.get().addXmppListener(mOnMsgReceivedListener);

		mMainCtrl = MainController.get();
		mMainCtrl.init(this);

		initUnReadNum();
		initIcons(true, true);

		initFence();

		AvatarChangedListener.get().addOnAvatarChangedListener(mOnAvatarChangedListener);// 监听自已头像的改变

		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				AdManager.get().init(MainActivity.this);// 初始化广告
				UpdateTask.launchUpdateTask(MainActivity.this, false);
			}
		});

		DefaultThreadHandler.post(mLocationRunnable);
	}

	private void initTitle() {
		mTitleTv.setText(R.string.app_name);
		mTitleRightBtn.setBackgroundResource(R.drawable.selector_setting);
		initReconnectView(mReconnectLayout);
	}

	private void initUnReadNum() {
		TextView unReadNumTv = (TextView) findViewById(R.id.unread_num);
		mUnreadView = new BadgeView(this, unReadNumTv);
		int unReadNum = DBUtil.getUnReadNum();
		if (unReadNum != 0) {
			mUnreadView.setText(unReadNum + "");
			mUnreadView.show();
		}
	}

	private void initIcons(boolean forParent, boolean forChild) {
		if (forParent) {
			String avatarUri = UserUtil.getParentInfo().avatarUri;
			VidUtil.asyncCacheAndDisplayAvatar(mLocateParentIv, avatarUri, true);
		}
		if (forChild) {
			String childAvatarUri = UserUtil.getBabyInfo().avatarUri;
			VidUtil.asyncCacheAndDisplayAvatar(mBabyIv, childAvatarUri, false);
			VidUtil.asyncCacheAndDisplayAvatar(mLocateChildIv, childAvatarUri, false);
		}
	}

	private void initFence() {
		FenceIQ fenceIq = AccManager.get().getFence();// get all fence
		if (fenceIq!=null && fenceIq.jid != null) {// 有围栏数据
			for (int i = 0; i < fenceIq.nameList.size(); i++) {
				double lat = fenceIq.latList.get(i);
				double lon = fenceIq.lonList.get(i);
				int radius = fenceIq.radiusList.get(i);
				LatLng center = new LatLng(lat, lon);
				mMapManager.addEfenceOverlay(fenceIq.nameList.get(i), center, radius);
			}
		}
	}

	@OnClick({ R.id.right, R.id.chat, R.id.efence, R.id.trace, R.id.baby_img, R.id.locate_child, R.id.locate_parent,
			R.id.nav })
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.right:
			startActivity(new Intent(this, SettingActivity.class));
			break;
		case R.id.chat:
			if (!XmppManager.get().isAuthenticated()) {
				AndrUtil.makeToast(R.string.not_login);
				return;
			}
			startActivity(new Intent(this, ChattingActivity.class));
			mUnreadView.hide();
			break;
		case R.id.efence:
			if (!UserUtil.isAuthorized(this, VipFuncType.TRACE)) {
				return;
			}
			if (!XmppManager.get().isAuthenticated()) {
				AndrUtil.makeToast(R.string.not_login);
				return;
			}
			Intent intent = new Intent(MainActivity.this, EfenceActivity.class);
			startActivityForResult(intent, 0);
			break;
		case R.id.trace:
			if (!UserUtil.isAuthorized(this, VipFuncType.TRACE)) {
				return;
			}
			Intent i = new Intent(MainActivity.this, MapActivity.class);
			i.putExtra(ExtraConst.EXTRA_MAP_TRACE, true);
			startActivityForResult(i, 0);
			break;
		case R.id.locate_child:
		case R.id.baby_img:
			if (mLocManager.getChildLoc() == null) {
				AndrUtil.makeToast(R.string.no_get_baby_location);
				return;
			}
			mMapManager.animateTo(GeoUtil.location2LatLng(mLocManager.getChildLoc()));
			mMapManager.updateChildMarker(false, true);
			mLocateChildIv.setBackgroundResource(R.drawable.shape_circle_green);
			mLocateParentIv.setBackgroundResource(R.drawable.shape_circle_grey);
			break;
		case R.id.locate_parent:
			LatLng loc = mLocManager.getCurLocation();
			if (loc == null) {
				new OpenGPSDlg(this, R.string.setting, R.string.location_notice).show();
				return;
			}
			mMapManager.animateTo(mLocManager.getCurLocation());
			mMapManager.updateParentMarker(true);
			mLocateParentIv.setBackgroundResource(R.drawable.shape_circle_green);
			mLocateChildIv.setBackgroundResource(R.drawable.shape_circle_grey);
			break;
		case R.id.nav:
			if (!UserUtil.isAuthorized(this, VipFuncType.NAV)) {
				return;
			}
			LatLng curLoc = mLocManager.getCurLocation();
			Location childLoc = mLocManager.getChildLoc();
			if (curLoc == null || childLoc == null) {
				AndrUtil.makeToast(R.string.no_get_baby_location);
				return;
			}
			mMapManager.startNavi(curLoc, GeoUtil.location2LatLng(childLoc));
			break;
		}
	}

	// @Override
	// protected void onActivityResult(int requestCode, int resultCode, Intent
	// data) {
	// switch (resultCode) {
	// case Activity.RESULT_CANCELED:
	// break;
	// case EfenceActivity.EFENCE_TO_ADD_RESULT_CODE:
	// ArrayList<String> curFenceNames =
	// data.getStringArrayListExtra(ExtraConst.EXTRA_CURRENT_FENCE_NAMES);
	// Intent i = new Intent(this, MapActivity.class);
	// i.putStringArrayListExtra(ExtraConst.EXTRA_CURRENT_FENCE_NAMES,
	// curFenceNames);
	// startActivityForResult(i, 0);
	// break;
	// }
	// super.onActivityResult(requestCode, resultCode, data);
	// };

	@Override
	protected void onPause() {
		// MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
		mMapManager.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		// 当activity恢复时需调用MapView.onResume()
		mMapManager.onResume();
		if (ChattingActivity.class.getName().equals(AbsVidActivity.getLastestActivityClzName())) {
			mUnreadView.hide();
		}
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		mIsDestroyed = true;
		DefaultThreadHandler.remove(mLocationRunnable);
		// MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
		XmppManager.get().removeXmppListener(mAbsOnRosterListener);
		XmppManager.get().removeXmppListener(mOnMsgReceivedListener);
		AvatarChangedListener.get().removeOnAvatarChangedListener(mOnAvatarChangedListener);

		mLocManager.removeListener(mLocationListener);
		mLocManager.destroy();
		mLocManager = null;
		mMapManager.destroy();
		mMapManager = null;
		VLog.i("test", "MainActivity onDestroy");
		AccManager.get().logout();
		AbsVidActivity.exitAll();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		new ExitDlg(this).show();
	}

}
