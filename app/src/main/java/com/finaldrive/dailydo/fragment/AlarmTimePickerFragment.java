package com.finaldrive.dailydo.fragment;

import android.widget.ListView;
import android.widget.TimePicker;

import com.finaldrive.dailydo.NotificationsActivity;
import com.finaldrive.dailydo.R;
import com.finaldrive.dailydo.domain.Alarm;
import com.finaldrive.dailydo.service.AlarmService;
import com.finaldrive.dailydo.store.DailyDoDatabaseHelper;

/**
 * Fragment used to display a TimePickerDialog to allow user input of the Alarm hour and minute.
 * It will update the Alarm entry, notify the Adapter of a change, and reschedule the upcoming Alarm.
 */
public class AlarmTimePickerFragment extends TimePickerFragment {

    public static final String POSITION = "POSITION";

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        final DailyDoDatabaseHelper dailyDoDatabaseHelper = DailyDoDatabaseHelper.getInstance(getActivity());
        final int position = getArguments().getInt(POSITION);
        final ListView listView = (ListView) getActivity().findViewById(R.id.alarm_list_view);
        final NotificationsActivity.AlarmArrayAdapter alarmArrayAdapter = ((NotificationsActivity.AlarmArrayAdapter) listView.getAdapter());
        final Alarm alarm = alarmArrayAdapter.getItem(position);
        alarm.setHour(hourOfDay);
        alarm.setMinute(minute);
        dailyDoDatabaseHelper.updateAlarmEntry(alarm);
        alarmArrayAdapter.notifyDataSetChanged();
        listView.smoothScrollToPosition(position);
        AlarmService.scheduleNextAlarm(getActivity());
    }
}
