package com.vidmt.telephone.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.utils.Enums.VipType;
import com.vidmt.telephone.utils.VipInfoUtil;

/**
 * 选择充值vip页（年或者临时会员）
 */
@ContentView(R.layout.activity_vip_center)
public class VipCenterActivity extends AbsVidActivity {
	// @ViewInject(R.id.vip_year)
	// private LinearLayout mVipYearLayout;
	// @ViewInject(R.id.vip_tmp)
	// private LinearLayout mVipTmpLayout;
	@ViewInject(R.id.vip_tmp_tv)
	private TextView mVipTmpTv;
	@ViewInject(R.id.vip_year_ttl)
	private TextView mVipYearTtlTv;
	@ViewInject(R.id.vip_tmp_ttl)
	private TextView mVipTmpTtlTv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		initTitle();
		mVipTmpTv.setText(getString(R.string.vip_temp) + "：" + VipInfoUtil.getLvl(VipType.TRY).getExpire()
				+ getString(R.string.day));
	}

	@Override
	protected void onResume() {
		super.onResume();
		initData();
	}

	private void initTitle() {
		TextView titleTv = (TextView) findViewById(R.id.title);
		titleTv.setText(R.string.vip_center);
		initReconnectView(findViewById(R.id.reconnect_layout));
	}

	private void initData() {
		User curUser = AccManager.get().getCurUser();
		if (curUser.isVip()) {// 是会员
			int leftDays = curUser.getLeftDays();
			if (curUser.vipType.equals(VipType.YEAR.name())) {
				// mVipYearLayout.setBackgroundColor(getResources().getColor(R.color.theme_blue));
				mVipYearTtlTv.setVisibility(View.VISIBLE);
				//huawei change;
				mVipTmpTtlTv.setVisibility(View.INVISIBLE);
				if (leftDays == -1) {
					mVipYearTtlTv.setText(R.string.vip_expired);
				} else {
					mVipYearTtlTv.setText(getString(R.string.left_days, leftDays + ""));
				}
			} else if (curUser.vipType.equals(VipType.TRY.name())) {
				// mVipTmpLayout.setBackgroundColor(getResources().getColor(R.color.theme_blue));
				mVipTmpTtlTv.setVisibility(View.VISIBLE);
				//huawei change;
				mVipYearTtlTv.setVisibility(View.INVISIBLE);
				if (leftDays == -1) {
					mVipTmpTtlTv.setText(R.string.vip_expired);
				} else {
					mVipTmpTtlTv.setText(getString(R.string.left_days, leftDays + ""));
				}
			}
		}
	}

	@OnClick({ R.id.back, R.id.vip_year, R.id.vip_tmp })
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.vip_year:
			Intent yi = new Intent(this, VipInfoActivity.class);
			yi.putExtra(ExtraConst.EXTRA_IS_VIP_FULL_YEAR, true);
			startActivity(yi);
			break;
		case R.id.vip_tmp:
			Intent ti = new Intent(this, VipInfoActivity.class);
			ti.putExtra(ExtraConst.EXTRA_IS_VIP_FULL_YEAR, false);
			startActivity(ti);
			break;
		}
	}

}
