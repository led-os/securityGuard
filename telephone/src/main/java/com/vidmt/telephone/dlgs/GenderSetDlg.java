package com.vidmt.telephone.dlgs;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.R;

public class GenderSetDlg extends BaseDialog {
	private RadioButton mMaleRb;
	private RadioButton mFemaleRb;

	private DialogClickListener mListener;
	private boolean mIsMale;

	public GenderSetDlg(Activity context, boolean isMale, DialogClickListener listener) {
		super(context, R.layout.dlg_gender_set);
		mIsMale = isMale;
		mListener = listener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mMaleRb = (RadioButton) findViewById(R.id.rb_male);
		mFemaleRb = (RadioButton) findViewById(R.id.rb_female);
		if (mIsMale) {
			mMaleRb.setChecked(true);
		} else {
			mFemaleRb.setChecked(true);
		}
		findViewById(R.id.male).setOnClickListener(mClickListener);
		findViewById(R.id.female).setOnClickListener(mClickListener);
	}
	
	private View.OnClickListener mClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.male:
				if (mMaleRb.isChecked()) {
					mMaleRb.setChecked(false);
					if (mFemaleRb.isChecked()) {
						mFemaleRb.setChecked(true);
					}
				} else {
					mMaleRb.setChecked(true);
					if (mFemaleRb.isChecked()) {
						mFemaleRb.setChecked(false);
					}
				}
				break;
			case R.id.female:
				if (mFemaleRb.isChecked()) {
					mFemaleRb.setChecked(false);
					if (mMaleRb.isChecked()) {
						mMaleRb.setChecked(true);
					}
				} else {
					mFemaleRb.setChecked(true);
					if (mMaleRb.isChecked()) {
						mMaleRb.setChecked(false);
					}
				}
				break;
			}
			Bundle bundle = new Bundle();
			bundle.putString(ExtraConst.EXTRA_TXT_CONTENT, mMaleRb.isChecked() ? "M" : "F");
			mListener.onOK(bundle);
			dismiss();
		}
	};

}
