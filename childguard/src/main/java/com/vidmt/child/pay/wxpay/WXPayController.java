package com.vidmt.child.pay.wxpay;

import android.app.Activity;

import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.child.Config;
import com.vidmt.child.R;
import com.vidmt.child.dlgs.LoadingDlg;
import com.vidmt.child.exceptions.VidException;
import com.vidmt.child.utils.HttpUtil;
import com.vidmt.child.vos.WxpayInfoVo;
import com.vidmt.xmpp.enums.XmppEnums.LvlType;

public class WXPayController {
	private Activity mActivity;
	private IWXAPI wxApi;

	public WXPayController(Activity act) {
		mActivity = act;
		wxApi = WXAPIFactory.createWXAPI(act, null);
		wxApi.registerApp(Config.WXPAY_APP_ID);
	}

	public boolean isWXAppInstalled() {
		return wxApi.isWXAppInstalled() && wxApi.isWXAppSupportAPI();
	}

	public void pay(final LvlType vipType, final int payNum) {
		final LoadingDlg dlg = new LoadingDlg(mActivity, R.string.loading);
		dlg.show();
		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					WxpayInfoVo payInfo = HttpUtil.getWxpayPayInfo(vipType, payNum);
					pay(payInfo);// 高超微信支付
				} catch (VidException e) {
					VLog.e("test", e);
					MainThreadHandler.makeToast("出错了!");
				}
				dlg.dismiss();
			}
		});
	}

	private void pay(WxpayInfoVo payInfo) {
		// 生成签名参数
		PayReq payReq = new PayReq();
		payReq.appId = payInfo.appId;
		payReq.partnerId = payInfo.partnerId;
		payReq.prepayId = payInfo.prepayId;
		payReq.packageValue = payInfo.packageValue;
		payReq.nonceStr = payInfo.nonceStr;
		payReq.timeStamp = payInfo.timeStamp;
		payReq.sign = payInfo.sign;
		// 发起支付
		boolean sentSuccess = wxApi.sendReq(payReq);
		VLog.d("wxpay", "send response to wechat app:" + sentSuccess);
		if (!sentSuccess) {
			MainThreadHandler.makeToast("调用微信失败！");
		}
	}

}
