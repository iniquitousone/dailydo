package com.finaldrive.dailydo.view;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

/**
 * Generic DragListView for handling swapping two items in a ListView using the Drag-and-drop API.
 */
public class DragListView extends ListView {

    private static final String CLASS_NAME = "DragListView";
    private boolean isDragging = false;
    public View draggedView = null;
    private int mDownX = -1;
    private int mDownY = -1;
    /**
     * The mobile position in the ListView accounting for visible items.
     */
    public int mPosition = -1;
    /**
     * The index in the List of the mobile position.
     */
    public int mIndex = -1;
    /**
     * The dragged position in the ListView accounting for visible items.
     */
    public int dPosition = -1;
    /**
     * The index in the List of the dragged position.
     */
    public int dIndex = -1;

    public DragListView(Context context) {
        super(context);
        init(context);
    }

    public DragListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DragListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mDownX = (int) motionEvent.getX();
                mDownY = (int) motionEvent.getY();
                if ((mDownX >= 0 && mDownX <= 70) && !isDragging) {
                    isDragging = true;
                    mIndex = pointToPosition(mDownX, mDownY);
                    mPosition = mIndex - getFirstVisiblePosition();
                    if (mPosition <= INVALID_POSITION) {
                        isDragging = false;
                        return true;
                    }
                    Log.d(CLASS_NAME, String.format("Initiated drag at Index=%d", mIndex));
                    draggedView = getChildAt(mPosition);
                    final ClipData dragData = ClipData.newPlainText(String.valueOf(draggedView.getId()), "");
                    final DragShadowBuilder dragShadowBuilder = new DragShadowBuilder(draggedView) {
                        @Override
                        public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
                            shadowSize.set(draggedView.getWidth(), draggedView.getHeight());
                            shadowTouchPoint.set(shadowTouchPoint.x, shadowSize.y / 2);
                        }
                    };
                    draggedView.startDrag(dragData, dragShadowBuilder, null, 0);
                    draggedView.setVisibility(View.INVISIBLE);
                }
                break;
        }

        return super.onTouchEvent(motionEvent);
    }

    @Override
    public boolean onDragEvent(DragEvent dragEvent) {
        switch (dragEvent.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                return true;
            case DragEvent.ACTION_DRAG_ENTERED:
                return true;
            case DragEvent.ACTION_DRAG_LOCATION:
                return true;
            case DragEvent.ACTION_DROP:
                return true;
            case DragEvent.ACTION_DRAG_ENDED:
                draggedView.setVisibility(View.VISIBLE);
                isDragging = false;
                return true;
            case DragEvent.ACTION_DRAG_EXITED:
                isDragging = false;
                return true;
            default:
                return false;
        }
    }

    public void setIsDragging(boolean isDragging) {
        this.isDragging = isDragging;
    }
}
