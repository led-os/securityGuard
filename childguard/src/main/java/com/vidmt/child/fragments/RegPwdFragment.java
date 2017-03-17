package com.vidmt.child.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.vidmt.child.R;
import com.vidmt.child.activities.RegisterActivity;
import com.vidmt.child.tasks.LogRegTask;
import com.vidmt.child.utils.VidUtil;

public class RegPwdFragment extends Fragment implements View.OnClickListener {
	private static final int MIN_PWD_LEN = 6;
	private EditText mPwdEt, mRePwdEt;
	private String mPwd;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.frag_reg_pwd, null, false);
		mPwdEt = (EditText) view.findViewById(R.id.pwd);
		mRePwdEt = (EditText) view.findViewById(R.id.re_pwd);
		view.findViewById(R.id.done).setOnClickListener(this);
		view.findViewById(R.id.backword).setOnClickListener(this);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.done:
			mPwd = mPwdEt.getText().toString();
			String rePwd = mRePwdEt.getText().toString();
			if (TextUtils.isEmpty(mPwd)) {
				VidUtil.setWarnText(mPwdEt, getString(R.string.input_new_pwd));
			} else if (mPwd.length() < MIN_PWD_LEN) {
				VidUtil.setWarnText(mPwdEt, getString(R.string.pwd_len_err));
			} else if (TextUtils.isEmpty(rePwd)) {
				VidUtil.setWarnText(mRePwdEt, getString(R.string.input_re_pwd));
			} else if (!mPwd.equals(rePwd)) {
				VidUtil.setWarnText(mRePwdEt, getString(R.string.second_pwd_not_equal));
			} else {
				RegisterActivity activity = (RegisterActivity) getActivity();
				new LogRegTask(getActivity(), activity.getAccount(), mPwd, true).execute();
			}
			break;
		case R.id.backword:
			RegisterActivity activity = (RegisterActivity) getActivity();
			activity.showFrag(RegisterActivity.FRAG_VERCODE_INDEX);
			break;
		}
	}
}
