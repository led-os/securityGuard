package com.vidmt.child.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.vidmt.child.managers.ServiceManager;
import com.vidmt.child.services.LoginLocateService;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            if (!ServiceManager.isServiceRunning(LoginLocateService.class)) {
                {
                    ServiceManager.get().startService(false);
                    return;
                }
            }
        }

    }
}
