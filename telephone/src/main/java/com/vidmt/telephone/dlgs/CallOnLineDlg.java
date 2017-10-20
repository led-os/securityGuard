package com.vidmt.telephone.dlgs;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.telephone.R;
import com.vidmt.telephone.activities.VipCenterActivity;
import com.vidmt.telephone.entities.User;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.managers.HttpManager;

public class CallOnLineDlg extends BaseMsgDlg {
    private int mConfirmBtnNameResId, mMsgResId;
    private String friend_uid;

    public CallOnLineDlg(Activity context, int confirmBtnNameResId, int msgResId, String uid) {
        super(context);
        mConfirmBtnNameResId = confirmBtnNameResId;
        mMsgResId = msgResId;
        friend_uid = uid;
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
                //mActivity.startActivity(new Intent(mActivity, VipCenterActivity.class));
                ThreadPool.execute(new Runnable() {
                                       @Override
                                       public void run() {
                                           try {
                                               final User user = HttpManager.get().getUserInfo(friend_uid);
                                               MainThreadHandler.post(new Runnable() {
                                                   @Override
                                                   public void run() {
                                                       Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
                                                       sendIntent.setData(Uri.parse("smsto:" + user.account));
                                                       sendIntent.putExtra("sms_body", "我是您的好友: " + AccManager.get().getCurUser().nick + " 喊您上线：请在浏览器中打开此链接，点击此网页中红字部分上线：http://m.vidmt.com/pd.jsp?pid=10&mid=3&desc=false");
                                                       mActivity.startActivity(sendIntent);
                                                   }
                                               });

                                           } catch (VidException e) {
                                               VLog.e("CallOnLineDlg", e.getMessage());
                                           }
                                       }
                                   });
            }
        });
    }

}
