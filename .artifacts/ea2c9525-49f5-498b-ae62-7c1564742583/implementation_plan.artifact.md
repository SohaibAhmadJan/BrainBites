# Implementation Plan - Release v4.4.6

Standardize versioning across the User App (Android) and the Web Admin Panel to `4.4.6`, and push the latest changes to GitHub.

## User Review Required

> [!IMPORTANT]
> This plan will perform a `git push` to `origin master` for BrainBites and `origin main` for webBasedAdminPanel. Ensure your remote credentials are configured.

## Proposed Changes

### [BrainBites] User App & Backend Configuration

#### [MODIFY] [build.gradle.kts](file:///F:/BrainBites/app/build.gradle.kts)
- Increment `versionCode` from `48` to `49`.
- Update `versionName` to `"4.4.6"`.

#### [MODIFY] [SplashScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/splash/SplashScreen.kt)
- Update version string text to `"Version 4.4.6"`.

#### [MODIFY] [SettingsScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/settings/SettingsScreen.kt)
- Update version string text to `"Version 4.4.6"`.

#### [MODIFY] [AppSettings.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/data/AppSettings.kt)
- Update `latestVersion` default to `"4.4.6"`.

#### [MODIFY] [MigrationManager.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/data/MigrationManager.kt)
- Update `latestVersion` string to `"4.4.6"`.

#### [MODIFY] [SettingsRepository.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/data/SettingsRepository.kt)
- Update fallback `latestVersion` to `"4.4.6"`.

#### [MODIFY] [fix_config.js](file:///F:/BrainBites/fix_config.js)
- Update `latestVersion` to `'4.4.6'`.

---

### [webBasedAdminPanel] Admin Dashboard

#### [MODIFY] [package.json](file:///F:/webBasedAdminPanel/package.json)
- Update `"version"` to `"4.4.6"`.

---

## Verification Plan

### Automated Tests
- Run `git status` in both directories to ensure clean working trees after commit.
- Verify `git log` shows the new version commits.

### Manual Verification
- Check the `SplashScreen` and `SettingsScreen` in the Android app to ensure the version string reflects `4.4.6`.
- Check `package.json` in the admin panel.
