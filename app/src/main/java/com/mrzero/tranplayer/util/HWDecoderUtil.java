package com.mrzero.tranplayer.util;

import java.util.HashMap;

/* loaded from: classes3.dex */
public class HWDecoderUtil {
    private static final AudioOutputBySOC[] sAudioOutputBySOCList;
    private static final DecoderBySOC[] sBlacklistedDecoderBySOCList;
    private static final DecoderBySOC[] sDecoderBySOCList;
    private static final HashMap<String, String> sSystemPropertyMap;

    public enum AudioOutput {
        OPENSLES,
        AUDIOTRACK,
        ALL
    }

    public static class AudioOutputBySOC {
        public final AudioOutput aout;
        public final String key;
        public final String value;

        public AudioOutputBySOC(String str, String str2, AudioOutput audioOutput) {
            this.key = str;
            this.value = str2;
            this.aout = audioOutput;
        }
    }

    public enum Decoder {
        UNKNOWN,
        NONE,
        OMX,
        MEDIACODEC,
        ALL
    }

    public static class DecoderBySOC {
        public final Decoder dec;
        public final String key;
        public final String value;

        public DecoderBySOC(String str, String str2, Decoder decoder) {
            this.key = str;
            this.value = str2;
            this.dec = decoder;
        }
    }

    static {
        Decoder decoder = Decoder.NONE;
        sBlacklistedDecoderBySOCList = new DecoderBySOC[]{new DecoderBySOC("ro.product.board", "MSM8225", decoder), new DecoderBySOC("ro.product.board", "hawaii", decoder)};
        Decoder decoder2 = Decoder.MEDIACODEC;
        Decoder decoder3 = Decoder.OMX;
        Decoder decoder4 = Decoder.ALL;
        sDecoderBySOCList = new DecoderBySOC[]{new DecoderBySOC("ro.product.brand", "SEMC", decoder), new DecoderBySOC("ro.board.platform", "msm7627", decoder), new DecoderBySOC("ro.product.brand", "Amazon", decoder2), new DecoderBySOC("ro.board.platform", "omap3", decoder3), new DecoderBySOC("ro.board.platform", "rockchip", decoder3), new DecoderBySOC("ro.board.platform", "rk29", decoder3), new DecoderBySOC("ro.board.platform", "msm7630", decoder3), new DecoderBySOC("ro.board.platform", "s5pc", decoder3), new DecoderBySOC("ro.board.platform", "montblanc", decoder3), new DecoderBySOC("ro.board.platform", "exdroid", decoder3), new DecoderBySOC("ro.board.platform", "sun6i", decoder3), new DecoderBySOC("ro.board.platform", "exynos4", decoder2), new DecoderBySOC("ro.board.platform", "omap4", decoder4), new DecoderBySOC("ro.board.platform", "tegra", decoder4), new DecoderBySOC("ro.board.platform", "tegra3", decoder4), new DecoderBySOC("ro.board.platform", "msm8660", decoder4), new DecoderBySOC("ro.board.platform", "exynos5", decoder4), new DecoderBySOC("ro.board.platform", "rk30", decoder4), new DecoderBySOC("ro.board.platform", "rk31", decoder4), new DecoderBySOC("ro.board.platform", "mv88de3100", decoder4), new DecoderBySOC("ro.hardware", "mt83", decoder4)};
        AudioOutput audioOutput = AudioOutput.OPENSLES;
        sAudioOutputBySOCList = new AudioOutputBySOC[]{new AudioOutputBySOC("ro.product.brand", "Amazon", audioOutput), new AudioOutputBySOC("ro.product.manufacturer", "Amazon", audioOutput)};
        sSystemPropertyMap = new HashMap<>();
    }

    public static AudioOutput getAudioOutputFromDevice() {
        for (AudioOutputBySOC audioOutputBySOC : sAudioOutputBySOCList) {
            String systemPropertyCached = getSystemPropertyCached(audioOutputBySOC.key);
            if (systemPropertyCached != null && systemPropertyCached.contains(audioOutputBySOC.value)) {
                return audioOutputBySOC.aout;
            }
        }
        return AudioOutput.ALL;
    }

    public static Decoder getDecoderFromDevice() {
        for (DecoderBySOC decoderBySOC : sBlacklistedDecoderBySOCList) {
            String systemPropertyCached = getSystemPropertyCached(decoderBySOC.key);
            if (systemPropertyCached != null && systemPropertyCached.contains(decoderBySOC.value)) {
                return decoderBySOC.dec;
            }
        }
        if (AndroidUtil.isJellyBeanMR2OrLater) {
            return Decoder.ALL;
        }
        for (DecoderBySOC decoderBySOC2 : sDecoderBySOCList) {
            String systemPropertyCached2 = getSystemPropertyCached(decoderBySOC2.key);
            if (systemPropertyCached2 != null && systemPropertyCached2.contains(decoderBySOC2.value)) {
                return decoderBySOC2.dec;
            }
        }
        return Decoder.UNKNOWN;
    }

    private static String getSystemProperty(String str, String str2) {
        try {
            Class<?> loadClass = ClassLoader.getSystemClassLoader().loadClass("android.os.SystemProperties");
            return (String) loadClass.getMethod("get", String.class, String.class).invoke(loadClass, str, str2);
        } catch (Exception unused) {
            return str2;
        }
    }

    private static String getSystemPropertyCached(String str) {
        HashMap<String, String> hashMap = sSystemPropertyMap;
        String str2 = hashMap.get(str);
        if (str2 != null) {
            return str2;
        }
        String systemProperty = getSystemProperty(str, "none");
        hashMap.put(str, systemProperty);
        return systemProperty;
    }
}
