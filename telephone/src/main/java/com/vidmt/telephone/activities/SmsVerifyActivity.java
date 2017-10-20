package com.vidmt.telephone.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.acmn.utils.andr.FLog;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.telephone.App;
import com.vidmt.telephone.Config;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.listeners.VerCodeReceivedListener;
import com.vidmt.telephone.listeners.VerCodeReceivedListener.OnVerCodeReceivedListener;
import com.vidmt.telephone.tasks.LogRegTask;
import com.vidmt.telephone.utils.VidUtil;

/**
 * 注册页的验证（验证码）页
 */
@ContentView(R.layout.activity_sms_verify)
public class SmsVerifyActivity extends AbsVidActivity {
	@ViewInject(R.id.phone)
	private TextView mPhoneTv;
	@ViewInject(R.id.verify_code)
	private EditText mVerifyCodeEt;
	@ViewInject(R.id.resend)
	private Button mResendBtn;
	private String mNick, mPhoneNO, mPwd, mCountryNO;
	private Parcelable mPhotoParcel;
	private TimeCount mTimeCount;
	private boolean register_no_code = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		initTitle();
		SMSSDK.registerEventHandler(mEventHandler);

		mNick = getIntent().getStringExtra(ExtraConst.EXTRA_NICKNAME);
		mPhoneNO = getIntent().getStringExtra(ExtraConst.EXTRA_PHONE_NO);
		mPwd = getIntent().getStringExtra(ExtraConst.EXTRA_PWD);
		mCountryNO = getIntent().getStringExtra(ExtraConst.EXTRA_COUNTRY_AREA_NO);
		mPhotoParcel = getIntent().getParcelableExtra(ExtraConst.EXTRA_PHOTO_PARCEL);

		mPhoneTv.setText("(+" + mCountryNO + ")" + mPhoneNO);

		mTimeCount = new TimeCount(60 * 1000, 1000);// 构造CountDownTimer对象
		register_no_code = false;
		mTimeCount.start();
		VerCodeReceivedListener.get().setOnVerCodeReceivedListener(this, new OnVerCodeReceivedListener() {
			@Override
			public void onVerCodeReceived(String vercode) {
				mVerifyCodeEt.setText(vercode);
			}
		});
	}

	private void initTitle() {
		TextView titleTv = (TextView) findViewById(R.id.title_bar).findViewById(R.id.title);
		titleTv.setText(R.string.verify_phone);
		initReconnectView(findViewById(R.id.reconnect_layout));
	}

	private EventHandler mEventHandler = new EventHandler() {
		@Override
		public void afterEvent(int event, int result, Object data) {
			if (result == SMSSDK.RESULT_COMPLETE) {
				if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {// 验证码验证成功
					register();
				} else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {// 验证码已发送
					MainThreadHandler.makeToast(R.string.verify_code_have_sent);
					mTimeCount.start();
				} else {
					MainThreadHandler.makeToast(R.string.verification_code_wrong);
				}
			} else {
				// {"detail":"无效验证码","status":520,"description":"验证码错误"}
				Throwable t = (Throwable) data;
//				String jsonMsg = t.getMessage();
//				JSONObject jsonObj = JSON.parseObject(jsonMsg);
//				int status = jsonObj.getInteger("status");
//				String errDesc = jsonObj.getString("description");
				MainThreadHandler.makeToast("短信验证异常，请重新注册！");
				VidUtil.fLog("test", (Throwable) data);
			}
		}
	};
	
	private void register() {
		MainThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				Bundle bundle = new Bundle();
				if (mPhotoParcel != null) {
					bundle.putParcelable(ExtraConst.EXTRA_PHOTO_PARCEL, mPhotoParcel);
				}
				bundle.putString(ExtraConst.EXTRA_NICKNAME, mNick);
				new LogRegTask(SmsVerifyActivity.this, mPhoneNO, mPwd, bundle).execute();
			}
		});
	}

	@OnClick({ R.id.back, R.id.resend, R.id.register })
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			startActivity(new Intent(this, RegisterActivity.class));
			finish();
			break;
		case R.id.resend:
			SMSSDK.getVerificationCode(mCountryNO, mPhoneNO);// 请求获取短信验证码
			break;
		case R.id.register:
			String verifyCode = mVerifyCodeEt.getText().toString();
			if(register_no_code == true){
				register();
				return;
			}
			if (TextUtils.isEmpty(verifyCode)) {
				MainThreadHandler.makeToast(R.string.verify_code_cant_empty);
				return;
			}
			if (Config.DEBUG_UNCHECK_VERCODE) {// 测试模式，随便输入验证码，即可注册
				register();
				return;
			}
			SMSSDK.submitVerificationCode(mCountryNO, mPhoneNO, verifyCode);
			break;
		}
	}

	/* 定义一个倒计时的内部类 */
	private class TimeCount extends CountDownTimer {
		public TimeCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);// 参数依次为总时长,和计时的时间间隔
		}

		@Override
		public void onFinish() {// 计时完毕时触发
			//change for huawei store;
			//here for other store

//			mVerifyCodeEt.setText("8888");
//			mResendBtn.setText(R.string.register_no_code);
//			register_no_code = true;
//			mResendBtn.setTextColor(getResources().getColor(R.color.dark_grey));
//			mResendBtn.setClickable(false);

			//here for other store end

			//here for huawei store
			mResendBtn.setText(R.string.resend_verify_code);
			mResendBtn.setTextColor(Color.WHITE);
			mResendBtn.setClickable(true);
			//here for huawei store end;
		}

		@Override
		public void onTick(long millisUntilFinished) {// 计时过程显示
			mResendBtn.setClickable(false);
			mResendBtn.setTextColor(getResources().getColor(R.color.dark_grey));
			mResendBtn.setText(App.get().getString(R.string.resend_verify_code) + "(" + millisUntilFinished / 1000
					+ ")");
		}
	}

	@Override
	protected void onDestroy() {
		mTimeCount.cancel();
		SMSSDK.unregisterEventHandler(mEventHandler);
		super.onDestroy();
	}

}
