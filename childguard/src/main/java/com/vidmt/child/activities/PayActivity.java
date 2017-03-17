package com.vidmt.child.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ContentView;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.lidroid.xutils.view.annotation.event.OnClick;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.child.R;
import com.vidmt.child.pay.alipay.AlipayController;
import com.vidmt.child.pay.wxpay.WXPayController;
import com.vidmt.child.utils.Enums.PayType;
import com.vidmt.child.utils.UserUtil;
import com.vidmt.child.vos.LvlVo;
import com.vidmt.xmpp.enums.XmppEnums.LvlType;

@ContentView(R.layout.activity_pay)
public class PayActivity extends AbsVidActivity {
	@ViewInject(R.id.alipay_icon)
	private ImageView mAlipayIv;
	@ViewInject(R.id.wechat_icon)
	private ImageView mWechatIv;
	@ViewInject(R.id.alipay_txt)
	private TextView mAlipayTv;
	@ViewInject(R.id.wechat_txt)
	private TextView mWechatTv;
	@ViewInject(R.id.vip_type)
	private TextView mVipTypeTv;
	@ViewInject(R.id.price)
	private TextView mPriceTv;
	@ViewInject(R.id.icon)
	private ImageView mIcon;
	@ViewInject(R.id.minus)
	private Button mMinusBtn;
	@ViewInject(R.id.plus)
	private Button mPlusBtn;
	@ViewInject(R.id.num)
	private TextView mPayNumTv;
	@ViewInject(R.id.total)
	private TextView mTotalTv;
	private PayType mPayType = PayType.ALI;
	private LvlVo mLvl;
	private AlipayController mAlipayCtrl;
	private WXPayController mWXPayCtrl;
	private int mPayNum = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewUtils.inject(this);
		initTitle();
		initCurrentPay();
		mAlipayCtrl = new AlipayController(this);
		mWXPayCtrl = new WXPayController(this);
	}

	private void initTitle() {
		View view = findViewById(R.id.title_bar);
		TextView titleTv = (TextView) view.findViewById(R.id.title);
		titleTv.setText(R.string.confirm_pay);
		initReconnectView(findViewById(R.id.reconnect_layout));
	}

	private void initCurrentPay() {
		Bundle bundle = getIntent().getExtras();
		if (bundle == null) {
			mLvl = UserUtil.getLvl();
		} else {
			mLvl = UserUtil.bundle2Lvl(bundle);
		}
		if (mLvl.code == LvlType.Cu) {
			mVipTypeTv.setText(R.string.vip_bronze);
			mIcon.setBackgroundResource(R.drawable.bronze);
		} else if (mLvl.code == LvlType.Ag) {
			mVipTypeTv.setText(R.string.vip_silver);
			mIcon.setBackgroundResource(R.drawable.silver);
		} else if (mLvl.code == LvlType.Au) {
			mVipTypeTv.setText(R.string.vip_gold);
			mIcon.setBackgroundResource(R.drawable.gold);
		}
		mPriceTv.setText(mLvl.price + getString(R.string.rmb) + "/" + UserUtil.getExpireUnit(mLvl.expire));
		mPayNumTv.setText("1");
		mTotalTv.setText("￥ " + mLvl.price);
	}

	@OnClick({ R.id.back, R.id.minus, R.id.plus, R.id.alipay, R.id.wechat, R.id.pay })
	private void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.minus:
			offsetOneNum(false);
			break;
		case R.id.plus:
			offsetOneNum(true);
			break;
		case R.id.alipay:
			resetPayStatus();
			mAlipayIv.setBackgroundResource(R.drawable.shape_pay_rect_checked_bg);
			mAlipayIv.setImageResource(R.drawable.alipay_checked);
			mAlipayTv.setTextColor(getResources().getColor(R.color.theme_green));
			mPayType = PayType.ALI;
			break;
		case R.id.wechat:
			resetPayStatus();
			mWechatIv.setBackgroundResource(R.drawable.shape_pay_rect_checked_bg);
			mWechatIv.setImageResource(R.drawable.wechat_checked);
			mWechatTv.setTextColor(getResources().getColor(R.color.theme_green));
			mPayType = PayType.WX;
			break;
		case R.id.pay:
			if (mPayType == PayType.ALI) {
				mAlipayCtrl.pay(mLvl.code, mPayNum);
			} else if (mPayType == PayType.WX) {
				if (!mWXPayCtrl.isWXAppInstalled()) {
					MainThreadHandler.makeToast(R.string.wechat_not_installed);
					return;
				}
				mWXPayCtrl.pay(mLvl.code, mPayNum);
			} else {
				MainThreadHandler.makeToast(R.string.choose_pay_type);
			}
			break;
		}
	}

	private void offsetOneNum(boolean increase) {
		String numStr = mPayNumTv.getText().toString();
		int num = Integer.parseInt(numStr);
		if (increase) {
			num += 1;
		} else {
			num -= 1;
		}
		if (num == 1) {
			mMinusBtn.setBackgroundResource(R.drawable.pay_minus_pressed);
			mMinusBtn.setEnabled(false);
		} else {
			mMinusBtn.setBackgroundResource(R.drawable.selector_pay_minus);
			mMinusBtn.setEnabled(true);
		}
		mPayNumTv.setText(num + "");
		mTotalTv.setText("￥ " + num * mLvl.price);
		mPayNum = num;
	}

	private void resetPayStatus() {
		mAlipayIv.setBackgroundResource(R.drawable.shape_pay_rect_bg);
		mAlipayIv.setImageResource(R.drawable.alipay);
		mAlipayTv.setTextColor(getResources().getColor(R.color.black_grey));
		mWechatIv.setBackgroundResource(R.drawable.shape_pay_rect_bg);
		mWechatIv.setImageResource(R.drawable.wechat);
		mWechatTv.setTextColor(getResources().getColor(R.color.black_grey));
	}

}
