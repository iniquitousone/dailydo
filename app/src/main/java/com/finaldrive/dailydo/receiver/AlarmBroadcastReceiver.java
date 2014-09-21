package com.finaldrive.dailydo.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.finaldrive.dailydo.MainActivity;
import com.finaldrive.dailydo.R;
import com.finaldrive.dailydo.TaskDetailsActivity;
import com.finaldrive.dailydo.domain.Task;
import com.finaldrive.dailydo.service.AlarmService;
import com.finaldrive.dailydo.store.DailyDoDatabaseHelper;

import java.util.List;

import static com.finaldrive.dailydo.receiver.NotificationActionBroadcastReceiver.ACTION_DISMISS;
import static com.finaldrive.dailydo.receiver.NotificationActionBroadcastReceiver.ID_DAILY_DO_NOTIFICATION;

/**
 * Listens to Intent(s) triggered by AlarmManager to send Notification(s) to the user.
 * Is responsible for notifying the user about the upcoming Task if there are any remaining.
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver {

    private static final String CLASS_NAME = "AlarmBroadcastReceiver";

    public AlarmBroadcastReceiver() {
    }

    /**
     * Creates a PendingIntent for Notification click.
     * This will send the user to the {@link com.finaldrive.dailydo.TaskDetailsActivity}
     *
     * @param context scope of the PendingIntent
     * @param task    that the user will be sent to
     * @return pendingIntent that will send the user to the TaskDetailsActivity
     */
    private static PendingIntent createNotificationClickIntent(Context context, Task task) {
        final Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);
        intent.putExtra(TaskDetailsActivity.EXTRA_TASK_ID, task.getId());
        intent.putExtra(TaskDetailsActivity.EXTRA_TASK_POSITION, task.getRowNumber());
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addNextIntentWithParentStack(intent);
        return taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Creates a PendingIntent for Notification action click.
     * This will broadcast the Intent to the {@link NotificationActionBroadcastReceiver}
     * for further processing of the given Action.
     *
     * @param context scope of the PendingIntent
     * @param action  String value
     * @return pendingIntent that represents the Notification action
     */
    private static PendingIntent createActionIntent(Context context, String action) {
        final Intent intent = new Intent(context.getApplicationContext(), NotificationActionBroadcastReceiver.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(CLASS_NAME, String.format("Received AlarmManager Intent=%s", intent.toString()));
        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final DailyDoDatabaseHelper dailyDoDatabaseHelper = DailyDoDatabaseHelper.getInstance(context);
        final List<Task> taskList = dailyDoDatabaseHelper.getUncheckedTaskEntries();
        final Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_logo)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true)
                .setOngoing(true)
                .addAction(R.drawable.ic_action_cancel,
                        context.getString(R.string.action_dismiss),
                        createActionIntent(context, ACTION_DISMISS));
        if (taskList != null && !taskList.isEmpty()) {
            final Task task = taskList.get(0);
            notificationBuilder
                    .setContentTitle(String.format("%d remaining DOs today", taskList.size()))
                    .setContentText(String.format("Ongoing: %s", task.getTitle()))
                    .setContentIntent(createNotificationClickIntent(context, task));
            if (taskList.size() > 1) {
                String bigTextMessage = "";
                for (int i = 0; i < taskList.size(); i++) {
                    bigTextMessage += taskList.get(i).getTitle() + "\n";
                }
                notificationBuilder.setStyle(new Notification.BigTextStyle().bigText(bigTextMessage.trim()));
            }
            final Notification notification = notificationBuilder.build();
            notificationManager.notify(ID_DAILY_DO_NOTIFICATION, notification);
        }
        AlarmService.scheduleNextAlarm(context);
    }
}
