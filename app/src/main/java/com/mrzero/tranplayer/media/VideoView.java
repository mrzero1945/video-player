package com.mrzero.tranplayer.media;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.MediaController;
import com.mrzero.tranplayer.LibVLC;
import com.mrzero.tranplayer.Media;
import java.io.InputStream;
import java.util.Map;

/* loaded from: classes3.dex */
public class VideoView extends SurfaceView implements MediaController.MediaPlayerControl {
    private static LibVLC sLibVLC;

    public VideoView(Context context) {
        super(context);
        sLibVLC = new LibVLC(context, null);
    }

    public void addSubtitleSource(InputStream inputStream, MediaFormat mediaFormat) {
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public boolean canPause() {
        return false;
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public boolean canSeekBackward() {
        return false;
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public boolean canSeekForward() {
        return false;
    }

    @Override // android.view.SurfaceView, android.view.View
    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    @TargetApi(18)
    public int getAudioSessionId() {
        return 0;
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public int getBufferPercentage() {
        return 0;
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public int getCurrentPosition() {
        return 0;
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public int getDuration() {
        return -1;
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public boolean isPlaying() {
        return false;
    }

    @Override // android.view.SurfaceView, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override // android.view.SurfaceView, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override // android.view.View
    @TargetApi(14)
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
    }

    @Override // android.view.View
    @TargetApi(14)
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
    }

    @Override // android.view.View, android.view.KeyEvent.Callback
    public boolean onKeyDown(int i10, KeyEvent keyEvent) {
        return super.onKeyDown(i10, keyEvent);
    }

    @Override // android.view.View
    public void onLayout(boolean z10, int i10, int i11, int i12, int i13) {
        super.onLayout(z10, i10, i11, i12, i13);
    }

    @Override // android.view.SurfaceView, android.view.View
    public void onMeasure(int i10, int i11) {
        super.onMeasure(i10, i11);
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return super.onTouchEvent(motionEvent);
    }

    @Override // android.view.View
    public boolean onTrackballEvent(MotionEvent motionEvent) {
        return super.onTrackballEvent(motionEvent);
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public void pause() {
    }

    public int resolveAdjustedSize(int i10, int i11) {
        return SurfaceView.getDefaultSize(i10, i11);
    }

    public void resume() {
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public void seekTo(int i10) {
    }

    public void setMediaController(MediaController mediaController) {
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener onCompletionListener) {
    }

    public void setOnErrorListener(MediaPlayer.OnErrorListener onErrorListener) {
    }

    @TargetApi(17)
    public void setOnInfoListener(MediaPlayer.OnInfoListener onInfoListener) {
    }

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener onPreparedListener) {
    }

    public void setVideoPath(String str) {
        new Media(sLibVLC, str);
    }

    public void setVideoURI(Uri uri) {
        new Media(sLibVLC, uri);
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public void start() {
    }

    public void stopPlayback() {
    }

    public void suspend() {
    }

    @TargetApi(21)
    public void setVideoURI(Uri uri, Map<String, String> map) {
        setVideoURI(uri);
    }

    public VideoView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public VideoView(Context context, AttributeSet attributeSet, int i10) {
        this(context, attributeSet, i10, 0);
    }

    @TargetApi(21)
    public VideoView(Context context, AttributeSet attributeSet, int i10, int i11) {
        super(context, attributeSet, i10, i11);
    }
}
