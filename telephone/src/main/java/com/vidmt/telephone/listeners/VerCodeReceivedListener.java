package com.vidmt.telephone.listeners;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsMessage;
import android.text.TextUtils;

import com.vidmt.acmn.utils.andr.async.MainThreadHandler;

public class VerCodeReceivedListener {
	private static VerCodeReceivedListener sInstance;
	private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	private IntentFilter mSmsFilter;
	private OnVerCodeReceivedListener mListener;
	private Activity mActivity;

	public static VerCodeReceivedListener get() {
		if (sInstance == null) {
			sInstance = new VerCodeReceivedListener();
		}
		return sInstance;
	}

	private VerCodeReceivedListener() {
		mSmsFilter = new IntentFilter();
		mSmsFilter.addAction(ACTION_SMS_RECEIVED);
		mSmsFilter.setPriority(Integer.MAX_VALUE);
	}

	public interface OnVerCodeReceivedListener {
		public void onVerCodeReceived(String vercode);
	}

	public void setOnVerCodeReceivedListener(Activity act, OnVerCodeReceivedListener listener) {
		mActivity = act;
		mListener = listener;
		act.registerReceiver(mSmsReceiver, mSmsFilter);
	}

	private BroadcastReceiver mSmsReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {// 注：需要手动开启'读取短信'权限,如MIUI
			Object[] objs = (Object[]) intent.getExtras().get("pdus");
			for (Object obj : objs) {
				byte[] pdu = (byte[]) obj;
				SmsMessage sms = SmsMessage.createFromPdu(pdu);
				String from = sms.getOriginatingAddress();// 短息的手机号,+86开头？
				String content = sms.getMessageBody();// 短信的内容
				final String vercode = parseVercode(content);
				if (TextUtils.isEmpty(vercode)) {
					return;
				}
				MainThreadHandler.post(new Runnable() {
					@Override
					public void run() {
						if (vercode == null) {
							return;
						}
						if (mListener != null) {
							mListener.onVerCodeReceived(vercode);
						}
					}
				});
			}
		}
	};

	public void removeOnVerCodeReceivedListener() {
		if (mActivity != null) {
			try {
				mActivity.unregisterReceiver(mSmsReceiver);
			} catch (Throwable t) {
			}
		}
		mListener = null;
	}

	private String parseVercode(String content) {
		String vercode = "";
		if(content == null){
			return null;
		}
		if (content.contains("验证码")) {// "xxx验证码：7545"
			int start = content.indexOf("：");// 注：中文冒号，且只用了这一种匹配模式
			if (start == -1) {
				return null;
			}
			char ch = content.charAt(++start);
			while (isInteger(ch)) {
				vercode += ch;
				if (start == content.length() - 1) {
					break;
				}
				ch = content.charAt(++start);
			}
		}
		if (vercode.equals("")) {
			return null;
		}
		return vercode;
	}

	private boolean isInteger(char value) {
		try {
			Integer.parseInt(String.valueOf(value));
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

}
