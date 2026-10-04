package com.mrzero.videoplayer;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Video sink berbasis GPU (OpenGL ES 2.0).
 *
 * <p>Frame dari libVLC ditangkap lewat {@link SurfaceTexture}, lalu dirender oleh shader kita:
 * <ul>
 *   <li><b>Upscale FHD+</b>: frame sumber di-*upsample* ke resolusi FHD+ (panjang sisi 2400)
 *       memakai bicubic 16 tap + adaptive sharpen, lalu diresample ke layar.</li>
 *   <li><b>Interpolasi 144 FPS</b>: frame N-1 disimpan di FBO, lalu dicampur dengan frame N
 *       pada grid waktu 144 Hz &rarr; frame penghubung dihasilkan GPU, dan permukaan diminta
 *       berjalan pada refresh-rate tinggi.</li>
 * </ul>
 */
public class GLVideoView extends GLSurfaceView {

    public interface Callback {
        void onVideoSurfaceReady(Surface surface);

        void onVideoSurfaceDestroyed();
    }

    private static final String TAG = "GLVideoView";

    /** Target interpolasi (frame per detik). */
    private static final float TARGET_FPS = 144f;
    /** Panjang sisi FHD+ (1080 x 2400). */
    private static final int FHD_PLUS_LONG = 2400;
    /** Batas ukuran FBO agar aman di memori. */
    private static final int MAX_FRAME_EDGE = 4096;
    /** Toleransi tanpa frame sebelum dianggap berhenti (deteksi tombol play). */
    private static final long FRAME_STOP_GRACE_NS = 600_000_000L;

    private static final String VERTEX_SHADER =
            "attribute vec4 aPos;\n"
                    + "attribute vec2 aTex;\n"
                    + "varying vec2 vTex;\n"
                    + "void main() {\n"
                    + "  gl_Position = aPos;\n"
                    + "  vTex = aTex;\n"
                    + "}\n";

    /** Salin frame dari SurfaceTexture ke FBO (menyimpan frame N-1). */
    private static final String COPY_FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n"
                    + "precision mediump float;\n"
                    + "uniform samplerExternalOES sExt;\n"
                    + "uniform mat4 uMtx;\n"
                    + "varying vec2 vTex;\n"
                    + "void main() {\n"
                    + "  gl_FragColor = texture2D(sExt, (uMtx * vec4(vTex, 0.0, 1.0)).xy);\n"
                    + "}\n";

    /** Upscale sumber -> FBO FHD+ (bicubic 16 tap + adaptive sharpen). */
    private static final String UPSCALE_FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n"
                    + "#ifdef GL_FRAGMENT_PRECISION_HIGH\n"
                    + "precision highp float;\n"
                    + "#else\n"
                    + "precision mediump float;\n"
                    + "#endif\n"
                    + "uniform samplerExternalOES sExt;\n"
                    + "uniform mat4 uMtx;\n"
                    + "uniform vec2 uSrcTexel;\n"
                    + "uniform float uSharp;\n"
                    + "varying vec2 vTex;\n"
                    + "vec4 cubic(float v) {\n"
                    + "  float v2 = v * v;\n"
                    + "  float v3 = v2 * v;\n"
                    + "  return vec4(-0.5 * v3 + v2 - 0.5 * v,\n"
                    + "               1.5 * v3 - 2.5 * v2 + 1.0,\n"
                    + "              -1.5 * v3 + 2.0 * v2 + 0.5 * v,\n"
                    + "               0.5 * v3 - 0.5 * v2);\n"
                    + "}\n"
                    + "vec4 bicubic(vec2 uv) {\n"
                    + "  vec2 p = uv / uSrcTexel - 0.5;\n"
                    + "  vec2 i = floor(p);\n"
                    + "  vec2 f = p - i;\n"
                    + "  vec4 wx = cubic(f.x);\n"
                    + "  vec4 wy = cubic(f.y);\n"
                    + "  vec2 b = i + 0.5;\n"
                    + "  vec4 s = vec4(0.0);\n"
                    + "  s += texture2D(sExt, (b + vec2(-1.0, -1.0)) * uSrcTexel) * (wx.x * wy.x);\n"
                    + "  s += texture2D(sExt, (b + vec2( 0.0, -1.0)) * uSrcTexel) * (wx.y * wy.x);\n"
                    + "  s += texture2D(sExt, (b + vec2( 1.0, -1.0)) * uSrcTexel) * (wx.z * wy.x);\n"
                    + "  s += texture2D(sExt, (b + vec2( 2.0, -1.0)) * uSrcTexel) * (wx.w * wy.x);\n"
                    + "  s += texture2D(sExt, (b + vec2(-1.0,  0.0)) * uSrcTexel) * (wx.x * wy.y);\n"
                    + "  s += texture2D(sExt, (b + vec2( 0.0,  0.0)) * uSrcTexel) * (wx.y * wy.y);\n"
                    + "  s += texture2D(sExt, (b + vec2( 1.0,  0.0)) * uSrcTexel) * (wx.z * wy.y);\n"
                    + "  s += texture2D(sExt, (b + vec2( 2.0,  0.0)) * uSrcTexel) * (wx.w * wy.y);\n"
                    + "  s += texture2D(sExt, (b + vec2(-1.0,  1.0)) * uSrcTexel) * (wx.x * wy.z);\n"
                    + "  s += texture2D(sExt, (b + vec2( 0.0,  1.0)) * uSrcTexel) * (wx.y * wy.z);\n"
                    + "  s += texture2D(sExt, (b + vec2( 1.0,  1.0)) * uSrcTexel) * (wx.z * wy.z);\n"
                    + "  s += texture2D(sExt, (b + vec2( 2.0,  1.0)) * uSrcTexel) * (wx.w * wy.z);\n"
                    + "  s += texture2D(sExt, (b + vec2(-1.0,  2.0)) * uSrcTexel) * (wx.x * wy.w);\n"
                    + "  s += texture2D(sExt, (b + vec2( 0.0,  2.0)) * uSrcTexel) * (wx.y * wy.w);\n"
                    + "  s += texture2D(sExt, (b + vec2( 1.0,  2.0)) * uSrcTexel) * (wx.z * wy.w);\n"
                    + "  s += texture2D(sExt, (b + vec2( 2.0,  2.0)) * uSrcTexel) * (wx.w * wy.w);\n"
                    + "  return s;\n"
                    + "}\n"
                    + "void main() {\n"
                    + "  vec2 uv = (uMtx * vec4(vTex, 0.0, 1.0)).xy;\n"
                    + "  vec4 c = bicubic(uv);\n"
                    + "  if (uSharp > 0.01) {\n"
                    + "    vec2 t = uSrcTexel * 1.5;\n"
                    + "    vec4 n = texture2D(sExt, uv + vec2(t.x, 0.0))\n"
                    + "           + texture2D(sExt, uv - vec2(t.x, 0.0))\n"
                    + "           + texture2D(sExt, uv + vec2(0.0, t.y))\n"
                    + "           + texture2D(sExt, uv - vec2(0.0, t.y));\n"
                    + "    n *= 0.25;\n"
                    + "    c = clamp(c + (c - n) * uSharp, 0.0, 1.0);\n"
                    + "  }\n"
                    + "  gl_FragColor = vec4(clamp(c.rgb, 0.0, 1.0), 1.0);\n"
                    + "}\n";

    /** Render ke layar langsung dari SurfaceTexture (upscale mati). */
    private static final String DRAW_FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n"
                    + "precision mediump float;\n"
                    + "uniform samplerExternalOES sExt;\n"
                    + "uniform sampler2D sPrev;\n"
                    + "uniform mat4 uMtx;\n"
                    + "uniform float uAlpha;\n"
                    + "varying vec2 vTex;\n"
                    + "void main() {\n"
                    + "  vec4 c = texture2D(sExt, (uMtx * vec4(vTex, 0.0, 1.0)).xy);\n"
                    + "  if (uAlpha > 0.001) {\n"
                    + "    c = mix(texture2D(sPrev, vTex), c, uAlpha);\n"
                    + "  }\n"
                    + "  gl_FragColor = c;\n"
                    + "}\n";

    /** Render ke layar dari FBO hasil upscale + interpolasi. */
    private static final String DRAW2D_FRAGMENT_SHADER =
            "precision mediump float;\n"
                    + "uniform sampler2D sCurr;\n"
                    + "uniform sampler2D sPrev;\n"
                    + "uniform vec2 uCurrTexel;\n"
                    + "uniform vec2 uFootprint;\n"
                    + "uniform float uAlpha;\n"
                    + "varying vec2 vTex;\n"
                    + "void main() {\n"
                    + "  vec2 o = uCurrTexel * uFootprint * 0.5;\n"
                    + "  vec4 c = 0.25 * (texture2D(sCurr, vTex + vec2(o.x, o.y))\n"
                    + "                 + texture2D(sCurr, vTex + vec2(-o.x, -o.y))\n"
                    + "                 + texture2D(sCurr, vTex + vec2(o.x, -o.y))\n"
                    + "                 + texture2D(sCurr, vTex + vec2(-o.x, o.y)));\n"
                    + "  if (uAlpha > 0.001) {\n"
                    + "    c = mix(texture2D(sPrev, vTex), c, uAlpha);\n"
                    + "  }\n"
                    + "  gl_FragColor = vec4(c.rgb, 1.0);\n"
                    + "}\n";

    private final Renderer renderer = new Renderer();
    private Callback callback;

    public GLVideoView(Context context) {
        this(context, null);
    }

    public GLVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        setRenderer(renderer);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
        setPreserveEGLContextOnPause(true);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
        renderer.callback = callback;
    }

    /** Nyalakan/matiakan upscale resolusi ke FHD+. */
    public void setUpscaling(boolean enabled) {
        renderer.upscaling = enabled;
        queueEvent(() -> {
            renderer.currValid = false;
            renderer.prevValid = false;
        });
    }

    /** Nyalakan/matiakan interpolasi frame 144 FPS. */
    public void setFrameInterpolation(boolean enabled) {
        renderer.interpolation = enabled;
        queueEvent(() -> renderer.prevValid = false);
        applyFrameRate(enabled ? TARGET_FPS : 0f);
    }

    public boolean isUpscaling() {
        return renderer.upscaling;
    }

    public boolean isFrameInterpolation() {
        return renderer.interpolation;
    }

    /**
     * Deteksi frame stop: true bila masih ada frame video yang masuk dalam
     * {@link #FRAME_STOP_GRACE_NS} terakhir (false = video berhenti/pause/putus).
     */
    public boolean isFrameActive() {
        long last = renderer.lastSourceFrameNanos;
        return last != 0L && System.nanoTime() - last < FRAME_STOP_GRACE_NS;
    }

    /** True bila minimal satu frame pernah diterima sejak surface dibuat. */
    public boolean hasFrameEver() {
        return renderer.everFramed;
    }

    public void setVideoSize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        renderer.videoW = width;
        renderer.videoH = height;
        queueEvent(() -> {
            renderer.currValid = false;
            renderer.prevValid = false;
            if (renderer.surfaceTexture != null) {
                renderer.surfaceTexture.setDefaultBufferSize(width, height);
            }
        });
    }

    /** Buang state interpolasi (dipanggil saat seek agar tidak ada crossfade palsu). */
    public void flushInterpolation() {
        queueEvent(() -> {
            renderer.prevTimestamp = 0L;
            renderer.prevValid = false;
        });
    }

    /** Minta display berjalan pada refresh-rate tinggi (hingga 144 Hz). */
    private void applyFrameRate(float fps) {
        if (Build.VERSION.SDK_INT < 30) return;
        try {
            Surface s = getHolder() != null ? getHolder().getSurface() : null;
            if (s != null && s.isValid()) {
                s.setFrameRate(fps, Surface.FRAME_RATE_COMPATIBILITY_FIXED_SOURCE);
                Log.d(TAG, "setFrameRate " + fps);
            }
        } catch (Throwable t) {
            Log.w(TAG, "setFrameRate failed", t);
        }
    }

    public void release() {
        Callback cb = callback;
        if (cb != null) cb.onVideoSurfaceDestroyed();
        queueEvent(renderer::releaseGl);
    }

    private final class Renderer implements GLSurfaceView.Renderer,
            SurfaceTexture.OnFrameAvailableListener {

        private final Handler mainHandler = new Handler(Looper.getMainLooper());
        private final float[] matrix = new float[16];
        private final FloatBuffer positions = floatBuffer(new float[]{
                -1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f});
        private final FloatBuffer texCoords = floatBuffer(new float[]{
                0f, 0f, 1f, 0f, 0f, 1f, 1f, 1f});

        private volatile Callback callback;
        private volatile boolean upscaling;
        private volatile boolean interpolation;
        private volatile boolean newFrame;
        /** Waktu terakhir frame sumber masuk (nanoTime), 0 bila belum pernah. */
        private volatile long lastSourceFrameNanos;
        private volatile boolean everFramed;
        private volatile int videoW = 1920;
        private volatile int videoH = 1080;

        private SurfaceTexture surfaceTexture;
        private Surface surface;

        private int copyProgram;
        private int upscaleProgram;
        private int drawProgram;
        private int draw2dProgram;

        private int extTex;

        private int prevTex;
        private int prevFbo;
        private int prevW;
        private int prevH;

        private int currTex;
        private int currFbo;
        private int currW;
        private int currH;

        private int viewW;
        private int viewH;

        private boolean hasFrame;
        private boolean prevValid;
        private boolean currValid;
        private long prevTimestamp;
        private long currentTimestamp;
        private int frameCount;

        private int aPosCopy;
        private int aTexCopy;
        private int uMtxCopy;

        private int aPosUp;
        private int aTexUp;
        private int uMtxUp;
        private int uSrcTexelUp;
        private int uSharpUp;

        private int aPosDraw;
        private int aTexDraw;
        private int uMtxDraw;
        private int uAlphaDraw;
        private int uExtDraw;
        private int uPrevDraw;

        private int aPosDraw2d;
        private int aTexDraw2d;
        private int uCurrDraw2d;
        private int uPrevDraw2d;
        private int uCurrTexelDraw2d;
        private int uFootprintDraw2d;
        private int uAlphaDraw2d;

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES20.glClearColor(0f, 0f, 0f, 1f);

            extTex = createExternalTexture();
            copyProgram = createProgram(VERTEX_SHADER, COPY_FRAGMENT_SHADER);
            upscaleProgram = createProgram(VERTEX_SHADER, UPSCALE_FRAGMENT_SHADER);
            drawProgram = createProgram(VERTEX_SHADER, DRAW_FRAGMENT_SHADER);
            draw2dProgram = createProgram(VERTEX_SHADER, DRAW2D_FRAGMENT_SHADER);

            aPosCopy = GLES20.glGetAttribLocation(copyProgram, "aPos");
            aTexCopy = GLES20.glGetAttribLocation(copyProgram, "aTex");
            uMtxCopy = GLES20.glGetUniformLocation(copyProgram, "uMtx");

            aPosUp = GLES20.glGetAttribLocation(upscaleProgram, "aPos");
            aTexUp = GLES20.glGetAttribLocation(upscaleProgram, "aTex");
            uMtxUp = GLES20.glGetUniformLocation(upscaleProgram, "uMtx");
            uSrcTexelUp = GLES20.glGetUniformLocation(upscaleProgram, "uSrcTexel");
            uSharpUp = GLES20.glGetUniformLocation(upscaleProgram, "uSharp");

            aPosDraw = GLES20.glGetAttribLocation(drawProgram, "aPos");
            aTexDraw = GLES20.glGetAttribLocation(drawProgram, "aTex");
            uMtxDraw = GLES20.glGetUniformLocation(drawProgram, "uMtx");
            uAlphaDraw = GLES20.glGetUniformLocation(drawProgram, "uAlpha");
            uExtDraw = GLES20.glGetUniformLocation(drawProgram, "sExt");
            uPrevDraw = GLES20.glGetUniformLocation(drawProgram, "sPrev");

            aPosDraw2d = GLES20.glGetAttribLocation(draw2dProgram, "aPos");
            aTexDraw2d = GLES20.glGetAttribLocation(draw2dProgram, "aTex");
            uCurrDraw2d = GLES20.glGetUniformLocation(draw2dProgram, "sCurr");
            uPrevDraw2d = GLES20.glGetUniformLocation(draw2dProgram, "sPrev");
            uCurrTexelDraw2d = GLES20.glGetUniformLocation(draw2dProgram, "uCurrTexel");
            uFootprintDraw2d = GLES20.glGetUniformLocation(draw2dProgram, "uFootprint");
            uAlphaDraw2d = GLES20.glGetUniformLocation(draw2dProgram, "uAlpha");

            hasFrame = false;
            prevValid = false;
            currValid = false;
            prevTimestamp = 0L;
            currentTimestamp = 0L;
            newFrame = false;
            lastSourceFrameNanos = 0L;
            everFramed = false;
            matrix[0] = 1f;
            matrix[5] = 1f;
            matrix[10] = 1f;
            matrix[15] = 1f;

            // SurfaceTexture lama (jika EGL surface dibuat ulang) dilepas setelah VLC dilepas.
            final SurfaceTexture oldTexture = surfaceTexture;
            if (oldTexture != null) {
                mainHandler.post(() -> {
                    Callback cb = callback;
                    if (cb != null) cb.onVideoSurfaceDestroyed();
                    queueEvent(oldTexture::release);
                });
            }

            Log.d(TAG, "GL surface created");
            try {
                surfaceTexture = new SurfaceTexture(extTex);
                surfaceTexture.setDefaultBufferSize(videoW, videoH);
                surfaceTexture.setOnFrameAvailableListener(this, mainHandler);
                surface = new Surface(surfaceTexture);
            } catch (Throwable t) {
                Log.e(TAG, "cannot create SurfaceTexture", t);
                surfaceTexture = null;
                surface = null;
                return;
            }

            if (interpolation) applyFrameRate(TARGET_FPS);

            final Surface ready = surface;
            mainHandler.post(() -> {
                Callback cb = callback;
                if (cb != null) cb.onVideoSurfaceReady(ready);
            });
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            viewW = width;
            viewH = height;
            GLES20.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            SurfaceTexture st = surfaceTexture;
            if (st == null || copyProgram == 0 || drawProgram == 0) return;

            boolean up = upscaling && upscaleProgram != 0;
            int[] target = up ? computeTarget() : null;
            int bw = up ? target[0] : Math.max(1, viewW);
            int bh = up ? target[1] : Math.max(1, viewH);

            if (up && !ensureBuffer(true, bw, bh)) up = false;
            if (interpolation && !ensureBuffer(false, bw, bh)) {
                interpolation = false;
            }

            if (newFrame) {
                if (hasFrame && interpolation && prevTex != 0) {
                    copyToPrev(bw, bh);
                    prevTimestamp = currentTimestamp;
                    prevValid = true;
                }
                try {
                    st.updateTexImage();
                    st.getTransformMatrix(matrix);
                    currentTimestamp = st.getTimestamp();
                } catch (Throwable ignored) {
                }
                newFrame = false;
                hasFrame = true;
                lastSourceFrameNanos = System.nanoTime();
                everFramed = true;
            }

            if (up) {
                renderUpscaled(bw, bh);
            }
            composite(up, bw, bh);
        }

        @Override
        public void onFrameAvailable(SurfaceTexture st) {
            newFrame = true;
            frameCount++;
            if (frameCount % 120 == 0) {
                Log.d(TAG, "frames from vlc: " + frameCount);
            }
        }

        /**
         * Ukuran render FHD+ dengan rasio video: minimal sebesar FHD+ (panjang sisi 2400)
         * dan tidak lebih kecil dari ukuran tampilan, sehingga tidak ada minifikasi berat.
         */
        private int[] computeTarget() {
            int w = videoW;
            int h = videoH;
            if (w <= 0 || h <= 0) return new int[]{Math.max(1, viewW), Math.max(1, viewH)};

            float aspect = w / (float) h;
            float fhdScale = (aspect >= 1f) ? (FHD_PLUS_LONG / (float) w)
                    : (FHD_PLUS_LONG / (float) h);
            float viewScale = Math.max(viewW / (float) w, viewH / (float) h);
            float scale = Math.max(1f, Math.max(fhdScale, viewScale));

            long longEdge = Math.max(w, h);
            if (longEdge * scale > MAX_FRAME_EDGE) {
                scale = MAX_FRAME_EDGE / (float) longEdge;
            }

            int tw = Math.max(1, Math.round(w * scale));
            int th = Math.max(1, Math.round(h * scale));
            return new int[]{tw, th};
        }

        private void renderUpscaled(int width, int height) {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, currFbo);
            GLES20.glViewport(0, 0, width, height);
            GLES20.glClearColor(0f, 0f, 0f, 1f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glUseProgram(upscaleProgram);
            bindExternal(0);
            GLES20.glUniform1i(GLES20.glGetUniformLocation(upscaleProgram, "sExt"), 0);
            GLES20.glUniformMatrix4fv(uMtxUp, 1, false, matrix, 0);
            GLES20.glUniform2f(uSrcTexelUp,
                    1f / Math.max(1, videoW), 1f / Math.max(1, videoH));
            GLES20.glUniform1f(uSharpUp, 0.55f);
            drawQuad(aPosUp, aTexUp);
            currValid = true;
        }

        private void composite(boolean upscaled, int width, int height) {
            boolean mix = interpolation && prevValid && prevTex != 0;
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            GLES20.glViewport(0, 0, viewW, viewH);
            GLES20.glClearColor(0f, 0f, 0f, 1f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            float alpha = mix ? interpolationAlpha() : 0f;

            if (upscaled && currTex != 0 && currValid) {
                GLES20.glUseProgram(draw2dProgram);
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, currTex);
                GLES20.glUniform1i(uCurrDraw2d, 0);
                GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mix ? prevTex : 0);
                GLES20.glUniform1i(uPrevDraw2d, 1);
                GLES20.glUniform2f(uCurrTexelDraw2d, 1f / width, 1f / height);
                GLES20.glUniform2f(uFootprintDraw2d,
                        Math.max(1f, width / (float) Math.max(1, viewW)),
                        Math.max(1f, height / (float) Math.max(1, viewH)));
                GLES20.glUniform1f(uAlphaDraw2d, alpha);
                drawQuad(aPosDraw2d, aTexDraw2d);
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                return;
            }

            GLES20.glUseProgram(drawProgram);
            bindExternal(0);
            GLES20.glUniform1i(uExtDraw, 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mix ? prevTex : 0);
            GLES20.glUniform1i(uPrevDraw, 1);
            GLES20.glUniformMatrix4fv(uMtxDraw, 1, false, matrix, 0);
            GLES20.glUniform1f(uAlphaDraw, alpha);
            drawQuad(aPosDraw, aTexDraw);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        }

        /**
         * Nilai pencampuran 0..1 antara frame N-1 dan N, dikuantisasi pada grid 144 FPS.
         * 0 = frame N-1, 1 = frame N.
         */
        private float interpolationAlpha() {
            if (!interpolation || prevTimestamp <= 0L || currentTimestamp <= prevTimestamp) {
                return 0f;
            }
            long interval = currentTimestamp - prevTimestamp;
            long now = System.nanoTime();
            float a = (now - currentTimestamp) / (float) interval;
            if (a < 0f) a = 0f;
            if (a > 1f) a = 1f;
            float steps = Math.max(1f, interval * TARGET_FPS / 1_000_000_000f);
            return (float) Math.floor(a * steps) / steps;
        }

        private void copyToPrev(int width, int height) {
            if (prevFbo == 0) return;
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, prevFbo);
            GLES20.glViewport(0, 0, width, height);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glUseProgram(copyProgram);
            bindExternal(0);
            GLES20.glUniformMatrix4fv(uMtxCopy, 1, false, matrix, 0);
            drawQuad(aPosCopy, aTexCopy);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        }

        private void bindExternal(int unit) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + unit);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, extTex);
        }

        private void drawQuad(int aPos, int aTex) {
            positions.position(0);
            texCoords.position(0);
            GLES20.glEnableVertexAttribArray(aPos);
            GLES20.glVertexAttribPointer(aPos, 2, GLES20.GL_FLOAT, false, 0, positions);
            GLES20.glEnableVertexAttribArray(aTex);
            GLES20.glVertexAttribPointer(aTex, 2, GLES20.GL_FLOAT, false, 0, texCoords);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            GLES20.glDisableVertexAttribArray(aPos);
            GLES20.glDisableVertexAttribArray(aTex);
        }

        /** true jika buffer siap dipakai (dibuat bila perlu). */
        private boolean ensureBuffer(boolean current, int width, int height) {
            if (width <= 0 || height <= 0) return false;
            int tex = current ? currTex : prevTex;
            int fbo = current ? currFbo : prevFbo;
            int w = current ? currW : prevW;
            int h = current ? currH : prevH;
            if (tex != 0 && w == width && h == height) return true;

            if (fbo != 0) {
                GLES20.glDeleteFramebuffers(1, new int[]{fbo}, 0);
                if (current) currFbo = 0; else prevFbo = 0;
            }
            if (tex != 0) {
                GLES20.glDeleteTextures(1, new int[]{tex}, 0);
                if (current) currTex = 0; else prevTex = 0;
            }

            int[] t = new int[1];
            GLES20.glGenTextures(1, t, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t[0]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

            int[] f = new int[1];
            GLES20.glGenFramebuffers(1, f, 0);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, f[0]);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                    GLES20.GL_TEXTURE_2D, t[0], 0);
            int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
                Log.e(TAG, "framebuffer incomplete: " + status);
                GLES20.glDeleteTextures(1, t, 0);
                GLES20.glDeleteFramebuffers(1, f, 0);
                return false;
            }
            if (current) {
                currTex = t[0];
                currFbo = f[0];
                currW = width;
                currH = height;
                currValid = false;
            } else {
                prevTex = t[0];
                prevFbo = f[0];
                prevW = width;
                prevH = height;
                prevValid = false;
            }
            Log.d(TAG, (current ? "upscale FBO " : "prev FBO ") + width + "x" + height);
            return true;
        }

        private void releaseGl() {
            if (surfaceTexture != null) {
                try {
                    surfaceTexture.release();
                } catch (Throwable ignored) {
                }
                surfaceTexture = null;
            }
            surface = null;
            deleteBuffers(true);
            deleteBuffers(false);
            if (copyProgram != 0) {
                GLES20.glDeleteProgram(copyProgram);
                copyProgram = 0;
            }
            if (upscaleProgram != 0) {
                GLES20.glDeleteProgram(upscaleProgram);
                upscaleProgram = 0;
            }
            if (drawProgram != 0) {
                GLES20.glDeleteProgram(drawProgram);
                drawProgram = 0;
            }
            if (draw2dProgram != 0) {
                GLES20.glDeleteProgram(draw2dProgram);
                draw2dProgram = 0;
            }
            if (extTex != 0) {
                GLES20.glDeleteTextures(1, new int[]{extTex}, 0);
                extTex = 0;
            }
        }

        private void deleteBuffers(boolean current) {
            if (current) {
                if (currFbo != 0) {
                    GLES20.glDeleteFramebuffers(1, new int[]{currFbo}, 0);
                    currFbo = 0;
                }
                if (currTex != 0) {
                    GLES20.glDeleteTextures(1, new int[]{currTex}, 0);
                    currTex = 0;
                }
                currW = 0;
                currH = 0;
                currValid = false;
            } else {
                if (prevFbo != 0) {
                    GLES20.glDeleteFramebuffers(1, new int[]{prevFbo}, 0);
                    prevFbo = 0;
                }
                if (prevTex != 0) {
                    GLES20.glDeleteTextures(1, new int[]{prevTex}, 0);
                    prevTex = 0;
                }
                prevW = 0;
                prevH = 0;
                prevValid = false;
            }
        }
    }

    private static int createExternalTexture() {
        int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0]);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        return tex[0];
    }

    private static int createProgram(String vertexSource, String fragmentSource) {
        int vertex = compileShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int fragment = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (vertex == 0 || fragment == 0) return 0;
        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertex);
        GLES20.glAttachShader(program, fragment);
        GLES20.glLinkProgram(program);
        int[] link = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, link, 0);
        GLES20.glDeleteShader(vertex);
        GLES20.glDeleteShader(fragment);
        if (link[0] == 0) {
            Log.e(TAG, "program link failed: " + GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            return 0;
        }
        return program;
    }

    private static int compileShader(int type, String source) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "shader compile failed: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            return 0;
        }
        return shader;
    }

    private static FloatBuffer floatBuffer(float[] values) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(values.length * 4);
        buffer.order(ByteOrder.nativeOrder());
        FloatBuffer fb = buffer.asFloatBuffer();
        fb.put(values);
        fb.position(0);
        return fb;
    }
}
