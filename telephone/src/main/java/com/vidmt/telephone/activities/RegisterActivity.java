package com.vidmt.telephone.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.smssdk.SMSSDK;

import com.vidmt.acmn.utils.andr.NetUtil;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.telephone.Config;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.PrefKeyConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.dlgs.BaseDialog.DialogClickListener;
import com.vidmt.telephone.dlgs.CountryListDlg;
import com.vidmt.telephone.dlgs.LoadingDlg;
import com.vidmt.telephone.dlgs.NetWarnDlg;
import com.vidmt.telephone.dlgs.PhoneNODlg;
import com.vidmt.telephone.tasks.LogRegTask;
import com.vidmt.telephone.ui.adapters.CountryListAdapter;
import com.vidmt.telephone.utils.AvatarUtil;
import com.vidmt.telephone.utils.VidUtil;

/**
 * 注册页
 */
public class RegisterActivity extends AbsVidActivity implements OnClickListener {
	private TextView mCountryTv;
	private EditText mNickEt;
	private EditText mAccountEt;
	private EditText mPwdEt;
	private ImageView mAvatarIv;
	private TextView mAvatarTagTv;
	private ImageView mEyeIv;

	private final int MIN_PWD_LEN = 6;

	private String mCountryNO;
	private List<Character> mCountrySortList = new ArrayList<Character>();
	private List<String> mCountryNameList = new ArrayList<String>();
	private List<String> mCountryAreaNOList = new ArrayList<String>();
	private Parcelable mPhotoParcel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		mCountryTv = (TextView) findViewById(R.id.country);
		mNickEt = (EditText) findViewById(R.id.nick);
		mAccountEt = (EditText) findViewById(R.id.account);
		mPwdEt = (EditText) findViewById(R.id.pwd);
		mEyeIv = (ImageView) findViewById(R.id.eye);
		mAvatarIv = (ImageView) findViewById(R.id.avatar);
		mAvatarTagTv = (TextView) findViewById(R.id.avatar_tag);
		findViewById(R.id.choose_country).setOnClickListener(this);
		findViewById(R.id.next).setOnClickListener(this);
		findViewById(R.id.back).setOnClickListener(this);
		findViewById(R.id.disclaimer).setOnClickListener(this);
		mAvatarIv.setOnClickListener(this);
		mEyeIv.setOnClickListener(this);

		SMSSDK.initSDK(this, Config.SMS_APPKEY, Config.SMS_APPSECRET);// 初始化SMS—SDK
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
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
		case R.id.next:
			final String nick = mNickEt.getText().toString();
			final String account = mAccountEt.getText().toString();
			final String pwd = mPwdEt.getText().toString();
			//huawei change;
			/*
			if (mPhotoParcel == null) {
				mAvatarTagTv.setTextColor(Color.RED);
				MainThreadHandler.makeToast(R.string.please_set_avatar);
			} else
			*/
			if (TextUtils.isEmpty(nick)) {
				VidUtil.setWarnText(mNickEt, getString(R.string.input_nick));
			} else if (!VidUtil.isNickFormat(nick)) {
				VidUtil.setWarnText(mNickEt, getString(R.string.nick_format_err));
			} else if (!VidUtil.isNickTooLong(nick)) {
				VidUtil.setWarnText(mNickEt, getString(R.string.nick_too_long));
			}else if (TextUtils.isEmpty(account)) {
				VidUtil.setWarnText(mAccountEt, getString(R.string.input_account));
			} else if (!VidUtil.isPhoneNO(account)) {
				VidUtil.setWarnText(mAccountEt, getString(R.string.phone_format_err));
			} else if (TextUtils.isEmpty(pwd)) {
				VidUtil.setWarnText(mPwdEt, getString(R.string.input_pwd));
			} else if (!VidUtil.isPwdFormat(pwd)) {
				VidUtil.setWarnText(mPwdEt, getString(R.string.pwd_format_err));
			} else if (pwd.length() < MIN_PWD_LEN) {
				VidUtil.setWarnText(mPwdEt, getString(R.string.pwd_len_err));
			} else {
				if (!NetUtil.isNetworkAvaiable()) {
					new NetWarnDlg(this).show();
					return;
				}
				Boolean booleanPref = SysUtil.getBooleanPref(PrefKeyConst.NEED_SMS_VERTIFY, true);
				if(!booleanPref){
					Bundle bundle = new Bundle();
					if (mPhotoParcel != null) {
						bundle.putParcelable(ExtraConst.EXTRA_PHOTO_PARCEL, mPhotoParcel);
					}
					bundle.putString(ExtraConst.EXTRA_NICKNAME, nick);
					new LogRegTask(RegisterActivity.this, account, pwd, bundle).execute();
				}else {
					PhoneNODlg dlg = new PhoneNODlg(this, account);
					dlg.setOnClickListener(new DialogClickListener() {
						@Override
						public void onOK() {
							super.onOK();
							if (TextUtils.isEmpty(mCountryNO)) {
								mCountryNO = "86";
							}
							SMSSDK.getVerificationCode(mCountryNO, account);// 请求获取短信验证码
							VidUtil.openSmsPermissionDlgIfNeed(RegisterActivity.this);
							Intent intent = new Intent(RegisterActivity.this, SmsVerifyActivity.class);
							if (mPhotoParcel != null) {
								intent.putExtra(ExtraConst.EXTRA_PHOTO_PARCEL, mPhotoParcel);
							}
							intent.putExtra(ExtraConst.EXTRA_NICKNAME, nick);
							intent.putExtra(ExtraConst.EXTRA_PHONE_NO, account);
							intent.putExtra(ExtraConst.EXTRA_PWD, pwd);
							intent.putExtra(ExtraConst.EXTRA_COUNTRY_AREA_NO, mCountryNO);
							RegisterActivity.this.startActivity(intent);
							RegisterActivity.this.finish();
						}
					});
					dlg.show();
				}

			}
			break;
		case R.id.back:
			startActivity(new Intent(this, LoginActivity.class));
			finish();
			break;
		case R.id.disclaimer:
			startActivity(new Intent(this, DisclaimerActivity.class));
			break;
		case R.id.eye:
			if (mEyeIv.getTag() == null) {
				mEyeIv.setImageResource(R.drawable.eye_checked);
				mEyeIv.setTag(new Object());// 非空tag
				mPwdEt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
			} else {
				mEyeIv.setImageResource(R.drawable.eye);
				mEyeIv.setTag(null);
				mPwdEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
			}
			break;
		case R.id.avatar:
			startActivityForResult(new Intent(this, ChoosePicActivity.class), 0);
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == ChoosePicActivity.ACT_RES_CODE) {
			Bundle bundle = data.getExtras();
			mPhotoParcel = bundle.getParcelable("data");
			Bitmap photo = AvatarUtil.toRoundCorner((Bitmap) mPhotoParcel);
			mAvatarIv.setImageBitmap(photo);
			mAvatarTagTv.setVisibility(View.GONE);
		}
	}

	private DialogClickListener mDialogClickListener = new DialogClickListener() {
		@Override
		public void onOK(Bundle bundle) {
			super.onOK(bundle);
			mCountryNO = bundle.getString(ExtraConst.EXTRA_COUNTRY_AREA_NO);
			String country = bundle.getString(ExtraConst.EXTRA_COUNTRY_NAME);
			mCountryTv.setText(country);
		}
	};

}
