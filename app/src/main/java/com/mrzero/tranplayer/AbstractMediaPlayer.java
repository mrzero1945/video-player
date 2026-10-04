package com.mrzero.tranplayer;

import com.mrzero.tranplayer.IMediaPlayer;

/* loaded from: classes3.dex */
public abstract class AbstractMediaPlayer implements IMediaPlayer {
    private IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;
    private IMediaPlayer.OnCompletionListener mOnCompletionListener;
    private IMediaPlayer.OnErrorListener mOnErrorListener;
    private IMediaPlayer.OnInfoListener mOnInfoListener;
    private IMediaPlayer.OnPreparedListener mOnPreparedListener;
    private IMediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener;
    private IMediaPlayer.OnSubTitleTypeListener mOnSubTitleTypeListener;
    private IMediaPlayer.OnTimedTextListener mOnTimedTextListener;
    private IMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener;

    public final void notifyOnBufferingUpdate(int i10) {
        IMediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener = this.mOnBufferingUpdateListener;
        if (onBufferingUpdateListener != null) {
            onBufferingUpdateListener.onBufferingUpdate(this, i10);
        }
    }

    public final void notifyOnCompletion() {
        IMediaPlayer.OnCompletionListener onCompletionListener = this.mOnCompletionListener;
        if (onCompletionListener != null) {
            onCompletionListener.onCompletion(this);
        }
    }

    public final boolean notifyOnError(int i10, int i11) {
        IMediaPlayer.OnErrorListener onErrorListener = this.mOnErrorListener;
        return onErrorListener != null && onErrorListener.onError(this, i10, i11);
    }

    public final boolean notifyOnInfo(int i10, int i11) {
        IMediaPlayer.OnInfoListener onInfoListener = this.mOnInfoListener;
        return onInfoListener != null && onInfoListener.onInfo(this, i10, i11);
    }

    public final void notifyOnPrepared() {
        IMediaPlayer.OnPreparedListener onPreparedListener = this.mOnPreparedListener;
        if (onPreparedListener != null) {
            onPreparedListener.onPrepared(this);
        }
    }

    public final void notifyOnSeekComplete() {
        IMediaPlayer.OnSeekCompleteListener onSeekCompleteListener = this.mOnSeekCompleteListener;
        if (onSeekCompleteListener != null) {
            onSeekCompleteListener.onSeekComplete(this);
        }
    }

    public final void notifyOnSubTitleTypeChanged(int i10, int i11) {
        IMediaPlayer.OnSubTitleTypeListener onSubTitleTypeListener = this.mOnSubTitleTypeListener;
        if (onSubTitleTypeListener != null) {
            onSubTitleTypeListener.onSubTitleType(this, i10, i11);
        }
    }

    public final void notifyOnTimedText(ITimedText iTimedText) {
        IMediaPlayer.OnTimedTextListener onTimedTextListener = this.mOnTimedTextListener;
        if (onTimedTextListener != null) {
            onTimedTextListener.onTimedText(this, iTimedText);
        }
    }

    public final void notifyOnVideoSizeChanged(int i10, int i11, int i12, int i13) {
        IMediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener = this.mOnVideoSizeChangedListener;
        if (onVideoSizeChangedListener != null) {
            onVideoSizeChangedListener.onVideoSizeChanged(this, i10, i11, i12, i13);
        }
    }

    public void resetListeners() {
        this.mOnPreparedListener = null;
        this.mOnBufferingUpdateListener = null;
        this.mOnCompletionListener = null;
        this.mOnSeekCompleteListener = null;
        this.mOnVideoSizeChangedListener = null;
        this.mOnErrorListener = null;
        this.mOnInfoListener = null;
        this.mOnTimedTextListener = null;
        this.mOnSubTitleTypeListener = null;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public void setDataSource(IMediaDataSource iMediaDataSource) {
        throw new UnsupportedOperationException();
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public final void setOnBufferingUpdateListener(IMediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener) {
        this.mOnBufferingUpdateListener = onBufferingUpdateListener;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public final void setOnCompletionListener(IMediaPlayer.OnCompletionListener onCompletionListener) {
        this.mOnCompletionListener = onCompletionListener;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public final void setOnErrorListener(IMediaPlayer.OnErrorListener onErrorListener) {
        this.mOnErrorListener = onErrorListener;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public final void setOnInfoListener(IMediaPlayer.OnInfoListener onInfoListener) {
        this.mOnInfoListener = onInfoListener;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public final void setOnPreparedListener(IMediaPlayer.OnPreparedListener onPreparedListener) {
        this.mOnPreparedListener = onPreparedListener;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public final void setOnSeekCompleteListener(IMediaPlayer.OnSeekCompleteListener onSeekCompleteListener) {
        this.mOnSeekCompleteListener = onSeekCompleteListener;
    }

    public final void setOnSubTitleTypeListener(IMediaPlayer.OnSubTitleTypeListener onSubTitleTypeListener) {
        this.mOnSubTitleTypeListener = onSubTitleTypeListener;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public final void setOnTimedTextListener(IMediaPlayer.OnTimedTextListener onTimedTextListener) {
        this.mOnTimedTextListener = onTimedTextListener;
    }

    @Override // com.mrzero.tranplayer.IMediaPlayer
    public final void setOnVideoSizeChangedListener(IMediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener) {
        this.mOnVideoSizeChangedListener = onVideoSizeChangedListener;
    }
}
