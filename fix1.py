#!/usr/bin/env python3
import os, re, glob

base = "app/src/main/java/com/mrzero/tranplayer"
app = "app/src/main/java/com/videoplayer/app"

# 1) drop classes that are not part of the playback path (and contain jadx artifacts)
for rel in ["Dialog.java", "MediaMetadataRetriever.java", "TranPlayerTranscoder.java",
            "util/Dumper.java", "util/MediaBrowser.java", "util/Extensions.java"]:
    p = os.path.join(base, rel)
    if os.path.exists(p):
        os.remove(p)
        print("removed", rel)

def read(p):
    return open(p, encoding="utf-8").read()

def write(p, s):
    open(p, "w", encoding="utf-8").write(s)

def stub_method(src, signature, body):
    """Replace a method body (up to the first line that is exactly '    }')."""
    idx = src.find(signature)
    if idx < 0:
        raise SystemExit("signature not found: " + signature)
    end = src.find("\n    }\n", idx)
    if end < 0:
        raise SystemExit("end not found: " + signature)
    return src[:idx] + body + src[end + len("\n    }\n"):]

# ---- VLCUtil: stub methods that jadx could not decompile and nothing calls
p = os.path.join(base, "util/VLCUtil.java")
src = read(p)
src = stub_method(src, "    private static int getUleb128(ByteBuffer byteBuffer) {",
                  "    private static int getUleb128(ByteBuffer byteBuffer) {\n        return 0;\n")
src = stub_method(src, "    private static ElfData readLib(File file) {",
                  "    private static ElfData readLib(File file) {\n        return null;\n")
write(p, src)
print("patched VLCUtil")

# ---- MediaPlayer: lost register in getEncodingFlags
p = os.path.join(base, "MediaPlayer.java")
src = read(p)
assert "j10 |= 1 << r4;" in src
src = src.replace("j10 |= 1 << r4;", "j10 |= 1L << i10;")
write(p, src)
print("patched MediaPlayer")

# ---- TranMediaPlayer: lost local variable r2 in parseHeader
p = os.path.join(base, "TranMediaPlayer.java")
src = read(p)
assert "    private void parseHeader(Map<String, String> map) {\n        long j10;" in src
src = src.replace("    private void parseHeader(Map<String, String> map) {\n        long j10;",
                  "    private void parseHeader(Map<String, String> map) {\n        long j10;\n        String r2 = null;", 1)
write(p, src)
print("patched TranMediaPlayer")

# ---- VLCObject: jadx wrote an anonymous class with constructor arguments
p = os.path.join(base, "VLCObject.java")
src = read(p)
old_start = "            handler.post(new Runnable(listener, onEventNative) { // from class: com.mrzero.tranplayer.VLCObject.1EventRunnable"
idx = src.find(old_start)
if idx < 0:
    raise SystemExit("VLCObject pattern not found")
end = src.find("\n            });\n", idx)
if end < 0:
    raise SystemExit("VLCObject end not found")
new = """            final VLCEvent.Listener<T> runnableListener = listener;
            final T runnableEvent = onEventNative;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    runnableListener.onEvent(runnableEvent);
                    runnableEvent.release();
                }
            });"""
src = src[:idx] + new + src[end + len("\n            });\n"):]
write(p, src)
print("patched VLCObject")

# ---- PlaybackService: androidx.media keeps the android.support.v4 package names
p = os.path.join(app, "PlaybackService.java")
src = read(p)
src = src.replace("import androidx.media.session.MediaMetadataCompat;",
                  "import android.support.v4.media.MediaMetadataCompat;")
src = src.replace("import androidx.media.session.MediaSessionCompat;",
                  "import android.support.v4.media.session.MediaSessionCompat;")
src = src.replace("import androidx.media.session.PlaybackStateCompat;",
                  "import android.support.v4.media.session.PlaybackStateCompat;")
write(p, src)
print("patched PlaybackService")

# ---- PlayerActivity: typo
p = os.path.join(app, "PlayerActivity.java")
src = read(p)
src = src.replace("int am = (AudioManager) getSystemService(AUDIO_SERVICE);",
                  "AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);")
write(p, src)
print("patched PlayerActivity")
print("ALL DONE")
