package com.mrzero.tranplayer;

import android.content.Context;
import android.util.Log;
import com.mrzero.tranplayer.util.HWDecoderUtil;
import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: classes3.dex */
public class LibVLC extends VLCObject<LibVLC.Event> {
    private static final String TAG = "VLC/LibVLC";
    private static boolean sLoaded = false;
    public final Context mAppContext;

    public static class Event extends VLCEvent {
        public Event(int i10) {
            super(i10);
        }
    }

    public LibVLC(Context context, ArrayList<String> arrayList) {
        this.mAppContext = context.getApplicationContext();
        loadLibraries();
        arrayList = arrayList == null ? new ArrayList<>() : arrayList;
        Iterator<String> it = arrayList.iterator();
        boolean z10 = true;
        boolean z11 = true;
        while (it.hasNext()) {
            String next = it.next();
            z10 = next.startsWith("--aout=") ? false : z10;
            z11 = next.startsWith("--android-display-chroma") ? false : z11;
            if (!z10 && !z11) {
                break;
            }
        }
        if (z10 || z11) {
            if (z10) {
                if (HWDecoderUtil.getAudioOutputFromDevice() == HWDecoderUtil.AudioOutput.OPENSLES) {
                    arrayList.add("--aout=opensles");
                } else {
                    arrayList.add("--aout=android_audiotrack");
                }
            }
            if (z11) {
                arrayList.add("--android-display-chroma");
                arrayList.add("RV16");
            }
        }
        nativeNew((String[]) arrayList.toArray(new String[arrayList.size()]), context.getDir("vlc", 0).getAbsolutePath());
    }

    public static synchronized void loadLibraries() {
        synchronized (LibVLC.class) {
            if (sLoaded) {
                return;
            }
            sLoaded = true;
            try {
                try {
                    System.loadLibrary("vlcffmpeg");
                    System.loadLibrary("vlc");
                    System.loadLibrary("vlcjni");
                } catch (UnsatisfiedLinkError e10) {
                    Log.e(TAG, "Can't load vlcjni library: " + e10);
                    System.exit(1);
                }
            } catch (SecurityException e11) {
                Log.e(TAG, "Encountered a security issue when loading vlcjni library: " + e11);
                System.exit(1);
            }
        }
    }

    private native void nativeNew(String[] strArr, String str);

    private native void nativeRelease();

    private native void nativeSetUserAgent(String str, String str2);

    public native String changeset();

    public native String compiler();

    @Override // com.mrzero.tranplayer.VLCObject
    public /* bridge */ /* synthetic */ boolean isReleased() {
        return super.isReleased();
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.mrzero.tranplayer.VLCObject
    public Event onEventNative(int i10, long j10, long j11, long j12, long j13, float f10, String str) {
        return null;
    }

    @Override // com.mrzero.tranplayer.VLCObject
    public void onReleaseNative() {
        nativeRelease();
    }

    @Override // com.mrzero.tranplayer.VLCObject
    public /* bridge */ /* synthetic */ void release() {
        super.release();
    }

    @Override // com.mrzero.tranplayer.VLCObject
    public synchronized /* bridge */ /* synthetic */ boolean retain() {
        return super.retain();
    }

    @Override // com.mrzero.tranplayer.VLCObject
    public /* bridge */ /* synthetic */ void setPinPVideoMode(boolean z10) {
        super.setPinPVideoMode(z10);
    }

    public void setUserAgent(String str, String str2) {
        nativeSetUserAgent(str, str2);
    }

    public native String version();

    public LibVLC(Context context) {
        this(context, null);
    }
}
