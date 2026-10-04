package com.mrzero.videoplayer;

import java.util.ArrayList;
import java.util.List;

public class FolderItem {

    public final String path;
    public final String name;
    public final List<MediaItem> videos = new ArrayList<>();

    public FolderItem(String path, String name) {
        this.path = path;
        this.name = name;
    }

    /** Video terbaru di folder ini (daftar urut DATE_ADDED DESC). */
    public MediaItem newest() {
        return videos.isEmpty() ? null : videos.get(0);
    }
}
