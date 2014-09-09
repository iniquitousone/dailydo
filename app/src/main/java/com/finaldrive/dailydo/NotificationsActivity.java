package com.finaldrive.dailydo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.finaldrive.dailydo.domain.Alarm;
import com.finaldrive.dailydo.fragment.AlarmTimePickerFragment;
import com.finaldrive.dailydo.fragment.TimePickerFragment;
import com.finaldrive.dailydo.helper.TimeFormatHelper;
import com.finaldrive.dailydo.service.AlarmService;
import com.finaldrive.dailydo.store.DailyDoDatabaseHelper;

import java.util.Calendar;
import java.util.List;

/**
 * Activity to handle the scheduling of Alarm(s) and the daily reset time.
 */
public class NotificationsActivity extends Activity {

    private DailyDoDatabaseHelper dailyDoDatabaseHelper;
    private List<Alarm> alarmList;
    private AlarmArrayAdapter alarmArrayAdapter;
    private ListView listView;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED);
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        dailyDoDatabaseHelper = DailyDoDatabaseHelper.getInstance(this);
        alarmList = dailyDoDatabaseHelper.getAlarmEntries();
        alarmArrayAdapter = new AlarmArrayAdapter(this, R.layout.alarm_entry, alarmList);
        listView = (ListView) findViewById(R.id.alarm_list_view);
        listView.setAdapter(alarmArrayAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notifications, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_alarm:
                final Calendar calendar = Calendar.getInstance();
                Alarm alarm = new Alarm(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
                alarm.setDaysRepeating(Alarm.WEEKDAYS);
                alarm = dailyDoDatabaseHelper.insertAlarmEntry(alarm);
                alarmList.add(alarm);
                final Bundle bundle = new Bundle();
                bundle.putInt(AlarmTimePickerFragment.POSITION, alarmList.size() - 1);
                bundle.putInt(TimePickerFragment.HOUR_OF_DAY, alarm.getHour());
                bundle.putInt(TimePickerFragment.MINUTE, alarm.getMinute());
                final AlarmTimePickerFragment alarmTimePickerFragment = new AlarmTimePickerFragment();
                alarmTimePickerFragment.setArguments(bundle);
                alarmTimePickerFragment.show(getFragmentManager(), "AlarmTimePickerFragment");
                return false;
        }
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handles state of the ListView whenever it is empty to show a helpful message to the user.
     */
    @Override
    public void onContentChanged() {
        super.onContentChanged();
        View emptyListView = findViewById(R.id.empty_alarm_list_view);
        ListView listView = (ListView) findViewById(R.id.alarm_list_view);
        if (listView == null) {
            throw new RuntimeException("No listview provided.");
        }
        if (emptyListView != null) {
            listView.setEmptyView(emptyListView);
        }
    }

    /**
     * Custom adapter for handling the View for each Alarm entry.
     */
    public final class AlarmArrayAdapter extends ArrayAdapter<Alarm> {

        private LayoutInflater layoutInflater;

        public AlarmArrayAdapter(Context context, int textViewResourceId, List<Alarm> objects) {
            super(context, textViewResourceId, objects);
            layoutInflater = LayoutInflater.from(getContext());
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.alarm_entry, parent, false);
            }
            final Alarm alarm = getItem(position);
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final AlertDialog alertDialog = new AlertDialog.Builder(getContext(), AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                            .setIcon(R.drawable.ic_action_about)
                            .setTitle("Confirm deletion")
                            .setMessage("Do you want to delete this notification?")
                            .setCancelable(true)
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    return;
                                }
                            })
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dailyDoDatabaseHelper.deleteAlarmEntry(alarm.getId());
                                    alarmList.remove(position);
                                    alarmArrayAdapter.notifyDataSetChanged();
                                    AlarmService.scheduleNextAlarm(getContext());
                                    Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .create();
                    alertDialog.show();
                    return false;
                }
            });
            final ToggleButton sundayButton = (ToggleButton) convertView.findViewById(R.id.sunday_toggle);
            setupDayToggleButton(sundayButton, alarm, Calendar.SUNDAY);
            final ToggleButton mondayButton = (ToggleButton) convertView.findViewById(R.id.monday_toggle);
            setupDayToggleButton(mondayButton, alarm, Calendar.MONDAY);
            final ToggleButton tuesdayButton = (ToggleButton) convertView.findViewById(R.id.tuesday_toggle);
            setupDayToggleButton(tuesdayButton, alarm, Calendar.TUESDAY);
            final ToggleButton wednesdayButton = (ToggleButton) convertView.findViewById(R.id.wednesday_toggle);
            setupDayToggleButton(wednesdayButton, alarm, Calendar.WEDNESDAY);
            final ToggleButton thursdayButton = (ToggleButton) convertView.findViewById(R.id.thursday_toggle);
            setupDayToggleButton(thursdayButton, alarm, Calendar.THURSDAY);
            final ToggleButton fridayButton = (ToggleButton) convertView.findViewById(R.id.friday_toggle);
            setupDayToggleButton(fridayButton, alarm, Calendar.FRIDAY);
            final ToggleButton saturdayButton = (ToggleButton) convertView.findViewById(R.id.saturday_toggle);
            setupDayToggleButton(saturdayButton, alarm, Calendar.SATURDAY);
            final TextView timeView = (TextView) convertView.findViewById(R.id.alarm_entry_time);
            timeView.setText(TimeFormatHelper.format(getContext(), alarm.getHour(), alarm.getMinute()));
            timeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Bundle bundle = new Bundle();
                    bundle.putInt(AlarmTimePickerFragment.POSITION, position);
                    bundle.putInt(TimePickerFragment.HOUR_OF_DAY, alarm.getHour());
                    bundle.putInt(TimePickerFragment.MINUTE, alarm.getMinute());
                    final AlarmTimePickerFragment alarmTimePickerFragment = new AlarmTimePickerFragment();
                    alarmTimePickerFragment.setArguments(bundle);
                    alarmTimePickerFragment.show(getFragmentManager(), "AlarmTimePickerFragment");
                }
            });
            final Switch switchView = (Switch) convertView.findViewById(R.id.alarm_entry_switch);
            switchView.setChecked(alarm.getIsEnabled() == 1 ? true : false);
            switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    alarm.setIsEnabled(isChecked ? 1 : 0);
                    dailyDoDatabaseHelper.updateAlarmEntry(alarm);
                    AlarmService.scheduleNextAlarm(getContext());
                }
            });
            return convertView;
        }

        /**
         * Setup the provided ToggleButton state and View.
         *
         * @param toggleButton
         * @param alarm
         * @param calendarDay
         */
        private void setupDayToggleButton(final ToggleButton toggleButton, final Alarm alarm, final int calendarDay) {
            toggleButton.setChecked(alarm.isCalendarDayEnabled(calendarDay));
            toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        alarm.enableCalendarDay(calendarDay);
                    } else {
                        alarm.disableCalendarDay(calendarDay);
                    }
                    dailyDoDatabaseHelper.updateAlarmEntry(alarm);
                    AlarmService.scheduleNextAlarm(getContext());
                }
            });
        }
    }
}
