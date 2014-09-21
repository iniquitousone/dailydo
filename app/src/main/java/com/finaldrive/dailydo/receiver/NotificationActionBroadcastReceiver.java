package com.finaldrive.dailydo.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Receiver for handling PendingIntent(s) for Notification actions.
 */
public class NotificationActionBroadcastReceiver extends BroadcastReceiver {

    private static final String CLASS_NAME = "NotificationActionBroadcastReceiver";

    /**
     * Dedicated notification ID so we always reuse the same notification rather than create many unique ones.
     */
    public static final int ID_DAILY_DO_NOTIFICATION = 0;

    public static final String ACTION_DISMISS = "ACTION_DISMISS";

    public NotificationActionBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(CLASS_NAME, String.format("Received NotificationAction Intent=%s", intent.toString()));
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final String intentAction = intent.getAction();

        if (intentAction.equals(ACTION_DISMISS)) {
            notificationManager.cancel(ID_DAILY_DO_NOTIFICATION);
        }
    }
}
