package com.mrzero.videoplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.lang.ref.WeakReference;

/**
 * Foreground media-playback service exposing the same MediaSession contract
 * (play/pause/seek/stop + lockscreen controls) as the original player.
 */
public class PlaybackService extends Service {

    public interface PlayerBridge {
        void bridgePlay();

        void bridgePause();

        void bridgeSeekTo(long positionMs);

        void bridgeStop();

        long bridgePosition();

        long bridgeDuration();

        boolean bridgeIsPlaying();

        String bridgeTitle();
    }

    private static final String CHANNEL = "playback";
    private static final int NOTIF_ID = 7401;

    public static final String ACTION_UPDATE = "com.mrzero.videoplayer.action.UPDATE";
    public static final String ACTION_PLAY = "com.mrzero.videoplayer.action.PLAY";
    public static final String ACTION_PAUSE = "com.mrzero.videoplayer.action.PAUSE";
    public static final String ACTION_FWD = "com.mrzero.videoplayer.action.FWD";
    public static final String ACTION_BACK = "com.mrzero.videoplayer.action.BACK";

    private static WeakReference<PlayerBridge> bridgeRef = new WeakReference<>(null);

    private MediaSessionCompat session;

    public static void attach(PlayerBridge bridge) {
        bridgeRef = new WeakReference<>(bridge);
    }

    public static void detach(PlayerBridge bridge) {
        PlayerBridge current = bridgeRef.get();
        if (current == null || current == bridge) {
            bridgeRef = new WeakReference<>(null);
        }
    }

    public static void notifyStateChanged(Context ctx) {
        if (bridgeRef.get() == null) return;
        start(ctx, ACTION_UPDATE);
    }

    private static void start(Context ctx, String action) {
        Intent i = new Intent(ctx, PlaybackService.class).setAction(action);
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                ctx.startForegroundService(i);
            } else {
                ctx.startService(i);
            }
        } catch (Throwable ignored) {
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createChannel();
        session = new MediaSessionCompat(this, "VideoPlayer");
        session.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        session.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                PlayerBridge b = bridgeRef.get();
                if (b != null) b.bridgePlay();
            }

            @Override
            public void onPause() {
                PlayerBridge b = bridgeRef.get();
                if (b != null) b.bridgePause();
            }

            @Override
            public void onSeekTo(long positionMs) {
                PlayerBridge b = bridgeRef.get();
                if (b != null) b.bridgeSeekTo(positionMs);
            }

            @Override
            public void onStop() {
                PlayerBridge b = bridgeRef.get();
                if (b != null) b.bridgeStop();
            }
        });
        session.setActive(true);
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL,
                    getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW);
            channel.setShowBadge(false);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent != null ? intent.getAction() : ACTION_UPDATE;
        PlayerBridge bridge = bridgeRef.get();

        // Penuhi syarat foreground service lebih dulu, sebelum ada jalur yang bisa
        // return/throw (system memaksa startForeground() dalam beberapa detik setelah
        // startForegroundService(), termasuk saat izin notifikasi belum diberikan).
        try {
            startForegroundCompat(new NotificationCompat.Builder(this, CHANNEL)
                    .setSmallIcon(R.drawable.ic_play)
                    .setContentTitle(getString(R.string.app_name))
                    .setOnlyAlertOnce(true)
                    .build());
        } catch (Throwable ignored) {
        }

        if (bridge == null) {
            teardown();
            return START_NOT_STICKY;
        }

        if (ACTION_PLAY.equals(action)) {
            bridge.bridgePlay();
        } else if (ACTION_PAUSE.equals(action)) {
            bridge.bridgePause();
        } else if (ACTION_FWD.equals(action)) {
            bridge.bridgeSeekTo(bridge.bridgePosition() + 10_000L);
        } else if (ACTION_BACK.equals(action)) {
            bridge.bridgeSeekTo(Math.max(0, bridge.bridgePosition() - 10_000L));
        }

        boolean playing = bridge.bridgeIsPlaying();
        long position = bridge.bridgePosition();
        long duration = bridge.bridgeDuration();
        String title = bridge.bridgeTitle();

        Intent open = new Intent(this, PlayerActivity.class);
        PendingIntent content = PendingIntent.getActivity(this, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL)
                .setSmallIcon(R.drawable.ic_play)
                .setContentTitle(title == null ? getString(R.string.app_name) : title)
                .setContentIntent(content)
                .setOngoing(playing)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(session.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .addAction(new NotificationCompat.Action(android.R.drawable.ic_media_previous,
                        "back", dispatch(ACTION_BACK)))
                .addAction(new NotificationCompat.Action(
                        playing ? R.drawable.ic_pause : R.drawable.ic_play,
                        playing ? "pause" : "play",
                        dispatch(playing ? ACTION_PAUSE : ACTION_PLAY)))
                .addAction(new NotificationCompat.Action(android.R.drawable.ic_media_ff,
                        "fwd", dispatch(ACTION_FWD)));

        Notification notification = builder.build();
        try {
            NotificationManagerCompat.from(this).notify(NOTIF_ID, notification);
        } catch (Throwable ignored) {
            // Izin POST_NOTIFICATIONS belum diberikan: abaikan, startForeground tetap jalan.
        }
        try {
            startForegroundCompat(notification);
        } catch (Throwable ignored) {
        }

        session.setPlaybackState(new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_PLAY_PAUSE
                        | PlaybackStateCompat.ACTION_SEEK_TO
                        | PlaybackStateCompat.ACTION_STOP)
                .setState(playing ? PlaybackStateCompat.STATE_PLAYING
                                : PlaybackStateCompat.STATE_PAUSED,
                        position, 1.0f)
                .build());
        session.setMetadata(new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                .build());

        return START_NOT_STICKY;
    }

    private void startForegroundCompat(Notification notification) {
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIF_ID, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(NOTIF_ID, notification);
        }
    }

    private PendingIntent dispatch(String action) {
        Intent i = new Intent(this, PlaybackService.class).setAction(action);
        return PendingIntent.getService(this, action.hashCode() & 0x7fffffff, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private void teardown() {
        if (Build.VERSION.SDK_INT >= 24) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (session != null) session.release();
        NotificationManagerCompat.from(this).cancel(NOTIF_ID);
    }
}
