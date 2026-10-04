package com.mrzero.tranplayer;

/* loaded from: classes3.dex */
public abstract class VLCEvent {
    public final long arg1;
    public final long arg2;
    public final long arg3;
    public final long arg4;
    public final float argf1;
    public final String argstr;
    public final int type;

    public interface Listener<T extends VLCEvent> {
        void onEvent(T t10);
    }

    public VLCEvent(int i10) {
        this.type = i10;
        this.arg1 = 0L;
        this.arg2 = 0L;
        this.arg3 = 0L;
        this.arg4 = 0L;
        this.argf1 = 0.0f;
        this.argstr = null;
    }

    public void release() {
    }

    public VLCEvent(int i10, long j10) {
        this.type = i10;
        this.arg1 = j10;
        this.arg2 = 0L;
        this.arg3 = 0L;
        this.arg4 = 0L;
        this.argf1 = 0.0f;
        this.argstr = null;
    }

    public VLCEvent(int i10, long j10, long j11) {
        this.type = i10;
        this.arg1 = j10;
        this.arg2 = j11;
        this.arg3 = 0L;
        this.arg4 = 0L;
        this.argf1 = 0.0f;
        this.argstr = null;
    }

    public VLCEvent(int i10, long j10, long j11, long j12, long j13) {
        this.type = i10;
        this.arg1 = j10;
        this.arg2 = j11;
        this.arg3 = j12;
        this.arg4 = j13;
        this.argf1 = 0.0f;
        this.argstr = null;
    }

    public VLCEvent(int i10, float f10) {
        this.type = i10;
        this.arg1 = 0L;
        this.arg2 = 0L;
        this.arg3 = 0L;
        this.arg4 = 0L;
        this.argf1 = f10;
        this.argstr = null;
    }

    public VLCEvent(int i10, long j10, long j11, String str) {
        this.type = i10;
        this.arg1 = j10;
        this.arg2 = j11;
        this.arg3 = 0L;
        this.arg4 = 0L;
        this.argf1 = 0.0f;
        this.argstr = str;
    }
}
