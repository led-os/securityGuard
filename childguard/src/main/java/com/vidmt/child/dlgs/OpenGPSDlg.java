package com.vidmt.child.dlgs;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import com.vidmt.child.R;

/**
 * Created by lihuichao on 2017/3/13.
 * 切换账号dialog提示
 */

public class OpenGPSDlg extends BaseMsgDlg {

    private int mConfirmBtnNameResId, mMsgResId;

    public OpenGPSDlg(Activity context, int confirmBtnNameResId, int msgResId) {
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
                // 转到手机设置界面，用户设置GPS
                Intent intent = new Intent(
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                getContext().startActivity(intent); // 设置完成后返回到原来的界面
            }
        });
    }
}
