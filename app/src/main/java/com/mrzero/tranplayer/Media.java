package com.mrzero.tranplayer;

import android.net.Uri;
import androidx.annotation.Nullable;
import com.mrzero.tranplayer.VLCEvent;
import com.mrzero.tranplayer.util.AndroidUtil;
import com.mrzero.tranplayer.util.HWDecoderUtil;
import com.mrzero.tranplayer.util.VLCUtil;
import java.io.FileDescriptor;

/* loaded from: classes3.dex */
public class Media extends VLCObject<Media.Event> {
    private static final int PARSE_STATUS_INIT = 0;
    private static final int PARSE_STATUS_PARSED = 2;
    private static final int PARSE_STATUS_PARSING = 1;
    private static final String TAG = "LibVLC/Media";
    private boolean mCodecOptionSet;
    private long mDuration;
    private boolean mFileCachingSet;
    private final String[] mNativeMetas;
    private Track[] mNativeTracks;
    private boolean mNetworkCachingSet;
    private int mParseStatus;
    private int mState;
    private MediaList mSubItems;
    private int mType;
    private Uri mUri;

    public static class AudioTrack extends Track {
        public final int channels;
        public final int rate;

        private AudioTrack(String str, String str2, int i10, int i11, int i12, int i13, String str3, String str4, int i14, int i15) {
            super(0, str, str2, i10, i11, i12, i13, str3, str4);
            this.channels = i14;
            this.rate = i15;
        }
    }

    public static class Event extends VLCEvent {
        public static final int DurationChanged = 2;
        public static final int MetaChanged = 0;
        public static final int ParsedChanged = 3;
        public static final int StateChanged = 5;
        public static final int SubItemAdded = 1;
        public static final int SubItemTreeAdded = 6;

        public Event(int i10) {
            super(i10);
        }

        public int getMetaId() {
            return (int) this.arg1;
        }

        public int getParsedStatus() {
            return (int) this.arg1;
        }

        public Event(int i10, long j10) {
            super(i10, j10);
        }
    }

    public interface EventListener extends VLCEvent.Listener<Event> {
    }

    public static class Meta {
        public static final int Actors = 22;
        public static final int Album = 4;
        public static final int AlbumArtist = 23;
        public static final int Artist = 1;
        public static final int ArtworkURL = 15;
        public static final int Copyright = 3;
        public static final int Date = 8;
        public static final int Description = 6;
        public static final int Director = 18;
        public static final int DiscNumber = 24;
        public static final int EncodedBy = 14;
        public static final int Episode = 20;
        public static final int Genre = 2;
        public static final int Language = 11;
        public static final int MAX = 25;
        public static final int NowPlaying = 12;
        public static final int Publisher = 13;
        public static final int Rating = 7;
        public static final int Season = 19;
        public static final int Setting = 9;
        public static final int ShowName = 21;
        public static final int Title = 0;
        public static final int TrackID = 16;
        public static final int TrackNumber = 5;
        public static final int TrackTotal = 17;
        public static final int URL = 10;
    }

    public static class Parse {
        public static final int DoInteract = 8;
        public static final int FetchLocal = 2;
        public static final int FetchNetwork = 4;
        public static final int ParseLocal = 0;
        public static final int ParseNetwork = 1;
    }

    public static class ParsedStatus {
        public static final int Done = 4;
        public static final int Failed = 2;
        public static final int Skipped = 1;
        public static final int Timeout = 3;
    }

    public static class Slave {
        public final int priority;
        public final int type;
        public final String uri;

        public static class Type {
            public static final int Audio = 1;
            public static final int Subtitle = 0;
        }

        public Slave(int i10, int i11, String str) {
            this.type = i10;
            this.priority = i11;
            this.uri = str;
        }
    }

    public static class State {
        public static final int Ended = 6;
        public static final int Error = 7;
        public static final int MAX = 8;
        public static final int NothingSpecial = 0;
        public static final int Opening = 1;
        public static final int Paused = 4;
        public static final int Playing = 3;
        public static final int Stopped = 5;
    }

    public static class Stats {
        public final int decodedAudio;
        public final int decodedVideo;
        public final float demuxBitrate;
        public final int demuxCorrupted;
        public final int demuxDiscontinuity;
        public final int demuxReadBytes;
        public final int displayedPictures;
        public final float inputBitrate;
        public final int lostAbuffers;
        public final int lostPictures;
        public final int playedAbuffers;
        public final int readBytes;
        public final float sendBitrate;
        public final int sentBytes;
        public final int sentPackets;

        public Stats(int i10, float f10, int i11, float f11, int i12, int i13, int i14, int i15, int i16, int i17, int i18, int i19, int i20, int i21, float f12) {
            this.readBytes = i10;
            this.inputBitrate = f10;
            this.demuxReadBytes = i11;
            this.demuxBitrate = f11;
            this.demuxCorrupted = i12;
            this.demuxDiscontinuity = i13;
            this.decodedVideo = i14;
            this.decodedAudio = i15;
            this.displayedPictures = i16;
            this.lostPictures = i17;
            this.playedAbuffers = i18;
            this.lostAbuffers = i19;
            this.sentPackets = i20;
            this.sentBytes = i21;
            this.sendBitrate = f12;
        }
    }

    public static class SubtitleTrack extends Track {
        public final String encoding;

        private SubtitleTrack(String str, String str2, int i10, int i11, int i12, int i13, String str3, String str4, String str5) {
            super(2, str, str2, i10, i11, i12, i13, str3, str4);
            this.encoding = str5;
        }
    }

    public static abstract class Track {
        public final int bitrate;
        public final String codec;
        public final String description;

        /* renamed from: id, reason: collision with root package name */
        public final int f9412id;
        public final String language;
        public final int level;
        public final String originalCodec;
        public final int profile;
        public final int type;

        public static class Type {
            public static final int Audio = 0;
            public static final int Text = 2;
            public static final int Unknown = -1;
            public static final int Video = 1;
        }

        private Track(int i10, String str, String str2, int i11, int i12, int i13, int i14, String str3, String str4) {
            this.type = i10;
            this.codec = str;
            this.originalCodec = str2;
            this.f9412id = i11;
            this.profile = i12;
            this.level = i13;
            this.bitrate = i14;
            this.language = str3;
            this.description = str4;
        }
    }

    public static class Type {
        public static final int Directory = 2;
        public static final int Disc = 3;
        public static final int File = 1;
        public static final int Playlist = 5;
        public static final int Stream = 4;
        public static final int Unknown = 0;
    }

    public static class UnknownTrack extends Track {
        private UnknownTrack(String str, String str2, int i10, int i11, int i12, int i13, String str3, String str4) {
            super(-1, str, str2, i10, i11, i12, i13, str3, str4);
        }
    }

    public static class VideoTrack extends Track {
        public final int frameRateDen;
        public final int frameRateNum;
        public final int height;
        public final int orientation;
        public final int projection;
        public final int sarDen;
        public final int sarNum;
        public final int width;

        public static final class Orientation {
            public static final int BottomLeft = 2;
            public static final int BottomRight = 3;
            public static final int LeftBottom = 5;
            public static final int LeftTop = 4;
            public static final int RightBottom = 7;
            public static final int RightTop = 6;
            public static final int TopLeft = 0;
            public static final int TopRight = 1;
        }

        public static final class Projection {
            public static final int CubemapLayoutStandard = 256;
            public static final int EquiRectangular = 1;
            public static final int Rectangular = 0;
        }

        private VideoTrack(String str, String str2, int i10, int i11, int i12, int i13, String str3, String str4, int i14, int i15, int i16, int i17, int i18, int i19, int i20, int i21) {
            super(1, str, str2, i10, i11, i12, i13, str3, str4);
            this.height = i14;
            this.width = i15;
            this.sarNum = i16;
            this.sarDen = i17;
            this.frameRateNum = i18;
            this.frameRateDen = i19;
            this.orientation = i20;
            this.projection = i21;
        }
    }

    public Media(LibVLC libVLC, String str) {
        super(libVLC);
        this.mUri = null;
        this.mSubItems = null;
        this.mParseStatus = 0;
        this.mNativeMetas = new String[25];
        this.mNativeTracks = null;
        this.mDuration = -1L;
        this.mState = -1;
        this.mType = -1;
        this.mCodecOptionSet = false;
        this.mFileCachingSet = false;
        this.mNetworkCachingSet = false;
        nativeNewFromPath(libVLC, str);
        this.mUri = VLCUtil.UriFromMrl(nativeGetMrl());
    }

    private static Track createAudioTrackFromNative(String str, String str2, int i10, int i11, int i12, int i13, String str3, String str4, int i14, int i15) {
        return new AudioTrack(str, str2, i10, i11, i12, i13, str3, str4, i14, i15);
    }

    private static Slave createSlaveFromNative(int i10, int i11, String str) {
        return new Slave(i10, i11, str);
    }

    private static Stats createStatsFromNative(int i10, float f10, int i11, float f11, int i12, int i13, int i14, int i15, int i16, int i17, int i18, int i19, int i20, int i21, float f12) {
        return new Stats(i10, f10, i11, f11, i12, i13, i14, i15, i16, i17, i18, i19, i20, i21, f12);
    }

    private static Track createSubtitleTrackFromNative(String str, String str2, int i10, int i11, int i12, int i13, String str3, String str4, String str5) {
        return new SubtitleTrack(str, str2, i10, i11, i12, i13, str3, str4, str5);
    }

    private static Track createUnknownTrackFromNative(String str, String str2, int i10, int i11, int i12, int i13, String str3, String str4) {
        return new UnknownTrack(str, str2, i10, i11, i12, i13, str3, str4);
    }

    private static Track createVideoTrackFromNative(String str, String str2, int i10, int i11, int i12, int i13, String str3, String str4, int i14, int i15, int i16, int i17, int i18, int i19, int i20, int i21) {
        return new VideoTrack(str, str2, i10, i11, i12, i13, str3, str4, i14, i15, i16, i17, i18, i19, i20, i21);
    }

    private static String getMediaCodecModule() {
        return AndroidUtil.isLolliPopOrLater ? "mediacodec_ndk" : "mediacodec_jni";
    }

    private Track[] getTracks() {
        synchronized (this) {
            Track[] trackArr = this.mNativeTracks;
            if (trackArr != null) {
                return trackArr;
            }
            if (isReleased()) {
                return null;
            }
            Track[] nativeGetTracks = nativeGetTracks();
            synchronized (this) {
                this.mNativeTracks = nativeGetTracks;
            }
            return nativeGetTracks;
        }
    }

    private native void nativeAddOption(String str);

    private native void nativeAddSlave(int i10, int i11, String str);

    private native void nativeClearSlaves();

    private native long nativeGetDuration();

    private native String nativeGetMeta(int i10);

    private native String nativeGetMrl();

    private native Slave[] nativeGetSlaves();

    private native int nativeGetState();

    private native Stats nativeGetStats();

    private native Track[] nativeGetTracks();

    private native int nativeGetType();

    private native void nativeNewFromFd(LibVLC libVLC, FileDescriptor fileDescriptor, long j10, long j11);

    private native void nativeNewFromFdWithOffsetLength(LibVLC libVLC, int i10, long j10, long j11);

    private native void nativeNewFromLocation(LibVLC libVLC, String str);

    private native void nativeNewFromMediaList(MediaList mediaList, int i10);

    private native void nativeNewFromPath(LibVLC libVLC, String str);

    private native boolean nativeParse(int i10);

    private native boolean nativeParseAsync(int i10, int i11);

    private native void nativeRelease();

    private synchronized void postParse() {
        int i10 = this.mParseStatus;
        if ((i10 & 2) != 0) {
            return;
        }
        this.mParseStatus = (i10 & (-2)) | 2;
        this.mNativeTracks = null;
        this.mDuration = -1L;
        this.mState = -1;
        this.mType = -1;
    }

    public void addOption(String str) {
        synchronized (this) {
            if (!this.mCodecOptionSet && str.startsWith(":codec=")) {
                this.mCodecOptionSet = true;
            }
            if (!this.mNetworkCachingSet && str.startsWith(":network-caching=")) {
                this.mNetworkCachingSet = true;
            }
            if (!this.mFileCachingSet && str.startsWith(":file-caching=")) {
                this.mFileCachingSet = true;
            }
        }
        nativeAddOption(str);
    }

    public void addSlave(Slave slave) {
        nativeAddSlave(slave.type, slave.priority, slave.uri);
    }

    public void clearSlaves() {
        nativeClearSlaves();
    }

    public long getDuration() {
        synchronized (this) {
            long j10 = this.mDuration;
            if (j10 != -1) {
                return j10;
            }
            if (isReleased()) {
                return 0L;
            }
            long nativeGetDuration = nativeGetDuration();
            synchronized (this) {
                this.mDuration = nativeGetDuration;
            }
            return nativeGetDuration;
        }
    }

    public String getMeta(int i10) {
        if (i10 < 0 || i10 >= 25) {
            return null;
        }
        synchronized (this) {
            String[] strArr = this.mNativeMetas;
            if (strArr[i10] != null) {
                return strArr[i10];
            }
            if (isReleased()) {
                return null;
            }
            String nativeGetMeta = nativeGetMeta(i10);
            synchronized (this) {
                this.mNativeMetas[i10] = nativeGetMeta;
            }
            return nativeGetMeta;
        }
    }

    @Nullable
    public Slave[] getSlaves() {
        return nativeGetSlaves();
    }

    public int getState() {
        synchronized (this) {
            int i10 = this.mState;
            if (i10 != -1) {
                return i10;
            }
            if (isReleased()) {
                return 7;
            }
            int nativeGetState = nativeGetState();
            synchronized (this) {
                this.mState = nativeGetState;
            }
            return nativeGetState;
        }
    }

    @Nullable
    public Stats getStats() {
        return nativeGetStats();
    }

    public Track getTrack(int i10) {
        Track[] tracks = getTracks();
        if (tracks == null || i10 < 0 || i10 >= tracks.length) {
            return null;
        }
        return tracks[i10];
    }

    public int getTrackCount() {
        Track[] tracks = getTracks();
        if (tracks != null) {
            return tracks.length;
        }
        return 0;
    }

    public int getType() {
        synchronized (this) {
            int i10 = this.mType;
            if (i10 != -1) {
                return i10;
            }
            if (isReleased()) {
                return 0;
            }
            int nativeGetType = nativeGetType();
            synchronized (this) {
                this.mType = nativeGetType;
            }
            return nativeGetType;
        }
    }

    public synchronized Uri getUri() {
        return this.mUri;
    }

    public synchronized boolean isParsed() {
        return (this.mParseStatus & 2) != 0;
    }

    @Override // com.mrzero.tranplayer.VLCObject
    public /* bridge */ /* synthetic */ boolean isReleased() {
        return super.isReleased();
    }

    @Override // com.mrzero.tranplayer.VLCObject
    public void onReleaseNative() {
        MediaList mediaList = this.mSubItems;
        if (mediaList != null) {
            mediaList.release();
        }
        nativeRelease();
    }

    public boolean parse(int i10) {
        boolean z10;
        synchronized (this) {
            int i11 = this.mParseStatus;
            if ((i11 & 3) == 0) {
                this.mParseStatus = i11 | 1;
                z10 = true;
            } else {
                z10 = false;
            }
        }
        if (!z10 || !nativeParse(i10)) {
            return false;
        }
        postParse();
        return true;
    }

    public boolean parseAsync(int i10, int i11) {
        boolean z10;
        synchronized (this) {
            int i12 = this.mParseStatus;
            if ((i12 & 3) == 0) {
                this.mParseStatus = i12 | 1;
                z10 = true;
            } else {
                z10 = false;
            }
        }
        return z10 && nativeParseAsync(i10, i11);
    }

    @Override // com.mrzero.tranplayer.VLCObject
    public /* bridge */ /* synthetic */ void release() {
        super.release();
    }

    @Override // com.mrzero.tranplayer.VLCObject
    public synchronized /* bridge */ /* synthetic */ boolean retain() {
        return super.retain();
    }

    public void setDefaultMediaPlayerOptions() {
        boolean z10;
        synchronized (this) {
            z10 = this.mCodecOptionSet;
            this.mCodecOptionSet = true;
        }
        if (!z10) {
            setHWDecoderEnabled(true, false);
        }
        Uri uri = this.mUri;
        if (uri == null || uri.getScheme() == null || this.mUri.getScheme().equalsIgnoreCase("file") || this.mUri.getLastPathSegment() == null || !this.mUri.getLastPathSegment().toLowerCase().endsWith(".iso")) {
            return;
        }
        addOption(":demux=dvdnav,any");
    }

    public void setEventListener(EventListener eventListener) {
        super.setEventListener((VLCEvent.Listener) eventListener);
    }

    public void setHWDecoderEnabled(boolean z10, boolean z11) {
        HWDecoderUtil.Decoder decoderFromDevice = z10 ? HWDecoderUtil.getDecoderFromDevice() : HWDecoderUtil.Decoder.NONE;
        HWDecoderUtil.Decoder decoder = HWDecoderUtil.Decoder.UNKNOWN;
        if (decoderFromDevice == decoder && z11) {
            decoderFromDevice = HWDecoderUtil.Decoder.ALL;
        }
        if (decoderFromDevice == HWDecoderUtil.Decoder.NONE || decoderFromDevice == decoder) {
            addOption(":codec=all");
            return;
        }
        if (!this.mFileCachingSet) {
            addOption(":file-caching=1500");
        }
        if (!this.mNetworkCachingSet) {
            addOption(":network-caching=1500");
        }
        StringBuilder sb2 = new StringBuilder(":codec=");
        if (decoderFromDevice == HWDecoderUtil.Decoder.MEDIACODEC || decoderFromDevice == HWDecoderUtil.Decoder.ALL) {
            sb2.append(getMediaCodecModule());
            sb2.append(",");
        }
        if (z11 && (decoderFromDevice == HWDecoderUtil.Decoder.OMX || decoderFromDevice == HWDecoderUtil.Decoder.ALL)) {
            sb2.append("iomx,");
        }
        sb2.append("all");
        addOption(sb2.toString());
    }

    @Override // com.mrzero.tranplayer.VLCObject
    public /* bridge */ /* synthetic */ void setPinPVideoMode(boolean z10) {
        super.setPinPVideoMode(z10);
    }

    public MediaList subItems() {
        MediaList mediaList;
        synchronized (this) {
            MediaList mediaList2 = this.mSubItems;
            if (mediaList2 != null) {
                mediaList2.retain();
                return this.mSubItems;
            }
            MediaList mediaList3 = new MediaList(this);
            synchronized (this) {
                this.mSubItems = mediaList3;
                mediaList3.retain();
                mediaList = this.mSubItems;
            }
            return mediaList;
        }
    }

    /* JADX WARN: Can't rename method to resolve collision */
    @Override // com.mrzero.tranplayer.VLCObject
    public synchronized Event onEventNative(int i10, long j10, long j11, long j12, long j13, float f10, String str) {
        if (i10 == 0) {
            int i11 = (int) j10;
            if (i11 >= 0 && i11 < 25) {
                this.mNativeMetas[i11] = null;
            }
            return new Event(i10, j10);
        }
        if (i10 == 5) {
            this.mState = -1;
        } else if (i10 == 2) {
            this.mDuration = -1L;
        } else if (i10 == 3) {
            postParse();
            return new Event(i10, j10);
        }
        return new Event(i10);
    }

    public boolean parseAsync(int i10) {
        return parseAsync(i10, -1);
    }

    public boolean parse() {
        return parse(2);
    }

    public boolean parseAsync() {
        return parseAsync(2);
    }

    public Media(LibVLC libVLC, Uri uri) {
        super(libVLC);
        this.mUri = null;
        this.mSubItems = null;
        this.mParseStatus = 0;
        this.mNativeMetas = new String[25];
        this.mNativeTracks = null;
        this.mDuration = -1L;
        this.mState = -1;
        this.mType = -1;
        this.mCodecOptionSet = false;
        this.mFileCachingSet = false;
        this.mNetworkCachingSet = false;
        nativeNewFromLocation(libVLC, VLCUtil.encodeVLCUri(uri));
        this.mUri = uri;
    }

    public Media(LibVLC libVLC, FileDescriptor fileDescriptor, long j10, long j11) {
        super(libVLC);
        this.mUri = null;
        this.mSubItems = null;
        this.mParseStatus = 0;
        this.mNativeMetas = new String[25];
        this.mNativeTracks = null;
        this.mDuration = -1L;
        this.mState = -1;
        this.mType = -1;
        this.mCodecOptionSet = false;
        this.mFileCachingSet = false;
        this.mNetworkCachingSet = false;
        nativeNewFromFd(libVLC, fileDescriptor, j10, j11);
        this.mUri = VLCUtil.UriFromMrl(nativeGetMrl());
    }

    public Media(LibVLC libVLC, int i10, long j10, long j11) {
        super(libVLC);
        this.mUri = null;
        this.mSubItems = null;
        this.mParseStatus = 0;
        this.mNativeMetas = new String[25];
        this.mNativeTracks = null;
        this.mDuration = -1L;
        this.mState = -1;
        this.mType = -1;
        this.mCodecOptionSet = false;
        this.mFileCachingSet = false;
        this.mNetworkCachingSet = false;
        nativeNewFromFdWithOffsetLength(libVLC, i10, j10, j11);
        this.mUri = VLCUtil.UriFromMrl(nativeGetMrl());
    }

    public Media(MediaList mediaList, int i10) {
        super(mediaList);
        this.mUri = null;
        this.mSubItems = null;
        this.mParseStatus = 0;
        this.mNativeMetas = new String[25];
        this.mNativeTracks = null;
        this.mDuration = -1L;
        this.mState = -1;
        this.mType = -1;
        this.mCodecOptionSet = false;
        this.mFileCachingSet = false;
        this.mNetworkCachingSet = false;
        if (mediaList != null && !mediaList.isReleased()) {
            if (mediaList.isLocked()) {
                nativeNewFromMediaList(mediaList, i10);
                this.mUri = VLCUtil.UriFromMrl(nativeGetMrl());
                return;
            }
            throw new IllegalStateException("MediaList should be locked");
        }
        throw new IllegalArgumentException("MediaList is null or released");
    }
}
