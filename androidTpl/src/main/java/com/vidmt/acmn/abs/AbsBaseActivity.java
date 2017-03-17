package com.vidmt.acmn.abs;

import java.util.ArrayList;
import java.util.List;

import com.vidmt.acmn.utils.andr.FLog;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.async.DefaultThreadHandler;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.os.Process;
import android.view.Window;

public abstract class AbsBaseActivity extends Activity {
	public static final String ACTION_EXIT = "com.vidmt.action.EXIT";
	private BroadcastReceiver receiver;

	public static final void exitAll() {
		FLog.endAllTag();
		VLib.app().sendBroadcast(new Intent(ACTION_EXIT));

//		DefaultThreadHandler.post(new Runnable() {
//
//			@Override
//			public void run() {
//				killProcess();
//			}
//		}, 1 * 1000);
	}
	
	public static void killProcess() {
		ActivityManager activityMgr = (ActivityManager) VLib.app().getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningAppProcessInfo> mRunningProcess = activityMgr.getRunningAppProcesses();

		PackageInfo pkgInfo = SysUtil.getPkgInfo();
		String pkgName = pkgInfo.packageName;
		List<Integer> pids = new ArrayList<Integer>();
		for (ActivityManager.RunningAppProcessInfo amProcess : mRunningProcess) {
			if (amProcess.processName.startsWith(pkgName) && !amProcess.processName.equals(pkgName)) {
				pids.add(amProcess.pid);
			}
		}
		pids.add(Process.myPid());
		for (Integer pid : pids) {
			Process.killProcess(pid);
		}
	}

	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_EXIT);
		receiver = new InnerReceiver(this);
		this.registerReceiver(receiver, filter);
	};

	@Override
	protected void onDestroy() {
		this.unregisterReceiver(receiver);
		receiver = null;
		super.onDestroy();
	}

	private static class InnerReceiver extends BroadcastReceiver {
		public Activity mActivity;

		public InnerReceiver(Activity act) {
			mActivity = act;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (mActivity != null) {
				mActivity.finish();
				mActivity = null;
			}
		}
	}

}
