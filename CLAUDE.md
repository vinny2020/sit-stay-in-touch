# CLAUDE.md — SIT: Stay in Touch (Monorepo)

## What This Repo Is

SIT is a privacy-first personal network manager with two native platform implementations.
All data is stored on-device. No cloud, no analytics, no account required.

## Repo Structure

```
sit/
├── ios/        — Swift 6, SwiftUI, SwiftData (see ios/CLAUDE.md for full iOS spec)
├── android/    — Kotlin, Jetpack Compose, Room (see android/CLAUDE.md for full Android spec)
└── assets/brand/ — Shared Pulse logo SVGs and color tokens
```

## Always read the platform CLAUDE.md before working

- iOS work → read `ios/CLAUDE.md` first
- Android work → read `android/CLAUDE.md` first

## Shared Brand Tokens

| Token | Value | Usage |
|---|---|---|
| Navy | `#0A1628` | App background, icon bg |
| Cobalt | `#2563EB` | Speech bubble, primary action |
| Amber | `#F5C842` | EKG wave, accent, tickle due state |
| Wordmark | Syne 800 | "SIT" logotype |
| Tagline font | Syne 400 | "STAY IN TOUCH" |

## Feature Parity Status

| Feature | iOS | Android |
|---|---|---|
| Contact import (phone + LinkedIn CSV) | ✅ | ✅ |
| Groups / circles | ✅ | ✅ |
| Tickle calendar (recurring reminders) | ✅ | ✅ |
| Compose SMS/MMS with templates | ✅ | ✅ |
| Settings | ✅ | ✅ |
| Unit tests | — | ✅ (3 suites) |
| App icon (Pulse identity) | ✅ | ✅ Adaptive icon |
| Store-ready | ⚠️ TestFlight pending | ⚠️ Signing pending |

## Platform-Specific Notes

**iOS** — `MFMessageComposeViewController` always requires user tap to send. No silent send possible.
**Android** — `SmsManager` can send silently with SEND_SMS permission. Intent fallback also available.

> Android's silent SMS send is a meaningful UX advantage. Surface as a user preference in Settings.
