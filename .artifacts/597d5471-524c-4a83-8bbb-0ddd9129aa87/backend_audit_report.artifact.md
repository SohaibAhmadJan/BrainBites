# BrainBites Backend Audit \u0026 Proposed Schema Report

This report documents the current state of user data storage in the BrainBites Android application and proposes the finalized Firestore schema required for the BrainBitesAdmin panel.

## 1. Current Android User-Data Storage

| Data Category | Current Local Storage | Firestore Status |
| :--- | :--- | :--- |
| **Profile / Account** | `SharedPreferences` (PreferenceManager) | Synced to `users/{uid}/profile` |
| **Favorites** | `SharedPreferences` (BiteRepository) | Synced to `users/{uid}/favorites` |
| **Reading History** | `SharedPreferences` (BiteRepository) | Synced to `users/{uid}/history` |
| **Streak \u0026 Stats** | `SharedPreferences` (PreferenceManager) | Partial sync to `users/{uid}/stats` |
| **Achievement Progress**| Calculated in-memory (AchievementManager) | Synced to `users/{uid}/achievements` |
| **User Preferences** | `SharedPreferences` (PreferenceManager) | Synced to `users/{uid}/preferences` |
| **Notifications** | In-memory list (NotificationRepository) | Fetched from `notifications` (Global) \u0026 `users/{uid}/notifications` (Targeted) |
| **Quiz Results** | Not explicitly persisted | Logged to `analytics_events` |
| **Collection Progress** | Calculated in-memory | Not synced to Firestore yet |

---

## 2. Proposed Firestore Schema (Authoritative)

### A. Users Collection (`users/{uid}`)
This document stores the primary identity and state of the mobile user.

```json
{
  "account": {
    "uid": "string",
    "createdAt": "timestamp",
    "updatedAt": "timestamp",
    "lastLoginAt": "timestamp",
    "status": "ACTIVE | DISABLED"
  },
  "profile": {
    "displayName": "string",
    "email": "string",
    "photoUrl": "string",
    "bio": "string",
    "isPublic": "boolean"
  },
  "stats": {
    "streakCount": "number",
    "factsReadCount": "number",
    "favoritesCount": "number",
    "sharesCount": "number",
    "lastActiveAt": "timestamp"
  },
  "preferences": {
    "dailyGoal": "number",
    "textScale": "float",
    "hapticsEnabled": "boolean",
    "analyticsEnabled": "boolean",
    "notificationsEnabled": "boolean"
  }
}
```

### B. User Subcollections
- **`favorites/{factId}`**: `{ addedAt: timestamp }`
- **`history/{factId}`**: `{ viewedAt: timestamp, completed: boolean }`
- **`achievements/{achId}`**: `{ progress: number, unlockedAt: timestamp, adminOverride: boolean }`
- **`collectionProgress/{collId}`**: `{ progress: float, lastUpdated: timestamp }`
- **`quizResults/{factId}`**: `{ isCorrect: boolean, attemptedAt: timestamp, answerIndex: number }`
- **`notifications/{notifId}`**: Targeted notifications for this specific user.

---

## 3. Administrator Architecture (`admins/{uid}`)

The Admin panel will use this collection for Role-Based Access Control (RBAC).

| Field | Description |
| :--- | :--- |
| **email** | Admin email address |
| **displayName** | Name for audit logs |
| **role** | `SUPER_ADMIN`, `ADMIN`, `CONTENT_MANAGER`, `ANALYST` |
| **permissions** | Array of granular strings (e.g., `["users.edit", "facts.publish"]`) |
| **isActive** | Boolean to disable access immediately |

---

## 4. Collection Reuse \u0026 Requirements

### Reusable Collections (KEEP)
- **`audit_logs`**: Will be preserved and used for ALL administrative actions performed via BrainBitesAdmin.
- **`achievements`**: Global definitions remain here.
- **`app_config`**: Unified remote configuration.
- **`facts`, `quizzes`, `categories`, `collections`**: Content collections are fully operational.

### Genuinely Required New Structures
1. **`admins`**: Required for secure access to the Admin Panel.
2. **`analytics_events`**: To store user behavior metrics (views, shares, quiz attempts) for the Admin Dashboard.
3. **Targeted Notifications**: The `users/{uid}/notifications` subcollection to allow admins to message specific users.

---

## 5. Authentication Requirements

1. **Anonymous Auth**: Required for the Android app to create a `uid` without forcing immediate login.
2. **Email/Google Auth**: Required for Admin Panel access and for mobile users wanting cross-device sync.
3. **Backend Security**: Firestore Rules will enforce that only `SUPER_ADMIN` can modify the `admins` collection.
