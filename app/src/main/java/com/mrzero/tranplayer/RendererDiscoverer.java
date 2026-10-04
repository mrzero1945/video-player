package com.mrzero.tranplayer;

import androidx.collection.LongSparseArray;
import com.mrzero.tranplayer.VLCEvent;

/* loaded from: classes3.dex */
public class RendererDiscoverer extends VLCObject<RendererDiscoverer.Event> {
    private static final String TAG = "LibVLC/RendererDiscoverer";
    private final LongSparseArray<RendererItem> index;

    public static class Description {
        public final String longName;
        public final String name;

        private Description(String str, String str2) {
            this.name = str;
            this.longName = str2;
        }
    }

    public static class Event extends VLCEvent {
        public static final int ItemAdded = 1282;
        public static final int ItemDeleted = 1283;
        private RendererItem item;

        public Event(int i10, long j10, RendererItem rendererItem) {
            super(i10, j10);
            this.item = rendererItem;
        }

        public RendererItem getItem() {
            return this.item;
        }
    }

    public interface EventListener extends VLCEvent.Listener<Event> {
    }

    public RendererDiscoverer(LibVLC libVLC, String str) {
        super(libVLC);
        this.index = new LongSparseArray<>();
        nativeNew(libVLC, str);
    }

    private static Description createDescriptionFromNative(String str, String str2) {
        return new Description(str, str2);
    }

    private static RendererItem createItemFromNative(String str, String str2, String str3, int i10, long j10) {
        return new RendererItem(str, str2, str3, i10, j10);
    }

    private synchronized RendererItem insertItemFromEvent(long j10) {
        RendererItem rendererItem;
        rendererItem = new RendererItem(this, j10);
        this.index.put(j10, rendererItem);
        return rendererItem;
    }

    public static Description[] list(LibVLC libVLC) {
        return nativeList(libVLC);
    }

    private static native Description[] nativeList(LibVLC libVLC);

    private native void nativeNew(LibVLC libVLC, String str);

    private native void nativeRelease();

    private native boolean nativeStart();

    private native void nativeStop();

    private synchronized RendererItem removeItemFromEvent(long j10) {
        RendererItem rendererItem;
        rendererItem = this.index.get(j10);
        if (rendererItem != null) {
            this.index.remove(j10);
        }
        return rendererItem;
    }

    @Override // com.mrzero.tranplayer.VLCObject
    public /* bridge */ /* synthetic */ boolean isReleased() {
        return super.isReleased();
    }

    @Override // com.mrzero.tranplayer.VLCObject
    public void onReleaseNative() {
        nativeRelease();
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
        setEventListener((EventListener) null);
        nativeStop();
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.mrzero.tranplayer.VLCObject
    public Event onEventNative(int i10, long j10, long j11, long j12, long j13, float f10, String str) {
        if (i10 == 1282) {
            return new Event(i10, j10, insertItemFromEvent(j10));
        }
        if (i10 != 1283) {
            return null;
        }
        return new Event(i10, j10, removeItemFromEvent(j10));
    }
}
