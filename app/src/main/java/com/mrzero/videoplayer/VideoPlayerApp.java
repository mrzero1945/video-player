package com.mrzero.videoplayer;

import android.app.Application;

public class VideoPlayerApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // LibVLC + FFmpeg natives are loaded lazily by com.mrzero.tranplayer.LibVLC
    }
}
