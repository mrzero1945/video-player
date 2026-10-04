package com.mrzero.tranplayer;

import android.graphics.Point;
import com.mrzero.tranplayer.MediaPlayer;

/* loaded from: classes3.dex */
public class GLRenderer {
    private MediaPlayer.SurfaceListener mListener;
    private int mLastTextureId = 0;
    private boolean mFrameUpdated = false;
    private boolean mSurfaceCreated = false;
    private long mInstance = 0;

    public GLRenderer(int i10, MediaPlayer mediaPlayer, MediaPlayer.SurfaceListener surfaceListener) {
        if (surfaceListener == null) {
            throw new IllegalArgumentException("listener can't be null");
        }
        this.mListener = surfaceListener;
        nativeInit(mediaPlayer, i10);
    }

    private native int nativeGetVideoTexture(Point point);

    private native void nativeInit(MediaPlayer mediaPlayer, int i10);

    private native void nativeOnSurfaceCreated();

    private native void nativeOnSurfaceDestroyed();

    private native void nativeRelease();

    public int getVideoTexture(Point point) {
        int nativeGetVideoTexture = nativeGetVideoTexture(point);
        if (nativeGetVideoTexture == this.mLastTextureId) {
            this.mLastTextureId = nativeGetVideoTexture;
            this.mFrameUpdated = false;
        } else if (nativeGetVideoTexture != 0) {
            this.mFrameUpdated = true;
        }
        return nativeGetVideoTexture;
    }

    public synchronized boolean isValid() {
        return this.mSurfaceCreated;
    }

    public boolean isVideoFrameUpdated() {
        return this.mFrameUpdated;
    }

    public synchronized void onSurfaceCreated() {
        nativeOnSurfaceCreated();
        if (!this.mSurfaceCreated) {
            this.mSurfaceCreated = true;
            this.mListener.onSurfaceCreated();
        }
    }

    public synchronized void onSurfaceDestroyed() {
        if (this.mSurfaceCreated) {
            this.mSurfaceCreated = false;
            this.mListener.onSurfaceDestroyed();
            nativeOnSurfaceDestroyed();
        }
    }

    public void release() {
        nativeRelease();
    }
}
