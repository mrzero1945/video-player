package com.mrzero.tranplayer;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import java.util.Objects;
import java.io.FileNotFoundException;
import android.media.RingtoneManager;
import android.content.res.AssetFileDescriptor;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import com.mrzero.tranplayer.IVLCVout;
import com.mrzero.tranplayer.MediaPlayer;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/* loaded from: classes3.dex */
public class TranMediaPlayer extends AbstractMediaPlayer implements IVLCVout.Callback, IVLCVout.OnNewVideoLayoutListener, MediaPlayer.EventListener {
    public static final int SOURCE_DIFF_TIME = 3000;
    public static final int SOURCE_TYPE_FILE = 3;
    public static final int SOURCE_TYPE_PATH = 2;
    public static final int SOURCE_TYPE_UNKNOWN = 0;
    public static final int SOURCE_TYPE_URI = 1;
    private static final String TAG = "TranMediaPlayer";
    private boolean bWillToSetSurface;
    private boolean mAOpensles;
    private AudioManager mAudioManager;
    private int mAudioSessionId;
    private int mAudioTracks;
    private Context mContext;
    private int mCurrentAudioTrack;
    private Media mCurrentMedia;
    private long mCurrentPosition;
    private long mDuration;
    private boolean mEofPaused;
    private int mFd;
    private ParcelFileDescriptor mFile;
    private boolean mFirstSetDataSource;
    private boolean mHasVideo;
    private boolean mIsBuffering;
    private boolean mIsFirstTimeChanged;
    private boolean mIsMediaCodec;
    private boolean mIsMute;
    private boolean mIsPlayingBeforeSeek;
    private boolean mIsReseting;
    private boolean mIsVlcReleasing;
    private boolean mIsprepareing;
    private long mLastSeekTime;
    private int mLastVolume;
    private float mLeftVolume;
    private long mLength;
    private LibVLC mLibVLC;
    private boolean mListenVideo;
    private Object mLock;
    private MediaPlayer mMediaPlayer;
    private boolean mNeedseek;
    private boolean mNoSubAutodetect;
    private long mOffset;
    private ArrayList<String> mOptions;
    private boolean mPIPVideoMode;
    private String mPath;
    private long mPausePosition;
    private boolean mPlayRequested;
    private boolean mPreferSoftDecoder;
    private boolean mReleaseMediaPlayer;
    private long mRenderedPicturePts;
    private float mRightVolume;
    private int mSarDen;
    private int mSarNum;
    private long mSeekCompleteConunt;
    private long mSeekPosition;
    private int mSourceType;
    private TextureView mSubtitlesTextureView;
    private SurfaceView mSubtitlesView;
    private Surface mSurface;
    private SurfaceHolder mSurfaceHolder;
    private Uri mUri;
    private boolean mVOpengl;
    private String mVersion;
    private String mVideoCodecName;
    private float mVideoFPS;
    private int mVideoHeight;
    private long mVideoSeekTime;
    private long mVideoStartTime;
    private int mVideoWidth;
    private boolean mSubtitleLoad = false;
    private boolean mVideoCodecDeleted = true;
    private boolean mHadVideoCodec = false;
    private boolean mPaused = false;
    private boolean mLosePic_seek = false;

    public static class TranTimedText implements ITimedText {
        private long mDuration;
        private long mStartTime;
        private Rect mTextBounds = null;
        private String mTextChars;

        public TranTimedText(String str, long j10, long j11) {
            this.mTextChars = str;
            this.mStartTime = j10;
            this.mDuration = j11;
        }

        @Override // com.mrzero.tranplayer.ITimedText
        public Rect getBounds() {
            return this.mTextBounds;
        }

        @Override // com.mrzero.tranplayer.ITimedText
        public long getDuration() {
            return this.mDuration;
        }

        @Override // com.mrzero.tranplayer.ITimedText
        public long getStartTime() {
            return this.mStartTime;
        }

        @Override // com.mrzero.tranplayer.ITimedText
        public String getText() {
            return this.mTextChars;
        }
    }

    public static class TranTrackInfo implements ITrackInfo {
        private int mTrackType = -1;
        private String mLanguage = "und";

        @Override // com.mrzero.tranplayer.ITrackInfo
        public IMediaFormat getFormat() {
            return null;
        }

        @Override // com.mrzero.tranplayer.ITrackInfo
        public String getInfoInline() {
            StringBuilder sb2 = new StringBuilder(128);
            int i10 = this.mTrackType;
            if (i10 == 0) {
                sb2.append("AUDIO");
                sb2.append(", ");
                sb2.append(getLanguage());
            } else if (i10 == 1) {
                sb2.append("VIDEO");
                sb2.append(", ");
                sb2.append(getLanguage());
            } else if (i10 == 3) {
                sb2.append("TIMEDTEXT");
                sb2.append(", ");
                sb2.append(getLanguage());
            } else if (i10 != 4) {
                sb2.append("UNKNOWN");
            } else {
                sb2.append("SUBTITLE");
            }
            return sb2.toString();
        }

        @Override // com.mrzero.tranplayer.ITrackInfo
        public String getLanguage() {
            return this.mLanguage;
        }

        @Override // com.mrzero.tranplayer.ITrackInfo
        public int getTrackType() {
            return this.mTrackType;
        }

        public void setLanguage(String str) {
            if (str.isEmpty()) {
                return;
            }
            this.mLanguage = str;
        }

        public void setTrackType(int i10) {
            this.mTrackType = i10;
        }
    }

    public TranMediaPlayer(Context context) {
        this.mIsReseting = false;
        this.mIsprepareing = false;
        this.bWillToSetSurface = false;
        String str = TAG;
        Log.d(str, "TranMediaPlayer {");
        this.mContext = context;
        this.mSourceType = 0;
        this.mDuration = -1L;
        this.mCurrentPosition = -1L;
        this.mSeekCompleteConunt = 0L;
        this.mRenderedPicturePts = -1L;
        this.mOffset = -1L;
        this.mLength = -1L;
        this.mVideoWidth = -1;
        this.mVideoHeight = -1;
        this.mSarNum = -1;
        this.mSarDen = -1;
        this.mFd = -1;
        this.mIsReseting = false;
        this.mIsprepareing = false;
        this.mIsBuffering = false;
        this.mIsVlcReleasing = false;
        this.mIsFirstTimeChanged = false;
        this.mHasVideo = false;
        this.mReleaseMediaPlayer = false;
        this.mCurrentAudioTrack = -1;
        AudioManager audioManager = (AudioManager) context.getSystemService("audio");
        this.mAudioManager = audioManager;
        int generateAudioSessionId = audioManager.generateAudioSessionId();
        this.mAudioSessionId = generateAudioSessionId;
        setAudioSessionId(generateAudioSessionId);
        this.mIsPlayingBeforeSeek = false;
        this.bWillToSetSurface = false;
        this.mLeftVolume = 0.0f;
        this.mRightVolume = 0.0f;
        this.mListenVideo = false;
        this.mPausePosition = -1L;
        this.mPIPVideoMode = false;
        this.mIsMute = false;
        this.mVideoStartTime = 0L;
        this.mVideoSeekTime = 0L;
        this.mVideoFPS = 0.0f;
        this.mIsMediaCodec = true;
        this.mSeekPosition = -1L;
        this.mFirstSetDataSource = false;
        this.mVersion = "2025.03.18-0";
        Log.d(str, "TranMediaPlayer sdkVersion:" + this.mVersion + " }");
    }

    private void ReleasePlayer() {
        Log.d(TAG, "releasePlayer " + this.mMediaPlayer + " {");
        setVolume(0.0f, 0.0f);
        this.mLastVolume = 0;
        if (this.mListenVideo) {
            try {
                Thread.sleep(70L);
            } catch (Exception unused) {
            }
            this.mListenVideo = false;
        }
        int i10 = 0;
        while (this.mIsprepareing) {
            if (i10 > 8) {
                this.mIsprepareing = false;
            }
            try {
                Thread.sleep(50L);
            } catch (Exception unused2) {
            }
            Log.d(TAG, "not Prepared");
            i10++;
        }
        releaseMediaPlayer();
        Log.d(TAG, "releasePlayer }");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fadedOutToSeek(long j10) {
        if (this.mMediaPlayer == null || this.mIsVlcReleasing || this.mSeekPosition <= 1000) {
            return;
        }
        Log.e(TAG, "faded out, seek to time:" + j10);
        this.mMediaPlayer.setTime(j10, true);
        this.mCurrentPosition = j10;
    }

    private String getMediaSourcePath(Uri uri) {
        Context context;
        Throwable th2;
        Cursor cursor;
        String str;
        Cursor cursor2 = null;
        String str2 = null;
        if (uri == null || (context = this.mContext) == null) {
            return null;
        }
        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor == null) {
                if (cursor == null) {
                    return null;
                }
                cursor.close();
                return null;
            }
            try {
                int columnIndexOrThrow = cursor.getColumnIndexOrThrow("_data");
                cursor.moveToFirst();
                str2 = cursor.getString(columnIndexOrThrow);
                Log.d(TAG, "path: " + str2);
                cursor.close();
                return str2;
            } catch (Exception unused) {
                String str3 = str2;
                cursor2 = cursor;
                str = str3;
                if (cursor2 != null) {
                    cursor2.close();
                }
                return str;
            } catch (Throwable th3) {
                th2 = th3;
                if (cursor != null) {
                    cursor.close();
                }
                throw th2;
            }
        } catch (Exception unused2) {
            return null;
        } catch (Throwable th4) {
            if (th4 instanceof Error) {
                throw ((Error) th4);
            }
            if (th4 instanceof RuntimeException) {
                throw ((RuntimeException) th4);
            }
            throw new RuntimeException(th4);
        }
    }

    private String getUriExtension() {
        Context context;
        if (this.mUri == null || (context = this.mContext) == null) {
            return "";
        }
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(this.mUri, null, null, null, null);
        } catch (Exception unused) {
            if (cursor == null) {
                return "";
            }
        } catch (Throwable th2) {
            if (cursor != null) {
                cursor.close();
            }
            throw th2;
        }
        if (cursor == null) {
            if (cursor == null) {
                return "";
            }
            cursor.close();
            return "";
        }
        int columnIndex = cursor.getColumnIndex("_display_name");
        cursor.moveToFirst();
        String string = cursor.getString(columnIndex);
        String substring = string.substring(string.lastIndexOf("."));
        cursor.close();
        return substring;
    }

    private void initMediaPlayer(boolean z10) {
        this.mMediaPlayer.play();
        if (this.mSurface != null) {
            this.mMediaPlayer.setListenVideoMode(false);
        }
    }

    private void parseHeader(Map<String, String> map) {
        long j10;
        String r2 = null;
        this.mVOpengl = false;
        this.mAOpensles = false;
        this.mPreferSoftDecoder = false;
        this.mNoSubAutodetect = false;
        if (map != null) {
            String str = map.get("prefer-soft-decoder");
            if (str != null && str.toString().compareTo("1") == 0) {
                this.mPreferSoftDecoder = true;
            }
            String str2 = map.get("codec-is-copyright");
            r2 = str2 != null ? str2.toString() : null;
            String str3 = map.get("start-time");
            if (str3 != null) {
                j10 = Long.parseLong(str3.toString());
                this.mCurrentPosition = j10;
            } else {
                j10 = 0;
            }
            String str4 = map.get("prepare-paused");
            if (str4 != null) {
                str4.toString().compareTo("1");
            }
            String str5 = map.get("pause-after-eof");
            if (str5 != null) {
                str5.toString().compareTo("1");
            }
            String str6 = map.get("audio-track");
            if (str6 != null) {
                this.mAudioTracks = Integer.parseInt(str6.toString());
            }
            if (map.get("vlc-video-opengl") != null) {
                this.mVOpengl = true;
            }
            if (map.get("no-sub-autodetect") != null) {
                this.mNoSubAutodetect = true;
            }
            String str7 = map.get("listen-video");
            if (str7 != null && str7.toString().compareTo("1") == 0) {
                this.mListenVideo = true;
            }
            String str8 = map.get("audio-setmute");
            if (str8 != null && str8.toString().compareTo("1") == 0) {
                this.mIsMute = true;
            }
        } else {
            j10 = 0;
        }
        if (this.mOptions == null) {
            this.mOptions = new ArrayList<>(50);
        }
        ArrayList<String> arrayList = this.mOptions;
        if (arrayList != null) {
            arrayList.clear();
        }
        if (this.mOptions.isEmpty()) {
            this.mOptions.add("-vv");
            this.mOptions.add("--network-caching=2000");
            this.mOptions.add("--file-caching=3000");
            this.mOptions.add("--audio-time-stretch");
            this.mOptions.add("--no-volume-save");
            this.mOptions.add("--android-display-chroma");
            this.mOptions.add("YV12");
            this.mOptions.add("--sub-notify");
            this.mOptions.add("--demux");
            this.mOptions.add("avformat,any");
            this.mOptions.add("avcodec-hurry-up");
            this.mOptions.add("--codec");
            if (this.mPreferSoftDecoder) {
                this.mOptions.add("avcodec,all");
            } else {
                this.mOptions.add("mediacodec_jni,all");
            }
            if (this.mAOpensles) {
                this.mOptions.add("--aout=opensles,none");
            } else {
                this.mOptions.add("--aout=android_audiotrack,none");
            }
            if (this.mVOpengl) {
                this.mOptions.add("--vout=gles2,none");
            } else {
                this.mOptions.add("--vout=android_display,none");
            }
            if (this.mListenVideo) {
                this.mOptions.add("--listen-video-mode");
            }
            if (r2 != null) {
                this.mOptions.add("--codec-is-copyright");
                this.mOptions.add(r2);
            }
            if (j10 != 0) {
                this.mOptions.add("--start-time=" + (j10 / 1000.0f));
            }
            this.mOptions.add("--start-paused");
            if (this.mAudioTracks != 0) {
                this.mOptions.add("--audio-track=" + this.mAudioTracks);
            }
            int audioSessionId = getAudioSessionId();
            this.mOptions.add("--audiotrack-session-id=" + audioSessionId);
            this.mOptions.add("--play-and-pause");
            if (this.mNoSubAutodetect) {
                this.mOptions.add("--no-sub-autodetect-file");
            }
            if (this.mIsMute) {
                this.mOptions.add("--audio-ismute");
            }
        }
    }

    private void preInitPlayer() {
        String str = TAG;
        Log.e(str, "preInitPlayer into {");
        releaseMediaPlayer();
        this.mReleaseMediaPlayer = false;
        this.mVideoStartTime = System.currentTimeMillis();
        LibVLC libVLC = new LibVLC(this.mContext, this.mOptions);
        this.mLibVLC = libVLC;
        MediaPlayer mediaPlayer = new MediaPlayer(libVLC);
        this.mMediaPlayer = mediaPlayer;
        mediaPlayer.setEventListener((MediaPlayer.EventListener) this);
        this.mLock = new Object();
        this.mIsVlcReleasing = false;
        int i10 = this.mSourceType;
        if (i10 == 1) {
            this.mCurrentMedia = new Media(this.mLibVLC, this.mUri);
        } else if (i10 == 2) {
            this.mCurrentMedia = new Media(this.mLibVLC, this.mPath);
        } else {
            if (i10 != 3) {
                Log.e(str, "do not have a valid data source, please call setDataSource");
                return;
            }
            this.mCurrentMedia = new Media(this.mLibVLC, this.mFd, this.mOffset, this.mLength);
        }
        this.mCurrentMedia.addOption(":network-caching=2000");
        this.mCurrentMedia.addOption(":file-caching=3000");
        if (this.mPreferSoftDecoder) {
            this.mCurrentMedia.addOption(":codec=avcodec,all");
        } else {
            this.mCurrentMedia.addOption(":codec=mediacodec_jni,all");
        }
        this.mMediaPlayer.setMedia(this.mCurrentMedia);
        if (this.mAOpensles) {
            this.mMediaPlayer.setAudioOutput("opensles");
        }
        if (this.mSubtitlesView != null) {
            this.mMediaPlayer.getVLCVout().setSubtitlesView(this.mSubtitlesView);
        }
        if (this.mSubtitlesTextureView != null) {
            this.mMediaPlayer.getVLCVout().setSubtitlesView(this.mSubtitlesTextureView);
        }
        if (this.mSurface != null) {
            this.mMediaPlayer.getVLCVout().setVideoSurface(this.mSurface, null);
            this.mMediaPlayer.getVLCVout().addCallback(this);
            this.mMediaPlayer.getVLCVout().attachViews(this);
            this.mMediaPlayer.setSurface(true);
            this.mMediaPlayer.setSurfaceDestroyed(false);
        }
        this.mFirstSetDataSource = true;
        Log.e(str, "preInitPlayer }");
    }

    private void releaseMediaPlayer() {
        String str = TAG;
        Log.d(str, "releaseMediaPlayer {");
        if (this.mIsVlcReleasing) {
            Log.d(str, "releaseMediaPlayer return  } ");
            return;
        }
        this.mIsVlcReleasing = true;
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        if (mediaPlayer != null) {
            if (mediaPlayer.getVLCVout().areViewsAttached()) {
                this.mMediaPlayer.getVLCVout().removeCallback(this);
                this.mMediaPlayer.getVLCVout().detachViews();
            }
            this.mMediaPlayer.setEventListener((MediaPlayer.EventListener) null);
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
        }
        Media media = this.mCurrentMedia;
        if (media != null) {
            media.release();
            this.mCurrentMedia = null;
        }
        LibVLC libVLC = this.mLibVLC;
        if (libVLC != null) {
            libVLC.release();
            this.mLibVLC = null;
        }
        this.mLock = null;
        Log.d(str, "releaseMediaPlayer }");
    }

    private void setVolumeFadeInOut(int i10, int i11, long j10) {
        String str = TAG;
        Log.e(str, "setVolumeFadeInOut. current_volume:" + i10 + " last_volume:" + i11 + " seekTime:" + j10 + ",mIsMute:" + this.mIsMute);
        if (i10 != i11 && !this.mIsMute) {
            new Thread(new FadeRunnable(i10, i11, j10)).start();
            return;
        }
        Log.e(str, "current_volume == last_volume return");
        if (j10 != -1) {
            fadedOutToSeek(j10);
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:18:0x00b8  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    private void willToSetDisplay(android.view.SurfaceHolder surfaceHolder) {
        this.bWillToSetSurface = surfaceHolder != null;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public void addTimedTextSource(String str, String str2) {
        String str3 = TAG;
        Log.d(str3, "addTimedTextSource {");
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        if (mediaPlayer != null) {
            mediaPlayer.addSlave(0, str, true);
        }
        Log.d(str3, "addTimedTextSource }");
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public void deselectTrack(int i10) throws IllegalStateException {
        String str = TAG;
        Log.d(str, "deselectTrack {");
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        if (mediaPlayer != null) {
            if (i10 == 1) {
                mediaPlayer.setVideoTrack(-1);
            } else if (i10 == 0) {
                mediaPlayer.setAudioTrack(-1);
            } else if (i10 == 3) {
                mediaPlayer.setSpuTrack(-1);
            }
        }
        Log.d(str, "deselectTrack }");
    }

    public void finalize() {
        Log.d(TAG, "finalize {");
        this.mReleaseMediaPlayer = true;
        if (this.mMediaPlayer != null && !this.mIsReseting) {
            this.mContext = null;
            releaseMediaPlayer();
            resetListeners();
            this.mSurface = null;
            this.mSubtitlesView = null;
            this.mSubtitlesTextureView = null;
        }
        ParcelFileDescriptor parcelFileDescriptor = this.mFile;
        if (parcelFileDescriptor != null) {
            try {
                parcelFileDescriptor.close();
            } catch (IOException unused) {
            }
            this.mFile = null;
            this.mFd = -1;
        }
        this.mIsprepareing = false;
        this.mPaused = false;
        this.bWillToSetSurface = false;
        this.mSurfaceHolder = null;
        Log.d(TAG, "finalize }");
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public int getAudioSessionId() {
        String str = TAG;
        Log.d(str, "getAudioSessionId {");
        Log.d(str, "getAudioSessionId ---- mAudioSessionId:" + this.mAudioSessionId + " }");
        return this.mAudioSessionId;
    }

    public int getAudioTrack() {
        String str = TAG;
        Log.d(str, "getAudioTrack {");
        int audioTrack = this.mMediaPlayer.getAudioTrack();
        Log.d(str, "getAudioTrack   currentAudioTrack:" + audioTrack + " }");
        return audioTrack;
    }

    public MediaPlayer.TrackDescription[] getAudioTracks() {
        return this.mMediaPlayer.getAudioTracks();
    }

    public int getAudioTracksCount() {
        String str = TAG;
        Log.d(str, "getAudioTracksCount {");
        int audioTracksCount = this.mMediaPlayer.getAudioTracksCount();
        Log.d(str, "getAudioTracksCount   audioTracksCount:" + audioTracksCount + " }");
        return audioTracksCount;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public long getCurrentPosition() {
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        if (mediaPlayer == null) {
            return 0L;
        }
        if (this.mPaused) {
            long j10 = this.mPausePosition;
            if (j10 > 0) {
                return j10;
            }
        }
        if (this.mSeekPosition < 0) {
            return mediaPlayer.getTime();
        }
        long time = mediaPlayer.getTime();
        return time <= 0 ? this.mSeekPosition : time;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public String getDataSource() {
        String str = TAG;
        Log.d(str, "getDataSource {");
        int i10 = this.mSourceType;
        String valueOf = i10 != 1 ? i10 != 2 ? i10 != 3 ? "" : String.valueOf(this.mFd) : this.mPath : this.mUri.toString();
        Log.d(str, "getDataSource }");
        return valueOf;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public long getDuration() {
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        if (mediaPlayer != null) {
            this.mDuration = mediaPlayer.getLength();
        }
        return this.mDuration;
    }

    public boolean getIsVideoMediaCodec() {
        return this.mIsMediaCodec;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public boolean getListenVideoMode() {
        String str = TAG;
        Log.d(str, "getListenVideoMode {");
        boolean z10 = this.mListenVideo;
        Log.d(str, "getListenVideoMode  listenVideo:" + z10 + " }");
        return z10;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public MediaInfo getMediaInfo() {
        String str = TAG;
        Log.d(str, "getMediaInfo {");
        Log.d(str, "getMediaInfo }");
        return null;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public int getSelectedTrack(int i10) throws IllegalStateException {
        int i11 = -1;
        String str = TAG;
        Log.d(str, "getSelectedTrack {");
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        if (mediaPlayer != null) {
            if (i10 == 0) {
                i11 = mediaPlayer.getAudioTrack();
            } else if (i10 == 1) {
                i11 = mediaPlayer.getVideoTrack();
            } else if (i10 == 3) {
                i11 = mediaPlayer.getSpuTrack();
            }
            Log.d(str, "getSelectedTrack " + i11 + " }");
            return i11;
        }
        i11 = -1;
        Log.d(str, "getSelectedTrack " + i11 + " }");
        return i11;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public float getSpeed() {
        String str = TAG;
        Log.d(str, "getSpeed {");
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        float rate = mediaPlayer != null ? mediaPlayer.getRate() : 1.0f;
        Log.d(str, "getSpeed }");
        return rate;
    }

    public int getSpuTrack() {
        String str = TAG;
        Log.d(str, "getVideoTrack {");
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        int spuTrack = mediaPlayer != null ? mediaPlayer.getSpuTrack() : -1;
        Log.d(str, "getVideoTrack   spuTrack:" + spuTrack + " }");
        return spuTrack;
    }

    public MediaPlayer.TrackDescription[] getSpuTracks() {
        return this.mMediaPlayer.getSpuTracks();
    }

    public int getSpuTracksCount() {
        String str = TAG;
        Log.d(str, "getVideoTracksCount {");
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        int spuTracksCount = mediaPlayer != null ? mediaPlayer.getSpuTracksCount() : 0;
        Log.d(str, "getVideoTracksCount  spuTracksCount:" + spuTracksCount + " }");
        return spuTracksCount;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public long getTimedTextDelay() {
        String str = TAG;
        Log.d(str, "getTimedTextDelay {");
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        long spuDelay = mediaPlayer != null ? mediaPlayer.getSpuDelay() : 0L;
        Log.d(str, "getTimedTextDelay delay:" + spuDelay + " }");
        return spuDelay;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public ITrackInfo[] getTrackInfo() throws IllegalStateException {
        MediaPlayer.TrackDescription[] spuTracks;
        MediaPlayer.TrackDescription[] audioTracks;
        MediaPlayer.TrackDescription[] videoTracks;
        String str = TAG;
        Log.d(str, "getTrackInfo {");
        if (this.mMediaPlayer == null) {
            Log.d(str, "getTrackInfo failed }");
            return null;
        }
        ArrayList arrayList = new ArrayList();
        if (this.mMediaPlayer.getVideoTracksCount() > 0 && (videoTracks = this.mMediaPlayer.getVideoTracks()) != null) {
            for (MediaPlayer.TrackDescription trackDescription : videoTracks) {
                if (trackDescription.f9413id >= 0) {
                    TranTrackInfo tranTrackInfo = new TranTrackInfo();
                    tranTrackInfo.setLanguage(trackDescription.name);
                    tranTrackInfo.setTrackType(1);
                    Log.d(TAG, "id " + trackDescription.f9413id + " name " + trackDescription.name);
                    arrayList.add(tranTrackInfo);
                }
            }
        }
        if (this.mMediaPlayer.getAudioTracksCount() > 0 && (audioTracks = this.mMediaPlayer.getAudioTracks()) != null) {
            for (MediaPlayer.TrackDescription trackDescription2 : audioTracks) {
                if (trackDescription2.f9413id >= 0) {
                    TranTrackInfo tranTrackInfo2 = new TranTrackInfo();
                    tranTrackInfo2.setLanguage(trackDescription2.name);
                    tranTrackInfo2.setTrackType(0);
                    Log.d(TAG, "id " + trackDescription2.f9413id + " name " + trackDescription2.name);
                    arrayList.add(tranTrackInfo2);
                }
            }
        }
        if (this.mMediaPlayer.getSpuTracksCount() > 0 && (spuTracks = this.mMediaPlayer.getSpuTracks()) != null) {
            for (MediaPlayer.TrackDescription trackDescription3 : spuTracks) {
                if (trackDescription3.f9413id >= 0) {
                    TranTrackInfo tranTrackInfo3 = new TranTrackInfo();
                    tranTrackInfo3.setLanguage(trackDescription3.name);
                    tranTrackInfo3.setTrackType(3);
                    Log.d(TAG, "id " + trackDescription3.f9413id + " name " + trackDescription3.name);
                    arrayList.add(tranTrackInfo3);
                }
            }
        }
        Log.d(TAG, "getTrackInfo success }");
        return (ITrackInfo[]) arrayList.toArray(new TranTrackInfo[arrayList.size()]);
    }

    public String getVideoCodecName() {
        return this.mVideoCodecName;
    }

    public float getVideoFPS() {
        return this.mVideoFPS;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public int getVideoHeight() {
        Log.d(TAG, "getVideoHeight " + this.mVideoHeight);
        return this.mVideoHeight;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public int getVideoSarDen() {
        Log.d(TAG, "getVideoSarDen " + this.mSarDen);
        return this.mSarDen;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public int getVideoSarNum() {
        Log.d(TAG, "getVideoSarNum " + this.mSarNum);
        return this.mSarNum;
    }

    public int getVideoTrack() {
        String str = TAG;
        Log.d(str, "getVideoTrack {");
        int videoTrack = this.mMediaPlayer.getVideoTrack();
        Log.d(str, "getVideoTrack   currentVideoTrack:" + videoTrack + " }");
        return videoTrack;
    }

    public MediaPlayer.TrackDescription[] getVideoTracks() {
        return this.mMediaPlayer.getVideoTracks();
    }

    public int getVideoTracksCount() {
        String str = TAG;
        Log.d(str, "getVideoTracksCount {");
        int videoTracksCount = this.mMediaPlayer.getVideoTracksCount();
        Log.d(str, "getVideoTracksCount   videoTracksCount:" + videoTracksCount + " }");
        return videoTracksCount;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public int getVideoWidth() {
        Log.d(TAG, "getVideoWidth " + this.mVideoWidth);
        return this.mVideoWidth;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public boolean isLooping() {
        String str = TAG;
        Log.d(str, "isLooping {");
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        boolean isLooping = mediaPlayer != null ? mediaPlayer.isLooping() : false;
        Log.d(str, "isLooping }");
        return isLooping;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public boolean isPlaying() {
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        if (mediaPlayer == null || this.mIsReseting) {
            return false;
        }
        return mediaPlayer.isPlaying();
    }

    @Override // com.mrzero.tranplayer.IVLCVout.OnNewVideoLayoutListener
    public void onNewVideoLayout(IVLCVout iVLCVout, int i10, int i11, int i12, int i13, int i14, int i15) {
        if (i10 == 0 || i11 == 0) {
            return;
        }
        notifyOnVideoSizeChanged(i10, i11, i14, i15);
    }

    @Override // com.mrzero.tranplayer.IVLCVout.Callback
    public void onSurfacesCreated(IVLCVout iVLCVout) {
        String str = TAG;
        Log.d(str, "onSurfacesCreated .  getListenVideoMode():" + getListenVideoMode() + " . " + this.mMediaPlayer + " {");
        if (this.mMediaPlayer != null && this.mCurrentAudioTrack >= 0) {
            Log.e(str, "onSurfacesCreated  ---- Before setAudioTrack .  mCurrentAudioTrack:" + this.mCurrentAudioTrack);
            this.mMediaPlayer.setAudioTrack(this.mCurrentAudioTrack);
        }
        Log.d(str, "onSurfacesCreated .  }");
    }

    @Override // com.mrzero.tranplayer.IVLCVout.Callback
    public void onSurfacesDestroyed(IVLCVout iVLCVout) {
        String str = TAG;
        Log.d(str, "onSurfacesDestroyed . getListenVideoMode():" + getListenVideoMode() + " . " + this.mMediaPlayer + " {");
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        if (mediaPlayer != null) {
            mediaPlayer.setSurfaceDestroyed(true);
            if (!getListenVideoMode()) {
                this.mCurrentAudioTrack = getSelectedTrack(0);
                Log.e(str, "onSurfacesDestroyed  ---- Before setAudioTrack(-1) .  mCurrentAudioTrack:" + this.mCurrentAudioTrack);
                this.mMediaPlayer.setAudioTrack(-1);
            }
            this.mMediaPlayer.setVideoTrack(-1);
        }
        Log.d(str, "onSurfacesDestroyed .  }");
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public void pause() throws IllegalStateException {
        String str = TAG;
        Log.d(str, "pause " + this.mMediaPlayer + " {");
        if (this.mMediaPlayer != null && isPlaying()) {
            float volume = this.mMediaPlayer.getVolume() / 100.0f;
            this.mRightVolume = volume;
            this.mLeftVolume = volume;
            this.mMediaPlayer.setVolume(0);
            this.mLastVolume = 0;
            this.mMediaPlayer.pause();
            this.mPausePosition = this.mMediaPlayer.getTime();
            this.mLosePic_seek = true;
        }
        this.mPlayRequested = false;
        this.mIsPlayingBeforeSeek = false;
        this.mPaused = true;
        Log.d(str, "pause }");
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public void prepare() throws IOException, IllegalStateException {
        String str = TAG;
        Log.d(str, "prepare {");
        this.mIsprepareing = true;
        this.mEofPaused = false;
        initMediaPlayer(false);
        Log.d(str, "prepare " + this.mMediaPlayer + " }");
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public void prepareAsync() throws IllegalStateException {
        String str = TAG;
        Log.d(str, "prepareAsync {");
        this.mIsprepareing = true;
        this.mEofPaused = false;
        initMediaPlayer(true);
        Log.d(str, "prepareAsync " + this.mMediaPlayer + " }");
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public void release() {
        Log.d(TAG, "release " + this.mMediaPlayer + " {");
        this.mReleaseMediaPlayer = true;
        if (this.mMediaPlayer != null) {
            this.mContext = null;
            ReleasePlayer();
            resetListeners();
            this.mSurface = null;
            this.mSubtitlesView = null;
            this.mSubtitlesTextureView = null;
        }
        ParcelFileDescriptor parcelFileDescriptor = this.mFile;
        if (parcelFileDescriptor != null) {
            try {
                parcelFileDescriptor.close();
            } catch (IOException unused) {
            }
            this.mFile = null;
            this.mFd = -1;
        }
        this.mPaused = false;
        this.mAudioSessionId = 0;
        this.bWillToSetSurface = false;
        this.mSurfaceHolder = null;
        this.mLeftVolume = 0.0f;
        this.mRightVolume = 0.0f;
        this.mPausePosition = -1L;
        this.mPIPVideoMode = false;
        this.mIsMute = false;
        this.mVideoStartTime = 0L;
        this.mVideoSeekTime = 0L;
        this.mVideoFPS = 0.0f;
        this.mIsMediaCodec = true;
        this.mSeekPosition = -1L;
        this.mPlayRequested = false;
        Log.d(TAG, "release }");
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public void reset() {
        Log.d(TAG, "reset " + this.mMediaPlayer + " {");
        this.mIsReseting = true;
        this.mReleaseMediaPlayer = true;
        if (this.mMediaPlayer != null) {
            setVolume(0.0f, 0.0f);
            this.mLastVolume = 0;
            this.mContext = null;
            ReleasePlayer();
        }
        ParcelFileDescriptor parcelFileDescriptor = this.mFile;
        if (parcelFileDescriptor != null) {
            try {
                parcelFileDescriptor.close();
            } catch (IOException unused) {
            }
            this.mFile = null;
            this.mFd = -1;
        }
        this.mPaused = false;
        this.mIsReseting = false;
        this.bWillToSetSurface = false;
        this.mVideoCodecDeleted = true;
        this.mSurfaceHolder = null;
        this.mPausePosition = -1L;
        this.mSeekPosition = -1L;
        Log.d(TAG, "reset }");
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public void seekTo(long j10, boolean z10, boolean z11) throws IllegalStateException {
        String str = TAG;
        Log.d(str, "seekTo " + j10 + "-1" + this.mMediaPlayer + " {");
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        if (mediaPlayer != null) {
            if (j10 < 0) {
                j10 = 0;
            } else {
                long j11 = this.mDuration;
                if (j11 > 0 && j10 > j11) {
                    j10 = j11;
                }
            }
            if (this.mEofPaused && j10 != this.mDuration) {
                this.mEofPaused = false;
            }
            this.mPausePosition = j10;
            this.mSeekPosition = j10;
            this.mSeekCompleteConunt = 0L;
            this.mIsFirstTimeChanged = false;
            this.mLastSeekTime = j10;
            if (j10 <= 1000 || this.mPaused || this.mIsMute || !z11) {
                mediaPlayer.setTime(j10, true);
                this.mCurrentPosition = j10;
            } else {
                setVolumeFadeInOut(0, this.mLastVolume, j10);
                this.mLastVolume = 0;
            }
            if (this.mVideoSeekTime == 0 && z10) {
                this.mVideoSeekTime = System.currentTimeMillis();
            }
        }
        Log.d(str, "seekTo }");
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public void selectTrack(int i10) throws IllegalStateException {
        String str = TAG;
        Log.d(str, "selectTrack {");
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        if (mediaPlayer != null) {
            int videoTracksCount = mediaPlayer.getVideoTracksCount() - 1;
            int audioTracksCount = this.mMediaPlayer.getAudioTracksCount() - 1;
            int spuTracksCount = this.mMediaPlayer.getSpuTracksCount() - 1;
            if (i10 >= 0 && i10 < videoTracksCount) {
                this.mMediaPlayer.setVideoTrack(i10);
            } else if (i10 < videoTracksCount || i10 >= videoTracksCount + audioTracksCount) {
                int i11 = videoTracksCount + audioTracksCount;
                if (i10 < i11 || i10 >= i11 + spuTracksCount) {
                    Log.d(str, "unknown track index");
                } else {
                    this.mMediaPlayer.setSpuTrack(i10);
                }
            } else {
                this.mMediaPlayer.setAudioTrack(i10);
            }
            if (getCurrentPosition() != this.mLastSeekTime || getCurrentPosition() == 0) {
                if (isPlaying()) {
                    seekTo(getCurrentPosition(), false, false);
                } else {
                    this.mNeedseek = true;
                }
            }
        }
        Log.d(str, "selectTrack }");
    }

    public void setAudioAttributes(AudioAttributes audioAttributes) throws IllegalArgumentException {
        String str = TAG;
        Log.d(str, "setAudioAttributes {");
        Log.d(str, "setAudioAttributes }");
    }

    public void setAudioSessionId(int i10) {
        String str = TAG;
        Log.d(str, "setAudioSessionId {");
        this.mAudioSessionId = i10;
        Log.d(str, "setAudioSessionId ---- mAudioSessionId:" + i10 + " }");
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public void setAudioStreamType(int i10) {
        String str = TAG;
        Log.d(str, "setAudioStreamType {");
        Log.d(str, "setAudioStreamType }");
    }

    public boolean setAudioTrack(int i10) {
        if (i10 != -1 && i10 == getAudioTrack()) {
            return false;
        }
        Log.d(TAG, "setAudioTrack  index: " + i10);
        boolean audioTrack = this.mMediaPlayer.setAudioTrack(i10);
        if (i10 != -1) {
            seekTo(getCurrentPosition(), false, false);
        }
        return audioTrack;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public void setDataSource(Context context, Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(context, uri, (Map<String, String>) null);
    }

    /* JADX WARN: Removed duplicated region for block: B:30:0x010a  */
    @Override // com.mrzero.tranplayer.IMediaPlayer
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void setDisplay(android.view.SurfaceHolder surfaceHolder) {
        String str = TAG;
        Log.d(str, "setDisplay " + surfaceHolder + " " + this.mMediaPlayer + " mVideoCodecDeleted: " + this.mVideoCodecDeleted + " libvlc {");
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        Surface surface = null;
        if (mediaPlayer != null) {
            if (surfaceHolder == null) {
                this.bWillToSetSurface = false;
                mediaPlayer.setSurfaceDestroyed(true);
                this.mMediaPlayer.setVideoTrack(-1);
                this.mMediaPlayer.setSurface(false);
                this.mSurfaceHolder = null;
            } else {
                if (this.mSurface != null && mediaPlayer.getVLCVout().areViewsAttached()) {
                    this.mMediaPlayer.getVLCVout().removeCallback(this);
                    this.mMediaPlayer.getVLCVout().detachViews();
                }
                this.mSurfaceHolder = surfaceHolder;
                if (this.mHadVideoCodec && !this.mVideoCodecDeleted) {
                    this.bWillToSetSurface = true;
                    this.mSurfaceHolder = surfaceHolder;
                    Log.d(str, "setDisplay goto bWillToSetSurface ");
                    this.mSurface = surfaceHolder.getSurface();
                    return;
                }
                this.mMediaPlayer.setSurfaceDestroyed(false);
                this.mMediaPlayer.getVLCVout().setVideoSurface(surfaceHolder.getSurface(), surfaceHolder);
                this.mMediaPlayer.getVLCVout().addCallback(this);
                this.mMediaPlayer.getVLCVout().attachViews(this);
                this.mMediaPlayer.setSurface(true);
                this.bWillToSetSurface = false;
                this.mMediaPlayer.setListenVideoMode(false);
                long currentPosition = getCurrentPosition();
                long duration = getDuration();
                this.mMediaPlayer.setPinPVideoMode(this.mPIPVideoMode);
                if (!this.mEofPaused) {
                    long j10 = duration - currentPosition;
                    if (j10 < 1000) {
                        seekTo(0L, false, false);
                    } else if (j10 < 3000) {
                        Log.d(str, "now:" + currentPosition + ", duration:" + duration + ". it\'s close to duration when resume playback");
                        seekTo(currentPosition, false, false);
                    }
                } else {
                    seekTo(0L, false, false);
                    this.mEofPaused = true;
                }
                if (this.mPaused || this.mEofPaused) {
                    this.mMediaPlayer.play();
                    this.mMediaPlayer.setRefreshFirstFrame(true);
                }
                if (this.mEofPaused) {
                    this.mMediaPlayer.setDropAudioFrames(true);
                }
            }
        }
        if (surfaceHolder != null && this.mMediaPlayer != null) {
            surface = surfaceHolder.getSurface();
        }
        this.mSurface = surface;
        Log.d(str, "setDisplay " + this.mSurface + " }");
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public void setListenVideoMode(boolean z10) {
        String str = TAG;
        Log.d(str, "setListenVideoMode " + z10 + " {");
        this.mListenVideo = z10;
        Log.d(str, "setListenVideoMode }");
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public void setLooping(boolean z10) {
        String str = TAG;
        Log.d(str, "setLooping {");
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        if (mediaPlayer != null) {
            mediaPlayer.setLooping(z10);
        }
        Log.d(str, "setLooping }");
    }

    public void setMute(boolean z10) {
        String str = TAG;
        Log.d(str, "setMute " + this.mIsMute + " {");
        this.mIsMute = z10;
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        if (mediaPlayer != null) {
            mediaPlayer.setAudioMute(z10);
        }
        Log.d(str, "setMute }");
    }

    public void setOnPause() {
        if (this.mMediaPlayer == null || this.mPIPVideoMode) {
            return;
        }
        Log.d(TAG, "setOnPause true");
        this.mMediaPlayer.setSurfaceDestroyed(true);
    }

    public void setPictureInPictureMode(boolean z10) {
        String str = TAG;
        Log.d(str, "setPictureInPictureMode " + z10 + " {");
        this.mPIPVideoMode = z10;
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        if (mediaPlayer != null) {
            mediaPlayer.setPinPVideoMode(z10);
        }
        Log.d(str, "setPictureInPictureMode }");
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public void setScreenOnWhilePlaying(boolean z10) {
        String str = TAG;
        Log.d(str, "setScreenOnWhilePlaying {");
        Log.d(str, "setScreenOnWhilePlaying }");
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public void setSpeed(float f10) {
        String str = TAG;
        Log.d(str, "setSpeed " + f10 + " {");
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        if (mediaPlayer != null) {
            mediaPlayer.setRate(f10);
        }
        Log.d(str, "setSpeed }");
    }

    public boolean setSpuTrack(int i10) {
        String str = TAG;
        Log.d(str, "setSpuTrack  index: " + i10);
        boolean spuTrack = this.mMediaPlayer.setSpuTrack(i10);
        if (getCurrentPosition() != this.mLastSeekTime || getCurrentPosition() == 0) {
            if (!isPlaying() || i10 == -1) {
                this.mNeedseek = true;
            } else {
                seekTo(getCurrentPosition(), false, false);
            }
        }
        if (this.mPaused && i10 == -1) {
            Log.d(str, "mNeedseek true ");
            this.mNeedseek = true;
        }
        return spuTrack;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public void setSurface(Surface surface) {
        String str = TAG;
        Log.d(str, "setSurface " + surface + "-1" + this.mMediaPlayer + " {");
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        if (mediaPlayer != null) {
            if (surface != null) {
                if (this.mSurface != null && mediaPlayer.getVLCVout().areViewsAttached()) {
                    this.mMediaPlayer.getVLCVout().removeCallback(this);
                    this.mMediaPlayer.getVLCVout().detachViews();
                }
                this.mMediaPlayer.getVLCVout().setVideoSurface(surface, null);
                this.mMediaPlayer.getVLCVout().addCallback(this);
                this.mMediaPlayer.getVLCVout().attachViews(this);
                this.mMediaPlayer.setSurface(true);
                this.mMediaPlayer.setSurfaceDestroyed(false);
            } else {
                mediaPlayer.getVLCVout().removeCallback(this);
                this.mMediaPlayer.getVLCVout().detachViews();
            }
        }
        this.mSurface = surface;
        Log.d(str, "setSurface }");
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public void setTimedTextDelay(long j10) {
        String str = TAG;
        Log.d(str, "setTimedTextDelay  delay:" + j10 + " {");
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        if (mediaPlayer != null) {
            mediaPlayer.setSpuDelay(j10);
        }
        Log.d(str, "setTimedTextDelay }");
    }

    public void setTimedTextView(SurfaceView surfaceView) {
        String str = TAG;
        Log.d(str, "setTimedTextView surfaceView " + surfaceView + "-1" + this.mMediaPlayer + " {");
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        if (mediaPlayer != null) {
            this.mSubtitlesView = null;
            if (surfaceView != null) {
                mediaPlayer.getVLCVout().setSubtitlesView(surfaceView);
            }
        } else {
            this.mSubtitlesView = surfaceView;
        }
        Log.d(str, "setTimedTextView }");
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public void setVolume(float f10, float f11) {
        String str = TAG;
        StringBuilder sb2 = new StringBuilder();
        sb2.append("setVolume ");
        float f12 = ((f10 + f11) * 100.0f) / 2.0f;
        sb2.append(f12);
        sb2.append(" {");
        Log.d(str, sb2.toString());
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        if (mediaPlayer != null) {
            mediaPlayer.setVolume((int) f12);
        }
        this.mLeftVolume = f10;
        this.mRightVolume = f11;
        if (f10 > 0.0f || f11 > 0.0f) {
            this.mIsMute = false;
        }
        Log.d(str, "setVolume }");
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public void start() throws IllegalStateException {
        String str = TAG;
        Log.d(str, "start mVideoCodecDeleted:" + this.mVideoCodecDeleted + ", mHadVideoCodec:" + this.mHadVideoCodec + ", " + this.mMediaPlayer + " {");
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        if (mediaPlayer != null) {
            mediaPlayer.setRefreshFirstFrame(false);
            this.mMediaPlayer.setDropAudioFrames(false);
            if (this.mHadVideoCodec && !this.mVideoCodecDeleted && this.bWillToSetSurface) {
                this.mPlayRequested = true;
                Log.d(str, "start mVideoCodecDeleted:" + this.mVideoCodecDeleted + "}");
                return;
            }
            if (!this.mListenVideo && this.mSurfaceHolder == null && this.mSurface == null) {
                this.mPlayRequested = true;
                Log.d(str, "start mListenVideo:" + this.mListenVideo + "}");
                return;
            }
            if (this.mEofPaused) {
                this.mEofPaused = false;
                seekTo(0L, false, false);
                this.mNeedseek = false;
            }
            this.mPlayRequested = false;
            this.mIsPlayingBeforeSeek = true;
            this.mMediaPlayer.play();
            if (this.mLeftVolume == 0.0f || this.mRightVolume == 0.0f) {
                this.mRightVolume = 1.0f;
                this.mLeftVolume = 1.0f;
            }
            if (this.mNeedseek) {
                this.mNeedseek = false;
                if (!this.mFirstSetDataSource) {
                    seekTo(getCurrentPosition(), false, false);
                }
            } else {
                float f10 = this.mLeftVolume;
                if ((f10 > 0.0f || this.mRightVolume > 0.0f) && !this.mIsMute) {
                    setVolumeFadeInOut((int) (((f10 + this.mRightVolume) * 100.0f) / 2.0f), this.mLastVolume);
                    this.mLastVolume = (((int) (this.mLeftVolume + this.mRightVolume)) * 100) / 2;
                }
            }
        }
        this.mPaused = false;
        this.mFirstSetDataSource = false;
        Log.d(str, "start}");
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public void stop() throws IllegalStateException {
        String str = TAG;
        Log.d(str, "stop {");
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            this.mPlayRequested = false;
        }
        Log.d(str, "stop }");
    }

    @Override // com.mrzero.tranplayer.VLCEvent.Listener
    public void onEvent(MediaPlayer.Event event) {
        Uri uri;
        Uri uri2;
        switch (event.type) {
            case 256:
                Log.d(TAG, "onEvent receive MediaChanged");
                break;
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
                Log.d(TAG, "onEvent unhandled event " + event.type);
                break;
            case MediaPlayer.Event.Opening /* 258 */:
                Log.d(TAG, "onEvent receive Opening");
                break;
            case MediaPlayer.Event.Buffering /* 259 */:
                float buffering = event.getBuffering();
                if (buffering != 100.0f) {
                    if (!this.mIsBuffering && isPlaying()) {
                        Log.d(TAG, "onEvent receive Start Buffering");
                        this.mIsBuffering = true;
                        notifyOnInfo(701, 0);
                    }
                    Uri uri3 = this.mUri;
                    if (uri3 != null && uri3.getScheme() != null && this.mUri.getScheme().startsWith("http")) {
                        notifyOnInfo(IMediaPlayer.MEDIA_INFO_BUFFERING_PERCENTAGE, (int) buffering);
                        break;
                    }
                } else if (this.mIsBuffering) {
                    this.mIsBuffering = false;
                    notifyOnInfo(702, 0);
                    Log.d(TAG, "onEvent receive End Buffering");
                    break;
                }
                break;
            case MediaPlayer.Event.Playing /* 260 */:
                Log.d(TAG, "onEvent receive playing event");
                notifyOnInfo(903, 0);
                break;
            case MediaPlayer.Event.Paused /* 261 */:
                Log.d(TAG, "onEvent receive Paused event");
                notifyOnInfo(904, 0);
                break;
            case MediaPlayer.Event.Stopped /* 262 */:
                Log.d(TAG, "onEvent receive Stopped event");
                notifyOnInfo(905, 0);
                break;
            case MediaPlayer.Event.EndReached /* 265 */:
                String str = TAG;
                Log.d(str, "onEvent receive EndReached");
                long duration = getDuration();
                long currentPosition = getCurrentPosition();
                if ((duration > 0 && duration - currentPosition <= 2000) || (uri = this.mUri) == null || uri.getScheme() == null || !this.mUri.getScheme().startsWith("http")) {
                    notifyOnCompletion();
                    break;
                } else {
                    Log.d(str, " currentposition " + currentPosition + " duration " + duration);
                    notifyOnError(1, -10105);
                    break;
                }
            case MediaPlayer.Event.EncounteredError /* 266 */:
                int errorCode = event.getErrorCode();
                Log.d(TAG, "onEvent receive EncounteredError " + errorCode);
                notifyOnError(1, errorCode);
                break;
            case MediaPlayer.Event.TimeChanged /* 267 */:
                long timeChanged = event.getTimeChanged();
                if (this.mCurrentPosition >= 0 && this.mSeekCompleteConunt > 0) {
                    boolean z10 = this.mHasVideo;
                    if (z10 && this.mIsFirstTimeChanged) {
                        Log.d(TAG, "onEvent receive SeekComplete video " + timeChanged);
                        this.mCurrentPosition = -1L;
                        if (this.mIsMute || this.mPaused) {
                            this.mMediaPlayer.setVolume(0);
                            this.mLastVolume = 0;
                        } else {
                            setVolumeFadeInOut(100, this.mLastVolume);
                            this.mLastVolume = 100;
                        }
                        notifyOnSeekComplete();
                        if (this.mSubtitleLoad) {
                            notifyOnTimedText(new TranTimedText("", 0L, 0L));
                        }
                    } else if (!z10 || this.mListenVideo) {
                        Log.d(TAG, "onEvent receive SeekComplete audio " + timeChanged);
                        this.mCurrentPosition = -1L;
                        if (this.mIsMute) {
                            this.mMediaPlayer.setVolume(0);
                            this.mLastVolume = 0;
                        } else {
                            setVolumeFadeInOut(100, this.mLastVolume);
                            this.mLastVolume = 100;
                        }
                        notifyOnSeekComplete();
                    }
                }
                this.mSeekCompleteConunt++;
                break;
            case MediaPlayer.Event.PositionChanged /* 268 */:
                event.getPositionChanged();
                break;
            case MediaPlayer.Event.SeekableChanged /* 269 */:
                boolean seekable = event.getSeekable();
                Log.d(TAG, "onEvent receive SeekableChanged " + seekable);
                break;
            case MediaPlayer.Event.PausableChanged /* 270 */:
                boolean pausable = event.getPausable();
                Log.d(TAG, "onEvent receive PausableChanged " + pausable);
                break;
            case 273:
                this.mDuration = event.getLengthChanged();
                notifyOnInfo(IMediaPlayer.MEDIA_AUDIO_TRACK_COUNT, getAudioTracksCount());
                Log.d(TAG, "onEvent receive LengthChanged " + this.mDuration);
                break;
            case MediaPlayer.Event.Vout /* 274 */:
                int voutCount = event.getVoutCount();
                this.mHasVideo = true;
                Log.d(TAG, "onEvent receive Vout " + voutCount);
                break;
            case MediaPlayer.Event.ESAdded /* 276 */:
            case MediaPlayer.Event.ESDeleted /* 277 */:
            case MediaPlayer.Event.ESSelected /* 278 */:
                int esChangedType = event.getEsChangedType();
                int esChangedID = event.getEsChangedID();
                Log.d(TAG, "onEvent receive ES " + esChangedType + "-1" + esChangedID);
                if (esChangedType != 1) {
                    if (esChangedType == 0) {
                        notifyOnInfo(IMediaPlayer.MEDIA_INFO_AUDIO_ES_CHANGED, esChangedID);
                        break;
                    }
                } else {
                    notifyOnInfo(IMediaPlayer.MEDIA_INFO_VIDEO_ES_CHANGED, esChangedID);
                    if (esChangedID >= 0 && !this.mFirstSetDataSource) {
                        try {
                            Thread.sleep(10L);
                        } catch (Exception unused) {
                        }
                        seekTo(getCurrentPosition(), false, false);
                        break;
                    }
                }
                break;
            case MediaPlayer.Event.FirstPicDisplay /* 286 */:
                String str2 = TAG;
                Log.d(str2, "onEvent receive FirstPicDisplay");
                this.mIsFirstTimeChanged = true;
                this.mMediaPlayer.setRefreshFirstFrame(false);
                if (this.mVideoStartTime > 0) {
                    long currentTimeMillis = System.currentTimeMillis() - this.mVideoStartTime;
                    Log.d(str2, "onEvent receive startvideotime:" + currentTimeMillis);
                    this.mVideoStartTime = 0L;
                    notifyOnInfo(IMediaPlayer.KPI_MEDIA_VIDEO_START_TIME, (int) currentTimeMillis);
                }
                if (this.mVideoSeekTime > 0) {
                    long currentTimeMillis2 = System.currentTimeMillis() - this.mVideoSeekTime;
                    Log.d(str2, "onEvent receive seektime:" + currentTimeMillis2);
                    this.mVideoSeekTime = 0L;
                    notifyOnInfo(IMediaPlayer.KPI_MEDIA_VIDEO_SEEK_TIME, (int) currentTimeMillis2);
                }
                notifyOnInfo(IMediaPlayer.MEDIA_INFO_FIRST_PIC_DISPLAY, 0);
                if (!this.mIsPlayingBeforeSeek) {
                    Log.d(str2, "mIsPlayingBeforeSeek pause ");
                    this.mMediaPlayer.pause();
                    break;
                }
                break;
            case MediaPlayer.Event.VideoSize /* 287 */:
                this.mVideoWidth = event.getVideoWidth();
                this.mVideoHeight = event.getVideoHeight();
                this.mSarNum = event.getVideoSarNum();
                this.mSarDen = event.getVideoSarDen();
                Log.d(TAG, "onEvent receive VideoSize " + this.mVideoWidth + "-1" + this.mVideoHeight + "-1" + this.mSarNum + "-1" + this.mSarDen);
                notifyOnVideoSizeChanged(this.mVideoWidth, this.mVideoHeight, this.mSarNum, this.mSarDen);
                break;
            case MediaPlayer.Event.LosePicSerious /* 288 */:
                Log.d(TAG, "onEvent receive LosePicSerious");
                notifyOnInfo(IMediaPlayer.MEDIA_INFO_LOSE_PIC_SERIOUS, 0);
                break;
            case MediaPlayer.Event.Unsupported /* 289 */:
                Log.d(TAG, "onEvent receive Unsupported");
                notifyOnError(-1010, 0);
                break;
            case MediaPlayer.Event.Prepared /* 290 */:
                Log.d(TAG, "onEvent receive Prepared");
                this.mIsprepareing = false;
                notifyOnPrepared();
                break;
            case MediaPlayer.Event.Subtitle /* 291 */:
                Log.d(TAG, "onEvent receive Subtitle " + event.getSubtitleStart() + "-" + event.getSubtitleDuration() + ":" + event.getSubtitleText());
                notifyOnTimedText(new TranTimedText(event.getSubtitleText(), (long) event.getSubtitleStart(), (long) event.getSubtitleDuration()));
                break;
            case MediaPlayer.Event.SubtitleLoad /* 292 */:
                Log.d(TAG, "onEvent receive SubtitleLoad " + event.getSubtitleLoadStatus());
                this.mSubtitleLoad = event.getSubtitleLoadStatus() == 1;
                notifyOnInfo(IMediaPlayer.MEDIA_INFO_SUBTITLE_LOAD_STATUS, event.getSubtitleLoadStatus());
                break;
            case MediaPlayer.Event.EofPaused /* 293 */:
                String str3 = TAG;
                Log.d(str3, "onEvent receive EofPaused");
                long duration2 = getDuration();
                long currentPosition2 = getCurrentPosition();
                if ((duration2 > 0 && duration2 - currentPosition2 <= 2000) || (uri2 = this.mUri) == null || uri2.getScheme() == null || !this.mUri.getScheme().startsWith("http")) {
                    if (!this.mIsFirstTimeChanged && ".mng".equals(getUriExtension())) {
                        notifyOnError(-1010, 0);
                        break;
                    } else {
                        this.mEofPaused = true;
                        this.mMediaPlayer.setVolume(0);
                        this.mLastVolume = 0;
                        this.mIsPlayingBeforeSeek = false;
                        notifyOnCompletion();
                        break;
                    }
                } else {
                    Log.d(str3, " currentposition " + currentPosition2 + " duration " + duration2);
                    notifyOnError(1, -10105);
                    break;
                }
            case MediaPlayer.Event.DisableVideo /* 294 */:
                Log.d(TAG, "onEvent receive DisableVideo");
                notifyOnInfo(IMediaPlayer.MEDIA_INFO_DISABLE_VIDEO, 0);
                break;
            case MediaPlayer.Event.DisableAudio /* 295 */:
                Log.d(TAG, "onEvent receive DisableAudio");
                notifyOnInfo(IMediaPlayer.MEDIA_INFO_DISABLE_AUDIO, 0);
                break;
            case MediaPlayer.Event.SubtitleType /* 296 */:
                int subtitleTypeId = event.getSubtitleTypeId();
                int subtitleType = event.getSubtitleType();
                Log.d(TAG, "onEvent receive spuid " + subtitleTypeId + "-1" + subtitleType);
                notifyOnSubTitleTypeChanged(subtitleTypeId, subtitleType);
                break;
            case MediaPlayer.Event.VideoCodecDeleted /* 297 */:
                int videoCodecStatus = event.getVideoCodecStatus();
                String str4 = TAG;
                Log.d(str4, "onEvent receive mVideoCodecDeleted codec: " + videoCodecStatus);
                if (videoCodecStatus != 1) {
                    this.mHadVideoCodec = true;
                    this.mVideoCodecDeleted = false;
                    break;
                } else {
                    this.mVideoCodecDeleted = true;
                    this.mHadVideoCodec = false;
                    if (this.bWillToSetSurface) {
                        Log.d(str4, "onEvent receive  bWillToSetSurface ");
                        setDisplay(this.mSurfaceHolder);
                    }
                    if (this.mPlayRequested) {
                        try {
                            Thread.sleep(70L);
                        } catch (Exception unused2) {
                        }
                        Log.d(TAG, "onEvent receive playRequested");
                        start();
                        break;
                    }
                }
                break;
            case MediaPlayer.Event.VideoFPS /* 298 */:
                this.mVideoFPS = event.getVideoFPS();
                Log.d(TAG, "onEvent receive mfps: " + this.mVideoFPS);
                break;
            case MediaPlayer.Event.VideoCodecInfo /* 299 */:
                this.mIsMediaCodec = event.getUseMediaCodec();
                this.mVideoCodecName = event.getVideoCodecName();
                Log.d(TAG, "onEvent receive mIsMediaCodec: " + this.mIsMediaCodec + " codec: " + event.getVideoCodecName());
                break;
        }
    }

    /* JADX WARN: Code restructure failed: missing block: B:54:0x00e0, code lost:
    
        if (0 == 0) goto L50;
     */
    @Override // com.mrzero.tranplayer.IMediaPlayer
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public void setDataSource(android.content.Context context, android.net.Uri uri, java.util.Map<java.lang.String, String> map) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        Objects.requireNonNull(context, "context param can not be null.");
        this.mUri = uri;
        this.mContext = context;
        if (map != null) {
            Object obj = map.get("listen-video");
            if (obj != null && obj.toString().compareTo("1") == 0) {
                this.mListenVideo = true;
            }
        }
        String scheme = uri.getScheme();
        if ("file".equals(scheme)) {
            setDataSource(uri.getPath(), map);
            return;
        }
        if ("content".equals(scheme) && "settings".equals(uri.getAuthority())) {
            Uri ringtone = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.getDefaultType(uri));
            if (ringtone == null) {
                throw new FileNotFoundException("Failed to resolve default ringtone");
            }
            uri = ringtone;
        }
        AssetFileDescriptor afd = null;
        try {
            afd = context.getContentResolver().openAssetFileDescriptor(uri, "r");
            if (afd == null) {
                return;
            }
            long declaredLength = afd.getDeclaredLength();
            if (declaredLength < 0) {
                if (this.mListenVideo) {
                    String path = getMediaSourcePath(uri);
                    if (path != null) {
                        setDataSource(path, map);
                    } else {
                        setDataSource(afd.getFileDescriptor(), map);
                    }
                } else {
                    setDataSource(afd.getFileDescriptor(), map);
                }
            } else {
                setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), declaredLength, map);
            }
            afd.close();
            return;
        } catch (IOException e) {
            Log.d(TAG, "setDataSource IOException " + e);
        } catch (SecurityException e) {
            Log.d(TAG, "setDataSource SecurityException " + e);
        } catch (Throwable t) {
            if (afd != null) {
                afd.close();
            }
            throw t;
        }
        if (afd != null) {
            afd.close();
        }
        setDataSource(uri.toString(), map);
    }

    public class FadeRunnable implements Runnable {
        private final int currentVolume;
        private final int lastVolume;
        private final long seekTime;

        public FadeRunnable(int i10, int i11) {
            this.currentVolume = i10;
            this.lastVolume = i11;
            this.seekTime = -1L;
        }

        @Override // java.lang.Runnable
        public void run() {
            synchronized (TranMediaPlayer.this.mLock) {
                boolean z10 = this.currentVolume > this.lastVolume;
                int i10 = z10 ? 10 : 3;
                Log.e(TranMediaPlayer.TAG, "setVolumeFadeInOut -- run -- Begin .");
                try {
                    Thread.sleep(z10 ? 300L : 100L);
                } catch (InterruptedException e10) {
                    e10.printStackTrace();
                }
                int i11 = this.lastVolume;
                while (true) {
                    Log.d(TranMediaPlayer.TAG, "setVolume .  volume_tmp:" + i11);
                    if (TranMediaPlayer.this.mMediaPlayer != null && !TranMediaPlayer.this.mIsVlcReleasing) {
                        try {
                            TranMediaPlayer.this.mMediaPlayer.setVolume(i11);
                        } catch (Exception unused) {
                        }
                    }
                    try {
                        Thread.sleep(i10);
                    } catch (InterruptedException e11) {
                        e11.printStackTrace();
                    }
                    i11 = z10 ? i11 + 10 : i11 - 10;
                    if ((z10 && i11 > this.currentVolume) || (!z10 && i11 < this.currentVolume)) {
                        break;
                    }
                }
                if (!z10 && this.seekTime != -1) {
                    try {
                        Thread.sleep(10L);
                    } catch (InterruptedException e12) {
                        e12.printStackTrace();
                    }
                    TranMediaPlayer.this.fadedOutToSeek(this.seekTime);
                }
            }
        }

        public FadeRunnable(int i10, int i11, long j10) {
            this.currentVolume = i10;
            this.lastVolume = i11;
            this.seekTime = j10;
        }
    }

    private void setVolumeFadeInOut(int i10, int i11) {
        setVolumeFadeInOut(i10, i11, -1L);
    }

    public void setTimedTextView(TextureView textureView) {
        String str = TAG;
        Log.d(str, "setTimedTextView textureView " + textureView + "-1" + this.mMediaPlayer + " {");
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        if (mediaPlayer != null) {
            this.mSubtitlesTextureView = null;
            if (textureView != null) {
                mediaPlayer.getVLCVout().setSubtitlesView(textureView);
            }
        } else {
            this.mSubtitlesTextureView = textureView;
        }
        Log.d(str, "setTimedTextView }");
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public void setDataSource(String str, Map<String, String> map) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        String str2 = TAG;
        Log.d(str2, "setDataSource path " + str + " {");
        parseHeader(map);
        String scheme = Uri.parse(str).getScheme();
        if (TextUtils.isEmpty(scheme)) {
            this.mSourceType = 2;
            this.mPath = str;
        } else if (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https") && !scheme.equalsIgnoreCase("rtsp")) {
            this.mSourceType = 2;
            if (str.contains("file://")) {
                this.mPath = Uri.parse(str).getPath();
            } else {
                this.mPath = str;
            }
        } else {
            this.mSourceType = 1;
            this.mUri = Uri.parse(str);
        }
        preInitPlayer();
        Log.d(str2, "setDataSource }");
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public void setDataSource(String str) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(str, (Map<String, String>) null);
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public void setDataSource(FileDescriptor fileDescriptor, long j10, long j11) throws IOException, IllegalArgumentException, IllegalStateException {
        setDataSource(fileDescriptor, j10, j11, null);
    }

    public void setDataSource(FileDescriptor fileDescriptor, long j10, long j11, Map<String, String> map) throws IOException, IllegalArgumentException, IllegalStateException {
        setDataSource(fileDescriptor, map);
        this.mOffset = j10;
        this.mLength = j11;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public void setDataSource(FileDescriptor fileDescriptor) throws IOException, IllegalArgumentException, IllegalStateException {
        setDataSource(fileDescriptor, (Map<String, String>) null);
    }

    public void setDataSource(FileDescriptor fileDescriptor, Map<String, String> map) throws IOException, IllegalArgumentException, IllegalStateException {
        Log.d(TAG, "setDataSource fd " + fileDescriptor + " {");
        parseHeader(map);
        this.mSourceType = 3;
        ParcelFileDescriptor parcelFileDescriptor = this.mFile;
        if (parcelFileDescriptor != null) {
            try {
                parcelFileDescriptor.close();
            } catch (IOException unused) {
            }
            this.mFile = null;
            this.mFd = -1;
        }
        ParcelFileDescriptor dup = ParcelFileDescriptor.dup(fileDescriptor);
        this.mFile = dup;
        this.mFd = dup.getFd();
        preInitPlayer();
        Log.d(TAG, "setDataSource }");
    }

    @Override // com.mrzero.tranplayer.AbstractMediaPlayer, com.mrzero.tranplayer.IMediaPlayer
    public void setDataSource(IMediaDataSource iMediaDataSource) throws IllegalArgumentException, SecurityException, IllegalStateException {
        Log.e(TAG, "interface setDataSource with IMediaDataSource is not implement, please use other setDataSource instead");
    }
}
