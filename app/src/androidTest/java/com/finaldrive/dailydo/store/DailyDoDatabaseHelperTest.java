package com.finaldrive.dailydo.store;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import com.finaldrive.dailydo.domain.Alarm;
import com.finaldrive.dailydo.domain.Task;

import java.util.Calendar;
import java.util.List;

public class DailyDoDatabaseHelperTest extends AndroidTestCase {

    private DailyDoDatabaseHelper dailyDoDatabaseHelper;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        RenamingDelegatingContext renamingDelegatingContext =
                new RenamingDelegatingContext(getContext(), "dailyDoDatabaseHelper");
        dailyDoDatabaseHelper = DailyDoDatabaseHelper.getTestInstance(renamingDelegatingContext);
    }

    public void testInsertAlarmEntry() throws Exception {
        Alarm alarm = new Alarm(9, 00);
        alarm = dailyDoDatabaseHelper.insertAlarmEntry(alarm);
        assertTrue(alarm.getId() > -1);
        assertTrue(alarm.getHour() == 9);
        assertTrue(alarm.getMinute() == 0);
        assertTrue(alarm.getDaysRepeating() == Alarm.NONE);
        assertTrue(alarm.getIsEnabled() == 1);
    }

    public void testUpdateAlarmEntry() throws Exception {
        Alarm alarm = new Alarm(9, 00);
        alarm = dailyDoDatabaseHelper.insertAlarmEntry(alarm);
        assertTrue(alarm.getId() > -1);
        assertTrue(alarm.getHour() == 9);
        assertTrue(alarm.getMinute() == 0);
        assertTrue(alarm.getDaysRepeating() == Alarm.NONE);
        assertTrue(alarm.getIsEnabled() == 1);

        alarm.setHour(10);
        alarm.setDaysRepeating(Alarm.FRIDAY);
        Alarm persistedAlarm = dailyDoDatabaseHelper.updateAlarmEntry(alarm);
        assertTrue(persistedAlarm.getId() == alarm.getId());
        assertTrue(persistedAlarm.getHour() == 10);
        assertTrue(persistedAlarm.getMinute() == 0);
        assertTrue(alarm.getDaysRepeating() == Alarm.FRIDAY);
        assertTrue(persistedAlarm.getIsEnabled() == 1);
    }

    public void testGetAlarmEntry() throws Exception {
        Alarm alarm = new Alarm(9, 00);
        alarm = dailyDoDatabaseHelper.insertAlarmEntry(alarm);
        assertTrue(alarm.getId() > -1);
        assertTrue(alarm.getHour() == 9);
        assertTrue(alarm.getMinute() == 0);
        assertTrue(alarm.getDaysRepeating() == Alarm.NONE);
        assertTrue(alarm.getIsEnabled() == 1);

        alarm = dailyDoDatabaseHelper.getAlarmEntry(alarm.getId());
        assertTrue(alarm.getId() > -1);
        assertTrue(alarm.getHour() == 9);
        assertTrue(alarm.getMinute() == 0);
        assertTrue(alarm.getDaysRepeating() == Alarm.NONE);
        assertTrue(alarm.getIsEnabled() == 1);
    }

    public void testGetAlarmEntries_Many() throws Exception {
        for (int i = 0; i < 5; i++) {
            Alarm alarm = new Alarm(i, 00);
            alarm = dailyDoDatabaseHelper.insertAlarmEntry(alarm);
            assertTrue(alarm.getId() > -1);
        }
        List<Alarm> alarmList = dailyDoDatabaseHelper.getAlarmEntries();
        assertTrue(alarmList != null);
        assertTrue(alarmList.size() == 5);
        for (int i = 0; i < 5; i++) {
            Alarm alarm = alarmList.get(i);
            assertTrue(alarm != null);
            assertTrue(alarm.getId() > -1);
            assertTrue(alarm.getHour() == i);
            assertTrue(alarm.getMinute() == 0);
            assertTrue(alarm.getDaysRepeating() == Alarm.NONE);
            assertTrue(alarm.getIsEnabled() == 1);
        }
    }

    public void testGetNextAlarmForCalendarDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        Alarm alarm1 = new Alarm(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        alarm1.setDaysRepeating(Alarm.getByteDayFromCalendarDay(calendar.get(Calendar.DAY_OF_WEEK)));
        alarm1 = dailyDoDatabaseHelper.insertAlarmEntry(alarm1);
        assertTrue(alarm1.getId() > -1);
        assertTrue(alarm1.getHour() == calendar.get(Calendar.HOUR_OF_DAY));
        assertTrue(alarm1.getMinute() == calendar.get(Calendar.MINUTE));
        assertTrue(alarm1.getDaysRepeating() == Alarm.getByteDayFromCalendarDay(calendar.get(Calendar.DAY_OF_WEEK)));
        assertTrue(alarm1.getIsEnabled() == 1);
        Alarm persistedAlarm = dailyDoDatabaseHelper.getNextAlarmForCalendarDay(Calendar.getInstance());
        assertTrue(persistedAlarm != null);
        assertTrue(persistedAlarm.getId() == alarm1.getId());
        assertTrue(persistedAlarm.getHour() == alarm1.getHour());
        assertTrue(persistedAlarm.getMinute() == alarm1.getMinute());
        assertTrue(persistedAlarm.getDaysRepeating() == alarm1.getDaysRepeating());
        assertTrue(persistedAlarm.getIsEnabled() == alarm1.getIsEnabled());

        calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        Alarm alarm2 = new Alarm(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        alarm2.setDaysRepeating(Alarm.getByteDayFromCalendarDay(calendar.get(Calendar.DAY_OF_WEEK)));
        alarm2 = dailyDoDatabaseHelper.insertAlarmEntry(alarm2);
        assertTrue(alarm2.getId() > -1);
        assertTrue(alarm2.getHour() == calendar.get(Calendar.HOUR_OF_DAY));
        assertTrue(alarm2.getMinute() == calendar.get(Calendar.MINUTE));
        assertTrue(alarm2.getDaysRepeating() == Alarm.getByteDayFromCalendarDay(calendar.get(Calendar.DAY_OF_WEEK)));
        assertTrue(alarm2.getIsEnabled() == 1);
        persistedAlarm = dailyDoDatabaseHelper.getNextAlarmForCalendarDay(Calendar.getInstance());
        assertTrue(persistedAlarm != null);
        assertTrue(persistedAlarm.getId() == alarm2.getId());
        assertTrue(persistedAlarm.getHour() == alarm2.getHour());
        assertTrue(persistedAlarm.getMinute() == alarm2.getMinute());
        assertTrue(persistedAlarm.getDaysRepeating() == alarm2.getDaysRepeating());
        assertTrue(persistedAlarm.getIsEnabled() == alarm2.getIsEnabled());

        calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -5);
        Alarm alarm3 = new Alarm(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        alarm3.setDaysRepeating(Alarm.getByteDayFromCalendarDay(calendar.get(Calendar.DAY_OF_WEEK)));
        alarm3 = dailyDoDatabaseHelper.insertAlarmEntry(alarm3);
        assertTrue(alarm3.getId() > -1);
        assertTrue(alarm3.getHour() == calendar.get(Calendar.HOUR_OF_DAY));
        assertTrue(alarm3.getMinute() == calendar.get(Calendar.MINUTE));
        assertTrue(alarm3.getDaysRepeating() == Alarm.getByteDayFromCalendarDay(calendar.get(Calendar.DAY_OF_WEEK)));
        assertTrue(alarm3.getIsEnabled() == 1);
        persistedAlarm = dailyDoDatabaseHelper.getNextAlarmForCalendarDay(Calendar.getInstance());
        assertTrue(persistedAlarm != null);
        assertTrue(persistedAlarm.getId() == alarm2.getId());
        assertTrue(persistedAlarm.getHour() == alarm2.getHour());
        assertTrue(persistedAlarm.getMinute() == alarm2.getMinute());
        assertTrue(persistedAlarm.getDaysRepeating() == alarm2.getDaysRepeating());
        assertTrue(persistedAlarm.getIsEnabled() == alarm2.getIsEnabled());

        calendar = Calendar.getInstance();
        Alarm alarm4 = new Alarm(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        alarm4.setDaysRepeating(Alarm.getByteDayFromCalendarDay(calendar.get(Calendar.DAY_OF_WEEK)));
        alarm4 = dailyDoDatabaseHelper.insertAlarmEntry(alarm4);
        assertTrue(alarm4.getId() > -1);
        assertTrue(alarm4.getHour() == calendar.get(Calendar.HOUR_OF_DAY));
        assertTrue(alarm4.getMinute() == calendar.get(Calendar.MINUTE));
        assertTrue(alarm4.getDaysRepeating() == Alarm.getByteDayFromCalendarDay(calendar.get(Calendar.DAY_OF_WEEK)));
        assertTrue(alarm4.getIsEnabled() == 1);
        persistedAlarm = dailyDoDatabaseHelper.getNextAlarmForCalendarDay(calendar);
        assertTrue(persistedAlarm != null);
        assertTrue(persistedAlarm.getId() == alarm2.getId());
        assertTrue(persistedAlarm.getHour() == alarm2.getHour());
        assertTrue(persistedAlarm.getMinute() == alarm2.getMinute());
        assertTrue(persistedAlarm.getDaysRepeating() == alarm2.getDaysRepeating());
        assertTrue(persistedAlarm.getIsEnabled() == alarm2.getIsEnabled());
    }

    public void testInsertTaskEntry() throws Exception {
        Task task = new Task("Task", "Note", 0);
        task = dailyDoDatabaseHelper.insertTaskEntry(task);
        assertTrue(task.getId() > -1);
        assertTrue(task.getTitle().equals("Task"));
        assertTrue(task.getNote().equals("Note"));
        assertTrue(task.getRowNumber() == 0);
        assertTrue(task.getIsChecked() == 0);
    }

    public void testUpdateTaskEntry() throws Exception {
        Task task = new Task("Task", "Note", 0);
        task = dailyDoDatabaseHelper.insertTaskEntry(task);
        assertTrue(task.getId() > -1);
        assertTrue(task.getTitle().equals("Task"));
        assertTrue(task.getNote().equals("Note"));
        assertTrue(task.getRowNumber() == 0);
        assertTrue(task.getIsChecked() == 0);

        task.setTitle("TaskEntry");
        Task persistedTask = dailyDoDatabaseHelper.updateTaskEntry(task);
        assertTrue(persistedTask.getId() == task.getId());
        assertTrue(persistedTask.getTitle().equals("TaskEntry"));
        assertTrue(persistedTask.getNote().equals("Note"));
        assertTrue(persistedTask.getRowNumber() == 0);
        assertTrue(persistedTask.getIsChecked() == 0);
    }

    public void testGetTaskEntry() throws Exception {
        Task task = new Task("Task", "Note", 0);
        task = dailyDoDatabaseHelper.insertTaskEntry(task);
        int rowId = task.getId();
        assertTrue(task.getId() > -1);
        assertTrue(task.getTitle().equals("Task"));
        assertTrue(task.getNote().equals("Note"));
        assertTrue(task.getRowNumber() == 0);
        assertTrue(task.getIsChecked() == 0);

        task = dailyDoDatabaseHelper.getTaskEntry(task.getId());
        assertTrue(task != null);
        assertTrue(task.getId() == rowId);
        assertTrue(task.getTitle().equals("Task"));
        assertTrue(task.getNote().equals("Note"));
        assertTrue(task.getRowNumber() == 0);
        assertTrue(task.getIsChecked() == 0);
    }

    public void testDeleteTaskEntry() throws Exception {
        Task task = new Task("Task", "Note", 0);
        task = dailyDoDatabaseHelper.insertTaskEntry(task);
        assertTrue(task.getId() > -1);
        dailyDoDatabaseHelper.deleteTaskEntry(task.getId());
        assertTrue(dailyDoDatabaseHelper.getTaskEntry(task.getId()) == null);
    }

    public void testGetTaskEntries_Many() throws Exception {
        for (int i = 0; i < 5; i++) {
            Task task = new Task("Task:" + i, "Note", i);
            task = dailyDoDatabaseHelper.insertTaskEntry(task);
            assertTrue(task.getId() > -1);
        }
        List<Task> taskList = dailyDoDatabaseHelper.getTaskEntries();
        assertTrue(taskList != null);
        assertTrue(taskList.size() == 5);
        for (int i = 0; i < 5; i++) {
            Task task = taskList.get(i);
            assertTrue(task != null);
            assertTrue(task.getTitle().equals("Task:" + i));
            assertTrue(task.getNote().equals("Note"));
        }
    }

    public void testUncheckAllTaskEntries() throws Exception {
        for (int i = 0; i < 5; i++) {
            Task task = new Task("Task:" + i, "Note", i);
            task.setIsChecked(1);
            task = dailyDoDatabaseHelper.insertTaskEntry(task);
            assertTrue(task.getId() > -1);
        }
        dailyDoDatabaseHelper.uncheckAllTaskEntries();
        assertTrue(dailyDoDatabaseHelper.getUncheckedTaskEntries().size() == 5);
    }
}