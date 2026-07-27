# Implementation Plan - Functional Notifications

The user wants to make the notification bell icon functional. I will implement a **Notifications Screen** that displays a list of app-related updates and integrate it into the existing navigation.

## User Review Required

> [!NOTE]
> I am adding a new `NotificationsScreen` accessible from the top bar across all root-level screens. This screen will show updates such as "New Fact of the Day," "Achievement Milestones," and "Daily Reminders."

## Proposed Questions
- Do you want actual Android System Notifications (push notifications) or just an in-app notifications center?
- For this iteration, I'll focus on an **In-app Notification Center**.

## Proposed Changes

### Navigation & Architecture

#### [MODIFY] [Screen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/navigation/Screen.kt)
- Add `object Notifications : Screen("notifications_screen")` to the `Screen` sealed class.

#### [MODIFY] [BrainBitesNavGraph.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/navigation/BrainBitesNavGraph.kt)
- Register `Screen.Notifications.route` in the nested `NavHost`.
- Provide a `NotificationsScreen` composable linked to a new `NotificationsViewModel`.

### Data Layer

#### [NEW] [Notification.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/data/Notification.kt)
- Define a `Notification` data class with fields: `id`, `title`, `message`, `timestamp`, and `isRead`.

#### [NEW] [NotificationRepository.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/data/NotificationRepository.kt)
- Manage a list of in-app notifications.
- Provide functions to mark notifications as read and clear them.

### UI Components

#### [NEW] [NotificationsViewModel.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/notifications/NotificationsViewModel.kt)
- Manage the state of the notifications list and handle "mark as read" logic.

#### [NEW] [NotificationsScreen.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/notifications/NotificationsScreen.kt)
- Design a clean, list-based UI for notifications.
- Include "Mark all as read" and "Clear all" actions.

#### [MODIFY] [MainScaffold.kt](file:///F:/BrainBites/app/src/main/java/com/example/brainbites/ui/main/MainScaffold.kt)
- Update the notification `IconButton` click listener to navigate to `Screen.Notifications.route`.
- Update `currentTitle` to display "Notifications" when on that screen.
- Hide the notification icon from the top bar when the user is already on the Notifications screen.

## Verification Plan

### Automated Tests
- Run `gradle_build app:assembleDebug` to ensure all new components and navigation links are valid.

### Manual Verification
- Deploy the app and verify:
    - Clicking the bell icon from Home, Explore, or Saved leads to the Notifications screen.
    - The "Notifications" title appears correctly.
    - The bell icon disappears when on the Notifications screen.
    - Back navigation works correctly from the Notifications screen.
