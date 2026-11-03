#!/usr/bin/env bash
# Installe l'APK sur l'émulateur, au format d'un écran d'autoradio 1280 x 720,
# puis photographie les principaux écrans du launcher.
set -x

PKG=be.corsa.launcher
APK=$(ls apk/*.apk | head -n 1)
OUT=shots
mkdir -p "$OUT"

adb wait-for-device
adb shell wm size 1280x720
adb shell wm density 160
adb shell settings put system user_rotation 0
adb install -r -g "$APK"
adb shell cmd notification allow_listener "$PKG/$PKG.MediaListener" || true

shot() {
  local name=$1
  local wait=$2
  shift 2
  adb shell am force-stop "$PKG"
  adb shell am start -W -n "$PKG/.MainActivity" "$@"
  sleep "$wait"
  adb exec-out screencap -p > "$OUT/$name.png"
}

shot 01-accueil-jour 25 --es demo_theme jour
shot 02-accueil-nuit 15 --es demo_theme nuit
shot 03-applis 10 --es demo_theme jour --es demo_sheet Apps
shot 04-musique 8 --es demo_theme jour --es demo_sheet Music
shot 05-recherche 8 --es demo_theme jour --es demo_sheet Search
shot 06-reglages 8 --es demo_theme jour --es demo_sheet Settings
shot 07-vehicule 8 --es demo_theme nuit --es demo_sheet Vehicle

adb logcat -d -b crash > "$OUT/crash-log.txt" || true
ls -la "$OUT"
