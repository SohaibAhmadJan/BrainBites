# Fix Profile Avatar Display Issues

The user reported that gallery images do not appear correctly in the profile or top bar. This is likely due to URI permission issues or how the image data is being passed to the loading library.

## User Review Required

> [!IMPORTANT]
> - I will improve the `AvatarView` component to handle both string paths and proper `Uri` objects more robustly.
> - I will add **persistable URI permissions** as a fallback, although the primary solution remains copying the image to internal storage.
> - I will refine the `ProfileViewModel` logic to ensure the file copy happens successfully before updating the user profile state.

## Proposed Changes

### [Avatar View]

#### [MODIFY] [AvatarView.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/components/AvatarView.kt)
- Update the `imageData` logic to prioritize local file paths.
- Ensure `Coil` receives a clean `Uri` object for local files to improve reliability.
- Add better handling for `content://` URIs that might be passed before the file is fully copied.

### [Profile Screen]

#### [MODIFY] [ProfileScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/profile/ProfileScreen.kt)
- Update the photo picker result handling.
- Ensure the selected image URI is passed correctly to the `EditProfileDialog`.

### [Profile Logic]

#### [MODIFY] [ProfileViewModel.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/profile/ProfileViewModel.kt)
- Refine `saveImageToInternalStorage` to handle potential exceptions more gracefully.
- Use `context.filesDir.absolutePath` to build a more standard file path string.
- Ensure the coroutine correctly handles the state update sequence.

## Verification Plan

### Manual Verification
1. Open the Profile edit dialog.
2. Select a photo from the gallery.
3. Verify the photo appears in the **picker preview** immediately.
4. Click "Save Changes".
5. Verify the photo appears in the **Profile header**.
6. Navigate back to Home and verify the photo appears in the **top bar icon**.
7. Restart the app and verify the custom photo is still there.
