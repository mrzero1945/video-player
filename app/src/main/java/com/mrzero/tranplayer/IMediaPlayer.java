package com.mrzero.tranplayer;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.view.Surface;
import android.view.SurfaceHolder;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Map;

/* loaded from: classes3.dex */
public interface IMediaPlayer {
    public static final int KPI_MEDIA_VIDEO_SEEK_TIME = 100012;
    public static final int KPI_MEDIA_VIDEO_START_TIME = 100011;
    public static final int MEDIA_AUDIO_TRACK_COUNT = 100013;
    public static final int MEDIA_ERROR_IO = -1004;
    public static final int MEDIA_ERROR_MALFORMED = -1007;
    public static final int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;
    public static final int MEDIA_ERROR_SERVER_DIED = 100;
    public static final int MEDIA_ERROR_TIMED_OUT = -110;
    public static final int MEDIA_ERROR_UNKNOWN = 1;
    public static final int MEDIA_ERROR_UNSUPPORTED = -1010;
    public static final int MEDIA_INFO_AUDIO_DECODED_START = 10003;
    public static final int MEDIA_INFO_AUDIO_ES_CHANGED = 100007;
    public static final int MEDIA_INFO_AUDIO_RENDERING_START = 10002;
    public static final int MEDIA_INFO_AUDIO_SEEK_RENDERING_START = 10009;
    public static final int MEDIA_INFO_BAD_INTERLEAVING = 800;
    public static final int MEDIA_INFO_BUFFERING_END = 702;
    public static final int MEDIA_INFO_BUFFERING_PERCENTAGE = 100003;
    public static final int MEDIA_INFO_BUFFERING_START = 701;
    public static final int MEDIA_INFO_COMPONENT_OPEN = 10007;
    public static final int MEDIA_INFO_DISABLE_AUDIO = 100010;
    public static final int MEDIA_INFO_DISABLE_VIDEO = 100009;
    public static final int MEDIA_INFO_FIND_STREAM_INFO = 10006;
    public static final int MEDIA_INFO_FIRST_PIC_DISPLAY = 100001;
    public static final int MEDIA_INFO_LOSE_PIC_SERIOUS = 100002;
    public static final int MEDIA_INFO_MEDIA_ACCURATE_SEEK_COMPLETE = 10100;
    public static final int MEDIA_INFO_METADATA_UPDATE = 802;
    public static final int MEDIA_INFO_NETWORK_BANDWIDTH = 703;
    public static final int MEDIA_INFO_NOT_SEEKABLE = 801;
    public static final int MEDIA_INFO_OPEN_INPUT = 10005;
    public static final int MEDIA_INFO_PLAYER_PAUSED = 904;
    public static final int MEDIA_INFO_PLAYER_PLAYING = 903;
    public static final int MEDIA_INFO_PLAYER_STOPPED = 905;
    public static final int MEDIA_INFO_REPLAY = 100005;
    public static final int MEDIA_INFO_SNAP_SHOT = 100008;
    public static final int MEDIA_INFO_STARTED_AS_NEXT = 2;
    public static final int MEDIA_INFO_SUBTITLE_LOAD_STATUS = 100004;
    public static final int MEDIA_INFO_SUBTITLE_TIMED_OUT = 902;
    public static final int MEDIA_INFO_TIMED_TEXT_ERROR = 900;
    public static final int MEDIA_INFO_UNKNOWN = 1;
    public static final int MEDIA_INFO_UNSUPPORTED_SUBTITLE = 901;
    public static final int MEDIA_INFO_VIDEO_DECODED_START = 10004;
    public static final int MEDIA_INFO_VIDEO_ES_CHANGED = 100006;
    public static final int MEDIA_INFO_VIDEO_RENDERING_START = 3;
    public static final int MEDIA_INFO_VIDEO_ROTATION_CHANGED = 10001;
    public static final int MEDIA_INFO_VIDEO_SEEK_RENDERING_START = 10008;
    public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;

    public interface OnBufferingUpdateListener {
        void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i10);
    }

    public interface OnCompletionListener {
        void onCompletion(IMediaPlayer iMediaPlayer);
    }

    public interface OnErrorListener {
        boolean onError(IMediaPlayer iMediaPlayer, int i10, int i11);
    }

    public interface OnInfoListener {
        boolean onInfo(IMediaPlayer iMediaPlayer, int i10, int i11);
    }

    public interface OnPreparedListener {
        void onPrepared(IMediaPlayer iMediaPlayer);
    }

    public interface OnSeekCompleteListener {
        void onSeekComplete(IMediaPlayer iMediaPlayer);
    }

    public interface OnSubTitleTypeListener {
        void onSubTitleType(IMediaPlayer iMediaPlayer, int i10, int i11);
    }

    public interface OnTimedTextListener {
        void onTimedText(IMediaPlayer iMediaPlayer, ITimedText iTimedText);
    }

    public interface OnVideoSizeChangedListener {
        void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i10, int i11, int i12, int i13);
    }

    void addTimedTextSource(String str, String str2) throws IOException, IllegalArgumentException, IllegalStateException;

    void deselectTrack(int i10);

    int getAudioSessionId();

    long getCurrentPosition();

    String getDataSource();

    long getDuration();

    boolean getListenVideoMode();

    MediaInfo getMediaInfo();

    int getSelectedTrack(int i10);

    float getSpeed();

    long getTimedTextDelay();

    ITrackInfo[] getTrackInfo();

    int getVideoHeight();

    int getVideoSarDen();

    int getVideoSarNum();

    int getVideoWidth();

    boolean isLooping();

    boolean isPlaying();

    void pause() throws IllegalStateException;

    void prepare() throws IOException, IllegalStateException;

    void prepareAsync() throws IllegalStateException;

    void release();

    void reset();

    void seekTo(long j10, boolean z10, boolean z11) throws IllegalStateException;

    void selectTrack(int i10);

    void setAudioStreamType(int i10);

    void setDataSource(Context context, Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    @TargetApi(14)
    void setDataSource(Context context, Uri uri, Map<String, String> map) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    void setDataSource(IMediaDataSource iMediaDataSource);

    void setDataSource(FileDescriptor fileDescriptor) throws IOException, IllegalArgumentException, IllegalStateException;

    void setDataSource(FileDescriptor fileDescriptor, long j10, long j11) throws IOException, IllegalArgumentException, IllegalStateException;

    void setDataSource(String str) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    void setDataSource(String str, Map<String, String> map) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

    void setDisplay(SurfaceHolder surfaceHolder);

    void setListenVideoMode(boolean z10);

    void setLooping(boolean z10);

    void setOnBufferingUpdateListener(OnBufferingUpdateListener onBufferingUpdateListener);

    void setOnCompletionListener(OnCompletionListener onCompletionListener);

    void setOnErrorListener(OnErrorListener onErrorListener);

    void setOnInfoListener(OnInfoListener onInfoListener);

    void setOnPreparedListener(OnPreparedListener onPreparedListener);

    void setOnSeekCompleteListener(OnSeekCompleteListener onSeekCompleteListener);

    void setOnTimedTextListener(OnTimedTextListener onTimedTextListener);

    void setOnVideoSizeChangedListener(OnVideoSizeChangedListener onVideoSizeChangedListener);

    void setScreenOnWhilePlaying(boolean z10);

    void setSpeed(float f10);

    void setSurface(Surface surface);

    void setTimedTextDelay(long j10);

    void setVolume(float f10, float f11);

    void start() throws IllegalStateException;

    void stop() throws IllegalStateException;
}
