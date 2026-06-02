# baosp-braille Roadmap

What we plan to build next, why it matters, and what state each item is in.

Open an issue or discussion before starting work on anything here so we can coordinate.

---

## Status key

- **Planned** — not started
- **In progress** — actively being worked on
- **Needs help** — no one assigned, good place to contribute
- **Done** — shipped in the nightly build

---

## Display support

### Orbit Reader 20 — Needs help

**What it is:** Key table for the Orbit Reader 20 — a low-cost 20-cell Braille display popular in the blind community.

**Why it matters:** The Orbit Reader 20 is one of the most affordable Braille displays available. Supporting it would make BAOSP Braille accessible to users who cannot afford higher-end devices.

**Where to start:** Create `res/xml/keytable_orbit_reader_20.xml`. Map the 5-key nav pad and 20 routing keys. The display connects via Bluetooth HID.

---

### Refreshabraille 18 — Needs help

**What it is:** Key table for the APH Refreshabraille 18, an 18-cell display with a perkins-style keyboard.

**Why it matters:** Widely used in the US, especially by students and professionals.

**Where to start:** The display presents as a standard HID keyboard over Bluetooth. Map the nav keys, routing keys, and the 8-dot perkins entry keys.

---

### HumanWare BrailleNote Touch Plus — Planned

**What it is:** Key table and connection profile for the BrailleNote Touch Plus.

---

### Freedom Scientific Focus 40 Blue — Planned

**What it is:** Key table for the Focus 40 Blue, a widely used professional display.

---

## Translation

### Grade 2 Braille (contracted) — Planned

**What it is:** Support for Grade 2 contracted Braille — a shortened form where common words and letter combinations are replaced with single Braille cells.

**Why it matters:** Most adult Braille readers use Grade 2. Without it, the display shows everything in Grade 1 (uncontracted), which is slower to read and not what users are accustomed to.

**Proposed approach:** Integrate the LibLouis library (or a Kotlin port) for Braille translation. LibLouis is the standard used by most screen readers.

---

### Language-specific Braille tables — Planned

**What it is:** Translation tables for Spanish, French, German, Portuguese, and other languages.

**Why it matters:** BAOSP is used internationally. Braille is language-specific — French Braille and English Braille use different cell assignments.

---

## Navigation

### Braille cursor routing — In progress

**What it is:** When the user presses a routing key above a word on the display, move focus to that word on screen.

**Why it matters:** This is the most important navigation feature of a Braille display. Without it, users can only pan left/right but cannot tap individual words.

---

### Pan-to-bottom / pan-to-top — Needs help

**What it is:** Two key commands that jump to the bottom or top of the current screen.

**Why it matters:** Panning one line at a time through a long screen takes many keypresses. Jump-to-top/bottom makes navigation faster.

**Where to start:** Map hardware key combinations in the key table and call `AccessibilityNodeInfoCompat.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS)` with the first/last node.

---

## Integration

### Add to BAOSP nightly bundle — Planned

**What it is:** Include baosp-braille APK in the nightly release at github.com/tech-master33/baosp/releases/tag/nightly.

**Status:** Will be added once the first working build lands.

---

## How priorities are set

Items move up the list when more users report being blocked or a contributor volunteers to lead the work.
Every item here has a stated impact on blind or disabled users.
