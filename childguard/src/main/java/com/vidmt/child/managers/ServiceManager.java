package com.vidmt.child.managers;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.vidmt.acmn.abs.VLib;
import com.vidmt.child.ExtraConst;
import com.vidmt.child.services.LoginLocateJobService;
import com.vidmt.child.services.LoginLocateService;

import java.util.ArrayList;

public class ServiceManager {
	private static ServiceManager sInstance;
	
	public static ServiceManager get() {
		if (sInstance == null) {
			sInstance = new ServiceManager();
		}
		return sInstance;
	}

	public static boolean isServiceRunning(Class<?> clz) {
		ActivityManager myManager = (ActivityManager) VLib.app().getSystemService(Context.ACTIVITY_SERVICE);
		ArrayList<RunningServiceInfo> runningServices = (ArrayList<RunningServiceInfo>) myManager
				.getRunningServices(500);
		for (int i = 0; i < runningServices.size(); i++) {
			if (runningServices.get(i).service.getClassName().equals(clz.getName())) {
				return true;
			}
		}
		return false;
	}
	
	public void startService(boolean alreadyLogined) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			Intent workIntent = new Intent(VLib.app().getApplicationContext(),LoginLocateJobService.class);
			workIntent.putExtra("work","work num:"+99);
			LoginLocateJobService.enqueueWork(VLib.app().getApplicationContext(),LoginLocateJobService.class,1,workIntent);
		}else {

			Intent service = new Intent(VLib.app(), LoginLocateService.class);
			if (alreadyLogined) {
				service.putExtra(ExtraConst.EXTRA_ALREADY_LOGINED, true);
			}
			VLib.app().startService(service);
		}

	}
	
	public void stopService() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			Intent service = new Intent(VLib.app(), LoginLocateJobService.class);
			VLib.app().stopService(service);
		}else {
			Intent service = new Intent(VLib.app(), LoginLocateService.class);
			VLib.app().stopService(service);
		}

	}

}
