package com.mrzero.tranplayer;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import androidx.annotation.MainThread;

/* loaded from: classes3.dex */
public interface IVLCVout {

    public interface Callback {
        @MainThread
        void onSurfacesCreated(IVLCVout iVLCVout);

        @MainThread
        void onSurfacesDestroyed(IVLCVout iVLCVout);
    }

    public interface OnNewVideoLayoutListener {
        @MainThread
        void onNewVideoLayout(IVLCVout iVLCVout, int i10, int i11, int i12, int i13, int i14, int i15);
    }

    @MainThread
    void addCallback(Callback callback);

    @MainThread
    boolean areViewsAttached();

    @MainThread
    void attachViews();

    @MainThread
    void attachViews(OnNewVideoLayoutListener onNewVideoLayoutListener);

    @MainThread
    void detachViews();

    @MainThread
    void removeCallback(Callback callback);

    @MainThread
    void sendMouseEvent(int i10, int i11, int i12, int i13);

    @TargetApi(14)
    @MainThread
    void setSubtitlesSurface(SurfaceTexture surfaceTexture);

    @MainThread
    void setSubtitlesSurface(Surface surface, SurfaceHolder surfaceHolder);

    @MainThread
    void setSubtitlesView(SurfaceView surfaceView);

    @TargetApi(14)
    @MainThread
    void setSubtitlesView(TextureView textureView);

    @TargetApi(14)
    @MainThread
    void setVideoSurface(SurfaceTexture surfaceTexture);

    @MainThread
    void setVideoSurface(Surface surface, SurfaceHolder surfaceHolder);

    @MainThread
    void setVideoView(SurfaceView surfaceView);

    @TargetApi(14)
    @MainThread
    void setVideoView(TextureView textureView);

    @MainThread
    void setWindowSize(int i10, int i11);
}
