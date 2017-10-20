package com.vidmt.telephone.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.telephone.App;
import com.vidmt.telephone.Config;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.dlgs.BaseDialog.DialogClickListener;
import com.vidmt.telephone.dlgs.CountryListDlg;
import com.vidmt.telephone.dlgs.LoadingDlg;
import com.vidmt.telephone.exceptions.VHttpException;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.listeners.VerCodeReceivedListener;
import com.vidmt.telephone.listeners.VerCodeReceivedListener.OnVerCodeReceivedListener;
import com.vidmt.telephone.managers.HttpManager;
import com.vidmt.telephone.ui.adapters.CountryListAdapter;
import com.vidmt.telephone.utils.VidUtil;

/**
 * 找回密码页
 */
@ContentView(R.layout.activity_find_pwd)
public class FindPwdActivity extends AbsVidActivity {
	@ViewInject(R.id.account)
	private EditText mAccountEt;
	@ViewInject(R.id.new_pwd)
	private EditText mNewPwdEt;
	@ViewInject(R.id.re_pwd)
	private EditText mRePwdEt;
	@ViewInject(R.id.verification_code)
	private EditText mVerCodeEt;
	@ViewInject(R.id.country)
	private TextView mCountryTv;
	@ViewInject(R.id.get_vercode)
	private Button mGetVercodeBtn;

	private String mPhoneNO, mNewPwd;

	private final int MIN_PWD_LEN = 6;
	private String mCountryNO;
	private List<Character> mCountrySortList = new ArrayList<Character>();
	private List<String> mCountryNameList = new ArrayList<String>();
	private List<String> mCountryAreaNOList = new ArrayList<String>();
	private TimeCount mTimeCount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		initTitle();
		SMSSDK.initSDK(this, Config.SMS_APPKEY, Config.SMS_APPSECRET);// 初始化SMS—SDK
		SMSSDK.registerEventHandler(mEventHandler);
		mTimeCount = new TimeCount(60 * 1000, 1000);// 构造CountDownTimer对象
		VerCodeReceivedListener.get().setOnVerCodeReceivedListener(this, new OnVerCodeReceivedListener() {
			@Override
			public void onVerCodeReceived(String vercode) {
				mVerCodeEt.setText(vercode);
			}
		});
	}

	private void initTitle() {
		TextView titleTv = (TextView) findViewById(R.id.title_bar).findViewById(R.id.title);
		titleTv.setText(R.string.find_pwd);
	}

	private EventHandler mEventHandler = new EventHandler() {
		@Override
		public void afterEvent(int event, int result, Object data) {
			if (result == SMSSDK.RESULT_COMPLETE) {
				if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {// 验证码验证成功
					resetPwd();
				} else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {// 验证码已发送
					MainThreadHandler.makeLongToast(getString(R.string.verification_code_have_sent_to_phone) + ":【"
							+ mPhoneNO + "】");
				} else {
					MainThreadHandler.makeToast(R.string.verification_code_wrong);
				}
			} else {
				MainThreadHandler.makeToast(R.string.something_wrong);
				VLog.d("test", (Throwable) data);
			}
		}
	};
	
	private void resetPwd() {
		MainThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				final LoadingDlg dlg = new LoadingDlg(FindPwdActivity.this, R.string.resetting_pwd);
				dlg.show();
				ThreadPool.execute(new Runnable() {
					@Override
					public void run() {
						try {
							HttpManager.get().changePwd(mPhoneNO, mNewPwd);
							dlg.dismiss();
							MainThreadHandler.makeToast(R.string.pwd_reset_success);
							FindPwdActivity.this.startActivity(new Intent(FindPwdActivity.this,
									LoginActivity.class));
							FindPwdActivity.this.finish();
						} catch (VidException e) {
							VLog.e("test", e);
							dlg.dismiss();
							if (e != null) {
								if (e instanceof VHttpException) {
									VHttpException httpE = (VHttpException) e;
									switch (httpE.getCode()) {
									case VHttpException.ERR_CODE_USER_NOT_EXISTS:
										MainThreadHandler.makeToast(R.string.account_not_exist);
										break;
									default:
										MainThreadHandler.makeToast(R.string.remote_server_error);
										break;
									}
								}
							}
						}
					}
				});
			}
		});
	}

	@OnClick({ R.id.back, R.id.choose_country, R.id.get_vercode, R.id.submit })
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			startActivity(new Intent(this, LoginActivity.class));
			finish();
			break;
		case R.id.choose_country:
//			LoadingDlg loadingDlg = new LoadingDlg(this, R.string.loading);
//			loadingDlg.show();
//			HashMap<Character, ArrayList<String[]>> rawData = SMSSDK.getGroupedCountryList();
//			for (Character ch : rawData.keySet()) {
//				List<String[]> rawCountryList = rawData.get(ch);
//				for (String[] arr : rawCountryList) {
//					mCountrySortList.add(ch);
//					mCountryNameList.add(arr[0]);
//					mCountryAreaNOList.add(arr[1]);
//				}
//			}
//			CountryListAdapter adapter = new CountryListAdapter(mCountrySortList, mCountryNameList, mCountryAreaNOList);
//			loadingDlg.dismiss();
//			new CountryListDlg(this, adapter, mDialogClickListener).show();
			break;
		case R.id.get_vercode:
			mPhoneNO = mAccountEt.getText().toString();
			if (TextUtils.isEmpty(mPhoneNO)) {
				VidUtil.setWarnText(mAccountEt, getString(R.string.input_account));
			} else if (!VidUtil.isPhoneNO(mPhoneNO)) {
				VidUtil.setWarnText(mAccountEt, getString(R.string.phone_format_err));
			} else {
				if (TextUtils.isEmpty(mCountryNO)) {
					mCountryNO = "86";
				}
				VidUtil.openSmsPermissionDlgIfNeed(this);
				SMSSDK.getVerificationCode(mCountryNO, mPhoneNO);// 请求获取短信验证码
				mGetVercodeBtn.setClickable(false);
				mTimeCount.start();
			}
			break;
		case R.id.submit:
			mPhoneNO = mAccountEt.getText().toString();
			mNewPwd = mNewPwdEt.getText().toString();
			String rePwd = mRePwdEt.getText().toString();
			String verCode = mVerCodeEt.getText().toString();
			if (TextUtils.isEmpty(mPhoneNO)) {
				VidUtil.setWarnText(mAccountEt, getString(R.string.input_account));
			} else if (!VidUtil.isPhoneNO(mPhoneNO)) {
				VidUtil.setWarnText(mAccountEt, getString(R.string.phone_format_err));
			} else if (TextUtils.isEmpty(mNewPwd)) {
				VidUtil.setWarnText(mNewPwdEt, getString(R.string.input_new_pwd));
			} else if (mNewPwd.length() < MIN_PWD_LEN) {
				VidUtil.setWarnText(mNewPwdEt, getString(R.string.pwd_len_err));
			} else if (TextUtils.isEmpty(rePwd)) {
				VidUtil.setWarnText(mRePwdEt, getString(R.string.input_re_pwd));
			} else if (!mNewPwd.equals(rePwd)) {
				VidUtil.setWarnText(mRePwdEt, getString(R.string.second_pwd_not_equal));
			} else if (TextUtils.isEmpty(verCode)) {
				VidUtil.setWarnText(mVerCodeEt, getString(R.string.input_sms_verification_code));
			} else {
				if (Config.DEBUG_UNCHECK_VERCODE) {
					resetPwd();
					return;
				}
				SMSSDK.submitVerificationCode(mCountryNO, mPhoneNO, verCode);
			}
			break;
		}
	}

	private DialogClickListener mDialogClickListener = new DialogClickListener() {
		public void onOK(Bundle bundle) {
			super.onOK(bundle);
			mCountryNO = bundle.getString(ExtraConst.EXTRA_COUNTRY_AREA_NO);
			String country = bundle.getString(ExtraConst.EXTRA_COUNTRY_NAME);
			mCountryTv.setText(country);
		};
	};

	/* 定义一个倒计时的内部类 */
	private class TimeCount extends CountDownTimer {
		public TimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
		}

		@Override
		public void onFinish() {// 计时完毕时触发
			mGetVercodeBtn.setText(R.string.resend_verify_code);
			mGetVercodeBtn.setTextColor(Color.WHITE);
			mGetVercodeBtn.setClickable(true);
		}

		@Override
		public void onTick(long millisUntilFinished) {// 计时过程显示
			mGetVercodeBtn.setClickable(false);
			mGetVercodeBtn.setTextColor(getResources().getColor(R.color.dark_grey));
			mGetVercodeBtn.setText(App.get().getString(R.string.resend_verify_code) + "(" + millisUntilFinished / 1000
					+ ")");
		}
	}

	@Override
	protected void onDestroy() {
		mTimeCount.cancel();
		SMSSDK.unregisterEventHandler(mEventHandler);
		VerCodeReceivedListener.get().removeOnVerCodeReceivedListener();
		super.onDestroy();
	}

}
