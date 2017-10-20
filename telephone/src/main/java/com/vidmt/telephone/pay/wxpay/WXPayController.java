package com.vidmt.telephone.pay.wxpay;

import android.app.Activity;
import android.content.Intent;

import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.telephone.Config;
import com.vidmt.telephone.R;
import com.vidmt.telephone.activities.VipCenterActivity;
import com.vidmt.telephone.dlgs.LoadingDlg;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.listeners.PaySuccessListener;
import com.vidmt.telephone.listeners.PaySuccessListener.OnPaySuccessListener;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.managers.HttpManager;
import com.vidmt.telephone.utils.Enums.PayType;
import com.vidmt.telephone.utils.Enums.VipType;
import com.vidmt.telephone.utils.VipInfoUtil;
import com.vidmt.telephone.vos.WxpayInfoVo;

public class WXPayController {
	private Activity mActivity;
	private IWXAPI wxApi;
	private VipType vipType;

	public WXPayController(Activity act) {
		mActivity = act;
		wxApi = WXAPIFactory.createWXAPI(act, null);
		wxApi.registerApp(Config.WXPAY_APP_ID);
		PaySuccessListener.get().addOnPaySuccessListener(mOnPaySuccessListener);
	}

	public boolean isWXAppInstalled() {
		return wxApi.isWXAppInstalled() && wxApi.isWXAppSupportAPI();
	}

	public void pay(final VipType vipType) {
		this.vipType = vipType;
		final LoadingDlg dlg = new LoadingDlg(mActivity, R.string.loading);
		dlg.show();
		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					WxpayInfoVo payInfo = HttpManager.get().getWxpayPayInfo(vipType);
					pay(payInfo);// 高超微信支付
				} catch (VidException e) {
					VLog.e("test", e);
					MainThreadHandler.makeToast("出错了~_~，请重试！");
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

	private OnPaySuccessListener mOnPaySuccessListener = new OnPaySuccessListener() {
		@Override
		public void onSuccess(PayType type) {
			if (type != PayType.WX) {
				return;
			}
			long timeLeft = 365 * 24L * 60 * 60 * 1000;
			if (vipType == VipType.TRY) {
				timeLeft = VipInfoUtil.getLvl(vipType).getExpire() * 24L * 60 * 60 * 1000;
			}
			User curUser = AccManager.get().getCurUser();
			curUser.vipType = vipType.name();
			curUser.timeLeft = curUser.timeLeft + timeLeft;
			mActivity.startActivity(new Intent(mActivity, VipCenterActivity.class));
			mActivity.finish();
		}
	};

	public void removeListener() {
		PaySuccessListener.get().removeOnPaySuccessListener(mOnPaySuccessListener);
	}

}
