package com.mrzero.tranplayer.util;

import android.net.Uri;
import android.os.Build;
import java.io.File;

/* loaded from: classes3.dex */
public class AndroidUtil {
    public static final boolean isJellyBeanMR2OrLater;
    public static final boolean isKitKatOrLater;
    public static final boolean isLolliPopOrLater;
    public static final boolean isMarshMallowOrLater;
    public static final boolean isNougatOrLater;
    public static final boolean isOOrLater;

    static {
        isOOrLater = Build.VERSION.SDK_INT >= 26;
        isNougatOrLater = true;
        isMarshMallowOrLater = true;
        isLolliPopOrLater = true;
        isKitKatOrLater = true;
        isJellyBeanMR2OrLater = true;
    }

    public static Uri FileToUri(File file) {
        return Uri.fromFile(file);
    }

    public static Uri LocationToUri(String str) {
        Uri parse = Uri.parse(str);
        if (parse.getScheme() != null) {
            return parse;
        }
        throw new IllegalArgumentException("location has no scheme");
    }

    public static Uri PathToUri(String str) {
        return Uri.fromFile(new File(str));
    }

    public static File UriToFile(Uri uri) {
        return new File(uri.getPath().replaceFirst("file://", ""));
    }
}
