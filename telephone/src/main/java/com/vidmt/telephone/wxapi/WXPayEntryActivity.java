package com.vidmt.telephone.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.telephone.Config;
import com.vidmt.telephone.R;
import com.vidmt.telephone.listeners.PaySuccessListener;
import com.vidmt.telephone.utils.Enums.PayType;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
	private static final String TAG = "wxpay";
	private IWXAPI wxApi;

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
			PaySuccessListener.get().triggerOnPaySuccessListener(PayType.WX);
			break;
		case -1:// 错误
			MainThreadHandler.makeToast("支付失败，请重试或更换支付方式！");
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