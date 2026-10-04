#!/usr/bin/env bash
# Ekspor SVG (Inkscape) -> PNG per density untuk proyek Android VideoPlayer.
# Sumber: design/*.svg   Hasil: app/src/main/res/{drawable,mipmap}-*dpi/*.png
set -euo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
RES="$(cd "$HERE/../app/src/main/res" && pwd)"

DENSITIES=(mdpi hdpi xhdpi xxhdpi xxxhdpi)
SCALES=(1 1.5 2 3 4)

export_png() {
    local svg="$1" name="$2" base_dp="$3" outdir="$4"
    local i dens scale w
    for i in "${!DENSITIES[@]}"; do
        dens="${DENSITIES[$i]}"
        scale="${SCALES[$i]}"
        w=$(python3 -c "print(int(round($base_dp * $scale)))")
        mkdir -p "$RES/$outdir-$dens"
        inkscape --export-type=png \
                 --export-filename="$RES/$outdir-$dens/$name.png" \
                 --export-width="$w" \
                 "$HERE/$svg" >/dev/null 2>&1
        echo "  $outdir-$dens/$name.png  ${w}px"
    done
}

# Ekspor ter-crop ke area gambar (tanpa margin kanvas) -> penuh sampai tepi.
export_png_drawing() {
    local svg="$1" name="$2" base_dp="$3" outdir="$4"
    local i dens scale w
    for i in "${!DENSITIES[@]}"; do
        dens="${DENSITIES[$i]}"
        scale="${SCALES[$i]}"
        w=$(python3 -c "print(int(round($base_dp * $scale)))")
        mkdir -p "$RES/$outdir-$dens"
        inkscape --export-type=png \
                 --export-filename="$RES/$outdir-$dens/$name.png" \
                 --export-width="$w" \
                 --export-area-drawing \
                 "$HERE/$svg" >/dev/null 2>&1
        echo "  $outdir-$dens/$name.png  ${w}px"
    done
}

echo "== ikon kontrol (24dp) =="
for n in ic_back ic_play ic_pause ic_fullscreen ic_fullscreen_exit ic_search ic_upscale ic_interpolate; do
    export_png "$n.svg" "$n" 24 drawable
done

echo "== ikon transport (46dp, safe-area 512) =="
for n in ic_back10 ic_fwd10 ic_prev ic_next; do
    export_png "$n.svg" "$n" 46 drawable
done

echo "== placeholder thumbnail (128x76dp) =="
export_png ic_placeholder.svg ic_placeholder 128 drawable

echo "== logo aplikasi: header (26dp) =="
export_png_drawing ic_launcher.svg ic_logo 26 drawable

echo "== logo aplikasi: launcher legacy (48dp) =="
export_png_drawing ic_launcher.svg ic_launcher 48 mipmap

echo "Selesai."
