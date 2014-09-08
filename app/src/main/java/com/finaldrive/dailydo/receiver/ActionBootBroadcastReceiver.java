package com.finaldrive.dailydo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.finaldrive.dailydo.service.AlarmService;

/**
 * Listens for the system boot Intent to reschedule the necessary Alarm(s).
 */
public class ActionBootBroadcastReceiver extends BroadcastReceiver {

    public ActionBootBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // Schedule the next upcoming Alarm.
        AlarmService.scheduleNextAlarm(context);
        // Schedule the internal Alarm dedicated to resetting the Task entries.
        AlarmService.scheduleNextReset(context);
    }
}
