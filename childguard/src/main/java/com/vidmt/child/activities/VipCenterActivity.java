package com.vidmt.child.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.child.R;
import com.vidmt.child.dlgs.LoadingDlg;
import com.vidmt.child.managers.AccManager;
import com.vidmt.child.utils.UserUtil;
import com.vidmt.child.vos.LvlVo;
import com.vidmt.child.vos.UserVo;
import com.vidmt.xmpp.enums.XmppEnums.LvlType;
import com.vidmt.xmpp.exts.CgUserIQ;
import com.vidmt.xmpp.exts.LvlIQ;
import com.vidmt.xmpp.inner.XmppManager;
import com.vidmt.xmpp.listeners.OnIQExtReceivedListener;

import org.jivesoftware.smack.packet.IQ;

@ContentView(R.layout.activity_vip_center)
public class VipCenterActivity extends AbsVidActivity implements OnIQExtReceivedListener {
	@ViewInject(R.id.bronze)
	private LinearLayout mCuLayout;
	@ViewInject(R.id.silver)
	private LinearLayout mAgLayout;
	@ViewInject(R.id.gold)
	private LinearLayout mAuLayout;
	@ViewInject(R.id.cu_money)
	private TextView mCuMoneyTv;
	@ViewInject(R.id.ag_money)
	private TextView mAgMoneyTv;
	@ViewInject(R.id.au_money)
	private TextView mAuMoneyTv;
	@ViewInject(R.id.cu_day_left)
	private TextView mCuLeftDaysTv;
	@ViewInject(R.id.ag_day_left)
	private TextView mAgLeftDaysTv;
	@ViewInject(R.id.au_day_left)
	private TextView mAuLeftDaysTv;
	private String mPriceUnit;
	private LoadingDlg mLoadingDlg;
	private Bundle mCuBundle, mAgBundle, mAuBundle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);

		initTitle();
		mPriceUnit = getString(R.string.rmb);
		initMoney();
		XmppManager.get().addXmppListener(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		initCurLvlView();
	}

	private void initTitle() {
		TextView titleTv = (TextView) findViewById(R.id.title);
		titleTv.setText(R.string.vip_center);
		initReconnectView(findViewById(R.id.reconnect_layout));
	}

	private void initMoney() {
		mLoadingDlg = new LoadingDlg(this, R.string.loading);
		mLoadingDlg.show();
		LvlVo lvl = UserUtil.getLvl();
		LvlType code = lvl.code;
		String price = UserUtil.getPriceStr(lvl.price, false);
		String timeUnit = UserUtil.getExpireUnit(lvl.expire);
		try {
			if (code == LvlType.NONE) {
				initLvlInfo(LvlType.Cu);
				initLvlInfo(LvlType.Ag);
				initLvlInfo(LvlType.Au);
			} else if (code == LvlType.Cu) {
				initLvlInfo(LvlType.Ag);
				initLvlInfo(LvlType.Au);
				mCuMoneyTv.setText("￥" + price + mPriceUnit + "/" + timeUnit);
			} else if (code == LvlType.Ag) {
				initLvlInfo(LvlType.Cu);
				initLvlInfo(LvlType.Au);
				mAgMoneyTv.setText("￥" + price + mPriceUnit + "/" + timeUnit);
			} else if (code == LvlType.Au) {
				initLvlInfo(LvlType.Cu);
				initLvlInfo(LvlType.Ag);
				mAuMoneyTv.setText("￥" + price + mPriceUnit + "/" + timeUnit);
			}
			mLoadingDlg.dismiss();
		} catch (IllegalStateException e) {
			VLog.e("test", e);
		}
	}

	private void initLvlInfo(LvlType lvlType) {
		LvlIQ lvlIq = AccManager.get().getLvlInfo(lvlType);
		LvlType code = lvlIq.code;
		final String price = UserUtil.getPriceStr(lvlIq.price, false);
		final String timeUnit = UserUtil.getExpireUnit(lvlIq.expire);
		if (code == LvlType.Cu) {
			MainThreadHandler.post(new Runnable() {
				@Override
				public void run() {
					mCuMoneyTv.setText("￥" + price + mPriceUnit + "/" + timeUnit);
				}
			});
			mCuBundle = UserUtil.lvlIq2Bundle(lvlIq);
		} else if (code == LvlType.Ag) {
			MainThreadHandler.post(new Runnable() {
				@Override
				public void run() {
					mAgMoneyTv.setText("￥" + price + mPriceUnit + "/" + timeUnit);
				}
			});
			mAgBundle = UserUtil.lvlIq2Bundle(lvlIq);
		} else if (code == LvlType.Au) {
			MainThreadHandler.post(new Runnable() {
				@Override
				public void run() {
					mAuMoneyTv.setText("￥" + price + mPriceUnit + "/" + timeUnit);
				}
			});
			mAuBundle = UserUtil.lvlIq2Bundle(lvlIq);
		}
	}

	private void initCurLvlView() {
		UserVo user = UserUtil.getParentInfo();
		if (user.code == LvlType.NONE || UserUtil.isExpired()) {
			return;
		}
		String leftDaysStr = "【" + getString(R.string.left) + ":" + UserUtil.getLeftDays() + getString(R.string.day) + "】";
		if (user.code == LvlType.Cu) {
			mCuLayout.setBackgroundResource(R.drawable.selector_green_pink);
			mCuLeftDaysTv.setText(leftDaysStr);
			mCuLeftDaysTv.setVisibility(View.VISIBLE);
			mAgLayout.setBackgroundResource(R.drawable.selector_white_green);
			mAuLayout.setBackgroundResource(R.drawable.selector_white_green);
			mAgLeftDaysTv.setVisibility(View.GONE);
			mAuLeftDaysTv.setVisibility(View.GONE);
		} else if (user.code == LvlType.Ag) {
			mAgLayout.setBackgroundResource(R.drawable.selector_green_pink);
			mAgLeftDaysTv.setText(leftDaysStr);
			mAgLeftDaysTv.setVisibility(View.VISIBLE);
			mCuLayout.setBackgroundResource(R.drawable.selector_white_green);
			mAuLayout.setBackgroundResource(R.drawable.selector_white_green);
			mCuLeftDaysTv.setVisibility(View.GONE);
			mAuLeftDaysTv.setVisibility(View.GONE);
		} else if (user.code == LvlType.Au) {
			mAuLayout.setBackgroundResource(R.drawable.selector_green_pink);
			mAuLeftDaysTv.setText(leftDaysStr);
			mAuLeftDaysTv.setVisibility(View.VISIBLE);
			mCuLayout.setBackgroundResource(R.drawable.selector_white_green);
			mAgLayout.setBackgroundResource(R.drawable.selector_white_green);
			mCuLeftDaysTv.setVisibility(View.GONE);
			mAgLeftDaysTv.setVisibility(View.GONE);
		}
	}

	@OnClick({ R.id.back, R.id.bronze, R.id.silver, R.id.gold })
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.bronze:
			Intent cuI = new Intent(this, VipPrivilegeActivity.class);
			if (mCuBundle != null) {
				cuI.putExtras(mCuBundle);
			}
			startActivity(cuI);
			break;
		case R.id.silver:
			Intent agI = new Intent(this, VipPrivilegeActivity.class);
			if (mAgBundle != null) {
				agI.putExtras(mAgBundle);
			}
			startActivity(agI);
			break;
		case R.id.gold:
			Intent auI = new Intent(this, VipPrivilegeActivity.class);
			if (mAuBundle != null) {
				auI.putExtras(mAuBundle);
			}
			startActivity(auI);
			break;
		}
	}

	@Override
	public void processIQExt(IQ iqExt) {
		if (iqExt instanceof CgUserIQ) {
			final CgUserIQ upgradedUserIq = (CgUserIQ) iqExt;
			if (upgradedUserIq.jid == null && upgradedUserIq.nick == null && upgradedUserIq.avatarUri == null) {
				MainThreadHandler.post(new Runnable() {
					@Override
					public void run() {
						mLoadingDlg.show();
					}
				});
				LvlIQ lvlIq = UserUtil.getParentInfo().code == upgradedUserIq.code ? null : AccManager.get().getLvlInfo(
						upgradedUserIq.code);
				UserUtil.upgradeLvl(upgradedUserIq, lvlIq);
				mLoadingDlg.dismiss();
				MainThreadHandler.post(new Runnable() {
					@Override
					public void run() {
						initCurLvlView();
					}
				});
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		XmppManager.get().removeXmppListener(this);
	}

}
