package com.vidmt.telephone.dlgs;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.vidmt.telephone.R;
import com.vidmt.telephone.activities.VipCenterActivity;
import com.vidmt.telephone.utils.Enums.AddFriendType;

public class AddFriendDlg extends BaseMsgDlg {
	private AddFriendType addType;

	public AddFriendDlg(Activity context, AddFriendType addType) {
		super(context);
		this.addType = addType;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		switch (addType) {
		case DIRECT:
			//huawei change;
			//setMsg(R.string.add_success);
			setMsg(R.string.self_is_vip_wait_for_agree);
			setBtnName(DialogInterface.BUTTON_POSITIVE, R.string.confirm);
			break;
		case WAIT:
			setMsg(R.string.side_is_vip_wait_for_agree_apply_for_vip);
			setBtnName(DialogInterface.BUTTON_POSITIVE, R.string.apply);
			setBtnName(DialogInterface.BUTTON_NEGATIVE, R.string.cancel);
			break;
		case VIP_WAIT:
			setMsg(R.string.side_is_vip_wait_for_agree);
			setBtnName(DialogInterface.BUTTON_POSITIVE, R.string.confirm);
			break;
		case NOT_ALLOWED:
			setMsg(R.string.no_vip_cannot_add_vip_for_friend);
			setBtnName(DialogInterface.BUTTON_POSITIVE, R.string.apply);
			setBtnName(DialogInterface.BUTTON_NEGATIVE, R.string.cancel);
			break;
		case NORMAL_WAIT:
			setMsg(R.string.add_friend_request_have_sent_please_wait);
			setBtnName(DialogInterface.BUTTON_POSITIVE, R.string.confirm);
			break;
		}
		setTitle(R.string.warm_prompt);
		setOnClickListener(new DialogClickListener() {
			@Override
			public void onOK() {
				super.onOK();
				if (addType == AddFriendType.WAIT || addType == AddFriendType.NOT_ALLOWED) {
					mActivity.startActivity(new Intent(mActivity, VipCenterActivity.class));
				}
			}
		});
	}

}
