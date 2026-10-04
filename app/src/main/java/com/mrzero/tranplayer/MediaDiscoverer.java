package com.mrzero.tranplayer;

import androidx.annotation.Nullable;
import com.mrzero.tranplayer.VLCEvent;

/* loaded from: classes3.dex */
public class MediaDiscoverer extends VLCObject<MediaDiscoverer.Event> {
    private static final String TAG = "LibVLC/MediaDiscoverer";
    private MediaList mMediaList;

    public static class Description {
        public final int category;
        public final String longName;
        public final String name;

        public static class Category {
            public static final int Devices = 0;
            public static final int Lan = 1;
            public static final int LocalDirs = 3;
            public static final int Podcasts = 2;
        }

        private Description(String str, String str2, int i10) {
            this.name = str;
            this.longName = str2;
            this.category = i10;
        }
    }

    public static class Event extends VLCEvent {
        public static final int Ended = 1281;
        public static final int Started = 1280;

        public Event(int i10) {
            super(i10);
        }
    }

    public interface EventListener extends VLCEvent.Listener<Event> {
    }

    public MediaDiscoverer(LibVLC libVLC, String str) {
        super(libVLC);
        this.mMediaList = null;
        nativeNew(libVLC, str);
    }

    private static Description createDescriptionFromNative(String str, String str2, int i10) {
        return new Description(str, str2, i10);
    }

    @Nullable
    public static Description[] list(LibVLC libVLC, int i10) {
        return nativeList(libVLC, i10);
    }

    private static native Description[] nativeList(LibVLC libVLC, int i10);

    private native void nativeNew(LibVLC libVLC, String str);

    private native void nativeRelease();

    private native boolean nativeStart();

    private native void nativeStop();

    public MediaList getMediaList() {
        MediaList mediaList;
        synchronized (this) {
            MediaList mediaList2 = this.mMediaList;
            if (mediaList2 != null) {
                mediaList2.retain();
                return this.mMediaList;
            }
            MediaList mediaList3 = new MediaList(this);
            synchronized (this) {
                this.mMediaList = mediaList3;
                mediaList3.retain();
                mediaList = this.mMediaList;
            }
            return mediaList;
        }
    }

    @Override // com.mrzero.tranplayer.VLCObject
    public /* bridge */ /* synthetic */ boolean isReleased() {
        return super.isReleased();
    }

    @Override // com.mrzero.tranplayer.VLCObject
    public void onReleaseNative() {
        MediaList mediaList = this.mMediaList;
        if (mediaList != null) {
            mediaList.release();
        }
        nativeRelease();
    }

    @Override // com.mrzero.tranplayer.VLCObject
    public /* bridge */ /* synthetic */ void release() {
        super.release();
    }

    public void setEventListener(EventListener eventListener) {
        super.setEventListener((VLCEvent.Listener) eventListener);
    }

    @Override // com.mrzero.tranplayer.VLCObject
    public /* bridge */ /* synthetic */ void setPinPVideoMode(boolean z10) {
        super.setPinPVideoMode(z10);
    }

    public boolean start() {
        if (isReleased()) {
            throw new IllegalStateException("MediaDiscoverer is released");
        }
        return nativeStart();
    }

    public void stop() {
        if (isReleased()) {
            throw new IllegalStateException("MediaDiscoverer is released");
        }
        nativeStop();
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.mrzero.tranplayer.VLCObject
    public Event onEventNative(int i10, long j10, long j11, long j12, long j13, float f10, String str) {
        if (i10 == 1280 || i10 == 1281) {
            return new Event(i10);
        }
        return null;
    }
}
