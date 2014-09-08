package com.finaldrive.dailydo.domain;

/**
 * POJO to capture a Task entry.
 */
public class Task {

    /**
     * Database row ID.
     */
    private int id;

    /**
     * Title for this given Task.
     */
    private String title;

    /**
     * Specific notes for this given Task.
     */
    private String note;

    /**
     * Row in the custom sort list for this Task.
     */
    private int rowNumber;

    /**
     * Status of this given Task.
     */
    private int isChecked = 0;

    public Task() {
    }

    public Task(String title, String note, int rowNumber) {
        this.title = title;
        this.note = note;
        this.rowNumber = rowNumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getRowNumber() {
        return rowNumber;
    }

    public void setRowNumber(int rowNumber) {
        this.rowNumber = rowNumber;
    }

    public int getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(int isChecked) {
        this.isChecked = isChecked;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", note='" + note + '\'' +
                ", rowNumber=" + rowNumber +
                ", isChecked=" + isChecked +
                '}';
    }
}
