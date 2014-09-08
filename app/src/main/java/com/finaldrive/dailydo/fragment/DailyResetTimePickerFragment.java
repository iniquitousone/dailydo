package com.finaldrive.dailydo.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.TimePicker;

import com.finaldrive.dailydo.R;
import com.finaldrive.dailydo.service.AlarmService;

/**
 * Fragment used to display a TimePickerDialog to allow user input of the daily reset time.
 * It will update the View in addition to reschedule the next reset.
 */
public class DailyResetTimePickerFragment extends TimePickerFragment {

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        final Activity context = getActivity();
        final SharedPreferences sharedPreferences = context.getSharedPreferences(getString(R.string.pref_daily_do), Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getString(R.string.pref_daily_reset_hour), hourOfDay);
        editor.putInt(getString(R.string.pref_daily_reset_minute), minute);
        editor.commit();
        AlarmService.scheduleNextReset(getActivity());
    }
}
