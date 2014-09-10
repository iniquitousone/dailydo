package com.finaldrive.dailydo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.finaldrive.dailydo.domain.Task;
import com.finaldrive.dailydo.fragment.DailyResetTimePickerFragment;
import com.finaldrive.dailydo.fragment.TimePickerFragment;
import com.finaldrive.dailydo.store.DailyDoDatabaseHelper;
import com.finaldrive.dailydo.view.DynamicListView;

import java.util.List;

/**
 * Main activity for handling the List of Task(s) to show.
 */
public class MainActivity extends Activity {

    private static final String CLASS_NAME = "MainActivity";
    private static final int INVALID_VALUE = -99;
    /**
     * BroadcastReceiver to listen to the DO reset.
     */
    private BroadcastReceiver resetListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Resetting all DOs!", Toast.LENGTH_SHORT).show();
            // Re-establish the underlying data set and re-render the List view.
            taskList.clear();
            taskList.addAll(dailyDoDatabaseHelper.getTaskEntries());
            taskArrayAdapter.notifyDataSetChanged();
        }
    };
    private ActionMode.Callback callback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    };
    private DailyDoDatabaseHelper dailyDoDatabaseHelper;
    private List<Task> taskList;
    private TaskArrayAdapter taskArrayAdapter;
    private DynamicListView dynamicListView;

    /**
     * Entry point into the application. Sets up data and renders ListView.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call super constructor to establish default Cursor.
        super.onCreate(savedInstanceState);
        // Set the View that will be rendered for this Activity.
        setContentView(R.layout.activity_main);
        final ImageView homeIcon = (ImageView) findViewById(android.R.id.home);
        homeIcon.setPadding(16, 0, homeIcon.getWidth(), 0);
        // Initialize the database helper and fetch the List of Task(s) from the database.
        dailyDoDatabaseHelper = DailyDoDatabaseHelper.getInstance(this);
        taskList = dailyDoDatabaseHelper.getTaskEntries();
        // Initialize and set the ArrayAdapter which acts as the adapter for the main ListView and its content.
        taskArrayAdapter = new TaskArrayAdapter(this, R.layout.task_entry, taskList);
        // Setup the DynamicListView to allow drag-and-sort functionality.
        dynamicListView = (DynamicListView) findViewById(R.id.task_list_view);
        dynamicListView.setTaskList(taskList);
        dynamicListView.setAdapter(taskArrayAdapter);
        dynamicListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        // Setup and persist the daily reset time to the SharedPreferences.
        // Though not used in this particular Activity, we want to initialize the data here as entry point.
        setupDailyResetPreferences();
    }

    /**
     * Setups the SharedPreferences to use across the application. If they have not been initialize, they will here.
     * Otherwise, it will simply re-use what is already in the {@link SharedPreferences}.
     */
    private void setupDailyResetPreferences() {
        final SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.pref_daily_do), MODE_PRIVATE);
        final boolean isDailyResetEnabled = sharedPreferences.getBoolean(getString(R.string.pref_daily_reset_enabled), true);
        final int hourOfReset = sharedPreferences.getInt(getString(R.string.pref_daily_reset_hour), 0);
        final int minuteOfReset = sharedPreferences.getInt(getString(R.string.pref_daily_reset_minute), 0);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.pref_daily_reset_enabled), isDailyResetEnabled);
        editor.putInt(getString(R.string.pref_daily_reset_hour), hourOfReset);
        editor.putInt(getString(R.string.pref_daily_reset_minute), minuteOfReset);
        editor.commit();
    }

    /**
     * The BroadcastReceiver for this Activity is only relevant while it is already running.
     */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(resetListener, new IntentFilter(getString(R.string.intent_action_reset_tasks)));
    }

    /**
     * Overridden to free up resources and so the framework does not complain.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dailyDoDatabaseHelper.close();
        unregisterReceiver(resetListener);
    }

    /**
     * Called when returning to this Activity as a result of an {@link Activity#finish} call to handle the Intent.
     *
     * @param requestCode of the Intent to handle what action to take
     * @param resultCode  of the Intent to determine whether to act
     * @param data        of the Intent to handle the action
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(CLASS_NAME, String.format("ActivityResult on RequestCode=%d and ResultCode=%d", requestCode, resultCode));
        switch (resultCode) {
            case Activity.RESULT_OK:
                int taskId = data.getIntExtra(TaskDetailsActivity.EXTRA_TASK_ID, INVALID_VALUE);
                int position = data.getIntExtra(TaskDetailsActivity.EXTRA_TASK_POSITION, INVALID_VALUE);
                if (taskId == INVALID_VALUE || position == INVALID_VALUE) {
                    return;
                }
                final Task task = dailyDoDatabaseHelper.getTaskEntry(taskId);
                switch (requestCode) {
                    case TaskDetailsActivity.REQUEST_CODE_TASK_CREATE:
                        taskList.add(task);
                        break;

                    case TaskDetailsActivity.REQUEST_CODE_TASK_DETAILS:
                        taskList.set(position, task);
                        break;
                }
                taskArrayAdapter.notifyDataSetChanged();
                dynamicListView.smoothScrollToPosition(position);
                break;

            case TaskDetailsActivity.RESULT_CODE_DELETE:
                taskId = data.getIntExtra(TaskDetailsActivity.EXTRA_TASK_ID, INVALID_VALUE);
                position = data.getIntExtra(TaskDetailsActivity.EXTRA_TASK_POSITION, INVALID_VALUE);
                if (taskId == INVALID_VALUE || position == INVALID_VALUE) {
                    return;
                }
                taskList.remove(position);
                for (int i = position; i < taskList.size(); i++) {
                    final Task taskToUpdate = taskList.get(i);
                    if (taskToUpdate.getRowNumber() != i) {
                        taskToUpdate.setRowNumber(i);
                        dailyDoDatabaseHelper.updateTaskEntry(taskToUpdate);
                    }
                }
                taskArrayAdapter.notifyDataSetChanged();
                break;
        }
    }

    /**
     * Handles state of the ListView whenever it is empty to show a helpful message to the user.
     */
    @Override
    public void onContentChanged() {
        super.onContentChanged();
        final View emptyListView = findViewById(R.id.empty_task_list_view);
        final ListView listView = (ListView) findViewById(R.id.task_list_view);
        if (listView == null) {
            throw new RuntimeException("No listview provided.");
        }
        if (emptyListView != null) {
            listView.setEmptyView(emptyListView);
        }
    }

    /**
     * Inflates the associated {@link menu/main.xml}, responsible for the ActionBar items.
     *
     * @param menu being used
     * @return boolean for if item was selected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Handles ActionBar item clicks.
     *
     * @param item that the user clicked
     * @return boolean for if item was selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_daily_reset:
                final SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.pref_daily_do), MODE_PRIVATE);
                final int hourOfReset = sharedPreferences.getInt(getString(R.string.pref_daily_reset_hour), 0);
                final int minuteOfReset = sharedPreferences.getInt(getString(R.string.pref_daily_reset_minute), 0);
                final Bundle bundle = new Bundle();
                bundle.putString(TimePickerFragment.TITLE, "When does your day start?\nThis is when your DOs reset.");
                bundle.putInt(TimePickerFragment.HOUR_OF_DAY, hourOfReset);
                bundle.putInt(TimePickerFragment.MINUTE, minuteOfReset);
                final DailyResetTimePickerFragment dailyResetTimePickerFragment = new DailyResetTimePickerFragment();
                dailyResetTimePickerFragment.setArguments(bundle);
                dailyResetTimePickerFragment.show(getFragmentManager(), "DailyResetTimePickerFragment");
                break;

            case R.id.action_notifications:
                startActivity(new Intent(this, NotificationsActivity.class));
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;

            case R.id.action_add_task:
                final Intent intent = new Intent(this, TaskDetailsActivity.class);
                intent.putExtra(TaskDetailsActivity.EXTRA_TASK_POSITION, taskList.size());
                startActivityForResult(intent, TaskDetailsActivity.REQUEST_CODE_TASK_CREATE);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Custom adapter for handling the View for each Task entry as well as providing a stable ID to the DynamicListView.
     * This helps ensure that the DynamicListView will always access the correct Task entry in the List.
     */
    private final class TaskArrayAdapter extends ArrayAdapter<Task> {

        private static final int INVALID_ID = -1;
        private LayoutInflater layoutInflater;

        public TaskArrayAdapter(Context context, int textViewResourceId, List<Task> objects) {
            super(context, textViewResourceId, objects);
            layoutInflater = LayoutInflater.from(getContext());
        }

        /**
         * Overridden to leverage our database ID as the unique and stable ID for the given Task position.
         *
         * @param position
         * @return id of the associated Task
         */
        @Override
        public long getItemId(int position) {
            if (position < 0 || position >= getCount()) {
                return INVALID_ID;
            }
            Task task = getItem(position);
            return task.getId();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        /**
         * Overridden to provide a custom view per Task entry in the ListVew.
         *
         * @param position    of the current Task to render
         * @param convertView that can be reused if necessary for rendering this entry
         * @param parent      of the current view
         * @return view that will be used to render this entry
         */
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.task_entry, parent, false);
            }
            final Task task = getItem(position);
            final boolean isChecked = task.getIsChecked() == 1 ? true : false;
            final CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.task_entry_checkbox);
            final LinearLayout contentView = (LinearLayout) convertView.findViewById(R.id.task_entry_content);
            final ImageView separatorView = (ImageView) convertView.findViewById(R.id.task_entry_separator);
            final TextView titleView = (TextView) convertView.findViewById(R.id.task_entry_title);
            // The Task note should only have visibility if it is non-null and not empty.
            final TextView noteView = (TextView) convertView.findViewById(R.id.task_entry_note);
            // Setup the isChecked state of the view.
            if (isChecked) {
                titleView.setTypeface(null, Typeface.BOLD_ITALIC);
                titleView.setTextColor(getResources().getColor(R.color.silver));
                noteView.setTypeface(null, Typeface.ITALIC);
                noteView.setTextColor(getResources().getColor(R.color.silver));
            } else {
                titleView.setTypeface(null, Typeface.BOLD);
                titleView.setTextColor(getResources().getColor(R.color.gray));
                noteView.setTypeface(null, Typeface.NORMAL);
                noteView.setTextColor(getResources().getColor(R.color.silver));
            }
            // Setup the values.
            titleView.setText(task.getTitle());
            if (task.getNote() != null && !task.getNote().isEmpty()) {
                noteView.setText(task.getNote());
                noteView.setVisibility(View.VISIBLE);
                separatorView.setVisibility(View.VISIBLE);
            } else {
                noteView.setVisibility(View.GONE);
                separatorView.setVisibility(View.GONE);
            }
            checkBox.setChecked(isChecked);
            // Setup the listeners.
            contentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startTaskDetailsActivity(task.getId(), position);
                }
            });
            contentView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    return false;
                }
            });
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    task.setIsChecked(task.getIsChecked() == 1 ? 0 : 1);
                    task.setRowNumber(position);
                    dailyDoDatabaseHelper.updateTaskEntry(task);
                    // TODO: Consider using the UI thread to only update this entry.
                    notifyDataSetChanged();
                }
            });

            return convertView;
        }

        /**
         * Starts the {@link TaskDetailsActivity} to handled the detailed view for the given Task.
         *
         * @param id       of the given Task
         * @param position of the given Task
         */
        private final void startTaskDetailsActivity(int id, int position) {
            final Intent intent = new Intent(getContext(), TaskDetailsActivity.class);
            intent.putExtra(TaskDetailsActivity.EXTRA_TASK_ID, id);
            intent.putExtra(TaskDetailsActivity.EXTRA_TASK_POSITION, position);
            startActivityForResult(intent, TaskDetailsActivity.REQUEST_CODE_TASK_DETAILS);
        }
    }
}
