# Identiconizer-Reborn

Identiconizer generates unique identicon avatars for contacts.

## Why this fork exists

Identiconizer is an app I’ve used personally for years, and I wanted to keep it alive on modern Android.

This fork focuses on practical modernization: bringing compatibility up to date, improving usability (including stable Light/Dark theme support), and expanding creative output with new identicon styles.

Development and implementation were accelerated with help from **OpenClaw**.

Most importantly: **full credit to GermainZ** for creating the original app and the foundation this project builds on.

## What changed in v1.9.0

### ✅ New identicon styles
- Added **Hex mosaic (tri-color)**
- Added **Voronoi mosaic**
- Reordered style list for better discovery

### ✅ Hex style subtype selector
- Added Hex subtype options with previews:
  - Tri-color symmetry
  - Filled hex grid
  - Variable hex radius

### ✅ Quality improvements
- **Dot Matrix** now scales properly at larger resolutions
- **Spirograph** improved with size-aware stroke/path tuning
- Better output quality across larger sizes (up to 1440)

### ✅ Theme support + stability
- Added app theme options:
  - Follow system
  - Light
  - Dark
- Implemented stable theme switching behavior

### ✅ Branding/UI updates
- New launcher icon (Hex Badge Cyber)
- Updated About developer attribution to **Liftedplane**

## Previous major updates

### v1.7
- Rebrand to **Identiconizer-Reborn**
- Standardized size presets:
  - 128, 256, 512, 720, 1024, 1440

### v1.6
- AndroidX migration + modernized build tooling
- Crash fixes for modern Android service/pending-intent requirements
- Updated About/Issue reporting links

## Releases
- Latest release: **v1.9.0**
- GitHub Releases: https://github.com/WYD-IT-Sites/Identiconizer-Reborn/releases

## Legacy reference
- Original XDA thread: http://forum.xda-developers.com/showthread.php?t=2718943
