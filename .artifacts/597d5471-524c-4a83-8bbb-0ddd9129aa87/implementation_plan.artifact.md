# Implementation Plan - Debugging Authentication Network Failures

This plan addresses the `auth/network-request-failed` error by improving diagnostic feedback and adding optional support for local Firebase Emulators to bypass public network issues.

## User Review Required

> [!WARNING]
> **Network Blockage**: The `auth/network-request-failed` error usually indicates that your browser or network (Firewall/VPN/Ad-blocker) is blocking requests to Google's authentication servers (`identitytoolkit.googleapis.com`).
> **Action Required**: Please try disabling any Ad-blockers or VPNs and check your internet connection.

## Proposed Changes

### 1. Enhanced Authentication Service (`firebaseService.ts`)
- **[MODIFY]**: Add logic to connect to Firebase Emulators if a new environment variable `VITE_USE_FIREBASE_EMULATORS` is set to `true`.
- **[MODIFY]**: This will allow you to run the Admin Panel completely offline or on restricted networks if you start the Firebase Emulator Suite.

### 2. Actionable Error Feedback (`Auth.tsx`)
- **[MODIFY]**: Catch the `auth/network-request-failed` error specifically.
- **[MODIFY]**: Display a targeted message advising the user to check their connection, ad-blockers, and system clock.

### 3. Environment Configuration (`.env.example`)
- **[MODIFY]**: Document the new `VITE_USE_FIREBASE_EMULATORS` flag.

## Troubleshooting Guide (For the User)

If you continue to see this error, please verify the following:
1.  **Firebase Console**: Ensure **Email/Password** authentication is enabled in the Authentication tab of your Firebase project (`brainbites-24332456`).
2.  **Authorized Domains**: Check that `localhost` is listed under "Authorized domains" in Firebase Auth settings.
3.  **Ad-blockers**: Disable extensions like Brave Shields, uBlock Origin, or AdBlock for this page.
4.  **System Time**: Ensure your computer's clock is synchronized with the internet time.
5.  **VPN**: If using a VPN, it might be interfering with Google API requests.

---

## Task List

- `[ ]` Update `firebaseService.ts` with Emulator support.
- `[ ]` Improve error handling in `Auth.tsx`.
- `[ ]` Verify build integrity with `npx tsc`.
- `[ ]` Provide instructions for creating the initial Admin credentials.

## Definition of Done
- [ ] Authentication logic supports both production and local emulator environments.
- [ ] Users receive specific troubleshooting steps for network failures.
- [ ] Admin Panel build remains clean and optimized.
