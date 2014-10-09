package com.finaldrive.dailydo.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.finaldrive.dailydo.R;
import com.finaldrive.dailydo.service.NotificationService;
import com.finaldrive.dailydo.store.DailyDoDatabaseHelper;

/**
 * Receiver for the daily reset Alarm. This class exists to separate the database job of unchecking Task entries.
 * This ensures that even if the MainActivity is NOT running, the database entries get updated.
 * MainActivity.onCreate() will then properly handle rendering the now unchecked entries.
 */
public class DailyResetBroadcastReceiver extends BroadcastReceiver {

    private static final String CLASS_NAME = "DailyResetBroadcastReceiver";

    public DailyResetBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(CLASS_NAME, String.format("Received reset Intent=%s", intent.toString()));
        final DailyDoDatabaseHelper dailyDoDatabaseHelper = DailyDoDatabaseHelper.getInstance(context);
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        dailyDoDatabaseHelper.uncheckAllTaskEntries();
        notificationManager.cancel(NotificationService.ID_DAILY_DO_NOTIFICATION);
        // This is intended to reset the Task entries if the user is actively looking at MainActivity.
        final Intent mainActivityIntent = new Intent();
        mainActivityIntent.setAction(context.getResources().getString(R.string.intent_action_reset_tasks));
        context.sendBroadcast(mainActivityIntent);
    }
}
