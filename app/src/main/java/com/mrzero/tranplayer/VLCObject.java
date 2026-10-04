package com.mrzero.tranplayer;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.mrzero.tranplayer.VLCEvent;
import java.lang.ref.WeakReference;

/* loaded from: classes3.dex */
abstract class VLCObject<T extends VLCEvent> {
    private VLCEvent.Listener<T> mEventListener;
    private Handler mHandler;
    private long mInstance;
    public final LibVLC mLibVLC;
    private int mNativeRefCount;
    private boolean mPiPMode;

    public VLCObject(LibVLC libVLC) {
        this.mEventListener = null;
        this.mHandler = null;
        this.mNativeRefCount = 1;
        this.mPiPMode = false;
        this.mInstance = 0L;
        this.mLibVLC = libVLC;
    }

    private synchronized void dispatchEventFromNative(int i10, long j10, long j11, long j12, long j13, float f10, String str) {
        VLCEvent.Listener<T> listener;
        Handler handler;
        VLCEvent.Listener<T> listener2;
        if (isReleased()) {
            return;
        }
        T onEventNative = onEventNative(i10, j10, j11, j12, j13, f10, str);
        if (this.mPiPMode && ((i10 == 278 && j10 == 1 && j11 == 0) || i10 == 260 || i10 == 261 || i10 == 262)) {
            Log.d("TranMediaPlayer", "dispatchEventFromNative eventType:" + i10 + " rg1:" + j10 + " arg2: " + j11 + " arg3: " + j12 + " arg4: " + j13 + " argf1: " + f10 + " argstr: " + str);
            if (onEventNative != null && (listener2 = this.mEventListener) != null && this.mHandler != null) {
                listener2.onEvent(onEventNative);
            }
        } else if (onEventNative != null && (listener = this.mEventListener) != null && (handler = this.mHandler) != null) {
            final VLCEvent.Listener<T> runnableListener = listener;
            final T runnableEvent = onEventNative;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    runnableListener.onEvent(runnableEvent);
                    runnableEvent.release();
                }
            });        }
    }

    private static void dispatchEventFromWeakNative(Object obj, int i10, long j10, long j11, long j12, long j13, float f10, String str) {
        VLCObject vLCObject = (VLCObject) ((WeakReference) obj).get();
        if (vLCObject != null) {
            vLCObject.dispatchEventFromNative(i10, j10, j11, j12, j13, f10, str);
        }
    }

    private Object getWeakReference() {
        return new WeakReference(this);
    }

    private native void nativeDetachEvents();

    public synchronized void finalize() {
        if (!isReleased()) {
            throw new AssertionError("VLCObject (" + getClass().getName() + ") finalized but not natively released (" + this.mNativeRefCount + " refs)");
        }
    }

    public synchronized boolean isReleased() {
        return this.mNativeRefCount == 0;
    }

    public abstract T onEventNative(int i10, long j10, long j11, long j12, long j13, float f10, String str);

    public abstract void onReleaseNative();

    public void release() {
        int i10;
        synchronized (this) {
            int i11 = this.mNativeRefCount;
            if (i11 == 0) {
                return;
            }
            if (i11 > 0) {
                i10 = i11 - 1;
                this.mNativeRefCount = i10;
            } else {
                i10 = -1;
            }
            if (i10 == 0) {
                setEventListener(null);
            }
            if (i10 == 0) {
                nativeDetachEvents();
                synchronized (this) {
                    onReleaseNative();
                }
            }
        }
    }

    public synchronized boolean retain() {
        int i10 = this.mNativeRefCount;
        if (i10 <= 0) {
            return false;
        }
        this.mNativeRefCount = i10 + 1;
        return true;
    }

    public synchronized void setEventListener(VLCEvent.Listener<T> listener) {
        setEventListener(listener, null);
    }

    public void setPinPVideoMode(boolean z10) {
        this.mPiPMode = z10;
    }

    public synchronized void setEventListener(VLCEvent.Listener<T> listener, Handler handler) {
        Handler handler2 = this.mHandler;
        if (handler2 != null) {
            handler2.removeCallbacksAndMessages(null);
        }
        this.mEventListener = listener;
        if (listener == null) {
            this.mHandler = null;
        } else if (this.mHandler == null) {
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
            this.mHandler = handler;
        }
    }

    public VLCObject(VLCObject vLCObject) {
        this.mEventListener = null;
        this.mHandler = null;
        this.mNativeRefCount = 1;
        this.mPiPMode = false;
        this.mInstance = 0L;
        this.mLibVLC = vLCObject.mLibVLC;
    }

    public VLCObject() {
        this.mEventListener = null;
        this.mHandler = null;
        this.mNativeRefCount = 1;
        this.mPiPMode = false;
        this.mInstance = 0L;
        this.mLibVLC = null;
    }
}
