package com.mrzero.tranplayer;

import android.annotation.TargetApi;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import androidx.annotation.MainThread;
import com.mrzero.tranplayer.IVLCVout;
import com.mrzero.tranplayer.MediaPlayer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/* loaded from: classes3.dex */
public class AWindow implements IVLCVout {
    private static final int AWINDOW_REGISTER_ERROR = 0;
    private static final int AWINDOW_REGISTER_FLAGS_HAS_VIDEO_LAYOUT_LISTENER = 2;
    private static final int AWINDOW_REGISTER_FLAGS_SUCCESS = 1;
    private static final int ID_MAX = 2;
    private static final int ID_SUBTITLES = 1;
    private static final int ID_VIDEO = 0;
    private static final int SURFACE_STATE_ATTACHED = 1;
    private static final int SURFACE_STATE_INIT = 0;
    private static final int SURFACE_STATE_READY = 2;
    private static final String TAG = "AWindow";
    private final NativeLock mNativeLock;
    private final MediaPlayer.SurfaceListener mSurfaceCallback;
    private SurfaceTextureThread mSurfaceTextureThread;
    private final AtomicInteger mSurfacesState = new AtomicInteger(0);
    private IVLCVout.OnNewVideoLayoutListener mOnNewVideoLayoutListener = null;
    private ArrayList<IVLCVout.Callback> mIVLCVoutCallbacks = new ArrayList<>();
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mUseGLRenderer = false;
    private long mCallbackNativeHandle = 0;
    private int mMouseAction = -1;
    private int mMouseButton = -1;
    private int mMouseX = -1;
    private int mMouseY = -1;
    private int mWindowWidth = -1;
    private int mWindowHeight = -1;
    private final SurfaceHelper[] mSurfaceHelpers = {null, null};
    private final Surface[] mSurfaces = {null, null};

    public static class NativeLock {
        private boolean buffersGeometryAbort;
        private boolean buffersGeometryConfigured;

        private NativeLock() {
            this.buffersGeometryConfigured = false;
            this.buffersGeometryAbort = false;
        }
    }

    @TargetApi(16)
    public static class SurfaceTextureThread implements Runnable, SurfaceTexture.OnFrameAvailableListener {
        private boolean mDoRelease;
        private boolean mFrameAvailable;
        private boolean mIsAttached;
        private Looper mLooper;
        private Surface mSurface;
        private SurfaceTexture mSurfaceTexture;
        private Thread mThread;

        /* JADX INFO: Access modifiers changed from: private */
        public synchronized boolean attachToGLContext(int i10) {
            if (this.mSurfaceTexture == null) {
                Thread thread = new Thread(this);
                this.mThread = thread;
                thread.start();
                while (this.mSurfaceTexture == null) {
                    try {
                        wait();
                    } catch (InterruptedException unused) {
                        return false;
                    }
                }
                this.mSurface = new Surface(this.mSurfaceTexture);
            }
            this.mSurfaceTexture.attachToGLContext(i10);
            this.mFrameAvailable = false;
            this.mIsAttached = true;
            return true;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public synchronized void detachFromGLContext() {
            if (this.mDoRelease) {
                this.mLooper.quit();
                this.mLooper = null;
                try {
                    this.mThread.join();
                } catch (InterruptedException unused) {
                }
                this.mThread = null;
                Surface surface = this.mSurface;
                if (surface != null) {
                    surface.release();
                    this.mSurface = null;
                }
                SurfaceTexture surfaceTexture = this.mSurfaceTexture;
                if (surfaceTexture != null) {
                    surfaceTexture.release();
                    this.mSurfaceTexture = null;
                }
                this.mDoRelease = false;
            } else {
                this.mSurfaceTexture.detachFromGLContext();
            }
            this.mIsAttached = false;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public synchronized Surface getSurface() {
            return this.mSurface;
        }

        /* JADX INFO: Access modifiers changed from: private */
        public synchronized void release() {
            if (this.mSurfaceTexture != null) {
                if (this.mIsAttached) {
                    this.mDoRelease = true;
                } else {
                    Surface surface = this.mSurface;
                    if (surface != null) {
                        surface.release();
                        this.mSurface = null;
                    }
                    this.mSurfaceTexture.release();
                    this.mSurfaceTexture = null;
                }
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public boolean waitAndUpdateTexImage(float[] fArr) {
            synchronized (this) {
                while (!this.mFrameAvailable) {
                    try {
                        wait(500L);
                    } catch (InterruptedException unused) {
                    }
                    if (!this.mFrameAvailable) {
                        return false;
                    }
                }
                this.mFrameAvailable = false;
                this.mSurfaceTexture.updateTexImage();
                this.mSurfaceTexture.getTransformMatrix(fArr);
                return true;
            }
        }

        @Override // android.graphics.SurfaceTexture.OnFrameAvailableListener
        public synchronized void onFrameAvailable(SurfaceTexture surfaceTexture) {
            if (surfaceTexture == this.mSurfaceTexture) {
                if (this.mFrameAvailable) {
                    throw new IllegalStateException("An available frame was not updated");
                }
                this.mFrameAvailable = true;
                notify();
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            Looper.prepare();
            synchronized (this) {
                this.mLooper = Looper.myLooper();
                SurfaceTexture surfaceTexture = new SurfaceTexture(0);
                this.mSurfaceTexture = surfaceTexture;
                surfaceTexture.detachFromGLContext();
                this.mSurfaceTexture.setOnFrameAvailableListener(this);
                notify();
            }
            Looper.loop();
        }

        private SurfaceTextureThread() {
            this.mSurfaceTexture = null;
            this.mSurface = null;
            this.mFrameAvailable = false;
            this.mLooper = null;
            this.mThread = null;
            this.mIsAttached = false;
            this.mDoRelease = false;
        }
    }

    public AWindow(MediaPlayer.SurfaceListener surfaceListener) {
        this.mSurfaceTextureThread = new SurfaceTextureThread();
        this.mNativeLock = new NativeLock();
        this.mSurfaceCallback = surfaceListener;
    }

    private void SurfaceTexture_detachFromGLContext() {
        this.mSurfaceTextureThread.detachFromGLContext();
    }

    private Surface SurfaceTexture_getSurface() {
        return this.mSurfaceTextureThread.getSurface();
    }

    private boolean SurfaceTexture_waitAndUpdateTexImage(float[] fArr) {
        return this.mSurfaceTextureThread.waitAndUpdateTexImage(fArr);
    }

    private void ensureInitState() throws IllegalStateException {
        if (this.mUseGLRenderer) {
            throw new IllegalStateException("Can't set view when using GL Renderer.");
        }
        if (this.mSurfacesState.get() == 0) {
            return;
        }
        throw new IllegalStateException("Can't set view when already attached. Current state: " + this.mSurfacesState.get() + ", mSurfaces[ID_VIDEO]: " + this.mSurfaceHelpers[0] + " / " + this.mSurfaces[0] + ", mSurfaces[ID_SUBTITLES]: " + this.mSurfaceHelpers[1] + " / " + this.mSurfaces[1]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Surface getNativeSurface(int i10) {
        Surface surface;
        synchronized (this.mNativeLock) {
            surface = this.mSurfaces[i10];
        }
        return surface;
    }

    private Surface getSubtitlesSurface() {
        return getNativeSurface(1);
    }

    private Surface getVideoSurface() {
        return getNativeSurface(0);
    }

    private static native void nativeOnMouseEvent(long j10, int i10, int i11, int i12, int i13);

    private static native void nativeOnWindowSize(long j10, int i10, int i11);

    /* JADX INFO: Access modifiers changed from: private */
    @MainThread
    public void onSurfaceCreated() {
        if (this.mSurfacesState.get() != 1) {
            throw new IllegalArgumentException("invalid state");
        }
        SurfaceHelper[] surfaceHelperArr = this.mSurfaceHelpers;
        SurfaceHelper surfaceHelper = surfaceHelperArr[0];
        SurfaceHelper surfaceHelper2 = surfaceHelperArr[1];
        Objects.requireNonNull(surfaceHelper, "videoHelper shouldn't be null here");
        if (surfaceHelper.isReady()) {
            if (surfaceHelper2 == null || surfaceHelper2.isReady()) {
                this.mSurfacesState.set(2);
                Iterator<IVLCVout.Callback> it = this.mIVLCVoutCallbacks.iterator();
                while (it.hasNext()) {
                    it.next().onSurfacesCreated(this);
                }
                MediaPlayer.SurfaceListener surfaceListener = this.mSurfaceCallback;
                if (surfaceListener != null) {
                    surfaceListener.onSurfaceCreated();
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    @MainThread
    public void onSurfaceDestroyed() {
        detachViews();
    }

    private int registerNative(long j10) {
        int i10;
        if (j10 == 0) {
            throw new IllegalArgumentException("nativeHandle is null");
        }
        synchronized (this.mNativeLock) {
            if (this.mCallbackNativeHandle != 0) {
                return 0;
            }
            this.mCallbackNativeHandle = j10;
            int i11 = this.mMouseAction;
            if (i11 != -1) {
                nativeOnMouseEvent(j10, i11, this.mMouseButton, this.mMouseX, this.mMouseY);
            }
            int i12 = this.mWindowWidth;
            if (i12 != -1 && (i10 = this.mWindowHeight) != -1) {
                nativeOnWindowSize(this.mCallbackNativeHandle, i12, i10);
            }
            return this.mOnNewVideoLayoutListener != null ? 3 : 1;
        }
    }

    private boolean setBuffersGeometry(Surface surface, int i10, int i11, int i12) {
        return false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setNativeSurface(int i10, Surface surface) {
        synchronized (this.mNativeLock) {
            this.mSurfaces[i10] = surface;
        }
    }

    private void setSurface(int i10, Surface surface, SurfaceHolder surfaceHolder) {
        ensureInitState();
        if (!surface.isValid() && surfaceHolder == null) {
            throw new IllegalStateException("surface is not attached and holder is null");
        }
        SurfaceHelper surfaceHelper = this.mSurfaceHelpers[i10];
        if (surfaceHelper != null) {
            surfaceHelper.release();
        }
        this.mSurfaceHelpers[i10] = new SurfaceHelper(i10, surface, surfaceHolder);
    }

    private void setVideoLayout(final int i10, final int i11, final int i12, final int i13, final int i14, final int i15) {
        this.mHandler.post(new Runnable() { // from class: com.mrzero.tranplayer.AWindow.1
            @Override // java.lang.Runnable
            public void run() {
                if (AWindow.this.mOnNewVideoLayoutListener != null) {
                    AWindow.this.mOnNewVideoLayoutListener.onNewVideoLayout(AWindow.this, i10, i11, i12, i13, i14, i15);
                }
            }
        });
    }

    private void setView(int i10, SurfaceView surfaceView) {
        ensureInitState();
        Objects.requireNonNull(surfaceView, "view is null");
        SurfaceHelper surfaceHelper = this.mSurfaceHelpers[i10];
        if (surfaceHelper != null) {
            surfaceHelper.release();
        }
        this.mSurfaceHelpers[i10] = new SurfaceHelper(i10, surfaceView);
    }

    private void unregisterNative() {
        synchronized (this.mNativeLock) {
            if (this.mCallbackNativeHandle == 0) {
                throw new IllegalArgumentException("unregister called when not registered");
            }
            this.mCallbackNativeHandle = 0L;
        }
    }

    public boolean SurfaceTexture_attachToGLContext(int i10) {
        return this.mSurfaceTextureThread.attachToGLContext(i10);
    }

    @Override // com.mrzero.tranplayer.IVLCVout
    public void addCallback(IVLCVout.Callback callback) {
        if (this.mIVLCVoutCallbacks.contains(callback)) {
            return;
        }
        this.mIVLCVoutCallbacks.add(callback);
    }

    public boolean areSurfacesWaiting() {
        return this.mSurfacesState.get() == 1;
    }

    @Override // com.mrzero.tranplayer.IVLCVout
    @MainThread
    public boolean areViewsAttached() {
        return this.mSurfacesState.get() != 0;
    }

    @Override // com.mrzero.tranplayer.IVLCVout
    @MainThread
    public void attachViews(IVLCVout.OnNewVideoLayoutListener onNewVideoLayoutListener) {
        if (this.mSurfacesState.get() == 0) {
            if (this.mSurfaceHelpers[0] != null) {
                this.mSurfacesState.set(1);
                synchronized (this.mNativeLock) {
                    this.mOnNewVideoLayoutListener = onNewVideoLayoutListener;
                    this.mNativeLock.buffersGeometryConfigured = false;
                    this.mNativeLock.buffersGeometryAbort = false;
                }
                for (int i10 = 0; i10 < 2; i10++) {
                    SurfaceHelper surfaceHelper = this.mSurfaceHelpers[i10];
                    if (surfaceHelper != null) {
                        surfaceHelper.attach();
                    }
                }
                return;
            }
        }
        throw new IllegalStateException("already attached or video view not configured");
    }

    @Override // com.mrzero.tranplayer.IVLCVout
    @MainThread
    public synchronized void detachViews() {
        if (this.mSurfacesState.get() == 0) {
            return;
        }
        this.mSurfacesState.set(0);
        this.mHandler.removeCallbacksAndMessages(null);
        synchronized (this.mNativeLock) {
            this.mOnNewVideoLayoutListener = null;
            this.mNativeLock.buffersGeometryAbort = true;
            this.mNativeLock.notifyAll();
        }
        for (int i10 = 0; i10 < 2; i10++) {
            SurfaceHelper surfaceHelper = this.mSurfaceHelpers[i10];
            if (surfaceHelper != null) {
                surfaceHelper.release();
            }
            this.mSurfaceHelpers[i10] = null;
        }
        Iterator<IVLCVout.Callback> it = this.mIVLCVoutCallbacks.iterator();
        while (it.hasNext()) {
            it.next().onSurfacesDestroyed(this);
        }
        MediaPlayer.SurfaceListener surfaceListener = this.mSurfaceCallback;
        if (surfaceListener != null) {
            surfaceListener.onSurfaceDestroyed();
        }
        this.mSurfaceTextureThread.release();
    }

    @Override // com.mrzero.tranplayer.IVLCVout
    public void removeCallback(IVLCVout.Callback callback) {
        this.mIVLCVoutCallbacks.remove(callback);
    }

    @Override // com.mrzero.tranplayer.IVLCVout
    public void sendMouseEvent(int i10, int i11, int i12, int i13) {
        synchronized (this.mNativeLock) {
            long j10 = this.mCallbackNativeHandle;
            if (j10 != 0 && (this.mMouseAction != i10 || this.mMouseButton != i11 || this.mMouseX != i12 || this.mMouseY != i13)) {
                nativeOnMouseEvent(j10, i10, i11, i12, i13);
            }
            this.mMouseAction = i10;
            this.mMouseButton = i11;
            this.mMouseX = i12;
            this.mMouseY = i13;
        }
    }

    @Override // com.mrzero.tranplayer.IVLCVout
    public void setSubtitlesSurface(Surface surface, SurfaceHolder surfaceHolder) {
        setSurface(1, surface, surfaceHolder);
    }

    @Override // com.mrzero.tranplayer.IVLCVout
    @MainThread
    public void setSubtitlesView(SurfaceView surfaceView) {
        setView(1, surfaceView);
    }

    @Override // com.mrzero.tranplayer.IVLCVout
    public void setVideoSurface(Surface surface, SurfaceHolder surfaceHolder) {
        setSurface(0, surface, surfaceHolder);
    }

    @Override // com.mrzero.tranplayer.IVLCVout
    @MainThread
    public void setVideoView(SurfaceView surfaceView) {
        setView(0, surfaceView);
    }

    @Override // com.mrzero.tranplayer.IVLCVout
    public void setWindowSize(int i10, int i11) {
        synchronized (this.mNativeLock) {
            long j10 = this.mCallbackNativeHandle;
            if (j10 != 0 && (this.mWindowWidth != i10 || this.mWindowHeight != i11)) {
                nativeOnWindowSize(j10, i10, i11);
            }
            this.mWindowWidth = i10;
            this.mWindowHeight = i11;
        }
    }

    @MainThread
    public void useGLRenderer() {
        this.mUseGLRenderer = true;
    }

    @Override // com.mrzero.tranplayer.IVLCVout
    @TargetApi(14)
    public void setSubtitlesSurface(SurfaceTexture surfaceTexture) {
        setSurface(1, new Surface(surfaceTexture), null);
    }

    @Override // com.mrzero.tranplayer.IVLCVout
    @MainThread
    public void setSubtitlesView(TextureView textureView) {
        setView(1, textureView);
    }

    @Override // com.mrzero.tranplayer.IVLCVout
    @TargetApi(14)
    public void setVideoSurface(SurfaceTexture surfaceTexture) {
        setSurface(0, new Surface(surfaceTexture), null);
    }

    @Override // com.mrzero.tranplayer.IVLCVout
    @MainThread
    public void setVideoView(TextureView textureView) {
        setView(0, textureView);
    }

    public class SurfaceHelper {
        private final int mId;
        private Surface mSurface;
        private final SurfaceHolder mSurfaceHolder;
        private final SurfaceHolder.Callback mSurfaceHolderCallback;
        private final TextureView.SurfaceTextureListener mSurfaceTextureListener;
        private final SurfaceView mSurfaceView;
        private final TextureView mTextureView;

        private void attachSurface() {
            SurfaceHolder surfaceHolder = this.mSurfaceHolder;
            if (surfaceHolder != null) {
                surfaceHolder.addCallback(this.mSurfaceHolderCallback);
            }
            setSurface(this.mSurface);
        }

        private void attachSurfaceView() {
            this.mSurfaceHolder.addCallback(this.mSurfaceHolderCallback);
            setSurface(this.mSurfaceHolder.getSurface());
        }

        @TargetApi(14)
        private void attachTextureView() {
            this.mTextureView.setSurfaceTextureListener(this.mSurfaceTextureListener);
            setSurface(new Surface(this.mTextureView.getSurfaceTexture()));
        }

        @TargetApi(14)
        private TextureView.SurfaceTextureListener createSurfaceTextureListener() {
            return new TextureView.SurfaceTextureListener() { // from class: com.mrzero.tranplayer.AWindow.SurfaceHelper.2
                @Override // android.view.TextureView.SurfaceTextureListener
                public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i10, int i11) {
                    SurfaceHelper.this.setSurface(new Surface(surfaceTexture));
                }

                @Override // android.view.TextureView.SurfaceTextureListener
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                    AWindow.this.onSurfaceDestroyed();
                    return true;
                }

                @Override // android.view.TextureView.SurfaceTextureListener
                public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i10, int i11) {
                }

                @Override // android.view.TextureView.SurfaceTextureListener
                public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
                }
            };
        }

        @TargetApi(14)
        private void releaseTextureView() {
            TextureView textureView = this.mTextureView;
            if (textureView != null) {
                textureView.setSurfaceTextureListener(null);
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void setSurface(Surface surface) {
            if (surface.isValid() && AWindow.this.getNativeSurface(this.mId) == null) {
                this.mSurface = surface;
                AWindow.this.setNativeSurface(this.mId, surface);
                AWindow.this.onSurfaceCreated();
            }
        }

        public void attach() {
            if (this.mSurfaceView != null) {
                attachSurfaceView();
            } else if (this.mTextureView != null) {
                attachTextureView();
            } else {
                if (this.mSurface == null) {
                    throw new IllegalStateException();
                }
                attachSurface();
            }
        }

        public Surface getSurface() {
            return this.mSurface;
        }

        public SurfaceHolder getSurfaceHolder() {
            return this.mSurfaceHolder;
        }

        public boolean isReady() {
            return this.mSurfaceView == null || this.mSurface != null;
        }

        public void release() {
            this.mSurface = null;
            AWindow.this.setNativeSurface(this.mId, null);
            SurfaceHolder surfaceHolder = this.mSurfaceHolder;
            if (surfaceHolder != null) {
                surfaceHolder.removeCallback(this.mSurfaceHolderCallback);
            }
            releaseTextureView();
        }

        private SurfaceHelper(int i10, SurfaceView surfaceView) {
            this.mSurfaceHolderCallback = new SurfaceHolder.Callback() { // from class: com.mrzero.tranplayer.AWindow.SurfaceHelper.1
                @Override // android.view.SurfaceHolder.Callback
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i11, int i12, int i13) {
                }

                @Override // android.view.SurfaceHolder.Callback
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    if (surfaceHolder != SurfaceHelper.this.mSurfaceHolder) {
                        throw new IllegalStateException("holders are different");
                    }
                    SurfaceHelper.this.setSurface(surfaceHolder.getSurface());
                }

                @Override // android.view.SurfaceHolder.Callback
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    Log.d(AWindow.TAG, "surfaceDestroyed called");
                    AWindow.this.onSurfaceDestroyed();
                }
            };
            this.mSurfaceTextureListener = createSurfaceTextureListener();
            this.mId = i10;
            this.mTextureView = null;
            this.mSurfaceView = surfaceView;
            this.mSurfaceHolder = surfaceView.getHolder();
        }

        private SurfaceHelper(int i10, TextureView textureView) {
            this.mSurfaceHolderCallback = new SurfaceHolder.Callback() { // from class: com.mrzero.tranplayer.AWindow.SurfaceHelper.1
                @Override // android.view.SurfaceHolder.Callback
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i11, int i12, int i13) {
                }

                @Override // android.view.SurfaceHolder.Callback
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    if (surfaceHolder != SurfaceHelper.this.mSurfaceHolder) {
                        throw new IllegalStateException("holders are different");
                    }
                    SurfaceHelper.this.setSurface(surfaceHolder.getSurface());
                }

                @Override // android.view.SurfaceHolder.Callback
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    Log.d(AWindow.TAG, "surfaceDestroyed called");
                    AWindow.this.onSurfaceDestroyed();
                }
            };
            this.mSurfaceTextureListener = createSurfaceTextureListener();
            this.mId = i10;
            this.mSurfaceView = null;
            this.mSurfaceHolder = null;
            this.mTextureView = textureView;
        }

        private SurfaceHelper(int i10, Surface surface, SurfaceHolder surfaceHolder) {
            this.mSurfaceHolderCallback = new SurfaceHolder.Callback() { // from class: com.mrzero.tranplayer.AWindow.SurfaceHelper.1
                @Override // android.view.SurfaceHolder.Callback
                public void surfaceChanged(SurfaceHolder surfaceHolder2, int i11, int i12, int i13) {
                }

                @Override // android.view.SurfaceHolder.Callback
                public void surfaceCreated(SurfaceHolder surfaceHolder2) {
                    if (surfaceHolder2 != SurfaceHelper.this.mSurfaceHolder) {
                        throw new IllegalStateException("holders are different");
                    }
                    SurfaceHelper.this.setSurface(surfaceHolder2.getSurface());
                }

                @Override // android.view.SurfaceHolder.Callback
                public void surfaceDestroyed(SurfaceHolder surfaceHolder2) {
                    Log.d(AWindow.TAG, "surfaceDestroyed called");
                    AWindow.this.onSurfaceDestroyed();
                }
            };
            this.mSurfaceTextureListener = createSurfaceTextureListener();
            this.mId = i10;
            this.mSurfaceView = null;
            this.mTextureView = null;
            this.mSurfaceHolder = surfaceHolder;
            this.mSurface = surface;
        }
    }

    private void setView(int i10, TextureView textureView) {
        ensureInitState();
        Objects.requireNonNull(textureView, "view is null");
        SurfaceHelper surfaceHelper = this.mSurfaceHelpers[i10];
        if (surfaceHelper != null) {
            surfaceHelper.release();
        }
        this.mSurfaceHelpers[i10] = new SurfaceHelper(i10, textureView);
    }

    @Override // com.mrzero.tranplayer.IVLCVout
    @MainThread
    public void attachViews() {
        attachViews(null);
    }
}
