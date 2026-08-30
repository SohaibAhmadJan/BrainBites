# Implementation Plan - Fix Authentication Flows & Google Integration

This plan addresses the non-functional authentication options (Sign In, Sign Up, Google, Guest) and implements the necessary backend and UI logic to make them operational.

## Proposed Changes

### 1. Build Configuration & Dependencies

#### [MODIFY] [libs.versions.toml](file:///F:/BrainBites/gradle/libs.versions.toml)
- Add versions for `credentials` and `googleid`.
- Add library definitions for `androidx-credentials`, `androidx-credentials-play-services-auth`, and `googleid`.

#### [MODIFY] [build.gradle.kts](file:///F:/BrainBites/app/build.gradle.kts)
- Add the new credential and auth libraries to the dependencies block.

### 2. Data Layer: Robust Auth Logic

#### [MODIFY] [AuthRepository.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/data/AuthRepository.kt)
- Implement `signInWithGoogle(idToken: String)` to verify Google credentials with Firebase.
- Enhance `signInAnonymously()` to return a `Result<Unit>` for better UI feedback.
- Improve error handling in `signIn` and `signUp` to catch and return specific Firebase Auth exceptions.

### 3. UI Components: Interactive Links

#### [MODIFY] [AuthComponents.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/auth/AuthComponents.kt)
- Add a reusable component or helper for clickable annotated strings (for Terms & Privacy).
- Ensure all interactive elements have sufficient touch targets.

### 4. Screen Implementation: Functional Auth

#### [MODIFY] [LoginScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/auth/LoginScreen.kt)
- Implement the Google Sign-In flow using the new `Credential Manager` API.
- Add `isLoading` states for the Guest and Google buttons.
- Ensure `onLoginSuccess()` is called only after a successful auth session is established.

#### [MODIFY] [SignUpScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/auth/SignUpScreen.kt)
- Implement Google Sign-In logic.
- Make "Terms" and "Privacy Policy" links interactive.
- Fix UI issues that might prevent button clicks.

## Verification Plan

### Automated Tests
- Gradle Build to verify dependencies and code syntax.

### Manual Verification
1.  **Guest Mode**: Click "Continue as Guest" -> Verify navigation to Home without credentials.
2.  **Email Auth**:
    - Sign Up with a new email -> Verify successful creation.
    - Sign In with existing email -> Verify entry.
    - Enter wrong password -> Verify error message appears.
3.  **Google Auth**: Click "Continue with Google" -> Verify the account selection dialog appears and successfully logs in.
4.  **Legal Links**: Click "Terms" -> Verify a Toast or dialog appears.

> [!WARNING]
> **Firebase Console Requirement**: For these changes to work, you MUST enable "Email/Password", "Google", and "Anonymous" sign-in methods in your Firebase Console under **Authentication > Sign-in method**.
