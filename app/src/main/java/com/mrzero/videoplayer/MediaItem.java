package com.mrzero.videoplayer;

import android.net.Uri;

public class MediaItem {

    public final long id;
    public final String title;
    public final String path;
    public final Uri uri;
    public final long duration;
    public final long size;
    public final long dateAdded;

    public MediaItem(long id, String title, String path, Uri uri, long duration, long size, long dateAdded) {
        this.id = id;
        this.title = title;
        this.path = path;
        this.uri = uri;
        this.duration = duration;
        this.size = size;
        this.dateAdded = dateAdded;
    }
}
