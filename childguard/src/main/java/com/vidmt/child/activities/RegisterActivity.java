package com.vidmt.child.activities;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.child.R;
import com.vidmt.child.fragments.RegPwdFragment;
import com.vidmt.child.fragments.RegVercodeFragment;

public class RegisterActivity extends AbsVidActivity implements OnClickListener {
	public static final int FRAG_VERCODE_INDEX = 0;
	public static final int FRAG_PWD_INDEX = 1;
	private Fragment mVercodeFragment, mPwdFragment;
	private View mTabVercodeLayout;
	private ImageView mTabVercodeIcon, mChkPointVercodeIv, mChkPointPwdIv;
	private String mPhoneNO;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		mTabVercodeLayout = findViewById(R.id.tab_layout_vercode);
		mTabVercodeIcon = (ImageView) findViewById(R.id.tab_icon_vercode);
		mChkPointVercodeIv = (ImageView) findViewById(R.id.chk_point_vercode);
		mChkPointPwdIv = (ImageView) findViewById(R.id.chk_point_pwd);

		mVercodeFragment = new RegVercodeFragment();
		mPwdFragment = new RegPwdFragment();
		handleFragment(mVercodeFragment, mPwdFragment);
		findViewById(R.id.close).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.close:
			startActivity(new Intent(this, LoginActivity.class));
			finish();
			break;
		}
	}

	public void showFrag(final int index) {
		MainThreadHandler.post(new Runnable() {
			@Override
			public void run() {
				switch (index) {
				case FRAG_VERCODE_INDEX:
					handleFragment(mVercodeFragment, mPwdFragment);
					mChkPointVercodeIv.setVisibility(View.VISIBLE);
					mTabVercodeLayout.setBackgroundResource(R.drawable.reg_tab_bg_dark);
					mTabVercodeIcon.setImageResource(R.drawable.reg_tab_pwd_unchk_icon);
					mChkPointPwdIv.setVisibility(View.GONE);
					break;
				case FRAG_PWD_INDEX:
					handleFragment(mPwdFragment, mVercodeFragment);
					mTabVercodeLayout.setBackgroundResource(R.drawable.reg_tab_bg_light);
					mTabVercodeIcon.setImageResource(R.drawable.reg_tab_pwd_icon);
					mChkPointPwdIv.setVisibility(View.VISIBLE);
					mChkPointVercodeIv.setVisibility(View.GONE);
					break;
				}
			}
		});
	}

	public String getAccount() {
		return mPhoneNO;
	}

	public void setAccount(String phone) {
		mPhoneNO = phone;
	}

	private void handleFragment(Fragment showFrag, Fragment hideFrag) {
		if (hideFrag.isAdded()) {
			hideFrag.onPause();
		}
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		if (showFrag.isAdded()) {
			showFrag.onResume();
		} else {
			ft.add(R.id.frags, showFrag);
			ft.commit();
		}
		ft = getFragmentManager().beginTransaction();
		ft.show(showFrag);
		ft.commit();
		if (hideFrag.isAdded()) {
			ft = getFragmentManager().beginTransaction();
			ft.hide(hideFrag);
			ft.commit();
		}
	}

}
