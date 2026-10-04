package com.mrzero.tranplayer;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioDeviceCallback;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.util.SparseArray;
import com.mrzero.tranplayer.Media;
import com.mrzero.tranplayer.VLCEvent;
import com.mrzero.tranplayer.util.AndroidUtil;
import com.mrzero.tranplayer.util.VLCUtil;
import java.io.File;

/* loaded from: classes3.dex */
public class MediaPlayer extends VLCObject<MediaPlayer.Event> {
    private final AudioDeviceCallback mAudioDeviceCallback;
    private boolean mAudioDigitalOutputEnabled;
    private String mAudioOutput;
    private String mAudioOutputDevice;
    private String mAudioPlugOutputDevice;
    private final BroadcastReceiver mAudioPlugReceiver;
    private boolean mAudioPlugRegistered;
    private boolean mAudioReset;
    private boolean mCanDoPassthrough;
    private GLRenderer mGLRenderer;
    private boolean mListenAudioPlug;
    private Media mMedia;
    private boolean mPlayRequested;
    private boolean mPlaying;
    private final SurfaceListener mSurfaceListener;
    private int mVoutCount;
    private final AWindow mWindow;

    public static class Chapter {
        public final long duration;
        public final String name;
        public final long timeOffset;

        private Chapter(long j10, long j11, String str) {
            this.timeOffset = j10;
            this.duration = j11;
            this.name = str;
        }
    }

    public static class Event extends VLCEvent {
        public static final int Buffering = 259;
        public static final int DisableAudio = 295;
        public static final int DisableVideo = 294;
        public static final int ESAdded = 276;
        public static final int ESDeleted = 277;
        public static final int ESSelected = 278;
        public static final int EncounteredError = 266;
        public static final int EndReached = 265;
        public static final int EofPaused = 293;
        public static final int FirstPicDisplay = 286;
        public static final int LengthChanged = 273;
        public static final int LosePicSerious = 288;
        public static final int MediaChanged = 256;
        public static final int Opening = 258;
        public static final int PausableChanged = 270;
        public static final int Paused = 261;
        public static final int Playing = 260;
        public static final int PositionChanged = 268;
        public static final int Prepared = 290;
        public static final int SeekableChanged = 269;
        public static final int Stopped = 262;
        public static final int Subtitle = 291;
        public static final int SubtitleLoad = 292;
        public static final int SubtitleType = 296;
        public static final int TimeChanged = 267;
        public static final int Unsupported = 289;
        public static final int VideoCodecDeleted = 297;
        public static final int VideoCodecInfo = 299;
        public static final int VideoFPS = 298;
        public static final int VideoSize = 287;
        public static final int Vout = 274;

        public Event(int i10) {
            super(i10);
        }

        public float getBuffering() {
            return this.argf1;
        }

        public long getCacheChanged() {
            return this.arg1;
        }

        public int getErrorCode() {
            return (int) this.arg1;
        }

        public int getEsChangedID() {
            return (int) this.arg2;
        }

        public int getEsChangedType() {
            return (int) this.arg1;
        }

        public long getLengthChanged() {
            return this.arg1;
        }

        public boolean getPausable() {
            return this.arg1 != 0;
        }

        public float getPositionChanged() {
            return this.argf1;
        }

        public boolean getSeekable() {
            return this.arg1 != 0;
        }

        public int getSubtitleDuration() {
            return (int) this.arg2;
        }

        public int getSubtitleLoadStatus() {
            return (int) this.arg1;
        }

        public int getSubtitleStart() {
            return (int) this.arg1;
        }

        public String getSubtitleText() {
            return this.argstr;
        }

        public int getSubtitleType() {
            return (int) this.arg2;
        }

        public int getSubtitleTypeId() {
            return (int) this.arg1;
        }

        public long getTimeChanged() {
            return this.arg1;
        }

        public boolean getUseMediaCodec() {
            return this.arg1 != 0;
        }

        public String getVideoCodecName() {
            return this.argstr;
        }

        public int getVideoCodecStatus() {
            return (int) this.arg1;
        }

        public float getVideoFPS() {
            return this.argf1;
        }

        public int getVideoHeight() {
            return (int) this.arg2;
        }

        public int getVideoSarDen() {
            return (int) this.arg4;
        }

        public int getVideoSarNum() {
            return (int) this.arg3;
        }

        public int getVideoWidth() {
            return (int) this.arg1;
        }

        public int getVoutCount() {
            return (int) this.arg1;
        }

        public Event(int i10, long j10) {
            super(i10, j10);
        }

        public Event(int i10, long j10, long j11) {
            super(i10, j10, j11);
        }

        public Event(int i10, long j10, long j11, long j12, long j13) {
            super(i10, j10, j11, j12, j13);
        }

        public Event(int i10, long j10, long j11, String str) {
            super(i10, j10, j11, str);
        }

        public Event(int i10, float f10) {
            super(i10, f10);
        }
    }

    public interface EventListener extends VLCEvent.Listener<Event> {
    }

    public static class Navigate {
        public static final int Activate = 0;
        public static final int Down = 2;
        public static final int Left = 3;
        public static final int Right = 4;
        public static final int Up = 1;
    }

    public static class Position {
        public static final int Bottom = 6;
        public static final int BottomLeft = 7;
        public static final int BottomRight = 8;
        public static final int Center = 0;
        public static final int Disable = -1;
        public static final int Left = 1;
        public static final int Right = 2;
        public static final int Top = 3;
        public static final int TopLeft = 4;
        public static final int TopRight = 5;
    }

    public interface SurfaceListener {
        void onSurfaceCreated();

        void onSurfaceDestroyed();
    }

    public static class Title {
        public final long duration;
        private final int flags;
        public final String name;

        public static class Flags {
            public static final int INTERACTIVE = 2;
            public static final int MENU = 1;

            private Flags() {
            }
        }

        public Title(long j10, String str, int i10) {
            this.duration = j10;
            this.name = str;
            this.flags = i10;
        }

        public boolean isInteractive() {
            return (this.flags & 2) != 0;
        }

        public boolean isMenu() {
            return (this.flags & 1) != 0;
        }
    }

    public static class TrackDescription {

        /* renamed from: id, reason: collision with root package name */
        public final int f9413id;
        public final String name;

        private TrackDescription(int i10, String str) {
            this.f9413id = i10;
            this.name = str;
        }
    }

    public MediaPlayer(LibVLC libVLC) {
        super(libVLC);
        this.mMedia = null;
        this.mPlaying = false;
        this.mPlayRequested = false;
        this.mListenAudioPlug = false;
        this.mVoutCount = 0;
        this.mAudioReset = false;
        this.mAudioOutput = "android_audiotrack";
        this.mAudioOutputDevice = null;
        this.mAudioPlugRegistered = false;
        this.mAudioDigitalOutputEnabled = false;
        this.mAudioPlugOutputDevice = "stereo";
        SurfaceListener surfaceListener = new SurfaceListener() { // from class: com.mrzero.tranplayer.MediaPlayer.1
            @Override // com.mrzero.tranplayer.MediaPlayer.SurfaceListener
            public void onSurfaceCreated() {
                boolean z10;
                boolean z11;
                synchronized (MediaPlayer.this) {
                    z10 = false;
                    if (MediaPlayer.this.mPlaying || !MediaPlayer.this.mPlayRequested) {
                        z11 = MediaPlayer.this.mVoutCount == 0;
                    } else {
                        z11 = false;
                        z10 = true;
                    }
                }
                if (z10) {
                    MediaPlayer.this.play();
                } else if (z11) {
                    MediaPlayer.this.setVideoTrackEnabled(true);
                }
            }

            @Override // com.mrzero.tranplayer.MediaPlayer.SurfaceListener
            public void onSurfaceDestroyed() {
                boolean z10;
                synchronized (MediaPlayer.this) {
                    z10 = MediaPlayer.this.mVoutCount > 0;
                }
                if (z10) {
                    MediaPlayer.this.setVideoTrackEnabled(false);
                }
            }
        };
        this.mSurfaceListener = surfaceListener;
        AWindow aWindow = new AWindow(surfaceListener);
        this.mWindow = aWindow;
        this.mGLRenderer = null;
        this.mAudioPlugReceiver = (!AndroidUtil.isLolliPopOrLater || AndroidUtil.isMarshMallowOrLater) ? null : createAudioPlugReceiver();
        this.mAudioDeviceCallback = AndroidUtil.isMarshMallowOrLater ? createAudioDeviceCallback() : null;
        nativeNewFromLibVlc(libVLC, aWindow);
    }

    @TargetApi(23)
    private AudioDeviceCallback createAudioDeviceCallback() {
        return new AudioDeviceCallback() { // from class: com.mrzero.tranplayer.MediaPlayer.3
            private SparseArray<Long> mEncodedDevices = new SparseArray<>();

            private void onAudioDevicesChanged() {
                long j10 = 0;
                for (int i10 = 0; i10 < this.mEncodedDevices.size(); i10++) {
                    j10 |= this.mEncodedDevices.valueAt(i10).longValue();
                }
                MediaPlayer.this.updateAudioOutputDevice(j10, "pcm");
            }

            @Override // android.media.AudioDeviceCallback
            public void onAudioDevicesAdded(AudioDeviceInfo[] audioDeviceInfoArr) {
                for (AudioDeviceInfo audioDeviceInfo : audioDeviceInfoArr) {
                    if (audioDeviceInfo.isSink()) {
                        long encodingFlags = MediaPlayer.this.getEncodingFlags(audioDeviceInfo.getEncodings());
                        if (encodingFlags != 0) {
                            this.mEncodedDevices.put(audioDeviceInfo.getId(), Long.valueOf(encodingFlags));
                        }
                    }
                }
                onAudioDevicesChanged();
            }

            @Override // android.media.AudioDeviceCallback
            public void onAudioDevicesRemoved(AudioDeviceInfo[] audioDeviceInfoArr) {
                for (AudioDeviceInfo audioDeviceInfo : audioDeviceInfoArr) {
                    if (audioDeviceInfo.isSink()) {
                        this.mEncodedDevices.remove(audioDeviceInfo.getId());
                    }
                }
                onAudioDevicesChanged();
            }
        };
    }

    @TargetApi(21)
    private BroadcastReceiver createAudioPlugReceiver() {
        return new BroadcastReceiver() { // from class: com.mrzero.tranplayer.MediaPlayer.2
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null && action.equalsIgnoreCase("android.media.action.HDMI_AUDIO_PLUG")) {
                    MediaPlayer.this.updateAudioOutputDevice(!(intent.getIntExtra("android.media.extra.AUDIO_PLUG_STATE", 0) == 1) ? 0L : MediaPlayer.this.getEncodingFlags(intent.getIntArrayExtra("android.media.extra.ENCODINGS")), "stereo");
                }
            }
        };
    }

    private static Chapter createChapterFromNative(long j10, long j11, String str) {
        return new Chapter(j10, j11, str);
    }

    private static Title createTitleFromNative(long j10, String str, int i10) {
        return new Title(j10, str, i10);
    }

    private static TrackDescription createTrackDescriptionFromNative(int i10, String str) {
        return new TrackDescription(i10, str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public long getEncodingFlags(int[] iArr) {
        long j10 = 0;
        if (iArr == null) {
            return 0L;
        }
        for (int i10 : iArr) {
            if (isEncoded(i10)) {
                j10 |= 1L << i10;
            }
        }
        return j10;
    }

    private boolean isAudioTrack() {
        String str = this.mAudioOutput;
        return str != null && str.equals("android_audiotrack");
    }

    private boolean isEncoded(int i10) {
        return i10 == 5 || i10 == 6 || i10 == 7 || i10 == 8 || i10 == 14;
    }

    private native boolean nativeAddSlave(int i10, String str, boolean z10);

    private native String nativeGetAspectRatio();

    private native long nativeGetAudioDelay();

    private native int nativeGetAudioTrack();

    private native TrackDescription[] nativeGetAudioTracks();

    private native int nativeGetAudioTracksCount();

    private native Chapter[] nativeGetChapters(int i10);

    private native float nativeGetScale();

    private native long nativeGetSpuDelay();

    private native int nativeGetSpuTrack();

    private native TrackDescription[] nativeGetSpuTracks();

    private native int nativeGetSpuTracksCount();

    private native Title[] nativeGetTitles();

    private native int nativeGetVideoTrack();

    private native TrackDescription[] nativeGetVideoTracks();

    private native int nativeGetVideoTracksCount();

    private native void nativeNewFromLibVlc(LibVLC libVLC, AWindow aWindow);

    private native void nativeNewFromMedia(Media media, AWindow aWindow);

    private native void nativePlay();

    private native void nativeRelease();

    private native void nativeSetAspectRatio(String str);

    private native boolean nativeSetAudioDelay(long j10);

    private native boolean nativeSetAudioOutput(String str);

    private native boolean nativeSetAudioOutputDevice(String str);

    private native boolean nativeSetAudioTrack(int i10);

    private native boolean nativeSetEqualizer(Equalizer equalizer);

    private native void nativeSetMedia(Media media);

    private native int nativeSetRenderer(RendererItem rendererItem);

    private native void nativeSetScale(float f10);

    private native boolean nativeSetSpuDelay(long j10);

    private native boolean nativeSetSpuTrack(int i10);

    private native void nativeSetVideoTitleDisplay(int i10, int i11);

    private native boolean nativeSetVideoTrack(int i10);

    private native void nativeStop();

    private native boolean nativeUpdateViewpoint(float f10, float f11, float f12, float f13, boolean z10);

    private void registerAudioPlug(boolean z10) {
        if (z10 == this.mAudioPlugRegistered) {
            return;
        }
        if (this.mAudioDeviceCallback != null) {
            registerAudioPlugV23(z10);
        } else if (this.mAudioPlugReceiver != null) {
            registerAudioPlugV21(z10);
        }
        this.mAudioPlugRegistered = z10;
    }

    @TargetApi(21)
    private void registerAudioPlugV21(boolean z10) {
        if (!z10) {
            this.mLibVLC.mAppContext.unregisterReceiver(this.mAudioPlugReceiver);
            return;
        }
        Intent registerReceiver = this.mLibVLC.mAppContext.registerReceiver(this.mAudioPlugReceiver, new IntentFilter("android.media.action.HDMI_AUDIO_PLUG"));
        if (registerReceiver != null) {
            this.mAudioPlugReceiver.onReceive(this.mLibVLC.mAppContext, registerReceiver);
        }
    }

    @TargetApi(23)
    private void registerAudioPlugV23(boolean z10) {
        AudioManager audioManager = (AudioManager) this.mLibVLC.mAppContext.getSystemService("audio");
        if (!z10) {
            audioManager.unregisterAudioDeviceCallback(this.mAudioDeviceCallback);
        } else {
            this.mAudioDeviceCallback.onAudioDevicesAdded(audioManager.getDevices(2));
            audioManager.registerAudioDeviceCallback(this.mAudioDeviceCallback, null);
        }
    }

    private synchronized boolean setAudioOutputDeviceInternal(String str, boolean z10) {
        boolean nativeSetAudioOutputDevice;
        this.mAudioOutputDevice = str;
        if (z10) {
            boolean z11 = str == null && isAudioTrack();
            this.mListenAudioPlug = z11;
            if (!z11) {
                registerAudioPlug(false);
            }
        }
        nativeSetAudioOutputDevice = nativeSetAudioOutputDevice(str);
        if (!nativeSetAudioOutputDevice) {
            this.mAudioOutputDevice = null;
            this.mListenAudioPlug = false;
        }
        if (this.mListenAudioPlug) {
            registerAudioPlug(true);
        }
        return nativeSetAudioOutputDevice;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public synchronized void updateAudioOutputDevice(long j10, String str) {
        boolean z10 = j10 != 0;
        this.mCanDoPassthrough = z10;
        if (this.mAudioDigitalOutputEnabled && z10) {
            str = "encoded:" + j10;
        }
        if (!str.equals(this.mAudioPlugOutputDevice)) {
            this.mAudioPlugOutputDevice = str;
            setAudioOutputDeviceInternal(str, false);
        }
    }

    public boolean addSlave(int i10, Uri uri, boolean z10) {
        return nativeAddSlave(i10, VLCUtil.encodeVLCUri(uri), z10);
    }

    public boolean canDoPassthrough() {
        return this.mCanDoPassthrough;
    }

    public GLRenderer enableGLRenderer(int i10) {
        if (this.mWindow.areViewsAttached()) {
            throw new IllegalArgumentException("can't work with views attached to IVLCVout");
        }
        if (this.mGLRenderer != null) {
            throw new IllegalArgumentException("GLRenderer already enabled");
        }
        this.mWindow.useGLRenderer();
        GLRenderer gLRenderer = new GLRenderer(i10, this, this.mSurfaceListener);
        this.mGLRenderer = gLRenderer;
        return gLRenderer;
    }

    public synchronized boolean forceAudioDigitalEncodings(int[] iArr) {
        if (!isAudioTrack()) {
            return false;
        }
        if (iArr.length == 0) {
            setAudioOutputDeviceInternal(null, true);
        } else {
            String str = "encoded:" + getEncodingFlags(iArr);
            if (!str.equals(this.mAudioPlugOutputDevice)) {
                this.mAudioPlugOutputDevice = str;
                setAudioOutputDeviceInternal(str, true);
            }
        }
        return true;
    }

    public String getAspectRatio() {
        return nativeGetAspectRatio();
    }

    public long getAudioDelay() {
        return nativeGetAudioDelay();
    }

    public int getAudioTrack() {
        return nativeGetAudioTrack();
    }

    public TrackDescription[] getAudioTracks() {
        return nativeGetAudioTracks();
    }

    public int getAudioTracksCount() {
        return nativeGetAudioTracksCount();
    }

    public native int getChapter();

    public Chapter[] getChapters(int i10) {
        return nativeGetChapters(i10);
    }

    public Media.VideoTrack getCurrentVideoTrack() {
        if (getVideoTrack() == -1) {
            return null;
        }
        int trackCount = this.mMedia.getTrackCount();
        for (int i10 = 0; i10 < trackCount; i10++) {
            Media.Track track = this.mMedia.getTrack(i10);
            if (track.type == 1) {
                return (Media.VideoTrack) track;
            }
        }
        return null;
    }

    public native long getLength();

    public synchronized Media getMedia() {
        Media media = this.mMedia;
        if (media != null) {
            media.retain();
        }
        return this.mMedia;
    }

    public native int getPlayerState();

    public native float getPosition();

    public native float getRate();

    public float getScale() {
        return nativeGetScale();
    }

    public long getSpuDelay() {
        return nativeGetSpuDelay();
    }

    public int getSpuTrack() {
        return nativeGetSpuTrack();
    }

    public TrackDescription[] getSpuTracks() {
        return nativeGetSpuTracks();
    }

    public int getSpuTracksCount() {
        return nativeGetSpuTracksCount();
    }

    public native long getTime();

    public native int getTitle();

    public Title[] getTitles() {
        return nativeGetTitles();
    }

    public IVLCVout getVLCVout() {
        return this.mWindow;
    }

    public int getVideoTrack() {
        return nativeGetVideoTrack();
    }

    public TrackDescription[] getVideoTracks() {
        return nativeGetVideoTracks();
    }

    public int getVideoTracksCount() {
        return nativeGetVideoTracksCount();
    }

    public native int getVolume();

    public synchronized boolean hasMedia() {
        return this.mMedia != null;
    }

    public native boolean isLooping();

    public native boolean isPlaying();

    @Override // com.mrzero.tranplayer.VLCObject
    public /* bridge */ /* synthetic */ boolean isReleased() {
        return super.isReleased();
    }

    public native boolean isSeekable();

    public native void navigate(int i10);

    public native int nextChapter();

    @Override // com.mrzero.tranplayer.VLCObject
    public void onReleaseNative() {
        GLRenderer gLRenderer = this.mGLRenderer;
        if (gLRenderer != null) {
            gLRenderer.release();
        }
        this.mWindow.detachViews();
        registerAudioPlug(false);
        Media media = this.mMedia;
        if (media != null) {
            media.release();
        }
        this.mVoutCount = 0;
        nativeRelease();
    }

    public native void pause();

    public void play() {
        GLRenderer gLRenderer;
        synchronized (this) {
            if (!this.mPlaying) {
                if (this.mAudioReset) {
                    String str = this.mAudioOutput;
                    if (str != null) {
                        nativeSetAudioOutput(str);
                    }
                    String str2 = this.mAudioOutputDevice;
                    if (str2 != null) {
                        nativeSetAudioOutputDevice(str2);
                    }
                    this.mAudioReset = false;
                }
                if (this.mListenAudioPlug) {
                    registerAudioPlug(true);
                }
                this.mPlayRequested = true;
                if (this.mWindow.areSurfacesWaiting() || ((gLRenderer = this.mGLRenderer) != null && !gLRenderer.isValid())) {
                    return;
                }
            }
            this.mPlaying = true;
            nativePlay();
        }
    }

    public native int previousChapter();

    @Override // com.mrzero.tranplayer.VLCObject
    public /* bridge */ /* synthetic */ void release() {
        super.release();
    }

    public void setAspectRatio(String str) {
        nativeSetAspectRatio(str);
    }

    public boolean setAudioDelay(long j10) {
        return nativeSetAudioDelay(j10);
    }

    public synchronized boolean setAudioDigitalOutputEnabled(boolean z10) {
        if (z10 == this.mAudioDigitalOutputEnabled) {
            return true;
        }
        if (this.mListenAudioPlug && isAudioTrack()) {
            registerAudioPlug(false);
            this.mAudioDigitalOutputEnabled = z10;
            registerAudioPlug(true);
            return true;
        }
        return false;
    }

    public native boolean setAudioMute(boolean z10);

    public synchronized boolean setAudioOutput(String str) {
        boolean nativeSetAudioOutput;
        this.mAudioOutput = str;
        boolean isAudioTrack = isAudioTrack();
        this.mListenAudioPlug = isAudioTrack;
        if (!isAudioTrack) {
            registerAudioPlug(false);
        }
        nativeSetAudioOutput = nativeSetAudioOutput(str);
        if (!nativeSetAudioOutput) {
            this.mAudioOutput = null;
            this.mListenAudioPlug = false;
        }
        if (this.mListenAudioPlug) {
            registerAudioPlug(true);
        }
        return nativeSetAudioOutput;
    }

    public boolean setAudioOutputDevice(String str) {
        return setAudioOutputDeviceInternal(str, true);
    }

    public boolean setAudioTrack(int i10) {
        return nativeSetAudioTrack(i10);
    }

    public native void setChapter(int i10);

    public native boolean setDropAudioFrames(boolean z10);

    public boolean setEqualizer(Equalizer equalizer) {
        return nativeSetEqualizer(equalizer);
    }

    public synchronized void setEventListener(EventListener eventListener) {
        super.setEventListener((VLCEvent.Listener) eventListener);
    }

    public native boolean setListenVideoMode(boolean z10);

    public native boolean setLooping(boolean z10);

    public void setMedia(Media media) {
        if (media != null) {
            if (media.isReleased()) {
                throw new IllegalArgumentException("Media is released");
            }
            media.setDefaultMediaPlayerOptions();
        }
        nativeSetMedia(media);
        synchronized (this) {
            Media media2 = this.mMedia;
            if (media2 != null) {
                media2.release();
            }
            if (media != null) {
                media.retain();
            }
            this.mMedia = media;
        }
    }

    @Override // com.mrzero.tranplayer.VLCObject
    public void setPinPVideoMode(boolean z10) {
        super.setPinPVideoMode(z10);
    }

    public native void setPosition(float f10, boolean z10);

    public native void setRate(float f10);

    public native boolean setRefreshFirstFrame(boolean z10);

    public int setRenderer(RendererItem rendererItem) {
        return nativeSetRenderer(rendererItem);
    }

    public void setScale(float f10) {
        nativeSetScale(f10);
    }

    public boolean setSpuDelay(long j10) {
        return nativeSetSpuDelay(j10);
    }

    public boolean setSpuTrack(int i10) {
        return nativeSetSpuTrack(i10);
    }

    public native boolean setSurface(boolean z10);

    public native boolean setSurfaceDestroyed(boolean z10);

    public native long setTime(long j10, boolean z10);

    public native void setTitle(int i10);

    public void setVideoTitleDisplay(int i10, int i11) {
        nativeSetVideoTitleDisplay(i10, i11);
    }

    public boolean setVideoTrack(int i10) {
        GLRenderer gLRenderer;
        if (i10 == -1 || ((this.mWindow.areViewsAttached() && !this.mWindow.areSurfacesWaiting()) || ((gLRenderer = this.mGLRenderer) != null && gLRenderer.isValid()))) {
            return nativeSetVideoTrack(i10);
        }
        return false;
    }

    public void setVideoTrackEnabled(boolean z10) {
        TrackDescription[] videoTracks;
        if (!z10) {
            setVideoTrack(-1);
            return;
        }
        if (getVideoTrack() != -1 || (videoTracks = getVideoTracks()) == null) {
            return;
        }
        for (TrackDescription trackDescription : videoTracks) {
            int i10 = trackDescription.f9413id;
            if (i10 != -1) {
                setVideoTrack(i10);
                return;
            }
        }
    }

    public native int setVolume(int i10);

    public void stop() {
        synchronized (this) {
            this.mPlayRequested = false;
            this.mPlaying = false;
            this.mAudioReset = true;
        }
        nativeStop();
    }

    public boolean updateViewpoint(float f10, float f11, float f12, float f13, boolean z10) {
        return nativeUpdateViewpoint(f10, f11, f12, f13, z10);
    }

    public static class Equalizer {
        private long mInstance;

        private Equalizer() {
            nativeNew();
        }

        public static Equalizer create() {
            return new Equalizer();
        }

        public static Equalizer createFromPreset(int i10) {
            return new Equalizer(i10);
        }

        public static int getBandCount() {
            return nativeGetBandCount();
        }

        public static float getBandFrequency(int i10) {
            return nativeGetBandFrequency(i10);
        }

        public static int getPresetCount() {
            return nativeGetPresetCount();
        }

        public static String getPresetName(int i10) {
            return nativeGetPresetName(i10);
        }

        private native float nativeGetAmp(int i10);

        private static native int nativeGetBandCount();

        private static native float nativeGetBandFrequency(int i10);

        private native float nativeGetPreAmp();

        private static native int nativeGetPresetCount();

        private static native String nativeGetPresetName(int i10);

        private native void nativeNew();

        private native void nativeNewFromPreset(int i10);

        private native void nativeRelease();

        private native boolean nativeSetAmp(int i10, float f10);

        private native boolean nativeSetPreAmp(float f10);

        public void finalize() throws Throwable {
            try {
                nativeRelease();
            } finally {
                super.finalize();
            }
        }

        public float getAmp(int i10) {
            return nativeGetAmp(i10);
        }

        public float getPreAmp() {
            return nativeGetPreAmp();
        }

        public boolean setAmp(int i10, float f10) {
            return nativeSetAmp(i10, f10);
        }

        public boolean setPreAmp(float f10) {
            return nativeSetPreAmp(f10);
        }

        private Equalizer(int i10) {
            nativeNewFromPreset(i10);
        }
    }

    public boolean addSlave(int i10, String str, boolean z10) {
        return addSlave(i10, Uri.fromFile(new File(str)), z10);
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.mrzero.tranplayer.VLCObject
    public synchronized Event onEventNative(int i10, long j10, long j11, long j12, long j13, float f10, String str) {
        switch (i10) {
            case 256:
            case Event.Stopped /* 262 */:
            case Event.EndReached /* 265 */:
                this.mVoutCount = 0;
                notify();
                return new Event(i10);
            case 257:
            case 263:
            case 264:
            case 271:
            case 272:
            case 275:
            case 279:
            case 280:
            case 281:
            case 282 /* 282 */:
            case 283:
            case 284:
            case 285:
            default:
                return null;
            case Event.Opening /* 258 */:
            case Event.Buffering /* 259 */:
                return new Event(i10, f10);
            case Event.Playing /* 260 */:
            case Event.Paused /* 261 */:
            case Event.Prepared /* 290 */:
                return new Event(i10);
            case Event.EncounteredError /* 266 */:
                this.mVoutCount = 0;
                notify();
                return new Event(i10, j10);
            case Event.TimeChanged /* 267 */:
                return new Event(i10, j10);
            case Event.PositionChanged /* 268 */:
                return new Event(i10, f10);
            case Event.SeekableChanged /* 269 */:
            case Event.PausableChanged /* 270 */:
                return new Event(i10, j10);
            case 273:
                return new Event(i10, j10);
            case Event.Vout /* 274 */:
                this.mVoutCount = (int) j10;
                notify();
                return new Event(i10, j10);
            case Event.ESAdded /* 276 */:
            case Event.ESDeleted /* 277 */:
            case Event.ESSelected /* 278 */:
                return new Event(i10, j10, j11);
            case Event.FirstPicDisplay /* 286 */:
                return new Event(i10);
            case Event.VideoSize /* 287 */:
                return new Event(i10, j10, j11, j12, j13);
            case Event.LosePicSerious /* 288 */:
                return new Event(i10);
            case Event.Unsupported /* 289 */:
                return new Event(i10);
            case Event.Subtitle /* 291 */:
                return new Event(i10, j10, j11, str);
            case Event.SubtitleLoad /* 292 */:
                return new Event(i10, j10);
            case Event.EofPaused /* 293 */:
                return new Event(i10);
            case Event.DisableVideo /* 294 */:
                return new Event(i10);
            case Event.DisableAudio /* 295 */:
                return new Event(i10);
            case Event.SubtitleType /* 296 */:
                return new Event(i10, j10, j11);
            case Event.VideoCodecDeleted /* 297 */:
                return new Event(i10, j10);
            case Event.VideoFPS /* 298 */:
                return new Event(i10, f10);
            case Event.VideoCodecInfo /* 299 */:
                return new Event(i10, j10, j11, str);
        }
    }

    public MediaPlayer(Media media) {
        super(media);
        this.mMedia = null;
        this.mPlaying = false;
        this.mPlayRequested = false;
        this.mListenAudioPlug = false;
        this.mVoutCount = 0;
        this.mAudioReset = false;
        this.mAudioOutput = "android_audiotrack";
        this.mAudioOutputDevice = null;
        this.mAudioPlugRegistered = false;
        this.mAudioDigitalOutputEnabled = false;
        this.mAudioPlugOutputDevice = "stereo";
        SurfaceListener surfaceListener = new SurfaceListener() { // from class: com.mrzero.tranplayer.MediaPlayer.1
            @Override // com.mrzero.tranplayer.MediaPlayer.SurfaceListener
            public void onSurfaceCreated() {
                boolean z10;
                boolean z11;
                synchronized (MediaPlayer.this) {
                    z10 = false;
                    if (MediaPlayer.this.mPlaying || !MediaPlayer.this.mPlayRequested) {
                        z11 = MediaPlayer.this.mVoutCount == 0;
                    } else {
                        z11 = false;
                        z10 = true;
                    }
                }
                if (z10) {
                    MediaPlayer.this.play();
                } else if (z11) {
                    MediaPlayer.this.setVideoTrackEnabled(true);
                }
            }

            @Override // com.mrzero.tranplayer.MediaPlayer.SurfaceListener
            public void onSurfaceDestroyed() {
                boolean z10;
                synchronized (MediaPlayer.this) {
                    z10 = MediaPlayer.this.mVoutCount > 0;
                }
                if (z10) {
                    MediaPlayer.this.setVideoTrackEnabled(false);
                }
            }
        };
        this.mSurfaceListener = surfaceListener;
        AWindow aWindow = new AWindow(surfaceListener);
        this.mWindow = aWindow;
        this.mGLRenderer = null;
        this.mAudioPlugReceiver = (!AndroidUtil.isLolliPopOrLater || AndroidUtil.isMarshMallowOrLater) ? null : createAudioPlugReceiver();
        this.mAudioDeviceCallback = AndroidUtil.isMarshMallowOrLater ? createAudioDeviceCallback() : null;
        if (media != null && !media.isReleased()) {
            this.mMedia = media;
            media.retain();
            nativeNewFromMedia(this.mMedia, aWindow);
            return;
        }
        throw new IllegalArgumentException("Media is null or released");
    }
}
