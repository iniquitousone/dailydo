package com.finaldrive.dailydo.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.finaldrive.dailydo.R;
import com.finaldrive.dailydo.TaskDetailsActivity;
import com.finaldrive.dailydo.domain.Task;
import com.finaldrive.dailydo.service.AlarmService;
import com.finaldrive.dailydo.store.DailyDoDatabaseHelper;

import java.util.List;

/**
 * Listens to Intent(s) triggered by AlarmManager to send Notification(s) to the user.
 * Is responsible for notifying the user about the upcoming Task if there are any remaining.
 */
public class AlarmBroadcastReceiver extends BroadcastReceiver {

    /**
     * Dedicated notification ID so we always reuse the same notification rather than create many unique ones.
     */
    private static final int ID_DAILY_DO_NOTIFICATION = 0;
    private NotificationManager notificationManager;
    private DailyDoDatabaseHelper dailyDoDatabaseHelper;

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
    private static PendingIntent createPendingIntent(Context context, Task task) {
        final Intent intent = new Intent(context.getApplicationContext(), TaskDetailsActivity.class);
        intent.putExtra(TaskDetailsActivity.EXTRA_TASK_ID, task.getId());
        intent.putExtra(TaskDetailsActivity.EXTRA_TASK_POSITION, task.getRowNumber());
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addNextIntentWithParentStack(intent);
        return taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        dailyDoDatabaseHelper = DailyDoDatabaseHelper.getInstance(context);
        final List<Task> taskList = dailyDoDatabaseHelper.getUncheckedTaskEntries();
        Notification.Builder notificationBuilder = new Notification.Builder(context)
                .setSmallIcon(R.drawable.ic_logo)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true);
        if (taskList != null && !taskList.isEmpty()) {
            final Task task = taskList.get(0);
            notificationBuilder.setContentTitle(task.getTitle())
                    .setContentText(task.getNote())
                    .setContentInfo(taskList.size() + " left")
                    .setContentIntent(createPendingIntent(context, task));
            final Notification notification = notificationBuilder.build();
            notificationManager.notify(ID_DAILY_DO_NOTIFICATION, notification);
        }
        AlarmService.scheduleNextAlarm(context);
    }
}
