package com.mrzero.tranplayer;

import android.os.Handler;
import android.util.SparseArray;
import com.mrzero.tranplayer.VLCEvent;

/* loaded from: classes3.dex */
public class MediaList extends VLCObject<MediaList.Event> {
    private static final String TAG = "LibVLC/MediaList";
    private int mCount;
    private boolean mLocked;
    private final SparseArray<Media> mMediaArray;

    public static class Event extends VLCEvent {
        public static final int EndReached = 516;
        public static final int ItemAdded = 512;
        public static final int ItemDeleted = 514;
        public final int index;
        public final Media media;
        private final boolean retain;

        public Event(int i10, Media media, boolean z10, int i11) {
            super(i10);
            if (z10 && (media == null || !media.retain())) {
                throw new IllegalStateException("invalid media reference");
            }
            this.media = media;
            this.retain = z10;
            this.index = i11;
        }

        @Override // com.mrzero.tranplayer.VLCEvent
        public void release() {
            if (this.retain) {
                this.media.release();
            }
        }
    }

    public interface EventListener extends VLCEvent.Listener<Event> {
    }

    public MediaList(LibVLC libVLC) {
        super(libVLC);
        this.mCount = 0;
        this.mMediaArray = new SparseArray<>();
        this.mLocked = false;
        nativeNewFromLibVlc(libVLC);
        init();
    }

    private void init() {
        lock();
        this.mCount = nativeGetCount();
        for (int i10 = 0; i10 < this.mCount; i10++) {
            this.mMediaArray.put(i10, new Media(this, i10));
        }
        unlock();
    }

    private synchronized Media insertMediaFromEvent(int i10) {
        Media media;
        int i11 = this.mCount + 1;
        this.mCount = i11;
        for (int i12 = i11 - 1; i12 >= i10; i12--) {
            SparseArray<Media> sparseArray = this.mMediaArray;
            sparseArray.put(i12 + 1, sparseArray.valueAt(i12));
        }
        media = new Media(this, i10);
        this.mMediaArray.put(i10, media);
        return media;
    }

    private synchronized void lock() {
        if (this.mLocked) {
            throw new IllegalStateException("already locked");
        }
        this.mLocked = true;
        nativeLock();
    }

    private native int nativeGetCount();

    private native void nativeLock();

    private native void nativeNewFromLibVlc(LibVLC libVLC);

    private native void nativeNewFromMedia(Media media);

    private native void nativeNewFromMediaDiscoverer(MediaDiscoverer mediaDiscoverer);

    private native void nativeRelease();

    private native void nativeUnlock();

    private synchronized Media removeMediaFromEvent(int i10) {
        Media media;
        this.mCount--;
        media = this.mMediaArray.get(i10);
        if (media != null) {
            media.release();
        }
        while (i10 < this.mCount) {
            SparseArray<Media> sparseArray = this.mMediaArray;
            int i11 = i10 + 1;
            sparseArray.put(i10, sparseArray.valueAt(i11));
            i10 = i11;
        }
        return media;
    }

    private synchronized void unlock() {
        if (!this.mLocked) {
            throw new IllegalStateException("not locked");
        }
        this.mLocked = false;
        nativeUnlock();
    }

    public synchronized int getCount() {
        return this.mCount;
    }

    public synchronized Media getMediaAt(int i10) {
        if (i10 >= 0 && i10 < getCount()) {
            Media media = this.mMediaArray.get(i10);
            media.retain();
            return media;
        }
        throw new IndexOutOfBoundsException();
    }

    public synchronized boolean isLocked() {
        return this.mLocked;
    }

    @Override // com.mrzero.tranplayer.VLCObject
    public /* bridge */ /* synthetic */ boolean isReleased() {
        return super.isReleased();
    }

    @Override // com.mrzero.tranplayer.VLCObject
    public void onReleaseNative() {
        for (int i10 = 0; i10 < this.mMediaArray.size(); i10++) {
            Media media = this.mMediaArray.get(i10);
            if (media != null) {
                media.release();
            }
        }
        nativeRelease();
    }

    @Override // com.mrzero.tranplayer.VLCObject
    public /* bridge */ /* synthetic */ void release() {
        super.release();
    }

    public void setEventListener(EventListener eventListener, Handler handler) {
        super.setEventListener((VLCEvent.Listener) eventListener, handler);
    }

    @Override // com.mrzero.tranplayer.VLCObject
    public /* bridge */ /* synthetic */ void setPinPVideoMode(boolean z10) {
        super.setPinPVideoMode(z10);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.mrzero.tranplayer.VLCObject
    public synchronized Event onEventNative(int i10, long j10, long j11, long j12, long j13, float f10, String str) {
        Event event;
        if (this.mLocked) {
            throw new IllegalStateException("already locked from event callback");
        }
        this.mLocked = true;
        event = null;
        if (i10 == 512) {
            int i11 = (int) j10;
            if (i11 != -1) {
                event = new Event(i10, insertMediaFromEvent(i11), true, i11);
            }
        } else if (i10 == 514) {
            int i12 = (int) j10;
            if (i12 != -1) {
                event = new Event(i10, removeMediaFromEvent(i12), false, i12);
            }
        } else if (i10 == 516) {
            event = new Event(i10, null, false, -1);
        }
        this.mLocked = false;
        return event;
    }

    public MediaList(MediaDiscoverer mediaDiscoverer) {
        super(mediaDiscoverer);
        this.mCount = 0;
        this.mMediaArray = new SparseArray<>();
        this.mLocked = false;
        nativeNewFromMediaDiscoverer(mediaDiscoverer);
        init();
    }

    public MediaList(Media media) {
        super(media);
        this.mCount = 0;
        this.mMediaArray = new SparseArray<>();
        this.mLocked = false;
        nativeNewFromMedia(media);
        init();
    }
}
