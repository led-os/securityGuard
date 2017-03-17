package com.vidmt.child.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.child.Config;
import com.vidmt.child.R;
import com.vidmt.child.activities.VipCenterActivity;
import com.vidmt.child.listeners.WXPaySuccessListener;
import com.vidmt.child.managers.AccManager;
import com.vidmt.child.utils.UserUtil;
import com.vidmt.xmpp.enums.XmppEnums;
import com.vidmt.xmpp.exts.CgUserIQ;
import com.vidmt.xmpp.exts.LvlIQ;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
	private static final String TAG = "wxpay";
	private IWXAPI wxApi;
	private AccManager.IAccManager accMgr = AccManager.get();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wx_pay_result);

		wxApi = WXAPIFactory.createWXAPI(this, Config.WXPAY_APP_ID);
		wxApi.handleIntent(getIntent(), this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		wxApi.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
		// called when a request is built from bundle
	}

	@Override
	public void onResp(BaseResp resp) {
		// called when a response is built from bundle
		VLog.d(TAG, "onWXPayFinish, errCode = " + resp.errCode);
		int code = resp.errCode;
		switch (code) {
		case 0:// 成功
			MainThreadHandler.makeToast(R.string.pay_success);
			WXPaySuccessListener.get().triggerOnWXPaySuccessListener();
			CgUserIQ selfInfoIq = accMgr.getUserInfo(null);// 得到自己所有资料
			UserUtil.initParentInfo(selfInfoIq);
			LvlIQ lvlInfoIq = accMgr.getLvlInfo(selfInfoIq.code == null ? XmppEnums.LvlType.NONE : selfInfoIq.code);
			UserUtil.initLvl(lvlInfoIq);
			startActivity(new Intent(getApplicationContext(), VipCenterActivity.class));
			break;
		case -1:// 错误
			MainThreadHandler.makeToast("发生错误！" + String.valueOf(resp.errCode)
					+ (TextUtils.isEmpty(resp.errStr) ? "" : ":" + resp.errStr));
			break;
		case -2:// 用户取消
			MainThreadHandler.makeToast(R.string.op_canceled);
			break;
		}
		finish();
		// if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
		// }
	}
}