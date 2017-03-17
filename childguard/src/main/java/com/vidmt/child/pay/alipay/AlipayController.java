package com.vidmt.child.pay.alipay;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import com.alipay.sdk.app.PayTask;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.child.R;
import com.vidmt.child.activities.VipCenterActivity;
import com.vidmt.child.dlgs.LoadingDlg;
import com.vidmt.child.exceptions.VidException;
import com.vidmt.child.managers.AccManager;
import com.vidmt.child.utils.HttpUtil;
import com.vidmt.child.utils.UserUtil;
import com.vidmt.xmpp.enums.XmppEnums.LvlType;
import com.vidmt.xmpp.exts.CgUserIQ;
import com.vidmt.xmpp.exts.LvlIQ;

public class AlipayController {
	private Activity mActivity;
	private AccManager.IAccManager accMgr = AccManager.get();
	public AlipayController(Activity act) {
		mActivity = act;
	}

	/**
	 * call alipay sdk pay. 调用SDK支付
	 * 
	 */
	public void pay(final LvlType vipType, final int payNum) {

		final LoadingDlg dlg = new LoadingDlg(mActivity, R.string.loading);
		dlg.show();
		ThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					// 获取完整的符合支付宝参数规范的订单信息
					String payInfo = HttpUtil.getAlipayPayInfo(vipType, payNum);
					// 构造PayTask 对象
					PayTask alipay = new PayTask(mActivity);
					// 调用支付接口，获取支付结果
					String result = alipay.pay(payInfo);
					PayResult payResult = new PayResult(result);
					// 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
					String resultInfo = payResult.getResult();
					String resultStatus = payResult.getResultStatus();
					// 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
					if (TextUtils.equals(resultStatus, "9000")) {
						MainThreadHandler.makeToast("支付成功");
						CgUserIQ selfInfoIq = accMgr.getUserInfo(null);// 得到自己所有资料
						UserUtil.initParentInfo(selfInfoIq);
						LvlIQ lvlInfoIq = accMgr.getLvlInfo(selfInfoIq.code == null ? LvlType.NONE : selfInfoIq.code);
						UserUtil.initLvl(lvlInfoIq);
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
					MainThreadHandler.makeToast("出错了!");
				}
				dlg.dismiss();
			}
		});
	}

}