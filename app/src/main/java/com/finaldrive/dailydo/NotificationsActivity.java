package com.finaldrive.dailydo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.finaldrive.dailydo.domain.Alarm;
import com.finaldrive.dailydo.fragment.AlarmTimePickerFragment;
import com.finaldrive.dailydo.fragment.TimePickerFragment;
import com.finaldrive.dailydo.helper.ActionBarStyleHelper;
import com.finaldrive.dailydo.helper.TimeFormatHelper;
import com.finaldrive.dailydo.service.AlarmService;
import com.finaldrive.dailydo.store.DailyDoDatabaseHelper;

import java.util.Calendar;
import java.util.List;

/**
 * Activity to handle the scheduling of Alarm(s).
 */
public class NotificationsActivity extends Activity {

    private static final CharSequence[] DAYS_OF_WEEK = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private DailyDoDatabaseHelper dailyDoDatabaseHelper;
    private List<Alarm> alarmList;
    private AlarmArrayAdapter alarmArrayAdapter;
    private ListView listView;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED);
        finishActivity();
    }

    private void finishActivity() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBarStyleHelper.setupActionBar(this, true);
        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            getActionBar().setIcon(R.drawable.ic_action_back);
            getActionBar().setTitle(R.string.title_activity_notifications);
        }
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

            case android.R.id.home:
                finishActivity();
                return true;
        }
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
            final boolean isEnabled = alarm.getIsEnabled() == 1 ? true : false;
            final ToggleButton toggleButton = (ToggleButton) convertView.findViewById(R.id.alarm_entry_toggle);
            final TextView timeView = (TextView) convertView.findViewById(R.id.alarm_entry_time);
            final ImageButton trashButton = (ImageButton) convertView.findViewById(R.id.alarm_entry_discard);
            final TextView daysView = (TextView) convertView.findViewById(R.id.alarm_entry_days);
            // Setup the views.
            if (isEnabled) {
                convertView.setAlpha(1.0f);
            } else {
                convertView.setAlpha(0.5f);
            }
            toggleButton.setChecked(isEnabled);
            timeView.setText(TimeFormatHelper.format(getContext(), alarm.getHour(), alarm.getMinute()));
            daysView.setText(getDays(alarm));
            // Setup the listeners.
            toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    alarm.setIsEnabled(isChecked ? 1 : 0);
                    dailyDoDatabaseHelper.updateAlarmEntry(alarm);
                    notifyDataSetChanged();
                    AlarmService.scheduleNextAlarm(getContext());
                }
            });
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
            trashButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog alertDialog = new AlertDialog.Builder(getContext(), AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                            .setIcon(R.drawable.ic_action_about)
                            .setTitle("Confirm deletion")
                            .setMessage("Do you want to delete this notification?")
                            .setCancelable(true)
                            .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    return;
                                }
                            })
                            .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
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
                }
            });
            daysView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final boolean[] booleanArray = new boolean[7];
                    for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
                        booleanArray[i - 1] = alarm.isCalendarDayEnabled(i);
                    }
                    final AlertDialog alertDialog = new AlertDialog.Builder(getContext(),
                            AlertDialog.THEME_DEVICE_DEFAULT_DARK)
                            .setTitle("Pick repeating days")
                            .setCancelable(true)
                            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    return;
                                }
                            })
                            .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    return;
                                }
                            })
                            .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
                                        if (booleanArray[i - 1]) {
                                            alarm.enableCalendarDay(i);
                                        } else {
                                            alarm.disableCalendarDay(i);
                                        }
                                    }
                                    dailyDoDatabaseHelper.updateAlarmEntry(alarm);
                                    notifyDataSetChanged();
                                    AlarmService.scheduleNextAlarm(getContext());
                                }
                            })
                            .setMultiChoiceItems(DAYS_OF_WEEK, booleanArray, new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                    booleanArray[which] = isChecked;
                                }
                            })
                            .create();
                    alertDialog.show();
                }
            });
            return convertView;
        }

        private String getDays(Alarm alarm) {
            int days = alarm.getDaysRepeating();
            String daysOfWeek = "";

            switch (days) {
                case Alarm.NONE:
                    daysOfWeek = "NONE";
                    break;
                case Alarm.WEEKEND:
                    daysOfWeek = "WEEKEND";
                    break;
                case Alarm.WEEKDAYS:
                    daysOfWeek = "WEEKDAYS";
                    break;
                case Alarm.EVERYDAY:
                    daysOfWeek = "EVERYDAY";
                    break;
                default:
                    for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
                        if (alarm.isCalendarDayEnabled(i)) {
                            daysOfWeek += String.format("%s  ", Alarm.getStringDayFromCalendarDay(i));
                        }
                    }
                    break;
            }
            return daysOfWeek;
        }
    }
}
