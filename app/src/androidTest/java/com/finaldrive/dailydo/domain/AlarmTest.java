package com.finaldrive.dailydo.domain;

import android.test.AndroidTestCase;

import java.util.Calendar;

public class AlarmTest extends AndroidTestCase {

    public void testGetByteDayFromCalendarDay() {
        assertTrue(Alarm.SUNDAY == Alarm.getByteDayFromCalendarDay(Calendar.SUNDAY));
        assertTrue(Alarm.MONDAY == Alarm.getByteDayFromCalendarDay(Calendar.MONDAY));
        assertTrue(Alarm.TUESDAY == Alarm.getByteDayFromCalendarDay(Calendar.TUESDAY));
        assertTrue(Alarm.WEDNESDAY == Alarm.getByteDayFromCalendarDay(Calendar.WEDNESDAY));
        assertTrue(Alarm.THURSDAY == Alarm.getByteDayFromCalendarDay(Calendar.THURSDAY));
        assertTrue(Alarm.FRIDAY == Alarm.getByteDayFromCalendarDay(Calendar.FRIDAY));
        assertTrue(Alarm.SATURDAY == Alarm.getByteDayFromCalendarDay(Calendar.SATURDAY));
    }

    public void testIsCalendarDayEnabled() {
        Alarm alarm = new Alarm(0, 00);
        assertTrue(alarm.getDaysRepeating() == Alarm.NONE);
        assertFalse(alarm.isCalendarDayEnabled(Calendar.SUNDAY));
        assertFalse(alarm.isCalendarDayEnabled(Calendar.MONDAY));
        assertFalse(alarm.isCalendarDayEnabled(Calendar.TUESDAY));
        assertFalse(alarm.isCalendarDayEnabled(Calendar.WEDNESDAY));
        assertFalse(alarm.isCalendarDayEnabled(Calendar.THURSDAY));
        assertFalse(alarm.isCalendarDayEnabled(Calendar.FRIDAY));
        assertFalse(alarm.isCalendarDayEnabled(Calendar.SATURDAY));

        alarm.setDaysRepeating(Alarm.SUNDAY);
        assertFalse(alarm.getDaysRepeating() == Alarm.NONE);
        assertTrue(alarm.isCalendarDayEnabled(Calendar.SUNDAY));
        assertFalse(alarm.isCalendarDayEnabled(Calendar.MONDAY));
        assertFalse(alarm.isCalendarDayEnabled(Calendar.TUESDAY));
        assertFalse(alarm.isCalendarDayEnabled(Calendar.WEDNESDAY));
        assertFalse(alarm.isCalendarDayEnabled(Calendar.THURSDAY));
        assertFalse(alarm.isCalendarDayEnabled(Calendar.FRIDAY));
        assertFalse(alarm.isCalendarDayEnabled(Calendar.SATURDAY));
    }

    public void testEnableCalendarDay() {
        Alarm alarm = new Alarm(0, 00);
        assertTrue(alarm.getDaysRepeating() == Alarm.NONE);
        alarm.enableCalendarDay(Calendar.SUNDAY);
        assertTrue(alarm.getDaysRepeating() == Alarm.SUNDAY);
    }

    public void testDisableCalendarDay() {
        Alarm alarm = new Alarm(0, 00);
        assertTrue(alarm.getDaysRepeating() == Alarm.NONE);
        alarm.enableCalendarDay(Calendar.SUNDAY);
        assertTrue(alarm.getDaysRepeating() == Alarm.SUNDAY);
        alarm.disableCalendarDay(Calendar.SUNDAY);
        assertFalse(alarm.getDaysRepeating() == Alarm.SUNDAY);
    }
}