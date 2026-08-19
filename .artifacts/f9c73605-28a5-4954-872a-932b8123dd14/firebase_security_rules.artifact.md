# BrainBites Production Security Rules

Copy and paste these rules into your **Firebase Console** to secure your production data.

## 1. Firestore Security Rules
Go to **Firestore Database** -> **Rules** and paste this:

```javascript
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {

    // Check if the request is from a logged-in admin
    function isAdmin() {
      return request.auth != null && request.auth.token.email == "sohaibahmedjan930@gmail.com";
    }

    // Facts and Collections: Public read, Admin write
    match /facts/{factId} {
      allow read: if true;
      allow write: if isAdmin();
    }

    match /collections/{colId} {
      allow read: if true;
      allow write: if isAdmin();
    }

    // App Settings: Public read, Admin write
    match /app_settings/global_config {
      allow read: if true;
      allow write: if isAdmin();
    }

    // Users and Activity: Private to user, Admin can read everything
    match /users/{userId} {
      allow read: if request.auth != null && (request.auth.uid == userId || isAdmin());
      allow write: if request.auth != null && (request.auth.uid == userId || isAdmin());
    }

    match /user_activity/{actId} {
      allow read: if isAdmin();
      allow create: if request.auth != null;
    }

    // Notifications and Audit Logs: Admin only
    match /notifications/{notId} {
      allow read, write: if isAdmin();
    }

    match /audit_logs/{logId} {
      allow read, write: if isAdmin();
    }
  }
}
```

## 2. Firebase Storage Rules
Go to **Storage** -> **Rules** and paste this:

```javascript
rules_version = '2';

service firebase.storage {
  match /b/{bucket}/o {

    function isAdmin() {
      return request.auth != null && request.auth.token.email == "sohaibahmedjan930@gmail.com";
    }

    // Media folder: Public read, Admin write
    match /media/{allPaths=**} {
      allow read: if true;
      allow write: if isAdmin();
    }

    // User uploads (if any): Private
    match /users/{userId}/{allPaths=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

> [!CAUTION]
> **Warning**: These rules strictly limit write access to your specific email address. Ensure your email is correct in the `isAdmin()` function before publishing!
