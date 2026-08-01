# Reset Default Avatar and Finalize Persona List

Ensure the app starts with the user's initials by default and provide the full set of 40 professional persona illustrations.

## User Review Required

> [!IMPORTANT]
> - I am resetting the profile avatar to the **Initials (Aa)** view for this update to ensure you start with the clean "first letter" look you requested.
> - Once you choose a persona and click "Save," it will remain saved.

## Proposed Changes

### Data Layer

#### [MODIFY] [PreferenceManager.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/data/PreferenceManager.kt)
- Update the cleanup logic to be more comprehensive.
- I will reset the `userImage` to an empty string if it contains any of the 40 seeds or old seeds, ensuring that on your next launch, you see the **Initials** circle by default.

### UI Components

#### [MODIFY] [AvatarPicker.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/components/AvatarPicker.kt)
- Verify and finalize the list of **40 unique persona seeds** for DiceBear.

## Verification Plan

### Manual Verification
- **Default State**: Launch the app and verify the profile header shows your initial (e.g., "K") on a brand-green background.
- **Picker Count**: Open Edit Profile and scroll through the picker to confirm there are **40 different illustrations** plus the "Aa" option.
- **Persistence**: Select a character, save it, and verify it stays after app restart.
