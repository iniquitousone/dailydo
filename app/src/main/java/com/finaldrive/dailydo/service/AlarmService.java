package com.finaldrive.dailydo.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.finaldrive.dailydo.R;
import com.finaldrive.dailydo.domain.Alarm;
import com.finaldrive.dailydo.receiver.AlarmBroadcastReceiver;
import com.finaldrive.dailydo.receiver.DailyResetBroadcastReceiver;
import com.finaldrive.dailydo.store.DailyDoDatabaseHelper;

import java.util.Calendar;

/**
 * Async service for handling the scheduling (and cancelling) of Alarm(s) using the AlarmManager system service.
 * This Service will remain available until all PendingIntent(s) are handled at the end of onHandleEvent.
 *
 * @see android.app.IntentService
 */
public class AlarmService extends IntentService {

    public static final String ACTION_SCHEDULE_NEXT_ALARM = "com.finaldrive.dailydo.service.action.SCHEDULE_NEXT_ALARM";
    public static final String ACTION_SCHEDULE_SNOOZE = "com.finaldrive.dailydo.service.action.SCHEDULE_SNOOZE";
    public static final String ACTION_SCHEDULE_NEXT_RESET = "com.finaldrive.dailydo.service.action.SCHEDULE_NEXT_RESET";
    public static final String EXTRA_ALARM_ID = "com.finaldrive.dailydo.service.extra.ALARM_ID";
    public static final String EXTRA_SNOOZE_DURATION = "com.finaldrive.dailydo.service.extra.SNOOZE_DURATION";
    private static final int ID_SNOOZE = 999;
    private static final String CLASS_NAME = "AlarmService";
    private static Alarm nextAlarm;
    private AlarmManager alarmManager;
    private DailyDoDatabaseHelper dailyDoDatabaseHelper;

    public AlarmService() {
        super("AlarmService");
    }

    /**
     * Entry point to scheduling an Alarm. It fires an Intent to schedule the next upcoming Alarm.
     * Takes care of cancelling any already pending Alarm(s) since we only want the next upcoming.
     *
     * @param context
     */
    public static void scheduleNextAlarm(Context context) {
        final Intent intent = new Intent(context, AlarmService.class);
        intent.setAction(ACTION_SCHEDULE_NEXT_ALARM);
        context.startService(intent);
    }

    /**
     * Entry point to scheduling an Alarm snooze based on current time.
     *
     * @param context
     * @param minutes
     */
    public static void scheduleSnooze(Context context, int minutes) {
        final Intent intent = new Intent(context, AlarmService.class);
        intent.setAction(ACTION_SCHEDULE_SNOOZE);
        intent.putExtra(EXTRA_SNOOZE_DURATION, minutes);
        context.startService(intent);
    }

    /**
     * Entry point to scheduling the next daily reset.
     *
     * @param context
     */
    public static void scheduleNextReset(Context context) {
        final Intent intent = new Intent(context, AlarmService.class);
        intent.setAction(ACTION_SCHEDULE_NEXT_RESET);
        context.startService(intent);
    }

    /**
     * Creates a unique PendingIntent (uniqueness determined by {@link Intent#filterEquals}) for scheduling an alarm.
     *
     * @param context
     * @param alarmId
     * @return pendingIntent
     */
    private static PendingIntent createAlarmIntent(Context context, int alarmId) {
        final Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        intent.putExtra(EXTRA_ALARM_ID, alarmId);
        return PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Creates non-unique PendingIntent for scheduling the next daily reset.
     *
     * @param context
     * @return pendingIntent
     */
    private static PendingIntent createResetIntent(Context context) {
        final Intent intent = new Intent(context, DailyResetBroadcastReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        dailyDoDatabaseHelper = DailyDoDatabaseHelper.getInstance(this);
    }

    /**
     * Takes the provided Intent and schedules the appropriate Alarm.
     *
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        final String action = intent.getAction();
        if (ACTION_SCHEDULE_NEXT_ALARM.equals(action)) {
            scheduleNextAlarm();
        } else if (ACTION_SCHEDULE_NEXT_RESET.equals(action)) {
            scheduleNextReset();
        } else if (ACTION_SCHEDULE_SNOOZE.equals(action)) {
            scheduleSnooze(intent.getIntExtra(EXTRA_SNOOZE_DURATION, 5));
        }
    }

    /**
     * Schedules the next upcoming Alarm. This is done by taking the current time and checking for future Alarm(s).
     * Handles cancelling the current Alarm.
     */
    private void scheduleNextAlarm() {
        // Cancel the currently scheduled Alarm in order to schedule the next.
        if (nextAlarm != null) {
            alarmManager.cancel(createAlarmIntent(this, nextAlarm.getId()));
            nextAlarm = null;
        }
        final Calendar calendar = Calendar.getInstance();
        Alarm alarm;
        int i = 0;
        do {
            // Look for an Alarm that is upcoming from the Calendar day and time.
            alarm = dailyDoDatabaseHelper.getNextAlarmForCalendarDay(calendar);
            if (alarm == null) {
                // If an Alarm was not found for the remainder of the day,
                // then check the next day starting midnight.
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.add(Calendar.DAY_OF_WEEK, 1);
                Log.d(CLASS_NAME, String.format("Checking next Calendar=%s for Alarm.", calendar));
            }
        } while (alarm == null && i++ < 7);
        if (alarm != null) {
            nextAlarm = alarm;
            // Reusing the previous Calendar because it has scope of the future day.
            calendar.set(Calendar.HOUR_OF_DAY, alarm.getHour());
            calendar.set(Calendar.MINUTE, alarm.getMinute());
            calendar.set(Calendar.SECOND, 0);
            alarmManager.set(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    createAlarmIntent(this, alarm.getId()));
            Log.d(CLASS_NAME, String.format("Alarm=%s was found, scheduled on Calendar=%s", alarm.toString(), calendar.toString()));
        } else {
            Log.d(CLASS_NAME, "No upcoming Alarm(s) found to schedule.");
        }
    }

    /**
     * Schedules a snooze for the provided number of minutes.
     *
     * @param minutes
     */
    private void scheduleSnooze(int minutes) {
        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, minutes);
        alarmManager.set(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                createAlarmIntent(this, ID_SNOOZE));
        Log.d(CLASS_NAME, String.format("Scheduled snooze for Minutes=%d", minutes));
    }

    /**
     * Schedules the next daily reset with the AlarmManager.
     */
    private void scheduleNextReset() {
        alarmManager.cancel(createResetIntent(this));
        final SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.pref_daily_do), Context.MODE_PRIVATE);
        final boolean isDailyResetEnabled = sharedPreferences.getBoolean(getString(R.string.pref_daily_reset_enabled), true);
        if (isDailyResetEnabled) {
            final int hourOfReset = sharedPreferences.getInt(getString(R.string.pref_daily_reset_hour), 0);
            final int minuteOfReset = sharedPreferences.getInt(getString(R.string.pref_daily_reset_minute), 0);
            final Calendar calendarToRepeat = Calendar.getInstance();
            calendarToRepeat.set(Calendar.HOUR_OF_DAY, hourOfReset);
            calendarToRepeat.set(Calendar.MINUTE, minuteOfReset);
            calendarToRepeat.set(Calendar.SECOND, 0);
            if (calendarToRepeat.before(Calendar.getInstance())) {
                calendarToRepeat.add(Calendar.DAY_OF_WEEK, 1);
            }
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    calendarToRepeat.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    createResetIntent(this));
            Log.d(CLASS_NAME, String.format("Next reset scheduled on Calendar=%s", calendarToRepeat.toString()));
        } else {
            Log.d(CLASS_NAME, String.format("Resets cancelled."));
        }
    }
}
