package com.vidmt.telephone.managers;

import java.util.ArrayList;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;

import com.vidmt.acmn.abs.VLib;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.services.LoginLocateService;

public class ServiceManager {
	private static ServiceManager sInstance;

	public static ServiceManager get() {
		if (sInstance == null) {
			sInstance = new ServiceManager();
		}
		return sInstance;
	}

	private ServiceManager() {
	}

//	public static boolean isServiceRunning(Class<?> clz) {// 注：>=5.0之后的系统不适用
//		ActivityManager myManager = (ActivityManager) VLib.app().getSystemService(Context.ACTIVITY_SERVICE);
//		ArrayList<RunningServiceInfo> runningServices = (ArrayList<RunningServiceInfo>) myManager
//				.getRunningServices(500);
//		for (int i = 0; i < runningServices.size(); i++) {
//			if (runningServices.get(i).service.getClassName().equals(clz.getName())) {
//				return true;
//			}
//		}
//		return false;
//	}

	public void startService() {
		Intent service = new Intent(VLib.app(), LoginLocateService.class);
		//service.
		VLib.app().startService(service);
	}

	public void stopService() {
		Intent service = new Intent(VLib.app(), LoginLocateService.class);
		VLib.app().stopService(service);
	}

}
