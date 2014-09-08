package com.finaldrive.dailydo.domain;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * POJO to capture an Alarm entry.
 * Note that even though this is NOT an alarm application, this is data for the AlarmManager.
 */
public class Alarm {
    public static final byte NONE = 0x00;       // 00000000
    public static final byte SUNDAY = 0x01;     // 00000001
    public static final byte MONDAY = 0x02;     // 00000010
    public static final byte TUESDAY = 0x04;    // 00000100
    public static final byte WEDNESDAY = 0x08;  // 00001000
    public static final byte THURSDAY = 0x10;   // 00010000
    public static final byte FRIDAY = 0x20;     // 00100000
    public static final byte WEEKDAYS = MONDAY | TUESDAY | WEDNESDAY | THURSDAY | FRIDAY;
    public static final byte SATURDAY = 0x40;   // 01000000
    private static final Map<Integer, Byte> CALENDAR_DAY_TO_BYTE_DAY_MAP;

    static {
        CALENDAR_DAY_TO_BYTE_DAY_MAP = new HashMap<Integer, Byte>();
        CALENDAR_DAY_TO_BYTE_DAY_MAP.put(Calendar.SUNDAY, SUNDAY);
        CALENDAR_DAY_TO_BYTE_DAY_MAP.put(Calendar.MONDAY, MONDAY);
        CALENDAR_DAY_TO_BYTE_DAY_MAP.put(Calendar.TUESDAY, TUESDAY);
        CALENDAR_DAY_TO_BYTE_DAY_MAP.put(Calendar.WEDNESDAY, WEDNESDAY);
        CALENDAR_DAY_TO_BYTE_DAY_MAP.put(Calendar.THURSDAY, THURSDAY);
        CALENDAR_DAY_TO_BYTE_DAY_MAP.put(Calendar.FRIDAY, FRIDAY);
        CALENDAR_DAY_TO_BYTE_DAY_MAP.put(Calendar.SATURDAY, SATURDAY);
    }

    /**
     * Database row ID.
     */
    private int id;
    /**
     * Hour of this Alarm.
     */
    private int hour = 0;
    /**
     * Minute of this Alarm.
     */
    private int minute = 0;
    /**
     * Byte to represent the days that are enabled and disabled.
     */
    private byte daysRepeating = 0x00;
    /**
     * Status of this Alarm.
     */
    private int isEnabled = 1;

    public Alarm() {
    }

    public Alarm(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    /**
     * Get the Byte day representation of the {@link Calendar#DAY_OF_WEEK}.
     *
     * @param calendarDay found in the {@link Calendar}
     * @return byte representation of the Calendar day
     */
    public static byte getByteDayFromCalendarDay(int calendarDay) {
        return CALENDAR_DAY_TO_BYTE_DAY_MAP.get(calendarDay);
    }

    public boolean isCalendarDayEnabled(int calendarDay) {
        if (calendarDay > Calendar.SATURDAY) {
            throw new IllegalArgumentException("Invalid Calendar day provided.");
        }
        final byte byteDay = Alarm.getByteDayFromCalendarDay(calendarDay);
        return (daysRepeating & byteDay) == byteDay;
    }

    public void disableCalendarDay(int calendarDay) {
        if (calendarDay > Calendar.SATURDAY) {
            throw new IllegalArgumentException("Invalid Calendar day provided.");
        }
        final byte byteDayInt = Alarm.getByteDayFromCalendarDay(calendarDay);
        final byte daysRepeatingInt = daysRepeating;
        daysRepeating = (byte) (daysRepeatingInt & (~byteDayInt));
    }

    public void enableCalendarDay(int calendarDay) {
        if (calendarDay > Calendar.SATURDAY) {
            throw new IllegalArgumentException("Invalid Calendar day provided.");
        }
        final byte byteDayInt = Alarm.getByteDayFromCalendarDay(calendarDay);
        final byte daysRepeatingInt = daysRepeating;
        daysRepeating = (byte) (daysRepeatingInt | byteDayInt);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public byte getDaysRepeating() {
        return daysRepeating;
    }

    public void setDaysRepeating(byte daysRepeating) {
        this.daysRepeating = daysRepeating;
    }

    public int getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(int isEnabled) {
        this.isEnabled = isEnabled;
    }

    @Override
    public String toString() {
        return "Alarm{" +
                "id=" + id +
                ", hour=" + hour +
                ", minute=" + minute +
                ", daysRepeating=" + daysRepeating +
                ", isEnabled=" + isEnabled +
                '}';
    }
}
