# CLAUDE.md — Ticklr Android

## ⚠️ Bugs to Fix (Next Session)

### Bug 1 — Tickle navigation from ContactDetail goes to Network list instead of TickleEdit
**Symptom:** Tapping "Add Tickle" on a contact's detail screen navigates to the full Network list instead of opening the TickleEdit screen pre-populated with that contact.
**Root cause:** In `NavGraph.kt` the `onAddTickle` lambda calls `Screen.TickleEdit.createRoute(-1L)` which creates a new blank tickle with no contact pre-selected. The `TickleEdit` route needs to accept an optional `contactId` parameter so the contact is pre-filled.
**Files to change:**
- `ui/nav/Screen.kt` — add optional `contactId` param to `TickleEdit` route: `"tickle_edit/{tickleId}?contactId={contactId}"`
- `ui/nav/NavGraph.kt` — update `onAddTickle` in `ContactDetailScreen` to pass the contactId: `navController.navigate(Screen.TickleEdit.createRouteWithContact(contactId))`
- `ui/tickle/TickleEditScreen.kt` — read the optional `contactId` from nav args and pre-populate the contact picker

### Bug 2 — "STAY IN TOUCH" still showing in OnboardingScreen and notification body
**Files to change:**
- `ui/onboarding/OnboardingScreen.kt` — change tagline text from `"STAY IN TOUCH"` to `"YOUR PEOPLE MATTER"`
- `service/TickleWorker.kt` — change notification body text from `"Stay in touch"` to `"Your people matter"` (or similar)
- `SITApp.kt` — change notification channel description from `"stay in touch"` to `"Ticklr reminders"`

### Bug 3 — Android app icon doesn't match iOS
**Current state:** The adaptive icon uses the Pulse speech bubble + EKG wave SVG in `drawable/ic_launcher_foreground.xml` on Navy background. This is the correct identity but the proportions/sizing may differ from the iOS icon.
**What to do:** Compare `ic_launcher_foreground.xml` against the iOS `icon_1024.png` in `ios/Sources/SIT/Resources/Assets.xcassets/AppIcon.appiconset/`. Adjust the SVG `pathData` coordinates so the bubble and EKG wave fill the same visual weight as the iOS version. The viewportWidth/Height is 108dp — the safe zone for adaptive icons is the inner 72dp circle.



## Architecture — MVVM + Repository
- **UI**: Jetpack Compose (no XML layouts)
- **State**: ViewModel + StateFlow
- **Persistence**: Room (version 2) + TypeConverters for List<String>
- **DI**: Hilt
- **Navigation**: Navigation Compose (NavGraph.kt)
- **Background work**: WorkManager (TickleWorker)
- **Min SDK**: 26 (Android 8.0) · **Target SDK**: 35

## Project Structure

```
app/src/main/java/com/xaymaca/sit/
├── SITApp.kt                       # @HiltAndroidApp, PREFS_NAME, KEY_ONBOARDING_COMPLETE
├── MainActivity.kt                 # Entry point, NavGraph host
├── data/
│   ├── model/
│   │   ├── Contact.kt              # Room @Entity
│   │   ├── ContactGroup.kt         # Room @Entity
│   │   ├── ContactGroupCrossRef.kt # Many-to-many join table
│   │   ├── MessageTemplate.kt      # Room @Entity
│   │   ├── TickleReminder.kt       # Room @Entity
│   │   ├── Enums.kt                # ImportSource, TickleFrequency, TickleStatus
│   │   └── Relations.kt            # ContactWithGroups, GroupWithContacts
│   ├── dao/
│   │   ├── ContactDao.kt
│   │   ├── ContactGroupDao.kt
│   │   ├── MessageTemplateDao.kt
│   │   └── TickleReminderDao.kt
│   ├── db/
│   │   └── SITDatabase.kt          # Room DB, version 2, StringListConverter
│   └── repository/
│       ├── ContactRepository.kt
│       ├── MessageTemplateRepository.kt
│       └── TickleRepository.kt
├── di/
│   └── DatabaseModule.kt           # Hilt module — provides DB and DAOs
├── service/
│   ├── ContactImportService.kt     # ContactsContract import
│   ├── LinkedInCSVParser.kt        # CSV parsing (mirrors iOS implementation)
│   ├── SmsService.kt               # SmsManager direct send + Intent fallback
│   ├── StringListConverter.kt      # Room TypeConverter for List<String>
│   ├── TickleScheduler.kt          # nextDueDate logic + WorkManager scheduling
│   └── TickleWorker.kt             # WorkManager Worker for notifications
├── ui/
│   ├── theme/
│   │   ├── Color.kt                # Navy, Cobalt, Amber + variants
│   │   ├── Theme.kt                # SITTheme (dark-first, Material3)
│   │   └── Type.kt                 # Typography
│   ├── nav/
│   │   ├── NavGraph.kt             # Full NavHost + BottomNavigation
│   │   └── Screen.kt              # Sealed class for all routes
│   ├── launch/
│   │   └── LaunchScreen.kt
│   ├── onboarding/
│   │   ├── OnboardingScreen.kt
│   │   └── ImportScreen.kt
│   ├── network/
│   │   ├── NetworkListScreen.kt    # Searchable contact list
│   │   ├── NetworkViewModel.kt
│   │   ├── ContactDetailScreen.kt
│   │   └── AddContactScreen.kt     # Create + edit (reused with contactId param)
│   ├── groups/
│   │   ├── GroupListScreen.kt
│   │   ├── GroupDetailScreen.kt
│   │   └── GroupViewModel.kt
│   ├── tickle/
│   │   ├── TickleListScreen.kt     # Due/Upcoming/Snoozed sections
│   │   ├── TickleEditScreen.kt
│   │   └── TickleViewModel.kt
│   ├── compose/
│   │   ├── ComposeScreen.kt
│   │   └── ComposeViewModel.kt
│   ├── settings/
│   │   ├── SettingsScreen.kt
│   │   └── SettingsViewModel.kt
│   └── shared/
│       └── TagChipRow.kt
└── tests/
    ├── LinkedInCSVParserTest.kt    # Unit tests — passing
    ├── StringListConverterTest.kt  # Unit tests — passing
    └── TickleSchedulerTest.kt      # Unit tests — passing
```


## What's Complete ✅ — Full Android Feature Set

- Room database — all 5 entities, 4 DAOs, StringListConverter, version 2
- Hilt DI — DatabaseModule providing all repositories
- Compose theme — Navy/Cobalt/Amber Pulse identity, dark-first Material3
- Full NavGraph — 5-tab bottom nav + all nested routes with proper back stack
- `NetworkListScreen` — searchable contact list
- `ContactDetailScreen` — full detail view with tickle shortcut
- `AddContactScreen` — create + edit (reused via contactId param)
- `GroupListScreen` + `GroupDetailScreen`
- `TickleListScreen` — Due/Upcoming/Snoozed sections
- `TickleEditScreen` — create/edit with contact/group picker, frequency, date, note
- `TickleScheduler` — nextDueDate logic + WorkManager scheduling
- `TickleWorker` — WorkManager Worker for background notifications
- `ComposeScreen` + `ComposeViewModel` — multi-select + SMS send
- `SmsService` — SmsManager direct send + Intent fallback
- `ImportScreen` — LinkedIn CSV + contacts import
- `ContactImportService` — ContactsContract import
- `LinkedInCSVParser` — mirrors iOS implementation
- `SettingsScreen` + `SettingsViewModel` — includes debug "Load Test Contacts" button
- `SeedDataService` — DEBUG only, loads `test_contacts.csv` from assets
- `LaunchScreen` — Pulse identity splash
- `OnboardingScreen`
- Unit tests — LinkedInCSVParser, StringListConverter, TickleScheduler (all passing)
- Build artifacts present — app has been compiled and built successfully
- Screenshot prep — `./gradlew screenshotPrep` sets 9:41, full signal, 100% battery, no notifications
- Screenshot teardown — `./gradlew screenshotTeardown` restores normal status bar

## Play Store Submission Status

- ✅ Signed release AAB built successfully
- ✅ Keystore created at `~/Documents/ticklr-release.keystore` (alias: `ticklr`)
- ✅ Signing config wired into `build.gradle.kts`
- ✅ Google Play Developer account enrolled
- ⏳ Blocked: Google requires physical Android device to verify developer account
- ⏳ Once device verified: upload AAB to Play Console, complete store listing, submit

### When you find the Android device:
1. Complete Google Play developer account verification on the physical device
2. Go to play.google.com/console → Create app → Ticklr
3. Upload `android/app/release/app-release.aab`
4. Fill store listing from `docs/app-store-listing.md`
5. Short description (80 chars): "Privacy-first contact manager with tickle reminders"
6. Complete Data safety section — declare no data collected
7. Set price to Free → Submit for review (3–7 days)

### Keystore reminder
The keystore at `~/Documents/ticklr-release.keystore` is critical — back it up to 1Password or encrypted cloud storage. Losing it means you can never update the app on Play Store.


- **App signing** — create release keystore, configure in `build.gradle`
- **Play Store listing** — screenshots using Pixel 7 Pro AVD + `./gradlew screenshotPrep`
- **Privacy policy URL** — already live at `xaymaca.com/sit/privacy` ✅
- **App icon** — ✅ Already done — adaptive icon with Pulse identity

### Android — Nice to Have
- **Clear All Contacts debug button** — iOS has it, Android only has Load (parity gap)
- **SmsManager direct send UX** — surface the "send directly vs open Messages" preference in Settings
- **Search on ComposeScreen** — parity with iOS (filter contacts while selecting recipients)

## Key Notes

- `SITApp.PREFS_NAME` + `SITApp.KEY_ONBOARDING_COMPLETE` — SharedPreferences keys for onboarding state
- Room DB is version 2 — any schema changes need a migration
- `ContactGroupCrossRef` handles the many-to-many Contact ↔ Group relationship
- `StringListConverter` serializes `List<String>` for phone numbers, emails, tags
- Android can send SMS silently via `SmsManager` with SEND_SMS permission — iOS cannot
- WorkManager handles tickle notifications — persists across reboots
- `AddContactScreen` doubles as edit screen via optional `contactId` parameter

## Pulse Brand in Compose

```kotlin
val Navy   = Color(0xFF0A1628)   // backgrounds
val Cobalt = Color(0xFF2563EB)   // primary actions
val Amber  = Color(0xFFF5C842)   // accent, tickle due state
```

## Permissions (AndroidManifest)
- `READ_CONTACTS` — contacts import
- `SEND_SMS` — direct SMS (runtime request, graceful fallback to Intent)
- `POST_NOTIFICATIONS` — tickle reminders (Android 13+)

## Build & Run
Open `android/` in Android Studio (Hedgehog or newer).
Run on API 26+ emulator or physical Android device.
SMS direct-send requires physical device with active SIM.

## Sensitive Files — Never Commit
`keystore.jks`, `*.keystore`, `release.properties`, `google-services.json`,
any file containing signing passwords or API keys.
