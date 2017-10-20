package com.vidmt.telephone.activities;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.PrefKeyConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.dlgs.AgeFilterDlg;
import com.vidmt.telephone.dlgs.BaseDialog.DialogClickListener;

/**
 * 选择筛选条件页
 */
@ContentView(R.layout.activity_filter_nearby)
public class FilterNearbyActivity extends AbsVidActivity {
	public static final int RES_CODE = 1;
	public static final String GENDER_FEMALE = "F";
	public static final String GENDER_MALE = "M";
	public static final int TIME_1 = 30 * 60 * 1000;
	public static final int TIME_2 = 1 * 60 * 60 * 1000;
	public static final int TIME_3 = 4 * 60 * 60 * 1000;
	public static final int AGE_RANGE_1_CODE = 0x11;
	public static final int AGE_RANGE_2_CODE = 0x22;
	public static final int AGE_RANGE_3_CODE = 0x33;
	public static final int AGE_RANGE_4_CODE = 0x44;
	public static final int AGE_RANGE_1_START = 18;
	public static final int AGE_RANGE_1_END = 22;
	public static final int AGE_RANGE_3_START = 27;
	public static final int AGE_RANGE_3_END = 35;

	@ViewInject(R.id.female)
	private RadioButton mFemaleRb;
	@ViewInject(R.id.male)
	private RadioButton mMaleRb;
	@ViewInject(R.id.gender_all)
	private RadioButton mGenderAllRb;
	@ViewInject(R.id.time_1)
	private RadioButton mTime1Rb;
	@ViewInject(R.id.time_2)
	private RadioButton mTime2Rb;
	@ViewInject(R.id.time_3)
	private RadioButton mTime3Rb;
	@ViewInject(R.id.time_all)
	private RadioButton mTimeAllRb;
	@ViewInject(R.id.age_tv)
	private TextView mAgeTv;
	private List<String> mAgeStrList;
	private String mGender;
	private int mTime = -1;
	private int mAgeCode = -1, mAgeIndex = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		initTitle();
		initData();
	}

	private void initTitle() {
		TextView titleTv = (TextView) findViewById(R.id.title_bar).findViewById(R.id.title);
		titleTv.setText(R.string.filter_nearby);
		Button backBtn = (Button) findViewById(R.id.back);
		backBtn.setBackgroundResource(R.drawable.selector_title_btn);
		backBtn.setTextColor(Color.WHITE);
		backBtn.setText(R.string.cancel);
		Button rightBtn = (Button) findViewById(R.id.right);
		rightBtn.setText(R.string.confirm);
		initReconnectView(findViewById(R.id.reconnect_layout));
	}

	private void initData() {
		mGender = SysUtil.getPref(PrefKeyConst.RPEF_FILTER_GENDER);
		mTime = SysUtil.getIntPref(PrefKeyConst.RPEF_FILTER_TIME);
		mAgeCode = SysUtil.getIntPref(PrefKeyConst.RPEF_FILTER_AGE_CODE);
		if ("M".equals(mGender)) {
			mMaleRb.setChecked(true);
		} else if ("F".equals(mGender)) {
			mFemaleRb.setChecked(true);
		} else {
			mGenderAllRb.setChecked(true);
		}
		switch (mTime) {
		case TIME_1:
			mTime1Rb.setChecked(true);
			break;
		case TIME_2:
			mTime2Rb.setChecked(true);
			break;
		case TIME_3:
			mTime3Rb.setChecked(true);
			break;
		case -1:
			mTimeAllRb.setChecked(true);
			break;
		}
		switch (mAgeCode) {
		case AGE_RANGE_1_CODE:
			mAgeIndex = 1;
			break;
		case AGE_RANGE_2_CODE:
			mAgeIndex = 2;
			break;
		case AGE_RANGE_3_CODE:
			mAgeIndex = 3;
			break;
		case AGE_RANGE_4_CODE:
			mAgeIndex = 4;
			break;
		case -1:
			mAgeIndex = 0;
			break;
		}
		mAgeStrList = initAgeStrList();
		mAgeTv.setText(mAgeStrList.get(mAgeIndex));
	}

	@OnClick({ R.id.back, R.id.age, R.id.right })
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finishSelf();
			break;
		case R.id.age:
			new AgeFilterDlg(this, mAgeStrList, mAgeIndex, new DialogClickListener() {
				@Override
				public void onOK(Bundle bundle) {
					super.onOK(bundle);
					int position = bundle.getInt(ExtraConst.EXTRA_POSITION, -1);
					mAgeTv.setText(mAgeStrList.get(position));
					mAgeIndex = position;
				};
			}).show();
			break;
		case R.id.right:
			// gender
			if (mFemaleRb.isChecked()) {
				mGender = "F";
			} else if (mMaleRb.isChecked()) {
				mGender = "M";
			} else {
				mGender = null;
			}
			if (mGender != null) {
				SysUtil.savePref(PrefKeyConst.RPEF_FILTER_GENDER, mGender);
			} else {
				SysUtil.removePref(PrefKeyConst.RPEF_FILTER_GENDER);
			}
			// time
			if (mTime1Rb.isChecked()) {
				mTime = TIME_1;
			} else if (mTime2Rb.isChecked()) {
				mTime = TIME_2;
			} else if (mTime3Rb.isChecked()) {
				mTime = TIME_3;
			} else {
				mTime = -1;
			}
			if (mTime != -1) {
				SysUtil.savePref(PrefKeyConst.RPEF_FILTER_TIME, mTime);
			} else {
				SysUtil.removePref(PrefKeyConst.RPEF_FILTER_TIME);
			}
			// age
			switch (mAgeIndex) {
			case 1:
				mAgeCode = AGE_RANGE_1_CODE;
				break;
			case 2:
				mAgeCode = AGE_RANGE_2_CODE;
				break;
			case 3:
				mAgeCode = AGE_RANGE_3_CODE;
				break;
			case 4:
				mAgeCode = AGE_RANGE_4_CODE;
				break;
			case 0:
				mAgeCode = -1;
				break;
			}
			if (mAgeCode != -1) {
				SysUtil.savePref(PrefKeyConst.RPEF_FILTER_AGE_CODE, mAgeCode);
			} else {
				SysUtil.removePref(PrefKeyConst.RPEF_FILTER_AGE_CODE);
			}

			Intent intent = new Intent();
			intent.putExtra(ExtraConst.EXTRA_FILTER_GENDER, mGender);
			intent.putExtra(ExtraConst.EXTRA_FILTER_TIME, mTime);
			intent.putExtra(ExtraConst.EXTRA_FILTER_AGE_CODE, mAgeCode);
			setResult(RES_CODE, intent);
			finish();
			break;
		}
	}

	private List<String> initAgeStrList() {
		List<String> list = new ArrayList<String>();
		list.add(getString(R.string.unlimitted));
		list.add(getString(R.string._age, AGE_RANGE_1_START + "-" + AGE_RANGE_1_END));
		list.add(getString(R.string._age, (AGE_RANGE_1_END + 1) + "-" + (AGE_RANGE_3_START - 1)));
		list.add(getString(R.string._age, AGE_RANGE_3_START + "-" + AGE_RANGE_3_END));
		list.add(getString(R.string.age_above, AGE_RANGE_3_END));
		return list;
	}

	private void finishSelf() {
		finish();
		overridePendingTransition(0, R.anim.act_close_from_top);
	}

	@Override
	public void onBackPressed() {
		finishSelf();
	}
}
