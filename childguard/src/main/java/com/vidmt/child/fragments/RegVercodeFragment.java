package com.vidmt.child.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.mob.MobSDK;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.child.App;
import com.vidmt.child.Config;
import com.vidmt.child.Const;
import com.vidmt.child.R;
import com.vidmt.child.activities.DisclaimerActivity;
import com.vidmt.child.activities.FindPwdActivity;
import com.vidmt.child.activities.LoginActivity;
import com.vidmt.child.activities.RegisterActivity;
import com.vidmt.child.dlgs.BaseDialog.DialogClickListener;
import com.vidmt.child.dlgs.LoadingDlg;
import com.vidmt.child.dlgs.PhoneNODlg;
import com.vidmt.child.utils.VidUtil;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class RegVercodeFragment extends Fragment implements OnClickListener {
	private EditText mAccountEt;
	private EditText mVercodeEt;
	private Button mGetVercodeBtn;
	private String mPhoneNO;
	private TimeCount mTimeCount;
	private LoadingDlg mLoadingDlg;
	private EventHandler mEventHandler = new EventHandler() {
		@Override
		public void afterEvent(int event, int result, Object data) {
			if (mLoadingDlg != null) {
				mLoadingDlg.dismiss();
			}
			if (result == SMSSDK.RESULT_COMPLETE) {
				if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {// 验证码验证成功
					RegisterActivity activity = (RegisterActivity) getActivity();
					activity.setAccount(mPhoneNO);
					activity.showFrag(RegisterActivity.FRAG_PWD_INDEX);
				} else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {// 验证码已发送
					MainThreadHandler.makeToast(R.string.verify_code_have_sent);
					mTimeCount.start();
				} else {
					MainThreadHandler.makeToast(R.string.verification_code_wrong);
				}
			} else {
				MainThreadHandler.makeToast(R.string.verification_code_wrong);
				VLog.d("test", (Throwable) data);
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.frag_reg_vercode, null, false);
		mAccountEt = (EditText) view.findViewById(R.id.account);
		mVercodeEt = (EditText) view.findViewById(R.id.vercode);
		mGetVercodeBtn = (Button) view.findViewById(R.id.get_vercode);
		mGetVercodeBtn.setOnClickListener(this);
		view.findViewById(R.id.login).setOnClickListener(this);
		view.findViewById(R.id.find_pwd).setOnClickListener(this);
		view.findViewById(R.id.next).setOnClickListener(this);
		view.findViewById(R.id.disclaimer).setOnClickListener(this);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		MobSDK.init(getActivity(),Config.SMS_APPKEY, Config.SMS_APPSECRET);
//		SMSSDK.initSDK(getActivity(), Config.SMS_APPKEY, Config.SMS_APPSECRET);// 初始化SMS—SDK
		SMSSDK.registerEventHandler(mEventHandler);
		mTimeCount = new TimeCount(60 * 1000, 1000);// 构造CountDownTimer对象
		mLoadingDlg = new LoadingDlg(getActivity(), R.string.loading);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login:
			startActivity(new Intent(getActivity(), LoginActivity.class));
			break;
		case R.id.find_pwd:
			startActivity(new Intent(getActivity(), FindPwdActivity.class));
			break;
		case R.id.get_vercode:
			mPhoneNO = mAccountEt.getText().toString();
			if (TextUtils.isEmpty(mPhoneNO)) {
				VidUtil.setWarnText(mAccountEt, getString(R.string.input_account));
			} else if (!VidUtil.isPhoneNO(mPhoneNO)) {
				VidUtil.setWarnText(mAccountEt, getString(R.string.phone_format_err));
			} else {
				PhoneNODlg dlg = new PhoneNODlg(getActivity(), mPhoneNO);
				dlg.setOnClickListener(new DialogClickListener() {
					@Override
					public void onOK() {
						super.onOK();
						SMSSDK.getVerificationCode(Const.PHONE_COUNTRY_NO, mPhoneNO);// 请求获取短信验证码
						mGetVercodeBtn.setClickable(false);
						mTimeCount.start();
					}
				});
				dlg.show();
			}
			break;
		case R.id.next:
			mPhoneNO = mAccountEt.getText().toString();
			String vercode = mVercodeEt.getText().toString();
			if (TextUtils.isEmpty(mPhoneNO)) {
				VidUtil.setWarnText(mAccountEt, getString(R.string.input_account));
			} else if (!VidUtil.isPhoneNO(mPhoneNO)) {
				VidUtil.setWarnText(mAccountEt, getString(R.string.phone_format_err));
			} else if (TextUtils.isEmpty(vercode)) {
				VidUtil.setWarnText(mVercodeEt, getString(R.string.input_sms_verification_code));
			} else {
				SMSSDK.submitVerificationCode(Const.PHONE_COUNTRY_NO, mPhoneNO, vercode);
				mLoadingDlg.show();
			}
			RegisterActivity activity = (RegisterActivity) getActivity();
			activity.setAccount(mPhoneNO);
			activity.showFrag(RegisterActivity.FRAG_PWD_INDEX);
			break;
		case R.id.disclaimer:
			startActivity(new Intent(getActivity(), DisclaimerActivity.class));
			break;
		}
	}

	@Override
	public void onDestroy() {
		mTimeCount.cancel();
		SMSSDK.unregisterEventHandler(mEventHandler);
		super.onDestroy();
	}

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
			mGetVercodeBtn.setTextColor(App.get().getResources().getColor(R.color.dark_grey));
			mGetVercodeBtn.setText(App.get().getString(R.string.resend_verify_code) + "(" + millisUntilFinished / 1000
					+ ")");
		}
	}

}
