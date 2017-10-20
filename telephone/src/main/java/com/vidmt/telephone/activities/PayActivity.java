package com.vidmt.telephone.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.pay.alipay.AlipayController;
import com.vidmt.telephone.pay.mmpay.MMPayController;
import com.vidmt.telephone.pay.wxpay.WXPayController;
import com.vidmt.telephone.tasks.ServerConfInfoTask;
import com.vidmt.telephone.utils.Enums.VipType;
import com.vidmt.telephone.utils.VipInfoUtil;

/**
 * 选择支付页（微信或支付宝）
 */
@ContentView(R.layout.activity_pay)
public class PayActivity extends AbsVidActivity {
	@ViewInject(R.id.phone)
	private LinearLayout mPhoneLayout;
	@ViewInject(R.id.rb_alipay)
	private RadioButton mAlipayRb;
	@ViewInject(R.id.rb_wechat)
	private RadioButton mWeChatRb;
	@ViewInject(R.id.rb_phone)
	private RadioButton mPhoneRb;
	@ViewInject(R.id.vip_type)
	private TextView mVipTypeTv;
	@ViewInject(R.id.price)
	private TextView mPriceTv;
	private AlipayController mAlipayCtrl;
	private WXPayController mWXPayCtrl;
	private MMPayController mMMPayCtrl;
	private boolean mIsFullYearVip;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		initTitle();
		initPayUi();
		mIsFullYearVip = getIntent().getBooleanExtra(ExtraConst.EXTRA_IS_VIP_FULL_YEAR, false);
		if (mIsFullYearVip) {
			mVipTypeTv.setText(R.string.vip_year);
			mPriceTv.setText(VipInfoUtil.getLvl(VipType.YEAR).getMoney() + getString(R.string.rmb));
			mPhoneLayout.setVisibility(View.GONE);
		} else {
			mVipTypeTv.setText(getString(R.string.vip_temp) + ":" + VipInfoUtil.getLvl(VipType.TRY).getExpire()
					+ getString(R.string.day));
			mPriceTv.setText(VipInfoUtil.getLvl(VipType.TRY).getMoney() + getString(R.string.rmb));
		}
		mAlipayCtrl = new AlipayController(this);
		mWXPayCtrl = new WXPayController(this);
		mMMPayCtrl = MMPayController.get();
		mMMPayCtrl.init(this);
	}

	private void initPayUi() {
		//lihuichao change for pay 2017-6-2
		LinearLayout alipayLayout = (LinearLayout) findViewById(R.id.alipay);
		boolean canAlipay = ServerConfInfoTask.canAlipay();
		if(!canAlipay){
			alipayLayout.setVisibility(View.GONE);
			mWeChatRb.setChecked(true);
			mAlipayRb.setChecked(false);
		}
		LinearLayout weixinPayLayout = (LinearLayout) findViewById(R.id.wechat);
		boolean canWeixinPay = ServerConfInfoTask.canWeixinPay();
		if(!canWeixinPay){
			weixinPayLayout.setVisibility(View.GONE);
			mAlipayRb.setChecked(true);
			mWeChatRb.setChecked(false);
		}

	}

	private void initTitle() {
		View view = findViewById(R.id.title_bar);
		TextView titleTv = (TextView) view.findViewById(R.id.title);
		titleTv.setText(R.string.confirm_pay);
		initReconnectView(findViewById(R.id.reconnect_layout));
	}

	@OnClick({ R.id.back, R.id.alipay, R.id.wechat, R.id.phone, R.id.pay })
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.alipay:
			mAlipayRb.setChecked(true);
			mWeChatRb.setChecked(false);
			mPhoneRb.setChecked(false);
			break;
		case R.id.wechat:
			mAlipayRb.setChecked(false);
			mWeChatRb.setChecked(true);
			mPhoneRb.setChecked(false);
			break;
		case R.id.phone:
			mAlipayRb.setChecked(false);
			mWeChatRb.setChecked(false);
			mPhoneRb.setChecked(true);
			break;
		case R.id.pay:
			VipType vipType = null;
			if (mIsFullYearVip) {
				vipType = VipType.YEAR;
			} else {
				vipType = VipType.TRY;
			}
			if (mAlipayRb.isChecked()) {
				mAlipayCtrl.pay(vipType);
			} else if (mWeChatRb.isChecked()) {
				if (!mWXPayCtrl.isWXAppInstalled()) {
					MainThreadHandler.makeToast(R.string.wechat_not_installed);
					return;
				}
				mWXPayCtrl.pay(vipType);
			} else if (mPhoneRb.isChecked()) {
				mMMPayCtrl.pay();
			}
			break;
		}
	}

	@Override
	protected void onDestroy() {
		mWXPayCtrl.removeListener();
		super.onDestroy();
	}

}
