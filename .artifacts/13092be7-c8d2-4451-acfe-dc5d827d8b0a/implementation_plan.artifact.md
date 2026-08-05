# Add "Choose from Gallery" Option for Profile Avatars

The user wants to be able to pick a profile picture from their device's gallery in addition to the predefined avatars.

## User Review Required

> [!IMPORTANT]
> - I will use the modern `ActivityResultContracts.PickVisualMedia` API to allow users to select an image from their gallery.
> - Selected gallery images will be stored as a URI string in the user's preferences.
> - To ensure the image is always accessible, I will copy the selected image to the app's internal storage and store the local file path.

## Proposed Changes

### [UI Components]

#### [MODIFY] [AvatarPicker.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/components/AvatarPicker.kt)
- Add a "Gallery" button at the beginning of the `LazyRow`.
- This button will trigger a callback to the parent screen to launch the image picker.
- Update `AvatarPicker` signature to include `onGalleryClick: () -> Unit`.

### [Profile Screen]

#### [MODIFY] [ProfileScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/profile/ProfileScreen.kt)
- Implement the `rememberLauncherForActivityResult` for `PickVisualMedia`.
- Update the `EditProfileDialog` to handle the gallery click and update the `userImage` state with the selected URI.
- Update `ProfileHeader` and `AvatarPicker` usage to handle both DiceBear seeds and local URIs.
- Add logic to check if `userImage` starts with "content://" or "file://" to determine how to load it.

### [Profile Logic]

#### [MODIFY] [ProfileViewModel.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/profile/ProfileViewModel.kt)
- Add a helper function to copy picked images to internal storage to ensure persistence.

## Verification Plan

### Manual Verification
1. Open the Profile screen.
2. Click "Edit Profile".
3. Verify a new "Gallery" or "Plus" icon is available in the avatar picker.
4. Click the icon, select an image from the gallery.
5. Verify the selected image is displayed in the preview and after saving.
6. Restart the app and verify the custom image persists.
