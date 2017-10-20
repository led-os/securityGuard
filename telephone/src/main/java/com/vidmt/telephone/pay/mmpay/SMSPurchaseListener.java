package com.vidmt.telephone.pay.mmpay;

import java.util.HashMap;

import mm.sms.purchasesdk.OnSMSPurchaseListener;
import mm.sms.purchasesdk.SMSPurchase;

// implment it to get the response of query/order
public class SMSPurchaseListener implements OnSMSPurchaseListener {

	@Override
	public void onBillingFinish(int statusCode, HashMap returnObject) {
		// return the response of checkAndOrder(), if purchase succeed, orderID
		// is returned together
		String desc = SMSPurchase.getDescription(statusCode);
	}

	@Override
	public void onInitFinish(int statusCode) {
		// return the response of init()
		String desc = SMSPurchase.getDescription(statusCode);
	}

}
