package com.vidmt.telephone.tasks;

import java.io.IOException;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.ConnectionException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.packet.XMPPError.Condition;
import org.jivesoftware.smack.sasl.SASLError;
import org.jivesoftware.smack.sasl.SASLErrorException;
import org.jivesoftware.smack.sasl.packet.SaslStreamElements.SASLFailure;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;

import com.vidmt.acmn.utils.andr.FLog;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.telephone.ExtraConst;
import com.vidmt.telephone.PrefKeyConst;
import com.vidmt.telephone.R;
import com.vidmt.telephone.activities.MainActivity;
import com.vidmt.telephone.dlgs.LoadingDlg;
import com.vidmt.telephone.exceptions.VHttpException;
import com.vidmt.telephone.exceptions.VidException;
import com.vidmt.telephone.managers.AccManager;
import com.vidmt.telephone.managers.AdManager;
import com.vidmt.telephone.managers.HttpManager;
import com.vidmt.telephone.managers.ServiceManager;
import com.vidmt.telephone.utils.EncryptUtil;
import com.vidmt.telephone.utils.Enums.ResType;
import com.vidmt.telephone.utils.VidUtil;

public class LogRegTask extends AsyncTask<String, Integer, VidException> {
    private static final String TAG = LogRegTask.class.getSimpleName();
    private Activity mActivity;
    private String mAccount, mPwd;
    private Bundle mBundle;
    private LoadingDlg mLoadingDlg;

    public LogRegTask(Activity act, String account, String pwd, Bundle bundle) {
        mActivity = act;
        mAccount = account;
        mPwd = pwd;
        mBundle = bundle;
        mLoadingDlg = new LoadingDlg(act, R.string.loading);
    }

    @Override
    protected void onPreExecute() {
        mLoadingDlg.show();
        mLoadingDlg.setCancelable(false);
        super.onPreExecute();
    }

    @Override
    protected VidException doInBackground(String... params) {
        VidException vidEx = null;
        if (mBundle != null) {// 注册
            try {
                HttpManager.get().register(mAccount, mPwd);
                // 注册成功
            } catch (VidException e) {// 注册失败
                mLoadingDlg.dismiss();
                return e;
            }
        }
        try {
            AccManager.get().login(mAccount, mPwd);
            if (mBundle != null) {// 注册
                String nick = mBundle.getString(ExtraConst.EXTRA_NICKNAME);
                Bitmap avatar = mBundle.getParcelable(ExtraConst.EXTRA_PHOTO_PARCEL);
                try {
                    AccManager.get().setUserInfo("nick", nick);// 设置昵称
                } catch (VidException e) {
                    VLog.d("test", e);
                }
                if (avatar != null) {
                    HttpManager.get().uploadFile(ResType.AVATAR, avatar);// 上传头像
                }
            }
            // 登录成功
            SysUtil.savePref(PrefKeyConst.PREF_ACCOUNT, mAccount);
            SysUtil.savePref(PrefKeyConst.PREF_PASSWORD, EncryptUtil.encryptLocalPwd(mPwd));
            ServiceManager.get().startService();// 启动Service服务
            mLoadingDlg.dismiss();
            mActivity.startActivity(new Intent(mActivity, MainActivity.class));
            mActivity.finish();
        } catch (VidException e) {
            mLoadingDlg.dismiss();
            vidEx = e;
        }
        return vidEx;
    }

    /**
     * @param result
     */
    @Override
    protected void onPostExecute(VidException result) {
        if (result != null) {
            VLog.e("test", result);
            if (result instanceof VHttpException) {
                VHttpException httpE = (VHttpException) result;
                switch (httpE.getCode()) {
                    case VHttpException.ERR_CODE_USER_NOT_EXISTS:
                        MainThreadHandler.makeToast(R.string.account_error);
                        break;
                    case VHttpException.ERR_CODE_USER_ALREADY_EXISTS:
                        MainThreadHandler.makeToast(R.string.account_exist);
                        break;
                    case VHttpException.ERR_CODE_PARAMS_ERROR:
                        MainThreadHandler.makeToast(R.string.invalid_params);
                        break;
                    case VHttpException.ERR_CODE_HTTP_SERVER_ERROR:

                        MainThreadHandler.makeToast(R.string.remote_server_error);
                        break;
                    default:
                        MainThreadHandler.makeToast(R.string.unknown_error);

                        break;
                }
                return;
            }
            Throwable e = result.getCause();
            if (e instanceof XMPPException) {
                XMPPException xmppE = (XMPPException) e;
                if (e instanceof SASLErrorException) {
                    SASLErrorException saslErrE = (SASLErrorException) e;
                    SASLFailure failure = saslErrE.getSASLFailure();
                    if (failure.getSASLError() == SASLError.not_authorized) {
                        MainThreadHandler.makeToast(R.string.account_error);
                    } else {
                        VidUtil.fLog(TAG, "SASLErrorException:" + e);
                    }
                } else if (e instanceof XMPPErrorException) {
                    XMPPErrorException xmppErrE = (XMPPErrorException) e;
                    XMPPError xmppErr = xmppErrE.getXMPPError();
                    Condition condition = xmppErr.getCondition();
                    if (condition == Condition.not_authorized) {
                        MainThreadHandler.makeToast(R.string.account_error);
                    } else if (condition == Condition.conflict) {
                        MainThreadHandler.makeToast(R.string.account_exist);
                    } else if (condition == Condition.internal_server_error
                            || condition == Condition.remote_server_not_found) {
                        MainThreadHandler.makeToast(R.string.remote_server_error);
                    } else if (condition == Condition.remote_server_timeout) {
                        MainThreadHandler.makeToast(R.string.timeout);
                    } else {
                        VidUtil.fLog(TAG, "XMPPErrorException:" + e);
                    }
                }
            } else if (e instanceof IOException) {
                IOException ioE = (IOException) e;
                MainThreadHandler.makeToast(R.string.net_error);
                VidUtil.fLog(TAG, "IOException:" + ioE);
            } else if (e instanceof SmackException) {
                if (e instanceof ConnectionException) {
                    MainThreadHandler.makeToast(R.string.connect_error);
                } else if (e instanceof NoResponseException) {
                    MainThreadHandler.makeToast(R.string.timeout);
                } else {
                    SmackException smackE = (SmackException) e;
                    VidUtil.fLog(TAG, "SmackException:" + smackE);
                }
            } else {
                VidUtil.fLog("LogRegTask onPostExecute 未知错误信息: " + e.getMessage());
                MainThreadHandler.makeToast(R.string.error_unknown);
            }
            VidUtil.fLog(TAG, "Exception:" + e);
        }
        super.onPostExecute(result);
    }

}