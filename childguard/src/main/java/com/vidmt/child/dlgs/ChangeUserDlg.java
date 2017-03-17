package com.vidmt.child.dlgs;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;

import com.vidmt.child.R;
import com.vidmt.child.utils.VidUtil;

/**
 * Created by lihuichao on 2017/3/13.
 * 切换账号dialog提示
 */

public class ChangeUserDlg extends BaseMsgDlg {

    private int mConfirmBtnNameResId, mMsgResId;

    public ChangeUserDlg(Activity context, int confirmBtnNameResId, int msgResId) {
        super(context);
        mConfirmBtnNameResId = confirmBtnNameResId;
        mMsgResId = msgResId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBtnName(DialogInterface.BUTTON_POSITIVE, mConfirmBtnNameResId);
        setBtnName(DialogInterface.BUTTON_NEGATIVE, R.string.cancel);
        setTitle(R.string.warm_prompt);
        setMsg(mMsgResId);
        setOnClickListener(new DialogClickListener() {
            @Override
            public void onOK() {
                super.onOK();
                VidUtil.logoutApp();
            }
        });
    }
}
