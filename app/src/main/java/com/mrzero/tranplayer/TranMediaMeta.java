package com.mrzero.tranplayer;

import android.os.Bundle;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

/* loaded from: classes3.dex */
public class TranMediaMeta {
    public static final long AV_CH_BACK_CENTER = 256;
    public static final long AV_CH_BACK_LEFT = 16;
    public static final long AV_CH_BACK_RIGHT = 32;
    public static final long AV_CH_FRONT_CENTER = 4;
    public static final long AV_CH_FRONT_LEFT = 1;
    public static final long AV_CH_FRONT_LEFT_OF_CENTER = 64;
    public static final long AV_CH_FRONT_RIGHT = 2;
    public static final long AV_CH_FRONT_RIGHT_OF_CENTER = 128;
    public static final long AV_CH_LAYOUT_2POINT1 = 11;
    public static final long AV_CH_LAYOUT_2_1 = 259;
    public static final long AV_CH_LAYOUT_2_2 = 1539;
    public static final long AV_CH_LAYOUT_3POINT1 = 15;
    public static final long AV_CH_LAYOUT_4POINT0 = 263;
    public static final long AV_CH_LAYOUT_4POINT1 = 271;
    public static final long AV_CH_LAYOUT_5POINT0 = 1543;
    public static final long AV_CH_LAYOUT_5POINT0_BACK = 55;
    public static final long AV_CH_LAYOUT_5POINT1 = 1551;
    public static final long AV_CH_LAYOUT_5POINT1_BACK = 63;
    public static final long AV_CH_LAYOUT_6POINT0 = 1799;
    public static final long AV_CH_LAYOUT_6POINT0_FRONT = 1731;
    public static final long AV_CH_LAYOUT_6POINT1 = 1807;
    public static final long AV_CH_LAYOUT_6POINT1_BACK = 319;
    public static final long AV_CH_LAYOUT_6POINT1_FRONT = 1739;
    public static final long AV_CH_LAYOUT_7POINT0 = 1591;
    public static final long AV_CH_LAYOUT_7POINT0_FRONT = 1735;
    public static final long AV_CH_LAYOUT_7POINT1 = 1599;
    public static final long AV_CH_LAYOUT_7POINT1_WIDE = 1743;
    public static final long AV_CH_LAYOUT_7POINT1_WIDE_BACK = 255;
    public static final long AV_CH_LAYOUT_HEXAGONAL = 311;
    public static final long AV_CH_LAYOUT_MONO = 4;
    public static final long AV_CH_LAYOUT_OCTAGONAL = 1847;
    public static final long AV_CH_LAYOUT_QUAD = 51;
    public static final long AV_CH_LAYOUT_STEREO = 3;
    public static final long AV_CH_LAYOUT_STEREO_DOWNMIX = 1610612736;
    public static final long AV_CH_LAYOUT_SURROUND = 7;
    public static final long AV_CH_LOW_FREQUENCY = 8;
    public static final long AV_CH_LOW_FREQUENCY_2 = 34359738368L;
    public static final long AV_CH_SIDE_LEFT = 512;
    public static final long AV_CH_SIDE_RIGHT = 1024;
    public static final long AV_CH_STEREO_LEFT = 536870912;
    public static final long AV_CH_STEREO_RIGHT = 1073741824;
    public static final long AV_CH_SURROUND_DIRECT_LEFT = 8589934592L;
    public static final long AV_CH_SURROUND_DIRECT_RIGHT = 17179869184L;
    public static final long AV_CH_TOP_BACK_CENTER = 65536;
    public static final long AV_CH_TOP_BACK_LEFT = 32768;
    public static final long AV_CH_TOP_BACK_RIGHT = 131072;
    public static final long AV_CH_TOP_CENTER = 2048;
    public static final long AV_CH_TOP_FRONT_CENTER = 8192;
    public static final long AV_CH_TOP_FRONT_LEFT = 4096;
    public static final long AV_CH_TOP_FRONT_RIGHT = 16384;
    public static final long AV_CH_WIDE_LEFT = 2147483648L;
    public static final long AV_CH_WIDE_RIGHT = 4294967296L;
    public static final int FF_PROFILE_H264_BASELINE = 66;
    public static final int FF_PROFILE_H264_CAVLC_444 = 44;
    public static final int FF_PROFILE_H264_CONSTRAINED = 512;
    public static final int FF_PROFILE_H264_CONSTRAINED_BASELINE = 578;
    public static final int FF_PROFILE_H264_EXTENDED = 88;
    public static final int FF_PROFILE_H264_HIGH = 100;
    public static final int FF_PROFILE_H264_HIGH_10 = 110;
    public static final int FF_PROFILE_H264_HIGH_10_INTRA = 2158;
    public static final int FF_PROFILE_H264_HIGH_422 = 122;
    public static final int FF_PROFILE_H264_HIGH_422_INTRA = 2170;
    public static final int FF_PROFILE_H264_HIGH_444 = 144;
    public static final int FF_PROFILE_H264_HIGH_444_INTRA = 2292;
    public static final int FF_PROFILE_H264_HIGH_444_PREDICTIVE = 244;
    public static final int FF_PROFILE_H264_INTRA = 2048;
    public static final int FF_PROFILE_H264_MAIN = 77;
    public static final String TRAN_META_KEY_AUDIO_STREAM = "audio";
    public static final String TRAN_META_KEY_BITRATE = "bitrate";
    public static final String TRAN_META_KEY_CHANNEL_LAYOUT = "channel_layout";
    public static final String TRAN_META_KEY_CODEC_LEVEL = "codec_level";
    public static final String TRAN_META_KEY_CODEC_LONG_NAME = "codec_long_name";
    public static final String TRAN_META_KEY_CODEC_NAME = "codec_name";
    public static final String TRAN_META_KEY_CODEC_PIXEL_FORMAT = "codec_pixel_format";
    public static final String TRAN_META_KEY_CODEC_PROFILE = "codec_profile";
    public static final String TRAN_META_KEY_CODEC_PROFILE_ID = "codec_profile_id";
    public static final String TRAN_META_KEY_DURATION_US = "duration_us";
    public static final String TRAN_META_KEY_FORMAT = "format";
    public static final String TRAN_META_KEY_FPS_DEN = "fps_den";
    public static final String TRAN_META_KEY_FPS_NUM = "fps_num";
    public static final String TRAN_META_KEY_HEIGHT = "height";
    public static final String TRAN_META_KEY_LANGUAGE = "language";
    public static final String TRAN_META_KEY_SAMPLE_RATE = "sample_rate";
    public static final String TRAN_META_KEY_SAR_DEN = "sar_den";
    public static final String TRAN_META_KEY_SAR_NUM = "sar_num";
    public static final String TRAN_META_KEY_START_US = "start_us";
    public static final String TRAN_META_KEY_STREAMS = "streams";
    public static final String TRAN_META_KEY_TBR_DEN = "tbr_den";
    public static final String TRAN_META_KEY_TBR_NUM = "tbr_num";
    public static final String TRAN_META_KEY_TIMEDTEXT_STREAM = "timedtext";
    public static final String TRAN_META_KEY_TYPE = "type";
    public static final String TRAN_META_KEY_VIDEO_STREAM = "video";
    public static final String TRAN_META_KEY_WIDTH = "width";
    public static final String TRAN_META_VAL_TYPE__AUDIO = "audio";
    public static final String TRAN_META_VAL_TYPE__TIMEDTEXT = "timedtext";
    public static final String TRAN_META_VAL_TYPE__UNKNOWN = "unknown";
    public static final String TRAN_META_VAL_TYPE__VIDEO = "video";
    public TranStreamMeta mAudioStream;
    public long mBitrate;
    public long mDurationUS;
    public String mFormat;
    public Bundle mMediaMeta;
    public long mStartUS;
    public final ArrayList<TranStreamMeta> mStreams = new ArrayList<>();
    public TranStreamMeta mVideoStream;

    public static class TranStreamMeta {
        public long mBitrate;
        public long mChannelLayout;
        public String mCodecLongName;
        public String mCodecName;
        public String mCodecProfile;
        public int mFpsDen;
        public int mFpsNum;
        public int mHeight;
        public final int mIndex;
        public String mLanguage;
        public Bundle mMeta;
        public int mSampleRate;
        public int mSarDen;
        public int mSarNum;
        public int mTbrDen;
        public int mTbrNum;
        public String mType;
        public int mWidth;

        public TranStreamMeta(int i10) {
            this.mIndex = i10;
        }

        public String getBitrateInline() {
            long j10 = this.mBitrate;
            return j10 <= 0 ? "N/A" : j10 < 1000 ? String.format(Locale.US, "%d bit/s", Long.valueOf(j10)) : String.format(Locale.US, "%d kb/s", Long.valueOf(j10 / 1000));
        }

        public String getChannelLayoutInline() {
            long j10 = this.mChannelLayout;
            return j10 <= 0 ? "N/A" : j10 == 4 ? "mono" : j10 == 3 ? "stereo" : String.format(Locale.US, "%x", Long.valueOf(j10));
        }

        public String getCodecLongNameInline() {
            return !TextUtils.isEmpty(this.mCodecLongName) ? this.mCodecLongName : !TextUtils.isEmpty(this.mCodecName) ? this.mCodecName : "N/A";
        }

        public String getCodecShortNameInline() {
            return !TextUtils.isEmpty(this.mCodecName) ? this.mCodecName : "N/A";
        }

        public String getFpsInline() {
            int i10;
            int i11 = this.mFpsNum;
            return (i11 <= 0 || (i10 = this.mFpsDen) <= 0) ? "N/A" : String.valueOf(i11 / i10);
        }

        public int getInt(String str) {
            return getInt(str, 0);
        }

        public long getLong(String str) {
            return getLong(str, 0L);
        }

        public String getResolutionInline() {
            int i10 = this.mWidth;
            return (i10 <= 0 || this.mHeight <= 0) ? "N/A" : (this.mSarNum <= 0 || this.mSarDen <= 0) ? String.format(Locale.US, "%d x %d", Integer.valueOf(i10), Integer.valueOf(this.mHeight)) : String.format(Locale.US, "%d x %d [SAR %d:%d]", Integer.valueOf(i10), Integer.valueOf(this.mHeight), Integer.valueOf(this.mSarNum), Integer.valueOf(this.mSarDen));
        }

        public String getSampleRateInline() {
            int i10 = this.mSampleRate;
            return i10 <= 0 ? "N/A" : String.format(Locale.US, "%d Hz", Integer.valueOf(i10));
        }

        public String getString(String str) {
            return this.mMeta.getString(str);
        }

        public int getInt(String str, int i10) {
            String string = getString(str);
            if (TextUtils.isEmpty(string)) {
                return i10;
            }
            try {
                return Integer.parseInt(string);
            } catch (NumberFormatException unused) {
                return i10;
            }
        }

        public long getLong(String str, long j10) {
            String string = getString(str);
            if (TextUtils.isEmpty(string)) {
                return j10;
            }
            try {
                return Long.parseLong(string);
            } catch (NumberFormatException unused) {
                return j10;
            }
        }
    }

    public static TranMediaMeta parse(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        TranMediaMeta tranMediaMeta = new TranMediaMeta();
        tranMediaMeta.mMediaMeta = bundle;
        tranMediaMeta.mFormat = tranMediaMeta.getString(TRAN_META_KEY_FORMAT);
        tranMediaMeta.mDurationUS = tranMediaMeta.getLong(TRAN_META_KEY_DURATION_US);
        tranMediaMeta.mStartUS = tranMediaMeta.getLong(TRAN_META_KEY_START_US);
        tranMediaMeta.mBitrate = tranMediaMeta.getLong(TRAN_META_KEY_BITRATE);
        int i10 = -1;
        int i11 = tranMediaMeta.getInt("video", -1);
        int i12 = tranMediaMeta.getInt("audio", -1);
        tranMediaMeta.getInt("timedtext", -1);
        ArrayList<Bundle> parcelableArrayList = tranMediaMeta.getParcelableArrayList(TRAN_META_KEY_STREAMS);
        if (parcelableArrayList == null) {
            return tranMediaMeta;
        }
        Iterator<Bundle> it = parcelableArrayList.iterator();
        while (it.hasNext()) {
            Bundle next = it.next();
            i10++;
            if (next != null) {
                TranStreamMeta tranStreamMeta = new TranStreamMeta(i10);
                tranStreamMeta.mMeta = next;
                tranStreamMeta.mType = tranStreamMeta.getString("type");
                tranStreamMeta.mLanguage = tranStreamMeta.getString(TRAN_META_KEY_LANGUAGE);
                if (!TextUtils.isEmpty(tranStreamMeta.mType)) {
                    tranStreamMeta.mCodecName = tranStreamMeta.getString(TRAN_META_KEY_CODEC_NAME);
                    tranStreamMeta.mCodecProfile = tranStreamMeta.getString(TRAN_META_KEY_CODEC_PROFILE);
                    tranStreamMeta.mCodecLongName = tranStreamMeta.getString(TRAN_META_KEY_CODEC_LONG_NAME);
                    tranStreamMeta.mBitrate = tranStreamMeta.getInt(TRAN_META_KEY_BITRATE);
                    if (tranStreamMeta.mType.equalsIgnoreCase("video")) {
                        tranStreamMeta.mWidth = tranStreamMeta.getInt("width");
                        tranStreamMeta.mHeight = tranStreamMeta.getInt("height");
                        tranStreamMeta.mFpsNum = tranStreamMeta.getInt(TRAN_META_KEY_FPS_NUM);
                        tranStreamMeta.mFpsDen = tranStreamMeta.getInt(TRAN_META_KEY_FPS_DEN);
                        tranStreamMeta.mTbrNum = tranStreamMeta.getInt(TRAN_META_KEY_TBR_NUM);
                        tranStreamMeta.mTbrDen = tranStreamMeta.getInt(TRAN_META_KEY_TBR_DEN);
                        tranStreamMeta.mSarNum = tranStreamMeta.getInt(TRAN_META_KEY_SAR_NUM);
                        tranStreamMeta.mSarDen = tranStreamMeta.getInt(TRAN_META_KEY_SAR_DEN);
                        if (i11 == i10) {
                            tranMediaMeta.mVideoStream = tranStreamMeta;
                        }
                    } else if (tranStreamMeta.mType.equalsIgnoreCase("audio")) {
                        tranStreamMeta.mSampleRate = tranStreamMeta.getInt(TRAN_META_KEY_SAMPLE_RATE);
                        tranStreamMeta.mChannelLayout = tranStreamMeta.getLong(TRAN_META_KEY_CHANNEL_LAYOUT);
                        if (i12 == i10) {
                            tranMediaMeta.mAudioStream = tranStreamMeta;
                        }
                    }
                    tranMediaMeta.mStreams.add(tranStreamMeta);
                }
            }
        }
        return tranMediaMeta;
    }

    public String getDurationInline() {
        long j10 = (this.mDurationUS + 5000) / 1000000;
        long j11 = j10 / 60;
        return String.format(Locale.US, "%02d:%02d:%02d", Long.valueOf(j11 / 60), Long.valueOf(j11 % 60), Long.valueOf(j10 % 60));
    }

    public int getInt(String str) {
        return getInt(str, 0);
    }

    public long getLong(String str) {
        return getLong(str, 0L);
    }

    public ArrayList<Bundle> getParcelableArrayList(String str) {
        return this.mMediaMeta.getParcelableArrayList(str);
    }

    public String getString(String str) {
        return this.mMediaMeta.getString(str);
    }

    public int getInt(String str, int i10) {
        String string = getString(str);
        if (TextUtils.isEmpty(string)) {
            return i10;
        }
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException unused) {
            return i10;
        }
    }

    public long getLong(String str, long j10) {
        String string = getString(str);
        if (TextUtils.isEmpty(string)) {
            return j10;
        }
        try {
            return Long.parseLong(string);
        } catch (NumberFormatException unused) {
            return j10;
        }
    }
}
