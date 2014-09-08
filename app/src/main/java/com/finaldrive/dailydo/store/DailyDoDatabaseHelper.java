package com.finaldrive.dailydo.store;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.finaldrive.dailydo.domain.Alarm;
import com.finaldrive.dailydo.domain.Task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.finaldrive.dailydo.store.AlarmEntryContract.AlarmEntry;
import static com.finaldrive.dailydo.store.TaskEntryContract.TaskEntry;

/**
 * The database helper for this application.
 */
public class DailyDoDatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "DailyDo";
    private static final String CLASS_NAME = "DailyDoDatabaseHelper";
    private static final String CREATE_TASK_ENTRY_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " +
            TaskEntry.TABLE + " (" +
            TaskEntry._ID + " INTEGER PRIMARY KEY, " +
            TaskEntry.COLUMN_TITLE + " TEXT, " +
            TaskEntry.COLUMN_NOTE + " TEXT, " +
            TaskEntry.COLUMN_ROW_NUMBER + " INTEGER, " +
            TaskEntry.COLUMN_IS_CHECKED + " INTEGER)";
    private static final String CREATE_ALARM_ENTRY_TABLE_SQL = "CREATE TABLE IF NOT EXISTS " +
            AlarmEntry.TABLE + " (" +
            AlarmEntry._ID + " INTEGER PRIMARY KEY, " +
            AlarmEntry.COLUMN_HOUR + " INTEGER, " +
            AlarmEntry.COLUMN_MINUTE + " INTEGER, " +
            AlarmEntry.COLUMN_DAYS_REPEATING + " BYTE, " +
            AlarmEntry.COLUMN_IS_ENABLED + " INTEGER)";
    private static DailyDoDatabaseHelper dailyDoDatabaseHelper;

    private DailyDoDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Returns the DailyDoDatabaseHelper singleton to share across the applications lifecycle.
     * This should be used for all of the applications usages of the database.
     *
     * @param context
     * @return dailyDoDatabaseHelper
     */
    public static final DailyDoDatabaseHelper getInstance(Context context) {
        if (dailyDoDatabaseHelper == null) {
            dailyDoDatabaseHelper = new DailyDoDatabaseHelper(context.getApplicationContext());
        }
        return dailyDoDatabaseHelper;
    }

    /**
     * Returns a new instance of the DailyDoDatabaseHelper for testing.
     * This ensures the test data and the application data do not get mixed together.
     *
     * @param context
     * @return new instance of dailyDoDatabaseHelper
     */
    public static final DailyDoDatabaseHelper getTestInstance(Context context) {
        return new DailyDoDatabaseHelper(context);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TASK_ENTRY_TABLE_SQL);
        sqLiteDatabase.execSQL(CREATE_ALARM_ENTRY_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE " + TaskEntry.TABLE);
        sqLiteDatabase.execSQL("DROP TABLE " + AlarmEntry.TABLE);
        onCreate(sqLiteDatabase);
    }

    /**
     * AlarmEntry database methods
     */

    public Alarm insertAlarmEntry(Alarm alarm) {
        final SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        final ContentValues contentValues = new ContentValues();
        contentValues.put(AlarmEntry.COLUMN_HOUR, alarm.getHour());
        contentValues.put(AlarmEntry.COLUMN_MINUTE, alarm.getMinute());
        contentValues.put(AlarmEntry.COLUMN_DAYS_REPEATING, alarm.getDaysRepeating());
        contentValues.put(AlarmEntry.COLUMN_IS_ENABLED, alarm.getIsEnabled());
        final Long alarmId = sqLiteDatabase.insert(
                AlarmEntry.TABLE,
                null,
                contentValues);
        alarm.setId(alarmId.intValue());
        Log.d(CLASS_NAME, String.format("INSERT Alarm=%s", alarm.toString()));

        return alarm;
    }

    public Alarm getAlarmEntry(int alarmId) {
        final SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        final Cursor cursor = sqLiteDatabase.query(
                AlarmEntry.TABLE,
                null, // PROJECTION. Get all columns in the table.
                AlarmEntry._ID + " = ?", // WHERE clause.
                new String[]{String.valueOf(alarmId)}, // WHERE clause params.
                null, // GROUP. Not grouping by rows.
                null, // FILTER. Not filtering by row groups.
                null // SORT. Using default sort order.
        );
        Alarm alarm = null;
        if (cursor.getCount() > 0) {
            // Since we're fetching the AlarmEntry by the primary key, the top result should be the only result.
            cursor.moveToFirst();
            alarm = getAlarmFromCursor(cursor);
            Log.d(CLASS_NAME, String.format("GET Alarm=%s", alarm.toString()));
        }
        cursor.close();

        return alarm;
    }

    public Alarm updateAlarmEntry(Alarm alarm) {
        final SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        final ContentValues contentValues = new ContentValues();
        contentValues.put(AlarmEntry.COLUMN_HOUR, alarm.getHour());
        contentValues.put(AlarmEntry.COLUMN_MINUTE, alarm.getMinute());
        contentValues.put(AlarmEntry.COLUMN_DAYS_REPEATING, alarm.getDaysRepeating());
        contentValues.put(AlarmEntry.COLUMN_IS_ENABLED, alarm.getIsEnabled());
        sqLiteDatabase.update(
                AlarmEntry.TABLE,
                contentValues,
                AlarmEntry._ID + " = ?",
                new String[]{String.valueOf(alarm.getId())});
        Log.d(CLASS_NAME, String.format("UPDATE Alarm=%s", alarm.toString()));

        return alarm;
    }

    public void deleteAlarmEntry(int alarmId) {
        final SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete(
                AlarmEntry.TABLE,
                AlarmEntry._ID + " = ?",
                new String[]{String.valueOf(alarmId)});
        Log.d(CLASS_NAME, String.format("DELETE Alarm with AlarmId=%d", alarmId));
    }

    public List<Alarm> getAlarmEntries() {
        final SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        final Cursor cursor = sqLiteDatabase.query(
                AlarmEntry.TABLE,
                null, // PROJECTION. Get all columns in the table.
                null, // WHERE clause.
                null, // WHERE clause params.
                null, // GROUP. Not grouping by rows.
                null, // FILTER. Not filtering by row groups.
                AlarmEntry.COLUMN_HOUR + " ASC, " + AlarmEntry.COLUMN_MINUTE + " ASC" // SORT.
        );
        final List<Alarm> alarmList = new ArrayList<Alarm>(cursor.getCount());
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                if (cursor.moveToPosition(i)) {
                    alarmList.add(getAlarmFromCursor(cursor));
                } else {
                    Log.w(CLASS_NAME, String.format("Could not move Cursor to Position=%d", i));
                }
            }
        }
        cursor.close();

        return alarmList;
    }

    public List<Alarm> getEnabledAlarmEntries() {
        final SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        final Cursor cursor = sqLiteDatabase.query(
                AlarmEntry.TABLE,
                null, // PROJECTION. Get all columns in the table.
                AlarmEntry.COLUMN_IS_ENABLED + " = ?", // WHERE clause.
                new String[]{String.valueOf(1)}, // WHERE clause params.
                null, // GROUP. Not grouping by rows.
                null, // FILTER. Not filtering by row groups.
                AlarmEntry.COLUMN_HOUR + " ASC, " + AlarmEntry.COLUMN_MINUTE + " ASC" // SORT.
        );
        final List<Alarm> alarmList = new ArrayList<Alarm>(cursor.getCount());
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                if (cursor.moveToPosition(i)) {
                    alarmList.add(getAlarmFromCursor(cursor));
                } else {
                    Log.w(CLASS_NAME, String.format("Could not move Cursor to Position=%d", i));
                }
            }
        }
        cursor.close();

        return alarmList;
    }

    /**
     * Given the Calendar, find the nearest upcoming Alarm for calendar day.
     * That means it will NOT look into future days for an Alarm.
     *
     * @param calendar
     * @return alarm
     */
    public Alarm getNextAlarmForCalendarDay(Calendar calendar) {
        final byte todayAsByte = Alarm.getByteDayFromCalendarDay(calendar.get(Calendar.DAY_OF_WEEK));
        final SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        final Cursor cursor = sqLiteDatabase.query(
                AlarmEntry.TABLE,
                null, // PROJECTION. Get all columns in the table.
                AlarmEntry.COLUMN_IS_ENABLED + " = ? AND ((" +
                        AlarmEntry.COLUMN_HOUR + " = ? AND " +
                        AlarmEntry.COLUMN_MINUTE + " > ?) OR (" +
                        AlarmEntry.COLUMN_HOUR + " > ?)) AND ((" +
                        AlarmEntry.COLUMN_DAYS_REPEATING + " & ?) = CAST(? AS BYTE))", // WHERE clause.
                new String[]{
                        String.valueOf(1),
                        String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)),
                        String.valueOf(calendar.get(Calendar.MINUTE)),
                        String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)),
                        String.valueOf(todayAsByte),
                        String.valueOf(todayAsByte)}, // WHERE clause params.
                null, // GROUP. Not grouping by rows.
                null, // FILTER. Not filtering by row groups.
                AlarmEntry.COLUMN_HOUR + " ASC, " + AlarmEntry.COLUMN_MINUTE + " ASC" // SORT.
        );
        Alarm alarm = null;
        if (cursor.getCount() > 0) {
            // We only care about the very first Alarm as it is the most recent one.
            cursor.moveToFirst();
            alarm = getAlarmFromCursor(cursor);
            Log.d(CLASS_NAME, String.format("GET next upcoming Alarm=%s", alarm.toString()));
        }
        cursor.close();

        return alarm;
    }

    /**
     * TaskEntry database methods
     */

    public Task insertTaskEntry(Task task) {
        final SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        final ContentValues contentValues = new ContentValues();
        contentValues.put(TaskEntry.COLUMN_TITLE, task.getTitle());
        contentValues.put(TaskEntry.COLUMN_NOTE, task.getNote());
        contentValues.put(TaskEntry.COLUMN_ROW_NUMBER, task.getRowNumber());
        contentValues.put(TaskEntry.COLUMN_IS_CHECKED, task.getIsChecked());
        final Long taskId = sqLiteDatabase.insert(
                TaskEntry.TABLE,
                null,
                contentValues);
        task.setId(taskId.intValue());
        Log.d(CLASS_NAME, String.format("INSERT Task=%s", task.toString()));

        return task;
    }

    public Task getTaskEntry(int taskId) {
        final SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        final Cursor cursor = sqLiteDatabase.query(
                TaskEntry.TABLE,
                null, // PROJECTION. Get all columns in the table.
                TaskEntry._ID + " = ?", // WHERE clause.
                new String[]{String.valueOf(taskId)}, // WHERE clause params.
                null, // GROUP. Not grouping by rows.
                null, // FILTER. Not filtering by row groups.
                null // SORT. Using default sort order.
        );
        Task task = null;
        if (cursor.getCount() > 0) {
            // Since we're fetching the TaskEntry by the primary key, the top result should be the only result.
            cursor.moveToFirst();
            task = getTaskFromCursor(cursor);
            Log.d(CLASS_NAME, String.format("GET Task=%s", task.toString()));
        }
        cursor.close();

        return task;
    }

    public Task updateTaskEntry(Task task) {
        final SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        final ContentValues contentValues = new ContentValues();
        contentValues.put(TaskEntry.COLUMN_TITLE, task.getTitle());
        contentValues.put(TaskEntry.COLUMN_NOTE, task.getNote());
        contentValues.put(TaskEntry.COLUMN_ROW_NUMBER, task.getRowNumber());
        contentValues.put(TaskEntry.COLUMN_IS_CHECKED, task.getIsChecked());
        sqLiteDatabase.update(
                TaskEntry.TABLE,
                contentValues,
                TaskEntry._ID + " = ?",
                new String[]{String.valueOf(task.getId())});
        Log.d(CLASS_NAME, String.format("UPDATE Task=%s", task.toString()));

        return task;
    }

    public void deleteTaskEntry(int taskId) {
        final SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete(
                TaskEntry.TABLE,
                TaskEntry._ID + " = ?",
                new String[]{String.valueOf(taskId)});
        Log.d(CLASS_NAME, String.format("DELETE Task with TaskId=%d", taskId));
    }

    public List<Task> getTaskEntries() {
        final SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        final Cursor cursor = sqLiteDatabase.query(
                TaskEntry.TABLE,
                null, // PROJECTION. Get all columns in the table.
                null, // WHERE clause.
                null, // WHERE clause params.
                null, // GROUP. Not grouping by rows.
                null, // FILTER. Not filtering by row groups.
                TaskEntry.COLUMN_ROW_NUMBER + " ASC" // SORT.
        );
        final List<Task> taskList = new ArrayList<Task>(cursor.getCount());
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                if (cursor.moveToPosition(i)) {
                    taskList.add(getTaskFromCursor(cursor));
                } else {
                    Log.w(CLASS_NAME, String.format("Could not move Cursor to Position=%d", i));
                }
            }
        }
        cursor.close();

        return taskList;
    }

    public List<Task> getUncheckedTaskEntries() {
        final SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        final Cursor cursor = sqLiteDatabase.query(
                TaskEntry.TABLE,
                null, // PROJECTION. Get all columns in the table.
                TaskEntry.COLUMN_IS_CHECKED + " = ?", // WHERE clause.
                new String[]{String.valueOf(0)}, // WHERE clause params.
                null, // GROUP. Not grouping by rows.
                null, // FILTER. Not filtering by row groups.
                TaskEntry.COLUMN_ROW_NUMBER + " ASC" // SORT.
        );
        final List<Task> taskList = new ArrayList<Task>(cursor.getCount());
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                if (cursor.moveToPosition(i)) {
                    taskList.add(getTaskFromCursor(cursor));
                } else {
                    Log.w(CLASS_NAME, String.format("Could not move Cursor to Position=%d", i));
                }
            }
        }
        cursor.close();

        return taskList;
    }

    public void uncheckAllTaskEntries() {
        final SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        final ContentValues contentValues = new ContentValues();
        contentValues.put(TaskEntry.COLUMN_IS_CHECKED, 0);
        final int numRows = sqLiteDatabase.update(
                TaskEntry.TABLE,
                contentValues,
                TaskEntry.COLUMN_IS_CHECKED + " = ?",
                new String[]{String.valueOf(1)});
        Log.d(CLASS_NAME, String.format("Updated %d Task entries to be unchecked (0).", numRows));
    }

    /**
     * Takes the Cursor and attempts to get an Alarm from it.
     * This method assumes you have already moved the provided Cursor to the correct position.
     *
     * @param cursor
     * @return alarm
     */
    private Alarm getAlarmFromCursor(final Cursor cursor) {
        final Alarm alarm = new Alarm();
        alarm.setId(cursor.getInt(0));
        alarm.setHour(cursor.getInt(1));
        alarm.setMinute(cursor.getInt(2));
        alarm.setDaysRepeating(Byte.parseByte(cursor.getString(3)));
        alarm.setIsEnabled(cursor.getInt(4));

        return alarm;
    }

    /**
     * Takes the Cursor and attempts to get a Task from it.
     * This method assumes you have already moved the provided Cursor to the correct position.
     *
     * @param cursor
     * @return task
     */
    private Task getTaskFromCursor(final Cursor cursor) {
        final Task task = new Task();
        task.setId(cursor.getInt(0));
        task.setTitle(cursor.getString(1));
        task.setNote(cursor.getString(2));
        task.setRowNumber(cursor.getInt(3));
        task.setIsChecked(cursor.getInt(4));

        return task;
    }
}
