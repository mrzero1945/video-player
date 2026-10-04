#!/usr/bin/env python3
import re, io, sys

path = "app/src/main/java/com/mrzero/tranplayer/TranMediaPlayer.java"
src = open(path, encoding="utf-8").read()

def replace_method(src, signature_re, new_body, classname):
    """Replace a method whose body ends at the 'throw new UnsupportedOperationException'."""
    pat = re.compile(
        r"[ \t]*" + signature_re + r"(?:\s+throws[^{]*?)?\s*\{.*?throw new UnsupportedOperationException\(\"Method not decompiled: " +
        re.escape(classname) + r"[^\n]*?\);\n[ \t]*\}",
        re.S)
    new_src, n = pat.subn(lambda m: new_body, src, count=1)
    if n != 1:
        raise SystemExit("FAILED to patch: " + signature_re + " (n=%d)" % n)
    return new_src

set_display = '''    public void setDisplay(android.view.SurfaceHolder surfaceHolder) {
        String str = TAG;
        Log.d(str, "setDisplay " + surfaceHolder + " " + this.mMediaPlayer + " mVideoCodecDeleted: " + this.mVideoCodecDeleted + " libvlc {");
        MediaPlayer mediaPlayer = this.mMediaPlayer;
        Surface surface = null;
        if (mediaPlayer != null) {
            if (surfaceHolder == null) {
                this.bWillToSetSurface = false;
                mediaPlayer.setSurfaceDestroyed(true);
                this.mMediaPlayer.setVideoTrack(-1);
                this.mMediaPlayer.setSurface(false);
                this.mSurfaceHolder = null;
            } else {
                if (this.mSurface != null && mediaPlayer.getVLCVout().areViewsAttached()) {
                    this.mMediaPlayer.getVLCVout().removeCallback(this);
                    this.mMediaPlayer.getVLCVout().detachViews();
                }
                this.mSurfaceHolder = surfaceHolder;
                if (this.mHadVideoCodec && !this.mVideoCodecDeleted) {
                    this.bWillToSetSurface = true;
                    this.mSurfaceHolder = surfaceHolder;
                    Log.d(str, "setDisplay goto bWillToSetSurface ");
                    this.mSurface = surfaceHolder.getSurface();
                    return;
                }
                this.mMediaPlayer.setSurfaceDestroyed(false);
                this.mMediaPlayer.getVLCVout().setVideoSurface(surfaceHolder.getSurface(), surfaceHolder);
                this.mMediaPlayer.getVLCVout().addCallback(this);
                this.mMediaPlayer.getVLCVout().attachViews(this);
                this.mMediaPlayer.setSurface(true);
                this.bWillToSetSurface = false;
                this.mMediaPlayer.setListenVideoMode(false);
                long currentPosition = getCurrentPosition();
                long duration = getDuration();
                this.mMediaPlayer.setPinPVideoMode(this.mPIPVideoMode);
                if (!this.mEofPaused) {
                    long j10 = duration - currentPosition;
                    if (j10 < 1000) {
                        seekTo(0L, false, false);
                    } else if (j10 < 3000) {
                        Log.d(str, "now:" + currentPosition + ", duration:" + duration + ". it\\'s close to duration when resume playback");
                        seekTo(currentPosition, false, false);
                    }
                } else {
                    seekTo(0L, false, false);
                    this.mEofPaused = true;
                }
                if (this.mPaused || this.mEofPaused) {
                    this.mMediaPlayer.play();
                    this.mMediaPlayer.setRefreshFirstFrame(true);
                }
                if (this.mEofPaused) {
                    this.mMediaPlayer.setDropAudioFrames(true);
                }
            }
        }
        if (surfaceHolder != null && this.mMediaPlayer != null) {
            surface = surfaceHolder.getSurface();
        }
        this.mSurface = surface;
        Log.d(str, "setDisplay " + this.mSurface + " }");
    }'''

set_data_source = '''    public void setDataSource(android.content.Context context, android.net.Uri uri, java.util.Map<java.lang.String, String> map) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        Objects.requireNonNull(context, "context param can not be null.");
        this.mUri = uri;
        this.mContext = context;
        if (map != null) {
            Object obj = map.get("listen-video");
            if (obj != null && obj.toString().compareTo("1") == 0) {
                this.mListenVideo = true;
            }
        }
        String scheme = uri.getScheme();
        if ("file".equals(scheme)) {
            setDataSource(uri.getPath(), map);
            return;
        }
        if ("content".equals(scheme) && "settings".equals(uri.getAuthority())) {
            Uri ringtone = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.getDefaultType(uri));
            if (ringtone == null) {
                throw new FileNotFoundException("Failed to resolve default ringtone");
            }
            uri = ringtone;
        }
        AssetFileDescriptor afd = null;
        try {
            afd = context.getContentResolver().openAssetFileDescriptor(uri, "r");
            if (afd == null) {
                return;
            }
            long declaredLength = afd.getDeclaredLength();
            if (declaredLength < 0) {
                if (this.mListenVideo) {
                    String path = getMediaSourcePath(uri);
                    if (path != null) {
                        setDataSource(path, map);
                    } else {
                        setDataSource(afd.getFileDescriptor(), map);
                    }
                } else {
                    setDataSource(afd.getFileDescriptor(), map);
                }
            } else {
                setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), declaredLength, map);
            }
            afd.close();
            return;
        } catch (IOException e) {
            Log.d(TAG, "setDataSource IOException " + e);
        } catch (SecurityException e) {
            Log.d(TAG, "setDataSource SecurityException " + e);
        } catch (Throwable t) {
            if (afd != null) {
                afd.close();
            }
            throw t;
        }
        if (afd != null) {
            afd.close();
        }
        setDataSource(uri.toString(), map);
    }'''

src = replace_method(src, r"public void setDisplay\(android\.view\.SurfaceHolder r15\)",
                     set_display, "com.mrzero.tranplayer.TranMediaPlayer.")
src = replace_method(src, r"public void setDataSource\(android\.content\.Context r9, android\.net\.Uri r10, java\.util\.Map<java\.lang\.String, java\.lang\.String> r11\)",
                     set_data_source, "com.mrzero.tranplayer.TranMediaPlayer.")

# willToSetDisplay: dead private helper -> simple reconstruction
src = replace_method(src, r"private void willToSetDisplay\(android\.view\.SurfaceHolder r11\)",
                     '''    private void willToSetDisplay(android.view.SurfaceHolder surfaceHolder) {
        this.bWillToSetSurface = surfaceHolder != null;
    }''', "com.mrzero.tranplayer.TranMediaPlayer.")

# imports
for imp in [
    "import android.content.res.AssetFileDescriptor;",
    "import android.media.RingtoneManager;",
    "import java.io.FileNotFoundException;",
    "import java.util.Objects;",
]:
    if imp not in src:
        src = src.replace("import android.net.Uri;", "import android.net.Uri;\n" + imp, 1)

open(path, "w", encoding="utf-8").write(src)
print("patched TranMediaPlayer ok")

# VLCUtil.hasCompatibleCPU - no callers, make it harmless
vpath = "app/src/main/java/com/mrzero/tranplayer/util/VLCUtil.java"
v = open(vpath, encoding="utf-8").read()
v2, n = re.subn(
    r"public static boolean hasCompatibleCPU\(android\.content\.Context r22\) \{.*?throw new UnsupportedOperationException\(\"Method not decompiled: com\.transsion\.tranplayer\.util\.VLCUtil\.hasCompatibleCPU[^\n]*?\);\n[ \t]*\}",
    "public static boolean hasCompatibleCPU(android.content.Context context) {\n        return true;\n    }",
    v, count=1, flags=re.S)
if n != 1:
    print("WARN: hasCompatibleCPU not patched", n)
else:
    open(vpath, "w", encoding="utf-8").write(v2)
    print("patched VLCUtil ok")
