# BrainBites

Welcome to the BrainBites project!

## Architecture

This repository contains:
1. **User App (`/app`)**: The main Android application written in Kotlin and Jetpack Compose.
2. **Backend (`/functions`)**: Firebase Cloud Functions for trusted server-side mutations.
3. **Admin Panel (`/webBasedAdminPanel`)**: A React/Vite web application for managing content and users.

## Security & Secrets

This project strictly adheres to secure secret management protocols.

### Environment Setup
- **No secrets should be committed to the repository.**
- **Web Admin Panel**: Create a `.env` file in the `webBasedAdminPanel` directory based on the provided `.env.example`. This file is ignored by Git. Do NOT commit the real `.env` file.
- **Android App**: Ensure `google-services.json` is configured. This file only contains public identifiers for Firebase.
- **Firebase Functions**: The backend uses Application Default Credentials. Service Account keys are *not* required in the source code.

### 🚨 Git History Warning 🚨
If you are joining this project or reviewing the repository:
**Check the Git history.** If any developer previously hardcoded a secret (such as a Service Account Key, database URI, or API Secret) into the codebase, that value remains in the Git history even if it was deleted in a later commit.

**Action Required**: If you suspect a secret was previously hardcoded, **rotate that secret immediately** in the respective provider's dashboard (e.g., Firebase, Google Cloud, Cloudinary).

## Deployment

Refer to `PRODUCTION_DOCS.md` for detailed instructions on deploying the Firebase backend and Admin Panel.
