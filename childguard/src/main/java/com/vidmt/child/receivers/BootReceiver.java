package com.vidmt.child.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vidmt.acmn.utils.andr.FLog;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.child.managers.ServiceManager;
import com.vidmt.child.services.LoginLocateService;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {
		VLog.i("test", "onReceive:" + intent.getAction());
		FLog.d("onReceive", intent.getAction());

		if (!ServiceManager.isServiceRunning(LoginLocateService.class)) {
			//if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))
			{
				VLog.i("test", "手机开机了！");
				ServiceManager.get().startService(false);
				return;
			}
		}
//		boolean exitButRecMsg = SysUtil.getBooleanPref(PrefKeyConst.PREF_EXIT_BUT_RECEIVE_MSG, true);
//		if (!exitButRecMsg) {// 完全退出
//			return;
//		}
//
//		if (!ServiceManager.isServiceRunning(LoginLocateService.class)) {
//			if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
//				VLog.i("test", "手机屏亮了！");
//				ServiceManager.get().startService(false);
//			}
//		} else {
//			VLog.i("test", "LoginService is already running # " + intent.getAction());
//		}

	}
}
