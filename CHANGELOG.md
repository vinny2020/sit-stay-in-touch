# Changelog

All notable changes to SIT: Stay in Touch are documented here.

## [Unreleased] — 2026-03-17

### iOS
- **Message Templates CRUD** — `TemplateListView` and `TemplateEditView` added; templates accessible from Settings → Message Templates; default "Checking in" template seeded on first launch
- **Settings expansion** — live contact count, tickle notification toggle (with system-denied deep link to iOS Settings), default tickle frequency picker, app version read from bundle at runtime
- **App icon** — full `AppIcon.appiconset` generated from Pulse SVG logo across all required sizes (20 → 1024px); Navy `#0A1628` background, App Store compliant (no alpha channel)
- **Search on ComposeView** — filter contacts by name, company, and job title while composing
- **LinkedIn import UX** — improved step-by-step guidance in ImportView

### Android
- **Full native app** — Kotlin + Jetpack Compose + Room + Hilt, feature parity with iOS: contact import (phone + LinkedIn CSV), groups, tickle reminders (WorkManager), SMS compose with direct send via SmsManager, onboarding, settings
- **Build warning fixes** — deprecated `Icons.Default.ArrowBack/Send/Message` replaced with `Icons.AutoMirrored` equivalents; `@OptIn(FlowPreview::class)` added; `@Index("groupId")` added to `ContactGroupCrossRef`; Room schema bumped to v2

## [0.1.0] — 2026-03-15

### Added
- Monorepo init with iOS and Android platform directories
- **iOS** — Swift 6, SwiftUI, SwiftData; full feature set: contacts, groups, tickle reminders, SMS compose, onboarding, settings
- **Android** — initial scaffold committed and flattened into monorepo
