package com.mrzero.tranplayer.util;

import android.annotation.TargetApi;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import com.mrzero.tranplayer.LibVLC;
import com.mrzero.tranplayer.Media;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/* loaded from: classes3.dex */
public class VLCUtil {
    private static final String[] CPU_archs = {"*Pre-v4", "*v4", "*v4T", "v5T", "v5TE", "v5TEJ", "v6", "v6KZ", "v6T2", "v6K", "v7", "*v6-M", "*v6S-M", "*v7E-M", "*v8"};
    private static final int ELF_HEADER_SIZE = 52;
    private static final int EM_386 = 3;
    private static final int EM_AARCH64 = 183;
    private static final int EM_ARM = 40;
    private static final int EM_MIPS = 8;
    private static final int EM_X86_64 = 62;
    private static final int SECTION_HEADER_SIZE = 40;
    private static final int SHT_ARM_ATTRIBUTES = 1879048195;
    public static final String TAG = "VLC/LibVLC/Util";
    private static final String URI_AUTHORIZED_CHARS = "'()*";
    private static String errorMsg = null;
    private static boolean isCompatible = false;
    private static MachineSpecs machineSpecs;

    public static class ElfData {
        public String att_arch;
        public boolean att_fpu;
        public int e_machine;
        public int e_shnum;
        public int e_shoff;
        public boolean is64bits;
        public ByteOrder order;
        public int sh_offset;
        public int sh_size;

        private ElfData() {
        }
    }

    public static class MachineSpecs {
        public float bogoMIPS;
        public float frequency;
        public boolean hasArmV6;
        public boolean hasArmV7;
        public boolean hasFpu;
        public boolean hasMips;
        public boolean hasNeon;
        public boolean hasX86;
        public boolean is64bits;
        public int processors;
    }

    public static Uri UriFromMrl(String str) {
        int parseInt = -1;
        char[] charArray = str.toCharArray();
        StringBuilder sb2 = new StringBuilder(charArray.length * 2);
        int i10 = 0;
        while (i10 < charArray.length) {
            char c10 = charArray[i10];
            if (c10 == '%' && charArray.length - i10 >= 3) {
                try {
                    parseInt = Integer.parseInt(new String(charArray, i10 + 1, 2), 16);
                } catch (NumberFormatException unused) {
                }
                if (URI_AUTHORIZED_CHARS.indexOf(parseInt) != -1) {
                    sb2.append((char) parseInt);
                    i10 += 2;
                    i10++;
                }
            }
            sb2.append(c10);
            i10++;
        }
        return Uri.parse(sb2.toString());
    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException unused) {
            }
        }
    }

    public static String encodeVLCString(@NonNull String str) {
        char[] charArray = str.toCharArray();
        StringBuilder sb2 = new StringBuilder(charArray.length * 2);
        for (char c10 : charArray) {
            if (URI_AUTHORIZED_CHARS.indexOf(c10) != -1) {
                sb2.append("%");
                sb2.append(Integer.toHexString(c10));
            } else {
                sb2.append(c10);
            }
        }
        return sb2.toString();
    }

    public static String encodeVLCUri(@NonNull Uri uri) {
        return encodeVLCString(uri.toString());
    }

    public static String[] getABIList() {
        return new String[]{Build.CPU_ABI, Build.CPU_ABI2};
    }

    @TargetApi(21)
    public static String[] getABIList21() {
        String[] strArr = Build.SUPPORTED_ABIS;
        return (strArr == null || strArr.length == 0) ? getABIList() : strArr;
    }

    public static String getErrorMsg() {
        return errorMsg;
    }

    public static MachineSpecs getMachineSpecs() {
        return machineSpecs;
    }

    private static String getString(ByteBuffer byteBuffer) {
        char c10;
        StringBuilder sb2 = new StringBuilder(byteBuffer.limit());
        while (byteBuffer.remaining() > 0 && (c10 = (char) byteBuffer.get()) != 0) {
            sb2.append(c10);
        }
        return sb2.toString();
    }

    public static byte[] getThumbnail(LibVLC libVLC, Uri uri, int i10, int i11) {
        if (uri.getLastPathSegment().endsWith(".iso")) {
            uri = Uri.parse("dvdsimple://" + uri.getEncodedPath());
        }
        Media media = new Media(libVLC, uri);
        byte[] thumbnail = getThumbnail(media, i10, i11);
        media.release();
        return thumbnail;
    }

    private static int getUleb128(ByteBuffer byteBuffer) {
        return 0;
    }

    /* JADX WARN: Can't wrap try/catch for region: R(26:5|(2:6|(3:8|(2:10|11)(4:13|(1:15)(2:18|(2:20|21)(2:22|(2:24|25)(2:26|(1:28)(1:29))))|16|17)|12)(1:30))|31|(2:33|(31:35|(1:222)(1:39)|(1:221)(1:43)|44|(1:46)(1:220)|47|(1:49)(1:(1:219))|50|(1:52)(1:217)|53|54|55|56|57|58|(2:59|(3:60|61|(14:63|(11:101|(1:103)(3:104|(2:110|(1:112)(2:113|(2:118|(1:120))(1:117)))|106)|66|(1:97)|70|(1:96)|76|(1:78)|79|(7:84|85|86|87|88|89|90)|91)|65|66|(1:68)|97|70|(2:72|74)|96|76|(0)|79|(1:95)(9:81|82|84|85|86|87|88|89|90)|91)(1:121)))|122|(1:124)(1:200)|125|(7:(2:159|(1:162))(1:(1:158)(1:131))|(1:(1:157))(1:134)|135|(1:140)|141|(2:143|(2:150|(1:153))(1:146))(1:154)|(1:149))|163|164|165|167|168|169|170|(2:172|173)(1:179)|174|175|176))(1:224)|223|54|55|56|57|58|(2:59|(4:60|61|(0)(0)|91))|122|(0)(0)|125|(0)|163|164|165|167|168|169|170|(0)(0)|174|175|176) */
    /* JADX WARN: Code restructure failed: missing block: B:181:0x02c6, code lost:
    
        android.util.Log.w(com.mrzero.tranplayer.util.VLCUtil.TAG, "Could not find maximum CPU frequency!");
        r10 = r10;
     */
    /* JADX WARN: Code restructure failed: missing block: B:182:0x02cb, code lost:
    
        close(r12);
        close(r10);
        r0 = -1.0f;
     */
    /* JADX WARN: Code restructure failed: missing block: B:184:0x02aa, code lost:
    
        android.util.Log.w(com.mrzero.tranplayer.util.VLCUtil.TAG, "Could not parse maximum CPU frequency!");
        android.util.Log.w(com.mrzero.tranplayer.util.VLCUtil.TAG, "Failed to parse: ");
        r10 = r10;
     */
    /* JADX WARN: Code restructure failed: missing block: B:187:0x02c5, code lost:
    
        r12 = null;
     */
    /* JADX WARN: Code restructure failed: missing block: B:189:0x02a9, code lost:
    
        r12 = null;
     */
    /* JADX WARN: Code restructure failed: missing block: B:190:0x029f, code lost:
    
        r0 = th;
     */
    /* JADX WARN: Code restructure failed: missing block: B:191:0x02a0, code lost:
    
        r3 = null;
        r10 = r10;
     */
    /* JADX WARN: Code restructure failed: missing block: B:192:0x0321, code lost:
    
        close(r3);
        close(r10);
     */
    /* JADX WARN: Code restructure failed: missing block: B:193:0x0327, code lost:
    
        throw r0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:196:0x02c4, code lost:
    
        r10 = null;
     */
    /* JADX WARN: Code restructure failed: missing block: B:197:0x02a8, code lost:
    
        r10 = null;
     */
    /* JADX WARN: Code restructure failed: missing block: B:198:0x02a3, code lost:
    
        r0 = th;
     */
    /* JADX WARN: Code restructure failed: missing block: B:199:0x02a4, code lost:
    
        r3 = null;
        r10 = 0;
     */
    /* JADX WARN: Code restructure failed: missing block: B:209:0x01d3, code lost:
    
        r9 = null;
        r16 = false;
        r17 = false;
        r18 = 0;
        r19 = false;
        r20 = false;
        r21 = -1.0f;
     */
    /* JADX WARN: Code restructure failed: missing block: B:210:0x01c3, code lost:
    
        r0 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:211:0x01c4, code lost:
    
        r1 = r0;
        r0 = r3;
     */
    /* JADX WARN: Code restructure failed: missing block: B:212:0x01ca, code lost:
    
        r3 = null;
     */
    /* JADX WARN: Code restructure failed: missing block: B:214:0x01d2, code lost:
    
        r3 = null;
     */
    /* JADX WARN: Code restructure failed: missing block: B:215:0x01c7, code lost:
    
        r0 = move-exception;
     */
    /* JADX WARN: Code restructure failed: missing block: B:216:0x01c8, code lost:
    
        r1 = r0;
        r0 = null;
     */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Removed duplicated region for block: B:121:0x01e0 A[EDGE_INSN: B:121:0x01e0->B:122:0x01e0 BREAK  A[LOOP:1: B:59:0x0100->B:93:0x0100, LOOP_LABEL: LOOP:1: B:59:0x0100->B:93:0x0100], SYNTHETIC] */
    /* JADX WARN: Removed duplicated region for block: B:124:0x01ee  */
    /* JADX WARN: Removed duplicated region for block: B:127:0x01f7  */
    /* JADX WARN: Removed duplicated region for block: B:172:0x028e A[Catch: NumberFormatException -> 0x02aa, IOException -> 0x02c6, all -> 0x031f, TRY_LEAVE, TryCatch #14 {all -> 0x031f, blocks: (B:170:0x0288, B:172:0x028e, B:184:0x02aa, B:181:0x02c6), top: B:164:0x027c }] */
    /* JADX WARN: Removed duplicated region for block: B:179:0x0296  */
    /* JADX WARN: Removed duplicated region for block: B:200:0x01f0  */
    /* JADX WARN: Removed duplicated region for block: B:63:0x0108 A[Catch: all -> 0x01be, IOException -> 0x01e0, TryCatch #10 {IOException -> 0x01e0, all -> 0x01be, blocks: (B:61:0x0102, B:63:0x0108, B:66:0x0161, B:68:0x0169, B:70:0x0172, B:72:0x017a, B:74:0x0182, B:76:0x018c, B:78:0x0194, B:82:0x019b, B:85:0x01a9, B:88:0x01b0, B:98:0x0113, B:101:0x011c, B:104:0x0126, B:107:0x0130, B:110:0x0139, B:113:0x0144, B:115:0x014c, B:118:0x0157), top: B:60:0x0102 }] */
    /* JADX WARN: Removed duplicated region for block: B:78:0x0194 A[Catch: all -> 0x01be, IOException -> 0x01e0, TryCatch #10 {IOException -> 0x01e0, all -> 0x01be, blocks: (B:61:0x0102, B:63:0x0108, B:66:0x0161, B:68:0x0169, B:70:0x0172, B:72:0x017a, B:74:0x0182, B:76:0x018c, B:78:0x0194, B:82:0x019b, B:85:0x01a9, B:88:0x01b0, B:98:0x0113, B:101:0x011c, B:104:0x0126, B:107:0x0130, B:110:0x0139, B:113:0x0144, B:115:0x014c, B:118:0x0157), top: B:60:0x0102 }] */
    /* JADX WARN: Type inference failed for: r10v2 */
    /* JADX WARN: Type inference failed for: r10v38 */
    /* JADX WARN: Type inference failed for: r10v39 */
    /* JADX WARN: Type inference failed for: r10v5, types: [java.io.Closeable] */
    /* JADX WARN: Type inference failed for: r12v0 */
    /* JADX WARN: Type inference failed for: r12v1 */
    /* JADX WARN: Type inference failed for: r12v16 */
    /* JADX WARN: Type inference failed for: r12v17 */
    /* JADX WARN: Type inference failed for: r12v2 */
    /* JADX WARN: Type inference failed for: r12v25 */
    /* JADX WARN: Type inference failed for: r12v26 */
    /* JADX WARN: Type inference failed for: r12v27 */
    /* JADX WARN: Type inference failed for: r12v28 */
    /* JADX WARN: Type inference failed for: r12v29 */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public static boolean hasCompatibleCPU(android.content.Context context) {
        return true;
    }

    private static native byte[] nativeGetThumbnail(Media media, int i10, int i11);

    private static boolean readArmAttributes(RandomAccessFile randomAccessFile, ElfData elfData) throws IOException {
        byte[] bArr = new byte[elfData.sh_size];
        randomAccessFile.seek(elfData.sh_offset);
        randomAccessFile.readFully(bArr);
        ByteBuffer wrap = ByteBuffer.wrap(bArr);
        wrap.order(elfData.order);
        if (wrap.get() != 65) {
            return false;
        }
        while (true) {
            if (wrap.remaining() <= 0) {
                break;
            }
            int position = wrap.position();
            int i10 = wrap.getInt();
            if (getString(wrap).equals("aeabi")) {
                while (wrap.position() < position + i10) {
                    int position2 = wrap.position();
                    byte b10 = wrap.get();
                    int i11 = wrap.getInt();
                    if (b10 != 1) {
                        wrap.position(position2 + i11);
                    } else {
                        while (wrap.position() < position2 + i11) {
                            int uleb128 = getUleb128(wrap);
                            if (uleb128 == 6) {
                                elfData.att_arch = CPU_archs[getUleb128(wrap)];
                            } else if (uleb128 == 27) {
                                getUleb128(wrap);
                                elfData.att_fpu = true;
                            } else {
                                int i12 = uleb128 % 128;
                                if (i12 == 4 || i12 == 5 || i12 == 32 || (i12 > 32 && (i12 & 1) != 0)) {
                                    getString(wrap);
                                } else {
                                    getUleb128(wrap);
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private static boolean readHeader(RandomAccessFile randomAccessFile, ElfData elfData) throws IOException {
        byte[] bArr = new byte[52];
        randomAccessFile.readFully(bArr);
        if (bArr[0] != Byte.MAX_VALUE || bArr[1] != 69 || bArr[2] != 76 || bArr[3] != 70 || (bArr[4] != 1 && bArr[4] != 2)) {
            Log.e(TAG, "ELF header invalid");
            return false;
        }
        elfData.is64bits = bArr[4] == 2;
        elfData.order = bArr[5] == 1 ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN;
        ByteBuffer wrap = ByteBuffer.wrap(bArr);
        wrap.order(elfData.order);
        elfData.e_machine = wrap.getShort(18);
        elfData.e_shoff = wrap.getInt(32);
        elfData.e_shnum = wrap.getShort(48);
        return true;
    }

    /* JADX WARN: Multi-variable type inference failed */
    private static ElfData readLib(File file) {
        return null;
    }

    private static boolean readSection(RandomAccessFile randomAccessFile, ElfData elfData) throws IOException {
        byte[] bArr = new byte[40];
        randomAccessFile.seek(elfData.e_shoff);
        for (int i10 = 0; i10 < elfData.e_shnum; i10++) {
            randomAccessFile.readFully(bArr);
            ByteBuffer wrap = ByteBuffer.wrap(bArr);
            wrap.order(elfData.order);
            if (wrap.getInt(4) == SHT_ARM_ATTRIBUTES) {
                elfData.sh_offset = wrap.getInt(16);
                elfData.sh_size = wrap.getInt(20);
                return true;
            }
        }
        return false;
    }

    @TargetApi(9)
    private static File searchLibrary(ApplicationInfo applicationInfo) {
        String[] split = (applicationInfo.flags & 1) != 0 ? System.getProperty("java.library.path").split(":") : new String[]{applicationInfo.nativeLibraryDir};
        if (split[0] == null) {
            Log.e(TAG, "can't find library path");
            return null;
        }
        for (String str : split) {
            File file = new File(str, "libvlcjni.so");
            if (file.exists() && file.canRead()) {
                return file;
            }
        }
        Log.e(TAG, "WARNING: Can't find shared library");
        return null;
    }

    public static byte[] getThumbnail(Media media, int i10, int i11) {
        media.addOption(":no-audio");
        media.addOption(":no-spu");
        media.addOption(":no-osd");
        media.addOption(":input-fast-seek");
        return nativeGetThumbnail(media, i10, i11);
    }
}
