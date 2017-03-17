package com.vidmt.child.activities.main;

import android.content.Intent;

import com.baidu.mapapi.map.Circle;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.andr.AndrUtil;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.child.App;
import com.vidmt.child.ExtraConst;
import com.vidmt.child.R;
import com.vidmt.child.activities.AlarmActivity;
import com.vidmt.child.activities.MainActivity;
import com.vidmt.child.managers.AccManager;
import com.vidmt.child.managers.MapManager;
import com.vidmt.child.utils.VidUtil;
import com.vidmt.xmpp.utils.XmppUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainController {
	private static MainController sInstance;
	private MainActivity mActivity;
	private List<String> mInEfenceList = new ArrayList<String>();
	private String mAlarmingEfenceName;

	public static MainController get() {
		if (sInstance == null) {
			sInstance = new MainController();
		}
		return sInstance;
	}

	public void init(MainActivity act) {
		mActivity = act;
	}

	public void removeEfenceByName(String name) {
		AccManager.get().deleteFence(XmppUtil.buildJid(VidUtil.getSideName()), name);
		mInEfenceList.remove(name);
		stopEfenceAlarm();
		MapManager.get(null).removeEfenceOverlay(name);
	}

	public void stopEfenceAlarm() {
		mAlarmingEfenceName = null;
	}

	public void warnBeyondEfence(LatLng ll) {
		Map<String, Overlay[]> efenceOverlayMap = MapManager.get(null).getEfenceOverlayMap();
		if (efenceOverlayMap.size() == 0) {
			return;
		}
		for (final String fenceName : efenceOverlayMap.keySet()) {
			Circle circleOverlay = (Circle) efenceOverlayMap.get(fenceName)[1];
			LatLng centerLl = circleOverlay.getCenter();
			double distance = DistanceUtil.getDistance(centerLl, ll);
			if (distance <= circleOverlay.getRadius() && !mInEfenceList.contains(fenceName)) {// 刚进入到此围栏
				mInEfenceList.add(fenceName);
				MainThreadHandler.makeToast(VLib.app().getString(R.string.baby_enter_efence) + "【" + fenceName + "】");
				VidUtil.stopSound();// 只要在围栏内就不报警
				mAlarmingEfenceName = null;
			} else if (distance > circleOverlay.getRadius() && mInEfenceList.contains(fenceName)) {// 进入后又离开围栏
				mInEfenceList.remove(fenceName);
				final String toastStr = App.get().getString(R.string.baby_beyond_efence) + "【" + fenceName + "】";
				MainThreadHandler.makeToast(toastStr);
				if (mInEfenceList.size() != 0) {// 还在其他围栏里面
					return;
				}
				mAlarmingEfenceName = fenceName;
				MainThreadHandler.post(new Runnable() {
					@Override
					public void run() {
						if (mAlarmingEfenceName == null) {
							return;
						}
						AndrUtil.makeToast(toastStr);
						MainThreadHandler.postDelayed(this, 2 * 1000);
					}
				});
				Intent i = new Intent(mActivity, AlarmActivity.class);
				i.putExtra(ExtraConst.EXTRA_FENCE_NAME, mAlarmingEfenceName);
				mActivity.startActivity(i);
			}
		}
	}

}
