# GitHub Deployment & Versioning Plan (v4.0.0)

Deploy the latest changes for both the **BrainBites Android App** and the **Web Admin Panel** to GitHub, setting their versions to `4.0.0` while ensuring private keys and sensitive configuration files remain excluded from the public repositories.

## User Review Required

> [!CAUTION]
> I will be strengthening the `.gitignore` for the Android project to ensure files like `google-services.json`, keystores, and crash logs are NOT pushed to your public profile.

> [!IMPORTANT]
> For the Android app, I will increment the `versionCode` to `40`. This is necessary for proper app distribution.

## Proposed Changes

### 1. Security & Versioning (Android)

#### [MODIFY] [.gitignore](file:///F:/BrainBites/.gitignore)
- Add `google-services.json` to the ignore list.
- Add `*.jks` and `*.keystore` (Private signing keys).
- Add crash logs (`hs_err_pid*.log`, `replay_pid*.log`).

#### [MODIFY] [build.gradle.kts](file:///F:/BrainBites/app/build.gradle.kts)
- Update `versionName` to `"4.0.0"`.
- Update `versionCode` to `40`.

---

### 2. Versioning (Web Admin Panel)

#### [MODIFY] [package.json](file:///F:/webBasedAdminPanel/package.json)
- Update `version` to `"4.0.0"`.
- (The existing `.gitignore` already properly excludes `.env` and `serviceAccountKey.json`).

---

### 3. Git Operations

#### **BrainBites Android** (Branch: `master`)
- `git add .`
- `git commit -m "Release v4.0.0: Unified Analytics Hub and Installation Logic Fix"`
- `git push origin master`

#### **Web Admin Panel** (Branch: `main`)
- `git add .`
- `git commit -m "Release v4.0.0: Consolidated Analytics Hub and Stability Fixes"`
- `git push origin main`

## Verification Plan

### Security Check
- Verify that `git status` shows sensitive files as "Untracked" or ignored before the commit.

### Build Check
- Run `gradlew app:assembleDebug` to ensure the project remains buildable after version changes.
