package com.finaldrive.dailydo.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.Settings;

import com.finaldrive.dailydo.R;

/**
 * Notification tone helper to retrieve and set the tone Uri.
 */
public class NotificationToneHelper {

    public static Uri getTone(final Context context) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.pref_daily_do),
                Context.MODE_PRIVATE);
        final String notificationTone = sharedPreferences.getString(context.getString(R.string.pref_notification_tone), null);
        if (notificationTone == null) {
            setTone(context, Settings.System.DEFAULT_NOTIFICATION_URI);
        }
        return Settings.System.DEFAULT_NOTIFICATION_URI;
    }

    public static void setTone(final Context context, Uri notificationToneUri) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getString(R.string.pref_daily_do),
                Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.pref_notification_tone), notificationToneUri.toString());
        editor.commit();
    }
}
