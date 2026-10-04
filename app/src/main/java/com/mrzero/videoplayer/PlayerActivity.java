package com.mrzero.videoplayer;

import android.Manifest;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Rational;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.mrzero.tranplayer.IMediaPlayer;
import com.mrzero.tranplayer.ITimedText;
import com.mrzero.tranplayer.TranMediaPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Playback screen. Uses the very same engine as the original "Pemutar Visha" app:
 * com.mrzero.tranplayer.TranMediaPlayer -> libVLC JNI (libvlcjni/libvlc/libvlcffmpeg).
 */
public class PlayerActivity extends AppCompatActivity implements
        PlayerRootLayout.Listener,
        GLVideoView.Callback,
        IMediaPlayer.OnPreparedListener,
        IMediaPlayer.OnCompletionListener,
        IMediaPlayer.OnErrorListener,
        IMediaPlayer.OnInfoListener,
        IMediaPlayer.OnSeekCompleteListener,
        IMediaPlayer.OnVideoSizeChangedListener,
        IMediaPlayer.OnTimedTextListener,
        PlaybackService.PlayerBridge {

    private static final String EXTRA_URI = "extra_uri";
    private static final String EXTRA_TITLE = "extra_title";
    private static final String EXTRA_PATH = "extra_path";
    private static final String EXTRA_URIS = "extra_uris";
    private static final String EXTRA_TITLES = "extra_titles";
    private static final String EXTRA_PATHS = "extra_paths";
    private static final String EXTRA_INDEX = "extra_index";
    private static final int REQ_NOTIFY = 201;
    private static final String TAG = "PlayerActivity";
    private static final long SEEK_STEP_MS = 10_000L;

    private PlayerRootLayout root;
    private GLVideoView surface;
    private Surface videoSurface;
    private View controls;
    private TextView titleView;
    private TextView timeCurrent;
    private TextView timeTotal;
    private TextView indicator;
    private ProgressBar buffering;
    private ImageButton playButton;
    private ImageButton fullscreenButton;
    private ImageButton upscaleButton;
    private ImageButton interpolateButton;
    private ImageButton prevButton;
    private ImageButton nextButton;
    private ImageButton back10Button;
    private ImageButton fwd10Button;
    private SeekBar seekBar;

    private TranMediaPlayer player;
    private Uri mediaUri;
    private String mediaPath;
    private String mediaTitle;

    private final List<String> playlistUris = new ArrayList<>();
    private final List<String> playlistTitles = new ArrayList<>();
    private final List<String> playlistPaths = new ArrayList<>();
    private int playlistIndex;

    private boolean prepared;
    private boolean controlsVisible = true;
    private boolean userSeeking;
    private boolean userPaused;
    private boolean fullscreen;
    private int videoWidth;
    private int videoHeight;
    private int pipEntered;

    private final Handler ui = new Handler(Looper.getMainLooper());
    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            refreshTime();
            refreshPlayButton();
            ui.postDelayed(this, 500);
        }
    };

    public static void open(Context ctx, Uri uri, String title, String path) {
        Intent i = new Intent(ctx, PlayerActivity.class);
        i.setData(uri);
        i.putExtra(EXTRA_URI, uri == null ? null : uri.toString());
        i.putExtra(EXTRA_TITLE, title);
        i.putExtra(EXTRA_PATH, path);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }

    /** Buka pemutar dengan daftar video (untuk navigasi video sebelumnya/berikutnya). */
    public static void open(Context ctx, List<MediaItem> items, int index) {
        if (items == null || items.isEmpty()) return;
        int idx = Math.max(0, Math.min(index, items.size() - 1));
        MediaItem first = items.get(idx);

        ArrayList<String> uris = new ArrayList<>(items.size());
        ArrayList<String> titles = new ArrayList<>(items.size());
        ArrayList<String> paths = new ArrayList<>(items.size());
        for (MediaItem item : items) {
            uris.add(item.uri == null ? null : item.uri.toString());
            titles.add(item.title);
            paths.add(item.path);
        }

        Intent i = new Intent(ctx, PlayerActivity.class);
        i.setData(first.uri);
        i.putExtra(EXTRA_URI, first.uri == null ? null : first.uri.toString());
        i.putExtra(EXTRA_TITLE, first.title);
        i.putExtra(EXTRA_PATH, first.path);
        i.putStringArrayListExtra(EXTRA_URIS, uris);
        i.putStringArrayListExtra(EXTRA_TITLES, titles);
        i.putStringArrayListExtra(EXTRA_PATHS, paths);
        i.putExtra(EXTRA_INDEX, idx);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(i);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_player);

        root = findViewById(R.id.root);
        surface = findViewById(R.id.surface);
        controls = findViewById(R.id.controls);
        titleView = findViewById(R.id.title);
        timeCurrent = findViewById(R.id.time_current);
        timeTotal = findViewById(R.id.time_total);
        indicator = findViewById(R.id.indicator);
        buffering = findViewById(R.id.buffering);
        playButton = findViewById(R.id.btn_play);
        fullscreenButton = findViewById(R.id.btn_fullscreen);
        seekBar = findViewById(R.id.seek);

        root.setListener(this);
        surface.setCallback(this);
        root.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, oR, oB) -> applyVideoSize());

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        playButton.setOnClickListener(v -> togglePlay());
        fullscreenButton.setOnClickListener(v -> applyFullscreen(!fullscreen));

        upscaleButton = findViewById(R.id.btn_upscale);
        interpolateButton = findViewById(R.id.btn_interpolate);
        upscaleButton.setOnClickListener(v -> {
            boolean on = !surface.isUpscaling();
            surface.setUpscaling(on);
            upscaleButton.setSelected(on);
            showIndicator(getString(on ? R.string.upscale_on : R.string.upscale_off));
        });
        interpolateButton.setOnClickListener(v -> {
            boolean on = !surface.isFrameInterpolation();
            surface.setFrameInterpolation(on);
            interpolateButton.setSelected(on);
            if (!on) surface.flushInterpolation();
            showIndicator(getString(on ? R.string.interpolation_on : R.string.interpolation_off));
        });

        back10Button = findViewById(R.id.btn_back10);
        fwd10Button = findViewById(R.id.btn_fwd10);
        prevButton = findViewById(R.id.btn_prev_video);
        nextButton = findViewById(R.id.btn_next_video);
        back10Button.setOnClickListener(v -> skipSeconds(-SEEK_STEP_MS));
        fwd10Button.setOnClickListener(v -> skipSeconds(SEEK_STEP_MS));
        prevButton.setOnClickListener(v -> playPrevious());
        nextButton.setOnClickListener(v -> playNext());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser) timeCurrent.setText(MainActivity.formatDuration(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar sb) {
                userSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar sb) {
                userSeeking = false;
                doSeek(sb.getProgress());
            }
        });

        resolveIntent(getIntent());

        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIFY);
        }

        applyFullscreen(getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE);
    }

    // -------------------------------------------------------------- fullscreen

    private void applyFullscreen(boolean enable) {
        fullscreen = enable;
        setRequestedOrientation(enable
                ? ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        WindowInsetsControllerCompat c = WindowCompat.getInsetsController(
                getWindow(), getWindow().getDecorView());
        if (enable) {
            c.hide(WindowInsetsCompat.Type.systemBars());
            c.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        } else {
            c.show(WindowInsetsCompat.Type.systemBars());
        }
        fullscreenButton.setImageResource(enable
                ? R.drawable.ic_fullscreen_exit : R.drawable.ic_fullscreen);
        fullscreenButton.setContentDescription(getString(enable
                ? R.string.exit_fullscreen : R.string.fullscreen));
        ui.post(this::applyVideoSize);
    }

    private void resolveIntent(Intent intent) {
        if (intent == null) return;
        Uri data = intent.getData();
        if (data == null) {
            String uriStr = intent.getStringExtra(EXTRA_URI);
            if (uriStr != null) data = Uri.parse(uriStr);
        }
        if (data != null) mediaUri = data;
        if (intent.hasExtra(EXTRA_PATH)) mediaPath = intent.getStringExtra(EXTRA_PATH);
        if (intent.hasExtra(EXTRA_TITLE)) mediaTitle = intent.getStringExtra(EXTRA_TITLE);

        if (mediaTitle == null && mediaUri != null) {
            mediaTitle = mediaUri.getLastPathSegment();
        }
        if (Intent.ACTION_SEND.equals(intent.getAction())) {
            String text = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (text != null && (text.startsWith("http://") || text.startsWith("https://"))) {
                mediaUri = Uri.parse(text.trim());
                mediaTitle = mediaUri.getLastPathSegment();
            }
        }
        if (titleView != null) titleView.setText(mediaTitle == null ? getString(R.string.app_name) : mediaTitle);

        ArrayList<String> uris = intent.getStringArrayListExtra(EXTRA_URIS);
        ArrayList<String> titles = intent.getStringArrayListExtra(EXTRA_TITLES);
        ArrayList<String> paths = intent.getStringArrayListExtra(EXTRA_PATHS);
        playlistUris.clear();
        playlistTitles.clear();
        playlistPaths.clear();
        if (uris != null && !uris.isEmpty()) {
            playlistUris.addAll(uris);
            if (titles != null) playlistTitles.addAll(titles);
            if (paths != null) playlistPaths.addAll(paths);
            while (playlistTitles.size() < playlistUris.size()) playlistTitles.add(null);
            while (playlistPaths.size() < playlistUris.size()) playlistPaths.add(null);
            playlistIndex = intent.getIntExtra(EXTRA_INDEX, 0);
        }
        refreshTransport();
    }

    /** Aktifkan/nonaktifkan tombol navigasi video sesuai panjang daftar. */
    private void refreshTransport() {
        boolean hasPlaylist = playlistUris.size() > 1;
        if (prevButton != null) prevButton.setEnabled(hasPlaylist);
        if (nextButton != null) nextButton.setEnabled(hasPlaylist);
        if (prevButton != null) prevButton.setAlpha(hasPlaylist ? 1f : 0.35f);
        if (nextButton != null) nextButton.setAlpha(hasPlaylist ? 1f : 0.35f);
    }

    // ---------------------------------------------------------------- playback

    private void startPlayback() {
        if (player != null || mediaUri == null) return;
        try {
            player = new TranMediaPlayer(this);
            player.setOnPreparedListener(this);
            player.setOnCompletionListener(this);
            player.setOnErrorListener(this);
            player.setOnInfoListener(this);
            player.setOnSeekCompleteListener(this);
            player.setOnVideoSizeChangedListener(this);
            player.setOnTimedTextListener(this);
            player.setScreenOnWhilePlaying(true);
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .build());

            HashMap<String, String> headers = new HashMap<>();
            if (mediaPath != null && !mediaPath.isEmpty()) {
                player.setDataSource(mediaPath, headers);
            } else {
                player.setDataSource(this, mediaUri, headers);
            }

            if (videoSurface != null) {
                player.setSurface(videoSurface);
            }
            player.prepareAsync();
            buffering.setVisibility(View.VISIBLE);
            PlaybackService.attach(this);
        } catch (Throwable t) {
            Toast.makeText(this, R.string.playback_error, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void togglePlay() {
        if (player == null || !prepared) return;
        try {
            if (player.isPlaying()) {
                if (isFramesActive()) {
                    player.pause();
                    userPaused = true;
                } else {
                    // Frame stop padahal VLC bilang main: kick ulang decoder.
                    player.pause();
                    player.start();
                    userPaused = false;
                }
            } else {
                player.start();
                userPaused = false;
            }
        } catch (Throwable ignored) {
        }
        refreshPlayButton();
        PlaybackService.notifyStateChanged(this);
    }

    private void doSeek(long positionMs) {
        if (player == null || !prepared) return;
        long dur = safeDuration();
        if (dur > 0) positionMs = Math.max(0, Math.min(positionMs, dur));
        try {
            player.seekTo(positionMs, false, true);
        } catch (Throwable ignored) {
        }
        surface.flushInterpolation();
        timeCurrent.setText(MainActivity.formatDuration(positionMs));
        refreshTime();
    }

    private long safeDuration() {
        try {
            return player != null ? player.getDuration() : 0L;
        } catch (Throwable t) {
            return 0L;
        }
    }

    private void skipSeconds(long deltaMs) {
        long target = safePosition() + deltaMs;
        long dur = safeDuration();
        if (dur > 0) target = Math.max(0, Math.min(target, dur));
        else target = Math.max(0, target);
        doSeek(target);
        showIndicator((deltaMs < 0 ? "« " : "» ") + MainActivity.formatDuration(target));
    }

    private void playPrevious() {
        if (playlistUris.isEmpty()) return;
        if (playlistIndex > 0) {
            playAt(playlistIndex - 1);
        } else {
            doSeek(0);
        }
    }

    private void playNext() {
        if (playlistIndex < playlistUris.size() - 1) {
            playAt(playlistIndex + 1);
        }
    }

    /** Ganti video yang diputar (tombol video sebelumnya / berikutnya). */
    private void playAt(int index) {
        if (index < 0 || index >= playlistUris.size()) return;
        playlistIndex = index;
        String uriStr = playlistUris.get(index);
        mediaUri = uriStr == null ? null : Uri.parse(uriStr);
        mediaPath = playlistPaths.get(index);
        mediaTitle = playlistTitles.get(index);
        if (mediaTitle == null && mediaUri != null) mediaTitle = mediaUri.getLastPathSegment();

        prepared = false;
        userPaused = false;
        userSeeking = false;
        videoWidth = 0;
        videoHeight = 0;

        if (titleView != null) {
            titleView.setText(mediaTitle == null ? getString(R.string.app_name) : mediaTitle);
        }
        timeCurrent.setText("00:00");
        timeTotal.setText("00:00");
        seekBar.setProgress(0);
        refreshPlayButton();
        buffering.setVisibility(View.VISIBLE);

        releasePlayer();
        if (surface != null) surface.flushInterpolation();
        startPlayback();
        PlaybackService.notifyStateChanged(this);
    }

    private void releasePlayer() {
        if (player == null) return;
        try {
            player.setDisplay(null);
        } catch (Throwable ignored) {
        }
        try {
            player.setSurface(null);
        } catch (Throwable ignored) {
        }
        try {
            player.release();
        } catch (Throwable ignored) {
        }
        player = null;
        prepared = false;
    }

    private long safePosition() {
        try {
            return player != null ? player.getCurrentPosition() : 0L;
        } catch (Throwable t) {
            return 0L;
        }
    }

    /**
     * Sinkron dengan deteksi frame: icon pause hanya bila masih ada frame masuk,
     * icon play bila frame stop (pause/putus/selesai) atau masih buffering.
     */
    private void refreshPlayButton() {
        boolean playing;
        if (userPaused) {
            playing = false;
        } else if (isFramesActive()) {
            playing = true;
        } else {
            try {
                // Buffering awal: VLC sudah main tapi frame belum pernah masuk.
                playing = player != null && player.isPlaying()
                        && (surface == null || !surface.hasFrameEver());
            } catch (Throwable ignored) {
                playing = false;
            }
        }
        playButton.setImageResource(playing ? R.drawable.ic_pause : R.drawable.ic_play);
    }

    /** True bila frame video masih mengalir (bila false = frame stop). */
    private boolean isFramesActive() {
        return surface != null && surface.isFrameActive();
    }

    private void refreshTime() {
        if (userSeeking) return;
        long dur = safeDuration();
        long pos = safePosition();
        timeCurrent.setText(MainActivity.formatDuration(pos));
        timeTotal.setText(MainActivity.formatDuration(dur));
        if (dur > 0) {
            seekBar.setMax((int) Math.min(dur, Integer.MAX_VALUE));
            seekBar.setProgress((int) Math.min(pos, Integer.MAX_VALUE));
        }
    }

    private void showIndicator(String text) {
        indicator.setText(text);
        indicator.setVisibility(View.VISIBLE);
        ui.removeCallbacks(hideIndicator);
        ui.postDelayed(hideIndicator, 900);
    }

    private final Runnable hideIndicator = () -> indicator.setVisibility(View.GONE);

    // ------------------------------------------------------------ video sink

    @Override
    public void onVideoSurfaceReady(Surface s) {
        videoSurface = s;
        Log.d(TAG, "video surface ready");
        if (player == null) {
            startPlayback();
        } else {
            try {
                player.setSurface(s);
            } catch (Throwable ignored) {
            }
            maybeResume();
        }
    }
    @Override
    public void onVideoSurfaceDestroyed() {
        videoSurface = null;
        if (player != null) {
            try {
                player.setDisplay(null);
            } catch (Throwable ignored) {
            }
        }
    }

    /** Lanjutkan pemutaran setelah surface GPU dibuat ulang (kembali dari background). */
    private void maybeResume() {
        if (player == null || !prepared || userPaused) return;
        try {
            if (!player.isPlaying()) {
                player.start();
                Log.d(TAG, "player.start() playing=" + player.isPlaying() + " pos=" + safePosition());
                ui.postDelayed(() -> Log.d(TAG, "after 1s playing=" + bridgeIsPlaying()
                        + " pos=" + safePosition() + " dur=" + safeDuration()), 1000);
            }
        } catch (Throwable ignored) {
        }
        refreshPlayButton();
        PlaybackService.notifyStateChanged(this);
    }

    @Override
    public void onPrepared(IMediaPlayer mp) {
        prepared = true;
        Log.d(TAG, "onPrepared");
        buffering.setVisibility(View.GONE);
        refreshPlayButton();
        refreshTime();
        PlaybackService.notifyStateChanged(this);
        maybeResume();
    }

    @Override
    public void onCompletion(IMediaPlayer mp) {
        if (playlistIndex < playlistUris.size() - 1) {
            playAt(playlistIndex + 1);
            return;
        }
        refreshPlayButton();
        PlaybackService.notifyStateChanged(this);
    }

    @Override
    public boolean onError(IMediaPlayer mp, int what, int extra) {
        buffering.setVisibility(View.GONE);
        Toast.makeText(this, R.string.playback_error, Toast.LENGTH_LONG).show();
        return true;
    }

    @Override
    public boolean onInfo(IMediaPlayer mp, int what, int extra) {
        if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_START) {
            buffering.setVisibility(View.VISIBLE);
        } else if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_END) {
            buffering.setVisibility(View.GONE);
        }
        return false;
    }

    @Override
    public void onSeekComplete(IMediaPlayer mp) {
        refreshTime();
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sarNum, int sarDen) {
        Log.d(TAG, "video size " + width + "x" + height);
        videoWidth = width;
        videoHeight = height;
        surface.setVideoSize(width, height);
        surface.flushInterpolation();
        applyVideoSize();
    }

    /**
     * Sesuaikan ukuran SurfaceView dengan rasio video (fit / letterbox),
     * berlaku sama di portrait maupun landscape.
     */
    private void applyVideoSize() {
        if (videoWidth <= 0 || videoHeight <= 0) return;
        int rootW = root.getWidth();
        int rootH = root.getHeight();
        if (rootW <= 0 || rootH <= 0) return;

        float aspect = videoWidth / (float) videoHeight;
        int w;
        int h;
        if (rootW / (float) rootH > aspect) {
            h = rootH;
            w = Math.round(h * aspect);
        } else {
            w = rootW;
            h = Math.round(w / aspect);
        }

        ViewGroup.LayoutParams current = surface.getLayoutParams();
        if (current != null && current.width == w && current.height == h) return;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(w, h, Gravity.CENTER);
        surface.setLayoutParams(params);
    }

    @Override
    public void onTimedText(IMediaPlayer mp, ITimedText text) {
        // subtitle rendering hook (same listener contract as VishaTranPlayer)
    }

    // --------------------------------------------------------------- gestures

    @Override
    public void onSingleTap() {
        controlsVisible = !controlsVisible;
        controls.setVisibility(controlsVisible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDoubleTap(boolean forward) {
        long pos = safePosition();
        long dur = safeDuration();
        long target = forward ? pos + SEEK_STEP_MS : pos - SEEK_STEP_MS;
        if (dur > 0) target = Math.max(0, Math.min(target, dur));
        doSeek(target);
        showIndicator((forward ? "» " : "« ") + MainActivity.formatDuration(target));
    }

    private float gestureBaseValue;
    private boolean gestureActive;

    @Override
    public void onGestureStart(boolean horizontal, boolean leftSide) {
        gestureActive = true;
        if (horizontal) {
            showIndicator(MainActivity.formatDuration(safePosition()));
        } else if (leftSide) {
            gestureBaseValue = currentBrightness();
            showIndicator("☀ " + Math.round(gestureBaseValue * 100) + "%");
        } else {
            gestureBaseValue = currentVolumeRatio();
            showIndicator("🔊 " + Math.round(gestureBaseValue * 100) + "%");
        }
    }

    @Override
    public void onGestureMove(boolean horizontal, boolean leftSide, float deltaRatio, float rawDelta) {
        if (horizontal) {
            long dur = safeDuration();
            long pos = safePosition();
            long target = pos + (long) (deltaRatio * Math.max(dur, 10_000L));
            if (dur > 0) target = Math.max(0, Math.min(target, dur));
            showIndicator(MainActivity.formatDuration(target));
            timeCurrent.setText(MainActivity.formatDuration(target));
        } else if (leftSide) {
            float v = clamp01(gestureBaseValue - deltaRatio);
            setBrightness(v);
            showIndicator("☀ " + Math.round(v * 100) + "%");
        } else {
            AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
            int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int cur = am.getStreamVolume(AudioManager.STREAM_MUSIC);
            int next = clampInt(0, max, Math.round(cur - deltaRatio * max));
            if (next != cur) {
                am.setStreamVolume(AudioManager.STREAM_MUSIC, next, 0);
            }
            showIndicator("🔊 " + Math.round(next / (float) max * 100) + "%");
        }
    }

    @Override
    public void onGestureEnd(boolean horizontal, boolean leftSide, float deltaRatio, float rawDelta) {
        gestureActive = false;
        if (horizontal && player != null) {
            long dur = safeDuration();
            long target = safePosition() + (long) (deltaRatio * Math.max(dur, 10_000L));
            doSeek(target);
            PlaybackService.notifyStateChanged(this);
        }
        refreshTime();
    }

    @Override
    public void onGestureCancel() {
        gestureActive = false;
        refreshTime();
    }

    private float currentBrightness() {
        float b = getWindow().getAttributes().screenBrightness;
        return b < 0 ? 0.5f : b;
    }

    private void setBrightness(float value) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = value;
        getWindow().setAttributes(lp);
    }

    private float currentVolumeRatio() {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        return am.getStreamVolume(AudioManager.STREAM_MUSIC)
                / (float) Math.max(1, am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private static int clampInt(int lo, int hi, int v) {
        return Math.max(lo, Math.min(hi, v));
    }

    // -------------------------------------------------------- lifecycle / pip

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        resolveIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ui.post(ticker);
        PlaybackService.attach(this);
        maybeResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ui.removeCallbacks(ticker);
        if (isInPictureInPictureModeCompat()) {
            // keep playing in PiP
        } else {
            try {
                if (player != null && player.isPlaying()) player.pause();
            } catch (Throwable ignored) {
            }
        }
        PlaybackService.notifyStateChanged(this);
    }

    private boolean isInPictureInPictureModeCompat() {
        if (Build.VERSION.SDK_INT >= 24) {
            return pipEntered > 0;
        }
        return false;
    }

    @Override
    public void onUserLeaveHint() {
        super.onUserLeaveHint();
        enterPip();
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode,
                                              @NonNull Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        pipEntered = isInPictureInPictureMode ? 1 : 0;
        controls.setVisibility(isInPictureInPictureMode || !controlsVisible ? View.GONE : View.VISIBLE);
    }

    private void enterPip() {
        if (Build.VERSION.SDK_INT < 26) return;
        try {
            if (player == null || !prepared || !player.isPlaying()) return;
            PictureInPictureParams params = new PictureInPictureParams.Builder()
                    .setAspectRatio(new Rational(16, 9))
                    .build();
            enterPictureInPictureMode(params);
            pipEntered = 1;
        } catch (Throwable ignored) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ui.removeCallbacks(ticker);
        ui.removeCallbacks(hideIndicator);
        PlaybackService.detach(this);
        if (player != null) {
            try {
                player.setDisplay(null);
            } catch (Throwable ignored) {
            }
            try {
                player.release();
            } catch (Throwable ignored) {
            }
            player = null;
        }
        if (surface != null) surface.release();
    }

    // ------------------------------------------------------------- PlayerBridge

    @Override
    public void bridgePlay() {
        togglePlay();
    }

    @Override
    public void bridgePause() {
        if (player != null && prepared) {
            try {
                player.pause();
            } catch (Throwable ignored) {
            }
            refreshPlayButton();
        }
    }

    @Override
    public void bridgeSeekTo(long positionMs) {
        doSeek(positionMs);
    }

    @Override
    public void bridgeStop() {
        finish();
    }

    @Override
    public long bridgePosition() {
        return safePosition();
    }

    @Override
    public long bridgeDuration() {
        return safeDuration();
    }

    @Override
    public boolean bridgeIsPlaying() {
        try {
            return player != null && player.isPlaying();
        } catch (Throwable t) {
            return false;
        }
    }

    @Override
    public String bridgeTitle() {
        return mediaTitle;
    }
}
