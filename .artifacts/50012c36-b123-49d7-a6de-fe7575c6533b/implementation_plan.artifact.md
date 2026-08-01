# Relocate Animated Tagline

Move the tagline from the top header to a more professional location (Settings and Profile screens) while maintaining the existing animations.

## User Review Required

> [!NOTE]
> Moving the tagline to the bottom of the Settings and Profile screens makes it less intrusive but still keeps the brand identity present.

## Proposed Changes

### UI Components

#### [MODIFY] [BrandHeader.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/components/BrandHeader.kt)
- Remove `AnimatedTagline` from the `BrandHeader` layout.
- Keep `AnimatedTagline` as a public composable so it can be used elsewhere.

#### [MODIFY] [SettingsScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/settings/SettingsScreen.kt)
- Import `AnimatedTagline` and `TaglineManager`.
- Add a footer item to the `LazyColumn` that displays the `AnimatedTagline`.

#### [MODIFY] [ProfileScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/profile/ProfileScreen.kt)
- Import `AnimatedTagline` and `TaglineManager`.
- Add a footer item to the `LazyColumn` that displays the `AnimatedTagline`.

### Cleanup

#### [MODIFY] [FactListScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/facts/FactListScreen.kt)
- Remove unused `BrandHeader` import.

#### [MODIFY] [HistoryScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/history/HistoryScreen.kt)
- Remove unused `BrandHeader` import.

## Verification Plan

### Manual Verification
- Deploy the app and verify that the tagline is gone from the top bar on all screens.
- Go to the **Settings** screen and scroll to the bottom to see the animated tagline.
- Go to the **Profile** screen and scroll to the bottom to see the animated tagline.
- Verify that the "jumping" animation and periodic tagline changes still work.
