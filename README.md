# The 57th

A deadpan calendar toy for Android. Pick an anchor month and year; the app tells you
what day of that month it is today — except the month never ends.

Anchor **August 2026** and on 26 Sep 2026 it's **August the 57th**.
Anchor **February 1776** and it's **February the 91,549th**.

## Rules

- `count = (whole days from the 1st of the anchor month to today) + 1`
- Plain (proleptic) Gregorian subtraction via `java.time`. No calendar reform, no Julian
  conversion — "February 1776" is just the modern month label.
- Future anchors go negative and are shown as-is: *February the −40th*.
- Display is month name + ordinal; the year is dropped on purpose.
- Suffixes follow English rules with the 11/12/13 exception per hundred
  (91,311th, 91,321st). Thousands are always separated with a comma.

## Layout

| Module | What |
| --- | --- |
| `core/` | Pure Kotlin/JVM: the count, ordinal and formatting logic, plus unit tests. |
| `app/`  | Android app (minSdk 26, no AndroidX): anchor picker and home-screen widget. |

The widget shows the real clock (`TextClock`) beside the count and refreshes just after
local midnight via an inexact alarm, and on time/timezone changes. It only reads the
system date — it never changes it, and needs no special permissions.

## Build

```sh
./gradlew :core:test            # regression tests, incl. both worked examples
./gradlew :app:assembleDebug    # needs the Android SDK (ANDROID_HOME)
```

CI (`.github/workflows/build.yml`) runs both and uploads the debug APK as an artifact.
