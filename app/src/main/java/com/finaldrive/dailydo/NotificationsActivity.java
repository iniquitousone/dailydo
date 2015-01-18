package com.finaldrive.dailydo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.finaldrive.dailydo.domain.Alarm;
import com.finaldrive.dailydo.fragment.TimePickerFragment;
import com.finaldrive.dailydo.helper.ActionBarStyleHelper;
import com.finaldrive.dailydo.helper.TimeFormatHelper;
import com.finaldrive.dailydo.helper.TranslateAnimationHelper;
import com.finaldrive.dailydo.listener.ListViewScrollListener;
import com.finaldrive.dailydo.service.AlarmService;
import com.finaldrive.dailydo.service.NotificationService;
import com.finaldrive.dailydo.store.DailyDoDatabaseHelper;

import java.util.Calendar;
import java.util.List;

/**
 * Activity to handle the scheduling of Alarm(s).
 */
public class NotificationsActivity extends ActionBarActivity {

    private static final String CLASS_NAME = "NotificationsActivity";
    private static final CharSequence[] DAYS_OF_WEEK = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private static final String ALARM_POSITION = "ALARM_POSITION";
    private static final int REQUEST_CODE_TONE_PICKER = 1;
    private static final int ALARM_CREATE_POSITION = -1;
    private DailyDoDatabaseHelper dailyDoDatabaseHelper;
    private List<Alarm> alarmList;
    private AlarmArrayAdapter alarmArrayAdapter;
    private ListView listView;
    private boolean isShowingNewAlarmButton = true;

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        finishActivity();
    }

    private void finishActivity() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        dailyDoDatabaseHelper = DailyDoDatabaseHelper.getInstance(this);
        alarmList = dailyDoDatabaseHelper.getAlarmEntries();
        alarmArrayAdapter = new AlarmArrayAdapter(this, R.layout.alarm_entry, alarmList);
        final View newAlarmButton = findViewById(R.id.new_alarm_button);
        listView = (ListView) findViewById(R.id.alarm_list_view);
        listView.setAdapter(alarmArrayAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnScrollListener(new ListViewScrollListener(listView) {
            @Override
            public void onDownwardScroll() {
                if (isShowingNewAlarmButton) {
                    isShowingNewAlarmButton = false;
                    newAlarmButton.startAnimation(TranslateAnimationHelper.DOWNWARD_BUTTON_TRANSLATION);
                    newAlarmButton.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onUpwardScroll() {
                if (!isShowingNewAlarmButton) {
                    isShowingNewAlarmButton = true;
                    newAlarmButton.startAnimation(TranslateAnimationHelper.UPWARD_BUTTON_TRANSLATION);
                    newAlarmButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.notifications, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(CLASS_NAME, String.format("ActivityResult on RequestCode=%d and ResultCode=%d", requestCode, resultCode));
        switch (resultCode) {
            case Activity.RESULT_OK:
                if (requestCode == REQUEST_CODE_TONE_PICKER) {
                    final Uri pickedTone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    NotificationService.setNotificationTone(this, pickedTone);
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_set_tone:
                final Uri notificationTone = NotificationService.getNotificationTone(this);
                final Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select tone");
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);
                if (notificationTone != null) {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, notificationTone);
                } else {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Settings.System.DEFAULT_NOTIFICATION_URI);
                }
                startActivityForResult(intent, REQUEST_CODE_TONE_PICKER);
                return true;

            case android.R.id.home:
                finishActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addNewAlarm(View view) {
        final Calendar calendar = Calendar.getInstance();
        final Bundle bundle = new Bundle();
        bundle.putInt(ALARM_POSITION, ALARM_CREATE_POSITION);
        bundle.putInt(TimePickerFragment.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
        bundle.putInt(TimePickerFragment.MINUTE, calendar.get(Calendar.MINUTE));
        final AlarmTimePickerFragment alarmTimePickerFragment = new AlarmTimePickerFragment();
        alarmTimePickerFragment.setArguments(bundle);
        alarmTimePickerFragment.show(getFragmentManager(), "AlarmTimePickerFragment");
    }

    /**
     * Handles state of the ListView whenever it is empty to show a helpful message to the user.
     */
//    @Override
//    public void onContentChanged() {
//        super.onContentChanged();
//        View emptyListView = findViewById(R.id.empty_alarm_list_view);
//        ListView listView = (ListView) findViewById(R.id.alarm_list_view);
//        if (listView == null) {
//            throw new RuntimeException("No listview provided.");
//        }
//        if (emptyListView != null) {
//            listView.setEmptyView(emptyListView);
//        }
//    }

    private static class ViewHolder {
        private ToggleButton toggleButton;
        private TextView timeView;
        private ImageButton trashButton;
        private TextView daysView;
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
            final ViewHolder viewHolder;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.alarm_entry, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.toggleButton = (ToggleButton) convertView.findViewById(R.id.alarm_entry_toggle);
                viewHolder.timeView = (TextView) convertView.findViewById(R.id.alarm_entry_time);
                viewHolder.trashButton = (ImageButton) convertView.findViewById(R.id.alarm_entry_discard);
                viewHolder.daysView = (TextView) convertView.findViewById(R.id.alarm_entry_days);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            final Alarm alarm = getItem(position);
            final boolean isEnabled = alarm.getIsEnabled() == 1 ? true : false;
            // Setup the views.
            if (isEnabled) {
                convertView.setAlpha(1.0f);
            } else {
                convertView.setAlpha(0.5f);
            }
            viewHolder.toggleButton.setChecked(isEnabled);
            viewHolder.timeView.setText(TimeFormatHelper.format(getContext(), alarm.getHour(), alarm.getMinute()));
            viewHolder.daysView.setText(getDays(alarm));
            // Setup the listeners.
            viewHolder.toggleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alarm.setIsEnabled(alarm.getIsEnabled() == 1 ? 0 : 1);
                    dailyDoDatabaseHelper.updateAlarmEntry(alarm);
                    notifyDataSetChanged();
                    AlarmService.scheduleNextAlarm(getContext());
                }
            });
            viewHolder.timeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Bundle bundle = new Bundle();
                    bundle.putInt(ALARM_POSITION, position);
                    bundle.putInt(TimePickerFragment.HOUR_OF_DAY, alarm.getHour());
                    bundle.putInt(TimePickerFragment.MINUTE, alarm.getMinute());
                    final AlarmTimePickerFragment alarmTimePickerFragment = new AlarmTimePickerFragment();
                    alarmTimePickerFragment.setArguments(bundle);
                    alarmTimePickerFragment.show(getFragmentManager(), "AlarmTimePickerFragment");
                }
            });
            viewHolder.trashButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog alertDialog = new AlertDialog.Builder(getContext(),
                            AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
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
            viewHolder.daysView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final boolean[] booleanArray = new boolean[7];
                    for (int i = Calendar.SUNDAY; i <= Calendar.SATURDAY; i++) {
                        booleanArray[i - 1] = alarm.isCalendarDayEnabled(i);
                    }
                    final AlertDialog alertDialog = new AlertDialog.Builder(getContext(),
                            AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
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
                    daysOfWeek = "WEEKENDS";
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

    @SuppressLint("ValidFragment")
    public final class AlarmTimePickerFragment extends TimePickerFragment {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            if (counter > 0) {
                return;
            }
            counter++;
            int position = getArguments().getInt(ALARM_POSITION);
            if (position == ALARM_CREATE_POSITION) {
                Alarm alarm = new Alarm(hourOfDay, minute);
                alarm.setDaysRepeating(Alarm.EVERYDAY);
                alarm = dailyDoDatabaseHelper.insertAlarmEntry(alarm);
                alarmList.add(alarm);
                position = alarmList.size() - 1;
            }
            final Alarm alarm = alarmArrayAdapter.getItem(position);
            alarm.setHour(hourOfDay);
            alarm.setMinute(minute);
            dailyDoDatabaseHelper.updateAlarmEntry(alarm);
            alarmArrayAdapter.notifyDataSetChanged();
            listView.smoothScrollToPosition(position);
            AlarmService.scheduleNextAlarm(getActivity());
        }
    }
}
