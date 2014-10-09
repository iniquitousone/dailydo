package com.finaldrive.dailydo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.finaldrive.dailydo.service.AlarmService;
import com.finaldrive.dailydo.service.NotificationService;

/**
 * Listens to Intent(s) triggered by AlarmManager to send Notification(s) to the user.
 * Is responsible for notifying the user about the upcoming Task if there are any remaining.
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver {

    private static final String CLASS_NAME = "AlarmBroadcastReceiver";

    public AlarmBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(CLASS_NAME, String.format("Received AlarmManager Intent=%s", intent.toString()));
        NotificationService.startNotificationCreate(context);
        AlarmService.scheduleNextAlarm(context);
    }
}
