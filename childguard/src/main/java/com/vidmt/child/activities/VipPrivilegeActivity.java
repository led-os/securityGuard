package com.vidmt.child.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.child.R;
import com.vidmt.child.ui.adapters.VipInfoGridViewAdapter;
import com.vidmt.child.utils.UserUtil;
import com.vidmt.child.vos.LvlVo;
import com.vidmt.xmpp.enums.XmppEnums.LvlType;

import java.util.ArrayList;
import java.util.List;

@ContentView(R.layout.activity_vip_privilege)
public class VipPrivilegeActivity extends AbsVidActivity {
	@ViewInject(R.id.title)
	private TextView mTitleTv;
	@ViewInject(R.id.vip_type)
	private TextView mVipTypeTv;
	@ViewInject(R.id.gv)
	private GridView mGv;
	private VipInfoGridViewAdapter mAdapter;
	private Bundle mBundle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		initTitle();
		initPrivilege();
	}

	private void initTitle() {
		mTitleTv.setText(R.string.vip_center);
		initReconnectView(findViewById(R.id.reconnect_layout));
	}

	private void initPrivilege() {
		LvlVo lvl;
		mBundle = getIntent().getExtras();
		if (mBundle == null) {
			lvl = UserUtil.getLvl();
		} else {
			lvl = UserUtil.bundle2Lvl(mBundle);
		}
		if (lvl.code == LvlType.Cu) {
			mVipTypeTv.setText(R.string.vip_bronze);
		} else if (lvl.code == LvlType.Ag) {
			mVipTypeTv.setText(R.string.vip_silver);
		} else if (lvl.code == LvlType.Au) {
			mVipTypeTv.setText(R.string.vip_gold);
		}
		List<Integer> shownFuncIndexList = new ArrayList<Integer>();
		List<String> extraDataList = new ArrayList<String>();
		if (lvl.remoteAudio) {
			shownFuncIndexList.add(0);
			extraDataList.add(null);
//		}
//		if (lvl.fenceNum != 0) {
			shownFuncIndexList.add(1);
			extraDataList.add(lvl.fenceNum + "");
//		}
//		if (lvl.nav) {
			shownFuncIndexList.add(2);
			extraDataList.add(null);
//		}
//		if (lvl.alarm) {
			shownFuncIndexList.add(3);
			extraDataList.add(null);
//		}
//		if (lvl.traceNum != 0) {
			shownFuncIndexList.add(4);
			extraDataList.add(lvl.traceNum + "");
//		}
//		if (lvl.hideIcon) {
			shownFuncIndexList.add(5);
			extraDataList.add(null);
//		}
//		if (lvl.sport) {
			shownFuncIndexList.add(6);
			extraDataList.add(null);
//		}
//		if (lvl.noAd) {
			shownFuncIndexList.add(7);
			extraDataList.add(null);
		}
		mAdapter = new VipInfoGridViewAdapter(this, shownFuncIndexList, extraDataList);
		mGv.setAdapter(mAdapter);
	}

	@OnClick({ R.id.back, R.id.be_vip })
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.be_vip:
			Intent intent = new Intent(this, PayActivity.class);
			if (mBundle != null) {
				intent.putExtras(mBundle);
			}
			startActivity(intent);
			finish();
			break;
		}
	}
}
