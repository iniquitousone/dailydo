package com.finaldrive.dailydo.helper;

import android.content.Context;
import android.text.format.DateFormat;

import java.util.Calendar;

/**
 * Helper to handle time formatting for rendering.
 */
public final class TimeFormatHelper {

    public static String format(final Context context, final Calendar calendar) {
        final java.text.DateFormat timeFormat = DateFormat.getTimeFormat(context.getApplicationContext());
        return timeFormat.format(calendar.getTime());
    }

    public static String format(final Context context, int hour, int minute) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        return format(context, calendar);
    }
}
