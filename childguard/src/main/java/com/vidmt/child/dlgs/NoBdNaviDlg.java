package com.vidmt.child.dlgs;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;

import com.baidu.mapapi.utils.OpenClientUtil;
import com.vidmt.child.R;

public class NoBdNaviDlg extends BaseMsgDlg {

	public NoBdNaviDlg(Activity context) {
		super(context);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setBtnName(DialogInterface.BUTTON_POSITIVE, R.string.confirm);
		setBtnName(DialogInterface.BUTTON_NEGATIVE, R.string.cancel);
		setTitle(R.string.warm_prompt);
		setMsg(R.string.baidu_app_not_support);
		setOnClickListener(new DialogClickListener(){
			@Override
			public void onOK() {
				super.onOK();
				OpenClientUtil.getLatestBaiduMapApp(mActivity);
			}
		});
	}

}
