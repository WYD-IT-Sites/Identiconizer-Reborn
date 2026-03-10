# Identiconizer-Reborn

Identiconizer generates unique identicon avatars for contacts.

This project started as a port of ChameleonOS' contact identicons feature, and has now been modernized for current Android versions.


## What changed in v1.7

### ✅ Rebrand
- App renamed to **Identiconizer-Reborn**
- Store title/metadata updated to match rebrand

### ✅ Simplified standard size options
- Replaced granular size picker with standard presets only:
  - 128, 256, 512, 720, 1024, 1440
- Removed oversized/low-value options to keep UX clean and practical

## What changed in v1.6

### ✅ Build + platform modernization
- Migrated from legacy Android Support libraries to **AndroidX**
- Updated Gradle/Android build tooling to a modern stack
- Updated app to target modern Android SDK levels

### ✅ Crash fixes on modern Android (why it was crashing)
The app was crashing during identicon creation on Android 12+/14+ due to newer foreground service and PendingIntent requirements.

Fixed by:
- Adding required PendingIntent immutability flags (`FLAG_IMMUTABLE`)
- Adding required foreground service permissions:
  - `android.permission.FOREGROUND_SERVICE`
  - `android.permission.FOREGROUND_SERVICE_DATA_SYNC`
- Declaring `foregroundServiceType="dataSync"` on relevant services

These are required by newer Android security/runtime rules and are the main reason old builds crash immediately when creation starts.

### ✅ UX/update changes
- About screen now includes **Updated by: Liftedplane**
- "Updated by" action now routes users to GitHub Issues for bug reports:
  - https://github.com/WYD-IT-Sites/Identiconizer-Reborn/issues

### ✅ Image size support
- Increased identicon size options to support larger outputs (including **1080×1080**)

## Releases
- Latest release: **v1.7**
- GitHub Releases: https://github.com/WYD-IT-Sites/Identiconizer-Reborn/releases

## Legacy reference
- Original XDA thread: http://forum.xda-developers.com/showthread.php?t=2718943
