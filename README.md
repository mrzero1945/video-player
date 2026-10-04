# Video Player

Pemutar video Android dengan mesin libVLC/FFmpeg (JNI), galeri berbasis folder, upscale GPU ke FHD+, dan interpolasi frame 144 FPS.

- Package: `com.mrzero.videoplayer`
- Min SDK: 24 (Android 7.0) — Target SDK: 34 — ABI: `arm64-v8a`

## Fitur

**Galeri**
- Grid folder (3 kolom) → grid video (4 kolom) dengan thumbnail, judul, dan durasi
- Pencarian video per folder, header dengan tombol kembali
- Tahan-panjang pada video → dialog **Hapus** (MediaStore → file → fallback `su rm`)

**Pemutaran**
- Fullscreen, seek bar, PiP (Android 8+), kontrol dari layar kunci/notifikasi (MediaSession)
- Transport: video sebelumnya/berikutnya, maju/mundur 10 detik
- Tombol play/pause tersinkron dengan deteksi frame stop (jendela 600 ms)
- **Upscale FHD+**: render ke FBO minimal 2400 px sisi panjang dengan rasio video asli
- **Interpolasi 144 FPS**: blend frame sumber (alpha = fraksi waktu) agar gerakan halus saat display 120/144 Hz
- Posisi, pref toggle upscale/interpolasi, dan playlist tersimpan antar sesi

**Tema**
- Dark theme aktif sebagai default (galeri + pemutar)

## Arsitektur singkat

```
MediaStore ── MainActivity (galeri)
                    │
                    ▼
             PlayerActivity ── TranMediaPlayer (JNI) ── libvlcjni.so / libvlc.so / libvlcffmpeg.so
                    │                                        │
                    ▼                                        ▼ surface
             GLVideoView  : OES texture → [upscale FBO] → [interpolasi prev/curr] → layar
```

- `com.mrzero.videoplayer` — UI, galeri, layanan playback, pipeline GL.
- `com.mrzero.tranplayer` — lapisan JNI libVLC (kelas, metode native, kontrak `IMediaPlayer`).
- Pustaka native di `app/src/main/jniLibs/arm64-v8a/`. Symbol JNI di dalam `.so` sudah di-patch dari prefix lama menjadi `com_mrzero_tranplayer` / `com/mrzero/tranplayer`, sehingga nama package Java harus dipertahankan.

## Build

```bash
./gradlew assembleDebug
# hasil: app/build/outputs/apk/debug/app-debug.apk
```

Syarat: JDK 17, Android SDK (compileSdk 35), Gradle 8.14 + AGP 8.13 (memakai wrapper).

Install ke perangkat:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Catatan

- Mesin pemutar memakai pustaka native hasil reverse-engineering pemutar bawaan perangkat; kode Java pada `com.mrzero.tranplayer` adalah glue layer-nya.
- Untuk membangun ulang penuh, `jniLibs` wajib ikut di-commit (jangan di-ignore).

## Lisensi

[GPL-3.0](LICENSE)
