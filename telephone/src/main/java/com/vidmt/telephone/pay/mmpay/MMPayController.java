package com.vidmt.telephone.pay.mmpay;

import java.util.HashMap;

import mm.sms.purchasesdk.OnSMSPurchaseListener;
import mm.sms.purchasesdk.PurchaseCode;
import mm.sms.purchasesdk.SMSPurchase;
import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.telephone.App;
import com.vidmt.telephone.Config;

public class MMPayController implements OnSMSPurchaseListener {
	private static MMPayController sInstance;
	private Activity mActivity;
	private String paycode;
	private SMSPurchase purchase;

	public static MMPayController get() {
		if (sInstance == null) {
			sInstance = new MMPayController();
		}
		return sInstance;
	}

	public void init(Activity act) {
		mActivity = act;
		purchase = SMSPurchase.getInstance();
		purchase.setAppInfo(Config.MMPAY_APP_ID, Config.MMPAY_APP_KEY);
		purchase.smsInit(mActivity, this);
		paycode = getPayCode();
	}

	public void pay() {
		purchase.smsOrder(mActivity, paycode, this, "userData");
	}

	@Override
	public void onInitFinish(int statusCode) {
		// return the response of init()
		String desc = SMSPurchase.getDescription(statusCode);
		String result = "初始化结果：" + SMSPurchase.getReason(statusCode);
		Log.i("lk", "onInitFinish:desc=" + desc + ",result=" + result);
	}

	@Override
	public void onBillingFinish(int statusCode, HashMap retObj) {
		// String desc = SMSPurchase.getDescription(statusCode);
		String result = null;
		// 商品信息
		if (statusCode == PurchaseCode.ORDER_OK || statusCode == PurchaseCode.ORDER_OK_TIMEOUT) {
			/**
			 * 商品购买成功或者已经购买。 此时会返回商品的paycode，orderID,以及剩余时间(租赁类型商品)
			 */
			String paycode = null;
			// 商品的交易 ID，用户可以根据这个交易ID，查询商品是否已经交易
			String tradeID = null;
			String netType = null;
			result = "订购结果：订购成功";
			if (retObj != null) {
				paycode = (String) retObj.get(OnSMSPurchaseListener.PAYCODE);
				if (paycode != null && paycode.trim().length() != 0) {
					result = result + ",Paycode:" + paycode;
				}
				tradeID = (String) retObj.get(OnSMSPurchaseListener.TRADEID);
				if (tradeID != null && tradeID.trim().length() != 0) {
					result = result + ",tradeid:" + tradeID;
				}
				netType = (String) retObj.get(OnSMSPurchaseListener.NETTYPE);
				if (netType != null && netType.trim().length() != 0) {
					result = result + ",NetType:" + netType;
				}
			}
		} else {// 订购失败。
			result = "订购结果：" + SMSPurchase.getReason(statusCode);
		}
		Log.i("lk", "onBillingFinish:result=" + result);
	}

	private String getPayCode() {
		TelephonyManager telManager = (TelephonyManager) App.get().getSystemService(Context.TELEPHONY_SERVICE);
		String operator = telManager.getSimOperator();
		if (operator != null) {
			if (operator.equals("46000") || operator.equals("46002") || operator.equals("46007")) {// 移动
				return Config.MMPAY_PAYCODE_MOBILE;
			} else if (operator.equals("46001")) {// 联通
				return Config.MMPAY_PAYCODE_UNICOM;
			} else if (operator.equals("46003")) {// 电信
				return Config.MMPAY_PAYCODE_TELECOM;
			}
			VLog.e("test", "sim unknown operator");
			return null;
		}
		VLog.e("test", "sim unknown");
		return null;
	}

}
