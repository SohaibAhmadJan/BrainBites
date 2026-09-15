# Implementation Plan - Firebase Storage to Cloudinary Pivot (Free Tier Fix)

Firebase recently changed their policy to require a "Blaze" (Pay-as-you-go) billing account equipped with a credit card just to initialize new Firebase Storage buckets, even if usage remains within the free tier. Since the user is strictly on the free plan, we are blocked from using Firebase Storage.

**The Solution:** We will pivot the image storage system to **Cloudinary**, a robust enterprise media service that offers an extremely generous, 100% free tier requiring NO credit card.

## Proposed Changes

### [Android User App]

#### [MODIFY] [build.gradle.kts](file:///F:/BrainBites/app/build.gradle.kts)
- Remove `implementation(libs.firebase.storage)` since we are abandoning Firebase Storage to avoid the billing requirement.

#### [MODIFY] [StorageRepository.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/data/StorageRepository.kt)
- Completely rewrite the `uploadProfilePicture` function.
- Instead of using the Firebase SDK, implement a native Android `HttpURLConnection` multipart POST request.
- The request will upload the image bytes directly to Cloudinary's unsigned upload endpoint: `https://api.cloudinary.com/v1_1/o884wjpk/image/upload`.
- It will use the preset `ml_default` (which matches the free Cloudinary config already discovered in the Admin Panel's `.env.example`).
- Parse the JSON response to extract the `secure_url` and return it.

## Result & Compatibility
The result is that images will be securely uploaded to a free CDN. The returned URL (`https://res.cloudinary.com/...`) is saved to Firestore as normal. The React Admin Panel and the Android App already know how to render public HTTP image URLs, so no other code needs to change!

## User Review Required

> [!IMPORTANT]
> Because Firebase is forcing a credit card requirement on new projects, I will completely remove Firebase Storage from the app and replace it with **Cloudinary**, a 100% free alternative that requires no credit card. Please approve this plan!