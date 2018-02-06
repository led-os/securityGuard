package com.vidmt.telephone.tasks;

import java.util.Map;

import android.app.Activity;
import android.content.Intent;

import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.vidmt.acmn.abs.AbsBaseActivity;
import com.vidmt.acmn.abs.VLib;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.PrefKeyConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.activities.UpdateActivity;
import com.vidmt.telephone.dlgs.BaseDialog.DialogClickListener;
import com.vidmt.telephone.dlgs.UpdateDlg;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.utils.HttpUtil;
import com.vidmt.telephone.utils.JsonResult;
import com.vidmt.telephone.utils.JsonUtil;

public class UpdateTask {
	public static synchronized void launchUpdateTask(final Activity act, final boolean manually) {
		try {
			HttpUtil.getConf(new RequestCallBack<String>() {
				@Override
				public void onSuccess(ResponseInfo<String> responseInfo) {
					String rawJson = responseInfo.result;
					try {
						JsonResult<String> jsonResult = JsonUtil.getCorrectJsonResult(rawJson);
						Map<String, String> resultMap = jsonResult.getDataAsMap();
						if (resultMap == null) {
							VLog.e("test", VLib.app().getString(R.string.remote_server_error));
							return;
						}
						int latestVer = Integer.parseInt(resultMap.get("ver"));
						int force = Integer.parseInt(resultMap.get("force"));
						boolean needSmsVertify = Boolean.parseBoolean(resultMap.get("need_sms_verify"));
						SysUtil.savePref(PrefKeyConst.NEED_SMS_VERTIFY,needSmsVertify);
						final String updateUrl = resultMap.get("url");
						VLog.i("test", "最新版本:" + latestVer + ",强制版本:" + force + ",url=" + updateUrl);
						final int curVer = SysUtil.getPkgInfo().versionCode;
						if (curVer < latestVer) {
							final boolean updateForcely = curVer <= force;
							int msgResId = updateForcely ? R.string.update_force : R.string.will_update;
							UpdateDlg dlg = new UpdateDlg(act, msgResId);
							dlg.setOnClickListener(new DialogClickListener() {
								@Override
								public void onOK() {
									super.onOK();
									Intent intent = new Intent(act, UpdateActivity.class);
									intent.putExtra(ExtraConst.EXTRA_UPDATE_FORCE, updateForcely);
									intent.putExtra(ExtraConst.EXTRA_UPDATE_URL, updateUrl);
									act.startActivity(intent);
								}

								@Override
								public void onCancel() {
									super.onCancel();
									if (updateForcely) {
										AbsBaseActivity.exitAll();
									}
								}
							});
							dlg.show();
							if (updateForcely) {
								dlg.setCancelable(false);
							}
						} else {
							if (manually) {
								MainThreadHandler.makeToast(R.string.already_newest_version);
							}
						}
					} catch (VidException e) {
						VLog.e("test", e);
					}
				}

				@Override
				public void onFailure(HttpException error, String msg) {
					VLog.e("获取配置文件错误：", error + "," + msg);
					MainThreadHandler.makeToast("获取配置文件失败，请检查网络。");
				}
			});
		} catch (Throwable e) {
			VLog.e("update Error", e);
		}
	}
}
