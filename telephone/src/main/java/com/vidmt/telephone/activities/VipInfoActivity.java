package com.vidmt.telephone.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.ui.adapters.VipInfoGridViewAdapter;
import com.vidmt.telephone.utils.Enums.VipType;
import com.vidmt.telephone.utils.VipInfoUtil;

/**
 * 会员功能介绍页（GridView）
 */
@ContentView(R.layout.activity_vip_info)
public class VipInfoActivity extends AbsVidActivity {
	@ViewInject(R.id.gv)
	private GridView mGv;
	private boolean mIsFullYearVip;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		initTitle();
		VipInfoGridViewAdapter adapter = new VipInfoGridViewAdapter(this);
		mGv.setAdapter(adapter);
	}

	private void initTitle() {
		TextView titleTv = (TextView) findViewById(R.id.title);
		mIsFullYearVip = getIntent().getBooleanExtra(ExtraConst.EXTRA_IS_VIP_FULL_YEAR, false);
		String title = getString(R.string.vip_year);
		if (!mIsFullYearVip) {
			title = getString(R.string.vip_temp) + "(" + VipInfoUtil.getLvl(VipType.TRY).getExpire()
					+ getString(R.string.day) + ")";
		}
		titleTv.setText(title);
		initReconnectView(findViewById(R.id.reconnect_layout));
	}

	// @OnItemClick(R.id.gv)
	// private void onItemClick(AdapterView<?> parent, View view, int position,
	// long id) {
	// }

	@OnClick({ R.id.back, R.id.be_vip })
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.be_vip:
			Intent i = new Intent(this, PayActivity.class);
			i.putExtra(ExtraConst.EXTRA_IS_VIP_FULL_YEAR, mIsFullYearVip);
			startActivity(i);
			break;
		}
	}
}
