package com.vidmt.telephone.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.vidmt.acmn.utils.andr.FLog;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.telephone.PrefKeyConst;
import com.vidmt.telephone.managers.ServiceManager;
import com.vidmt.telephone.services.LoginLocateService;
import com.vidmt.telephone.utils.VidUtil;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {

		//huawei change not self-start;

		VidUtil.fLog("onReceive", intent.getAction());

			//if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))
			{
				//check server whether allow self start;
				Boolean self_start = SysUtil.getBooleanPref(PrefKeyConst.PREF_SELF_START , false);
				VidUtil.fLog("onReceive", "self_start: "+ self_start);
				if(self_start == false){
					return;
				}
				VLog.i("test", "手机开机了！");
				String account = SysUtil.getPref(PrefKeyConst.PREF_ACCOUNT);
				String encryptedPwd = SysUtil.getPref(PrefKeyConst.PREF_PASSWORD);
				if (TextUtils.isEmpty(account) || TextUtils.isEmpty(encryptedPwd)){
					// 从未登录过不进行自启动；
				}else {
					ServiceManager.get().startService();
				}
				return;
			}


	}
}
