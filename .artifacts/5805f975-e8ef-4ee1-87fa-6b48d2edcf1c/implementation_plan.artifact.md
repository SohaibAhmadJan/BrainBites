# Implementation Plan - Functional Settings Actions

The user wants to make the remaining settings actions functional: Notification Time, Rate App, Share App, and About BrainBites. I will implement these using standard Android intents and Compose dialogs.

## User Review Required

> [!NOTE]
> - **Notification Time**: Will use the system `TimePickerDialog` to allow users to select a daily reminder time.
> - **Rate App**: Will attempt to open the Play Store. Since this is a development app, it will fallback to a Toast if the store isn't available.
> - **Share App**: Will open the system share sheet with a link to the app (placeholder).
> - **About**: Will show a Material 3 `AlertDialog` with app version and mission information.

## Proposed Changes

### Utils & Helpers

#### [MODIFY] [ShareUtils.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/util/ShareUtils.kt)
- Add `shareApp(context: Context)` to open the system share sheet.
- Add `rateApp(context: Context)` to open the Play Store.

### UI Components

#### [MODIFY] [SettingsScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/settings/SettingsScreen.kt)
- Implement `showTimePicker` state and logic using `TimePickerDialog`.
- Implement `showAboutDialog` state and logic using `AlertDialog`.
- Connect "Share App" and "Rate App" to `ShareUtils`.
- Update "Notification Time" subtitle dynamically based on user selection.

## Verification Plan

### Automated Tests
- Run `gradle_build app:assembleDebug` to ensure all new logic and dependencies are correct.

### Manual Verification
- Verify that clicking each button triggers the expected action:
    - **Notification Time**: Opens a time picker; selecting a time updates the UI.
    - **Rate App**: Opens the Play Store or shows a Toast.
    - **Share App**: Opens the system share sheet.
    - **About**: Shows a clear, informative dialog.
