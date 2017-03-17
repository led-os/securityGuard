package com.vidmt.acmn.utils.andr;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.widget.Toast;

import com.vidmt.acmn.abs.VLib;

public class AndrUtil {
	private static Toast sToast;

	public static void makeToast(int resId) {
		getToast(VLib.app().getString(resId), Toast.LENGTH_SHORT).show();
	}

	public static void makeToast(CharSequence msg) {
		getToast(msg, Toast.LENGTH_SHORT).show();
	}

	public static void makeLongToast(int resId) {
		getToast(VLib.app().getString(resId), Toast.LENGTH_LONG).show();
	}

	public static void makeLongToast(CharSequence msg) {
		getToast(msg, Toast.LENGTH_LONG).show();
	}

	@SuppressLint("ShowToast")
	private static Toast getToast(CharSequence msg, int duration) {
		if (sToast == null) {
			sToast = Toast.makeText(VLib.app(), msg, duration);
		} else {
			sToast.setText(msg);
		}
		return Toast.makeText(VLib.app(), msg, duration);
	}

	public static ComponentName getTopActivity() {
		ActivityManager manager = (ActivityManager) VLib.app().getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
		return runningTaskInfos.get(0).topActivity;
	}

}
