# baosp-braille

[![Android CI — baosp-braille](https://github.com/tech-master33/baosp-braille/actions/workflows/android.yml/badge.svg)](https://github.com/tech-master33/baosp-braille/actions/workflows/android.yml)

A Bluetooth HID Braille display driver for Android — routes screen content from the BAOSP screen reader to a connected Braille display. Part of the [BAOSP](https://github.com/tech-master33/baosp) ecosystem.

## Download

**Latest APK → [github.com/tech-master33/baosp/releases/tag/nightly](https://github.com/tech-master33/baosp/releases/tag/nightly)**

A fresh build is posted there automatically every night alongside the screen reader, TTS engine, launcher, clock, calculator, and panel.
You can also find standalone builds on the [releases page](https://github.com/tech-master33/baosp-braille/releases) of this repo.

## Features

- **Bluetooth HID support** — connects to any Bluetooth HID-compatible Braille display
- **BrailleBack-compatible** — uses the same display protocol as the Android BrailleBack service
- **Screen content routing** — displays text from baosp-screenreader on the Braille device
- **Navigation commands** — Braille display buttons map to swipe and focus gestures
- **Auto-detect display** — identifies connected device and configures the correct key table
- **Grade 1 and Grade 2 Braille** — selectable translation modes

## Connecting your Braille display

1. Download the APK from the nightly link above
2. Transfer it to your Android device and install it (allow unknown sources if prompted)
3. Pair your Braille display: Settings → Bluetooth → pair your device
4. Open **BAOSP Braille** from your app drawer
5. Enable it in **Settings → Accessibility → Downloaded services → BAOSP Braille → On**
6. The display should start showing screen content immediately

## Building locally

```bash
git clone https://github.com/tech-master33/baosp-braille.git
cd baosp-braille
chmod +x gradlew
./gradlew assembleDebug
# APK at: app/build/outputs/apk/debug/app-debug.apk
```

Requires JDK 17 and Android SDK with API level 34.

## Permissions

| Permission | Why it is needed |
|-----------|-----------------|
| `BLUETOOTH` | Connect to the Braille display |
| `BLUETOOTH_CONNECT` | Android 12+ Bluetooth permission |
| `BLUETOOTH_SCAN` | Discover Braille displays nearby |
| `BIND_ACCESSIBILITY_SERVICE` | Read screen content to send to the display |

## CI/CD

Every push to `main` automatically builds a new APK and posts it as a GitHub Release.
The badge above shows whether the latest build passed or failed.

## BAOSP Ecosystem

baosp-braille is part of BAOSP — an accessible Android platform for blind and visually impaired users:

| Repo | What it does |
|------|-------------|
| [baosp](https://github.com/tech-master33/baosp) | Main project — nightly bundle, coordination |
| [baosp-screenreader](https://github.com/tech-master33/baosp-screenreader) | Screen reader — accessibility service |
| [baosp-tts](https://github.com/tech-master33/baosp-tts) | SVOX Pico TTS engine |
| [aoler](https://github.com/tech-master33/aoler) | Accessible home screen launcher |
| [baosp-clock](https://github.com/tech-master33/baosp-clock) | Accessible clock, alarm, timer |
| [baosp-calc](https://github.com/tech-master33/baosp-calc) | Accessible calculator |
| [baosp-panel](https://github.com/tech-master33/baosp-panel) | Quick-access control panel |
| **[baosp-braille](https://github.com/tech-master33/baosp-braille)** | **Bluetooth Braille display driver (this repo)** |

All APKs are bundled together and published every night at  
**[github.com/tech-master33/baosp/releases/tag/nightly](https://github.com/tech-master33/baosp/releases/tag/nightly)**

## License

Apache License 2.0 — same as BAOSP and AOSP.
