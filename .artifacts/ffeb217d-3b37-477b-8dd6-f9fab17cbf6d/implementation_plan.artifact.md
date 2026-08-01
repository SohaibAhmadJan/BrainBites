# Enhance User Profile and Edit Functionality

This plan details the steps to add more user information (Profile Image, User ID, and Bio) to the profile section and expand the edit profile dialog to allow managing these new fields.

## User Review Required

> [!IMPORTANT]
> - A unique **User ID** will be generated automatically for new users if not set.
> - The **Profile Image** will initially support selecting from a set of built-in avatars or providing a custom image URI.

## Proposed Changes

### Data Layer

#### [MODIFY] [PreferenceManager.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/data/PreferenceManager.kt)
- Add constants for `KEY_USER_IMAGE`, `KEY_USER_BIO`, and `KEY_USER_ID`.
- Add `MutableStateFlow` and `asStateFlow` for `userImage`, `userBio`, and `userId`.
- Update `initialize` to load these values.
- Add setter functions: `setUserImage`, `setUserBio`, and `setUserId`.

### View Model

#### [MODIFY] [ProfileViewModel.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/profile/ProfileViewModel.kt)
- Expose `userImage`, `userBio`, and `userId` from `PreferenceManager`.
- Add `updateProfile(name, bio, userId, image)` function to update all fields at once or individual setters.

### UI Layer

#### [MODIFY] [ProfileScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/profile/ProfileScreen.kt)
- **ProfileHeader**: Update to show the user's profile image and display the `@userId` below the name.
- **ProfileScreenContent**: Pass the new data (Bio, User ID, Image) to `ProfileHeader`.
- **Bio Section**: Add a small bio section below the stats or header.
- **EditProfileDialog**: Add text fields for Bio and User ID. Add an avatar selection row.

#### [NEW] [AvatarPicker.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/components/AvatarPicker.kt)
- A simple component to choose from a few pre-defined profile images.

## Verification Plan

### Manual Verification
- Navigate to the Profile screen.
- Verify that a default avatar and a generated User ID are shown.
- Click "Edit Profile".
- Change the name, bio, and user ID.
- Select a different avatar.
- Save and verify the changes are reflected on the Profile screen and persisted after app restart.
