package com.finaldrive.dailydo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
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
import com.finaldrive.dailydo.service.NotificationService;
import com.finaldrive.dailydo.store.DailyDoDatabaseHelper;
import com.finaldrive.dailydo.view.DragListView;

import java.util.List;

/**
 * Main Fragment for handling the Task list view.
 */
public class TasksFragment extends Fragment {

    private static final String CLASS_NAME = "TaskFragment";
    private static final int INVALID_VALUE = -99;
    private DailyDoDatabaseHelper dailyDoDatabaseHelper;
    private TaskArrayAdapter taskArrayAdapter;
    private DragListView dragListView;
    private LayoutInflater layoutInflater;
    /**
     * BroadcastReceiver to listen to the DO reset.
     */
    private BroadcastReceiver resetListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Resetting all DOs!", Toast.LENGTH_SHORT).show();
            // Re-establish the underlying data set and re-render the List view.
            taskArrayAdapter.clear();
            taskArrayAdapter.addAll(dailyDoDatabaseHelper.getTaskEntries());
            taskArrayAdapter.notifyDataSetChanged();
        }
    };

    public TasksFragment() {
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
        final View contentView = layoutInflater.inflate(R.layout.fragment_task, container, false);
        final List<Task> taskList = dailyDoDatabaseHelper.getTaskEntries();
        // Initialize and set the ArrayAdapter which acts as the adapter for the main ListView and its content.
        taskArrayAdapter = new TaskArrayAdapter(this.getActivity(), R.layout.entry_task, taskList);
        final View emptyListView = contentView.findViewById(R.id.empty_task_list_view);
        dragListView = (DragListView) contentView.findViewById(R.id.task_list_view);
        dragListView.addFooterView(layoutInflater.inflate(R.layout.list_view_footer, null), null, false);
        dragListView.setAdapter(taskArrayAdapter);
        dragListView.setEmptyView(emptyListView);
        dragListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        dragListView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent dragEvent) {
                // Effectively overriding default behavior in the DragListView.
                if (dragEvent.getAction() == DragEvent.ACTION_DRAG_LOCATION) {
                    int mIndex = dragListView.mIndex; // Index of the mobile view in adapter.
                    int dDownY = (int) dragEvent.getY(); // Y pixel location.
                    int dIndex = dragListView.pointToPosition(1, dDownY); // Dragged index in adapter.
                    if (dIndex >= 0
                            && dIndex < taskList.size()
                            && mIndex >= 0
                            && mIndex < taskList.size()
                            && mIndex != dIndex) {
                        // View position that is currently being crossed into.
                        int dPosition = dIndex - dragListView.getFirstVisiblePosition();
                        final View view = dragListView.getChildAt(dPosition);
                        // Only proceed to swap if Y is greater than halfway point of covered View.
                        if (view != null
                                && (dDownY < (view.getTop() + view.getBottom()) / 2)) {
                            return false;
                        }
                        Task temp = taskList.get(mIndex);
                        // Set the row numbers to be what they should be now.
                        taskList.get(mIndex).setRowNumber(dIndex);
                        taskList.get(dIndex).setRowNumber(mIndex);
                        // Swap positions in the list.
                        taskList.set(mIndex, taskList.get(dIndex));
                        taskList.set(dIndex, temp);
                        // Persist to database.
                        dailyDoDatabaseHelper.updateTaskEntry(taskList.get(mIndex));
                        dailyDoDatabaseHelper.updateTaskEntry(taskList.get(dIndex));
                        // Notify the change in the list.
                        taskArrayAdapter.notifyDataSetChanged();
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            if (dragListView.draggedView != null) {
                                dragListView.draggedView.setVisibility(View.VISIBLE);
                            }
                        }
                        // Notify the change in the notification.
                        NotificationService.startNotificationUpdate(TasksFragment.this.getActivity(),
                                taskList.get(mIndex).getId(),
                                taskList.get(mIndex).getIsChecked() == 1);
                        NotificationService.startNotificationUpdate(TasksFragment.this.getActivity(),
                                taskList.get(dIndex).getId(),
                                taskList.get(dIndex).getIsChecked() == 1);
                        // Update the index of the mobile view, since it has been swapped.
                        dragListView.mIndex = dIndex;
                        Log.d(CLASS_NAME, String.format("Swapped OriginalIndex=%d and CoveredIndex=%d", mIndex, dIndex));
                    }
                }
                return false; // Do no want to consume the other drag events in the ListView.
            }
        });
        final View newTaskButton = contentView.findViewById(R.id.new_task_button);
        newTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(TasksFragment.this.getActivity(), TaskDetailsActivity.class);
                intent.putExtra(TaskDetailsActivity.EXTRA_TASK_POSITION, 0);
                startActivityForResult(intent, TaskDetailsActivity.REQUEST_CODE_TASK_CREATE);
            }
        });

        return contentView;
    }

    @Override
    public void onPause() {
        super.onPause();
        // On fragment pause, synchronize the whole list to be safe.
        syncRowNumbers(0);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.getActivity().registerReceiver(resetListener, new IntentFilter(getString(R.string.intent_action_reset_tasks)));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.getActivity().unregisterReceiver(resetListener);
    }

    /**
     * Called when returning to this Activity as a result of an {@link Activity#finish} call to handle the Intent.
     *
     * @param requestCode of the Intent to handle what action to take
     * @param resultCode  of the Intent to determine whether to act
     * @param data        of the Intent to handle the action
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                if (requestCode == TaskDetailsActivity.REQUEST_CODE_TASK_CREATE) {
                    // On creates, we can simply insert the new entry and sync the rowNumbers.
                    taskArrayAdapter.insert(task, 0);
                    syncRowNumbers(0);
                } else {
                    // On updates, we just clear and re-add all the entries to force a UI render.
                    taskArrayAdapter.clear();
                    taskArrayAdapter.addAll(dailyDoDatabaseHelper.getTaskEntries());
                }
                taskArrayAdapter.notifyDataSetChanged();
                dragListView.smoothScrollToPosition(position);
                NotificationService.startNotificationUpdate(this.getActivity(), task.getId(), task.getIsChecked() == 1);
                break;

            case TaskDetailsActivity.RESULT_CODE_DELETE:
                taskId = data.getIntExtra(TaskDetailsActivity.EXTRA_TASK_ID, INVALID_VALUE);
                position = data.getIntExtra(TaskDetailsActivity.EXTRA_TASK_POSITION, INVALID_VALUE);
                if (taskId == INVALID_VALUE || position == INVALID_VALUE) {
                    return;
                }
                NotificationService.startNotificationUpdate(this.getActivity(), taskId, true);
                taskArrayAdapter.clear();
                taskArrayAdapter.addAll(dailyDoDatabaseHelper.getTaskEntries());
                // Sync everything that followed the deleted Task.
                syncRowNumbers(position);
                taskArrayAdapter.notifyDataSetChanged();
                dragListView.smoothScrollToPosition(position);
                break;
        }
    }

    /**
     * Performs a sync on the Task.rowNumber and the list position, starting at the provided point.
     * On deletes, we should only need to sync where the deleted entry was.
     *
     * @param startingPosition to start the sync
     */
    private void syncRowNumbers(int startingPosition) {
        for (int i = startingPosition; i < taskArrayAdapter.getCount(); i++) {
            final Task task = taskArrayAdapter.getItem(i);
            if (task.getRowNumber() != i) {
                task.setRowNumber(i);
                dailyDoDatabaseHelper.updateTaskEntry(task);
            }
        }
    }

    private static class ViewHolder {
        private CheckBox checkBox;
        private LinearLayout contentView;
        private ImageView separatorView;
        private TextView titleView;
        private TextView noteView;
    }

    /**
     * Custom adapter for handling the View for each Task entry as well as providing a stable ID to the DynamicListView.
     * This helps ensure that the DynamicListView will always access the correct Task entry in the List.
     */
    private final class TaskArrayAdapter extends ArrayAdapter<Task> {

        private static final int INVALID_ID = -1;

        public TaskArrayAdapter(Context context, int textViewResourceId, List<Task> objects) {
            super(context, textViewResourceId, objects);
        }

        /**
         * Overridden to leverage our database ID as the unique and stable ID for the given Task position.
         *
         * @param position of the Task
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
            final ViewHolder viewHolder;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.entry_task, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.task_entry_checkbox);
                viewHolder.contentView = (LinearLayout) convertView.findViewById(R.id.task_entry_content);
                viewHolder.separatorView = (ImageView) convertView.findViewById(R.id.task_entry_separator);
                viewHolder.titleView = (TextView) convertView.findViewById(R.id.task_entry_title);
                // The Task note should only have visibility if it is non-null and not empty.
                viewHolder.noteView = (TextView) convertView.findViewById(R.id.task_entry_note);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final Task task = getItem(position);
            final boolean isChecked = task.getIsChecked() == 1;

            // Setup the isChecked state of the view.
            if (isChecked) {
                convertView.setAlpha(0.67f);
            } else {
                convertView.setAlpha(1.0f);
            }
            // Setup the values.
            viewHolder.titleView.setText(task.getTitle());
            if (task.getNote() != null && !task.getNote().isEmpty()) {
                viewHolder.noteView.setText(task.getNote());
                viewHolder.noteView.setVisibility(View.VISIBLE);
                viewHolder.separatorView.setVisibility(View.VISIBLE);
            } else {
                viewHolder.noteView.setVisibility(View.GONE);
                viewHolder.separatorView.setVisibility(View.GONE);
            }
            viewHolder.checkBox.setChecked(isChecked);
            // Setup the listeners.
            viewHolder.contentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startTaskDetailsActivity(task.getId(), position);
                }
            });
            viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewHolder.checkBox.toggle();
                    task.setIsChecked(task.getIsChecked() == 1 ? 0 : 1);
                    task.setRowNumber(position);
                    dailyDoDatabaseHelper.updateTaskEntry(task);
                    notifyDataSetChanged();
                    NotificationService.startNotificationUpdate(getContext(), task.getId(), task.getIsChecked() == 1);
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
        private void startTaskDetailsActivity(int id, int position) {
            final Intent intent = new Intent(getContext(), TaskDetailsActivity.class);
            intent.putExtra(TaskDetailsActivity.EXTRA_TASK_ID, id);
            intent.putExtra(TaskDetailsActivity.EXTRA_TASK_POSITION, position);
            startActivityForResult(intent, TaskDetailsActivity.REQUEST_CODE_TASK_DETAILS);
        }
    }
}
