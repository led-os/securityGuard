package com.vidmt.telephone.pay.alipay;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import com.alipay.sdk.app.PayTask;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.telephone.R;
import com.vidmt.telephone.activities.VipCenterActivity;
import com.vidmt.telephone.dlgs.LoadingDlg;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.listeners.PaySuccessListener;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.managers.HttpManager;
import com.vidmt.telephone.utils.Enums.PayType;
import com.vidmt.telephone.utils.Enums.VipType;
import com.vidmt.telephone.utils.VipInfoUtil;

public class AlipayController {
	private Activity mActivity;

	public AlipayController(Activity act) {
		mActivity = act;
	}

	/**
	 * call alipay sdk pay. 调用SDK支付
	 * 
	 */
	public void pay(final VipType vipType) {
		final LoadingDlg dlg = new LoadingDlg(mActivity, R.string.loading);
		dlg.show();
		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					// 获取完整的符合支付宝参数规范的订单信息
					String payInfo = HttpManager.get().getAlipayPayInfo(vipType);
					// 构造PayTask 对象
					PayTask alipay = new PayTask(mActivity);
					// 调用支付接口，获取支付结果
					//huawei change;
					//String result = alipay.pay(payInfo);
					String result = alipay.pay(payInfo, true);

					PayResult payResult = new PayResult(result);
					// 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
					String resultInfo = payResult.getResult();
					String resultStatus = payResult.getResultStatus();
					// 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
					dlg.dismiss();
					if (TextUtils.equals(resultStatus, "9000")) {
						PaySuccessListener.get().triggerOnPaySuccessListener(PayType.ALI);
						MainThreadHandler.makeToast("支付成功");
						long timeLeft = VipInfoUtil.getLvl(vipType).getExpire() * 24L * 60 * 60 * 1000;
						User curUser = AccManager.get().getCurUser();
						curUser.vipType = vipType.name();
						curUser.timeLeft = curUser.timeLeft + timeLeft;
						mActivity.startActivity(new Intent(mActivity, VipCenterActivity.class));
						mActivity.finish();
					} else {
						// 判断resultStatus 为非“9000”则代表可能支付失败
						// “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
						if (TextUtils.equals(resultStatus, "8000")) {
							MainThreadHandler.makeToast("支付结果确认中");
						} else if (TextUtils.equals(resultStatus, "4000")) {
							MainThreadHandler.makeLongToast("订单支付失败：" + payResult.getMemo());
						} else if (TextUtils.equals(resultStatus, "6002")) {
							MainThreadHandler.makeLongToast("网络连接出错：" + payResult.getMemo());
						} else if (TextUtils.equals(resultStatus, "6001")) {
							MainThreadHandler.makeLongToast("用户中途取消：" + payResult.getMemo());
						}
					}
				} catch (VidException e) {
					VLog.e("test", e);
					MainThreadHandler.makeToast("出错了~_~，请重试！");
					dlg.dismiss();
				}
			}
		});
	}

}