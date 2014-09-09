package com.finaldrive.dailydo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.finaldrive.dailydo.service.AlarmService;

/**
 * Listens for the system boot Intent to reschedule the necessary Alarm(s).
 */
public class ActionBootBroadcastReceiver extends BroadcastReceiver {

    private static final String CLASS_NAME = "ActionBootBroadcastReceiver";

    public ActionBootBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(CLASS_NAME, "Received boot notification. Rescheduling necessary alarms.");
        // Schedule the next upcoming Alarm.
        AlarmService.scheduleNextAlarm(context);
        // Schedule the internal Alarm dedicated to resetting the Task entries.
        AlarmService.scheduleNextReset(context);
    }
}
