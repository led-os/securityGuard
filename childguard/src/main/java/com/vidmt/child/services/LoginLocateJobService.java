package com.vidmt.child.services;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import com.baidu.mapapi.model.LatLng;
import com.vidmt.acmn.utils.andr.FLog;
import com.vidmt.acmn.utils.andr.SysUtil;
import com.vidmt.acmn.utils.andr.VLog;
import com.vidmt.acmn.utils.andr.async.DefaultThreadHandler;
import com.vidmt.acmn.utils.andr.async.MainThreadHandler;
import com.vidmt.acmn.utils.andr.async.ThreadPool;
import com.vidmt.child.Const;
import com.vidmt.child.PrefKeyConst;
import com.vidmt.child.activities.LoginActivity;
import com.vidmt.child.exceptions.VidException;
import com.vidmt.child.managers.AccManager;
import com.vidmt.child.managers.LocationManager;
import com.vidmt.child.utils.EncryptUtil;
import com.vidmt.child.utils.GeoUtil;
import com.vidmt.child.utils.HttpUtil;
import com.vidmt.child.utils.UserUtil;
import com.vidmt.child.utils.VidUtil;
import com.vidmt.xmpp.enums.XmppEnums;
import com.vidmt.xmpp.exts.CgUserIQ;
import com.vidmt.xmpp.exts.LvlIQ;
import com.vidmt.xmpp.inner.XmppManager;
import com.vidmt.xmpp.listeners.OnConnectionListener;
import com.vidmt.xmpp.listeners.OnRosterListener;
import com.vidmt.xmpp.listeners.XmppListenerHolder;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.sasl.SASLError;
import org.jivesoftware.smack.sasl.SASLErrorException;
import org.jivesoftware.smack.sasl.packet.SaslStreamElements;

public class LoginLocateJobService extends JobIntentService {
    private LocationListener mLocListener;
    private LocationManager mLocMgr;
    private OnUserStatusListener mOnUserStatusListener;
    private boolean mIsLogining;

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        MainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                initLocMgr();
                mOnUserStatusListener = new OnUserStatusListener();
                XmppManager.get().addXmppListener(mOnUserStatusListener);
                //huawei add
                XmppManager.get().addXmppListener(mOnConnectionListener);
                login();
            }
        });
    }
    private void initLocMgr() {
        if (mLocMgr != null) {
            return;
        }
        mLocMgr = LocationManager.get();
        mLocMgr.start();
        mLocListener = new LocationListener();
        mLocMgr.addListener(mLocListener);

    }

    private void login() {
        if (mIsLogining || !VidUtil.isNetworkConnected() || XmppManager.get().isAuthenticated()) {
            return;// 已被另一方法调用处于正登录状态 或无网络 或 已登录
        }
        mIsLogining = true;
        final AccManager.IAccManager accMgr = AccManager.get();
        final String account = SysUtil.getPref(PrefKeyConst.PREF_ACCOUNT);
        String encodedPassword = SysUtil.getPref(PrefKeyConst.PREF_PASSWORD);
        if (encodedPassword != null) {
            final String pwd = EncryptUtil.decryptLocalPwd(encodedPassword);
            ThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        String acc = null;
                        boolean isBabyClient = SysUtil.getBooleanPref(PrefKeyConst.PREF_IS_BABY_CLIENT);
                        if (isBabyClient) {
                            acc = Const.BABY_ACCOUNT_PREFIX + account;
                        } else {
                            acc = account;
                        }
                        accMgr.login(acc, pwd);
                        // 登录成功
                        CgUserIQ selfInfoIq = accMgr.getUserInfo(null);// 得到自己所有资料
                        CgUserIQ sideInfoIq = accMgr.getUserInfo(VidUtil.getSideName());// 得到对方资料
                        LvlIQ lvlInfoIq = null;
                        if (isBabyClient) {
                            UserUtil.initBabyInfo(selfInfoIq);
                            UserUtil.initParentInfo(sideInfoIq);
                            lvlInfoIq = accMgr.getLvlInfo(sideInfoIq.code == null ? XmppEnums.LvlType.NONE : sideInfoIq.code);// 得到等级信息
                        } else {
                            UserUtil.initParentInfo(selfInfoIq);
                            UserUtil.initBabyInfo(sideInfoIq);
                            lvlInfoIq = accMgr.getLvlInfo(selfInfoIq.code == null ? XmppEnums.LvlType.NONE : selfInfoIq.code);// 得到等级信息
                        }
                        UserUtil.initLvl(lvlInfoIq);
                        XmppListenerHolder.callListeners(OnConnectionListener.class,
                                new XmppListenerHolder.IXmppListenerExecutor<OnConnectionListener>() {
                                    @Override
                                    public void execute(OnConnectionListener listener) {
                                        listener.onConnected();
                                    }
                                });
                        mIsLogining = false;
                    } catch (VidException ve) {
                        Throwable e = ve.getCause();
                        if (e instanceof XMPPException) {
                            VLog.e("test", e);
                            XMPPException xmppE = (XMPPException) e;
                            if (e instanceof SASLErrorException) {
                                SASLErrorException saslErrE = (SASLErrorException) e;
                                SaslStreamElements.SASLFailure failure = saslErrE.getSASLFailure();
                                if (failure.getSASLError() == SASLError.not_authorized) {
                                    notAuthorized();
                                }
                            } else if (e instanceof XMPPException.XMPPErrorException) {
                                XMPPException.XMPPErrorException xmppErrE = (XMPPException.XMPPErrorException) e;
                                XMPPError xmppErr = xmppErrE.getXMPPError();
                                XMPPError.Condition condition = xmppErr.getCondition();
                                if (condition == XMPPError.Condition.not_authorized) {
                                    notAuthorized();
                                }
                            }
                        }
                        // 登录失败
                        mIsLogining = false;
                        // 若有网络,10秒后重连。无网络刚交给BroadcastReceiver处理
                        if (VidUtil.isNetworkConnected()) {
                            DefaultThreadHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    login();
                                }
                            }, 10 * 1000);
                        }
                        VLog.e("test", e);
                    }
                }
            });
        }
    }

    private void notAuthorized() {
        SysUtil.removePref(PrefKeyConst.PREF_PASSWORD);
        VidUtil.startNewTaskActivity(LoginActivity.class);
        stopSelf();
    }

    private OnConnectionListener.AbsOnConnectionListener mOnConnectionListener = new OnConnectionListener.AbsOnConnectionListener() {
        @Override
        public void connectionClosed() {
            FLog.d("LoginLocateService", "conn:connectionClosed");
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            if (e != null) {
                if (e.getMessage().contains("stream:error (conflict)")) {
                    FLog.d("LoginLocateService", "stream:error (conflict)");
                    return;
                }
                FLog.d("LoginLocateService", e);
            } else {
                FLog.d("LoginLocateService", "conn:" + XmppManager.get().isAuthenticated());
            }
        }

        @Override
        public void reconnectingIn(final int seconds) {
        }

        @Override
        public void reconnectionFailed(final Exception e) {
            FLog.d("LoginLocateService", "reconnectionFailed"+e);
        }

        @Override
        public void reconnectionSuccessful() {
            FLog.d("LoginLocateService", "reconnectionSuccessful");
        }
    };

    private class LocationListener implements android.location.LocationListener {
        private Location lastLoc;

        @Override
        public void onLocationChanged(final Location location) {
            boolean isBabyClient = SysUtil.getBooleanPref(PrefKeyConst.PREF_IS_BABY_CLIENT);
            if (!isBabyClient) {
                return;
            }
            ThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (lastLoc == null) {
                            HttpUtil.uploadLocation(UserUtil.getBabyInfo().uid, location);
                            lastLoc = location;
                            return;
                        }
                        double distance = GeoUtil.getDistance(location, lastLoc);
                        if (distance >= Const.PUB_LOC_RANGE) {
                            HttpUtil.uploadLocation(UserUtil.getBabyInfo().uid, location);
                            lastLoc = location;
                        }
                    } catch (VidException e) {
                        VLog.e("test", e);
                    }
                }
            });
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    private class OnUserStatusListener extends OnRosterListener.AbsOnRosterListener {
        @Override
        public void presenceChanged(Presence presence) {
            boolean isBabyClient = SysUtil.getBooleanPref(PrefKeyConst.PREF_IS_BABY_CLIENT);
            if (!isBabyClient) {
                return;
            }
            if (presence.getType() == Presence.Type.available) {// 上线
                if (mLocMgr != null) {
                    LatLng curLoc = mLocMgr.getCurLocation();
                    if (curLoc != null) {
                        final Location loc = GeoUtil.latlng2Location(curLoc);
                        DefaultThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                // 避免在后台运行时，定位变慢发送不了位置的问题
                                try {
                                    HttpUtil.uploadLocation(UserUtil.getBabyInfo().uid, loc);
                                } catch (VidException e) {
                                    VLog.e("test", e);
                                }
                            }
                        }, 3 * 1000);// [depressed]延迟到大人端进入主界面
                    }
                }
            }
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        XmppManager.get().removeXmppListener(mOnUserStatusListener);
        XmppManager.get().removeXmppListener(mOnConnectionListener);
        return super.onUnbind(intent);
    }
}
