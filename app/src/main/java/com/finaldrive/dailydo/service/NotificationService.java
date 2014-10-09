package com.finaldrive.dailydo.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.finaldrive.dailydo.MainActivity;
import com.finaldrive.dailydo.R;
import com.finaldrive.dailydo.SnoozePickerActivity;
import com.finaldrive.dailydo.TaskDetailsActivity;
import com.finaldrive.dailydo.domain.Task;
import com.finaldrive.dailydo.receiver.NotificationActionBroadcastReceiver;
import com.finaldrive.dailydo.store.DailyDoDatabaseHelper;

import java.util.List;

import static com.finaldrive.dailydo.receiver.NotificationActionBroadcastReceiver.ACTION_DISMISS;

/**
 * Provides an async way for the MainActivity to update the Notification if it is present.
 */
public class NotificationService extends IntentService {

    /**
     * Dedicated notification ID so we always reuse the same notification rather than create many unique ones.
     */
    public static final int ID_DAILY_DO_NOTIFICATION = 0;
    private static final String CLASS_NAME = "NotificationService";
    private static final String ACTION_CREATE_NOTIFICATION = "com.finaldrive.dailydo.service.action.CREATE_NOTIFICATION";
    private static final String ACTION_UPDATE_NOTIFICATION = "com.finaldrive.dailydo.service.action.UPDATE_NOTIFICATION";
    private static final String ACTION_CANCEL_NOTIFICATION = "com.finaldrive.dailydo.service.action.CANCEL_NOTIFICATION";
    private static final String EXTRA_TASK_ID = "com.finaldrive.dailydo.service.extra.TASK_ID";
    private static final String EXTRA_TASK_TITLE = "com.finaldrive.dailydo.service.extra.TASK_TITLE";
    private static final String EXTRA_TASK_IS_CHECKED = "com.finaldrive.dailydo.service.extra.IS_CHECKED";
    private static final int INVALID_ID = -99;
    private DailyDoDatabaseHelper dailyDoDatabaseHelper;
    private NotificationManager notificationManager;
    private List<Task> taskList;

    public NotificationService() {
        super("NotificationUpdaterService");
    }

    private static void setIsPresent(Context context) {
        setNotificationStatus(context, true);
    }

    private static void setNotPresent(Context context) {
        setNotificationStatus(context, false);
    }

    private static void setNotificationStatus(Context context, boolean status) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.pref_daily_do),
                Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(context.getString(R.string.pref_notified), status);
        editor.commit();
    }

    private static boolean isPresent(Context context) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.pref_daily_do),
                Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(context.getString(R.string.pref_notified), false);
    }

    public static void startNotificationCreate(Context context) {
        final Intent intent = new Intent(context, NotificationService.class);
        intent.setAction(ACTION_CREATE_NOTIFICATION);
        context.startService(intent);
    }

    public static void startNotificationUpdate(Context context, int taskId, boolean isChecked) {
        if (!isPresent(context)) {
            return;
        }
        final Intent intent = new Intent(context, NotificationService.class);
        intent.setAction(ACTION_UPDATE_NOTIFICATION);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        intent.putExtra(EXTRA_TASK_IS_CHECKED, isChecked);
        context.startService(intent);
    }

    public static void startNotificationCancel(Context context) {
        final Intent intent = new Intent(context, NotificationService.class);
        intent.setAction(ACTION_CANCEL_NOTIFICATION);
        context.startService(intent);
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
        final Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(TaskDetailsActivity.EXTRA_TASK_ID, task.getId());
        intent.putExtra(TaskDetailsActivity.EXTRA_TASK_POSITION, task.getRowNumber());
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        final TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addNextIntentWithParentStack(intent);
        return taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Creates a PendingIntent for Notification action click.
     * This will broadcast the Intent to the {@link com.finaldrive.dailydo.receiver.NotificationActionBroadcastReceiver}
     * for further processing of the given Action.
     *
     * @param context scope of the PendingIntent
     * @param action  String value
     * @return pendingIntent that represents the Notification action
     */
    private static PendingIntent createActionIntent(Context context, String action) {
        final Intent intent = new Intent(context, NotificationActionBroadcastReceiver.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Creates a PendingIntent for the Snooze action click.
     * This will send the user to the {@link com.finaldrive.dailydo.SnoozePickerActivity} to pick a snooze duration.
     *
     * @param context
     * @return pendingIntent to direct to SnoozePickerActivity
     */
    private static PendingIntent createSnoozeIntent(Context context) {
        final Intent intent = new Intent(context, SnoozePickerActivity.class);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void handleNotification(boolean isAlertOnce) {
        int remaining = 0;
        Task ongoingTask = null;
        String bigTextMessage = "";
        for (int i = 0; i < taskList.size(); i++) {
            final Task task = taskList.get(i);
            if (task == null) {
                continue;
            }
            remaining++;
            if (ongoingTask == null) {
                ongoingTask = task;
            }
            if (remaining <= 5) {
                bigTextMessage += task.getTitle() + "\n";
            }
        }
        if (remaining == 0) {
            cancelNotification();
            return;
        } else if (remaining > 5) {
            bigTextMessage += "...";
        }

        final Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setOnlyAlertOnce(isAlertOnce)
                .setSmallIcon(R.drawable.ic_notification)
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(false)
                .setOngoing(true)
                .addAction(R.drawable.ic_action_alarms,
                        getString(R.string.action_snooze),
                        createSnoozeIntent(this))
                .addAction(R.drawable.ic_action_cancel,
                        getString(R.string.action_dismiss),
                        createActionIntent(this, ACTION_DISMISS))
                .setContentTitle(String.format("%d remaining DO(s) today", remaining))
                .setContentText(String.format("Ongoing: %s", ongoingTask.getTitle()))
                .setContentIntent(createNotificationClickIntent(this, ongoingTask));
        if (remaining > 1) {
            notificationBuilder.setStyle(new Notification.BigTextStyle().bigText(bigTextMessage.trim()));
        }
        notificationManager.notify(ID_DAILY_DO_NOTIFICATION, notificationBuilder.build());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dailyDoDatabaseHelper = DailyDoDatabaseHelper.getInstance(this);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        taskList = dailyDoDatabaseHelper.getTaskEntriesCheckedAsNull();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CREATE_NOTIFICATION.equals(action)) {
                createNotification();
            } else if (ACTION_UPDATE_NOTIFICATION.equals(action)) {
                final int taskId = intent.getIntExtra(EXTRA_TASK_ID, INVALID_ID);
                final boolean isChecked = intent.getBooleanExtra(EXTRA_TASK_IS_CHECKED, false);
                updateNotification(taskId, isChecked);
            } else if (ACTION_CANCEL_NOTIFICATION.equals(action)) {
                cancelNotification();
            }
        }
    }

    private void createNotification() {
        if (!taskList.isEmpty()) {
            handleNotification(false);
            setIsPresent(this);
        }
    }

    private void updateNotification(int taskId, boolean isChecked) {
        if (taskList.isEmpty()) {
            cancelNotification();
        } else {
            if (taskId == INVALID_ID) {
                return;
            }
            final Task updatedTask = dailyDoDatabaseHelper.getTaskEntry(taskId);
            if (isChecked) {
                // If checked, that means the Task is done, so remove it from the notification.
                taskList.set(updatedTask.getRowNumber(), null);
            } else {
                // Has been unchecked, that means add it back into the notification.
                taskList.set(updatedTask.getRowNumber(), updatedTask);
            }
            handleNotification(true);
        }
    }

    private void cancelNotification() {
        Log.d(CLASS_NAME, "Cancelled notification.");
        notificationManager.cancel(NotificationService.ID_DAILY_DO_NOTIFICATION);
        setNotPresent(this);
    }
}
