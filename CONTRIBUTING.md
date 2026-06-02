# Contributing to baosp-braille

Thank you for contributing to baosp-braille, the Bluetooth Braille display driver for BAOSP.
This guide is written to work well with screen readers and keyboard-only navigation.
Every step is numbered and linear — no visual layout is assumed.

---

## Ways to contribute

1. Report a bug — something does not work with your Braille display
2. Add support for a new Braille display model
3. Improve key table mappings for an existing display
4. Improve Grade 2 Braille translation
5. Improve documentation — especially display-specific setup guides
6. Write code — fix bugs or add features

---

## Claiming an issue

1. Open the issue you want to work on
2. Leave a comment: "I'd like to take this on"
3. Wait for a maintainer to assign it before starting

---

## Before you start

1. A GitHub account — github.com/join
2. Git — git-scm.com
3. Android Studio or VS Code
4. Java 17 — adoptium.net
5. Android SDK (API level 34)
6. A Bluetooth HID Braille display (or the Android Bluetooth HID emulator for testing)

---

## Step 1 — Fork

1. Open github.com/tech-master33/baosp-braille
2. Activate Fork → Create fork
3. Your copy is at github.com/YOUR-USERNAME/baosp-braille

---

## Step 2 — Clone

```bash
git clone https://github.com/YOUR-USERNAME/baosp-braille.git
cd baosp-braille
git remote add upstream https://github.com/tech-master33/baosp-braille.git
```

---

## Step 3 — Branch

```bash
git checkout -b your-branch-name
```

Examples: `fix/focus-lost-on-scroll`, `feature/refreshabraille-keytable`, `docs/orbit-reader-setup`

---

## Step 4 — Make changes

Key files:

- `app/src/main/java/org/baosp/braille/` — all Kotlin source
- `app/src/main/res/xml/` — accessibility service config and key tables
- `app/src/main/AndroidManifest.xml` — permissions and service declaration

### When adding a new display

1. Create a key table XML file in `res/xml/` named after the display model
2. Map all navigation keys (pan left/right, routing, forward/back)
3. Add the display's Bluetooth name pattern to the auto-detect list
4. Test every navigation key on a real device if possible

### Accessibility rules

1. The accessibility service must handle all `AccessibilityEvent` types
2. Screen content must update on the display within 200ms of a focus change
3. All navigation key events must be consumed and not passed through
4. Grade 1 Braille must always be available as a fallback

---

## Step 5 — Build and test

```bash
chmod +x gradlew
./gradlew assembleDebug
adb install app/build/outputs/apk/debug/app-debug.apk
```

Checklist:
- App builds without errors
- Service shows up in Accessibility settings
- Display connects when service is enabled
- Screen content appears on the display
- Pan left/right moves focus on screen
- Routing keys activate focused element

---

## Step 6 — Commit

```bash
git add .
git commit -m "feature: add key table for Orbit Reader 20

Maps the five-key nav pad and all 20 routing keys.
Tested with Orbit Reader 20 firmware 1.9."
```

Types: `fix`, `feature`, `docs`, `refactor`, `a11y`, `keytable`, `test`

---

## Step 7 — Push and pull request

```bash
git push origin your-branch-name
```

1. Open github.com/YOUR-USERNAME/baosp-braille
2. Activate Compare and pull request
3. Title: one sentence — what changed
4. Description: display model tested on, firmware version, what was verified
5. Activate Create pull request

---

## Reporting a bug

1. Open github.com/tech-master33/baosp-braille/issues
2. Activate New issue → Bug report
3. Include: display model and firmware, Android version, what happened

---

## Community

- Issues: github.com/tech-master33/baosp-braille/issues
- Screen reader: github.com/tech-master33/baosp-screenreader
- BAOSP main: github.com/tech-master33/baosp
