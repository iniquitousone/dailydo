package com.finaldrive.dailydo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import com.finaldrive.dailydo.receiver.NotificationActionBroadcastReceiver;
import com.finaldrive.dailydo.service.AlarmService;

/**
 * Wrapper Activity that houses the Snooze picker dialog.
 */
public class SnoozePickerActivity extends Activity {

    private static final CharSequence[] MINUTE_VALUES = {"5 minutes", "15 minutes", "30 minutes", "45 minutes", "1 hour", "2 hours"};
    private static final int[] MINUTE_ARRAY = {5, 15, 30, 45, 60, 120};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        final AlertDialog alertDialog = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                .setIcon(R.drawable.ic_action_alarms)
                .setTitle("Snooze duration:")
                .setCancelable(true)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setItems(MINUTE_VALUES, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final int minutes = MINUTE_ARRAY[which];
                        AlarmService.scheduleSnooze(getApplicationContext(), minutes);
                        notificationManager.cancel(NotificationActionBroadcastReceiver.ID_DAILY_DO_NOTIFICATION);
                        Toast.makeText(getApplicationContext(),
                                String.format("Snoozing for %d minutes", minutes),
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .create();
        alertDialog.show();
    }
}
