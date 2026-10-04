package com.mrzero.tranplayer;

import android.graphics.Rect;

/* loaded from: classes3.dex */
public interface ITimedText {
    Rect getBounds();

    long getDuration();

    long getStartTime();

    String getText();
}
