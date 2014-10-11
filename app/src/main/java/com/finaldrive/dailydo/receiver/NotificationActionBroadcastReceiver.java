package com.finaldrive.dailydo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.finaldrive.dailydo.service.NotificationService;

/**
 * Receiver for handling PendingIntent(s) for Notification actions.
 */
public class NotificationActionBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_DISMISS = "ACTION_DISMISS";
    private static final String CLASS_NAME = "NotificationActionBroadcastReceiver";

    public NotificationActionBroadcastReceiver() {
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(CLASS_NAME, String.format("Received NotificationAction Intent=%s", intent.toString()));
        final String intentAction = intent.getAction();
        if (intentAction.equals(ACTION_DISMISS)) {
            NotificationService.startNotificationCancel(context);
        }
    }
}
