# Synchronize Profile Avatar in Top Bar

This plan addresses the issue where the selected profile avatar is not reflected in the top bar icon on the Home screen (or other screens).

## User Review Required

> [!IMPORTANT]
> - I will replace the static "Account" icon in the `MainScaffold` top bar with a dynamic avatar that reflects the user's selected image or initials.
> - A new shared component `AvatarView` will be created to ensure consistency between the `ProfileScreen` and the `MainScaffold`.

## Proposed Changes

### [UI Components]

#### [NEW] [AvatarView.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/components/AvatarView.kt)
- Create a reusable `AvatarView` component that handles:
    - Loading gallery images (`content://`, `file://`).
    - Loading DiceBear avatars (seeds).
    - Displaying initials as a fallback.
- This component will be used in both `ProfileScreen` and `MainScaffold`.

### [Main Scaffold]

#### [MODIFY] [MainScaffold.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/main/MainScaffold.kt)
- Collect `userImage` and `userName` from `PreferenceManager`.
- Replace the static `IconButton` icon with `AvatarView`.
- Ensure it's sized correctly for the `TopAppBar`.

### [Profile Screen]

#### [MODIFY] [ProfileScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/profile/ProfileScreen.kt)
- Use the new `AvatarView` component in the `ProfileHeader` to reduce code duplication and ensure identical behavior.

## Verification Plan

### Manual Verification
1. Open the app to the Home screen.
2. Observe the profile icon in the top bar (should show initials or default).
3. Navigate to the Profile screen and edit the profile.
4. Select a new avatar or upload a gallery image.
5. Save changes and navigate back to the Home screen.
6. Verify that the top bar icon now matches the selected avatar.
