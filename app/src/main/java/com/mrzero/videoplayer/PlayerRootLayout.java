package com.mrzero.videoplayer;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

/**
 * Full screen gesture surface used by the player.
 * Vertical drag on the left half = brightness, right half = volume,
 * horizontal drag = seek, single tap = show/hide controls, double tap = seek +-10s.
 */
public class PlayerRootLayout extends FrameLayout {

    public interface Listener {
        void onSingleTap();

        void onDoubleTap(boolean forward);

        void onGestureStart(boolean horizontal, boolean leftSide);

        void onGestureMove(boolean horizontal, boolean leftSide, float deltaRatio, float rawDelta);

        void onGestureEnd(boolean horizontal, boolean leftSide, float deltaRatio, float rawDelta);

        void onGestureCancel();
    }

    private static final int SLOP_UNSET = -1;

    private Listener listener;
    private final int touchSlop;
    private final long tapTimeout;

    private float downX, downY;
    private long downTime;
    private int mode = SLOP_UNSET; // 0 idle, 1 horizontal, 2 vertical
    private boolean leftSide;
    private boolean moved;
    private float accumDelta;
    private long lastTapTime;
    private float lastTapX;

    public PlayerRootLayout(Context context) {
        this(context, null);
    }

    public PlayerRootLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlayerRootLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        tapTimeout = ViewConfiguration.get(context).getTapTimeout();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (listener == null) return super.onTouchEvent(ev);
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                downY = ev.getY();
                downTime = System.currentTimeMillis();
                mode = SLOP_UNSET;
                moved = false;
                accumDelta = 0;
                return true;
            case MotionEvent.ACTION_MOVE: {
                float dx = ev.getX() - downX;
                float dy = ev.getY() - downY;
                if (mode == SLOP_UNSET) {
                    if (Math.abs(dx) > touchSlop || Math.abs(dy) > touchSlop) {
                        moved = true;
                        if (Math.abs(dx) > Math.abs(dy)) {
                            mode = 1;
                            leftSide = downX < getWidth() / 2f;
                            accumDelta = dx;
                            listener.onGestureStart(true, leftSide);
                        } else {
                            mode = 2;
                            leftSide = downX < getWidth() / 2f;
                            accumDelta = dy;
                            listener.onGestureStart(false, leftSide);
                        }
                    }
                } else if (mode == 1) {
                    accumDelta = dx;
                    float ratio = getWidth() > 0 ? dx / getWidth() : 0;
                    listener.onGestureMove(true, leftSide, ratio, dx);
                } else {
                    accumDelta = dy;
                    float ratio = getHeight() > 0 ? dy / getHeight() : 0;
                    listener.onGestureMove(false, leftSide, ratio, dy);
                }
                return true;
            }
            case MotionEvent.ACTION_UP: {
                if (mode == 1) {
                    float ratio = getWidth() > 0 ? accumDelta / getWidth() : 0;
                    listener.onGestureEnd(true, leftSide, ratio, accumDelta);
                } else if (mode == 2) {
                    float ratio = getHeight() > 0 ? accumDelta / getHeight() : 0;
                    listener.onGestureEnd(false, leftSide, ratio, accumDelta);
                } else if (!moved) {
                    long now = System.currentTimeMillis();
                    float x = ev.getX();
                    boolean doubleTap = now - lastTapTime < 300
                            && Math.abs(x - lastTapX) < getWidth() * 0.4f;
                    if (doubleTap) {
                        lastTapTime = 0;
                        listener.onDoubleTap(x > getWidth() / 2f);
                    } else {
                        lastTapTime = now;
                        lastTapX = x;
                        final Handler h = new Handler(getContext().getMainLooper());
                        h.postDelayed(() -> {
                            if (lastTapTime != 0
                                    && System.currentTimeMillis() - lastTapTime >= 290) {
                                lastTapTime = 0;
                                listener.onSingleTap();
                            }
                        }, 300);
                    }
                }
                mode = SLOP_UNSET;
                return true;
            }
            case MotionEvent.ACTION_CANCEL:
                if (mode == 1 || mode == 2) listener.onGestureCancel();
                mode = SLOP_UNSET;
                return true;
            default:
                return super.onTouchEvent(ev);
        }
    }
}
