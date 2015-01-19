package com.finaldrive.dailydo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.finaldrive.dailydo.domain.Task;
import com.finaldrive.dailydo.store.DailyDoDatabaseHelper;

/**
 * Activity for handling the detailed view of a Task.
 * Said view is also leveraged for editing and deleting.
 */
public class TaskDetailsActivity extends ActionBarActivity {

    /**
     * Request code for the create Task intent.
     */
    public static final int REQUEST_CODE_TASK_CREATE = 1;
    /**
     * Request code for the detailed Task view intent.
     */
    public static final int REQUEST_CODE_TASK_DETAILS = 2;
    /**
     * Extra key for the id in details Intent.
     */
    public static final String EXTRA_TASK_ID = "com.finaldrive.dailydo.activity.extra.TASK_ID";
    /**
     * Extra key for the position in details Intent.
     */
    public static final String EXTRA_TASK_POSITION = "com.finaldrive.dailydo.activity.extra.TASK_POSITION";
    /**
     * Result code to represent a deletion.
     */
    public static final int RESULT_CODE_DELETE = -99;
    /**
     * Intent default extra to represent this was a create Task event.
     */
    public static final int TASK_CREATE_ID = -1;
    private static final int INVALID_VALUE = -99;
    private DailyDoDatabaseHelper dailyDoDatabaseHelper;
    // The unique id of the Task entry.
    private int taskId;
    // Position of the Task within the List itself, NOT the rowNumber stored on the entry.
    private int position;

    @Override
    public void onBackPressed() {
        final EditText titleView = (EditText) findViewById(R.id.details_title);
        final EditText noteView = (EditText) findViewById(R.id.details_note);
        final CheckBox checkBox = (CheckBox) findViewById(R.id.details_checkbox);
        final String title = titleView.getText().toString();
        if (title.isEmpty()) {
            Toast.makeText(this, "Please provide a title for your entry", Toast.LENGTH_SHORT).show();
            return;
        }
        final String note = noteView.getText().toString();
        final Intent editIntent = new Intent(this, MainActivity.class);
        Task task;
        if (taskId == TASK_CREATE_ID) {
            task = new Task(title, note, position);
            task.setIsChecked(checkBox.isChecked() ? 1 : 0);
            task = dailyDoDatabaseHelper.insertTaskEntry(task);
        } else {
            task = dailyDoDatabaseHelper.getTaskEntry(taskId);
            task.setIsChecked(checkBox.isChecked() ? 1 : 0);
            task.setTitle(title);
            task.setNote(note);
            task.setRowNumber(position);
            dailyDoDatabaseHelper.updateTaskEntry(task);
        }
        editIntent.putExtra(EXTRA_TASK_ID, task.getId());
        editIntent.putExtra(EXTRA_TASK_POSITION, position);
        setResult(Activity.RESULT_OK, editIntent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);
        dailyDoDatabaseHelper = DailyDoDatabaseHelper.getInstance(this);
        // Fetch the Intent extra. If the extra is not found, then this is a create Task event.
        Intent intent = getIntent();
        taskId = intent.getIntExtra(EXTRA_TASK_ID, TASK_CREATE_ID);
        position = intent.getIntExtra(EXTRA_TASK_POSITION, INVALID_VALUE);
        final CheckBox checkBox = (CheckBox) findViewById(R.id.details_checkbox);
        // Only load the views with the appropriate text if this is NOT a new Task.
        if (taskId != TASK_CREATE_ID) {
            final Task task = dailyDoDatabaseHelper.getTaskEntry(taskId);
            final EditText titleView = (EditText) findViewById(R.id.details_title);
            final EditText noteView = (EditText) findViewById(R.id.details_note);
            titleView.setText(task.getTitle());
            noteView.setText(task.getNote());
            checkBox.setChecked(task.getIsChecked() == 1);
        } else {
            // Bring up the keyboard whenever it is a new Task for easier input.
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.task_details, menu);
        return true;
    }

    /**
     * Handles ActionBar item clicks.
     *
     * @param item
     * @return boolean for if item was selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete_task:
                if (taskId == TASK_CREATE_ID) {
                    Toast.makeText(this, "Discarded", Toast.LENGTH_SHORT).show();
                    finish();
                    return true;
                }
                final AlertDialog alertDialog = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                        .setTitle("Confirm deletion")
                        .setMessage("Do you want to delete this DO?")
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
                                if (taskId == TASK_CREATE_ID) {
                                    // If the user tried to delete an uncreated Task, just bail as a cancel.
                                    setResult(Activity.RESULT_CANCELED);
                                } else {
                                    // Send Intent to MainActivity to remove the Task from the list.
                                    dailyDoDatabaseHelper.deleteTaskEntry(taskId);
                                    final Intent deleteIntent = new Intent(getApplicationContext(), MainActivity.class);
                                    deleteIntent.putExtra(EXTRA_TASK_ID, taskId);
                                    deleteIntent.putExtra(EXTRA_TASK_POSITION, position);
                                    setResult(RESULT_CODE_DELETE, deleteIntent);
                                }
                                Toast.makeText(getApplicationContext(), "Discarded", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .create();
                alertDialog.show();
                return false;

            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
