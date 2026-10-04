package com.mrzero.tranplayer.media;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaFormat;
import android.media.TimedText;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.mrzero.tranplayer.LibVLC;
import com.mrzero.tranplayer.Media;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Map;

/* loaded from: classes3.dex */
public class MediaPlayer {
    public static final int MEDIA_ERROR_IO = -1004;
    public static final int MEDIA_ERROR_MALFORMED = -1007;
    public static final int MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK = 200;
    public static final int MEDIA_ERROR_SERVER_DIED = 100;
    public static final int MEDIA_ERROR_TIMED_OUT = -110;
    public static final int MEDIA_ERROR_UNKNOWN = 1;
    public static final int MEDIA_ERROR_UNSUPPORTED = -1010;
    public static final int MEDIA_INFO_BAD_INTERLEAVING = 800;
    public static final int MEDIA_INFO_BUFFERING_END = 702;
    public static final int MEDIA_INFO_BUFFERING_START = 701;
    public static final int MEDIA_INFO_EXTERNAL_METADATA_UPDATE = 803;
    public static final int MEDIA_INFO_METADATA_UPDATE = 802;
    public static final int MEDIA_INFO_NOT_SEEKABLE = 801;
    public static final int MEDIA_INFO_STARTED_AS_NEXT = 2;
    public static final int MEDIA_INFO_SUBTITLE_TIMED_OUT = 902;
    public static final int MEDIA_INFO_TIMED_TEXT_ERROR = 900;
    public static final int MEDIA_INFO_UNKNOWN = 1;
    public static final int MEDIA_INFO_UNSUPPORTED_SUBTITLE = 901;
    public static final int MEDIA_INFO_VIDEO_RENDERING_START = 3;
    public static final int MEDIA_INFO_VIDEO_TRACK_LAGGING = 700;
    public static final String MEDIA_MIMETYPE_TEXT_SUBRIP = "application/x-subrip";
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT = 1;
    public static final int VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING = 2;
    private Media mCurrentMedia = null;
    private long mLength;
    private final LibVLC mLibVLC;
    private com.mrzero.tranplayer.MediaPlayer mMediaPlayer;
    private long mOffset;

    public interface OnBufferingUpdateListener {
        void onBufferingUpdate(MediaPlayer mediaPlayer, int i10);
    }

    public interface OnCompletionListener {
        void onCompletion(MediaPlayer mediaPlayer);
    }

    public interface OnErrorListener {
        boolean onError(MediaPlayer mediaPlayer, int i10, int i11);
    }

    public interface OnInfoListener {
        boolean onInfo(MediaPlayer mediaPlayer, int i10, int i11);
    }

    public interface OnPreparedListener {
        void onPrepared(MediaPlayer mediaPlayer);
    }

    public interface OnSeekCompleteListener {
        void onSeekComplete(MediaPlayer mediaPlayer);
    }

    public interface OnTimedTextListener {
        void onTimedText(MediaPlayer mediaPlayer, TimedText timedText);
    }

    public interface OnVideoSizeChangedListener {
        void onVideoSizeChanged(MediaPlayer mediaPlayer, int i10, int i11);
    }

    public static class TrackInfo implements Parcelable {
        public static final int MEDIA_TRACK_TYPE_AUDIO = 2;
        public static final int MEDIA_TRACK_TYPE_SUBTITLE = 4;
        public static final int MEDIA_TRACK_TYPE_TIMEDTEXT = 3;
        public static final int MEDIA_TRACK_TYPE_UNKNOWN = 0;
        public static final int MEDIA_TRACK_TYPE_VIDEO = 1;

        public TrackInfo(Parcel parcel) {
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        public MediaFormat getFormat() {
            return null;
        }

        public String getLanguage() {
            return "und";
        }

        public int getTrackType() {
            return 0;
        }

        public String toString() {
            return "";
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel parcel, int i10) {
        }
    }

    public MediaPlayer() {
        LibVLC libVLC = new LibVLC(null);
        this.mLibVLC = libVLC;
        this.mMediaPlayer = new com.mrzero.tranplayer.MediaPlayer(libVLC);
        this.mOffset = -1L;
        this.mLength = -1L;
    }

    public static MediaPlayer create(Context context, int i10, AudioAttributes audioAttributes, int i11) {
        return null;
    }

    public static MediaPlayer create(Context context, Uri uri) {
        return create(context, uri, null);
    }

    public void addTimedTextSource(FileDescriptor fileDescriptor, long j10, long j11, String str) throws IllegalArgumentException, IllegalStateException {
    }

    public void addTimedTextSource(FileDescriptor fileDescriptor, String str) throws IllegalArgumentException, IllegalStateException {
    }

    public void addTimedTextSource(String str, String str2) {
        this.mMediaPlayer.addSlave(0, str, false);
    }

    public void attachAuxEffect(int i10) {
    }

    public void deselectTrack(int i10) throws IllegalStateException {
    }

    public void finalize() {
    }

    public int getAudioSessionId() {
        return 0;
    }

    public int getCurrentPosition() {
        return (int) this.mMediaPlayer.getTime();
    }

    public int getDuration() {
        return (int) this.mMediaPlayer.getLength();
    }

    public int getSelectedTrack(int i10) throws IllegalStateException {
        return 0;
    }

    public TrackInfo[] getTrackInfo() throws IllegalStateException {
        return new TrackInfo[1];
    }

    public int getVideoHeight() {
        return -1;
    }

    public int getVideoWidth() {
        return -1;
    }

    public boolean isLooping() {
        return false;
    }

    public boolean isPlaying() {
        return this.mMediaPlayer.isPlaying();
    }

    public void pause() throws IllegalStateException {
        this.mMediaPlayer.pause();
    }

    public void prepare() throws IOException, IllegalStateException {
    }

    public void prepareAsync() {
        this.mCurrentMedia.addOption(":video-paused");
        this.mMediaPlayer.play();
    }

    public void release() {
        this.mMediaPlayer.release();
    }

    public void reset() {
    }

    public void seekTo(int i10) throws IllegalStateException {
    }

    public void selectTrack(int i10) throws IllegalStateException {
    }

    public void setAudioAttributes(AudioAttributes audioAttributes) throws IllegalArgumentException {
    }

    public void setAudioSessionId(int i10) throws IllegalArgumentException, IllegalStateException {
    }

    public void setAudioStreamType(int i10) {
    }

    public void setAuxEffectSendLevel(float f10) {
    }

    public void setDataSource(Context context, Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(context, uri, (Map<String, String>) null);
    }

    public void setDisplay(SurfaceHolder surfaceHolder) {
        this.mMediaPlayer.getVLCVout().setVideoSurface(surfaceHolder.getSurface(), surfaceHolder);
    }

    public void setLooping(boolean z10) {
    }

    public void setNextMediaPlayer(MediaPlayer mediaPlayer) {
    }

    public void setOnBufferingUpdateListener(OnBufferingUpdateListener onBufferingUpdateListener) {
    }

    public void setOnCompletionListener(OnCompletionListener onCompletionListener) {
    }

    public void setOnErrorListener(OnErrorListener onErrorListener) {
    }

    public void setOnInfoListener(OnInfoListener onInfoListener) {
    }

    public void setOnPreparedListener(OnPreparedListener onPreparedListener) {
    }

    public void setOnSeekCompleteListener(OnSeekCompleteListener onSeekCompleteListener) {
    }

    public void setOnTimedTextListener(OnTimedTextListener onTimedTextListener) {
    }

    public void setOnVideoSizeChangedListener(OnVideoSizeChangedListener onVideoSizeChangedListener) {
    }

    public void setScreenOnWhilePlaying(boolean z10) {
    }

    public void setSurface(Surface surface) {
        this.mMediaPlayer.getVLCVout().setVideoSurface(surface, null);
    }

    public void setVideoScalingMode(int i10) {
    }

    public void setVolume(float f10, float f11) {
        this.mMediaPlayer.setVolume((int) (((f10 + f11) * 100.0f) / 2.0f));
    }

    public void setWakeMode(Context context, int i10) {
    }

    public void start() throws IllegalStateException {
        this.mMediaPlayer.play();
    }

    public void stop() throws IllegalStateException {
        this.mMediaPlayer.stop();
    }

    public static MediaPlayer create(Context context, Uri uri, SurfaceHolder surfaceHolder) {
        return create(context, uri, surfaceHolder, null, 0);
    }

    public void addTimedTextSource(Context context, Uri uri, String str) {
        this.mMediaPlayer.addSlave(0, uri, false);
    }

    public void setDataSource(Context context, Uri uri, Map<String, String> map) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        Media media = new Media(this.mLibVLC, uri);
        this.mCurrentMedia = media;
        this.mMediaPlayer.setMedia(media);
    }

    public static MediaPlayer create(Context context, Uri uri, SurfaceHolder surfaceHolder, AudioAttributes audioAttributes, int i10) {
        return new MediaPlayer();
    }

    public static MediaPlayer create(Context context, int i10) {
        return create(context, i10, null, 0);
    }

    public void setDataSource(String str) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        Media media = new Media(this.mLibVLC, str);
        this.mCurrentMedia = media;
        this.mMediaPlayer.setMedia(media);
    }

    public void setDataSource(FileDescriptor fileDescriptor) throws IOException, IllegalArgumentException, IllegalStateException {
        Media media = new Media(this.mLibVLC, fileDescriptor, this.mOffset, this.mLength);
        this.mCurrentMedia = media;
        this.mMediaPlayer.setMedia(media);
    }

    public void setDataSource(FileDescriptor fileDescriptor, long j10, long j11) throws IOException, IllegalArgumentException, IllegalStateException {
        this.mOffset = j10;
        this.mLength = j11;
        setDataSource(fileDescriptor);
    }
}
