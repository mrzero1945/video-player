package com.mrzero.tranplayer;

/* loaded from: classes3.dex */
public class RendererItem extends VLCObject<RendererItem.Event> {
    public static final int LIBVLC_RENDERER_CAN_AUDIO = 1;
    public static final int LIBVLC_RENDERER_CAN_VIDEO = 2;
    public final String displayName;
    public final int flags;
    public final String iconUrl;
    public final String name;
    private final long ref;
    public final String type;

    public static class Event extends VLCEvent {
        public Event(int i10) {
            super(i10);
        }
    }

    public RendererItem(RendererDiscoverer rendererDiscoverer, long j10) {
        super(rendererDiscoverer);
        RendererItem nativeNewItem = nativeNewItem(rendererDiscoverer, j10);
        this.name = nativeNewItem == null ? null : nativeNewItem.name;
        this.displayName = nativeNewItem == null ? null : nativeNewItem.displayName;
        this.type = nativeNewItem == null ? null : nativeNewItem.type;
        this.iconUrl = nativeNewItem != null ? nativeNewItem.iconUrl : null;
        this.flags = nativeNewItem == null ? 0 : nativeNewItem.flags;
        this.ref = nativeNewItem != null ? nativeNewItem.ref : j10;
    }

    private native RendererItem nativeNewItem(RendererDiscoverer rendererDiscoverer, long j10);

    private native void nativeReleaseItem();

    public boolean equals(Object obj) {
        return (obj instanceof RendererItem) && this.ref == ((RendererItem) obj).ref;
    }

    @Override // com.mrzero.tranplayer.VLCObject
    public /* bridge */ /* synthetic */ boolean isReleased() {
        return super.isReleased();
    }

    @Override // com.mrzero.tranplayer.VLCObject
    public void onReleaseNative() {
        nativeReleaseItem();
    }

    @Override // com.mrzero.tranplayer.VLCObject
    public /* bridge */ /* synthetic */ void setPinPVideoMode(boolean z10) {
        super.setPinPVideoMode(z10);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.mrzero.tranplayer.VLCObject
    public Event onEventNative(int i10, long j10, long j11, long j12, long j13, float f10, String str) {
        return new Event(i10);
    }

    public RendererItem(String str, String str2, String str3, int i10, long j10) {
        int lastIndexOf = str.lastIndexOf(45);
        this.name = str;
        this.displayName = lastIndexOf != -1 ? str.substring(0, lastIndexOf).replace('-', ' ') : str;
        this.type = str2;
        this.iconUrl = str3;
        this.flags = i10;
        this.ref = j10;
    }
}
