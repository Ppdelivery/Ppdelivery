# The 57th

A deadpan calendar toy for Android. Pick an anchor month and year; the app tells you
what day of that month it is today — except the month never ends.

Anchor **August 2026** and on 26 Sep 2026 it's **August the 57th**.
Anchor **February 1776** and it's **February the 91,549th**.

## Rules

- `count = (whole days from the 1st of the anchor month to today) + 1`
- Plain (proleptic) Gregorian subtraction via `java.time`. No calendar reform, no Julian
  conversion — "February 1776" is just the modern month label.
- Future anchors go negative and are shown as-is.
- The year is always dropped.

## Wording (in-app settings)

**Match my phone** (default): the phone's language, word order, digits, thousands
separator and minus sign, using the platform's own month-day pattern.
English phones get the ordinal — *August the 57th* (US) or *the 57th of August* (UK);
other languages use their native form — *57. August*, *57 août*, *57 августа*.

**Classic**: the original fixed style on any phone — English month + ordinal, 11th/12th/13th
rule per hundred, comma thousands, a real minus sign: *February the 91,549th*, *February the −40th*.

**Timely wording** (off by default, English only): words the date as the anchor's era would.

| Anchor year | Example |
| --- | --- |
| before 1500 | *the .lvij. day of August* (medieval numerals; 91,549 → `.xcj.M.dxlix.`) |
| 1500–1699 | *the 57th Day of August* |
| 1700–1799 | *Augt. 57th* / *Feby. 91,549th* (Georgian letter heading) |
| 1800–1899 | *the 57th inst.* ("of this month") |
| 1900–1959 | *AUGUST 57TH STOP* (telegram) |
| 1960–1999 | *AUG 57* (digital display) |
| 2000 on, or count ≤ 0 | modern wording |

## Layout

| Module | What |
| --- | --- |
| `core/` | Pure Kotlin/JVM: the count, ordinal and formatting logic, plus unit tests. |
| `app/`  | Android app (minSdk 26, no AndroidX): anchor picker and home-screen widget. |

### Where the count shows up

- **Home-screen widget** — the real clock beside the count.
- **Quiet notification** (off by default) — an ongoing, soundless notification. Android
  doesn't let apps draw on the lock screen, but notifications appear there if the phone's
  lock-screen notification settings allow it. Asks for notification permission on Android 13+.
- **Live wallpaper** — "Set as wallpaper…" in the app opens the system preview, where you
  pick home screen, lock screen or both. The count sits below the middle so it doesn't
  collide with the lock-screen clock; follows dark mode.

The widget shows the real clock (`TextClock`) beside the count and refreshes (with the notification) just after
local midnight via an inexact alarm, and on time/timezone/language changes and reboots. It only reads the
system date — it never changes it, and needs no special permissions.

## Build

```sh
./gradlew :core:test            # regression tests, incl. both worked examples
./gradlew :app:assembleDebug    # needs the Android SDK (ANDROID_HOME)
```

CI (`.github/workflows/build.yml`) runs both and uploads the debug APK as an artifact.
