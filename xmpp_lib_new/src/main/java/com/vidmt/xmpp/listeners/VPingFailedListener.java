package com.vidmt.xmpp.listeners;

import com.vidmt.acmn.utils.andr.FLog;
import com.vidmt.xmpp.IXmppManager;
import com.vidmt.xmpp.inner.XmppManager;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.ping.PingFailedListener;

import java.io.IOException;

/**
 * Created by MacBook on 2016-07-20.
 */
public class VPingFailedListener implements PingFailedListener {
    private final String tag = "PingFailed";
    private static VPingFailedListener sInstance;

    public static VPingFailedListener get() {
        if (sInstance == null) {
            sInstance = new VPingFailedListener();
        }
        return sInstance;
    }

    private VPingFailedListener() {
    }
    @Override
    public void pingFailed() {
        FLog.d(tag, "invoked pingFailed()");
        final IXmppManager xmppMgr = XmppManager.get();
        AbstractXMPPConnection connection = xmppMgr.getConnection();
        ReconnectionManager reconnMgr = ReconnectionManager.getInstanceFor(connection);
        reconnMgr.reconnect();
        //huawei test
//        try {
//            xmppMgr.logout();
//            xmppMgr.login(xmppMgr.getmUid(), xmppMgr.getmPwd());
//        }catch(SmackException e){
//            FLog.d(tag, "invoked pingFailed()  relogin exception" + e.getMessage());
//        }catch(XMPPException e){
//            FLog.d(tag, "invoked pingFailed()  relogin exception" + e.getMessage());
//        }catch(IOException e){
//            FLog.d(tag, "invoked pingFailed()  relogin exception" + e.getMessage());
//        }catch (Exception e){
//            FLog.d(tag, "invoked pingFailed()  relogin exception" + e.getMessage());
//        }
        //test end;

        /*
        if(!connection.isConnected()){
            try {
                FLog.d(tag, "do connect()");
                connection.connect();
            }catch (SmackException.AlreadyLoggedInException e) {
                FLog.d(tag, "do connect() exception, logout()" + e.getMessage());
                return;
            }catch (SmackException e) {
                FLog.d(tag, "do connect() exception, logout()" + e.getMessage());
                xmppMgr.logout();
            } catch (IOException e) {
                FLog.d(tag, "do connect() exception, logout()" + e.getMessage());
                xmppMgr.logout();
            } catch (XMPPException e) {
                FLog.d(tag, "do connect() exception, logout()" + e.getMessage());
                xmppMgr.logout();
            }catch (Exception e){
                FLog.d(tag, "do connect() exception, logout()" + e.getMessage());
                xmppMgr.logout();
            }
        }
        */
    }
}
