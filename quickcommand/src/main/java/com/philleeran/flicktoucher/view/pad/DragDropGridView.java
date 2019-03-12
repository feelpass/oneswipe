
package com.philleeran.flicktoucher.view.pad;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

public class DragDropGridView extends GridView {
    private Context _context;

    private int _selectItemIndex = -1;

    private ImageView _dragView;

    private WindowManager _windowManager;

    private WindowManager.LayoutParams _windowParams;

    private Point _touchPosition = new Point(0, 0);

    private Point _touchPositionOffset = new Point(0, 0);

    private OnDropListener _onDropListener;

    public DragDropGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public DragDropGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DragDropGridView(Context context) {
        super(context);
        init(context);
    }

    public void init(Context context) {
        _context = context;
        setOnItemLongClickListener(onItemLongClickListener);

        _windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        _windowParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        _windowParams.gravity = Gravity.LEFT | Gravity.TOP;
        _windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        _windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        _windowParams.format = PixelFormat.TRANSLUCENT;
        _windowParams.flags = WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING//
                | WindowManager.LayoutParams.FLAG_FULLSCREEN
                // | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED//
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS//
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                // | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                // | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        int x = (int) e.getRawX();
        int y = (int) e.getRawY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                _touchPosition.set(x, y);
                break;
        }
        return super.onInterceptTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int x = (int) e.getRawX();
        int y = (int) e.getRawY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                doDragView(x, y);
                break;

            case MotionEvent.ACTION_UP:
                doDropView((int) e.getX(), (int) e.getY());
                break;

            case MotionEvent.ACTION_CANCEL:
                setNullDragView();
                break;

            default:
                break;
        }
        return super.onTouchEvent(e);
    }

    private void doMakeDragview() {
        View item = (View) getChildAt(_selectItemIndex - getFirstVisiblePosition()); // getFirstVisiblePosition()

        item.destroyDrawingCache();
        item.buildDrawingCache();
        Bitmap bitmap = item.getDrawingCache();

        ImageView image = new ImageView(_context);
        image.setBackgroundColor(Color.TRANSPARENT);
        image.setImageBitmap(bitmap);

        _touchPositionOffset.x = (int) (item.getWidth() * 0.5);
        _touchPositionOffset.y = (int) (item.getHeight() * 0.5);

        _windowParams.x = _touchPosition.x - _touchPositionOffset.x;
        _windowParams.y = _touchPosition.y - _touchPositionOffset.y;
        _windowManager.addView(image, _windowParams);
        _dragView = image;
        _dragView.setScaleX(1.5f);
        _dragView.setScaleY(1.5f);
    }

    private void doDragView(int x, int y) {
        if (_dragView == null)
            return;

        _windowParams.x = x - _touchPositionOffset.x;
        _windowParams.y = y - _touchPositionOffset.y;
        _windowManager.updateViewLayout(_dragView, _windowParams);
    }

    private void doDropView(int x, int y) {
        if (_dragView == null)
            return;

        int toIndex = pointToPosition(x, y);
        if (toIndex <= INVALID_POSITION) {
            setNullDragView();
            return;
        }

        _onDropListener.drop(_selectItemIndex, toIndex);
        setNullDragView();
    }

    private void setNullDragView() {
        if (_dragView != null) {
            _windowManager.removeView(_dragView);
            _dragView = null;
        }
    }

    public void setOnDropListener(OnDropListener listener) {
        _onDropListener = listener;
    }

    /**
     * *************
     * Listener
     * *************
     */
    OnItemLongClickListener onItemLongClickListener = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
            if (position <= INVALID_POSITION)
                return true;

            _selectItemIndex = position;

            doMakeDragview();
            return true;
        }
    };

    public void doDrag(int position) {
        _selectItemIndex = position;
        doMakeDragview();
    }

    public interface OnDragListener {
        void drag(int from, int to);
    }

    public interface OnDropListener {
        void drop(int from, int to);
    }
}
