package com.finaldrive.dailydo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.finaldrive.dailydo.helper.TimeFormatHelper;
import com.finaldrive.dailydo.service.AlarmService;
import com.finaldrive.dailydo.service.NotificationService;
import com.finaldrive.dailydo.store.DailyDoDatabaseHelper;

import java.util.Calendar;
import java.util.List;


/**
 * Fragment for handling the Notification list view.
 */
public class NotificationsFragment extends Fragment {

    private static final String CLASS_NAME = "NotificationsFragment";
    private static final CharSequence[] DAYS_OF_WEEK = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
    private static final String ALARM_POSITION = "ALARM_POSITION";
    private static final int ALARM_CREATE_POSITION = -1;
    private DailyDoDatabaseHelper dailyDoDatabaseHelper;
    private AlarmArrayAdapter alarmArrayAdapter;
    private ListView listView;
    private LayoutInflater layoutInflater;

    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dailyDoDatabaseHelper = DailyDoDatabaseHelper.getInstance(this.getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layoutInflater = inflater;
        final View contentView = layoutInflater.inflate(R.layout.fragment_notifications, container, false);
        final List<Alarm> alarmList = dailyDoDatabaseHelper.getAlarmEntries();
        alarmArrayAdapter = new AlarmArrayAdapter(this.getActivity(), R.layout.entry_alarm, alarmList);
        final View emptyListView = contentView.findViewById(R.id.empty_alarm_list_view);
        listView = (ListView) contentView.findViewById(R.id.alarm_list_view);
        listView.addFooterView(layoutInflater.inflate(R.layout.list_view_footer, null), null, false);
        listView.setAdapter(alarmArrayAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setEmptyView(emptyListView);
        final View newNotificationButton = contentView.findViewById(R.id.new_alarm_button);
        newNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                final Bundle bundle = new Bundle();
                bundle.putInt(ALARM_POSITION, ALARM_CREATE_POSITION);
                bundle.putInt(TimePickerFragment.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
                bundle.putInt(TimePickerFragment.MINUTE, calendar.get(Calendar.MINUTE));
                final AlarmTimePickerFragment alarmTimePickerFragment = new AlarmTimePickerFragment();
                alarmTimePickerFragment.setArguments(bundle);
                alarmTimePickerFragment.show(NotificationsFragment.this.getActivity().getFragmentManager(), "AlarmTimePickerFragment");
            }
        });

        return contentView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

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

        public AlarmArrayAdapter(Context context, int textViewResourceId, List<Alarm> objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.entry_alarm, parent, false);
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
                    alarmTimePickerFragment.show(NotificationsFragment.this.getActivity().getFragmentManager(), "AlarmTimePickerFragment");
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
                                    alarmArrayAdapter.clear();
                                    alarmArrayAdapter.addAll(dailyDoDatabaseHelper.getAlarmEntries());
                                    alarmArrayAdapter.notifyDataSetChanged();
                                    AlarmService.scheduleNextAlarm(getContext());
                                    Toast.makeText(NotificationsFragment.this.getActivity().getApplicationContext(), "Deleted", Toast.LENGTH_SHORT).show();
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
                dailyDoDatabaseHelper.insertAlarmEntry(alarm);
                alarmArrayAdapter.clear();
                alarmArrayAdapter.addAll(dailyDoDatabaseHelper.getAlarmEntries());
            } else {
                final Alarm alarm = alarmArrayAdapter.getItem(position);
                alarm.setHour(hourOfDay);
                alarm.setMinute(minute);
                dailyDoDatabaseHelper.updateAlarmEntry(alarm);
            }
            alarmArrayAdapter.notifyDataSetChanged();
            AlarmService.scheduleNextAlarm(this.getActivity());
        }
    }
}
